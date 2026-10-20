package johnsmith.enchantingoverhauled.entity.ai.goal;

import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.entity.EnchantedBookEntity;
import johnsmith.enchantingoverhauled.entity.EnchantedTomeEntity;
import johnsmith.enchantingoverhauled.entity.Entities;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * AI Goal that directs {@link EnchantedTomeEntity} instances to approach nearby bookshelves
 * and summon {@link EnchantedBookEntity} minions.
 * <p>
 * Optimized to throttle combat scan attempts, filter loaded chunk sections via
 * {@link LevelChunkSection#maybeHas(Predicate)}, and cap 3D A* pathfinding checks
 * to at most the 3 closest non-visible shelves.
 */
public class TomeSummonMinionsGoal extends Goal {
    private static final int MAX_ACTIVE_MINIONS = 3;
    private static final int MIN_SUMMON_COUNT = 1;
    private static final int MAX_SUMMON_COUNT = 3;
    private static final int MAX_APPROACH_TICKS = 160;
    private static final int STUCK_REPATH_TICKS = 30;

    private static final int HORIZONTAL_SEARCH_RANGE = 20;
    private static final int VERTICAL_SEARCH_RANGE = 10;
    private static final int MAX_PATHFIND_CANDIDATES = 3;
    private static final int LOS_CHECK_INTERVAL_TICKS = 10;

    /**
     * Reusable section-palette predicate to skip 16x16x16 chunk sections that contain no shelves.
     */
    private static final Predicate<BlockState> SHELF_STATE_PREDICATE =
            state -> !state.isAir() && state.is(Tags.ENCHANTED_BOOK_IDLE_TARGET);

    private final EnchantedTomeEntity mob;
    private final double speedModifier;
    @Nullable
    private BlockPos targetBlock;
    @Nullable
    private BlockPos approachAirPos;
    private int cooldown = 0;
    private int chantTicks = 0;
    private int approachTicks = 0;
    private int stuckTicks = 0;
    private int losCheckCooldown = 0;
    private boolean cachedShelfLoS = false;
    private double bestDistSqr = Double.MAX_VALUE;
    private boolean isChanting = false;
    private boolean combatSummon = false;
    private boolean finished = false;

    private record ShelfCandidate(BlockPos shelfPos, BlockPos openPos, double distSqr) {}

    public TomeSummonMinionsGoal(EnchantedTomeEntity mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }

        LivingEntity combatTarget = this.mob.getTarget();
        boolean inCombat = (combatTarget != null && combatTarget.isAlive()) || this.mob.isAngry();
        if (!inCombat) {
            if (this.mob.level().isDay() || this.mob.isRoosting()) {
                return false;
            }
            if (this.mob.getAnimationState() == BookAnimationState.BARTERING) {
                return false;
            }
            if (this.mob.getRandom().nextInt(60) != 0) {
                return false;
            }
        } else if ((this.mob.tickCount + this.mob.getId()) % 20 != 0) {
            // Throttle combat summon checks to once per second per Tome (staggered by entity ID)
            return false;
        }

        List<EnchantedBookEntity> activeMinions = this.getActiveMinions();
        if (activeMinions.size() >= MAX_ACTIVE_MINIONS) {
            this.cooldown = 40 + this.mob.getRandom().nextInt(40);
            return false;
        }

        ShelfCandidate resolved = this.resolveReachableTargetShelf(null);
        if (resolved != null) {
            this.targetBlock = resolved.shelfPos();
            this.approachAirPos = resolved.openPos();
            this.combatSummon = inCombat;
            return true;
        }

        // Back off when no reachable shelf exists nearby to prevent per-tick scans in combat
        this.cooldown = 40 + this.mob.getRandom().nextInt(40);
        return false;
    }

    @Override
    public void start() {
        this.chantTicks = 0;
        this.approachTicks = 0;
        this.stuckTicks = 0;
        this.losCheckCooldown = 0;
        this.cachedShelfLoS = false;
        this.bestDistSqr = Double.MAX_VALUE;
        this.isChanting = false;
        this.finished = false;
        this.mob.setRoostPos(null);
        this.mob.setCastSchoolKey(null);
        this.mob.setAnimationState(BookAnimationState.SCOUTING);
        this.recalculate3DPath();
    }

    @Override
    public void tick() {
        if (this.targetBlock == null || !this.isShelfBlockLoaded(this.targetBlock)) {
            this.finished = true;
            return;
        }

        Vec3 shelfCenter = Vec3.atCenterOf(this.targetBlock);
        Vec3 dockTarget = this.approachAirPos != null
                ? Vec3.atCenterOf(this.approachAirPos)
                : shelfCenter.add(0.0D, 1.0D, 0.0D);

        double distToDockSqr = this.mob.distanceToSqr(dockTarget);
        double maxChantDistSqr = this.combatSummon ? 25.0D : 6.25D;

        if (--this.losCheckCooldown <= 0 || (!this.isChanting && distToDockSqr <= maxChantDistSqr)) {
            this.losCheckCooldown = LOS_CHECK_INTERVAL_TICKS;
            this.cachedShelfLoS = this.hasLineOfSightToShelf(this.targetBlock, this.approachAirPos);
        }

        if (!this.isChanting) {
            this.approachTicks++;
            if (this.approachTicks >= MAX_APPROACH_TICKS) {
                this.finished = true;
                return;
            }

            if (this.cachedShelfLoS && distToDockSqr <= maxChantDistSqr) {
                this.isChanting = true;
                this.mob.getNavigation().stop();
                this.mob.setCastSchoolKey(null);
                this.mob.setAnimationState(BookAnimationState.CHANTING);
                return;
            }

            if (distToDockSqr < this.bestDistSqr - 0.15D) {
                this.bestDistSqr = distToDockSqr;
                this.stuckTicks = 0;
            } else {
                this.stuckTicks++;
            }

            if (this.stuckTicks >= STUCK_REPATH_TICKS) {
                this.stuckTicks = 0;
                this.bestDistSqr = Double.MAX_VALUE;
                ShelfCandidate altShelf = this.resolveReachableTargetShelf(this.targetBlock);
                if (altShelf != null) {
                    this.targetBlock = altShelf.shelfPos();
                    this.approachAirPos = altShelf.openPos();
                    this.losCheckCooldown = 0;
                } else {
                    this.finished = true;
                    return;
                }
                this.recalculate3DPath();
            } else if (this.mob.getNavigation().isDone() || this.approachTicks % 15 == 0) {
                this.recalculate3DPath();
            }

            Vec3 nextWaypoint = this.resolveNext3DWaypoint(dockTarget);
            Vec3 dir = this.apply3DObstacleAvoidance(nextWaypoint.subtract(this.mob.position()));
            Vec3 steerTarget = this.mob.position().add(dir.scale(2.2D));
            this.mob.getMoveControl().setWantedPosition(steerTarget.x, steerTarget.y, steerTarget.z, this.speedModifier);
            this.mob.getLookControl().setLookAt(shelfCenter.x, shelfCenter.y, shelfCenter.z, 30.0F, 30.0F);
        } else {
            if (!this.cachedShelfLoS) {
                this.isChanting = false;
                this.mob.setAnimationState(BookAnimationState.SCOUTING);
                this.recalculate3DPath();
                return;
            }

            this.mob.setDeltaMovement(Vec3.ZERO);
            LivingEntity target = this.mob.getTarget();
            if (target != null && target.isAlive()) {
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            } else {
                this.mob.getLookControl().setLookAt(shelfCenter.x, shelfCenter.y, shelfCenter.z, 30.0F, 30.0F);
            }

            this.chantTicks++;
            int requiredTicks = this.combatSummon ? 40 : 60;
            if (this.chantTicks >= requiredTicks) {
                this.executeSummon();
            }
        }
    }

    private void executeSummon() {
        if (this.mob.level() instanceof ServerLevel serverLevel
                && this.targetBlock != null
                && this.isShelfBlockLoaded(this.targetBlock)) {
            int currentCount = this.getActiveMinions().size();
            int remainingCapacity = Math.max(0, MAX_ACTIVE_MINIONS - currentCount);
            int rolledCount = MIN_SUMMON_COUNT + this.mob.getRandom().nextInt(MAX_SUMMON_COUNT - MIN_SUMMON_COUNT + 1);
            int spawnCount = Math.min(remainingCapacity, rolledCount);

            Vec3 shelfOrigin = this.approachAirPos != null
                    ? Vec3.atCenterOf(this.approachAirPos)
                    : Vec3.atCenterOf(this.targetBlock.above());

            for (int i = 0; i < spawnCount; i++) {
                EnchantedBookEntity book = (EnchantedBookEntity) Entities.ENCHANTED_BOOK.get().create(serverLevel);
                if (book == null) {
                    continue;
                }

                ResourceKey<EnchantmentTheme> minionTheme = EnchantedBookEntity.rollPreferredTheme(
                        serverLevel.registryAccess(),
                        this.mob.getRandom(),
                        this.mob.getThemeKey()
                );
                ResourceKey<MagicSchool> minionSchool = EnchantedBookEntity.rollPreferredCycleSchool(
                        serverLevel.registryAccess(),
                        this.mob.getRandom(),
                        this.mob.getSchoolKey()
                );
                book.initializeWithThemeAndSchool(serverLevel.registryAccess(), this.mob.getRandom(), minionTheme, minionSchool);
                book.setCommanderUuid(this.mob.getUUID());
                book.setHomeShelfPos(this.targetBlock);
                book.setDeescalationTimer(MinionShelvingDespawnGoal.DEESCALATION_DURATION_TICKS);
                book.setEngagedInCombat(this.combatSummon);

                LivingEntity target = this.mob.getTarget();
                if (target != null && target.isAlive()) {
                    book.setTarget(target);
                    book.setEngagedInCombat(true);
                }

                for (int attempt = 0; attempt < 12; attempt++) {
                    double spawnX = shelfOrigin.x + (this.mob.getRandom().nextDouble() - 0.5D) * 2.2D;
                    double spawnY = shelfOrigin.y + (this.mob.getRandom().nextDouble() - 0.3D) * 1.6D;
                    double spawnZ = shelfOrigin.z + (this.mob.getRandom().nextDouble() - 0.5D) * 2.2D;
                    Vec3 candidatePos = new Vec3(spawnX, spawnY, spawnZ);
                    book.moveTo(spawnX, spawnY, spawnZ, this.mob.getRandom().nextFloat() * 360.0F, 0.0F);
                    if (serverLevel.noCollision(book) && this.hasClearRay(serverLevel, shelfOrigin, candidatePos)) {
                        serverLevel.addFreshEntity(book);
                        serverLevel.sendParticles(ParticleTypes.POOF, spawnX, spawnY, spawnZ, 10, 0.2D, 0.2D, 0.2D, 0.05D);
                        serverLevel.sendParticles(SimpleParticleTypes.SGA_RUNE.type(), spawnX, spawnY, spawnZ, 12, 0.25D, 0.25D, 0.25D, 0.04D);
                        break;
                    }
                }
            }

            serverLevel.playSound(null, this.targetBlock, SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        this.finished = true;
    }

    /**
     * Scans only loaded chunks within Manhattan range (20, 10, 20), skipping any 16x16x16
     * chunk section whose palette does not contain a bookshelf target. Sorts candidates by
     * distance so line-of-sight is checked on the closest shelves first and 3D A* pathfinding
     * is capped to at most {@link #MAX_PATHFIND_CANDIDATES} shelves.
     */
    @Nullable
    private ShelfCandidate resolveReachableTargetShelf(@Nullable BlockPos ignoredShelf) {
        Level level = this.mob.level();
        BlockPos origin = this.mob.blockPosition();
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        int minX = originX - HORIZONTAL_SEARCH_RANGE;
        int maxX = originX + HORIZONTAL_SEARCH_RANGE;
        int minY = Math.max(level.getMinBuildHeight(), originY - VERTICAL_SEARCH_RANGE);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, originY + VERTICAL_SEARCH_RANGE);
        int minZ = originZ - HORIZONTAL_SEARCH_RANGE;
        int maxZ = originZ + HORIZONTAL_SEARCH_RANGE;

        if (minY > maxY) {
            return null;
        }

        int minChunkX = SectionPos.blockToSectionCoord(minX);
        int maxChunkX = SectionPos.blockToSectionCoord(maxX);
        int minChunkZ = SectionPos.blockToSectionCoord(minZ);
        int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);

        List<ShelfCandidate> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        ChunkSource chunkSource = level.getChunkSource();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
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
                    if (section == null || section.hasOnlyAir() || !section.maybeHas(SHELF_STATE_PREDICATE)) {
                        continue;
                    }

                    int sectionBottomY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIdx));
                    int sectionStartY = Math.max(minY, sectionBottomY);
                    int sectionEndY = Math.min(maxY, sectionBottomY + 15);

                    for (int y = sectionStartY; y <= sectionEndY; y++) {
                        int dy = Math.abs(y - originY);
                        int remainingHorizontal = HORIZONTAL_SEARCH_RANGE - dy;
                        if (remainingHorizontal < 0) {
                            continue;
                        }
                        int localY = y & 15;

                        for (int x = chunkStartX; x <= chunkEndX; x++) {
                            int dx = Math.abs(x - originX);
                            if (dx > remainingHorizontal) {
                                continue;
                            }
                            int maxDz = remainingHorizontal - dx;
                            int localX = x & 15;

                            for (int z = chunkStartZ; z <= chunkEndZ; z++) {
                                if (Math.abs(z - originZ) > maxDz) {
                                    continue;
                                }
                                if (ignoredShelf != null
                                        && x == ignoredShelf.getX()
                                        && y == ignoredShelf.getY()
                                        && z == ignoredShelf.getZ()) {
                                    continue;
                                }

                                int localZ = z & 15;
                                BlockState state = section.getBlockState(localX, localY, localZ);
                                if (!SHELF_STATE_PREDICATE.test(state)) {
                                    continue;
                                }

                                mutablePos.set(x, y, z);
                                BlockPos openPos = this.findBestOpenApproachPos(chunkSource, chunk, mutablePos);
                                if (openPos == null) {
                                    continue;
                                }

                                double distSqr = this.mob.distanceToSqr(Vec3.atCenterOf(openPos));
                                candidates.add(new ShelfCandidate(mutablePos.immutable(), openPos, distSqr));
                            }
                        }
                    }
                }
            }
        }

        /* STREAMING_CHUNK:Sorting candidates by distance and bounding 3D A* pathfinding... */
        if (candidates.isEmpty()) {
            return null;
        }

        candidates.sort(Comparator.comparingDouble(ShelfCandidate::distSqr));

        // 1. Prefer the closest shelf with direct line-of-sight (zero A* pathfinding)
        for (ShelfCandidate candidate : candidates) {
            if (this.hasLineOfSightToShelf(candidate.shelfPos(), candidate.openPos())) {
                return candidate;
            }
        }

        // 2. If no shelf is directly visible, test 3D A* pathfinding on at most the 3 closest candidates
        int pathChecks = Math.min(MAX_PATHFIND_CANDIDATES, candidates.size());
        for (int i = 0; i < pathChecks; i++) {
            ShelfCandidate candidate = candidates.get(i);
            if (this.canPathToAirPos(candidate.openPos())) {
                return candidate;
            }
        }

        return null;
    }

    @Nullable
    private BlockPos findBestOpenApproachPos(ChunkSource chunkSource, LevelChunk shelfChunk, BlockPos shelfPos) {
        BlockPos above = shelfPos.above();
        if (shelfChunk.getBlockState(above).isAir()) {
            return above;
        }
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos rel = shelfPos.relative(dir);
            if (isAirAtLoaded(chunkSource, shelfChunk, rel)) {
                double d = this.mob.distanceToSqr(Vec3.atCenterOf(rel));
                if (d < bestDist) {
                    bestDist = d;
                    best = rel.immutable();
                }
            }
        }
        BlockPos below = shelfPos.below();
        if (best == null && shelfChunk.getBlockState(below).isAir()) {
            return below;
        }
        return best;
    }

    private static boolean isAirAtLoaded(ChunkSource chunkSource, LevelChunk referenceChunk, BlockPos pos) {
        int cx = SectionPos.blockToSectionCoord(pos.getX());
        int cz = SectionPos.blockToSectionCoord(pos.getZ());
        LevelChunk targetChunk = (cx == referenceChunk.getPos().x && cz == referenceChunk.getPos().z)
                ? referenceChunk
                : chunkSource.getChunkNow(cx, cz);
        return targetChunk != null && targetChunk.getBlockState(pos).isAir();
    }

    private boolean isShelfBlockLoaded(BlockPos pos) {
        LevelChunk chunk = this.mob.level().getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );
        return chunk != null && chunk.getBlockState(pos).is(Tags.ENCHANTED_BOOK_IDLE_TARGET);
    }

    private boolean hasLineOfSightToShelf(BlockPos shelfPos, @Nullable BlockPos openApproach) {
        Vec3 eye = this.mob.getEyePosition();
        if (openApproach != null && this.hasClearRay(this.mob.level(), eye, Vec3.atCenterOf(openApproach))) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            Vec3 faceCenter = Vec3.atCenterOf(shelfPos).add(
                    dir.getStepX() * 0.52D,
                    dir.getStepY() * 0.52D,
                    dir.getStepZ() * 0.52D
            );
            if (this.hasClearRay(this.mob.level(), eye, faceCenter)) {
                return true;
            }
        }
        return false;
    }

    private boolean canPathToAirPos(BlockPos airPos) {
        Path path = this.mob.getNavigation().createPath(airPos, 0);
        return path != null && path.canReach();
    }

    private void recalculate3DPath() {
        if (this.targetBlock == null) {
            return;
        }
        BlockPos dest = this.approachAirPos != null ? this.approachAirPos : this.targetBlock.above();
        this.mob.getNavigation().moveTo(dest.getX() + 0.5D, dest.getY() + 0.5D, dest.getZ() + 0.5D, this.speedModifier);
    }

    private Vec3 resolveNext3DWaypoint(Vec3 fallbackTarget) {
        Path path = this.mob.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            Vec3 nodePos = path.getNextEntityPos(this.mob).add(0.0D, 0.25D, 0.0D);
            if (nodePos.distanceToSqr(this.mob.position()) < 1.35D && path.getNextNodeIndex() + 1 < path.getNodeCount()) {
                path.advance();
                nodePos = path.getNextEntityPos(this.mob).add(0.0D, 0.25D, 0.0D);
            }
            return nodePos;
        }
        return fallbackTarget;
    }

    private Vec3 apply3DObstacleAvoidance(Vec3 rawDelta) {
        if (rawDelta.lengthSqr() < 0.001D) {
            return this.mob.getLookAngle();
        }
        Vec3 desiredDir = rawDelta.normalize();
        Level level = this.mob.level();
        Vec3 origin = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);

        if (this.hasClearRay(level, origin, origin.add(desiredDir.scale(1.6D)))) {
            return desiredDir;
        }
        Vec3 upDir = new Vec3(desiredDir.x * 0.55D, 0.82D, desiredDir.z * 0.55D).normalize();
        if (this.hasClearRay(level, origin, origin.add(upDir.scale(1.6D)))) {
            return upDir;
        }
        Vec3 leftDir = new Vec3(-desiredDir.z, desiredDir.y + 0.25D, desiredDir.x).normalize();
        if (this.hasClearRay(level, origin, origin.add(leftDir.scale(1.6D)))) {
            return leftDir;
        }
        Vec3 rightDir = new Vec3(desiredDir.z, desiredDir.y + 0.25D, -desiredDir.x).normalize();
        if (this.hasClearRay(level, origin, origin.add(rightDir.scale(1.6D)))) {
            return rightDir;
        }
        Vec3 downDir = new Vec3(desiredDir.x * 0.55D, -0.78D, desiredDir.z * 0.55D).normalize();
        if (this.hasClearRay(level, origin, origin.add(downDir.scale(1.6D)))) {
            return downDir;
        }
        return desiredDir;
    }

    private boolean hasClearRay(Level level, Vec3 from, Vec3 to) {
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob));
        return hit.getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.combatSummon && this.mob.level().isDay() && this.mob.getTarget() == null && !this.mob.isAngry()) {
            return false;
        }
        return !this.finished
                && this.targetBlock != null
                && this.isShelfBlockLoaded(this.targetBlock);
    }

    @Override
    public void stop() {
        this.cooldown = this.combatSummon
                ? (200 + this.mob.getRandom().nextInt(200))
                : (400 + this.mob.getRandom().nextInt(400));
        this.chantTicks = 0;
        this.approachTicks = 0;
        this.stuckTicks = 0;
        this.losCheckCooldown = 0;
        this.cachedShelfLoS = false;
        this.isChanting = false;
        this.combatSummon = false;
        this.targetBlock = null;
        this.approachAirPos = null;
        this.mob.setAnimationState(BookAnimationState.IDLE);
    }

    private List<EnchantedBookEntity> getActiveMinions() {
        return this.mob.level().getEntitiesOfClass(
                EnchantedBookEntity.class,
                this.mob.getBoundingBox().inflate(28.0D),
                book -> book.isAlive() && book.getCommanderUuid().map(id -> id.equals(this.mob.getUUID())).orElse(false)
        );
    }
}