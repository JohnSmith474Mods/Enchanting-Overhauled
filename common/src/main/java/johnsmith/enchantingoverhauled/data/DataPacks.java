package johnsmith.enchantingoverhauled.data;

import com.mojang.serialization.Codec;

import johnsmith.configoverhauled.api.Property;
import johnsmith.enchantmentcore.api.config.PackInclusionType;
import johnsmith.enchantingoverhauled.Config;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum DataPacks implements StringRepresentable {
    ENCHANTING_ALTAR(
            "data/enchanting_altar",
            "Enchanting Altar Data",
            () -> Config.RESOURCE.define("enchanting_altar_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Enchanting Altar pack.")
                    .register()
    ),
    DEFAULT_ENCHANTMENT_THEMES(
            "data/default_enchantment_themes",
            "Default Enchantment Themes Data",
            () -> Config.RESOURCE.define("default_enchantment_themes_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Default Enchantment Themes pack.")
                    .register()
    ),
    ENCHANTED_TOME_LOOT_INJECTION(
            "data/enchanted_tome_loot_injection",
            "Enchanted Tome Loot Injection Data",
            () -> Config.RESOURCE.define("enchanted_tome_loot_injection_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Enchanted Tome Loot Injection pack.")
                    .register()
    ),
    ENCHANTED_PAGE_LOOT_INJECTION(
            "data/enchanted_page_loot_injection",
            "Enchanted Page Loot Injection Data",
            () -> Config.RESOURCE.define("enchanted_page_loot_injection_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Enchanted Page Loot Injection pack.")
                    .register()
    ),
    ENCHANTED_LIBRARY(
            "data/enchanted_library_structure",
            "Enchanted Library Structure Data",
            () -> Config.RESOURCE.define("enchanted_library_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Enchanted Library pack.")
                    .register()
    ),
    SCRIBES_RISE(
            "data/scribes_rise_structure",
            "Scribe's Rise Structure Data",
            () -> Config.RESOURCE.define("scribes_rise_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Scribe's Rise pack.")
                    .register()
    ),
    SCRIBE_MENU(
            "data/scribe_menu",
            "Scribe Menu Data",
            () -> Config.RESOURCE.define("scribe_menu_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Scribe Menu pack.")
                    .register()
    ),
    DEFAULT_ENCHANTMENT_SCHOOLS(
            "data/default_enchantment_schools",
            "Default Enchantment Schools Data",
            () -> Config.RESOURCE.define("default_enchantment_school_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Default Enchantment Schools pack.")
                    .register()
    ),
    AI_SPELLS(
            "data/ai_spells",
            "AI Spell Data",
            () -> Config.RESOURCE.define("ai_spells_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in AI Spells pack.")
                    .register()
    ),
    ADAPTED_ENCHANTMENTS(
            "data/adapted_enchantments",
            "Adapted Default Enchantments Data",
            () -> Config.RESOURCE.define("adapted_default_enchantments_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.ACTIVE, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Adapted Default Enchantments pack.")
                    .register()
    ),
    CONFIGURABLE_ADAPTED_ENCHANTMENTS(
            "data/configurable_adapted_enchantments",
            "Configurable Adapted Default Enchantments Data",
            () -> Config.RESOURCE.define("configurable_adapted_default_enchantments_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.OPTIONAL, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Configurable Adapted Default Enchantments pack.")
                    .register()
    ),
    ENCHANTABLE_GOLDEN_APPLES(
            "data/enchantable_golden_apples",
            "Enchantable Golden Apples Data",
            () -> Config.RESOURCE.define("enchantable_golden_apples_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.OPTIONAL, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Enchantable Golden Apples pack.")
                    .register()
    ),
    ENCHANTABLE_POTIONS(
            "data/enchantable_potions",
            "Enchantable Potions Data",
            () -> Config.RESOURCE.define("enchantable_potions_pack_inclusion_type")
                    .globalSide()
                    .asEnum(PackInclusionType.OPTIONAL, Codec.STRING.xmap(PackInclusionType::valueOf, Enum::name))
                    .withComment("Defines the activation behavior of the built-in Enchantable Potions pack.")
                    .register()
    );

    public final String path;
    public final String title;
    private final Supplier<Property<PackInclusionType>> factory;
    private Property<PackInclusionType> packInclusionType;

    DataPacks(String path, String title, Supplier<Property<PackInclusionType>> factory) {
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
