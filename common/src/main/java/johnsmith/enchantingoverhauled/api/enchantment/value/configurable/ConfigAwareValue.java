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

public class ConfigAwareValue implements LevelBasedValue {

    public static final MapCodec<ConfigAwareValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ConfigMetadata.CODEC.fieldOf("config").forGetter(val -> val.metadata),
            Codec.unboundedMap(Codec.STRING, LevelBasedValue.CODEC).fieldOf("cases").forGetter(val -> val.cases),
            LevelBasedValue.CODEC.fieldOf("fallback").forGetter(val -> val.fallback)
    ).apply(instance, ConfigAwareValue::new));

    private final ConfigMetadata metadata;
    private final Map<String, LevelBasedValue> cases;
    private final LevelBasedValue fallback;

    // Resolved at construction time
    @Nullable
    private final Property<?> targetProperty;
    private final boolean isConfigured;

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