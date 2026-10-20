package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/**
 * Represents a directional axis that combines both global (world-absolute) coordinates
 * and structure-relative (template-local) coordinates for use in {@link GradientOutput}.
 *
 * @param x       Global world X axis weight.
 * @param y       Global world Y axis weight.
 * @param z       Global world Z axis weight.
 * @param right   Template-local X (width/right) axis weight.
 * @param up      Template-local Y (height/up) axis weight.
 * @param forward Template-local Z (length/forward) axis weight.
 */
public record SpatialVector(
        float x, float y, float z,
        float right, float up, float forward
) {

    /**
     * Default vertical axis vector pointing along the global Y axis ({@code y = 1.0F}).
     */
    public static final SpatialVector UP = new SpatialVector(
            0.0F, 1.0F, 0.0F,
            0.0F, 0.0F, 0.0F
    );

    /**
     * The codec responsible for serializing and deserializing {@link SpatialVector} instances from JSON.
     */
    public static final Codec<SpatialVector> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("x", 0.0F).forGetter(SpatialVector::x),
                    Codec.FLOAT.optionalFieldOf("y", 0.0F).forGetter(SpatialVector::y),
                    Codec.FLOAT.optionalFieldOf("z", 0.0F).forGetter(SpatialVector::z),
                    Codec.FLOAT.optionalFieldOf("right", 0.0F).forGetter(SpatialVector::right),
                    Codec.FLOAT.optionalFieldOf("up", 0.0F).forGetter(SpatialVector::up),
                    Codec.FLOAT.optionalFieldOf("forward", 0.0F).forGetter(SpatialVector::forward)
            ).apply(instance, SpatialVector::new)
    );

    /**
     * Calculates the scalar dot-product projection distance of the current block position along this spatial axis.
     * <p>
     * By utilizing the unrotated {@code localPos} for {@link #right}, {@link #up}, and {@link #forward},
     * relative evaluations always apply consistently to the structure template's original orientation
     * regardless of how the piece was rotated into the world.
     *
     * @param localPos    The unrotated template-local block position.
     * @param absolutePos The transformed world-space block position.
     * @return The combined scalar projection distance.
     */
    public float dot(BlockPos localPos, BlockPos absolutePos) {
        float absoluteDot = absolutePos.getX() * this.x
                + absolutePos.getY() * this.y
                + absolutePos.getZ() * this.z;

        // Local pos in templates: X is width (Right), Y is height (Up), Z is length/depth (Forward)
        float relativeDot = localPos.getX() * this.right
                + localPos.getY() * this.up
                + localPos.getZ() * this.forward;

        return absoluteDot + relativeDot;
    }
}