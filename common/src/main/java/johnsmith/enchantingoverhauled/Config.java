package johnsmith.enchantingoverhauled;

import com.mojang.serialization.Codec;

import johnsmith.configoverhauled.api.Category;
import johnsmith.configoverhauled.api.ConfigManager;
import johnsmith.configoverhauled.api.Group;
import johnsmith.configoverhauled.api.Property;
import johnsmith.configoverhauled.api.registry.ConfigRegistry;

import johnsmith.enchantingoverhauled.api.config.type.*;
import johnsmith.enchantingoverhauled.data.DataPacks;

import johnsmith.enchantmentcore.api.config.PackInclusionType;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public final class Config {
    public static final ConfigManager MANAGER = ConfigRegistry.getOrCreateManager(Constants.MOD_ID);

    // region Categories
    private static final Category GLOBAL = MANAGER.define("global");
    private static final Category CLIENT = MANAGER.define("client");
    private static final Category ENCHANTMENT = MANAGER.define("enchantment");
    // endregion

    // region Groups
    private static final Group ENCHANTING_TABLE = GLOBAL.define("enchanting_table");
    private static final Group ENCHANT_MENU = GLOBAL.define("enchant_menu");
    private static final Group LECTERN = GLOBAL.define("lectern");
    private static final Group ANVIL = GLOBAL.define("anvil");
    private static final Group TRADE = GLOBAL.define("trade");
    private static final Group ENTITY = GLOBAL.define("entity");
    private static final Group EXPERIENCE = GLOBAL.define("experience");
    public static final Group RESOURCE = GLOBAL.define("resource");

    private static final Group ENCHANT_SCREEN = CLIENT.define("enchant_screen");
    private static final Group SCRIBE_SCREEN = CLIENT.define("scribe_screen");
    private static final Group TOOLTIP_FORMATTING = CLIENT.define("tooltip_formatting");

    private static final Group OVERRIDE = ENCHANTMENT.define("override");
    private static final Group TOOLTIP_MODIFICATION = ENCHANTMENT.define("tooltip_modification");
    // endregion

    // region Enchanting Table Properties
    public static final Property<Boolean> ENABLE_ARCANE_RETRIBUTION = ENCHANTING_TABLE.define("arcane_retribution")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether or not trying to activate the altar with a book causes a violent explosion.")
            .register();

    public static final Property<Boolean> ENABLE_ALTAR_ACTIVATION_EFFECTS = ENCHANTING_TABLE.define("activation_effects")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether activating the enchanting altar should be spectacular or mundane.")
            .register();

    public static final Property<Boolean> ENABLE_MINEABLE_ENCHANTING_TABLE = ENCHANTING_TABLE.define("mineable_enchanting_table")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether the default enchanting table can be mined or instead drops its tome.")
            .register();
    // endregion

    // region Enchant Menu Properties
    public static final Property<Integer> SCROLL_STEP_SIZE = ENCHANT_MENU.define("scroll_step_size")
            .globalSide()
            .asInteger(3, 1, Integer.MAX_VALUE)
            .withComment("How many enchantments are skipped when pressing the navigation buttons.")
            .register();

    public static final Property<UpgradeSourceType> UPGRADE_SOURCE_TYPE = ENCHANT_MENU.define("upgrade_source_type")
            .globalSide()
            .asEnum(UpgradeSourceType.EQUAL, Codec.STRING.xmap(UpgradeSourceType::valueOf, Enum::name))
            .withComment("Defines the level requirement of the source item when upgrading enchantments at the Enchanting Altar.")
            .register();

    public static final Property<UpgradePriorityType> UPGRADE_PRIORITY_TYPE = ENCHANT_MENU.define("upgrade_priority_type")
            .globalSide()
            .asEnum(UpgradePriorityType.TRANSFER, Codec.STRING.xmap(UpgradePriorityType::valueOf, Enum::name))
            .withComment("Defines the resolution behavior when a source item possesses a higher enchantment level than the target item.")
            .register();

    public static final Property<Integer> MAX_ENCHANTING_POWER = ENCHANT_MENU.define("max_enchanting_power")
            .globalSide()
            .asInteger(50, 1, Integer.MAX_VALUE)
            .withComment("The maximum enchanting power the Enchanting Altar can utilize for slot generation and XP discounts.")
            .register();

    public static final Property<EnchantPowerHalvingType> ENCHANT_POWER_HALVING_TYPE = ENCHANT_MENU.define("enchant_power_halving_type")
            .globalSide()
            .asEnum(EnchantPowerHalvingType.PER_SLOT, Codec.STRING.xmap(EnchantPowerHalvingType::valueOf, Enum::name))
            .withComment("Defines how available enchanting power is halved across multiple enchantment slots (NONE, PER_TABLE_ROLL, PER_SLOT).")
            .register();
    // endregion

    // region Scribe Menu Properties
    public static final Property<Boolean> LETHAL_SCRIBING = LECTERN.define("lethal_scribing")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether loosing a game of Scribe should be punished with a violent explosion or not.")
            .register();

    public static final Property<AbortionRefundPolicy> SCRIBE_ABORTION_REFUND_POLICY = LECTERN.define("abortion_refund_policy")
            .globalSide()
            .asEnum(AbortionRefundPolicy.UNTAGGED, Codec.STRING.xmap(AbortionRefundPolicy::valueOf, Enum::name))
            .withComment("Controls when enchantment sources are processed during the Scribe minigame.")
            .register();

    public static final Property<Boolean> SCRIBE_REFUND_ENCHANT_FUEL = LECTERN.define("refund_enchant_fuel")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether the Lapis Lazuli is refunded if the player aborts the Scribe minigame (i.e. processed at the end).")
            .register();

    public static final Property<Float> SCRIBE_FUEL_COST_SCALAR = LECTERN.define("scribe_fuel_cost_scalar")
            .globalSide()
            .asFloat(1.0F, 0.0F, 1.0F)
            .withComment("The multiplier for the lapis cost based on the number of mines. If 0, the cost is a constant 1.")
            .register();

    public static final Property<Integer> SCRIBE_XP_REWARD_SCALAR = LECTERN.define("scribe_xp_reward_scalar")
            .globalSide()
            .asInteger(1, 0, 1000)
            .withComment("The amount of XP points awarded per mine upon winning the Scribe minigame.")
            .register();

    public static final Property<Integer> SCRIBE_MINE_SCALAR = LECTERN.define("scribe_mine_scalar")
            .globalSide()
            .asInteger(3, 1, 10)
            .withComment("The multiplier for how many mines are added per enchantment level on the target.")
            .register();

    public static final Property<Integer> SCRIBE_FAILURE_DISPLAY_TIME = LECTERN.define("scribe_failure_display_time")
            .clientSide()
            .asInteger(10, 1, 60)
            .withComment("How many seconds the failed grid is displayed before resetting (if Arcane Retribution is disabled).")
            .register();

    public static final Property<Integer> SCRIBE_FAILURE_PROGRESS_COLOR = LECTERN.define("scribe_failure_progress_color")
            .clientSide()
            .asRGBColor(0xFF4444)
            .withComment("The color of the progress bar shown during a failed scribe display.")
            .register();

    public static final Property<ScribeVariant> SCRIBE_MINIGAME_VARIANT = LECTERN.define("scribe_minigame_variant")
            .globalSide()
            .asEnum(ScribeVariant.SUPERPOSITION, Codec.STRING.xmap(ScribeVariant::valueOf, Enum::name))
            .withComment("Which variant of the Scribe minigame to play.")
            .register();

    public static final Property<TomeConversionUpgradeCondition> SCRIBE_TOME_CONVERSION_UPGRADE_CONDITION = LECTERN.define("tome_conversion_upgrade_condition")
            .globalSide()
            .asEnum(TomeConversionUpgradeCondition.OVERLEVELED, Codec.STRING.xmap(TomeConversionUpgradeCondition::valueOf, Enum::name))
            .withComment("Determines at which enchantment level an Enchanted Book is converted into an Enchanted Tome when successfully upgrading an applied enchantment.")
            .register();

    public static final Property<Integer> SCRIBE_CUSTOM_TOME_CONVERSION_UPGRADE_LEVEL = LECTERN.define("custom_tome_conversion_upgrade_level")
            .globalSide()
            .asInteger(3, 2, Integer.MAX_VALUE)
            .withComment("Sets the enchantment level at which an Enchanted Book is converted into an Enchanted Tome when successfully upgrading an applied enchantment. Only active if \"tome_conversion_upgrade_condition\" is set to CUSTOM.")
            .register();

    public static final Property<TomeConversionAppendCondition> SCRIBE_TOME_CONVERSION_APPEND_CONDITION = LECTERN.define("tome_conversion_append_condition")
            .globalSide()
            .asEnum(TomeConversionAppendCondition.CAP, Codec.STRING.xmap(TomeConversionAppendCondition::valueOf, Enum::name))
            .withComment("Determines at which enchantment amount an Enchanted Book is converted into an Enchanted Tome when successfully appending an new enchantment.")
            .register();

    public static final Property<Integer> SCRIBE_CUSTOM_TOME_CONVERSION_APPEND_AMOUNT = LECTERN.define("custom_tome_conversion_append_amount")
            .globalSide()
            .asInteger(3, 2, Integer.MAX_VALUE)
            .withComment("Sets the amount of enchantments at which an Enchanted Book is converted into an Enchanted Tome when successfully appending a new enchantment. Only active if \"tome_conversion_append_condition\" is set to CUSTOM.")
            .register();
    // endregion

    // region Anvil Properties
    public static final Property<Boolean> ENABLE_ANVIL_ENCHANTING = ANVIL.define("enchanting")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether items can be enchanted using the anvil")
            .register();

    public static final Property<Boolean> ENABLE_ANVIL_XP_COST = ANVIL.define("xp_cost")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether using the anvil costs any xp.")
            .register();

    public static final Property<Integer> ANVIL_MAX_XP_COST = ANVIL.define("max_xp_cost")
            .globalSide()
            .asInteger(40, 0, Integer.MAX_VALUE)
            .withComment("The xp cost limit at which using the anvil becomes \"Too Expensive!\".")
            .register();

    public static final Property<Integer> ANVIL_MAX_ITEM_COST = ANVIL.define("max_item_repair_cost")
            .globalSide()
            .asInteger(4, 1, 64)
            .withComment("The maximum repair item cost for an item.")
            .register();

    public static final Property<Integer> ANVIL_REPAIR_BONUS = ANVIL.define("item_repair_bonus")
            .globalSide()
            .asInteger(12, 0, 100)
            .withComment("Durability bonus (as percentage) added when combining damaged items.")
            .register();

    public static final Property<Float> ANVIL_BREAK_CHANCE = ANVIL.define("anvil_break_chance")
            .globalSide()
            .asFloat(0.12F, 0.F, 1.F)
            .withComment("Chance for the anvil to take damage on use.")
            .register();
    // endregion

    // region Entity Properties
    public static final Property<Double> ENCHANTED_TOME_HP = ENTITY.define("enchanted_tome_hp")
            .globalSide()
            .asDouble(40D, 1D, Double.MAX_VALUE)
            .withComment("The HP value of Enchanted Tome Entities.")
            .register();

    public static final Property<Double> ENCHANTED_TOME_ARMOR = ENTITY.define("enchanted_tome_armor")
            .globalSide()
            .asDouble(12D, 0D, Double.MAX_VALUE)
            .withComment("The Armor value of Enchanted Tome Entities.")
            .register();

    public static final Property<Double> ENCHANTED_TOME_ARMOR_TOUGHNESS = ENTITY.define("enchanted_tome_armor_toughness")
            .globalSide()
            .asDouble(0D, 0D, Double.MAX_VALUE)
            .withComment("The Armor Toughness value of Enchanted Tome Entities.")
            .register();

    public static final Property<Double> ENCHANTED_TOME_FLYING_SPEED = ENTITY.define("enchanted_tome_flying_speed")
            .globalSide()
            .asDouble(0.5D, 0D, Double.MAX_VALUE)
            .withComment("The Flight Speed value of Enchanted Tome Entities.")
            .register();

    public static final Property<Double> ENCHANTED_TOME_MOVEMENT_SPEED = ENTITY.define("enchanted_tome_movement_speed")
            .globalSide()
            .asDouble(0.25D, 0D, Double.MAX_VALUE)
            .withComment("The Movement Speed value of Enchanted Tome Entities.")
            .register();

    public static final Property<Double> ENCHANTED_TOME_FOLLOW_RANGE = ENTITY.define("enchanted_tome_follow_range")
            .globalSide()
            .asDouble(32D, 0D, Double.MAX_VALUE)
            .withComment("The Follow Range value of Enchanted Tome Entities.")
            .register();

    public static final Property<Double> ENCHANTED_BOOK_HP = ENTITY.define("enchanted_book_hp")
            .globalSide()
            .asDouble(20D, 1D, Double.MAX_VALUE)
            .withComment("The default HP value of Enchanted Book Entities.")
            .register();

    public static final Property<Double> ENCHANTED_BOOK_ARMOR = ENTITY.define("enchanted_book_armor")
            .globalSide()
            .asDouble(4D, 0D, Double.MAX_VALUE)
            .withComment("The default Armor value of Enchanted Book Entities.")
            .register();

    public static final Property<Double> ENCHANTED_BOOK_ARMOR_TOUGHNESS = ENTITY.define("enchanted_book_armor_toughness")
            .globalSide()
            .asDouble(0D, 0D, Double.MAX_VALUE)
            .withComment("The default Armor Toughness value of Enchanted Book Entities.")
            .register();

    public static final Property<Double> ENCHANTED_BOOK_FLYING_SPEED = ENTITY.define("enchanted_book_flying_speed")
            .globalSide()
            .asDouble(0.5D, 0D, Double.MAX_VALUE)
            .withComment("The Flight Speed value of Enchanted Book Entities.")
            .register();

    public static final Property<Double> ENCHANTED_BOOK_MOVEMENT_SPEED = ENTITY.define("enchanted_book_movement_speed")
            .globalSide()
            .asDouble(0.25D, 0D, Double.MAX_VALUE)
            .withComment("The Movement Speed value of Enchanted Book Entities.")
            .register();

    public static final Property<Double> ENCHANTED_BOOK_FOLLOW_RANGE = ENTITY.define("enchanted_book_follow_range")
            .globalSide()
            .asDouble(32D, 0D, Double.MAX_VALUE)
            .withComment("The Follow Range value of Enchanted Book Entities.")
            .register();

    public static final Property<Boolean> ENABLE_SPELL_AREA_CLEARANCE_CHECKS = ENTITY.define("enable_spell_area_clearance_checks")
            .globalSide()
            .asBoolean(false)
            .withComment("Causes Enchanted Tome and Enchanted Book Entities to evaluate the area around their current target for collateral entities"
                       + "\nEnable this if your custom AI Spell data does not filter for #enchanting_overhauled:enchanted_tome_avoid_collateral.")
            .register();

    public static final Property<Integer> DEFENSIVE_ABSORPTION_AMOUNT = ENTITY.define("defensive_absorption_amount")
            .globalSide()
            .asInteger(4, 0, Integer.MAX_VALUE)
            .withComment("The amount of Absorption HP an Enchanted Tome or Enchanted Book Entity gains when casting a defensive spell.")
            .register();

    public static final Property<Integer> WEAKNESS_CAST_DURATION_TICKS = ENTITY.define("weakness_cast_duration_ticks")
            .globalSide()
            .asInteger(600, 0, Integer.MAX_VALUE)
            .withComment("The Weakness Potion Effect duration in ticks when an Enchanted Tome or Enchanted Book Entity casts a supportive spell on a hostile target.")
            .register();

    public static final Property<Integer> WEAKNESS_CAST_LEVEL = ENTITY.define("weakness_cast_level")
            .globalSide()
            .asInteger(1, 0, 255)
            .withComment("The Weakness Potion Effect level when an Enchanted Tome or Enchanted Book Entity casts a supportive spell on a hostile target.")
            .register();

    public static final Property<Integer> RESISTANCE_CAST_DURATION_TICKS = ENTITY.define("resistance_cast_duration_ticks")
            .globalSide()
            .asInteger(600, 0, Integer.MAX_VALUE)
            .withComment("The Resistance Potion Effect duration in ticks when an Enchanted Tome or Enchanted Book Entity casts a supportive spell on an ally.")
            .register();

    public static final Property<Integer> RESISTANCE_CAST_LEVEL = ENTITY.define("resistance_cast_level")
            .globalSide()
            .asInteger(0, 0, 4)
            .withComment("The Resistance Potion Effect level when an Enchanted Tome or Enchanted Book Entity casts a supportive spell on an ally.")
            .register();
    // endregion

    // region Trade Properties
    public static final Property<Boolean> ENABLE_ENCHANTED_TOME_TRADES = TRADE.define("enable_enchanted_tome_trades")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether Librarians may trade Enchanted Tomes or not.")
            .register();

    public static final Property<List<Integer>> ENCHANTED_TOME_TRADE_LEVELS = TRADE.define("enchanted_tome_trade_levels")
            .globalSide()
            .asList(List.of(5), Codec.INT)
            .withComment("The profession levels at which Librarians may trade Enchanted Tomes.")
            .register();

    public static final Property<List<Integer>> ENCHANTED_TOME_TRADE_XP = TRADE.define("enchanted_tome_trade_xp")
            .globalSide()
            .asList(List.of(0, 0, 0, 0, 0), Codec.INT)
            .withComment("The amount of xp gained per Librarian profession level at which an Enchanted Tome trade is offered.")
            .register();

    public static final Property<Integer> ENCHANTED_TOME_TRADE_MIN_COST_MODIFIER = TRADE.define("enchanted_tome_trade_min_cost_modifier")
            .globalSide()
            .asInteger(64, 1, 64)
            .withComment("The lower bound for calculating the base cost of Enchanted Tome trades.")
            .register();

    public static final Property<Integer> ENCHANTED_TOME_TRADE_MAX_COST_MODIFIER = TRADE.define("enchanted_tome_trade_max_cost_modifier")
            .globalSide()
            .asInteger(64, 1, 64)
            .withComment("The upper bound for calculating the base cost of Enchanted Tome trades.")
            .register();

    public static final Property<Float> ENCHANTED_TOME_TRADE_DEMAND_MARKUP = TRADE.define("enchanted_tome_trade_demand_markup")
            .globalSide()
            .asFloat(0.F, 0.F, 1.F)
            .withComment("The high-demand cost markup of Enchanted Tome trades.")
            .register();

    public static final Property<Boolean> ENABLE_ENCHANTED_TOME_SECONDARY_COST = TRADE.define("enable_enchanted_tome_trade_secondary_cost")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether Enchanted Tome trades should cost a secondary item.")
            .register();

    public static final Property<Item> ENCHANTED_TOME_SECONDARY_COST = TRADE.define("enchanted_tome_trade_secondary_cost")
            .globalSide()
            .asItem(Items.BOOK)
            .withComment("The secondary item required for purchasing an Enchanted Tome trade.")
            .register();

    public static final Property<Integer> ENCHANTED_TOME_TRADE_AMOUNT = TRADE.define("enchanted_tome_trade_amount")
            .globalSide()
            .asInteger(1, 1, Integer.MAX_VALUE)
            .withComment("The amount of times an enchanted tome can be purchased until the Librarian is out of stock.")
            .register();

    public static final Property<Boolean> ALLOW_ENCHANTED_TOME_RESTOCK = TRADE.define("allow_enchanted_tome_restock")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether Librarians are allowed to restock their Enchanted Tome trade.")
            .register();

    public static final Property<Boolean> ENABLE_ENCHANTED_PAGE_TRADES = TRADE.define("enable_enchanted_page_trades")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether Librarians can trade Enchanted Pages or not.")
            .register();

    public static final Property<List<Integer>> ENCHANTED_PAGE_TRADE_LEVELS = TRADE.define("enchanted_page_trade_levels")
            .globalSide()
            .asList(List.of(1, 2), Codec.INT)
            .withComment("The profession levels at which Librarians may trade Enchanted Pages.")
            .register();

    public static final Property<List<Integer>> ENCHANTED_PAGE_TRADE_XP = TRADE.define("enchanted_page_trade_xp")
            .globalSide()
            .asList(List.of(2, 2, 2, 2, 2), Codec.INT)
            .withComment("The amount of xp gained per Librarian profession level at which an Enchanted Page trade is offered.")
            .register();

    public static final Property<Integer> ENCHANTED_PAGE_TRADE_MIN_COST_MODIFIER = TRADE.define("enchanted_page_trade_min_cost_modifier")
            .globalSide()
            .asInteger(1, 1, 64)
            .withComment("The lower bound for calculating the base cost of Enchanted Page trades.")
            .register();

    public static final Property<Integer> ENCHANTED_PAGE_TRADE_MAX_COST_MODIFIER = TRADE.define("enchanted_page_trade_max_cost_modifier")
            .globalSide()
            .asInteger(12, 1, 64)
            .withComment("The upper bound for calculating the base cost of Enchanted Page trades.")
            .register();

    public static final Property<Float> ENCHANTED_PAGE_TRADE_DEMAND_MARKUP = TRADE.define("enchanted_page_trade_demand_markup")
            .globalSide()
            .asFloat(0.05F, 0.F, 1.F)
            .withComment("The high-demand cost markup of Enchanted Page trades.")
            .register();

    public static final Property<Boolean> ENABLE_ENCHANTED_PAGE_SECONDARY_COST = TRADE.define("enable_enchanted_page_trade_secondary_cost")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether Enchanted Tome trades should cost a secondary item.")
            .register();

    public static final Property<Item> ENCHANTED_PAGE_SECONDARY_COST = TRADE.define("enchanted_page_trade_secondary_cost")
            .globalSide()
            .asItem(Items.PAPER)
            .withComment("The secondary item required for purchasing an Enchanted Tome trade.")
            .register();

    public static final Property<Integer> ENCHANTED_PAGE_TRADE_AMOUNT = TRADE.define("enchanted_page_trade_amount")
            .globalSide()
            .asInteger(12, 1, Integer.MAX_VALUE)
            .withComment("The amount of times an enchanted tome can be purchased until the Librarian is out of stock.")
            .register();

    public static final Property<Boolean> ALLOW_ENCHANTED_PAGE_RESTOCK = TRADE.define("allow_enchanted_page_restock")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether Librarians are allowed to restock their Enchanted Page trades.")
            .register();

    public static final Property<Boolean> DISABLE_ENCHANTED_BOOK_TRADES = TRADE.define("disable_enchanted_book_trades")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether Enchanted Book trades should be removed at certain Librarian profession levels or not.")
            .register();

    public static final Property<List<Integer>> ENCHANTED_BOOK_TRADE_LEVELS = TRADE.define("enchanted_book_trade_disable_levels")
            .globalSide()
            .asList(List.of(1), Codec.INT)
            .withComment("The profession levels at which Librarians may not trade Enchanted Books.")
            .register();
    // endregion

    // region Data
    public static final Property<PackInclusionType> ENCHANTING_ALTAR_DATA_INCLUSION_TYPE = DataPacks.ENCHANTING_ALTAR.register();

    public static final Property<PackInclusionType> DEFAULT_ENCHANTMENT_THEMES_DATA_INCLUSION_TYPE = DataPacks.DEFAULT_ENCHANTMENT_THEMES.register();

    public static final Property<PackInclusionType> ENCHANTED_TOME_LOOT_INJECTION_DATA_INCLUSION_TYPE = DataPacks.ENCHANTED_TOME_LOOT_INJECTION.register();

    public static final Property<PackInclusionType> ENCHANTED_PAGE_LOOT_INJECTION_DATA_INCLUSION_TYPE = DataPacks.ENCHANTED_PAGE_LOOT_INJECTION.register();

    public static final Property<PackInclusionType> ENCHANTED_LIBRARY_DATA_INCLUSION_TYPE = DataPacks.ENCHANTED_LIBRARY.register();

    public static final Property<PackInclusionType> SCRIBES_RISE_DATA_INCLUSION_TYPE = DataPacks.SCRIBES_RISE.register();

    public static final Property<PackInclusionType> SCRIBE_MENU_DATA_INCLUSION_TYPE = DataPacks.SCRIBE_MENU.register();

    public static final Property<PackInclusionType> DEFAULT_ENCHANTMENT_SCHOOLS_DATA_INCLUSION_TYPE = DataPacks.DEFAULT_ENCHANTMENT_SCHOOLS.register();

    public static final Property<PackInclusionType> AI_SPELLS_DATA_INCLUSION_TYPE = DataPacks.AI_SPELLS.register();

    public static final Property<PackInclusionType> ADAPTED_DEFAULT_ENCHANTMENTS_DATA_INCLUSION_TYPE = DataPacks.ADAPTED_ENCHANTMENTS.register();

    public static final Property<PackInclusionType> CONFIGURABLE_ADAPTED_DEFAULT_ENCHANTMENTS_DATA_INCLUSION_TYPE = DataPacks.CONFIGURABLE_ADAPTED_ENCHANTMENTS.register();

    public static final Property<PackInclusionType> ENCHANTABLE_GOLDEN_APPLES_DATA_INCLUSION_TYPE = DataPacks.ENCHANTABLE_GOLDEN_APPLES.register();

    public static final Property<PackInclusionType> ENCHANTABLE_POTIONS_DATA_INCLUSION_TYPE = DataPacks.ENCHANTABLE_POTIONS.register();
    // endregion

    // region Enchant Screen Properties
    public static final Property<Boolean> USE_PLAIN_BACKGROUND = ENCHANT_SCREEN.define("use_plain_background")
            .clientSide()
            .asBoolean(false)
            .withComment("Whether the enchanting table buttons should be plain or textured.")
            .register();

    public static final Property<FormatApplicationMode> ENCHANTMENT_MENU_FORMAT_APPLICATION_MODE = ENCHANT_SCREEN.define("format_application_mode")
            .clientSide()
            .asEnum(FormatApplicationMode.ALL, Codec.STRING.xmap(FormatApplicationMode::valueOf, Enum::name))
            .withComment("Determines where custom coloring and formatting rules are applied in the Enchanting Altar.")
            .register();

    public static final Property<ColoringMode> ENCHANTMENT_MENU_THEME_COLORING = ENCHANT_SCREEN.define("theme_coloring")
            .clientSide()
            .asEnum(ColoringMode.ENCHANTMENT, Codec.STRING.xmap(ColoringMode::valueOf, Enum::name))
            .withComment("Determines the name coloring mode by theme in the Enchanting Altar.")
            .register();

    public static final Property<ColoringMode> ENCHANTMENT_MENU_LEVEL_COLORING = ENCHANT_SCREEN.define("level_coloring")
            .clientSide()
            .asEnum(ColoringMode.ENCHANTMENT, Codec.STRING.xmap(ColoringMode::valueOf, Enum::name))
            .withComment("Determines the level coloring mode by level in the Enchanting Altar.")
            .register();

    public static final Property<Boolean> ENCHANTMENT_MENU_SHOW_FIRST_LEVEL = ENCHANT_SCREEN.define("show_first_level")
            .clientSide()
            .asBoolean(false)
            .withComment("Whether scaling enchantments should show their level numeral at level 1 in the Enchanting Altar.")
            .register();

    // endregion

    // region Scribe Screen Properties
    public static final Property<Integer> UNREVEALED_GRID_COLOR = SCRIBE_SCREEN.define("unrevealed_color")
            .clientSide()
            .asARGBColor(0x203E2613)
            .withComment("The ARGB color value for the borders of unrevealed cells on the Scribe grid.")
            .register();

    public static final Property<Integer> REVEALED_GRID_COLOR = SCRIBE_SCREEN.define("revealed_color")
            .clientSide()
            .asARGBColor(0x00FFFFFF)
            .withComment("The ARGB color value for the background of revealed cells on the Scribe grid.")
            .register();

    public static final Property<FormatApplicationMode> SCRIBE_MENU_FORMAT_APPLICATION_MODE = SCRIBE_SCREEN.define("format_application_mode")
            .clientSide()
            .asEnum(FormatApplicationMode.ALL, Codec.STRING.xmap(FormatApplicationMode::valueOf, Enum::name))
            .withComment("Determines where custom coloring and formatting rules are applied in the Scribe Menu.")
            .register();

    public static final Property<ColoringMode> SCRIBE_MENU_THEME_COLORING = SCRIBE_SCREEN.define("theme_coloring")
            .clientSide()
            .asEnum(ColoringMode.ENCHANTMENT, Codec.STRING.xmap(ColoringMode::valueOf, Enum::name))
            .withComment("Determines the name coloring mode by theme in the Scribe Menu.")
            .register();

    public static final Property<ColoringMode> SCRIBE_MENU_LEVEL_COLORING = SCRIBE_SCREEN.define("level_coloring")
            .clientSide()
            .asEnum(ColoringMode.ENCHANTMENT, Codec.STRING.xmap(ColoringMode::valueOf, Enum::name))
            .withComment("Determines the level coloring mode by level in the Scribe Menu.")
            .register();

    public static final Property<Boolean> SCRIBE_MENU_SHOW_FIRST_LEVEL = SCRIBE_SCREEN.define("show_first_level")
            .clientSide()
            .asBoolean(false)
            .withComment("Whether scaling enchantments should show their level numeral at level 1 in the Scribe Menu.")
            .register();
    // endregion

    // region Tooltip Formatting Properties
    public static final Property<EnchantmentHeaderMode> ENCHANTMENT_HEADER_MODE = TOOLTIP_FORMATTING.define("enchantment_header_mode")
            .clientSide()
            .asEnum(EnchantmentHeaderMode.ALL, Codec.STRING.xmap(EnchantmentHeaderMode::valueOf, Enum::name))
            .withComment("Determines which Enchantment Component Headers to display.")
            .register();

    public static final Property<Integer> HEADER_COLOR_VALUE = TOOLTIP_FORMATTING.define("header_color")
            .clientSide()
            .asARGBColor(0xFFFFFF55)
            .withComment("The color of Enchantment Component Headers.")
            .register();

    public static final Property<ColoringMode> THEME_COLORING = TOOLTIP_FORMATTING.define("theme_coloring")
            .clientSide()
            .asEnum(ColoringMode.ENCHANTMENT, Codec.STRING.xmap(ColoringMode::valueOf, Enum::name))
            .withComment("Determines the name coloring mode by theme.")
            .register();

    public static final Property<ColoringMode> LEVEL_COLORING_LEVEL = TOOLTIP_FORMATTING.define("level_coloring")
            .clientSide()
            .asEnum(ColoringMode.ENCHANTMENT, Codec.STRING.xmap(ColoringMode::valueOf, Enum::name))
            .withComment("Determines the level coloring mode by level.")
            .register();

    public static final Property<Boolean> SHOW_FIRST_LEVEL = TOOLTIP_FORMATTING.define("show_first_level")
            .clientSide()
            .asBoolean(false)
            .withComment("Whether scaling enchantments should show their level numeral at level 1.")
            .register();

    public static final Property<Boolean> ENABLE_DESCRIPTION = TOOLTIP_FORMATTING.define("enable_description")
            .clientSide()
            .asBoolean(true)
            .withComment("Switch controlling the enchantment description.")
            .register();

    public static final Property<Integer> DESCRIPTION_COLOR = TOOLTIP_FORMATTING.define("description_color")
            .clientSide()
            .asARGBColor(0xFFAAAAAA)
            .withComment("Controls the color of the description.")
            .register();

    public static final Property<Integer> DESCRIPTION_MAX_LINE_WIDTH = TOOLTIP_FORMATTING.define("description_max_line_width")
            .clientSide()
            .asInteger(40, 10, 100)
            .withComment("The character length to wrap enchantment descriptions at in tooltips.")
            .register();

    public static final Property<Integer> DESCRIPTION_INDENTATION_DEPTH = TOOLTIP_FORMATTING.define("description_indentation_depth")
            .clientSide()
            .asInteger(2, 0, 10)
            .withComment("The indentation depth for enchantment descriptions.")
            .register();
    // endregion

    // region Enchantment Properties
    public static final Property<Boolean> NORMALIZE_ENCHANTMENT_MAX_LEVEL = OVERRIDE.define("normalize_max_level")
            .globalSide()
            .asBoolean(false)
            .withComment("Whether the max level of scalable enchantments should be overridden by the \"max_level\" value.")
            .register();

    public static final Property<Integer> ENCHANTMENT_MAX_LEVEL = OVERRIDE.define("max_level")
            .globalSide()
            .asInteger(3, 1, 3999)
            .withComment("The maximum level an enchantment can be *naturally* obtained at.")
            .register();

    public static final Property<Integer> MAX_ENCHANTMENT_AMOUNT = OVERRIDE.define("max_enchantment_amount")
            .globalSide()
            .asInteger(3, 1, Integer.MAX_VALUE)
            .withComment("The maximum amount of enchantments an Item can entertain.")
            .register();

    public static final Property<Integer> TOME_ENCHANTMENT_AMOUNT = OVERRIDE.define("tome_enchantment_amount")
            .globalSide()
            .asInteger(3, 1, Integer.MAX_VALUE)
            .withComment("The amount of enchantments randomly generated Enchanted Tomes have. Bound by max_enchantment_amount.")
            .register();

    public static final Property<Boolean> CAN_TOMES_HAVE_GREATER_ENCHANTMENTS = OVERRIDE.define("tomes_have_greater_enchantments")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether Enchanted Tomes can contain enchantments of a higher level than *naturally* possible.")
            .register();

    public static final Property<Integer> GLOBAL_ENCHANTMENT_MIN_COST = OVERRIDE.define("min_cost")
            .globalSide()
            .asInteger(0, 0, Integer.MAX_VALUE)
            .withComment("The global lower bound for the minimum cost of any enchantment.")
            .register();

    public static final Property<Integer> GLOBAL_ENCHANTMENT_MAX_COST = OVERRIDE.define("max_cost")
            .globalSide()
            .asInteger(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)
            .withComment("The global upper bound for the maximum cost of any enchantment.")
            .register();
    // endregion

    // region Tooltip Modification Properties
    public static final Property<Boolean> ENABLE_TOOLTIP_MODIFICATIONS = TOOLTIP_MODIFICATION.define("enable")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether the Tooltip Formatter of Enchanting Overhauled should be enabled.")
            .register();

    public static final Property<Integer> TOOLTIP_FORMATTER_PRIORITY = TOOLTIP_MODIFICATION.define("formatter_priority")
            .globalSide()
            .asInteger(0, Integer.MIN_VALUE, Integer.MAX_VALUE)
            .withComment("The priority level of the Tooltip Formatter. Higher is earlier.")
            .register();

    public static final Property<ObfuscationLevel> OBFUSCATION_LEVEL = TOOLTIP_MODIFICATION.define("obfuscation_level")
            .globalSide()
            .asEnum(ObfuscationLevel.UNTAGGED, Codec.STRING.xmap(ObfuscationLevel::valueOf, Enum::name))
            .withComment("Determines the level at which enchantment sources should be obfuscated.")
            .register();

    public static final Property<CurseConcealmentMode> CURSE_CONCEALMENT_MODE = TOOLTIP_MODIFICATION.define("curse_concealment_mode")
            .globalSide()
            .asEnum(CurseConcealmentMode.APPLIED, Codec.STRING.xmap(CurseConcealmentMode::valueOf, Enum::name))
            .withComment("Determines the component type to hide curses on.")
            .register();
    // endregion

    // region XP Properties
    public static final Property<Boolean> ENABLE_XP_MODIFICATIONS = EXPERIENCE.define("enable_xp_and_level_modifications")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether the XP requirement formula changes are applied or not.")
            .register();

    public static final Property<Integer> XP_GROWTH_FACTOR = EXPERIENCE.define("xp_growth_factor")
            .globalSide()
            .asInteger(1, 0, Integer.MAX_VALUE)
            .withComment("XP requirement growth factor.")
            .register();

    public static final Property<Integer> XP_LEVEL_BRACKET_SIZE = EXPERIENCE.define("xp_level_bracket_size")
            .globalSide()
            .asInteger(2, 1, Integer.MAX_VALUE)
            .withComment("Size of XP level brackets for growth calculation.")
            .register();

    public static final Property<Integer> XP_GROWTH_Y_OFFSET = EXPERIENCE.define("xp_growth_initial_value")
            .globalSide()
            .asInteger(2, 1, Integer.MAX_VALUE)
            .withComment("Initial XP requirement value (Y-offset).")
            .register();

    public static final Property<Boolean> CAP_MAX_XP_LEVEL = EXPERIENCE.define("cap_max_xp_level")
            .globalSide()
            .asBoolean(true)
            .withComment("Whether the player xp level should have a defined maximum or be unbound.")
            .register();

    public static final Property<Integer> XP_MAX_LEVEL = EXPERIENCE.define("xp_max_level")
            .globalSide()
            .asInteger(474, 0, Integer.MAX_VALUE)
            .withComment("Maximum player level.")
            .register();
    // endregion

    private Config() {}
}