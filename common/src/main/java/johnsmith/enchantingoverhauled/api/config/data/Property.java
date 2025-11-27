package johnsmith.enchantingoverhauled.api.config.data;

import com.mojang.serialization.Codec;

import net.minecraft.world.item.enchantment.LevelBasedValue;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An abstract sealed class representing a single configuration option.
 * <p>
 * This is the foundational class for all mod configuration fields, holding common metadata
 * and runtime state. It enforces the separation of a configuration's definition (metadata, default value, codec)
 * from its current runtime value, allowing for dynamic configuration management and GUI generation.
 *
 * @param <T> The data type of the configuration value (must be comparable for boundary checks).
 */
public abstract sealed class Property<T extends Comparable<T>> permits Property.Bounded, Property.Binary {
    /**
     * The simple, non-localized name of the property, used as the key in the properties file.
     */
    public final String resourceName;

    /**
     * The fully qualified translation key for the property's label in the GUI.
     */
    public final String translationKey;

    /**
     * A description of the property's function, used for tooltip/hover text in the GUI.
     */
    public final String description;

    /**
     * The parent group this property belongs to, defining its hierarchical position.
     */
    public final PropertyGroup parentGroup;

    /**
     * The default value assigned to this property. Used for initialization and the reset function.
     */
    public final T defaultValue;

    /**
     * The serialization codec for reading and writing the property's value to the configuration file.
     */
    public final Codec<T> codec;

    /**
     * The volatile current runtime value of the property.
     * Use {@code volatile} to ensure visibility across different threads (e.g., config loading vs. game logic).
     */
    private volatile T value;

    /**
     * Constructs the base configuration property.
     *
     * @param resourceName The simple name for the property (e.g., "max_level").
     * @param description The detailed description of the property.
     * @param category The parent property group.
     * @param defaultValue The initial and default value.
     * @param codec The codec used for serialization.
     */
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

    /**
     * Generates the hierarchical translation key for the property.
     *
     * @param group The parent property group.
     * @param resourceName The property's resource name.
     * @return The combined translation key (e.g., "config.modid.tabid.groupid.resourcename").
     */
    protected static String createTranslationKey(PropertyGroup group, String resourceName) {
        return group.translationKey() + "." + resourceName;
    }

    /**
     * Generates the unique, fully-qualified identifier for this config entry, used for file I/O and lookups.
     *
     * @return The unique ID string in the format: {@code tabId.groupId.resourceName}.
     */
    public String getUniqueId() {
        return this.parentGroup.parent().id() + "." + this.parentGroup.id() + "." + this.resourceName;
    }

    /**
     * Gets the current runtime value of the property.
     *
     * @return The current value.
     */
    public T get() {
        return value;
    }

    /**
     * Sets the new value, first running it through the property's validation logic.
     *
     * @param newValue The value to attempt to set.
     */
    public void set(T newValue) {
        this.value = validate(newValue);
    }

    /**
     * Sets the new value, running it through validation first.
     *
     * @param newValue The value to attempt to set.
     * @return Returns this {@code Property} instance for fluent method chaining.
     */
    public Property<T> with(T newValue) {
        this.value = validate(newValue);
        return this;
    }

    /**
     * Validates the input value against any constraints defined for this property type.
     * Subclasses must implement this to enforce domain-specific constraints (e.g., min/max bounds).
     *
     * @param value The raw value to validate.
     * @return The validated and potentially clamped value.
     */
    protected T validate(T value) {
        return value;
    }

    /**
     * A non-sealed subclass for numeric configuration values that require hard upper and lower limits.
     *
     * @param <T> The numeric type that extends {@code Number} and {@code Comparable} (e.g., {@code Integer}, {@code Float}).
     */
    public static non-sealed class Bounded<T extends Number & Comparable<T>> extends Property<T> {
        /**
         * The minimum acceptable value for this property.
         */
        public final T lowerBound;

        /**
         * The maximum acceptable value for this property.
         */
        public final T upperBound;

        /**
         * Constructs a new bounded numeric property.
         */
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

        /**
         * Validates the input value, clamping it to remain between {@code lowerBound} and {@code upperBound}.
         *
         * @param value The raw value.
         * @return The clamped value.
         */
        @Override
        protected T validate(T value) {
            if (value.compareTo(lowerBound) < 0) return lowerBound;
            if (value.compareTo(upperBound) > 0) return upperBound;
            return value;
        }
    }

    /**
     * A final subclass for simple boolean toggle configurations.
     */
    public static final class Binary extends Property<Boolean> {
        /**
         * Constructs a new boolean property.
         */
        public Binary(
                String resourceName,
                String description,
                PropertyGroup category,
                Boolean defaultValue
        ) {
            super(resourceName, description, category, defaultValue, Codec.BOOL);
        }
    }
}