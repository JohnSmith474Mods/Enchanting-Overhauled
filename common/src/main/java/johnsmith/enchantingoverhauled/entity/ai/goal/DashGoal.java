package johnsmith.enchantingoverhauled.entity.ai.goal;

import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.ai.control.BookMoveControl;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AI Goal that propels {@link AbstractFlyingBookEntity} instances via direct 3D velocity impulses
 * to evade melee strikes, back off from ranged targets, or close long pursuit gaps.
 * <p>
 * Optimized to eliminate all 3D A* pathfinding calls (previously up to 49 per dash) and use
 * branch-and-bound score pruning so raycasts and AABB collision checks only run on viable candidates.
 */
public class DashGoal extends Goal {
    private static final int INTAKE_TICKS = 4;
    private static final int MAX_DASH_TICKS = 26;

    private static final double CLEAR_ORIGIN_RAY_BONUS = 22.0D;
    private static final double BLOCKED_ORIGIN_RAY_PENALTY = 30.0D;
    private static final double CLEAR_TARGET_RAY_BONUS = 18.0D;

    private final AbstractFlyingBookEntity mob;
    private final double speedModifier;
    private final float minSafeDistance;
    private final float maxPursuitDistance;
    private final int maxInterruptTicks;
    private int dashTicks = 0;
    private Vec3 dashDestination = Vec3.ZERO;
    private Vec3 dashDirection = Vec3.ZERO;

    public DashGoal(
            AbstractFlyingBookEntity mob,
            double speedModifier,
            float minSafeDistance,
            float maxPursuitDistance,
            int maxInterruptTicks
    ) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minSafeDistance = minSafeDistance;
        this.maxPursuitDistance = maxPursuitDistance;
        this.maxInterruptTicks = maxInterruptTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        BookAnimationState state = this.mob.getAnimationState();
        if (state.isChanting() && this.mob.getServerStateTicks() > this.maxInterruptTicks) {
            return false;
        }

        boolean rangedTarget = BookMoveControl.isTargetRanged(target);
        double euclideanDistSqr = this.mob.distanceToSqr(target);

