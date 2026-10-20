package johnsmith.enchantingoverhauled.entity.ai.goal;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.data.datagen.spellfield.DefaultMagicSchools;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.ai.control.BookMoveControl;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellFields;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.CastType;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.SpellRole;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.SpellTarget;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.entity.projectile.MagicMissileEntity;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * AI Goal that handles offensive, defensive, and supportive spellcasting for {@link AbstractFlyingBookEntity}.
 * <p>
 * Optimized to throttle 3D A* repathing, steer radial kiting directly via {@link BookMoveControl}
 * without pathfinding, and eliminate duplicate per-tick line-of-sight raycasts.
 */
public class CastSpellGoal extends Goal {
    private static final double AOE_SAFETY_RADIUS = 4.5D;
    private static final double ALLY_SUPPORT_RANGE = 14.0D;
    private static final int MAX_HOLD_ANGLE_TICKS = 100;
    private static final double VACATED_DISTANCE_SQR = 81.0D;
    private static final double MISSILE_SPEED = 1.6D;

    private static final int REPATH_INTERVAL_TICKS = 14;
    private static final double REPATH_TARGET_MOVE_SQR = 6.25D;
    private static final int LOS_CHECK_INTERVAL_TICKS = 4;

    private final AbstractFlyingBookEntity mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private final float minDistanceSqr;
    private final int castIntervalMin;
    private final int castIntervalMax;

    private int attackTime = -1;
    private int chargeTime = -1;
    private int hiddenBehindCoverTicks = 0;
    private int defensiveCooldown = 0;
    private int supportiveCooldown = 0;
    private int repathCooldown = 0;
    private int losCheckCooldown = 0;
    private boolean cachedCanSeePrimary = false;
    @Nullable
    private LivingEntity lastLosTarget = null;
    @Nullable
    private Vec3 lastPathTargetPos = null;
    @Nullable
    private Vec3 lastSeenTargetPos = null;
    @Nullable
    private SpellRole activeRole = null;
    @Nullable
    private SpellTarget activeSpellTarget = null;
    @Nullable
    private CastType activeCastType = null;
    @Nullable
    private LivingEntity activeRoleTarget = null;
    @Nullable
    private AiSpellField selectedSpellField = null;

