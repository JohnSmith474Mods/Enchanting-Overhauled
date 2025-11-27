package johnsmith.enchantingoverhauled.config;

/**
 * The central holder for all configuration values in the mod.
 * <p>
 * This class contains static fields that represent the current state of the game's configuration.
 * These values are loaded from the platform-specific configuration implementations (Fabric/NeoForge)
 * at startup and synchronized to clients when joining a server.
 */
public class Config {

    // region Enchanting Table Settings
    /**
     * Whether the enchanting table should violently reject books.
     */
    public static Boolean ARCANE_RETRIBUTION;
    /**
     * Whether the enchanting table should have a spectacular activation.
     */
    public static Boolean ACTIVATION_EFFECTS;

    /** Default value for {@link #ARCANE_RETRIBUTION}. */
    public static final boolean ARCANE_RETRIBUTION_DEFAULT = true;

    /** Default value for {@link #ACTIVATION_EFFECTS}. */
    public static final boolean ACTIVATION_EFFECTS_DEFAULT = true;

    /** Translation key value for {@link #ARCANE_RETRIBUTION}. */
    public static final String ARCANE_RETRIBUTION_KEY = "enchanting_table.arcane_retribution";

    /** Translation key value for {@link #ACTIVATION_EFFECTS}. */
    public static final String ACTIVATION_EFFECTS_KEY = "enchanting_table.activation_effects";
    // endregion

    // region Anvil Settings
    /**
     * The maximum number of items (e.g., diamonds, ingots) required to fully repair an item in an anvil.
     */
    public static Integer ANVIL_MAX_ITEM_COST;
    /**
     * The percentage of extra durability restored when combining two damaged items in an anvil.
     */
    public static Integer ANVIL_REPAIR_BONUS;
    /**
     * The chance (0.0 - 1.0) that an anvil will take damage after being used.
     */
    public static Double ANVIL_BREAK_CHANCE;

    /** Default value for {@link #ANVIL_MAX_ITEM_COST}. */
    public static final int ANVIL_MAX_ITEM_COST_DEFAULT = 4;
    /** Default value for {@link #ANVIL_REPAIR_BONUS}. */
    public static final int ANVIL_REPAIR_BONUS_DEFAULT = 12;
    /** Default value for {@link #ANVIL_BREAK_CHANCE}. */
    public static final double ANVIL_BREAK_CHANCE_DEFAULT = 0.12;

    /** Minimum value for {@link #ANVIL_MAX_ITEM_COST}. */
    public static final int ANVIL_MAX_ITEM_COST_FLOOR = 1;
    /** Minimum value for {@link #ANVIL_REPAIR_BONUS}. */
    public static final int ANVIL_REPAIR_BONUS_FLOOR = 0;
    /** Minimum value for {@link #ANVIL_BREAK_CHANCE}. */
    public static final double ANVIL_BREAK_CHANCE_FLOOR = 0.0;

    /** Maximum value for {@link #ANVIL_MAX_ITEM_COST}. */
    public static final int ANVIL_MAX_ITEM_COST_CEILING = 25;
    /** Maximum value for {@link #ANVIL_REPAIR_BONUS}. */
    public static final int ANVIL_REPAIR_BONUS_CEILING = 100;
    /** Maximum value for {@link #ANVIL_BREAK_CHANCE}. */
    public static final double ANVIL_BREAK_CHANCE_CEILING = 1.0;

    /** Translation key value for {@link #ANVIL_MAX_ITEM_COST}. */
    public static final String ANVIL_MAX_ITEM_COST_KEY = "anvil.max.item.repair.cost";
    /** Translation key value for {@link #ANVIL_REPAIR_BONUS}. */
    public static final String ANVIL_REPAIR_BONUS_KEY = "anvil.item.repair.bonus";
    /** Translation key value for {@link #ANVIL_BREAK_CHANCE}. */
    public static final String ANVIL_BREAK_CHANCE_KEY = "anvil.break.chance";
    // endregion

    // region Enchantment Level Settings
    /**
     * The maximum level at which an enchantment can be obtained via natural means (Enchanting Table).
     */
    public static Integer ENCHANTMENT_MAX_LEVEL;
    /**
     * Whether Enchanted Tomes found in chests or created via the Altar can contain enchantments
     * one level higher than the natural maximum (e.g., Sharpness VI if max is V).
     */
    public static Boolean TOMES_HAVE_GREATER_ENCHANTMENTS;

