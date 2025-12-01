package johnsmith.enchantingoverhauled.config;

import johnsmith.enchantingoverhauled.Constants;

import com.mojang.serialization.Codec;
import johnsmith.enchantingoverhauled.api.config.data.PropertyGroup;
import johnsmith.enchantingoverhauled.api.config.data.PropertyTab;
import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.ConfigManager;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentMenu;

public class Config {

    // The central manager instance for this mod
    public static final ConfigManager MANAGER = new ConfigManager(Constants.MOD_ID, Constants.MOD_NAME, Constants.LOG);

    // region Tabs
    public static final PropertyTab TAB_GENERAL = MANAGER.registerTab("general", "config." + Constants.MOD_ID);
    public static final PropertyTab TAB_ENCHANTMENTS = MANAGER.registerTab("enchantment", "config." + Constants.MOD_ID);
    public static final PropertyTab TAB_XP = MANAGER.registerTab("xp", "config." + Constants.MOD_ID);
    public static final PropertyTab TAB_LOOT = MANAGER.registerTab("loot", "config." + Constants.MOD_ID);
    public static final PropertyTab TAB_ACCESSIBILITY = MANAGER.registerTab("accessibility", "config." + Constants.MOD_ID);
    // endregion

    // region Groups
    public static final PropertyGroup GROUP_ENCHANTING_TABLE = TAB_GENERAL.registerGroup("enchanting_table");
    public static final PropertyGroup GROUP_ANVIL = TAB_GENERAL.registerGroup("anvil");
    public static final PropertyGroup GROUP_ENCHANTMENT_GENERAL = TAB_ENCHANTMENTS.registerGroup("general");
    public static final PropertyGroup GROUP_ENCHANTMENT_DAMAGE_CALCULATION = TAB_ENCHANTMENTS.registerGroup("damage_calculation");
    public static final PropertyGroup GROUP_ENCHANTMENT_TOOLTIP = TAB_ENCHANTMENTS.registerGroup("enchantment_tooltip");
    public static final PropertyGroup GROUP_XP_GENERAL = TAB_XP.registerGroup("general");
    public static final PropertyGroup GROUP_LOOT_GENERAL = TAB_LOOT.registerGroup("general");
    public static final PropertyGroup GROUP_ACCESSIBILITY_ENCHANTING_TABLE = TAB_ACCESSIBILITY.registerGroup("enchanting_table");
    public static final PropertyGroup GROUP_ACCESSIBILITY_ENCHANTMENT_NAME = TAB_ACCESSIBILITY.registerGroup("enchantment_name");
    public static final PropertyGroup GROUP_ACCESSIBILITY_ENCHANTMENT_LEVEL = TAB_ACCESSIBILITY.registerGroup("enchantment_level");
    public static final PropertyGroup GROUP_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION = TAB_ACCESSIBILITY.registerGroup("enchantment_description");
    public static final PropertyGroup GROUP_ACCESSIBILITY_ENCHANTMENT_TOOLTIP = TAB_ACCESSIBILITY.registerGroup("enchantment_tooltip");
    // endregion

    // region Enchanting Table Values
    public static final Property.Binary BINARY_ARCANE_RETRIBUTION = GROUP_ENCHANTING_TABLE.register(
            new Property.Binary("arcane_retribution", "Whether or not trying to activate the altar with a book causes a violent explosion.", GROUP_ENCHANTING_TABLE, true)
    );
    public static final Property.Binary BINARY_ACTIVATION_EFFECTS = GROUP_ENCHANTING_TABLE.register(
            new Property.Binary("activation_effects", "Whether activating the enchanting altar should be spectacular or mundane.", GROUP_ENCHANTING_TABLE, true)
    );
    public static final Property.Binary BINARY_MINEABLE_ENCHANTING_TABLE = GROUP_ENCHANTING_TABLE.register(
            new Property.Binary("mineable_enchanting_table", "Whether the default enchanting table can be mined or instead drops its tome.", GROUP_ENCHANTING_TABLE, false)
    );
    public static final Property.Bounded<Integer> BOUNDED_MAX_ENCHANTMENTS = GROUP_ENCHANTING_TABLE.register(
            new Property.Bounded<>("max_enchantments", "The max. amount of enchantments an item can have.", GROUP_ENCHANTING_TABLE, 3, 1, OverhauledEnchantmentMenu.AVAILABLE_SLOTS, Codec.INT)
    );
    // endregion

    // region Anvil Values
    public static final Property.Bounded<Integer> BOUNDED_ANVIL_MAX_ITEM_COST = GROUP_ANVIL.register(
            new Property.Bounded<>("max_item_repair_cost", "The maximum repair item cost for an item.", GROUP_ANVIL, 4, 1, 25, Codec.INT)
    );
    public static final Property.Bounded<Integer> BOUNDED_ANVIL_REPAIR_BONUS = GROUP_ANVIL.register(
            new Property.Bounded<>("item_repair_bonus", "Durability bonus (as percentage) added when combining damaged items.", GROUP_ANVIL, 12, 0, 100, Codec.INT)
    );
    public static final Property.Bounded<Float> BOUNDED_ANVIL_BREAK_CHANCE = GROUP_ANVIL.register(
            new Property.Bounded<>("anvil_break_chance", "Chance for the anvil to take damage on use.", GROUP_ANVIL, .12F, 0.F, 1.F, Codec.FLOAT)
    );
    // endregion

