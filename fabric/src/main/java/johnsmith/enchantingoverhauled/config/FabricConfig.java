package johnsmith.enchantingoverhauled.config;

import johnsmith.enchantingoverhauled.Constants;

import com.mojang.datafixers.util.Pair;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Fabric implementation of the configuration system.
 * <p>
 * This class bridges the gap between the common {@link Config} holder and the physical configuration file
 * on the Fabric platform. It uses the lightweight {@link SimpleConfig} wrapper and a custom {@link ConfigProvider}
 * to define, load, and save configuration settings.
 */
public class FabricConfig {
    /**
     * The loaded SimpleConfig instance, acting as a map of key-value pairs read from the file.
     */
    public static SimpleConfig CONFIG;

    /**
     * The provider defining the structure, comments, and default values of the configuration file.
     */
    public static ConfigProvider configs;

    /**
     * Defines the structure of the configuration file.
     * <p>
     * Populates the {@link ConfigProvider} with sections, entries, default values, and comments.
     * This schema is used to generate the config file if it doesn't exist and to format it when saving.
     */
    public static void createConfigs() {
        // Enchanting Table
        configs.addSection("Enchanting Table Settings");
        configs.addEntry(new Pair<>(Config.ARCANE_RETRIBUTION_KEY, Config.ARCANE_RETRIBUTION_DEFAULT),
                new String[]{"Whether the enchanting table should violently reject books."}, "");
        configs.addEntry(new Pair<>(Config.ACTIVATION_EFFECTS_KEY, Config.ACTIVATION_EFFECTS_DEFAULT),
                new String[]{"Whether the enchanting table should have a spectacular activation."}, "");

        // Anvil
        configs.addSection("Anvil Settings");
        configs.addEntry(new Pair<>(Config.ANVIL_MAX_ITEM_COST_KEY, Config.ANVIL_MAX_ITEM_COST_DEFAULT),
                new String[]{"The maximum repair item cost for an item."},
                "Min: " + Config.ANVIL_MAX_ITEM_COST_FLOOR + ", Max: " + Config.ANVIL_MAX_ITEM_COST_CEILING);
        configs.addEntry(new Pair<>(Config.ANVIL_REPAIR_BONUS_KEY, Config.ANVIL_REPAIR_BONUS_DEFAULT),
                new String[]{"Durability bonus (as percentage) added when combining damaged items.",
                        "Vanilla Minecraft uses 12%."},
                "Min: " + Config.ANVIL_REPAIR_BONUS_FLOOR + ", Max: " + Config.ANVIL_REPAIR_BONUS_CEILING);
        configs.addEntry(new Pair<>(Config.ANVIL_BREAK_CHANCE_KEY, Config.ANVIL_BREAK_CHANCE_DEFAULT),
                new String[]{"Chance for the anvil to take damage on use. Vanilla is 12% (0.12)."},
                "Min: " + Config.ANVIL_BREAK_CHANCE_FLOOR + ", Max: " + Config.ANVIL_BREAK_CHANCE_CEILING );

        // Enchantment Levels
        configs.addSection("Enchantment Level Settings");
        configs.addEntry(new Pair<>(Config.ENCHANTMENT_MAX_LEVEL_KEY, Config.ENCHANTMENT_MAX_LEVEL_DEFAULT),
                new String[]{"The maximum level an enchantment can be *naturally* obtained at (e.g., enchanting table)."},
                "Min: " + Config.ENCHANTMENT_MAX_LEVEL_FLOOR + ", Max: " + Config.ENCHANTMENT_MAX_LEVEL_CEILING);
        configs.addEntry(new Pair<>(Config.TOMES_HAVE_GREATER_ENCHANTMENTS_KEY, Config.TOMES_HAVE_GREATER_ENCHANTMENTS_DEFAULT),
                new String[]{"Whether enchanted tomes can contain enchantments of a higher level than naturally possible."}, "");

        // Protection Enchantment
        configs.addSection("Protection Enchantment Settings");
        configs.addEntry(new Pair<>(Config.PROTECTION_CAP_KEY, Config.PROTECTION_CAP_DEFAULT),
                new String[]{"The maximum effective points you can get from enchantments.",
                        "Vanilla default is 20."},
                "Min: " + Config.PROTECTION_CAP_FLOOR + ", Max: " + Config.PROTECTION_CAP_CEILING);
        configs.addEntry(new Pair<>(Config.PROTECTION_DIVISOR_KEY, Config.PROTECTION_DIVISOR_DEFAULT),
                new String[]{"The value used to calculate damage reduction from protection points. (Value / Divisor)",
                        "This value *WILL* be higher than 'protection.cap'."},
                "Min: " + Config.PROTECTION_DIVISOR_FLOOR + ", Max: " + Config.PROTECTION_DIVISOR_CEILING);
        configs.addEntry(new Pair<>(Config.PHYSICAL_PROTECTION_STRENGTH_KEY, Config.PHYSICAL_PROTECTION_STRENGTH_DEFAULT),
                new String[]{"Enchantment protection factor for 'Physical Protection'."},
                "Min: " + Config.PROTECTION_STRENGTH_FLOOR + ", Max: " + Config.PROTECTION_STRENGTH_CEILING);
        configs.addEntry(new Pair<>(Config.FIRE_PROTECTION_STRENGTH_KEY, Config.FIRE_PROTECTION_STRENGTH_DEFAULT),
                new String[]{"Enchantment protection factor for 'Fire Protection'."},
                "Min: " + Config.PROTECTION_STRENGTH_FLOOR + ", Max: " + Config.PROTECTION_STRENGTH_CEILING);
        configs.addEntry(new Pair<>(Config.BLAST_PROTECTION_STRENGTH_KEY, Config.BLAST_PROTECTION_STRENGTH_DEFAULT),
                new String[]{"Enchantment protection factor for 'Blast Protection'."},
                "Min: " + Config.PROTECTION_STRENGTH_FLOOR + ", Max: " + Config.PROTECTION_STRENGTH_CEILING);
        configs.addEntry(new Pair<>(Config.PROJECTILE_PROTECTION_STRENGTH_KEY, Config.PROJECTILE_PROTECTION_STRENGTH_DEFAULT),
                new String[]{"Enchantment protection factor for 'Projectile Protection'."},
                "Min: " + Config.PROTECTION_STRENGTH_FLOOR + ", Max: " + Config.PROTECTION_STRENGTH_CEILING);
        configs.addEntry(new Pair<>(Config.MAGIC_PROTECTION_STRENGTH_KEY, Config.MAGIC_PROTECTION_STRENGTH_DEFAULT),
                new String[]{"Enchantment protection factor for 'Magic Protection' (if implemented)."},
                "Min: " + Config.PROTECTION_STRENGTH_FLOOR + ", Max: " + Config.PROTECTION_STRENGTH_CEILING);
        configs.addEntry(new Pair<>(Config.FEATHER_FALLING_STRENGTH_KEY, Config.FEATHER_FALLING_STRENGTH_DEFAULT),
                new String[]{"Enchantment protection factor for 'Feather Falling'."},
                "Min: " + Config.PROTECTION_STRENGTH_FLOOR + ", Max: " + Config.PROTECTION_STRENGTH_CEILING);

        // Damage Enchantments
        configs.addSection("Damage Enchantment Settings");
        configs.addEntry(new Pair<>(Config.SHARPNESS_INITIAL_DAMAGE_KEY, Config.SHARPNESS_INITIAL_DAMAGE_DEFAULT),
                new String[]{"Initial damage bonus applied by Sharpness (Level 1)."}, "Min: " + Config.SHARPNESS_INITIAL_DAMAGE_FLOOR + ", Max: " + Config.SHARPNESS_INITIAL_DAMAGE_CEILING);
        configs.addEntry(new Pair<>(Config.SHARPNESS_DIMINISHING_RETURNS_KEY, Config.SHARPNESS_DIMINISHING_RETURNS_DEFAULT),
                new String[]{"How much the damage bonus decreases per subsequent level."}, "Min: " + Config.SHARPNESS_DIMINISHING_RETURNS_FLOOR + ", Max: " + Config.SHARPNESS_DIMINISHING_RETURNS_CEILING);
        configs.addEntry(new Pair<>(Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_KEY, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_DEFAULT),
                new String[]{"The minimum damage added per level (prevents damage bonus from hitting 0)."}, "Min: " + Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_FLOOR + ", Max: " + Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_CEILING);
        configs.addEntry(new Pair<>(Config.SMITE_MULTIPLIER_KEY, Config.DAMAGE_MULTIPLIER_DEFAULT),
                new String[]{"Multiplier applied to damage for Smite (vs Undead)."}, "Min: " + Config.DAMAGE_MULTIPLIER_FLOOR + ", Max: " + Config.DAMAGE_MULTIPLIER_CEILING);
        configs.addEntry(new Pair<>(Config.EXTERMINATION_MULTIPLIER_KEY, Config.DAMAGE_MULTIPLIER_DEFAULT),
                new String[]{"Multiplier applied to damage for Bane of Arthropods."}, "Min: " + Config.DAMAGE_MULTIPLIER_FLOOR + ", Max: " + Config.DAMAGE_MULTIPLIER_CEILING);

        // Loot Enchantments
        configs.addSection("Loot Enchantment Settings");
        configs.addEntry(new Pair<>(Config.FORTUNE_INITIAL_LIMIT_KEY, Config.FORTUNE_INITIAL_LIMIT_DEFAULT),
                new String[]{"Initial bonus applied by Fortune (Level 1)."}, "Min: " + Config.LOOT_LIMIT_FLOOR + ", Max: " + Config.LOOT_LIMIT_CEILING);
        configs.addEntry(new Pair<>(Config.FORTUNE_DIMINISHING_RETURNS_KEY, Config.FORTUNE_DIMINISHING_RETURNS_DEFAULT),
                new String[]{"How much the bonus decreases per subsequent level."}, "Min: " + Config.LOOT_LIMIT_FLOOR + ", Max: " + Config.LOOT_LIMIT_CEILING);
        configs.addEntry(new Pair<>(Config.FORTUNE_MINIMUM_INCREMENT_KEY, Config.FORTUNE_MINIMUM_INCREMENT_DEFAULT),
                new String[]{"The minimum bonus added per level."}, "Min: " + Config.LOOT_LIMIT_FLOOR + ", Max: " + Config.LOOT_LIMIT_CEILING);
        configs.addEntry(new Pair<>(Config.LOOTING_INITIAL_LIMIT_KEY, Config.LOOTING_INITIAL_LIMIT_DEFAULT),
                new String[]{"Initial bonus applied by Looting (Level 1)."}, "Min: " + Config.LOOT_LIMIT_FLOOR + ", Max: " + Config.LOOT_LIMIT_CEILING);
        configs.addEntry(new Pair<>(Config.LOOTING_DIMINISHING_RETURNS_KEY, Config.LOOTING_DIMINISHING_RETURNS_DEFAULT),
                new String[]{"How much the bonus decreases per subsequent level."}, "Min: " + Config.LOOT_LIMIT_FLOOR + ", Max: " + Config.LOOT_LIMIT_CEILING);
        configs.addEntry(new Pair<>(Config.LOOTING_MINIMUM_INCREMENT_KEY, Config.LOOTING_MINIMUM_INCREMENT_DEFAULT),
                new String[]{"The minimum bonus added per level."}, "Min: " + Config.LOOT_LIMIT_FLOOR + ", Max: " + Config.LOOT_LIMIT_CEILING);

        // Unbreaking Enchantment
        configs.addSection("Unbreaking Enchantment Settings");
        configs.addEntry(new Pair<>(Config.UNBREAKING_STRENGTH_KEY, Config.UNBREAKING_STRENGTH_DEFAULT),
                new String[]{"A multiplier for Unbreaking's effectiveness."},
                "Min: " + Config.UNBREAKING_STRENGTH_FLOOR + ", Max: " + Config.UNBREAKING_STRENGTH_CEILING);
        configs.addEntry(new Pair<>(Config.UNBREAKING_ARMOR_PENALTY_FACTOR_KEY, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_DEFAULT),
                new String[]{"The chance for unbreaking to not take effect on armor.",
                        "0.0 = no penalty, 1.0 = 100% penalty (no effect)."},
                "Min: " + Config.UNBREAKING_ARMOR_PENALTY_FACTOR_FLOOR + ", Max: " + Config.UNBREAKING_ARMOR_PENALTY_FACTOR_CEILING);

        // XP Growth
        configs.addSection("XP Growth Settings");
        configs.addEntry(new Pair<>(Config.XP_GROWTH_FACTOR_KEY, Config.XP_GROWTH_FACTOR_DEFAULT),
                new String[]{"XP requirement growth factor."},
                "Min: " + Config.XP_GROWTH_FACTOR_FLOOR + ", Max: " + Config.XP_GROWTH_FACTOR_CEILING);
        configs.addEntry(new Pair<>(Config.XP_LEVEL_BRACKET_SIZE_KEY, Config.XP_LEVEL_BRACKET_SIZE_DEFAULT),
                new String[]{"Size of XP level brackets for growth calculation."},
                "Min: " + Config.XP_LEVEL_BRACKET_SIZE_FLOOR + ", Max: " + Config.XP_LEVEL_BRACKET_SIZE_CEILING);
        configs.addEntry(new Pair<>(Config.XP_GROWTH_Y_OFFSET_KEY, Config.XP_GROWTH_Y_OFFSET_DEFAULT),
                new String[]{"Initial XP requirement value (Y-offset)."},
                "Min: " + Config.XP_GROWTH_Y_OFFSET_FLOOR + ", Max: " + Config.XP_GROWTH_Y_OFFSET_CEILING);
        configs.addEntry(new Pair<>(Config.XP_MAX_LEVEL_KEY, Config.XP_MAX_LEVEL_DEFAULT),
                new String[]{"Maximum player level."},
                "Min: " + Config.XP_MAX_LEVEL_FLOOR + ", Max: " + Config.XP_MAX_LEVEL_CEILING);

        // Loot
        configs.addSection("Loot Settings");
        configs.addEntry(new Pair<>(Config.EPIC_LOOT_CHANCE_KEY, Config.EPIC_LOOT_CHANCE_DEFAULT),
                new String[]{"Chance for an Enchanted Tome to appear in epic loot chests (0.0 - 1.0)."}, "Min: " + Config.LOOT_CHANCE_FLOOR + ", Max: " + Config.LOOT_CHANCE_CEILING);
        configs.addEntry(new Pair<>(Config.RARE_LOOT_CHANCE_KEY, Config.RARE_LOOT_CHANCE_DEFAULT),
                new String[]{"Chance for an Enchanted Tome to appear in rare loot chests (0.0 - 1.0)."}, "Min: " + Config.LOOT_CHANCE_FLOOR + ", Max: " + Config.LOOT_CHANCE_CEILING);
        configs.addEntry(new Pair<>(Config.UNCOMMON_LOOT_CHANCE_KEY, Config.UNCOMMON_LOOT_CHANCE_DEFAULT),
                new String[]{"Chance for an Enchanted Tome to appear in uncommon loot chests (0.0 - 1.0)."}, "Min: " + Config.LOOT_CHANCE_FLOOR + ", Max: " + Config.LOOT_CHANCE_CEILING);
        configs.addEntry(new Pair<>(Config.COMMON_LOOT_CHANCE_KEY, Config.COMMON_LOOT_CHANCE_DEFAULT),
                new String[]{"Chance for an Enchanted Tome to appear in common loot chests (0.0 - 1.0)."}, "Min: " + Config.LOOT_CHANCE_FLOOR + ", Max: " + Config.LOOT_CHANCE_CEILING);

        // Accessibility
        configs.addSection("Accessibility Settings");
        configs.addEntry(new Pair<>(Config.USE_PLAIN_BACKGROUND_KEY, Config.USE_PLAIN_BACKGROUND_DEFAULT),
                new String[]{"Whether the enchanting table buttons should be plain or textured."}, "");
        configs.addEntry(new Pair<>(Config.OBFUSCATE_NEW_ENCHANTMENTS_KEY, Config.OBFUSCATE_NEW_ENCHANTMENTS_DEFAULT),
                new String[]{"Whether the enchanting table should show new enchantments in the standard galactic alphabet."}, "");

        configs.addEntry(new Pair<>(Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_KEY, Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_DEFAULT),
                new String[]{"Whether enchantment names should be colored uniformly or according to their theme."}, "");
        configs.addEntry(new Pair<>(Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_KEY, Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_DEFAULT),
                new String[]{"The override color of enchantment names. Only applies if override is true."},
                "Min: " + Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_FLOOR + ", Max: " + Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_CEILING);

        configs.addEntry(new Pair<>(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_KEY, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_DEFAULT),
                new String[]{"Whether enchantment levels should be colored uniformly or according to their level."}, "");
        configs.addEntry(new Pair<>(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_KEY, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_DEFAULT),
                new String[]{"The override color of enchantment levels. Only applies if override is true."},
                "Min: " + Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_FLOOR + ", Max: " + Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_CEILING);

        configs.addEntry(new Pair<>(Config.SHOW_ENCHANTMENT_DESCRIPTIONS_KEY, Config.SHOW_ENCHANTMENT_DESCRIPTIONS_DEFAULT),
                new String[]{"Whether enchantment descriptions should be shown."}, "");
        configs.addEntry(new Pair<>(Config.ENCHANTMENT_DESCRIPTION_COLOR_KEY, Config.ENCHANTMENT_DESCRIPTION_COLOR_DEFAULT),
                new String[]{"The color of enchantment description texts. Only applicable if show enchantment descriptions is true."},
                "Min: " + Config.ENCHANTMENT_DESCRIPTION_COLOR_FLOOR + ", Max: " + Config.ENCHANTMENT_DESCRIPTION_COLOR_CEILING);
    }