    public CastSpellGoal(
            AbstractFlyingBookEntity mob,
            double speedModifier,
            int castIntervalMin,
            int castIntervalMax,
            float minDistance,
            float maxDistance
    ) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.castIntervalMin = castIntervalMin;
        this.castIntervalMax = castIntervalMax;
        this.minDistanceSqr = minDistance * minDistance;
        this.attackRadiusSqr = maxDistance * maxDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && this.mob.isValidHostileTarget(target);
    }

    @Override
    public void start() {
        this.attackTime = this.castIntervalMin / 2;
        this.repathCooldown = 0;
        this.losCheckCooldown = 0;
        this.cachedCanSeePrimary = false;
        this.lastLosTarget = null;
        this.lastPathTargetPos = null;
        this.resetChargeState();
        this.mob.setRoostPos(null);
        this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
    }

    @Override
    public void stop() {
        this.mob.setCastSchoolKey(null);
        this.mob.setAnimationState(BookAnimationState.SCOUTING);
        this.repathCooldown = 0;
        this.losCheckCooldown = 0;
        this.lastLosTarget = null;
        this.lastPathTargetPos = null;
        this.resetChargeState();
    }

    private void resetChargeState() {
        this.chargeTime = -1;
        this.hiddenBehindCoverTicks = 0;
        this.lastSeenTargetPos = null;
        this.activeRole = null;
        this.activeSpellTarget = null;
        this.activeCastType = null;
        this.activeRoleTarget = null;
        this.selectedSpellField = null;
    }

    private static BookAnimationState getAnimationStateForCast(
            SpellRole role,
            @Nullable SpellTarget target,
            @Nullable CastType castType
    ) {
        if (role == SpellRole.DEFENSIVE && target == SpellTarget.SELF && castType != CastType.PROJECTILE) {
            return BookAnimationState.CHANTING_DEFENSIVE;
        }
        return switch (role) {
            case OFFENSIVE -> BookAnimationState.CHANTING_OFFENSIVE;
            case DEFENSIVE, SUPPORTIVE -> BookAnimationState.CHANTING_SUPPORTIVE;
        };
    }

    private static ResourceKey<MagicSchool> getDefaultSchoolForRole(SpellRole role) {
        return switch (role) {
            case OFFENSIVE -> DefaultMagicSchools.OBLITERATION;
            case DEFENSIVE -> DefaultMagicSchools.ABJURATION;
            case SUPPORTIVE -> DefaultMagicSchools.PERPETUATION;
        };
    }

    private Vec3 getMissileSpawnPos(LivingEntity target) {
        Vec3 focal = this.mob.getChantingFocalPoint();
        BlockPos focalBlock = BlockPos.containing(focal);
        if (this.mob.level().getBlockState(focalBlock).getCollisionShape(this.mob.level(), focalBlock).isEmpty()) {
            return focal;
        }
        Vec3 center = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.55D, 0.0D);
        Vec3 toTarget = target.getEyePosition().subtract(center);
        if (toTarget.lengthSqr() > 0.001D) {
            toTarget = toTarget.normalize().scale(0.38D);
        }
        return center.add(toTarget);
    }

    private boolean hasFrontClearance(LivingEntity target) {
        BlockPos pos = BlockPos.containing(this.getMissileSpawnPos(target));
        return this.mob.level().getBlockState(pos).getCollisionShape(this.mob.level(), pos).isEmpty();
    }

    private boolean hasLineOfSightToTarget(LivingEntity target) {
        if (target == this.mob) {
            return true;
        }
        if (!this.mob.getSensing().hasLineOfSight(target)) {
            return false;
        }
        Vec3 from = this.getMissileSpawnPos(target);
        Vec3 to = target.getEyePosition();
        HitResult hit = this.mob.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.mob
        ));
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }
        Vec3 bodyCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        HitResult bodyHit = this.mob.level().clip(new ClipContext(
                from,
                bodyCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.mob
        ));
        return bodyHit.getType() == HitResult.Type.MISS;
    }

    private boolean checkPrimaryLineOfSightThrottled(LivingEntity primaryTarget) {
        if (this.lastLosTarget != primaryTarget || --this.losCheckCooldown <= 0 || this.attackTime <= 1) {
            this.lastLosTarget = primaryTarget;
            this.losCheckCooldown = LOS_CHECK_INTERVAL_TICKS;
            this.cachedCanSeePrimary = this.hasLineOfSightToTarget(primaryTarget);
        }
        return this.cachedCanSeePrimary;
    }

    @Override
    public void tick() {
        if (this.defensiveCooldown > 0) {
            this.defensiveCooldown--;
        }
        if (this.supportiveCooldown > 0) {
            this.supportiveCooldown--;
        }
        if (this.repathCooldown > 0) {
            this.repathCooldown--;
        }

        LivingEntity primaryTarget = this.mob.getTarget();
        if (primaryTarget == null || !primaryTarget.isAlive()) {
            return;
        }

        // Delegate immediately when charging a spell to avoid duplicate LoS checks and pathing
        if (this.chargeTime >= 0 && this.activeRole != null && this.activeSpellTarget != null && this.activeCastType != null) {
            this.tickChargingCast(primaryTarget);
            return;
        }

        double euclideanDistSqr = this.mob.distanceToSqr(primaryTarget);
        boolean withinAttackRange = euclideanDistSqr <= (double) this.attackRadiusSqr;
        boolean canSeePrimary = this.checkPrimaryLineOfSightThrottled(primaryTarget);
        boolean rangedTarget = BookMoveControl.isTargetRanged(primaryTarget);

        this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);

        if (rangedTarget && euclideanDistSqr < (double) this.minDistanceSqr) {
            // Direct radial backoff via MoveControl (zero 3D A* pathfinding overhead)
            if (!this.mob.getNavigation().isDone()) {
                this.mob.getNavigation().stop();
            }
            this.lastPathTargetPos = null;
            Vec3 diff = this.mob.position().subtract(primaryTarget.position());
            Vec3 away = diff.lengthSqr() > 0.001D
                    ? diff.normalize().scale(6.0D)
                    : this.mob.getLookAngle().scale(-6.0D);
            double desiredY = BookMoveControl.resolveCombatAltitudeForIncline(this.mob, primaryTarget);
            this.mob.getMoveControl().setWantedPosition(
                    primaryTarget.getX() + away.x,
                    desiredY,
                    primaryTarget.getZ() + away.z,
                    this.speedModifier
            );
        } else if (!withinAttackRange || !canSeePrimary) {
            Vec3 targetPos = primaryTarget.position();
            if (this.repathCooldown <= 0
                    || this.mob.getNavigation().isDone()
                    || this.lastPathTargetPos == null
                    || targetPos.distanceToSqr(this.lastPathTargetPos) >= REPATH_TARGET_MOVE_SQR) {
                double desiredY = BookMoveControl.resolveCombatAltitudeForIncline(this.mob, primaryTarget);
                this.mob.getNavigation().moveTo(
                        targetPos.x,
                        desiredY,
                        targetPos.z,
                        this.speedModifier
                );
                this.lastPathTargetPos = targetPos;
                this.repathCooldown = REPATH_INTERVAL_TICKS + this.mob.getRandom().nextInt(6);
            }
        } else {
            if (!this.mob.getNavigation().isDone()) {
                this.mob.getNavigation().stop();
            }
            this.lastPathTargetPos = null;
        }

        this.mob.getLookControl().setLookAt(primaryTarget, 35.0F, 85.0F);

        if (--this.attackTime <= 0) {
            if (!this.tryInitiateCast(primaryTarget, withinAttackRange, canSeePrimary)) {
                this.attackTime = 5;
                return;
            }
            this.chargeTime = 40;
            this.hiddenBehindCoverTicks = 0;
            LivingEntity focusTarget = this.activeRoleTarget != null ? this.activeRoleTarget : primaryTarget;
            this.lastSeenTargetPos = focusTarget.getEyePosition();
            this.mob.setAnimationState(getAnimationStateForCast(this.activeRole, this.activeSpellTarget, this.activeCastType));
        }
    }

    private void tickChargingCast(LivingEntity primaryTarget) {
        if (this.selectedSpellField != null
                && this.selectedSpellField.griefing()
                && !this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            this.resetChargeState();
            this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
            this.attackTime = 15;
            return;
        }
        if (!this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().stop();
        }
        this.mob.setAnimationState(getAnimationStateForCast(this.activeRole, this.activeSpellTarget, this.activeCastType));

        if (this.activeSpellTarget == SpellTarget.SELF) {
            if (this.activeRole == SpellRole.DEFENSIVE) {
                this.mob.setXRot(-90.0F);
            } else {
                this.mob.getLookControl().setLookAt(primaryTarget, 35.0F, 85.0F);
            }
            if (--this.chargeTime <= 0) {
                this.executeActiveCast(this.mob);
                this.completeCastCycle();
            }
            return;
        }

        boolean targetingAlly = this.activeSpellTarget == SpellTarget.ALLY;
        LivingEntity aimTarget;
        if (targetingAlly) {
            if (this.activeRoleTarget == null || !this.activeRoleTarget.isAlive() || this.activeRoleTarget == this.mob) {
                this.activeRoleTarget = this.findBestSupportAlly();
            }
            if (this.activeRoleTarget == null) {
                this.resetChargeState();
                this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
                this.attackTime = 15;
                return;
            }
            aimTarget = this.activeRoleTarget;
        } else {
            aimTarget = primaryTarget;
        }

        double maxAllowedDistSqr = targetingAlly
                ? (ALLY_SUPPORT_RANGE * ALLY_SUPPORT_RANGE)
                : (double) this.attackRadiusSqr;
        boolean withinRange = this.mob.distanceToSqr(aimTarget) <= maxAllowedDistSqr;

        // Throttle LoS raycasts during the charge phase until the final release ticks
        boolean canSee;
        if (aimTarget == primaryTarget && this.chargeTime > 2) {
            canSee = this.checkPrimaryLineOfSightThrottled(primaryTarget);
        } else {
            canSee = this.hasLineOfSightToTarget(aimTarget);
        }

        boolean requiresProjectileClearance = this.activeCastType == CastType.PROJECTILE;
        boolean hasClearance = !requiresProjectileClearance || this.hasFrontClearance(aimTarget);

        if (canSee) {
            this.lastSeenTargetPos = aimTarget.getEyePosition();
            this.hiddenBehindCoverTicks = 0;
            this.mob.getLookControl().setLookAt(aimTarget, 35.0F, 85.0F);
        } else if (this.lastSeenTargetPos != null) {
            this.mob.getLookControl().setLookAt(
                    this.lastSeenTargetPos.x,
                    this.lastSeenTargetPos.y,
                    this.lastSeenTargetPos.z,
                    35.0F,
                    85.0F
            );
        } else {
            this.mob.getLookControl().setLookAt(aimTarget, 35.0F, 85.0F);
        }

        if (this.chargeTime > 0) {
            this.chargeTime--;
        }

        if (canSee && hasClearance && withinRange) {
            if (this.activeSpellTarget == SpellTarget.HOSTILE && !this.isImpactAreaSafe(aimTarget.position(), aimTarget)) {
                this.resetChargeState();
                this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
                this.attackTime = 15;
                return;
            }
            if (this.chargeTime <= 0) {
                this.executeActiveCast(aimTarget);
                this.completeCastCycle();
            }
        } else {
            this.hiddenBehindCoverTicks++;
            boolean targetVacated = !withinRange
                    || (this.lastSeenTargetPos != null
                    && aimTarget.getEyePosition().distanceToSqr(this.lastSeenTargetPos) > VACATED_DISTANCE_SQR);
            if (this.hiddenBehindCoverTicks > MAX_HOLD_ANGLE_TICKS || targetVacated) {
                this.resetChargeState();
                this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
                this.attackTime = 15;
                double desiredY = BookMoveControl.resolveCombatAltitudeForIncline(this.mob, primaryTarget);
                this.mob.getNavigation().moveTo(
                        primaryTarget.getX(),
                        desiredY,
                        primaryTarget.getZ(),
                        this.speedModifier
                );
                this.lastPathTargetPos = primaryTarget.position();
                this.repathCooldown = REPATH_INTERVAL_TICKS;
            }
        }
    }

    private void completeCastCycle() {
        this.resetChargeState();
        this.mob.setAnimationState(BookAnimationState.TARGET_ACQUISITION);
        this.attackTime = this.castIntervalMin
                + this.mob.getRandom().nextInt(this.castIntervalMax - this.castIntervalMin + 1);
    }

    @Nullable
    private LivingEntity resolveTargetEntity(
            SpellTarget spellTarget,
            CastType castType,
            LivingEntity hostileTarget,
            boolean withinAttackRange,
            boolean canSeeHostile,
            @Nullable LivingEntity supportAlly
    ) {
        return switch (spellTarget) {
            case SELF -> this.mob;
            case ALLY -> {
                if (supportAlly == null) {
                    yield null;
                }
                if (castType == CastType.PROJECTILE && !this.hasFrontClearance(supportAlly)) {
                    yield null;
                }
                yield supportAlly;
            }
            case HOSTILE -> {
                if (!withinAttackRange || !canSeeHostile || !this.isImpactAreaSafe(hostileTarget.position(), hostileTarget)) {
                    yield null;
                }
                if (castType == CastType.PROJECTILE && !this.hasFrontClearance(hostileTarget)) {
                    yield null;
                }
                yield hostileTarget;
            }
        };
    }

    private boolean tryInitiateCast(LivingEntity hostileTarget, boolean withinAttackRange, boolean canSeeHostile) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        boolean defensiveReady = this.defensiveCooldown <= 0
                && (this.mob.getHealth() < this.mob.getMaxHealth() * 0.85F
                || this.mob.hurtTime > 0
                || (withinAttackRange && canSeeHostile));
        boolean supportiveReady = this.supportiveCooldown <= 0;
        LivingEntity supportAlly = (defensiveReady || supportiveReady) ? this.findBestSupportAlly() : null;

        List<CastOption> options = new ArrayList<>();
        int totalWeight = 0;

        if (withinAttackRange) {
            CastOption offensiveOpt = this.buildRoleOption(
                    serverLevel,
                    SpellRole.OFFENSIVE,
                    hostileTarget,
                    withinAttackRange,
                    canSeeHostile,
                    supportAlly
            );
            if (offensiveOpt != null) {
                options.add(offensiveOpt);
                totalWeight += offensiveOpt.weight();
            }
        }
        if (defensiveReady) {
            CastOption defensiveOpt = this.buildRoleOption(
                    serverLevel,
                    SpellRole.DEFENSIVE,
                    hostileTarget,
                    withinAttackRange,
                    canSeeHostile,
                    supportAlly
            );
            if (defensiveOpt != null) {
                options.add(defensiveOpt);
                totalWeight += defensiveOpt.weight();
            }
        }
        if (supportiveReady) {
            CastOption supportiveOpt = this.buildRoleOption(
                    serverLevel,
                    SpellRole.SUPPORTIVE,
                    hostileTarget,
                    withinAttackRange,
                    canSeeHostile,
                    supportAlly
            );
            if (supportiveOpt != null) {
                options.add(supportiveOpt);
                totalWeight += supportiveOpt.weight();
            }
        }

        if (options.isEmpty() || totalWeight <= 0) {
            return false;
        }

        CastOption chosen = options.getFirst();
        int roll = this.mob.getRandom().nextInt(totalWeight);
        for (CastOption option : options) {
            roll -= option.weight();
            if (roll < 0) {
                chosen = option;
                break;
            }
        }

        this.activeRole = chosen.role();
        this.activeSpellTarget = chosen.spellTarget();
        this.activeCastType = chosen.castType();
        this.activeRoleTarget = chosen.targetEntity();
        this.selectedSpellField = chosen.spellField();
        this.mob.setCastSchoolKey(chosen.castSchool());
        return true;
    }

    @Nullable
    private CastOption buildRoleOption(
            ServerLevel serverLevel,
            SpellRole role,
            LivingEntity hostileTarget,
            boolean withinAttackRange,
            boolean canSeeHostile,
            @Nullable LivingEntity supportAlly
    ) {
        Difficulty currentDifficulty = serverLevel.getDifficulty();
        if (currentDifficulty == Difficulty.PEACEFUL) {
            return null;
        }

        var registryOpt = serverLevel.registryAccess().registry(AiSpellFields.REGISTRY_KEY);
        ResourceKey<EnchantmentTheme> entityTheme = this.mob.getThemeKey();
        ResourceKey<MagicSchool> entitySchool = this.mob.getSchoolKey();
        boolean canGrief = serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);

        List<AiSpellField> candidates = new ArrayList<>();
        List<LivingEntity> candidateTargets = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int poolWeight = 0;

        if (registryOpt.isPresent()) {
            for (AiSpellField field : registryOpt.get()) {
                if (field.role() != role) {
                    continue;
                }
                if (!field.isDifficultyMet(currentDifficulty)) {
                    continue;
                }
                if (field.griefing() && !canGrief) {
                    continue;
                }
                LivingEntity resolvedTarget = this.resolveTargetEntity(
                        field.target(),
                        field.castType(),
                        hostileTarget,
                        withinAttackRange,
                        canSeeHostile,
                        supportAlly
                );
                if (resolvedTarget == null) {
                    continue;
                }
                int w = field.calculateEffectiveWeight(serverLevel.registryAccess(), entityTheme, entitySchool);
                if (w > 0) {
                    candidates.add(field);
                    candidateTargets.add(resolvedTarget);
                    weights.add(w);
                    poolWeight += w;
                }
            }
        }

        if (candidates.isEmpty() || poolWeight <= 0) {
            if (role == SpellRole.DEFENSIVE
                    && this.mob.getAbsorptionAmount() >= Config.DEFENSIVE_ABSORPTION_AMOUNT.get()
                    && this.mob.getHealth() >= this.mob.getMaxHealth() * 0.85F
                    && this.mob.hurtTime <= 0) {
                return null;
            }
            SpellTarget fallbackSpellTarget = role.getDefaultTarget();
            CastType fallbackCastType = role.getDefaultCastType();
            LivingEntity fallbackEntity = this.resolveTargetEntity(
                    fallbackSpellTarget,
                    fallbackCastType,
                    hostileTarget,
                    withinAttackRange,
                    canSeeHostile,
                    supportAlly
            );
            if (fallbackEntity == null && role == SpellRole.SUPPORTIVE && !hostileTarget.hasEffect(MobEffects.WEAKNESS)) {
                LivingEntity hostileFallback = this.resolveTargetEntity(
                        SpellTarget.HOSTILE,
                        CastType.DIRECT,
                        hostileTarget,
                        withinAttackRange,
                        canSeeHostile,
                        supportAlly
                );
                if (hostileFallback != null) {
                    fallbackSpellTarget = SpellTarget.HOSTILE;
                    fallbackCastType = CastType.DIRECT;
                    fallbackEntity = hostileFallback;
                }
            }
            if (fallbackEntity == null) {
                return null;
            }
            return new CastOption(role, fallbackSpellTarget, fallbackCastType, fallbackEntity, null, getDefaultSchoolForRole(role), 10);
        }

        AiSpellField chosenField = candidates.getFirst();
        LivingEntity chosenTarget = candidateTargets.getFirst();
        int chosenWeight = weights.getFirst();
        int roll = this.mob.getRandom().nextInt(poolWeight);
        for (int i = 0; i < candidates.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                chosenField = candidates.get(i);
                chosenTarget = candidateTargets.get(i);
                chosenWeight = weights.get(i);
                break;
            }
        }

        ResourceKey<MagicSchool> resolvedSchool = chosenField
                .resolveSpellSchool(serverLevel.registryAccess(), entitySchool)
                .orElseGet(() -> getDefaultSchoolForRole(role));
        CastType effectiveCastType = chosenField.target() == SpellTarget.SELF
                ? CastType.DIRECT
                : chosenField.castType();

        return new CastOption(
                role,
                chosenField.target(),
                effectiveCastType,
                chosenTarget,
                chosenField,
                resolvedSchool,
                chosenWeight
        );
    }

    @Nullable
    private LivingEntity findBestSupportAlly() {
        AABB allyBox = this.mob.getBoundingBox().inflate(ALLY_SUPPORT_RANGE);
        List<LivingEntity> allies = this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                allyBox,
                ally -> ally != this.mob
                        && ally.isAlive()
                        && this.mob.isBookAlly(ally)
                        && ally != this.mob.getTarget()
                        && this.hasLineOfSightToTarget(ally)
        );
        if (allies.isEmpty()) {
            return null;
        }
        allies.sort(Comparator
                .<LivingEntity, Boolean>comparing(ally -> ally.hasEffect(MobEffects.DAMAGE_RESISTANCE) && ally.getHealth() >= ally.getMaxHealth() * 0.85F)
                .thenComparingDouble(ally -> (double) (ally.getHealth() / Math.max(1.0F, ally.getMaxHealth()))));
        return allies.getFirst();
    }

    private void executeActiveCast(LivingEntity target) {
        if (this.activeRole == null) {
            return;
        }
        switch (this.activeRole) {
            case OFFENSIVE -> this.executeOffensiveCast(target);
            case DEFENSIVE -> this.executeDefensiveCast(target);
            case SUPPORTIVE -> this.executeSupportiveCast(target);
        }
    }

    private void executeOffensiveCast(LivingEntity target) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.activeCastType == CastType.PROJECTILE && target != this.mob) {
            this.fireTrueFlightMissile(
                    serverLevel,
                    target,
                    this.selectedSpellField != null ? this.selectedSpellField.spellField() : null,
                    this.selectedSpellField != null ? this.selectedSpellField.anchor() : Optional.empty()
            );
        } else if (this.selectedSpellField != null) {
            this.selectedSpellField.deployAt(
                    serverLevel,
                    target.position(),
                    this.mob,
                    target,
                    this.mob.getMainHandItem()
            );
        }
    }

    private void executeDefensiveCast(LivingEntity target) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.defensiveCooldown = 160 + this.mob.getRandom().nextInt(60);

        LivingEntity wardRecipient = (this.activeSpellTarget == SpellTarget.ALLY && target != null) ? target : this.mob;
        float newAbsorption = Math.max(wardRecipient.getAbsorptionAmount(), Config.DEFENSIVE_ABSORPTION_AMOUNT.get());
        AttributeInstance maxAbsorbAttr = wardRecipient.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorbAttr != null && maxAbsorbAttr.getBaseValue() < (double) newAbsorption) {
            maxAbsorbAttr.setBaseValue(newAbsorption);
        }
        wardRecipient.setAbsorptionAmount(newAbsorption);

        serverLevel.playSound(
                null,
                wardRecipient.getX(),
                wardRecipient.getY(),
                wardRecipient.getZ(),
                SoundEvents.BEACON_ACTIVATE,
                this.mob.getSoundSource(),
                0.9F,
                1.35F + (this.mob.getRandom().nextFloat() - 0.5F) * 0.15F
        );
        serverLevel.sendParticles(
                SimpleParticleTypes.SGA_RUNE.type(),
                wardRecipient.getX(),
                wardRecipient.getY() + wardRecipient.getBbHeight() * 0.5D,
                wardRecipient.getZ(),
                18,
                0.35D,
                0.35D,
                0.35D,
                0.05D
        );

        if (this.activeCastType == CastType.PROJECTILE && target != this.mob) {
            this.fireTrueFlightMissile(
                    serverLevel,
                    target,
                    this.selectedSpellField != null ? this.selectedSpellField.spellField() : null,
                    this.selectedSpellField != null ? this.selectedSpellField.anchor() : Optional.empty()
            );
        } else if (this.selectedSpellField != null) {
            this.selectedSpellField.deployAt(
                    serverLevel,
                    target.position(),
                    this.mob,
                    target,
                    this.mob.getMainHandItem()
            );
        }
    }

    private void executeSupportiveCast(LivingEntity target) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.supportiveCooldown = 140 + this.mob.getRandom().nextInt(60);

        if (this.activeCastType == CastType.PROJECTILE && target != this.mob) {
            if (this.activeSpellTarget != SpellTarget.HOSTILE) {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Config.RESISTANCE_CAST_DURATION_TICKS.get(), Config.RESISTANCE_CAST_LEVEL.get()), this.mob);
            }
            this.fireTrueFlightMissile(
                    serverLevel,
                    target,
                    this.selectedSpellField != null ? this.selectedSpellField.spellField() : null,
                    this.selectedSpellField != null ? this.selectedSpellField.anchor() : Optional.empty()
            );
            return;
        }

        if (this.activeSpellTarget == SpellTarget.HOSTILE) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, Config.WEAKNESS_CAST_DURATION_TICKS.get(), Config.WEAKNESS_CAST_LEVEL.get()), this.mob);
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Config.RESISTANCE_CAST_DURATION_TICKS.get(), Config.RESISTANCE_CAST_LEVEL.get()), this.mob);
        }

        serverLevel.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                this.mob.getSoundSource(),
                0.95F,
                1.25F
        );
        serverLevel.sendParticles(
                SimpleParticleTypes.SGA_RUNE.type(),
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                16,
                0.35D,
                0.4D,
                0.35D,
                0.04D
        );

        if (this.selectedSpellField != null) {
            this.selectedSpellField.deployAt(
                    serverLevel,
                    target.position(),
                    this.mob,
                    target,
                    this.mob.getMainHandItem()
            );
        }
    }

    private void fireTrueFlightMissile(
            ServerLevel serverLevel,
            LivingEntity target,
            @Nullable SpellFieldComponent spellField,
            Optional<AiSpellField.AnchorPayload> anchorPayload
    ) {
        MagicMissileEntity missile = new MagicMissileEntity(
                serverLevel,
                this.mob,
                spellField,
                anchorPayload,
                this.mob.getMainHandItem()
        );
        Vec3 spawnPos = this.getMissileSpawnPos(target);
        missile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        double aimHeightFactor = 0.5D + this.mob.getRandom().nextDouble() * 0.38D;
        Vec3 targetAimPos = target.position().add(0.0D, target.getBbHeight() * aimHeightFactor, 0.0D);
        Vec3 targetVel = target.getDeltaMovement();
        double dist = spawnPos.distanceTo(targetAimPos);
        double travelTicks = dist / MISSILE_SPEED;

        double aimX = (targetAimPos.x + targetVel.x * travelTicks * 0.5D) - spawnPos.x;
        double aimY = targetAimPos.y - spawnPos.y;
        double aimZ = (targetAimPos.z + targetVel.z * travelTicks * 0.5D) - spawnPos.z;

        missile.shoot(aimX, aimY, aimZ, (float) MISSILE_SPEED, 0.0F);
        serverLevel.addFreshEntity(missile);

        serverLevel.playSound(
                null,
                spawnPos.x,
                spawnPos.y,
                spawnPos.z,
                SoundEvents.ILLUSIONER_CAST_SPELL,
                this.mob.getSoundSource(),
                1.0F,
                1.2F + (this.mob.getRandom().nextFloat() - 0.5F) * 0.2F
        );
        serverLevel.playSound(
                null,
                spawnPos.x,
                spawnPos.y,
                spawnPos.z,
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                this.mob.getSoundSource(),
                0.9F,
                1.4F + (this.mob.getRandom().nextFloat() - 0.5F) * 0.2F
        );
    }

    private boolean isImpactAreaSafe(Vec3 impactPos, LivingEntity intendedTarget) {
        if (!Config.ENABLE_SPELL_AREA_CLEARANCE_CHECKS.get()) {
            return true;
        }
        if (this.mob.isAvoidCollateralEntity(intendedTarget)) {
            return false;
        }
        AABB blastBox = AABB.ofSize(impactPos, AOE_SAFETY_RADIUS * 2.0D, AOE_SAFETY_RADIUS * 2.0D, AOE_SAFETY_RADIUS * 2.0D);
        List<LivingEntity> avoidCollateralEntities = this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                blastBox,
                entity -> entity.isAlive() && this.mob.isAvoidCollateralEntity(entity)
        );
        if (!avoidCollateralEntities.isEmpty()) {
            return false;
        }
        List<Player> pacifiedPlayers = this.mob.level().getEntitiesOfClass(
                Player.class,
                blastBox,
                player -> player != intendedTarget && player.isAlive() && this.mob.isPlayerPacified(player)
        );
        return pacifiedPlayers.isEmpty();
    }

    private record CastOption(
            SpellRole role,
            SpellTarget spellTarget,
            CastType castType,
            LivingEntity targetEntity,
            @Nullable AiSpellField spellField,
            ResourceKey<MagicSchool> castSchool,
            int weight
    ) {}
}