package johnsmith.enchantingoverhauled.entity.ai.goal;

import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.entity.EnchantedBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * AI Goal that directs summoned {@link EnchantedBookEntity} minions to return to a nearby
 * bookshelf and despawn once combat has de-escalated.
 * <p>
 * Optimized to scan only loaded chunks using {@link LevelChunkSection#maybeHas(Predicate)}
 * palette filtering and to bound 3D A* pathfinding to at most the 3 closest candidate shelves.
 */
public class MinionShelvingDespawnGoal extends Goal {
    public static final int DEESCALATION_DURATION_TICKS = 1200;
    private static final int MAX_DESPAWN_ATTEMPT_TICKS = 200;
    private static final int STUCK_REPATH_TICKS = 35;

    private static final int HORIZONTAL_SEARCH_RANGE = 16;
    private static final int VERTICAL_SEARCH_RANGE = 8;
    private static final int MAX_PATHFIND_CANDIDATES = 3;

    /**
     * Reusable section-palette predicate to skip 16x16x16 chunk sections that contain no shelves.
     */
    private static final Predicate<BlockState> SHELF_STATE_PREDICATE =
            state -> !state.isAir() && state.is(Tags.ENCHANTED_BOOK_IDLE_TARGET);

    private final EnchantedBookEntity book;
    private final double speedModifier;
    @Nullable
    private BlockPos targetShelfPos;
    @Nullable
    private BlockPos approachAirPos;
    private int totalDespawnTicks = 0;
    private int stuckTicks = 0;
    private int scanCooldown = 0;
    private double bestDistSqr = Double.MAX_VALUE;

    private record ShelfCandidate(BlockPos shelfPos, BlockPos openPos, double distSqr) {}

    public MinionShelvingDespawnGoal(EnchantedBookEntity book, double speedModifier) {
        this.book = book;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.book.getCommanderUuid().isEmpty() && this.book.getHomeShelfPos() == null) {
            return false;
        }
        if (this.book.isPersistenceRequired() || this.book.hasCustomName()) {
            return false;
        }
        if (this.book.getTarget() != null || this.book.isAngry()) {
            this.book.setDeescalationTimer(DEESCALATION_DURATION_TICKS);
            return false;
        }
        if (this.book.getDeescalationTimer() > 0) {
            return false;
        }
        if (this.scanCooldown > 0) {
            this.scanCooldown--;
            return false;
        }
        ShelfCandidate resolved = this.resolveTargetShelf(null);
        if (resolved != null) {
            this.targetShelfPos = resolved.shelfPos();
            this.approachAirPos = resolved.openPos();
            return true;
        }
        this.scanCooldown = 20 + this.book.getRandom().nextInt(20);
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.book.getTarget() != null || this.book.isAngry()) {
            this.book.setDeescalationTimer(DEESCALATION_DURATION_TICKS);
            return false;
        }
        return this.targetShelfPos != null && this.book.isAlive();
    }

    @Override
    public void start() {
        this.totalDespawnTicks = 0;
        this.stuckTicks = 0;
        this.bestDistSqr = Double.MAX_VALUE;
        this.book.setRoostPos(null);
        this.book.setAnimationState(BookAnimationState.IDLE);
        this.recalculate3DPath();
    }

    @Override
    public void tick() {
        if (this.targetShelfPos == null) {
            return;
        }

        this.totalDespawnTicks++;
        Vec3 dockTarget = this.approachAirPos != null
                ? Vec3.atCenterOf(this.approachAirPos)
                : Vec3.atCenterOf(this.targetShelfPos).add(0.0D, 0.85D, 0.0D);

        this.book.getLookControl().setLookAt(
                this.targetShelfPos.getX() + 0.5D,
                this.targetShelfPos.getY() + 0.5D,
                this.targetShelfPos.getZ() + 0.5D,
                25.0F,
                25.0F
        );

        double distToShelfSqr = this.book.distanceToSqr(Vec3.atCenterOf(this.targetShelfPos));
        double distToDockSqr = this.book.distanceToSqr(dockTarget);

        if (distToShelfSqr <= 4.5D || distToDockSqr <= 1.75D || this.totalDespawnTicks >= MAX_DESPAWN_ATTEMPT_TICKS) {
            this.completeShelvingDespawn();
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
            ShelfCandidate fallbackShelf = this.resolveTargetShelf(this.targetShelfPos);
            if (fallbackShelf != null) {
                this.targetShelfPos = fallbackShelf.shelfPos();
                this.approachAirPos = fallbackShelf.openPos();
            }
            this.recalculate3DPath();
        } else if (this.book.getNavigation().isDone() || this.totalDespawnTicks % 15 == 0) {
            this.recalculate3DPath();
        }

        Vec3 nextWaypoint = this.resolveNext3DWaypoint(dockTarget);
        Vec3 dir = this.apply3DObstacleAvoidance(nextWaypoint.subtract(this.book.position()));
        Vec3 steerTarget = this.book.position().add(dir.scale(2.0D));
        this.book.getMoveControl().setWantedPosition(steerTarget.x, steerTarget.y, steerTarget.z, this.speedModifier);
    }

    private void recalculate3DPath() {
        if (this.targetShelfPos == null) {
            return;
        }
        BlockPos dest = this.approachAirPos != null ? this.approachAirPos : this.targetShelfPos.above();
        this.book.getNavigation().moveTo(dest.getX() + 0.5D, dest.getY() + 0.5D, dest.getZ() + 0.5D, this.speedModifier);
    }

    private Vec3 resolveNext3DWaypoint(Vec3 fallbackTarget) {
        Path path = this.book.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            Vec3 nodePos = path.getNextEntityPos(this.book).add(0.0D, 0.25D, 0.0D);
            if (nodePos.distanceToSqr(this.book.position()) < 1.35D && path.getNextNodeIndex() + 1 < path.getNodeCount()) {
                path.advance();
                nodePos = path.getNextEntityPos(this.book).add(0.0D, 0.25D, 0.0D);
            }
            return nodePos;
        }
        return fallbackTarget;
    }

    private Vec3 apply3DObstacleAvoidance(Vec3 rawDelta) {
        if (rawDelta.lengthSqr() < 0.001D) {
            return this.book.getLookAngle();
        }
        Vec3 desiredDir = rawDelta.normalize();
        Level level = this.book.level();
        Vec3 origin = this.book.position().add(0.0D, this.book.getBbHeight() * 0.5D, 0.0D);

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

    private void completeShelvingDespawn() {
        if (this.book.level() instanceof ServerLevel serverLevel) {
            this.book.ejectBarterItems();
            BlockPos soundPos = this.targetShelfPos != null ? this.targetShelfPos : this.book.blockPosition();
            serverLevel.playSound(
                    null,
                    soundPos,
                    SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            serverLevel.sendParticles(
                    SimpleParticleTypes.SGA_RUNE.type(),
                    this.book.getX(),
                    this.book.getY() + 0.3D,
                    this.book.getZ(),
                    15,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.03D
            );
            this.book.discard();
        }
    }

    @Override
    public void stop() {
        this.targetShelfPos = null;
        this.approachAirPos = null;
    }

    /**
     * Resolves a target bookshelf using loaded-chunk section palette filtering and distance-sorted
     * evaluation, avoiding in-loop 3D A* pathfinding across every shelf in the room.
     */
    @Nullable
    private ShelfCandidate resolveTargetShelf(@Nullable BlockPos ignoredShelf) {
        Level level = this.book.level();
        ChunkSource chunkSource = level.getChunkSource();
        Vec3 bookCenter = this.book.position().add(0.0D, this.book.getBbHeight() * 0.5D, 0.0D);

        BlockPos home = this.book.getHomeShelfPos();
        if (home != null && !home.equals(ignoredShelf)) {
            LevelChunk homeChunk = chunkSource.getChunkNow(
                    SectionPos.blockToSectionCoord(home.getX()),
                    SectionPos.blockToSectionCoord(home.getZ())
            );
            if (homeChunk != null && homeChunk.getBlockState(home).is(Tags.ENCHANTED_BOOK_IDLE_TARGET)) {
                BlockPos openHome = this.findBestOpenApproachPos(chunkSource, homeChunk, home);
                if (openHome != null && this.canReachAirPos(bookCenter, openHome)) {
                    return new ShelfCandidate(home, openHome, this.book.distanceToSqr(Vec3.atCenterOf(openHome)));
                }
            }
        }

        BlockPos origin = this.book.blockPosition();
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        int minX = originX - HORIZONTAL_SEARCH_RANGE;
        int maxX = originX + HORIZONTAL_SEARCH_RANGE;
        int minY = Math.max(level.getMinBuildHeight(), originY - VERTICAL_SEARCH_RANGE);
        int maxY = Math.min(level.getMaxBuildHeight() - 2, originY + VERTICAL_SEARCH_RANGE);
        int minZ = originZ - HORIZONTAL_SEARCH_RANGE;
        int maxZ = originZ + HORIZONTAL_SEARCH_RANGE;

        List<ShelfCandidate> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        if (minY <= maxY) {
            int minChunkX = SectionPos.blockToSectionCoord(minX);
            int maxChunkX = SectionPos.blockToSectionCoord(maxX);
            int minChunkZ = SectionPos.blockToSectionCoord(minZ);
            int maxChunkZ = SectionPos.blockToSectionCoord(maxZ);

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

                                    double distSqr = this.book.distanceToSqr(Vec3.atCenterOf(openPos));
                                    candidates.add(new ShelfCandidate(mutablePos.immutable(), openPos, distSqr));
                                }
                            }
                        }
                    }
                }
            }
        }

        /* STREAMING_CHUNK:Evaluating sorted candidates with LoS priority and bounded A* pathing... */
        if (!candidates.isEmpty()) {
            candidates.sort(Comparator.comparingDouble(ShelfCandidate::distSqr));

            // 1. Check direct line-of-sight on the closest shelves first (zero A* pathfinding)
            for (ShelfCandidate candidate : candidates) {
                if (this.hasClearRay(level, bookCenter, Vec3.atCenterOf(candidate.openPos()))) {
                    return candidate;
                }
            }

            // 2. Cap 3D A* pathfinding to at most the 3 closest shelves
            int pathChecks = Math.min(MAX_PATHFIND_CANDIDATES, candidates.size());
            for (int i = 0; i < pathChecks; i++) {
                ShelfCandidate candidate = candidates.get(i);
                Path path = this.book.getNavigation().createPath(candidate.openPos(), 0);
                if (path != null && path.canReach()) {
                    return candidate;
                }
            }

            // 3. Fallback to the closest shelf with an open approach position
            return candidates.getFirst();
        }

        BlockPos fallbackPos = home != null ? home : origin;
        return new ShelfCandidate(fallbackPos, fallbackPos.above(), 0.0D);
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
                double d = this.book.distanceToSqr(Vec3.atCenterOf(rel));
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

    private boolean canReachAirPos(Vec3 from, BlockPos airPos) {
        Vec3 to = Vec3.atCenterOf(airPos);
        if (this.hasClearRay(this.book.level(), from, to)) {
            return true;
        }
        Path path = this.book.getNavigation().createPath(airPos, 0);
        return path != null && path.canReach();
    }

    private boolean hasClearRay(Level level, Vec3 from, Vec3 to) {
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.book));
        return hit.getType() == HitResult.Type.MISS;
    }
}