        if (this.mob.consumePendingHurtDash()) {
            if (rangedTarget && euclideanDistSqr < (double) ((this.minSafeDistance + 2.0F) * (this.minSafeDistance + 2.0F))) {
                return true;
            }
            if (!rangedTarget && !BookMoveControl.canAscendFromMelee(this.mob, target)) {
                return true;
            }
        }
        if (euclideanDistSqr > (double) (this.maxPursuitDistance * this.maxPursuitDistance)) {
            return true;
        }
        if (rangedTarget) {
            return euclideanDistSqr < (double) (this.minSafeDistance * this.minSafeDistance);
        }
        if (BookMoveControl.isWithinMeleeStrikingRange(this.mob, target)) {
            return !BookMoveControl.canAscendFromMelee(this.mob, target);
        }
        return false;
    }

    /* STREAMING_CHUNK:Initializing dash trajectory without A* pathfinding... */
    @Override
    public void start() {
        this.dashTicks = 0;
        this.mob.setRoostPos(null);
        this.mob.setAnimationState(BookAnimationState.DASHING);
        this.mob.setHurtDashCooldown(40);
        // Stop any active path navigation; DashGoal drives motion directly via velocity impulses
        this.mob.getNavigation().stop();

        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            this.dashDirection = this.mob.getLookAngle();
            this.dashDestination = this.mob.position().add(this.dashDirection.scale(6.0D));
            return;
        }

        this.dashDestination = this.findBest3DDashDestination(target);
        Vec3 nextSteer = this.resolveCurrentSteeringVector();
        this.dashDirection = nextSteer.lengthSqr() > 0.001D ? nextSteer.normalize() : this.mob.getLookAngle();
        this.orientAlongVector(this.dashDirection);
    }

    @Override
    public void tick() {
        // Re-evaluate obstacle avoidance every 2 ticks once airborne to halve per-tick raycasts
        if (this.dashTicks >= INTAKE_TICKS && (this.dashTicks & 1) == 0) {
            Vec3 steeredDir = this.resolveCurrentSteeringVector();
            if (steeredDir.lengthSqr() > 0.001D) {
                this.dashDirection = this.dashDirection.scale(0.65D).add(steeredDir.normalize().scale(0.35D)).normalize();
            }
        }

        if (this.dashTicks < INTAKE_TICKS) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.45D));
            this.orientAlongVector(this.dashDirection);
        } else if (this.dashTicks == INTAKE_TICKS) {
            double impulseSpeed = 0.62D * this.speedModifier;
            this.mob.setDeltaMovement(this.dashDirection.scale(impulseSpeed));
            this.orientAlongVector(this.dashDirection);
            this.mob.playSound(SoundEvents.BOOK_PAGE_TURN, 1.2F, 0.85F);
        } else {
            double progress = (double) (this.dashTicks - INTAKE_TICKS) / (double) (MAX_DASH_TICKS - INTAKE_TICKS);
            double desiredSpeed = Mth.lerp(progress, 0.56D * this.speedModifier, 0.22D * this.speedModifier);
            Vec3 currentVel = this.mob.getDeltaMovement();
            Vec3 targetVel = this.dashDirection.scale(desiredSpeed);
            Vec3 blendedVel = currentVel.scale(0.65D).add(targetVel.scale(0.35D));
            this.mob.setDeltaMovement(blendedVel);
            if (blendedVel.lengthSqr() > 0.002D) {
                this.orientAlongVector(blendedVel);
            }
        }
        this.dashTicks++;
    }

    private Vec3 resolveCurrentSteeringVector() {
        Vec3 rawDir = this.dashDestination.subtract(this.mob.position());
        if (rawDir.lengthSqr() < 0.001D) {
            return this.mob.getLookAngle();
        }
        return this.apply3DObstacleAvoidance(rawDir.normalize());
    }

    private Vec3 apply3DObstacleAvoidance(Vec3 desiredDir) {
        Level level = this.mob.level();
        Vec3 origin = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);
        Vec3 probeEnd = origin.add(desiredDir.scale(1.8D));

        if (this.hasClearRay(level, origin, probeEnd)) {
            return desiredDir;
        }

        // Ensure a valid horizontal basis even when diving or climbing nearly vertically
        double horizLen = Math.sqrt(desiredDir.x * desiredDir.x + desiredDir.z * desiredDir.z);
        double hx;
        double hz;
        if (horizLen > 1.0E-4D) {
            hx = desiredDir.x / horizLen;
            hz = desiredDir.z / horizLen;
        } else {
            Vec3 look = this.mob.getLookAngle();
            double lookHoriz = Math.sqrt(look.x * look.x + look.z * look.z);
            hx = lookHoriz > 1.0E-4D ? (look.x / lookHoriz) : 1.0D;
            hz = lookHoriz > 1.0E-4D ? (look.z / lookHoriz) : 0.0D;
        }

        boolean descending = desiredDir.y < -0.1D;
        double lateralY = descending ? desiredDir.y : (desiredDir.y + 0.2D);

        Vec3 downDir = new Vec3(hx * 0.6D, -0.8D, hz * 0.6D).normalize();
        Vec3 upDir = new Vec3(hx * 0.6D, 0.8D, hz * 0.6D).normalize();
        Vec3 leftDir = new Vec3(-hz, lateralY, hx).normalize();
        Vec3 rightDir = new Vec3(hz, lateralY, -hx).normalize();

        if (descending) {
            if (this.hasClearRay(level, origin, origin.add(downDir.scale(1.8D)))) {
                return downDir;
            }
            if (this.hasClearRay(level, origin, origin.add(leftDir.scale(1.8D)))) {
                return leftDir;
            }
            if (this.hasClearRay(level, origin, origin.add(rightDir.scale(1.8D)))) {
                return rightDir;
            }
            if (this.hasClearRay(level, origin, origin.add(upDir.scale(1.8D)))) {
                return upDir;
            }
        } else {
            if (this.hasClearRay(level, origin, origin.add(upDir.scale(1.8D)))) {
                return upDir;
            }
            if (this.hasClearRay(level, origin, origin.add(leftDir.scale(1.8D)))) {
                return leftDir;
            }
            if (this.hasClearRay(level, origin, origin.add(rightDir.scale(1.8D)))) {
                return rightDir;
            }
            if (this.hasClearRay(level, origin, origin.add(downDir.scale(1.8D)))) {
                return downDir;
            }
        }
        return desiredDir;
    }

    /**
     * Evaluates 48 candidate dash destinations using loaded-chunk air checks and branch-and-bound
     * score pruning, deferring raycasts and AABB collision checks until a candidate can provably
     * beat {@code bestScore} and completely eliminating 3D A* pathfinding.
     */
    private Vec3 findBest3DDashDestination(LivingEntity target) {
        Level level = this.mob.level();
        ChunkSource chunkSource = level.getChunkSource();
        Vec3 mobPos = this.mob.position();
        Vec3 mobEye = mobPos.add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);
        Vec3 targetPos = target.position();
        Vec3 targetEye = target.getEyePosition();
        AABB mobBox = this.mob.getBoundingBox();

        double euclideanDistSqr = this.mob.distanceToSqr(target);
        boolean pursuing = euclideanDistSqr > (double) (this.maxPursuitDistance * this.maxPursuitDistance);
        boolean rangedTarget = BookMoveControl.isTargetRanged(target);

        double minSafeAltitude = BookMoveControl.getMinimumSafeCombatAltitude(target);
        double meleeVerticalLimit = BookMoveControl.getMeleeVerticalStrikeLimit(target);
        double baseCombatAltitude = rangedTarget
                ? minSafeAltitude
                : Math.max(minSafeAltitude, meleeVerticalLimit);

        Vec3 toMob = mobPos.subtract(targetPos);
        double horizDist = toMob.horizontalDistance();
        double baseAngle = toMob.horizontalDistanceSqr() > 0.001D
                ? Mth.atan2(toMob.z, toMob.x)
                : this.mob.getRandom().nextDouble() * Math.PI * 2.0D;

        // Scale pursuit horizontal radius with horizontal distance so overhead books dive steeply downward
        double radius = pursuing
                ? Mth.clamp(horizDist * 0.65D, this.minSafeDistance + 1.5D, 12.0D)
                : (rangedTarget ? (this.minSafeDistance + 3.5D) : 6.0D);
        Vec3 anchor = pursuing ? targetPos : mobPos;

        double verticalDelta = mobPos.y - baseCombatAltitude;
        double[] candidateYs;
        if (Math.abs(verticalDelta) > 8.0D) {
            // Include both target combat altitudes and intermediate vertical steps when far above/below target
            candidateYs = new double[]{
                    baseCombatAltitude,
                    baseCombatAltitude + 1.5D,
                    Mth.lerp(0.35D, mobPos.y, baseCombatAltitude),
                    Mth.lerp(0.65D, mobPos.y, baseCombatAltitude)
            };
        } else {
            candidateYs = new double[]{
                    Math.max(minSafeAltitude, baseCombatAltitude - 0.5D),
                    baseCombatAltitude + 0.25D,
                    baseCombatAltitude + 1.25D,
                    baseCombatAltitude + 2.5D
            };
        }

        Vec3 bestPos = null;
        double bestScore = -Double.MAX_VALUE;
        int angleSamples = 12;

        for (int i = 0; i < angleSamples; i++) {
            double angleOffset = i * (Math.PI * 2.0D / angleSamples);
            double angle = baseAngle + angleOffset;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double candX = anchor.x + cos * radius;
            double candZ = anchor.z + sin * radius;

            for (double candY : candidateYs) {
                BlockPos candBlock = BlockPos.containing(candX, candY, candZ);

                LevelChunk chunk = chunkSource.getChunkNow(
                        SectionPos.blockToSectionCoord(candBlock.getX()),
                        SectionPos.blockToSectionCoord(candBlock.getZ())
                );
                if (chunk == null || !chunk.getBlockState(candBlock).isAir()) {
                    continue;
                }

                Vec3 candidate = new Vec3(candX, candY, candZ);
                double score = 0.0D;
                double euclideanToTarget = candidate.distanceTo(targetPos);

                if (!rangedTarget && candY >= meleeVerticalLimit) {
                    score += 32.0D;
                }
                if (euclideanToTarget >= this.minSafeDistance + 1.0D && euclideanToTarget <= BookMoveControl.MAX_EUCLIDEAN_ATTACK_RANGE) {
                    score += 24.0D;
                } else if (euclideanToTarget < this.minSafeDistance) {
                    score -= (this.minSafeDistance - euclideanToTarget) * 12.0D;
                } else if (euclideanToTarget > BookMoveControl.MAX_EUCLIDEAN_ATTACK_RANGE) {
                    score -= (euclideanToTarget - BookMoveControl.MAX_EUCLIDEAN_ATTACK_RANGE) * 5.0D;
                }

                // Prefer candidates closer to the target's ideal combat altitude band
                score -= Math.abs(candY - baseCombatAltitude) * 1.5D;

                int openNeighbors = 0;
                for (Direction dir : Direction.values()) {
                    if (isAirAtLoaded(chunkSource, chunk, candBlock.relative(dir))) {
                        openNeighbors++;
                    }
                }
                score += openNeighbors * 2.5D;
                score += this.mob.getRandom().nextDouble() * 2.5D;

                // Branch-and-bound: skip raycasts if even both clear-ray bonuses cannot beat bestScore
                if (score + CLEAR_ORIGIN_RAY_BONUS + CLEAR_TARGET_RAY_BONUS <= bestScore) {
                    continue;
                }

                if (this.hasClearRay(level, mobEye, candidate)) {
                    score += CLEAR_ORIGIN_RAY_BONUS;
                } else {
                    score -= BLOCKED_ORIGIN_RAY_PENALTY;
                }

                // Skip second raycast if the remaining target-LoS bonus cannot beat bestScore
                if (score + CLEAR_TARGET_RAY_BONUS <= bestScore) {
                    continue;
                }

                if (this.hasClearRay(level, candidate, targetEye)) {
                    score += CLEAR_TARGET_RAY_BONUS;
                }

                if (score <= bestScore) {
                    continue;
                }

                // Only run the AABB voxel collision check when this candidate actually beats bestScore
                if (!level.noCollision(this.mob, mobBox.move(candidate.subtract(mobPos)))) {
                    continue;
                }

                bestScore = score;
                bestPos = candidate;
            }
        }

        if (bestPos != null) {
            return bestPos;
        }
        return new Vec3(targetPos.x, baseCombatAltitude, targetPos.z);
    }

    private static boolean isAirAtLoaded(ChunkSource chunkSource, LevelChunk referenceChunk, BlockPos pos) {
        int cx = SectionPos.blockToSectionCoord(pos.getX());
        int cz = SectionPos.blockToSectionCoord(pos.getZ());
        LevelChunk targetChunk = (cx == referenceChunk.getPos().x && cz == referenceChunk.getPos().z)
                ? referenceChunk
                : chunkSource.getChunkNow(cx, cz);
        return targetChunk != null && targetChunk.getBlockState(pos).isAir();
    }

    private boolean hasClearRay(Level level, Vec3 from, Vec3 to) {
        HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void orientAlongVector(Vec3 dir) {
        if (dir.lengthSqr() < 0.0001D) {
            return;
        }
        float targetYaw = (float) (Mth.atan2(dir.z, dir.x) * (180.0D / Math.PI)) - 90.0F;
        this.mob.setYRot(targetYaw);
        this.mob.yRotO = targetYaw;
        this.mob.yBodyRot = targetYaw;
        this.mob.yHeadRot = targetYaw;

        float targetPitch = (float) (-(Mth.atan2(dir.y, dir.horizontalDistance()) * (180.0D / Math.PI)));
        this.mob.setXRot(targetPitch);
        this.mob.xRotO = targetPitch;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.dashTicks >= MAX_DASH_TICKS) {
            return false;
        }
        if (this.dashTicks > INTAKE_TICKS + 3 && this.mob.distanceToSqr(this.dashDestination) <= 1.75D) {
            return false;
        }
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.35D));
        this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
    }
}