    /** Default value for {@link #ENCHANTMENT_MAX_LEVEL}. */
    public static final int ENCHANTMENT_MAX_LEVEL_DEFAULT = 3;
    /** Default value for {@link #TOMES_HAVE_GREATER_ENCHANTMENTS}. */
    public static final boolean TOMES_HAVE_GREATER_ENCHANTMENTS_DEFAULT = true;

    /** Minimum value for {@link #ENCHANTMENT_MAX_LEVEL}. */
    public static final int ENCHANTMENT_MAX_LEVEL_FLOOR = 1;
    /** Maximum value for {@link #ENCHANTMENT_MAX_LEVEL}. */
    public static final int ENCHANTMENT_MAX_LEVEL_CEILING = 254;

    /** Translation key value for {@link #ENCHANTMENT_MAX_LEVEL}. */
    public static final String ENCHANTMENT_MAX_LEVEL_KEY = "enchantments.max.level";
    /** Translation key value for {@link #TOMES_HAVE_GREATER_ENCHANTMENTS}. */
    public static final String TOMES_HAVE_GREATER_ENCHANTMENTS_KEY = "enchantments.tomes.greater_enchantments";
    // endregion

    // region Protection Enchantment Settings
    /**
     * The maximum total protection points a player can accumulate from armor enchantments.
     */
    public static Double PROTECTION_CAP;
    /**
     * The divisor used in the damage reduction formula.
     * <p>
     * Formula: {@code Damage * (1.0 - (TotalProtectionPoints / PROTECTION_DIVISOR))}
     */
    public static Double PROTECTION_DIVISOR;

    /** Protection point multiplier for the generic Protection enchantment (now Physical). */
    public static Integer PHYSICAL_PROTECTION_STRENGTH;
    /** Protection point multiplier for Fire Protection. */
    public static Integer FIRE_PROTECTION_STRENGTH;
    /** Protection point multiplier for Blast Protection. */
    public static Integer BLAST_PROTECTION_STRENGTH;
    /** Protection point multiplier for Projectile Protection. */
    public static Integer PROJECTILE_PROTECTION_STRENGTH;
    /** Protection point multiplier for Magic Protection. */
    public static Integer MAGIC_PROTECTION_STRENGTH;
    /** Protection point multiplier for Feather Falling. */
    public static Integer FEATHER_FALLING_STRENGTH;

    /** Default value for {@link #PROTECTION_CAP}. */
    public static final double PROTECTION_CAP_DEFAULT = 8.0;
    /** Default value for {@link #PROTECTION_DIVISOR}. */
    public static final double PROTECTION_DIVISOR_DEFAULT = 16.0;

    /** Default value for {@link #PHYSICAL_PROTECTION_STRENGTH}. */
    public static final int PHYSICAL_PROTECTION_STRENGTH_DEFAULT = 2;
    /** Default value for {@link #FIRE_PROTECTION_STRENGTH}. */
    public static final int FIRE_PROTECTION_STRENGTH_DEFAULT = 2;
    /** Default value for {@link #BLAST_PROTECTION_STRENGTH}. */
    public static final int BLAST_PROTECTION_STRENGTH_DEFAULT = 2;
    /** Default value for {@link #PROJECTILE_PROTECTION_STRENGTH}. */
    public static final int PROJECTILE_PROTECTION_STRENGTH_DEFAULT = 2;
    /** Default value for {@link #MAGIC_PROTECTION_STRENGTH}. */
    public static final int MAGIC_PROTECTION_STRENGTH_DEFAULT = 2;
    /** Default value for {@link #FEATHER_FALLING_STRENGTH}. */
    public static final int FEATHER_FALLING_STRENGTH_DEFAULT = 3;

    /** Minimum value for {@link #PROTECTION_CAP}. */
    public static final double PROTECTION_CAP_FLOOR = 1.0D;
    /** Minimum value for {@link #PROTECTION_DIVISOR}. */
    public static final double PROTECTION_DIVISOR_FLOOR = PROTECTION_CAP_FLOOR + 0.5D;