    // region Enchantment Values
    public static final Property.Bounded<Integer> BOUNDED_ENCHANTMENT_MAX_LEVEL = GROUP_ENCHANTMENT_GENERAL.register(
            new Property.Bounded<>("enchantment_max_level", "The maximum level an enchantment can be *naturally* obtained at.", GROUP_ENCHANTMENT_GENERAL, 3, 1, 254, Codec.INT)
    );
    public static final Property<Boolean> BINARY_TOMES_HAVE_GREATER_ENCHANTMENTS = GROUP_ENCHANTMENT_GENERAL.register(
            new Property.Binary("tomes_have_greater_enchantments", "Whether enchanted tomes can contain enchantments of a higher level than naturally possible.", GROUP_ENCHANTMENT_GENERAL, true)
    );
    public static final Property.Bounded<Float> BOUNDED_PROTECTION_NUMERATOR = GROUP_ENCHANTMENT_DAMAGE_CALCULATION.register(
            new Property.Bounded<>("protection_numerator", "The maximum effective points you can get from enchantments.", GROUP_ENCHANTMENT_DAMAGE_CALCULATION, 30.F, 1.F, 32_768.F, Codec.FLOAT)
    );
    public static final Property.Bounded<Float> BOUNDED_PROTECTION_DENOMINATOR = GROUP_ENCHANTMENT_DAMAGE_CALCULATION.register(
            new Property.Bounded<>("protection_denominator", "The value used to calculate damage reduction.", GROUP_ENCHANTMENT_DAMAGE_CALCULATION,
                    BOUNDED_PROTECTION_NUMERATOR.defaultValue + 10.F, // Default
                    BOUNDED_PROTECTION_NUMERATOR.lowerBound + .5F,    // Min
                    BOUNDED_PROTECTION_NUMERATOR.upperBound / .75F,   // Max
                    Codec.FLOAT
            ) {
                @Override
                public Float get() {
                    float configuredVal = super.get();
                    float cap = BOUNDED_PROTECTION_NUMERATOR.get();
                    float minSafeDivisor = cap / .75F;
                    return Math.max(configuredVal, minSafeDivisor);
                }
            }
    );
    public static final Property.Binary BINARY_SHOW_ENCHANTMENT_TOOLTIP_HEADER = GROUP_ENCHANTMENT_TOOLTIP.register(
            new Property.Binary("show_enchantment_tooltip_header", "Whether the enchantment tooltip block should have a header spelling either \"Applied Enchantments\" or \"Stored Enchantments\".", GROUP_ENCHANTMENT_TOOLTIP, true)
    );
    // endregion

    // region XP Values
    public static final Property.Bounded<Integer> BOUNDED_XP_GROWTH_FACTOR = GROUP_XP_GENERAL.register(
            new Property.Bounded<>("xp_growth_factor", "XP requirement growth factor.", GROUP_XP_GENERAL, 1, 0, Integer.MAX_VALUE, Codec.INT)
    );

    public static final Property.Bounded<Integer> BOUNDED_XP_LEVEL_BRACKET_SIZE = GROUP_XP_GENERAL.register(
            new Property.Bounded<>("xp_level_bracket_size", "Size of XP level brackets for growth calculation.", GROUP_XP_GENERAL, 2, 1, Integer.MAX_VALUE, Codec.INT)
    );

    public static final Property.Bounded<Integer> BOUNDED_XP_GROWTH_Y_OFFSET = GROUP_XP_GENERAL.register(
            new Property.Bounded<>("xp_growth_initial_value", "Initial XP requirement value (Y-offset).", GROUP_XP_GENERAL, 2, 1, Integer.MAX_VALUE, Codec.INT)
    );

    public static final Property.Bounded<Integer> BOUNDED_XP_MAX_LEVEL = GROUP_XP_GENERAL.register(
            new Property.Bounded<>("xp_max_level", "Maximum player level.", GROUP_XP_GENERAL, 100, 3, Integer.MAX_VALUE, Codec.INT)
    );
    // endregion

    // region Loot Injection Values
    public static final Property.Bounded<Float> BOUNDED_LOOT_CHANCE_EPIC = GROUP_LOOT_GENERAL.register(
            new Property.Bounded<>("epic_loot_chance", "Chance for an Enchanted Tome to appear in epic loot chests.", GROUP_LOOT_GENERAL, .8F, 0.F, 1.F, Codec.FLOAT)
    );

    public static final Property.Bounded<Float> BOUNDED_LOOT_CHANCE_RARE = GROUP_LOOT_GENERAL.register(
            new Property.Bounded<>("rare_loot_chance", "Chance for an Enchanted Tome to appear in rare loot chests.", GROUP_LOOT_GENERAL, .45F, 0.F, 1.F, Codec.FLOAT)
    );

