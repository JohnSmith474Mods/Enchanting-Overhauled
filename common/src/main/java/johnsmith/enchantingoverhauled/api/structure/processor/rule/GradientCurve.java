package johnsmith.enchantingoverhauled.api.structure.processor.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A sealed interface representing a mathematical easing/scaling curve applied to a normalized
 * position fraction {@code t} in {@code [0.0, 1.0]} within a {@link GradientOutput}.
 */
public sealed interface GradientCurve permits
        GradientCurve.Linear,
        GradientCurve.Sigmoid,
        GradientCurve.Logarithmic,
        GradientCurve.Polynomial,
        GradientCurve.Exponential {

    /**
     * Dispatch codec for serializing and deserializing any {@link GradientCurve} implementation
     * based on its {@code "type"} field.
     */
    Codec<GradientCurve> CODEC = Codec.STRING.dispatch(
            "type",
            GradientCurve::getTypeId,
            id -> switch (id) {
                case "linear" -> Linear.CODEC;
                case "sigmoid" -> Sigmoid.CODEC;
                case "logarithmic" -> Logarithmic.CODEC;
                case "polynomial" -> Polynomial.CODEC;
                case "exponential" -> Exponential.CODEC;
                default -> throw new IllegalArgumentException("Unknown GradientCurve type: " + id);
            }
    );

    /**
     * Retrieves the unique string identifier for this curve type.
     *
     * @return The curve type ID string.
     */
    String getTypeId();

    /**
     * Applies the selected scaling curve to the provided normalized fraction {@code t}.
     *
     * @param t A float between {@code 0.0} and {@code 1.0} representing distance along the gradient.
     * @return The scaled float representing the new weight mapped along the curve.
     */
    float apply(float t);

    /**
     * A linear 1:1 gradient curve ({@code f(t) = clamp(t, 0, 1)}).
     */
    record Linear() implements GradientCurve {
        /**
         * Unit {@link MapCodec} for {@link Linear}.
         */
        public static final MapCodec<Linear> CODEC = MapCodec.unit(new Linear());

        @Override
        public String getTypeId() {
            return "linear";
        }

        @Override
        public float apply(float t) {
            return Math.max(0.0F, Math.min(1.0F, t));
        }
    }

    /**
     * A logistic S-curve ({@code f(t) = 1 / (1 + e^(-steepness * (t - midpoint)))}) for sharp or smooth transitions.
     *
     * @param steepness Controls the slope at the midpoint (default: {@code 10.0F}).
     * @param midpoint  The normalized inflection point in {@code [0.0, 1.0]} (default: {@code 0.5F}).
     */
    record Sigmoid(float steepness, float midpoint) implements GradientCurve {
        /**
         * The {@link MapCodec} for serializing and deserializing a {@link Sigmoid} curve.
         */
        public static final MapCodec<Sigmoid> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("steepness", 10.0F).forGetter(Sigmoid::steepness),
                        Codec.FLOAT.optionalFieldOf("midpoint", 0.5F).forGetter(Sigmoid::midpoint)
                ).apply(instance, Sigmoid::new)
        );

        @Override
        public String getTypeId() {
            return "sigmoid";
        }

        @Override
        public float apply(float t) {
            t = Math.max(0.0F, Math.min(1.0F, t));
            return (float) (1.0 / (1.0 + Math.exp(-this.steepness * (t - this.midpoint))));
        }
    }

    /**
     * A logarithmic curve ({@code f(t) = ln(1 + baseMultiplier * t) / ln(1 + baseMultiplier)})
     * that rises rapidly near {@code 0.0} and flattens toward {@code 1.0}.
     *
     * @param baseMultiplier The logarithmic scaling factor (default: {@code 9.0F}).
     */
    record Logarithmic(float baseMultiplier) implements GradientCurve {
        /**
         * The {@link MapCodec} for serializing and deserializing a {@link Logarithmic} curve.
         */
        public static final MapCodec<Logarithmic> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("base_multiplier", 9.0F).forGetter(Logarithmic::baseMultiplier)
                ).apply(instance, Logarithmic::new)
        );

        @Override
        public String getTypeId() {
            return "logarithmic";
        }

        @Override
        public float apply(float t) {
            t = Math.max(0.0F, Math.min(1.0F, t));
            // Math.log1p(x) is equivalent to ln(1 + x)
            return (float) (Math.log1p(this.baseMultiplier * t) / Math.log1p(this.baseMultiplier));
        }
    }

    /**
     * A power/polynomial curve ({@code f(t) = t^exponent}).
     *
     * @param exponent The power exponent applied to {@code t} (default: {@code 2.0F}).
     */
    record Polynomial(float exponent) implements GradientCurve {
        /**
         * The {@link MapCodec} for serializing and deserializing a {@link Polynomial} curve.
         */
        public static final MapCodec<Polynomial> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("exponent", 2.0F).forGetter(Polynomial::exponent)
                ).apply(instance, Polynomial::new)
        );

        @Override
        public String getTypeId() {
            return "polynomial";
        }

        @Override
        public float apply(float t) {
            t = Math.max(0.0F, Math.min(1.0F, t));
            return (float) Math.pow(t, this.exponent);
        }
    }

    /**
     * A normalized exponential curve ({@code f(t) = (base^t - 1) / (base - 1)}).
     *
     * @param base The exponential base (default: {@link Math#E}).
     */
    record Exponential(float base) implements GradientCurve {
        /**
         * The {@link MapCodec} for serializing and deserializing an {@link Exponential} curve.
         */
        public static final MapCodec<Exponential> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.FLOAT.optionalFieldOf("base", (float) Math.E).forGetter(Exponential::base)
                ).apply(instance, Exponential::new)
        );

        @Override
        public String getTypeId() {
            return "exponential";
        }

        @Override
        public float apply(float t) {
            t = Math.max(0.0F, Math.min(1.0F, t));
            if (this.base == 1.0F) return t; // Safety check to prevent divide by zero
            return (float) ((Math.pow(this.base, t) - 1.0) / (this.base - 1.0));
        }
    }
}