    /** Minimum value for
     * {@link #PHYSICAL_PROTECTION_STRENGTH},
     * {@link #FIRE_PROTECTION_STRENGTH},
     * {@link #BLAST_PROTECTION_STRENGTH},
     * {@link #PROJECTILE_PROTECTION_STRENGTH},
     * {@link #MAGIC_PROTECTION_STRENGTH} and
     * {@link #FEATHER_FALLING_STRENGTH}. */
    public static final int PROTECTION_STRENGTH_FLOOR = 1;

    /** Maximum value for {@link #PROTECTION_CAP}. */
    public static final double PROTECTION_CAP_CEILING = 32768.0D;
    /** Maximum value for {@link #PROTECTION_DIVISOR}. */
    public static final double PROTECTION_DIVISOR_CEILING = PROTECTION_CAP_CEILING + 1.0D;

    /** Maximum value for
     * {@link #PHYSICAL_PROTECTION_STRENGTH},
     * {@link #FIRE_PROTECTION_STRENGTH},
     * {@link #BLAST_PROTECTION_STRENGTH},
     * {@link #PROJECTILE_PROTECTION_STRENGTH},
     * {@link #MAGIC_PROTECTION_STRENGTH} and
     * {@link #FEATHER_FALLING_STRENGTH}. */
    public static final int PROTECTION_STRENGTH_CEILING = 32768;

    /** Translation key value for {@link #PROTECTION_CAP}. */
    public static final String PROTECTION_CAP_KEY = "protection.cap";
    /** Translation key value for {@link #PROTECTION_DIVISOR}. */
    public static final String PROTECTION_DIVISOR_KEY = "protection.divisor";
    /** Translation key value for {@link #PHYSICAL_PROTECTION_STRENGTH}. */
    public static final String PHYSICAL_PROTECTION_STRENGTH_KEY = "physical.protection.strength";
    /** Translation key value for {@link #FIRE_PROTECTION_STRENGTH}. */
    public static final String FIRE_PROTECTION_STRENGTH_KEY = "fire.protection.strength";
    /** Translation key value for {@link #BLAST_PROTECTION_STRENGTH}. */
    public static final String BLAST_PROTECTION_STRENGTH_KEY = "blast.protection.strength";
    /** Translation key value for {@link #PROJECTILE_PROTECTION_STRENGTH}. */
    public static final String PROJECTILE_PROTECTION_STRENGTH_KEY = "projectile.protection.strength";
    /** Translation key value for {@link #MAGIC_PROTECTION_STRENGTH}. */
    public static final String MAGIC_PROTECTION_STRENGTH_KEY = "magic.protection.strength";
    /** Translation key value for {@link #FEATHER_FALLING_STRENGTH}. */
    public static final String FEATHER_FALLING_STRENGTH_KEY = "feather.falling.strength";
    // endregion

    // region Damage Enchantment Settings
    /** The base damage added by Sharpness at Level 1. */
    public static Float SHARPNESS_INITIAL_DAMAGE;
    /** The amount the damage bonus decreases per subsequent level of Sharpness. */
    public static Float SHARPNESS_DIMINISHING_RETURNS;
    /** The minimum amount of damage added per level of Sharpness (prevents 0 or negative gain). */
    public static Float SHARPNESS_MINIMUM_DAMAGE_INCREMENT;
    /** The damage multiplier applied by Smite against undead targets. */
    public static Float SMITE_MULTIPLIER;
    /** The damage multiplier applied by Bane of Arthropods against arthropod targets. */
    public static Float EXTERMINATION_MULTIPLIER;

    /** Default value for {@link #SHARPNESS_INITIAL_DAMAGE}. */
    public static final double SHARPNESS_INITIAL_DAMAGE_DEFAULT = 1.5D;
    /** Default value for {@link #SHARPNESS_DIMINISHING_RETURNS}. */
    public static final double SHARPNESS_DIMINISHING_RETURNS_DEFAULT = 0.5D;
    /** Default value for {@link #SHARPNESS_MINIMUM_DAMAGE_INCREMENT}. */
    public static final double SHARPNESS_MINIMUM_DAMAGE_INCREMENT_DEFAULT = 0.5D;
    /** Default value for {@link #SMITE_MULTIPLIER} and {@link #EXTERMINATION_MULTIPLIER}. */
    public static final double DAMAGE_MULTIPLIER_DEFAULT = 4.0D;

