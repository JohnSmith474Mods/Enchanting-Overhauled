package johnsmith.enchantingoverhauled.api.config.data;

import com.mojang.serialization.Codec;

/**
 * Represents a configuration option that holds both its config (name, constraints, scope)
 * and its current runtime value.
 *
 * @param <T> The type of the configuration value.
 */
public abstract sealed class Property<T extends Comparable<T>> permits Property.Bounded, Property.Binary {
    // region Definitions
    public final String resourceName;
    public final String translationKey;
    public final String description;
    public final PropertyGroup parentGroup;
    public final T defaultValue;
    public final Codec<T> codec;

    private volatile T value;

    protected Property(
            String resourceName,
            String description,
            PropertyGroup category,
            T defaultValue,
            Codec<T> codec
    ) {
        this.resourceName = resourceName;
        this.translationKey = createTranslationKey(category, resourceName);
        this.description = description;
        this.parentGroup = category;
        this.defaultValue = defaultValue;
        this.codec = codec;
        this.value = defaultValue;
    }

    protected static String createTranslationKey(PropertyGroup group, String resourceName) {
        return group.translationKey() + "." + resourceName;
    }

    /**
     * Generates the unique, fully-qualified identifier for this config entry.
     * Format: "tabId.groupId.resourceName"
     */
    public String getUniqueId() {
        return this.parentGroup.parent().id() + "." + this.parentGroup.id() + "." + this.resourceName;
    }

    /**
     * Gets the current runtime value.
     */
    public T get() {
        return value;
    }

    /**
     * Sets the new value, running it through validation (bounds check) first.
     */
    public void set(T newValue) {
        this.value = validate(newValue);
    }

    /**
     * Sets the new value, running it through validation (bounds check) first.
     *
     * @return Returns itself for fluent method chaining.
     */
    public Property<T> with(T newValue) {
        this.value = validate(newValue);
        return this;
    }

    /**
     * Validates the input. Subclasses override this to enforce logic like Min/Max.
     */
    protected T validate(T value) {
        return value;
    }// endregion

    // region implementations
    /**
     * A configuration for numeric values that requires hard limits (Floor/Ceiling).
     */
    public static non-sealed class Bounded<T extends Number & Comparable<T>> extends Property<T> {
        public final T lowerBound;
        public final T upperBound;

        public Bounded(
                String resourceName,
                String description,
                PropertyGroup category,
                T defaultValue,
                T lowerBound,
                T upperBound,
                Codec<T> codec
        ) {
            super(resourceName, description, category, defaultValue, codec);
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        protected T validate(T value) {
            if (value.compareTo(lowerBound) < 0) return lowerBound;
            if (value.compareTo(upperBound) > 0) return upperBound;
            return value;
        }
    }

    /**
     * A configuration for simple boolean toggles.
     */
    public static final class Binary extends Property<Boolean> {
        public Binary(
                String resourceName,
                String description,
                PropertyGroup category,
                Boolean defaultValue
        ) {
            super(resourceName, description, category, defaultValue, Codec.BOOL);
        }
    } // endregion
}