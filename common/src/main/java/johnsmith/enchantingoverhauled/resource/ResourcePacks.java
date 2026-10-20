package johnsmith.enchantingoverhauled.resource;

import johnsmith.configoverhauled.api.Property;
import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantmentcore.api.config.PackInclusionType;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum ResourcePacks implements StringRepresentable {
    ENCHANTING_ALTAR(
            "assets/enchanting_altar",
            "Enchanting Altar Assets",
            () -> Config.ENCHANTING_ALTAR_DATA_INCLUSION_TYPE
    );

    public final String path;
    public final String title;
    private final Supplier<Property<PackInclusionType>> factory;
    private Property<PackInclusionType> packInclusionType;


    ResourcePacks(String path, String title, Supplier<Property<PackInclusionType>> factory) {
        this.path = path;
        this.title = title;
        this.factory = factory;
    }

    public Property<PackInclusionType> register() {
        if (this.packInclusionType == null) this.packInclusionType = factory.get();
        return this.packInclusionType;
    }

    public Property<PackInclusionType> packInclusionType() {
        return this.register();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.path;
    }
}