    /** Minimum value for {@link #SHARPNESS_INITIAL_DAMAGE}. */
    public static final double SHARPNESS_INITIAL_DAMAGE_FLOOR = 0.5D;
    /** Minimum value for {@link #SHARPNESS_DIMINISHING_RETURNS}. */
    public static final double SHARPNESS_DIMINISHING_RETURNS_FLOOR = 0.5D;
    /** Minimum value for {@link #SHARPNESS_MINIMUM_DAMAGE_INCREMENT}. */
    public static final double SHARPNESS_MINIMUM_DAMAGE_INCREMENT_FLOOR = 0.5D;
    /** Minimum value for {@link #SMITE_MULTIPLIER} and {@link #EXTERMINATION_MULTIPLIER}. */
    public static final double DAMAGE_MULTIPLIER_FLOOR = 1.0D;

    /** Maximum value for {@link #SHARPNESS_INITIAL_DAMAGE}. */
    public static final double SHARPNESS_INITIAL_DAMAGE_CEILING = 32768.0D;
    /** Maximum value for {@link #SHARPNESS_DIMINISHING_RETURNS}. */
    public static final double SHARPNESS_DIMINISHING_RETURNS_CEILING = 32768.0D;
    /** Maximum value for {@link #SHARPNESS_MINIMUM_DAMAGE_INCREMENT}. */
    public static final double SHARPNESS_MINIMUM_DAMAGE_INCREMENT_CEILING = 32768.0D;
    /** Maximum value for {@link #SMITE_MULTIPLIER} and {@link #EXTERMINATION_MULTIPLIER}. */
    public static final double DAMAGE_MULTIPLIER_CEILING = 32768.0D;

    /** Translation key value for {@link #SHARPNESS_INITIAL_DAMAGE}. */
    public static final String SHARPNESS_INITIAL_DAMAGE_KEY = "damage.sharpness.initial";
    /** Translation key value for {@link #SHARPNESS_DIMINISHING_RETURNS}. */
    public static final String SHARPNESS_DIMINISHING_RETURNS_KEY = "damage.sharpness.diminishing";
    /** Translation key value for {@link #SHARPNESS_MINIMUM_DAMAGE_INCREMENT}. */
    public static final String SHARPNESS_MINIMUM_DAMAGE_INCREMENT_KEY = "damage.sharpness.minimum";
    /** Translation key value for {@link #SMITE_MULTIPLIER}. */
    public static final String SMITE_MULTIPLIER_KEY = "damage.smite.multiplier";
    /** Translation key value for {@link #EXTERMINATION_MULTIPLIER}. */
    public static final String EXTERMINATION_MULTIPLIER_KEY = "damage.extermination.multiplier";
    // endregion

    // region Loot Enchantment Settings
    /** The initial drop bonus/chance cap provided by Fortune at Level 1. */
    public static Float FORTUNE_INITIAL_LIMIT;
    /** The amount the drop limit decreases per subsequent level of Fortune. */
    public static Float FORTUNE_DIMINISHING_RETURNS;
    /** The minimum increase to the drop limit per level of Fortune. */
    public static Float FORTUNE_MINIMUM_INCREMENT;

    /** The initial drop bonus/chance cap provided by Looting at Level 1. */
    public static Float LOOTING_INITIAL_LIMIT;
    /** The amount the drop limit decreases per subsequent level of Looting. */
    public static Float LOOTING_DIMINISHING_RETURNS;
    /** The minimum increase to the drop limit per level of Looting. */
    public static Float LOOTING_MINIMUM_INCREMENT;