    /**
     * Reads values from the loaded {@link #CONFIG} object and assigns them to the static
     * fields in the common {@link Config} class.
     * <p>
     * This method also applies clamping to ensure that all values are within their valid
     * min/max ranges as defined in {@link Config}.
     */
    public static void assignConfigs() {
        // Enchanting Table
        Config.ARCANE_RETRIBUTION = CONFIG.getOrDefault(Config.ARCANE_RETRIBUTION_KEY, Config.ARCANE_RETRIBUTION_DEFAULT);
        Config.ACTIVATION_EFFECTS = CONFIG.getOrDefault(Config.ACTIVATION_EFFECTS_KEY, Config.ACTIVATION_EFFECTS_DEFAULT);

        // Anvil
        Config.ANVIL_MAX_ITEM_COST = Math.clamp(CONFIG.getOrDefault(Config.ANVIL_MAX_ITEM_COST_KEY, Config.ANVIL_MAX_ITEM_COST_DEFAULT), Config.ANVIL_MAX_ITEM_COST_FLOOR, Config.ANVIL_MAX_ITEM_COST_CEILING);
        Config.ANVIL_REPAIR_BONUS = Math.clamp(CONFIG.getOrDefault(Config.ANVIL_REPAIR_BONUS_KEY, Config.ANVIL_REPAIR_BONUS_DEFAULT), Config.ANVIL_REPAIR_BONUS_FLOOR, Config.ANVIL_REPAIR_BONUS_CEILING);
        Config.ANVIL_BREAK_CHANCE = Math.clamp(CONFIG.getOrDefault(Config.ANVIL_BREAK_CHANCE_KEY, Config.ANVIL_BREAK_CHANCE_DEFAULT), Config.ANVIL_BREAK_CHANCE_FLOOR, Config.ANVIL_BREAK_CHANCE_CEILING);

        // Enchantment Levels
        Config.ENCHANTMENT_MAX_LEVEL = Math.clamp(CONFIG.getOrDefault(Config.ENCHANTMENT_MAX_LEVEL_KEY, Config.ENCHANTMENT_MAX_LEVEL_DEFAULT), Config.ENCHANTMENT_MAX_LEVEL_FLOOR, Config.ENCHANTMENT_MAX_LEVEL_CEILING);
        Config.TOMES_HAVE_GREATER_ENCHANTMENTS = CONFIG.getOrDefault(Config.TOMES_HAVE_GREATER_ENCHANTMENTS_KEY, Config.TOMES_HAVE_GREATER_ENCHANTMENTS_DEFAULT);

        // Protection Enchantment
        Config.PROTECTION_CAP = Math.clamp(CONFIG.getOrDefault(Config.PROTECTION_CAP_KEY, Config.PROTECTION_CAP_DEFAULT), Config.PROTECTION_CAP_FLOOR, Config.PROTECTION_CAP_CEILING);
        Config.PROTECTION_DIVISOR = Math.clamp(CONFIG.getOrDefault(Config.PROTECTION_DIVISOR_KEY, Config.PROTECTION_DIVISOR_DEFAULT), Config.PROTECTION_DIVISOR_FLOOR, Config.PROTECTION_CAP_CEILING);

        // Protection Enchantment
        Config.PHYSICAL_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.PHYSICAL_PROTECTION_STRENGTH_KEY, Config.PHYSICAL_PROTECTION_STRENGTH_DEFAULT), Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
        Config.FIRE_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.FIRE_PROTECTION_STRENGTH_KEY, Config.FIRE_PROTECTION_STRENGTH_DEFAULT), Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
        Config.BLAST_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.BLAST_PROTECTION_STRENGTH_KEY, Config.BLAST_PROTECTION_STRENGTH_DEFAULT), Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
        Config.PROJECTILE_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.PROJECTILE_PROTECTION_STRENGTH_KEY, Config.PROJECTILE_PROTECTION_STRENGTH_DEFAULT), Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
        Config.MAGIC_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.MAGIC_PROTECTION_STRENGTH_KEY, Config.MAGIC_PROTECTION_STRENGTH_DEFAULT), Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
        Config.FEATHER_FALLING_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.FEATHER_FALLING_STRENGTH_KEY, Config.FEATHER_FALLING_STRENGTH_DEFAULT), Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);

        // Damage Enchantment
        Config.SHARPNESS_INITIAL_DAMAGE = ((Double) Math.clamp(CONFIG.getOrDefault(Config.SHARPNESS_INITIAL_DAMAGE_KEY, Config.SHARPNESS_INITIAL_DAMAGE_DEFAULT), Config.SHARPNESS_INITIAL_DAMAGE_FLOOR, Config.SHARPNESS_INITIAL_DAMAGE_CEILING)).floatValue();
        Config.SHARPNESS_DIMINISHING_RETURNS = ((Double) Math.clamp(CONFIG.getOrDefault(Config.SHARPNESS_DIMINISHING_RETURNS_KEY, Config.SHARPNESS_DIMINISHING_RETURNS_DEFAULT), Config.SHARPNESS_DIMINISHING_RETURNS_FLOOR, Config.SHARPNESS_DIMINISHING_RETURNS_CEILING)).floatValue();
        Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT = ((Double) Math.clamp(CONFIG.getOrDefault(Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_KEY, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_DEFAULT), Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_FLOOR, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_CEILING)).floatValue();
        Config.SMITE_MULTIPLIER = ((Double) Math.clamp(CONFIG.getOrDefault(Config.SMITE_MULTIPLIER_KEY, Config.DAMAGE_MULTIPLIER_DEFAULT), Config.DAMAGE_MULTIPLIER_FLOOR, Config.DAMAGE_MULTIPLIER_CEILING)).floatValue();
        Config.EXTERMINATION_MULTIPLIER = ((Double) Math.clamp(CONFIG.getOrDefault(Config.EXTERMINATION_MULTIPLIER_KEY, Config.DAMAGE_MULTIPLIER_DEFAULT), Config.DAMAGE_MULTIPLIER_FLOOR, Config.DAMAGE_MULTIPLIER_CEILING)).floatValue();

        // Loot Enchantment
        Config.FORTUNE_INITIAL_LIMIT = ((Double) Math.clamp(CONFIG.getOrDefault(Config.FORTUNE_INITIAL_LIMIT_KEY, Config.FORTUNE_INITIAL_LIMIT_DEFAULT), Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING)).floatValue();
        Config.FORTUNE_DIMINISHING_RETURNS = ((Double) Math.clamp(CONFIG.getOrDefault(Config.FORTUNE_DIMINISHING_RETURNS_KEY, Config.FORTUNE_DIMINISHING_RETURNS_DEFAULT), Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING)).floatValue();
        Config.FORTUNE_MINIMUM_INCREMENT = ((Double) Math.clamp(CONFIG.getOrDefault(Config.FORTUNE_MINIMUM_INCREMENT_KEY, Config.FORTUNE_MINIMUM_INCREMENT_DEFAULT), Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING)).floatValue();
        Config.LOOTING_INITIAL_LIMIT = ((Double) Math.clamp(CONFIG.getOrDefault(Config.LOOTING_INITIAL_LIMIT_KEY, Config.LOOTING_INITIAL_LIMIT_DEFAULT), Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING)).floatValue();
        Config.LOOTING_DIMINISHING_RETURNS = ((Double) Math.clamp(CONFIG.getOrDefault(Config.LOOTING_DIMINISHING_RETURNS_KEY, Config.LOOTING_DIMINISHING_RETURNS_DEFAULT), Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING)).floatValue();
        Config.LOOTING_MINIMUM_INCREMENT = ((Double) Math.clamp(CONFIG.getOrDefault(Config.LOOTING_MINIMUM_INCREMENT_KEY, Config.LOOTING_MINIMUM_INCREMENT_DEFAULT), Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING)).floatValue();

        // Unbreaking Enchantment
        Config.UNBREAKING_STRENGTH = Math.clamp(CONFIG.getOrDefault(Config.UNBREAKING_STRENGTH_KEY, Config.UNBREAKING_STRENGTH_DEFAULT), Config.UNBREAKING_STRENGTH_FLOOR, Config.UNBREAKING_STRENGTH_CEILING);
        Config.UNBREAKING_ARMOR_PENALTY_FACTOR = Math.clamp(CONFIG.getOrDefault(Config.UNBREAKING_ARMOR_PENALTY_FACTOR_KEY, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_DEFAULT), Config.UNBREAKING_ARMOR_PENALTY_FACTOR_FLOOR, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_CEILING);

        // XP Growth
        Config.XP_GROWTH_FACTOR = Math.clamp(CONFIG.getOrDefault(Config.XP_GROWTH_FACTOR_KEY, Config.XP_GROWTH_FACTOR_DEFAULT), Config.XP_GROWTH_FACTOR_FLOOR, Config.XP_GROWTH_FACTOR_CEILING);
        Config.XP_LEVEL_BRACKET_SIZE = Math.clamp(CONFIG.getOrDefault(Config.XP_LEVEL_BRACKET_SIZE_KEY, Config.XP_LEVEL_BRACKET_SIZE_DEFAULT), Config.XP_LEVEL_BRACKET_SIZE_FLOOR, Config.XP_LEVEL_BRACKET_SIZE_CEILING);
        Config.XP_GROWTH_Y_OFFSET = Math.clamp(CONFIG.getOrDefault(Config.XP_GROWTH_Y_OFFSET_KEY, Config.XP_GROWTH_Y_OFFSET_DEFAULT), Config.XP_GROWTH_Y_OFFSET_FLOOR, Config.XP_GROWTH_Y_OFFSET_CEILING);
        Config.XP_MAX_LEVEL = Math.clamp(CONFIG.getOrDefault(Config.XP_MAX_LEVEL_KEY, Config.XP_MAX_LEVEL_DEFAULT),  Config.XP_MAX_LEVEL_FLOOR, Config.XP_MAX_LEVEL_CEILING);

        // Loot
        Config.EPIC_LOOT_CHANCE = ((Double) Math.clamp(CONFIG.getOrDefault(Config.EPIC_LOOT_CHANCE_KEY, Config.EPIC_LOOT_CHANCE_DEFAULT), Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING)).floatValue();
        Config.RARE_LOOT_CHANCE = ((Double) Math.clamp(CONFIG.getOrDefault(Config.RARE_LOOT_CHANCE_KEY, Config.RARE_LOOT_CHANCE_DEFAULT), Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING)).floatValue();
        Config.UNCOMMON_LOOT_CHANCE = ((Double) Math.clamp(CONFIG.getOrDefault(Config.UNCOMMON_LOOT_CHANCE_KEY, Config.UNCOMMON_LOOT_CHANCE_DEFAULT), Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING)).floatValue();
        Config.COMMON_LOOT_CHANCE = ((Double) Math.clamp(CONFIG.getOrDefault(Config.COMMON_LOOT_CHANCE_KEY, Config.COMMON_LOOT_CHANCE_DEFAULT), Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING)).floatValue();
        // Accessibility
        Config.USE_PLAIN_BACKGROUND = CONFIG.getOrDefault(Config.USE_PLAIN_BACKGROUND_KEY, Config.USE_PLAIN_BACKGROUND_DEFAULT);
        Config.OBFUSCATE_NEW_ENCHANTMENTS = CONFIG.getOrDefault(Config.OBFUSCATE_NEW_ENCHANTMENTS_KEY, Config.OBFUSCATE_NEW_ENCHANTMENTS_DEFAULT);

        Config.OVERRIDE_ENCHANTMENT_NAME_COLORING = CONFIG.getOrDefault(Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_KEY, Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_DEFAULT);
        Config.OVERRIDE_ENCHANTMENT_NAME_COLOR = CONFIG.getOrDefault(Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_KEY, Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_DEFAULT);

        Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING = CONFIG.getOrDefault(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_KEY, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_DEFAULT);
        Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR = CONFIG.getOrDefault(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_KEY, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_DEFAULT);

        Config.SHOW_ENCHANTMENT_DESCRIPTIONS = CONFIG.getOrDefault(Config.SHOW_ENCHANTMENT_DESCRIPTIONS_KEY, Config.SHOW_ENCHANTMENT_DESCRIPTIONS_DEFAULT);
        Config.ENCHANTMENT_DESCRIPTION_COLOR = CONFIG.getOrDefault(Config.ENCHANTMENT_DESCRIPTION_COLOR_KEY, Config.ENCHANTMENT_DESCRIPTION_COLOR_DEFAULT);
    }

    /**
     * Saves the current in-memory configuration values to the physical file on disk.
     * <p>
     * It gathers the current values from the static fields in the {@link Config} class,
     * generates the file content using the {@link ConfigProvider}, and writes it to the config directory.
     */
    public static void save() {
        // 1. Create a map of the current runtime values
        Map<String, Object> currentValues = new HashMap<>();

        // Enchanting Table
        currentValues.put(Config.ARCANE_RETRIBUTION_KEY, Config.ARCANE_RETRIBUTION);
        currentValues.put(Config.ACTIVATION_EFFECTS_KEY, Config.ACTIVATION_EFFECTS);

        // Anvil
        currentValues.put(Config.ANVIL_MAX_ITEM_COST_KEY, Config.ANVIL_MAX_ITEM_COST);
        currentValues.put(Config.ANVIL_REPAIR_BONUS_KEY, Config.ANVIL_REPAIR_BONUS);
        currentValues.put(Config.ANVIL_BREAK_CHANCE_KEY, Config.ANVIL_BREAK_CHANCE);

        // Enchantment Levels
        currentValues.put(Config.ENCHANTMENT_MAX_LEVEL_KEY, Config.ENCHANTMENT_MAX_LEVEL);

        // Protection
        currentValues.put(Config.PROTECTION_CAP_KEY, Config.PROTECTION_CAP);
        currentValues.put(Config.PROTECTION_DIVISOR_KEY, Config.PROTECTION_DIVISOR);
        currentValues.put(Config.PHYSICAL_PROTECTION_STRENGTH_KEY, Config.PHYSICAL_PROTECTION_STRENGTH);
        currentValues.put(Config.FIRE_PROTECTION_STRENGTH_KEY, Config.FIRE_PROTECTION_STRENGTH);
        currentValues.put(Config.BLAST_PROTECTION_STRENGTH_KEY, Config.BLAST_PROTECTION_STRENGTH);
        currentValues.put(Config.PROJECTILE_PROTECTION_STRENGTH_KEY, Config.PROJECTILE_PROTECTION_STRENGTH);
        currentValues.put(Config.MAGIC_PROTECTION_STRENGTH_KEY, Config.MAGIC_PROTECTION_STRENGTH);
        currentValues.put(Config.FEATHER_FALLING_STRENGTH_KEY, Config.FEATHER_FALLING_STRENGTH);

        // Loot Enchantment
        currentValues.put(Config.FORTUNE_INITIAL_LIMIT_KEY, (double)Config.FORTUNE_INITIAL_LIMIT);
        currentValues.put(Config.FORTUNE_DIMINISHING_RETURNS_KEY, (double)Config.FORTUNE_DIMINISHING_RETURNS);
        currentValues.put(Config.FORTUNE_MINIMUM_INCREMENT_KEY, (double)Config.FORTUNE_MINIMUM_INCREMENT);
        currentValues.put(Config.LOOTING_INITIAL_LIMIT_KEY, (double)Config.LOOTING_INITIAL_LIMIT);
        currentValues.put(Config.LOOTING_DIMINISHING_RETURNS_KEY, (double)Config.LOOTING_DIMINISHING_RETURNS);
        currentValues.put(Config.LOOTING_MINIMUM_INCREMENT_KEY, (double)Config.LOOTING_MINIMUM_INCREMENT);

        // Unbreaking
        currentValues.put(Config.UNBREAKING_STRENGTH_KEY, Config.UNBREAKING_STRENGTH);
        currentValues.put(Config.UNBREAKING_ARMOR_PENALTY_FACTOR_KEY, Config.UNBREAKING_ARMOR_PENALTY_FACTOR);

        // XP
        currentValues.put(Config.XP_GROWTH_FACTOR_KEY, Config.XP_GROWTH_FACTOR);
        currentValues.put(Config.XP_LEVEL_BRACKET_SIZE_KEY, Config.XP_LEVEL_BRACKET_SIZE);
        currentValues.put(Config.XP_GROWTH_Y_OFFSET_KEY, Config.XP_GROWTH_Y_OFFSET);
        currentValues.put(Config.XP_MAX_LEVEL_KEY, Config.XP_MAX_LEVEL);

        // Loot
        currentValues.put(Config.EPIC_LOOT_CHANCE_KEY, (double)Config.EPIC_LOOT_CHANCE);
        currentValues.put(Config.RARE_LOOT_CHANCE_KEY, (double)Config.RARE_LOOT_CHANCE);
        currentValues.put(Config.UNCOMMON_LOOT_CHANCE_KEY, (double)Config.UNCOMMON_LOOT_CHANCE);
        currentValues.put(Config.COMMON_LOOT_CHANCE_KEY, (double)Config.COMMON_LOOT_CHANCE);

        // Accessibility
        currentValues.put(Config.USE_PLAIN_BACKGROUND_KEY, Config.USE_PLAIN_BACKGROUND);
        currentValues.put(Config.OBFUSCATE_NEW_ENCHANTMENTS_KEY, Config.OBFUSCATE_NEW_ENCHANTMENTS);

        currentValues.put(Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_KEY, Config.OVERRIDE_ENCHANTMENT_NAME_COLORING);
        currentValues.put(Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_KEY, Config.OVERRIDE_ENCHANTMENT_NAME_COLOR);

        currentValues.put(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_KEY, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING);
        currentValues.put(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_KEY, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR);

        currentValues.put(Config.SHOW_ENCHANTMENT_DESCRIPTIONS_KEY, Config.SHOW_ENCHANTMENT_DESCRIPTIONS);
        currentValues.put(Config.ENCHANTMENT_DESCRIPTION_COLOR_KEY, Config.ENCHANTMENT_DESCRIPTION_COLOR);


        // 2. Ask the provider to generate the file content using these values
        String content = configs.generate(currentValues);


        // 3. Write to disk
        try {
            Path path = FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID + ".properties");
            Files.writeString(path, content);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save config", e);
        }
    }

    /**
     * Initializes the configuration system for Fabric.
     * <p>
     * Creates the configuration structure, loads the config file from disk (or generates defaults),
     * and assigns the initial values to the {@link Config} class.
     */
    public static void initialize() {
        // Use the Fabric logger and common MOD_ID
        Constants.LOG.info("Initializing config...");
        configs = new ConfigProvider();
        createConfigs();
        CONFIG = SimpleConfig.of(Constants.MOD_ID).provider(configs).request();
        assignConfigs();
    }
}