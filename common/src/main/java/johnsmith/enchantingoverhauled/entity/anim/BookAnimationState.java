package johnsmith.enchantingoverhauled.entity.anim;

import net.minecraft.util.Mth;

import org.joml.Quaternionf;

public enum BookAnimationState {
    SCOUTING(0.0F, 1.2F, 0.12F, Alignment.VERTICAL, Orientation.FORWARD, 0.0F, 0.0F, 0.0F, false),
    DASHING(0.0F, 1.2F, 0.0F, Alignment.LATERAL, Orientation.BACKWARD, 0.0F, 0.0F, 0.0F, true),
    TARGET_ACQUISITION(0.0F, 1.2F, 0.0F, Alignment.VERTICAL, Orientation.FORWARD, 0.12F, 0.06F, 1.0F, false),
    CHANTING(0.0F, 1.12F, 0.0F, Alignment.VERTICAL, Orientation.FORWARD, 0.45F, 0.35F, 1.0F, true),
    CHANTING_OFFENSIVE(0.0F, 1.12F, 0.0F, Alignment.VERTICAL, Orientation.FORWARD, 0.45F, 0.35F, 1.0F, true),
    CHANTING_DEFENSIVE(0.0F, 1.12F, 0.0F, Alignment.LONGITUDINAL, Orientation.UP, 0.45F, 0.35F, 1.0F, true),
    CHANTING_SUPPORTIVE(0.0F, 1.12F, 0.0F, Alignment.VERTICAL, Orientation.FORWARD, 0.45F, 0.35F, 1.0F, true),
    IDLE(0.0F, 1.2F, 0.12F, Alignment.VERTICAL, Orientation.FORWARD, 0.08F, 0.03F, 1.0F, true),
    RESTING(0.0F, 1.485F, -0.12F, Alignment.LATERAL, Orientation.BACKWARD, 0.0F, 0.0F, 0.0F, false),
    ALTAR_VIGIL(0.0F, 1.2F, 0.0F, Alignment.LONGITUDINAL, Orientation.UP, 0.08F, 0.025F, 1.0F, false),
    LECTERN_ROOST(0.0F, 1.2F, 0.0F, Alignment.LONGITUDINAL, Orientation.UP, 0.0F, 0.0F, 1.0F, false),
    BARTERING(0.0F, 1.2F, 0.0F, Alignment.VERTICAL, Orientation.FORWARD, 0.35F, 0.25F, 1.0F, true);

    public final float xOffset;
    public final float yOffset;
    public final float zOffset;
    public final Alignment alignment;
    public final Orientation orientation;
    public final float flutterSpeed;
    public final float flutterChance;
    public final float openness;
    public final boolean shouldGenerateParticles;

    BookAnimationState(
            float xOffset,
            float yOffset,
            float zOffset,
            Alignment alignment,
            Orientation orientation,
            float flutterSpeed,
            float flutterChance,
            float openness,
            boolean shouldGenerateParticles
    ) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.alignment = alignment;
        this.orientation = orientation;
        this.flutterSpeed = flutterSpeed;
        this.flutterChance = flutterChance;
        this.openness = openness;
        this.shouldGenerateParticles = shouldGenerateParticles;
    }

    public boolean isChanting() {
        return this == CHANTING
                || this == CHANTING_OFFENSIVE
                || this == CHANTING_DEFENSIVE
                || this == CHANTING_SUPPORTIVE;
    }

    public Quaternionf getTargetRotation(float entityPitch) {
        Quaternionf rotation = getBaseRotation();

        if ((this == TARGET_ACQUISITION
                || this == CHANTING
                || this == CHANTING_OFFENSIVE
                || this == CHANTING_SUPPORTIVE
                || this == BARTERING
                || this == DASHING) && entityPitch != 0.0F) {
            rotation.rotateLocalX(entityPitch * Mth.DEG_TO_RAD);
        }

        return rotation;
    }

    private Quaternionf getBaseRotation() {
        return switch (this.alignment) {
            case LATERAL -> switch (this.orientation) {
                case FORWARD -> this.quaternion(270, 90, 180);
                case BACKWARD -> this.quaternion(90, 90, 180);
                case UP -> this.quaternion(0, 0, 270);
                case DOWN -> this.quaternion(0, 0, 90);
                default -> new Quaternionf();
            };
            case VERTICAL -> switch (this.orientation) {
                case FORWARD -> this.quaternion(270, 0, 180);
                case BACKWARD -> this.quaternion(270, 0, 0);
                case LEFT -> this.quaternion(180, 0, 0);
                case RIGHT -> new Quaternionf();
                default -> new Quaternionf();
            };
            case LONGITUDINAL -> switch (this.orientation) {
                case UP -> this.quaternion(90, 180, 90);
                case DOWN -> this.quaternion(90, 0, 90);
                case LEFT -> this.quaternion(180, 90, 270);
                case RIGHT -> this.quaternion(0, 90, 0);
                default -> new Quaternionf();
            };
        };
    }

    private Quaternionf quaternion(float yaw, float pitch, float roll) {
        return new Quaternionf().rotationYXZ(
                yaw * Mth.DEG_TO_RAD,
                pitch * Mth.DEG_TO_RAD,
                roll * Mth.DEG_TO_RAD
        );
    }

    public enum Alignment {
        LATERAL,
        VERTICAL,
        LONGITUDINAL
    }

    public enum Orientation {
        FORWARD,
        BACKWARD,
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
}