    /** Default value for {@link #FORTUNE_INITIAL_LIMIT}. */
    public static final double FORTUNE_INITIAL_LIMIT_DEFAULT = 1.0D;
    /** Default value for {@link #FORTUNE_DIMINISHING_RETURNS}. */
    public static final double FORTUNE_DIMINISHING_RETURNS_DEFAULT = 0.0D;
    /** Default value for {@link #FORTUNE_MINIMUM_INCREMENT}. */
    public static final double FORTUNE_MINIMUM_INCREMENT_DEFAULT = 1.0D;
    /** Default value for {@link #LOOTING_INITIAL_LIMIT}. */
    public static final double LOOTING_INITIAL_LIMIT_DEFAULT = 1.0D;
    /** Default value for {@link #LOOTING_DIMINISHING_RETURNS}. */
    public static final double LOOTING_DIMINISHING_RETURNS_DEFAULT = 0.0D;
    /** Default value for {@link #LOOTING_MINIMUM_INCREMENT}. */
    public static final double LOOTING_MINIMUM_INCREMENT_DEFAULT = 1.0D;

    /** Minimum value for
     * {@link #FORTUNE_INITIAL_LIMIT},
     * {@link #FORTUNE_DIMINISHING_RETURNS},
     * {@link #FORTUNE_MINIMUM_INCREMENT},
     * {@link #LOOTING_INITIAL_LIMIT},
     * {@link #LOOTING_DIMINISHING_RETURNS}and
     * {@link #LOOTING_MINIMUM_INCREMENT}. */
    public static final double LOOT_LIMIT_FLOOR = 0.0D;

    /** Maximum value for
     * {@link #FORTUNE_INITIAL_LIMIT},
     * {@link #FORTUNE_DIMINISHING_RETURNS},
     * {@link #FORTUNE_MINIMUM_INCREMENT},
     * {@link #LOOTING_INITIAL_LIMIT},
     * {@link #LOOTING_DIMINISHING_RETURNS}and
     * {@link #LOOTING_MINIMUM_INCREMENT}. */
    public static final double LOOT_LIMIT_CEILING = 32768.0D;

    /** Translation key value for {@link #FORTUNE_INITIAL_LIMIT}. */
    public static final String FORTUNE_INITIAL_LIMIT_KEY = "loot.fortune.initial";
    /** Translation key value for {@link #FORTUNE_DIMINISHING_RETURNS}. */
    public static final String FORTUNE_DIMINISHING_RETURNS_KEY = "loot.fortune.diminishing";
    /** Translation key value for {@link #FORTUNE_MINIMUM_INCREMENT}. */
    public static final String FORTUNE_MINIMUM_INCREMENT_KEY = "loot.fortune.minimum";
    /** Translation key value for {@link #LOOTING_INITIAL_LIMIT}. */
    public static final String LOOTING_INITIAL_LIMIT_KEY = "loot.looting.initial";
    /** Translation key value for {@link #LOOTING_DIMINISHING_RETURNS}. */
    public static final String LOOTING_DIMINISHING_RETURNS_KEY = "loot.looting.diminishing";
    /** Translation key value for {@link #LOOTING_MINIMUM_INCREMENT}. */
    public static final String LOOTING_MINIMUM_INCREMENT_KEY = "loot.looting.minimum";
    // endregion

    // region Unbreaking Enchantment Settings
    /**
     * A multiplier for the effectiveness of the Unbreaking enchantment.
     * Higher values effectively increase the "virtual" durability.
     */
    public static Integer UNBREAKING_STRENGTH;
    /**
     * The chance (0.0 - 1.0) that Unbreaking will simply fail to apply on Armor.
     * Used to balance armor durability vs tool durability.
     */
    public static Double UNBREAKING_ARMOR_PENALTY_FACTOR;

    /** Default value for {@link #UNBREAKING_STRENGTH}. */
    public static final int UNBREAKING_STRENGTH_DEFAULT = 1;
    /** Default value for {@link #UNBREAKING_ARMOR_PENALTY_FACTOR}. */
    public static final double UNBREAKING_ARMOR_PENALTY_FACTOR_DEFAULT = 0.6D;

    /** Minimum value for {@link #UNBREAKING_STRENGTH}. */
    public static final int UNBREAKING_STRENGTH_FLOOR = 1;
    /** Minimum value for {@link #UNBREAKING_ARMOR_PENALTY_FACTOR}. */
    public static final double UNBREAKING_ARMOR_PENALTY_FACTOR_FLOOR = 0.0D;

