package johnsmith.enchantingoverhauled.api.enchantment.value.configurable;

import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.api.config.data.ConfigMetadata;
import johnsmith.enchantingoverhauled.api.config.data.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A dynamic implementation of {@link LevelBasedValue} that delegates calculation
 * logic based on the current runtime value of a mod configuration property.
 * <p>
 * This allows enchantment behaviors (like damage scaling or protection amounts) to be
 * configurable by players via the config file/GUI, without needing to reload data packs.
 * It acts as a switch statement: "If config X is 'A', calculate like Y; otherwise, calculate like Z."
 */
public class ConfigAwareValue implements LevelBasedValue {

    /**
     * The codec responsible for serializing and deserializing this value provider from JSON.
     * <p>
     * The JSON structure requires:
     * <ul>
     * <li>{@code "config"}: Metadata identifying the target configuration property.</li>
     * <li>{@code "cases"}: A map where keys are string representations of config values (e.g., "true", "1")
     * and values are the {@link LevelBasedValue} to use in that case.</li>
     * <li>{@code "fallback"}: The default {@link LevelBasedValue} to use if the config property is missing
     * or its current value doesn't match any case.</li>
     * </ul>
     */
    public static final MapCodec<ConfigAwareValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ConfigMetadata.CODEC.fieldOf("config").forGetter(val -> val.metadata),
            Codec.unboundedMap(Codec.STRING, LevelBasedValue.CODEC).fieldOf("cases").forGetter(val -> val.cases),
            LevelBasedValue.CODEC.fieldOf("fallback").forGetter(val -> val.fallback)
    ).apply(instance, ConfigAwareValue::new));

    /**
     * Metadata used to locate the configuration property in the {@link ConfigManager}.
     */
    private final ConfigMetadata metadata;

    /**
     * A map of specific calculation logic to use for given configuration values.
     * Keys are the {@link String#valueOf(Object)} representation of the config property's value.
     */
    private final Map<String, LevelBasedValue> cases;

    /**
     * The default calculation logic to use if the config is unavailable or the value is unmapped.
     */
    private final LevelBasedValue fallback;

    /**
     * The resolved configuration property instance. Null if resolution failed.
     */
    @Nullable
    private final Property<?> targetProperty;

    /**
     * Flag indicating if the property was successfully resolved and is ready for use.
     */
    private final boolean isConfigured;

    /**
     * Constructs a new configuration-aware value provider.
     * <p>
     * During construction, it attempts to resolve the {@link Property} reference immediately using the
     * {@link ConfigManager}. If the property cannot be found (e.g., typo in JSON or config not registered),
     * an error is logged, and the class defaults to the fallback behavior.
     *
     * @param metadata Data describing which config option to track.
     * @param cases    A map of value cases.
     * @param fallback The default behavior.
     */
    public ConfigAwareValue(ConfigMetadata metadata, Map<String, LevelBasedValue> cases, LevelBasedValue fallback) {
        this.metadata = metadata;
        this.cases = cases;
        this.fallback = fallback;

        ConfigManager manager = metadata.getManager();
        Property<?> resolvedProperty = null;

        if (manager != null) {
            String configKey = metadata.getUniqueId();
            resolvedProperty = manager.getConfig(configKey);

            if (resolvedProperty == null) {
                manager.logError("ConfigAwareValue: Configuration property '{}' not found. Check your JSON spelling.", configKey);
            }
        }

        this.targetProperty = resolvedProperty;
        this.isConfigured = (this.targetProperty != null);
    }

    /**
     * Calculates the value for the given enchantment level.
     * <p>
     * This method checks the current value of the linked configuration property.
     * It converts that value to a string and looks for a matching delegate in the {@code cases} map.
     * If a match is found, that delegate calculates the result; otherwise, the {@code fallback} is used.
     *
     * @param level The level of the enchantment.
     * @return The calculated float value.
     */
    @Override
    public float calculate(int level) {
        if (this.isConfigured) {
            assert this.targetProperty != null;
            String currentKey = String.valueOf(this.targetProperty.get());

            LevelBasedValue specificLogic = this.cases.get(currentKey);
            if (specificLogic != null) {
                return specificLogic.calculate(level);
            }
        }
        return this.fallback.calculate(level);
    }

    /**
     * {@inheritDoc}
     *
     * @return The {@link MapCodec} for this class.
     */
    @Override
    public @NotNull MapCodec<? extends LevelBasedValue> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "ConfigAwareValue[metadata=" + metadata + ", cases=" + cases + ", fallback=" + fallback + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigAwareValue that = (ConfigAwareValue) o;
        return Objects.equals(metadata, that.metadata) &&
                Objects.equals(cases, that.cases) &&
                Objects.equals(fallback, that.fallback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, cases, fallback);
    }
}