    public static final Property.Bounded<Float> BOUNDED_LOOT_CHANCE_UNCOMMON = GROUP_LOOT_GENERAL.register(
            new Property.Bounded<>("uncommon_loot_chance", "Chance for an Enchanted Tome to appear in uncommon loot chests.", GROUP_LOOT_GENERAL, .2F, 0.F, 1.F, Codec.FLOAT)
    );

    public static final Property.Bounded<Float> BOUNDED_LOOT_CHANCE_COMMON = GROUP_LOOT_GENERAL.register(
            new Property.Bounded<>("common_loot_chance", "Chance for an Enchanted Tome to appear in common loot chests.", GROUP_LOOT_GENERAL, .05F, 0.F, 1.F, Codec.FLOAT)
    );
    // endregion

    // region Accessibility Values
    public static final Property<Boolean> BINARY_ACCESSIBILITY_USE_PLAIN_BACKGROUND = GROUP_ACCESSIBILITY_ENCHANTING_TABLE.register(
            new Property.Binary("use_plain_background", "Whether the enchanting table buttons should be plain or textured.", GROUP_ACCESSIBILITY_ENCHANTING_TABLE, false)
    );
    public static final Property<Boolean> BINARY_ACCESSIBILITY_OBFUSCATE_NEW_ENCHANTMENTS = GROUP_ACCESSIBILITY_ENCHANTING_TABLE.register(
            new Property.Binary("obfuscate_new_enchantments", "Whether the enchanting table should show new enchantments in the standard galactic alphabet.", GROUP_ACCESSIBILITY_ENCHANTING_TABLE, true)
    );

    public static final Property<Boolean> BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_NAME_COLOR = GROUP_ACCESSIBILITY_ENCHANTMENT_NAME.register(
            new Property.Binary("override_enchantment_name_color", "Whether enchantment names should not be colored according to their assigned theme.", GROUP_ACCESSIBILITY_ENCHANTMENT_NAME, false)
    );
    public static final Property.Bounded<Integer> BOUNDED_ACCESSIBILITY_ENCHANTMENT_NAME_COLOR_VALUE = GROUP_ACCESSIBILITY_ENCHANTMENT_NAME.register(
            new Property.Bounded<>("enchantment_name_color_override_value", "The color value enchantment names should be override with.", GROUP_ACCESSIBILITY_ENCHANTMENT_NAME, 0xFFFFFF, 0x000000, 0xFFFFFF, Codec.INT)
    );

    public static final Property<Boolean> BINARY_ACCESSIBILITY_OVERRIDE_ENCHANTMENT_LEVEL_COLOR = GROUP_ACCESSIBILITY_ENCHANTMENT_LEVEL.register(
            new Property.Binary("override_enchantment_level_color", "Whether enchantment levels should not be colored depending on their value.", GROUP_ACCESSIBILITY_ENCHANTMENT_LEVEL, false)
    );
    public static final Property.Bounded<Integer> BOUNDED_ACCESSIBILITY_ENCHANTMENT_LEVEL_COLOR_VALUE = GROUP_ACCESSIBILITY_ENCHANTMENT_LEVEL.register(
            new Property.Bounded<>("enchantment_level_color_override_value", "The color value enchantment levels should be override with.", GROUP_ACCESSIBILITY_ENCHANTMENT_LEVEL, 0xFFFFFF, 0x000000, 0xFFFFFF, Codec.INT)
    );

    public static final Property<Boolean> BINARY_ACCESSIBILITY_SHOW_ENCHANTMENT_DESCRIPTIONS = GROUP_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION.register(
            new Property.Binary("show_enchantment_descriptions", "Whether enchantments descriptions should be removed or displayed.", GROUP_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION, true)
    );
    public static final Property.Bounded<Integer> BOUNDED_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION_COLOR = GROUP_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION.register(
            new Property.Bounded<>("enchantment_description_color_value", "The color value enchantment descriptions should be override with.", GROUP_ACCESSIBILITY_ENCHANTMENT_DESCRIPTION, 0xA7A7A7, 0x000000, 0xFFFFFF, Codec.INT)
    );

    public static final Property.Binary BINARY_ENCHANTMENT_TOOLTIP_HEADER_COLOR = GROUP_ACCESSIBILITY_ENCHANTMENT_TOOLTIP.register(
            new Property.Binary("override_enchantment_header_color", "Whether the enchantment tooltip block header should be colored uniformly.", GROUP_ACCESSIBILITY_ENCHANTMENT_TOOLTIP, false)
    );
    public static final Property.Bounded<Integer> BINARY_ENCHANTMENT_TOOLTIP_HEADER_COLOR_VALUE = GROUP_ACCESSIBILITY_ENCHANTMENT_TOOLTIP.register(
            new Property.Bounded<>("enchantment_level_color_override_value", "The color value enchantment tooltips headers should be overridden with.", GROUP_ACCESSIBILITY_ENCHANTMENT_TOOLTIP, 0xFFFFFF, 0x000000, 0xFFFFFF, Codec.INT)
    );
    // endregion

    public static void initialize() {

    }
}