    /** Maximum value for {@link #UNBREAKING_STRENGTH}. */
    public static final int UNBREAKING_STRENGTH_CEILING = Integer.MAX_VALUE;
    /** Maximum value for {@link #UNBREAKING_ARMOR_PENALTY_FACTOR}. */
    public static final double UNBREAKING_ARMOR_PENALTY_FACTOR_CEILING = 1.0D;

    /** Translation key value for {@link #UNBREAKING_STRENGTH}. */
    public static final String UNBREAKING_STRENGTH_KEY = "unbreaking.strength";
    /** Translation key value for {@link #UNBREAKING_ARMOR_PENALTY_FACTOR}. */
    public static final String UNBREAKING_ARMOR_PENALTY_FACTOR_KEY = "unbreaking.armor.penalty.factor";
    // endregion

    // region XP Growth Settings
    /** The slope of the XP requirement curve. */
    public static Integer XP_GROWTH_FACTOR;
    /** The size of level brackets where the XP cost remains static. */
    public static Integer XP_LEVEL_BRACKET_SIZE;
    /** The initial XP requirement (Y-offset). */
    public static Integer XP_GROWTH_Y_OFFSET;
    /** The maximum level a player can reach. */
    public static Integer XP_MAX_LEVEL;

    /** Default value for {@link #XP_GROWTH_FACTOR}. */
    public static final int XP_GROWTH_FACTOR_DEFAULT = 1;
    /** Default value for {@link #XP_LEVEL_BRACKET_SIZE}. */
    public static final int XP_LEVEL_BRACKET_SIZE_DEFAULT = 2;
    /** Default value for {@link #XP_GROWTH_Y_OFFSET}. */
    public static final int XP_GROWTH_Y_OFFSET_DEFAULT = 2;
    /** Default value for {@link #XP_MAX_LEVEL}. */
    public static final int XP_MAX_LEVEL_DEFAULT = 100;

    /** Minimum value for {@link #XP_GROWTH_FACTOR}. */
    public static final int XP_GROWTH_FACTOR_FLOOR = 0;
    /** Minimum value for {@link #XP_LEVEL_BRACKET_SIZE}. */
    public static final int XP_LEVEL_BRACKET_SIZE_FLOOR = 1;
    /** Minimum value for {@link #XP_GROWTH_Y_OFFSET}. */
    public static final int XP_GROWTH_Y_OFFSET_FLOOR = 1;
    /** Minimum value for {@link #XP_MAX_LEVEL}. */
    public static final int XP_MAX_LEVEL_FLOOR = 3;

    /** Maximum value for {@link #XP_GROWTH_FACTOR}. */
    public static final int XP_GROWTH_FACTOR_CEILING = Integer.MAX_VALUE;
    /** Maximum value for {@link #XP_LEVEL_BRACKET_SIZE}. */
    public static final int XP_LEVEL_BRACKET_SIZE_CEILING = Integer.MAX_VALUE;
    /** Maximum value for {@link #XP_GROWTH_Y_OFFSET}. */
    public static final int XP_GROWTH_Y_OFFSET_CEILING = Integer.MAX_VALUE;
    /** Maximum value for {@link #XP_MAX_LEVEL}. */
    public static final int XP_MAX_LEVEL_CEILING = Integer.MAX_VALUE;

    /** Translation key value for {@link #XP_GROWTH_FACTOR}. */
    public static final String XP_GROWTH_FACTOR_KEY = "xp.requirement.growth.factor";
    /** Translation key value for {@link #XP_LEVEL_BRACKET_SIZE}. */
    public static final String XP_LEVEL_BRACKET_SIZE_KEY = "xp.requirement.growth.bracket.size";
    /** Translation key value for {@link #XP_GROWTH_Y_OFFSET}. */
    public static final String XP_GROWTH_Y_OFFSET_KEY = "xp.requirement.growth.initial.value";
    /** Translation key value for {@link #XP_MAX_LEVEL}. */
    public static final String XP_MAX_LEVEL_KEY = "xp.max.level";
    // endregion

