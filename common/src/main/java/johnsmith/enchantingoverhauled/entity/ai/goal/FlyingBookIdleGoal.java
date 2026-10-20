package johnsmith.enchantingoverhauled.entity.ai.goal;

import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.EnchantedBookEntity;
import johnsmith.enchantingoverhauled.entity.EnchantedTomeEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * AI Goal that directs idle {@link AbstractFlyingBookEntity} instances to roost on nearby
 * Enchanting Altars, empty Lecterns, or Bookshelves, or perform periodic daytime/minion scouting.
 * <p>
 * Optimized to scan only loaded chunks using {@link LevelChunkSection#maybeHas(Predicate)}
 * palette filtering and a single-pass 3D evaluation with lazy entity-claim pre-fetching.
 */
public class FlyingBookIdleGoal extends Goal {
    private static final int BOREDOM_THRESHOLD_TICKS = 400;
    private static final int SCOUTING_DURATION_TICKS = 240;

    /**
     * Sensible bounds on horizontal and vertical roost searches to prevent cross-chunk thrashing
     * while still covering full library rooms and multi-story structures.
     */
    private static final int MAX_HORIZONTAL_SEARCH_RANGE = 24;
    private static final int MAX_VERTICAL_SEARCH_RANGE = 12;

    /**
     * Reusable section-palette predicate that checks whether a 16x16x16 chunk section
     * contains any block capable of acting as a roost target in O(1) palette time.
     */
    private static final Predicate<BlockState> ROOST_STATE_PREDICATE = FlyingBookIdleGoal::isPossibleRoostState;

    private final AbstractFlyingBookEntity book;
    private final double speedModifier;
    private final int searchRange;
    private final int verticalSearchRange;
    private BlockPos targetPos;
    private RoostTier activeTier = RoostTier.NONE;
    private int scoutingTimer = 0;
    private int scanCooldown = 0;

    private enum RoostTier {
        ALTAR_VIGIL,
        EMPTY_LECTERN,
        BOOKSHELF_PERUSAL,
        ACTIVE_SCOUTING,
        NONE
    }

    public FlyingBookIdleGoal(AbstractFlyingBookEntity book, double speedModifier, int searchRange, int verticalSearchRange) {
        this.book = book;
        this.speedModifier = speedModifier;
        this.searchRange = Math.clamp(searchRange, 1, MAX_HORIZONTAL_SEARCH_RANGE);
        this.verticalSearchRange = Math.clamp(verticalSearchRange, 1, MAX_VERTICAL_SEARCH_RANGE);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.book.getTarget() != null
                || this.book.isAngry()
                || this.book.getAnimationState().isChanting()
                || this.book.getAnimationState() == BookAnimationState.BARTERING) {
            return false;
        }
        if (this.scanCooldown > 0) {
            this.scanCooldown--;
            return false;
        }
        if (this.shouldPerformScouting()) {
            this.activeTier = RoostTier.ACTIVE_SCOUTING;
            this.scoutingTimer = SCOUTING_DURATION_TICKS;
            this.targetPos = this.findRandomScoutingPos();
            if (this.targetPos != null) {
                return true;
            }
            this.scanCooldown = 40 + this.book.getRandom().nextInt(40);
            return false;
        }
        if (this.findBestRoostTarget()) {
            return true;
        }
        // Longer cooldown when no roost target is found nearby to avoid redundant scans in open terrain
        this.scanCooldown = 60 + this.book.getRandom().nextInt(60);
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.book.getTarget() != null
                || this.book.isAngry()
                || this.book.getAnimationState().isChanting()
                || this.book.getAnimationState() == BookAnimationState.BARTERING) {
            return false;
        }
        if (this.activeTier == RoostTier.ALTAR_VIGIL
                && this.book instanceof EnchantedTomeEntity tome
                && (tome.isAltarDeceptionExpired() || tome.getAltarAbandonCooldown() > 0)) {
            return false;
        }
        if (this.activeTier == RoostTier.ACTIVE_SCOUTING) {
            if (this.book instanceof EnchantedBookEntity) {
                return this.shouldPerformScouting() && this.targetPos != null;
            }
            return this.scoutingTimer > 0 && this.targetPos != null;
        }
        if (this.shouldPerformScouting()) {
            return false;
        }
        if (this.targetPos == null) {
            return false;
        }
        // Throttle the entity-claim AABB lookup to once every 20 ticks (staggered by entity ID)
        boolean verifyClaims = (this.book.tickCount + this.book.getId()) % 20 == 0;
        return this.isValidTierBlock(this.book.level(), this.targetPos, this.activeTier, verifyClaims);
    }

    @Override
    public void start() {
        if (this.activeTier == RoostTier.ACTIVE_SCOUTING) {
            this.book.setRoostPos(null);
            this.book.setAnimationState(BookAnimationState.SCOUTING);
        } else {
            this.book.setRoostPos(this.targetPos);
            this.book.setAnimationState(BookAnimationState.IDLE);
        }
        this.moveToApproachTarget();
    }

    @Override
    public void stop() {
        if (this.book.getAnimationState() != BookAnimationState.BARTERING) {
            this.book.setRoostPos(null);
        }
        if (this.book instanceof EnchantedTomeEntity tome && this.activeTier == RoostTier.ACTIVE_SCOUTING) {
            tome.setBoredomTicks(0);
        }
        this.targetPos = null;
        this.activeTier = RoostTier.NONE;
        this.scoutingTimer = 0;
        if (this.book.getAnimationState() != BookAnimationState.BARTERING) {
            this.book.setAnimationState(BookAnimationState.IDLE);
        }
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        if (this.activeTier == RoostTier.ACTIVE_SCOUTING) {
            this.scoutingTimer--;
            this.book.setAnimationState(BookAnimationState.SCOUTING);
            Vec3 dest = Vec3.atCenterOf(this.targetPos);
            if (this.book.distanceToSqr(dest) <= 2.25D || this.book.getNavigation().isDone()) {
                BlockPos nextPos = this.findRandomScoutingPos();
                if (nextPos != null) {
                    this.targetPos = nextPos;
                    this.moveToApproachTarget();
                }
            }
            return;
        }

        this.book.setRoostPos(this.targetPos);

        Vec3 hoverSpot = this.getHoverPosition(this.targetPos, this.activeTier);
        Vec3 approachSpot = this.getOverheadApproachPosition(this.targetPos);

        double dx = (this.targetPos.getX() + 0.5D) - this.book.getX();
        double dz = (this.targetPos.getZ() + 0.5D) - this.book.getZ();
        double horizDistSqr = dx * dx + dz * dz;

        boolean isAlreadyRoosted = this.book.isRoosting();
        boolean isAboveTargetBlock = this.book.getY() >= (this.targetPos.getY() + 0.72D);
        boolean withinOverheadColumn = (horizDistSqr <= 0.45D && isAboveTargetBlock && this.book.getY() <= this.targetPos.getY() + 2.35D)
                || (isAlreadyRoosted && horizDistSqr <= 0.85D && isAboveTargetBlock);

        if (withinOverheadColumn) {
            this.book.getNavigation().stop();
            Vec3 toHover = hoverSpot.subtract(this.book.position());
            double distToHoverSqr = toHover.lengthSqr();

            this.book.setDeltaMovement(toHover.scale(0.32D));

            if (distToHoverSqr <= 0.65D || isAlreadyRoosted) {
                if (this.book instanceof EnchantedTomeEntity tome) {
                    this.tickDaytimeBoredom(tome);
                }

                switch (this.activeTier) {
                    case ALTAR_VIGIL -> this.book.setAnimationState(BookAnimationState.ALTAR_VIGIL);
                    case EMPTY_LECTERN -> {
                        this.book.setAnimationState(BookAnimationState.LECTERN_ROOST);
                        BlockState state = this.book.level().getBlockState(this.targetPos);
                        if (state.hasProperty(LecternBlock.FACING)) {
                            Direction facing = state.getValue(LecternBlock.FACING);
                            float yaw = facing.toYRot();
                            this.book.setYRot(yaw);
                            this.book.yBodyRot = yaw;
                            this.book.yHeadRot = yaw;
                        }
                    }
                    case BOOKSHELF_PERUSAL -> {
                        this.book.setAnimationState(BookAnimationState.RESTING);
                        this.book.getLookControl().setLookAt(
                                this.targetPos.getX() + 0.5D,
                                this.targetPos.getY() + 0.5D,
                                this.targetPos.getZ() + 0.5D,
                                10.0F,
                                this.book.getMaxHeadXRot()
                        );
                    }
                    default -> this.book.setAnimationState(BookAnimationState.IDLE);
                }
            } else {
                this.book.setAnimationState(BookAnimationState.IDLE);
            }
        } else {
            this.book.setAnimationState(BookAnimationState.IDLE);
            if (horizDistSqr <= 2.25D && this.book.getY() >= this.targetPos.getY() + 0.85D) {
                this.book.getNavigation().stop();
                this.book.getMoveControl().setWantedPosition(approachSpot.x, approachSpot.y, approachSpot.z, this.speedModifier);
            } else if (this.book.getNavigation().isDone() || this.book.tickCount % 15 == 0) {
                this.moveToApproachTarget();
            }
        }
    }

    /**
     * Scans only loaded chunks within range, skipping any 16x16x16 chunk section whose palette
     * does not contain an Altar, Lectern, or Bookshelf, and resolves the highest-priority
     * closest roost in a single pass.
     */
    private boolean findBestRoostTarget() {
        Level level = this.book.level();
        BlockPos origin = this.book.blockPosition();
        boolean canSeekAltar = this.book instanceof EnchantedTomeEntity tome && tome.getAltarAbandonCooldown() <= 0;

        if (canSeekAltar) {
            BlockPos existingRoost = this.book.getRoostPos();
            if (existingRoost != null && this.isValidTierBlock(level, existingRoost, RoostTier.ALTAR_VIGIL, true)) {
                this.targetPos = existingRoost;
                this.activeTier = RoostTier.ALTAR_VIGIL;
                return true;
            }
        }

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        int minX = originX - this.searchRange;
        int maxX = originX + this.searchRange;
        int minY = Math.max(level.getMinBuildHeight(), originY - this.verticalSearchRange);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, originY + this.verticalSearchRange);
        int minZ = originZ - this.searchRange;
        int maxZ = originZ + this.searchRange;

        if (minY > maxY) {
            return false;
        }

        int minChunkX = SectionPos.blockToSectionCoord(minX);
        int maxChunkX = SectionPos.blockToSectionCoord(maxX);
        int minChunkZ = SectionPos.blockToSectionCoord(minZ);
        int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);

        BlockPos bestAltarPos = null;
        int bestAltarDistSqr = Integer.MAX_VALUE;

        BlockPos bestLecternPos = null;
        int bestLecternDistSqr = Integer.MAX_VALUE;

        BlockPos bestShelfPos = null;
        int bestShelfDistSqr = Integer.MAX_VALUE;

        List<AbstractFlyingBookEntity> nearbyClaimants = null;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        ChunkSource chunkSource = level.getChunkSource();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                // Only inspect already-loaded chunks; never trigger synchronous chunk loads or tickets
                LevelChunk chunk = chunkSource.getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }

                int chunkStartX = Math.max(minX, cx << 4);
                int chunkEndX = Math.min(maxX, (cx << 4) + 15);
                int chunkStartZ = Math.max(minZ, cz << 4);
                int chunkEndZ = Math.min(maxZ, (cz << 4) + 15);

                int minSectionIdx = chunk.getSectionIndex(minY);
                int maxSectionIdx = chunk.getSectionIndex(maxY);
                LevelChunkSection[] sections = chunk.getSections();

                for (int sectionIdx = minSectionIdx; sectionIdx <= maxSectionIdx; sectionIdx++) {
                    if (sectionIdx < 0 || sectionIdx >= sections.length) {
                        continue;
                    }
                    LevelChunkSection section = sections[sectionIdx];
                    if (section == null || section.hasOnlyAir() || !section.maybeHas(ROOST_STATE_PREDICATE)) {
                        continue;
                    }

                    int sectionBottomY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIdx));
                    int sectionStartY = Math.max(minY, sectionBottomY);
                    int sectionEndY = Math.min(maxY, sectionBottomY + 15);

                    for (int y = sectionStartY; y <= sectionEndY; y++) {
                        int dy = y - originY;
                        int dySqr = dy * dy;
                        int localY = y & 15;

                        for (int x = chunkStartX; x <= chunkEndX; x++) {
                            int dx = x - originX;
                            int dxDySqr = dx * dx + dySqr;
                            int localX = x & 15;

                            for (int z = chunkStartZ; z <= chunkEndZ; z++) {
                                int localZ = z & 15;
                                BlockState state = section.getBlockState(localX, localY, localZ);
                                if (state.isAir()) {
                                    continue;
                                }

                                // 1. Classify block state first (cheapest check) and skip lower tiers once a higher tier is found
                                RoostTier candidateTier = classifyCandidateState(
                                        state,
                                        canSeekAltar,
                                        bestAltarPos != null,
                                        bestLecternPos != null
                                );
                                if (candidateTier == RoostTier.NONE) {
                                    continue;
                                }

                                // 2. Check squared distance against the best candidate of the same tier
                                int dz = z - originZ;
                                int distSqr = dxDySqr + dz * dz;
                                if (candidateTier == RoostTier.ALTAR_VIGIL && distSqr >= bestAltarDistSqr) {
                                    continue;
                                }
                                if (candidateTier == RoostTier.EMPTY_LECTERN && distSqr >= bestLecternDistSqr) {
                                    continue;
                                }
                                if (candidateTier == RoostTier.BOOKSHELF_PERUSAL && distSqr >= bestShelfDistSqr) {
                                    continue;
                                }

                                // 3. Verify that the block directly above is air using direct section access
                                if (!isAboveAir(sections, section, sectionIdx, localX, localY, localZ)) {
                                    continue;
                                }

                                // 4. Lazily fetch active roost claimants once per scan only when a valid block is found
                                if (nearbyClaimants == null) {
                                    AABB claimBox = new AABB(origin).inflate(
                                            this.searchRange * 2.0D + 4.0D,
                                            this.verticalSearchRange * 2.0D + 4.0D,
                                            this.searchRange * 2.0D + 4.0D
                                    );
                                    nearbyClaimants = level.getEntitiesOfClass(
                                            AbstractFlyingBookEntity.class,
                                            claimBox,
                                            other -> other != this.book && other.isAlive() && other.getRoostPos() != null
                                    );
                                }

                                mutablePos.set(x, y, z);
                                if (this.isClaimedByHigherPriorityBook(mutablePos, nearbyClaimants)) {
                                    continue;
                                }

                                BlockPos immutableCandidate = mutablePos.immutable();
                                switch (candidateTier) {
                                    case ALTAR_VIGIL -> {
                                        bestAltarPos = immutableCandidate;
                                        bestAltarDistSqr = distSqr;
                                    }
                                    case EMPTY_LECTERN -> {
                                        bestLecternPos = immutableCandidate;
                                        bestLecternDistSqr = distSqr;
                                    }
                                    case BOOKSHELF_PERUSAL -> {
                                        bestShelfPos = immutableCandidate;
                                        bestShelfDistSqr = distSqr;
                                    }
                                    default -> {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bestAltarPos != null) {
            this.targetPos = bestAltarPos;
            this.activeTier = RoostTier.ALTAR_VIGIL;
            return true;
        }
        if (bestLecternPos != null) {
            this.targetPos = bestLecternPos;
            this.activeTier = RoostTier.EMPTY_LECTERN;
            return true;
        }
        if (bestShelfPos != null) {
            this.targetPos = bestShelfPos;
            this.activeTier = RoostTier.BOOKSHELF_PERUSAL;
            return true;
        }
        return false;
    }

    private static boolean isPossibleRoostState(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Tags.ENCHANTED_BOOK_IDLE_TARGET)) {
            return true;
        }
        if (state.getBlock() instanceof LecternBlock) {
            return !state.hasProperty(LecternBlock.HAS_BOOK) || !state.getValue(LecternBlock.HAS_BOOK);
        }
        return state.is(Blocks.DEACTIVATED_ENCHANTING_TABLE.build())
                || state.is(Blocks.DISTURBED_ENCHANTING_TABLE.build());
    }

    private static RoostTier classifyCandidateState(
            BlockState state,
            boolean canSeekAltar,
            boolean hasFoundAltar,
            boolean hasFoundLectern
    ) {
        if (canSeekAltar
                && (state.is(Blocks.DEACTIVATED_ENCHANTING_TABLE.build())
                || state.is(Blocks.DISTURBED_ENCHANTING_TABLE.build()))) {
            return RoostTier.ALTAR_VIGIL;
        }
        if (hasFoundAltar) {
            return RoostTier.NONE;
        }
        if (state.getBlock() instanceof LecternBlock) {
            if (state.hasProperty(LecternBlock.HAS_BOOK) && !state.getValue(LecternBlock.HAS_BOOK)) {
                return RoostTier.EMPTY_LECTERN;
            }
            return RoostTier.NONE;
        }
        if (hasFoundLectern) {
            return RoostTier.NONE;
        }
        if (state.is(Tags.ENCHANTED_BOOK_IDLE_TARGET)) {
            return RoostTier.BOOKSHELF_PERUSAL;
        }
        return RoostTier.NONE;
    }

    private static boolean isAboveAir(
            LevelChunkSection[] sections,
            LevelChunkSection currentSection,
            int sectionIdx,
            int localX,
            int localY,
            int localZ
    ) {
        if (localY < 15) {
            return currentSection.getBlockState(localX, localY + 1, localZ).isAir();
        }
        int nextSectionIdx = sectionIdx + 1;
        if (nextSectionIdx >= sections.length) {
            return true;
        }
        LevelChunkSection aboveSection = sections[nextSectionIdx];
        return aboveSection == null
                || aboveSection.hasOnlyAir()
                || aboveSection.getBlockState(localX, 0, localZ).isAir();
    }

    private boolean isValidTierBlock(Level level, BlockPos pos, RoostTier tier, boolean checkClaims) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );
        if (chunk == null) {
            return false;
        }
        // 1. Check the block state itself first before checking air above or entity claims
        BlockState state = chunk.getBlockState(pos);
        boolean matchesState = switch (tier) {
            case ALTAR_VIGIL -> state.is(Blocks.DEACTIVATED_ENCHANTING_TABLE.build())
                    || state.is(Blocks.DISTURBED_ENCHANTING_TABLE.build());
            case EMPTY_LECTERN -> state.getBlock() instanceof LecternBlock
                    && state.hasProperty(LecternBlock.HAS_BOOK)
                    && !state.getValue(LecternBlock.HAS_BOOK);
            case BOOKSHELF_PERUSAL -> state.is(Tags.ENCHANTED_BOOK_IDLE_TARGET)
                    && !(state.getBlock() instanceof LecternBlock);
            default -> false;
        };
        if (!matchesState) {
            return false;
        }
        // 2. Verify air above within the same loaded chunk
        if (!chunk.getBlockState(pos.above()).isAir()) {
            return false;
        }
        // 3. Only run the entity claim query when requested
        return !checkClaims || !this.isClaimedByHigherPriorityBook(level, pos);
    }

    private boolean isClaimedByHigherPriorityBook(BlockPos pos, List<AbstractFlyingBookEntity> nearbyClaimants) {
        for (AbstractFlyingBookEntity other : nearbyClaimants) {
            if (pos.equals(other.getRoostPos()) && this.shouldYieldTo(other, pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClaimedByHigherPriorityBook(Level level, BlockPos pos) {
        AABB searchBox = new AABB(pos).inflate(this.searchRange + 4.0D);
        List<AbstractFlyingBookEntity> claimants = level.getEntitiesOfClass(
                AbstractFlyingBookEntity.class,
                searchBox,
                other -> other != this.book && other.isAlive() && pos.equals(other.getRoostPos())
        );
        return this.isClaimedByHigherPriorityBook(pos, claimants);
    }

    private boolean shouldYieldTo(AbstractFlyingBookEntity other, BlockPos pos) {
        if (other instanceof EnchantedTomeEntity && !(this.book instanceof EnchantedTomeEntity)) {
            return true;
        }
        if (this.book instanceof EnchantedTomeEntity && !(other instanceof EnchantedTomeEntity)) {
            return false;
        }
        boolean otherRoosted = other.isRoosting();
        boolean selfRoosted = this.book.isRoosting();
        if (otherRoosted && !selfRoosted) {
            return true;
        }
        if (selfRoosted && !otherRoosted) {
            return false;
        }
        Vec3 center = Vec3.atCenterOf(pos.above());
        double selfDist = this.book.distanceToSqr(center);
        double otherDist = other.distanceToSqr(center);
        if (Math.abs(selfDist - otherDist) > 0.25D) {
            return otherDist < selfDist;
        }
        return other.getId() < this.book.getId();
    }

    private Vec3 getOverheadApproachPosition(BlockPos pos) {
        boolean hasTwoBlocksHeadroom = this.book.level().isEmptyBlock(pos.above(2));
        double approachY = hasTwoBlocksHeadroom ? (pos.getY() + 1.6D) : (pos.getY() + 1.25D);
        return new Vec3(pos.getX() + 0.5D, approachY, pos.getZ() + 0.5D);
    }

    private Vec3 getHoverPosition(BlockPos pos, RoostTier tier) {
        return switch (tier) {
            case ALTAR_VIGIL -> new Vec3(pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D);
            case EMPTY_LECTERN -> new Vec3(pos.getX() + 0.5D, pos.getY() + 0.98D, pos.getZ() + 0.5D);
            default -> new Vec3(pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D);
        };
    }

    private void moveToApproachTarget() {
        if (this.targetPos == null) {
            return;
        }
        Vec3 dest = this.activeTier == RoostTier.ACTIVE_SCOUTING
                ? Vec3.atCenterOf(this.targetPos)
                : this.getOverheadApproachPosition(this.targetPos);
        this.book.getNavigation().moveTo(dest.x, dest.y, dest.z, this.speedModifier);
    }

    private void tickDaytimeBoredom(EnchantedTomeEntity tome) {
        if (this.book.level().isDay() && this.book.level().getNearestPlayer(this.book, 16.0D) == null) {
            tome.setBoredomTicks(tome.getBoredomTicks() + 1);
        } else {
            tome.setBoredomTicks(0);
        }
    }

    private boolean shouldPerformScouting() {
        if (this.book instanceof EnchantedTomeEntity tome) {
            return this.book.level().isDay() && tome.getBoredomTicks() >= BOREDOM_THRESHOLD_TICKS;
        }
        if (this.book instanceof EnchantedBookEntity minion) {
            if (minion.getCommanderUuid().isPresent()) {
                return true;
            }
            return !this.book.level().isDay();
        }
        return false;
    }

    private BlockPos findRandomScoutingPos() {
        BlockPos origin = this.book.blockPosition();
        if (this.book instanceof EnchantedBookEntity minion && minion.getHomeShelfPos() != null) {
            origin = minion.getHomeShelfPos();
        }
        Level level = this.book.level();
        ChunkSource chunkSource = level.getChunkSource();
        for (int i = 0; i < 12; i++) {
            int dx = this.book.getRandom().nextInt(17) - 8;
            int dy = this.book.getRandom().nextInt(7) - 2;
            int dz = this.book.getRandom().nextInt(17) - 8;
            BlockPos candidate = origin.offset(dx, dy, dz);
            LevelChunk chunk = chunkSource.getChunkNow(
                    SectionPos.blockToSectionCoord(candidate.getX()),
                    SectionPos.blockToSectionCoord(candidate.getZ())
            );
            if (chunk != null
                    && chunk.getBlockState(candidate).isAir()
                    && chunk.getBlockState(candidate.below()).isAir()) {
                return candidate;
            }
        }
        return null;
    }
}