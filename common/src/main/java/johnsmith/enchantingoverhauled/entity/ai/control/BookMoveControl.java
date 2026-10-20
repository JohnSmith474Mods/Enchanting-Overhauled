package johnsmith.enchantingoverhauled.entity.ai.control;

import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BookMoveControl extends FlyingMoveControl {

    public static final double MAX_EUCLIDEAN_ATTACK_RANGE = 18.0D;

    private final AbstractFlyingBookEntity book;
    private final int maxTurn;
    private int orbitDirection = 1;
    private int orbitFlipTimer = 0;

    public BookMoveControl(AbstractFlyingBookEntity book, int maxTurn, boolean hoversInPlace) {
        super(book, maxTurn, hoversInPlace);
        this.book = book;
        this.maxTurn = maxTurn;
    }

    @Override
    public void tick() {
        BookAnimationState state = this.book.getAnimationState();
        if (this.book.isRoosting()) {
            this.book.setNoGravity(true);
            this.book.setDeltaMovement(this.book.getDeltaMovement().scale(0.65D));
            return;
        }

        if (state == BookAnimationState.DASHING) {
            this.book.setNoGravity(true);
            if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                Vec3 toWanted = new Vec3(
                        this.wantedX - this.book.getX(),
                        this.wantedY - this.book.getY(),
                        this.wantedZ - this.book.getZ()
                );
                Vec3 currentVel = this.book.getDeltaMovement();
                double speed = currentVel.length();
                if (speed > 0.04D && toWanted.lengthSqr() > 0.05D) {
                    Vec3 steered = currentVel.normalize().scale(0.7D).add(toWanted.normalize().scale(0.3D)).normalize().scale(speed);
                    this.book.setDeltaMovement(steered);
                }
            }
            return;
        }

        LivingEntity target = this.book.getTarget();

        if (state.isChanting() || state == BookAnimationState.BARTERING) {
            this.book.setNoGravity(true);
            Vec3 damped = this.book.getDeltaMovement().scale(0.5D);
            if (state.isChanting() && target != null && target.isAlive()) {
                double minSafeY = getMinimumSafeCombatAltitude(target);
                if (this.book.getY() < minSafeY - 0.25D && hasHeadroom(this.book, this.book.getY() + 0.8D)) {
                    double climb = Mth.clamp((minSafeY - this.book.getY()) * 0.09D, 0.025D, 0.1D);
                    damped = new Vec3(damped.x, Math.max(damped.y, climb), damped.z);
                }
            }
            this.book.setDeltaMovement(damped);
            this.syncBodyToHeadRotation();
            return;
        }

        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            this.book.setNoGravity(true);

            double targetX = this.wantedX;
            double targetY = this.wantedY;
            double targetZ = this.wantedZ;

            if (target != null && target.isAlive()) {
                double desiredY = resolveCombatAltitudeForIncline(this.book, target);
                double clampedY = resolveHighestClearAltitude(this.book, desiredY);
                targetY = Mth.lerp(0.75D, targetY, clampedY);
            }

            double dx = targetX - this.book.getX();
            double dy = targetY - this.book.getY();
            double dz = targetZ - this.book.getZ();
            double distSqr = dx * dx + dy * dy + dz * dz;

            if (distSqr < 2.5000003E-7D) {
                this.book.setYya(0.0F);
                this.book.setZza(0.0F);
                if (target != null && target.isAlive()) {
                    this.faceEntity(target);
                }
                return;
            }

            float baseSpeed = (float) (this.speedModifier * this.book.getAttributeValue(Attributes.FLYING_SPEED));
            Vec3 currentVel = this.book.getDeltaMovement();
            Vec3 desiredDir = new Vec3(dx, dy, dz).normalize();

            if (target != null && target.isAlive()) {
                if (--this.orbitFlipTimer <= 0) {
                    this.orbitFlipTimer = 40 + this.book.getRandom().nextInt(40);
                    if (this.book.getRandom().nextFloat() < 0.35F) {
                        this.orbitDirection = -this.orbitDirection;
                    }
                }
                Vec3 toTarget = target.position().subtract(this.book.position());
                Vec3 horizontalTangent = new Vec3(-toTarget.z * this.orbitDirection, 0.0D, toTarget.x * this.orbitDirection);
                if (horizontalTangent.lengthSqr() > 0.001D) {
                    horizontalTangent = horizontalTangent.normalize();
                    if (!this.isDirectionClear(horizontalTangent, 1.6D)) {
                        this.orbitDirection = -this.orbitDirection;
                        horizontalTangent = horizontalTangent.scale(-1.0D);
                    }
                    double strafeWeight = isTargetRanged(target) ? 0.55D : 0.45D;
                    desiredDir = desiredDir.scale(1.0D - strafeWeight).add(horizontalTangent.scale(strafeWeight)).normalize();
                }
            }

            double accel = 0.18D;
            Vec3 targetVel = desiredDir.scale(baseSpeed * 0.65D);
            Vec3 nextVel = new Vec3(
                    Mth.lerp(accel, currentVel.x, targetVel.x),
                    Mth.lerp(accel, currentVel.y, targetVel.y),
                    Mth.lerp(accel, currentVel.z, targetVel.z)
            );
            this.book.setDeltaMovement(nextVel);

            if (target != null && target.isAlive()) {
                this.faceEntity(target);
            } else {
                float targetYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
                float newYaw = this.rotlerp(this.book.getYRot(), targetYaw, (float) this.maxTurn);
                this.book.setYRot(newYaw);
                this.book.yBodyRot = newYaw;
                this.book.yHeadRot = newYaw;
            }
        } else if (target != null && target.isAlive() && state == BookAnimationState.TARGET_ACQUISITION) {
            this.tickHoverStrafe(target);
            this.faceEntity(target);
        } else {
            this.book.setNoGravity(true);
            this.book.setYya(0.0F);
            this.book.setZza(0.0F);
        }
    }

    private void faceEntity(LivingEntity target) {
        double lx = target.getX() - this.book.getX();
        double ly = target.getEyeY() - this.book.getEyeY();
        double lz = target.getZ() - this.book.getZ();
        double horiz = Math.sqrt(lx * lx + lz * lz);

        float targetYaw = (float) (Mth.atan2(lz, lx) * Mth.RAD_TO_DEG) - 90.0F;
        float targetPitch = (float) (-(Mth.atan2(ly, horiz) * Mth.RAD_TO_DEG));

        float newYaw = this.rotlerp(this.book.getYRot(), targetYaw, 35.0F);
        float newPitch = this.rotlerp(this.book.getXRot(), targetPitch, 30.0F);

        this.book.setYRot(newYaw);
        this.book.yBodyRot = newYaw;
        this.book.yHeadRot = newYaw;
        this.book.setXRot(newPitch);
    }

    private void syncBodyToHeadRotation() {
        this.book.setYRot(this.book.yHeadRot);
        this.book.yBodyRot = this.book.yHeadRot;
    }

    private void tickHoverStrafe(LivingEntity target) {
        this.book.setNoGravity(true);
        if (--this.orbitFlipTimer <= 0) {
            this.orbitFlipTimer = 40 + this.book.getRandom().nextInt(40);
            this.orbitDirection = -this.orbitDirection;
        }

        boolean rangedTarget = isTargetRanged(target);
        double desiredAltitude = resolveHighestClearAltitude(this.book, resolveCombatAltitudeForIncline(this.book, target));
        double dy = desiredAltitude - this.book.getY();

        Vec3 toTarget = target.position().subtract(this.book.position());
        double euclideanDist = Math.sqrt(this.book.distanceToSqr(target));
        double horizDist = toTarget.horizontalDistance();
        Vec3 radial = horizDist > 0.001D ? new Vec3(toTarget.x / horizDist, 0.0D, toTarget.z / horizDist) : Vec3.ZERO;
        Vec3 tangent = new Vec3(-radial.z * this.orbitDirection, 0.0D, radial.x * this.orbitDirection);

        if (!this.isDirectionClear(tangent, 1.75D)) {
            this.orbitDirection = -this.orbitDirection;
            this.orbitFlipTimer = 45;
            tangent = tangent.scale(-1.0D);
        }

        double radialCorrection = 0.0D;
        if (euclideanDist > MAX_EUCLIDEAN_ATTACK_RANGE - 0.75D) {
            radialCorrection = 0.14D;
        } else if (rangedTarget && euclideanDist < 4.5D) {
            radialCorrection = this.isDirectionClear(radial.scale(-1.0D), 1.6D) ? -0.14D : 0.06D;
        } else if (!rangedTarget && horizDist < 3.2D && isWithinMeleeStrikingRange(this.book, target)) {
            radialCorrection = this.isDirectionClear(radial.scale(-1.0D), 1.5D) ? -0.11D : 0.0D;
        }

        double strafeSpeed = rangedTarget ? 0.14D : 0.12D;
        double verticalSpeed;
        if (dy > 0.12D) {
            boolean meleeThreatBelow = !rangedTarget && horizDist < 6.0D;
            double maxClimb = meleeThreatBelow ? 0.25D : 0.16D;
            verticalSpeed = Mth.clamp(dy * 0.22D, 0.06D, maxClimb);
        } else {
            verticalSpeed = Mth.clamp(dy * 0.08D, -0.06D, 0.12D);
        }

        Vec3 desiredVel = tangent.scale(strafeSpeed)
                .add(radial.scale(radialCorrection))
                .add(0.0D, verticalSpeed, 0.0D);

        this.book.setDeltaMovement(this.book.getDeltaMovement().scale(0.72D).add(desiredVel.scale(0.28D)));
    }

    public static double getMinimumSafeCombatAltitude(LivingEntity target) {
        if (isTargetRanged(target)) {
            return target.getY() + Math.max(0.85D, target.getBbHeight() * 0.45D);
        }
        return target.getY() + target.getBbHeight() + 2.4D;
    }

    public static double getDesiredCombatAltitude(LivingEntity target) {
        return getMinimumSafeCombatAltitude(target);
    }

    public static double resolveCombatAltitudeForIncline(AbstractFlyingBookEntity book, LivingEntity target) {
        double minSafeY = getMinimumSafeCombatAltitude(target);
        double maxAllowedY = target.getEyeY() + 10.5D;
        if (book.getY() > minSafeY && book.distanceToSqr(target) <= (MAX_EUCLIDEAN_ATTACK_RANGE * MAX_EUCLIDEAN_ATTACK_RANGE)) {
            return Math.min(book.getY(), maxAllowedY);
        }
        return minSafeY;
    }

    public static double getMeleeVerticalStrikeLimit(LivingEntity target) {
        if (target instanceof Player) {
            return target.getEyeY() + 3.1D;
        }
        return target.getY() + target.getBbHeight() + 1.45D;
    }

    public static boolean isWithinMeleeStrikingRange(AbstractFlyingBookEntity book, LivingEntity target) {
        if (target == null || !target.isAlive() || isTargetRanged(target)) {
            return false;
        }
        double verticalLimit = getMeleeVerticalStrikeLimit(target);
        if (book.getY() >= verticalLimit) {
            return false;
        }
        if (book.getY() + book.getBbHeight() < target.getY() - 0.8D) {
            return false;
        }
        double horizDist = book.position().subtract(target.position()).horizontalDistance();
        double horizStrikeLimit = (target.getBbWidth() * 0.5D)
                + (book.getBbWidth() * 0.5D)
                + (target instanceof Player ? 3.2D : 1.95D);
        return horizDist <= horizStrikeLimit;
    }

    public static boolean canAscendFromMelee(AbstractFlyingBookEntity book, LivingEntity target) {
        double desiredY = getMinimumSafeCombatAltitude(target);
        if (book.getY() >= desiredY - 0.2D) {
            return true;
        }
        return hasHeadroom(book, book.getY() + 1.0D);
    }

    public static double resolveHighestClearAltitude(AbstractFlyingBookEntity book, double desiredY) {
        if (desiredY <= book.getY()) {
            return desiredY;
        }
        double currentY = book.getY();
        double bestY = currentY;
        double step = 0.5D;
        for (double testY = currentY + step; testY <= desiredY + 0.01D; testY += step) {
            if (hasHeadroom(book, testY)) {
                bestY = testY;
            } else {
                break;
            }
        }
        return bestY;
    }

    public static boolean hasHeadroom(AbstractFlyingBookEntity book, double targetY) {
        if (targetY <= book.getY()) {
            return true;
        }
        BlockPos checkPos = BlockPos.containing(book.getX(), targetY + book.getBbHeight() + 0.35D, book.getZ());
        return book.level().getBlockState(checkPos).getCollisionShape(book.level(), checkPos).isEmpty();
    }

    private boolean isDirectionClear(Vec3 dir, double distance) {
        if (dir.lengthSqr() < 0.0001D) {
            return true;
        }
        Vec3 start = this.book.position().add(0.0D, this.book.getBbHeight() * 0.5D, 0.0D);
        Vec3 end = start.add(dir.normalize().scale(distance));
        HitResult hit = this.book.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.book
        ));
        return hit.getType() == HitResult.Type.MISS;
    }

    public static boolean isTargetRanged(LivingEntity target) {
        if (target instanceof RangedAttackMob) {
            return true;
        }
        return isRangedWeapon(target.getMainHandItem()) || isRangedWeapon(target.getOffhandItem());
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(Tags.ALL_RANGED)
                || stack.getItem() instanceof ProjectileWeaponItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem;
    }
}