    // region Loot Injection Settings
    /**
     * The probability (0.0 - 1.0) of finding an Enchanted Tome in "Epic" loot chests.
     */
    public static Float EPIC_LOOT_CHANCE;
    /**
     * The probability (0.0 - 1.0) of finding an Enchanted Tome in "Rare" loot chests.
     */
    public static Float RARE_LOOT_CHANCE;
    /**
     * The probability (0.0 - 1.0) of finding an Enchanted Tome in "Uncommon" loot chests.
     */
    public static Float UNCOMMON_LOOT_CHANCE;
    /**
     * The probability (0.0 - 1.0) of finding an Enchanted Tome in "Common" loot chests.
     */
    public static Float COMMON_LOOT_CHANCE;

    /** Default value for {@link #EPIC_LOOT_CHANCE}. */
    public static final double EPIC_LOOT_CHANCE_DEFAULT = 0.8;
    /** Default value for {@link #RARE_LOOT_CHANCE}. */
    public static final double RARE_LOOT_CHANCE_DEFAULT = 0.45;
    /** Default value for {@link #UNCOMMON_LOOT_CHANCE}. */
    public static final double UNCOMMON_LOOT_CHANCE_DEFAULT = 0.2;
    /** Default value for {@link #COMMON_LOOT_CHANCE}. */
    public static final double COMMON_LOOT_CHANCE_DEFAULT = 0.05;

    /** Maximum value for {@link #RARE_LOOT_CHANCE}. */
    public static final double LOOT_CHANCE_FLOOR = 0.00;

    /** Maximum value for {@link #UNCOMMON_LOOT_CHANCE}. */
    public static final double LOOT_CHANCE_CEILING = 1.00;

    /** Translation key value for {@link #EPIC_LOOT_CHANCE}. */
    public static final String EPIC_LOOT_CHANCE_KEY = "loot.chance.epic";
    /** Translation key value for {@link #RARE_LOOT_CHANCE}. */
    public static final String RARE_LOOT_CHANCE_KEY = "loot.chance.rare";
    /** Translation key value for {@link #UNCOMMON_LOOT_CHANCE}. */
    public static final String UNCOMMON_LOOT_CHANCE_KEY = "loot.chance.uncommon";
    /** Translation key value for {@link #COMMON_LOOT_CHANCE}. */
    public static final String COMMON_LOOT_CHANCE_KEY = "loot.chance.common";
    // endregion

    // region Accessibility Settings
    /**
     * If true, the enchanting table UI will use simplified, plain textures for slots
     * instead of the standard stylized ones.
     */
    public static Boolean USE_PLAIN_BACKGROUND;
    /**
     * If true, new enchantments in the table (Apply slot) will be rendered in the
     * Standard Galactic Alphabet (obfuscated). If false, they are readable.
     */
    public static Boolean OBFUSCATE_NEW_ENCHANTMENTS;
    /**
     * If true, enchantment names are no longer colored according to their theme.
     */
    public static Boolean OVERRIDE_ENCHANTMENT_NAME_COLORING;
    /**
     * The override color of enchantment names.
     */
    public static Integer OVERRIDE_ENCHANTMENT_NAME_COLOR;
    /**
     * If true, enchantment levels are no longer colored according to their level.
     */
    public static Boolean OVERRIDE_ENCHANTMENT_LEVEL_COLORING;
    /**
     * The override color of enchantment levels
     */
    public static Integer OVERRIDE_ENCHANTMENT_LEVEL_COLOR;
    /**
     * If false, enchantment descriptions are no longer being displayed.
     */
    public static Boolean SHOW_ENCHANTMENT_DESCRIPTIONS;
    /**
     * The color of enchantment description texts.
     */
    public static Integer ENCHANTMENT_DESCRIPTION_COLOR;

    /** Default value for {@link #USE_PLAIN_BACKGROUND}. */
    public static final boolean USE_PLAIN_BACKGROUND_DEFAULT = false;
    /** Default value for {@link #OBFUSCATE_NEW_ENCHANTMENTS}. */
    public static final boolean OBFUSCATE_NEW_ENCHANTMENTS_DEFAULT = true;
    /** Default value for {@link #OVERRIDE_ENCHANTMENT_NAME_COLORING}. */
    public static final boolean OVERRIDE_ENCHANTMENT_NAME_COLORING_DEFAULT = false;
    /** Default value for {@link #OVERRIDE_ENCHANTMENT_NAME_COLOR}. */
    public static final int OVERRIDE_ENCHANTMENT_NAME_COLOR_DEFAULT = 0xFFFFFF;
    /** Default value for {@link #OVERRIDE_ENCHANTMENT_LEVEL_COLORING}. */
    public static final boolean OVERRIDE_ENCHANTMENT_LEVEL_COLORING_DEFAULT = false;
    /** Default value for {@link #OVERRIDE_ENCHANTMENT_LEVEL_COLOR}. */
    public static final int OVERRIDE_ENCHANTMENT_LEVEL_COLOR_DEFAULT = 0xFFFFFF;
    /** Default value for {@link #SHOW_ENCHANTMENT_DESCRIPTIONS}. */
    public static final boolean SHOW_ENCHANTMENT_DESCRIPTIONS_DEFAULT = true;
    /** Default value for {@link #ENCHANTMENT_DESCRIPTION_COLOR}. */
    public static final int ENCHANTMENT_DESCRIPTION_COLOR_DEFAULT = 0xA7A7A7;

    /** Max value for {@link #OVERRIDE_ENCHANTMENT_NAME_COLOR}. */
    public static final int OVERRIDE_ENCHANTMENT_NAME_COLOR_CEILING = 0xFFFFFF;
    /** Max value for {@link #OVERRIDE_ENCHANTMENT_LEVEL_COLOR}. */
    public static final int OVERRIDE_ENCHANTMENT_LEVEL_COLOR_CEILING = 0xFFFFFF;
    /** Max value for {@link #ENCHANTMENT_DESCRIPTION_COLOR}. */
    public static final int ENCHANTMENT_DESCRIPTION_COLOR_CEILING = 0xFFFFFF;

    /** Min value for {@link #OVERRIDE_ENCHANTMENT_NAME_COLOR}. */
    public static final int OVERRIDE_ENCHANTMENT_NAME_COLOR_FLOOR = 0x000000;
    /** Min value for {@link #OVERRIDE_ENCHANTMENT_LEVEL_COLOR}. */
    public static final int OVERRIDE_ENCHANTMENT_LEVEL_COLOR_FLOOR = 0x000000;
    /** Min value for {@link #ENCHANTMENT_DESCRIPTION_COLOR}. */
    public static final int ENCHANTMENT_DESCRIPTION_COLOR_FLOOR = 0x000000;

    /** Translation key value for {@link #USE_PLAIN_BACKGROUND}. */
    public static final String USE_PLAIN_BACKGROUND_KEY = "accessibility.use.accessible.button.textures";
    /** Translation key value for {@link #OBFUSCATE_NEW_ENCHANTMENTS}. */
    public static final String OBFUSCATE_NEW_ENCHANTMENTS_KEY = "accessibility.obfuscate.new.enchantments";
    /** Translation key value for {@link #OVERRIDE_ENCHANTMENT_NAME_COLORING}. */
    public static final String OVERRIDE_ENCHANTMENT_NAME_COLORING_KEY = "accessibility.enchantment_name.override_enchantment_name_color";
    /** Translation key value for {@link #OVERRIDE_ENCHANTMENT_NAME_COLOR}. */
    public static final String OVERRIDE_ENCHANTMENT_NAME_COLOR_KEY = "accessibility.enchantment_name.enchantment_name_color_override_value";
    /** Translation key value for {@link #OVERRIDE_ENCHANTMENT_LEVEL_COLORING}. */
    public static final String OVERRIDE_ENCHANTMENT_LEVEL_COLORING_KEY = "accessibility.enchantment_level.override_enchantment_level_color";
    /** Translation key value for {@link #OVERRIDE_ENCHANTMENT_LEVEL_COLOR}. */
    public static final String OVERRIDE_ENCHANTMENT_LEVEL_COLOR_KEY = "accessibility.enchantment_level.enchantment_level_color_override_value";
    /** Translation key value for {@link #SHOW_ENCHANTMENT_DESCRIPTIONS}. */
    public static final String SHOW_ENCHANTMENT_DESCRIPTIONS_KEY = "accessibility.enchantment_description.show_enchantment_descriptions";
    /** Translation key value for {@link #ENCHANTMENT_DESCRIPTION_COLOR}. */
    public static final String ENCHANTMENT_DESCRIPTION_COLOR_KEY = "accessibility.enchantment_description.enchantment_description_color_value";
    // endregion
}