package johnsmith.config;

import com.mojang.datafixers.util.Pair;
import johnsmith.EnchantingOverhauled;

public class Config {
    public static SimpleConfig CONFIG;
    public static ConfigProvider configs;

    // Anvil
    public static Integer ANVIL_MAX_ITEM_COST;
    public static Integer ANVIL_REPAIR_BONUS;
    public static Double ANVIL_BREAK_CHANCE;

    // Enchantment Levels
    public static Integer ENCHANTMENT_MAX_LEVEL;

    // Protection Enchantment
    public static Double PROTECTION_DIVISOR;
    public static Double PROTECTION_CAP;
    public static Integer PHYSICAL_PROTECTION_STRENGTH;
    public static Integer FIRE_PROTECTION_STRENGTH;
    public static Integer BLAST_PROTECTION_STRENGTH;
    public static Integer PROJECTILE_PROTECTION_STRENGTH;
    public static Integer FEATHER_FALLING_STRENGTH;
    public static Integer MAGIC_PROTECTION_STRENGTH;

    // Unbreaking Enchantment
    public static Integer UNBREAKING_STRENGTH;
    public static Double UNBREAKING_ARMOR_PENALTY_FACTOR;

    // XP Growth
    public static Integer XP_GROWTH_FACTOR;
    public static Integer XP_LEVEL_BRACKET_SIZE;
    public static Integer XP_GROWTH_Y_OFFSET;
    public static Integer XP_MAX_LEVEL;

    // Accessibility
    public static Boolean USE_PLAIN_BACKGROUND;
    public static Boolean OBFUSCATE_NEW_ENCHANTMENTS;

    public static void createConfigs() {
        // Anvil
        configs.addSection("Anvil Settings");
        configs.addEntry(new Pair<>("anvil.max.item.repair.cost", 4),
                new String[]{"The maximum repair item cost for an item."},
                "Min: 1");
        configs.addEntry(new Pair<>("anvil.item.repair.bonus", 12),
                new String[]{"Durability bonus (as percentage) added when combining damaged items.",
                        "Vanilla Minecraft uses 12%."},
                "Min: 0, Max: 100");
        configs.addEntry(new Pair<>("anvil.break.chance", 0.12),
                new String[]{"Chance for the anvil to take damage on use. Vanilla is 12% (0.12)."},
                "Min: 0.0, Max: 1.0");

        // Enchantment Levels
        configs.addSection("Enchantment Level Settings");
        configs.addEntry(new Pair<>("enchantments.max.level", 3),
                new String[]{"The maximum level an enchantment can be *naturally* obtained at (e.g., enchanting table)."},
                "Min: 1, Max: 254");

        // Protection Enchantment
        configs.addSection("Protection Enchantment Settings");
        configs.addEntry(new Pair<>("protection.cap", 8.0),
                new String[]{"The maximum effective points you can get from enchantments.",
                        "Vanilla default is 20."},
                "Min: 1.0");
        configs.addEntry(new Pair<>("protection.divisor", 16.0),
                new String[]{"The value used to calculate damage reduction from protection points. (Value / Divisor)",
                        "This value *WILL* be higher than 'protection.cap'."},
                "Min: (protection.cap + 1.0)");
        configs.addEntry(new Pair<>("physical.protection.strength", 2),
                new String[]{"Enchantment protection factor for 'Physical Protection'."},
                "Min: 1");
        configs.addEntry(new Pair<>("fire.protection.strength", 2),
                new String[]{"Enchantment protection factor for 'Fire Protection'."},
                "Min: 1");
        configs.addEntry(new Pair<>("blast.protection.strength", 2),
                new String[]{"Enchantment protection factor for 'Blast Protection'."},
                "Min: 1");
        configs.addEntry(new Pair<>("projectile.protection.strength", 2),
                new String[]{"Enchantment protection factor for 'Projectile Protection'."},
                "Min: 1");
        configs.addEntry(new Pair<>("magic.protection.strength", 2),
                new String[]{"Enchantment protection factor for 'Magic Protection' (if implemented)."},
                "Min: 1");
        configs.addEntry(new Pair<>("feather.falling.strength", 3),
                new String[]{"Enchantment protection factor for 'Feather Falling'."},
                "Min: 1");

        // Unbreaking Enchantment
        configs.addSection("Unbreaking Enchantment Settings");
        configs.addEntry(new Pair<>("unbreaking.strength", 1),
                new String[]{"A multiplier for Unbreaking's effectiveness."},
                "Min: 1");
        configs.addEntry(new Pair<>("unbreaking.armor.penalty.factor", 0.0),
                new String[]{"The chance for unbreaking to not take effect on armor.",
                        "0.0 = no penalty, 1.0 = 100% penalty (no effect)."},
                "Min: 0.0, Max: 1.0");

        // XP Growth
        configs.addSection("XP Growth Settings");
        configs.addEntry(new Pair<>("xp.requirement.growth.factor", 1),
                new String[]{"XP requirement growth factor."},
                "Min: 1");
        configs.addEntry(new Pair<>("xp.requirement.growth.bracket.size", 2),
                new String[]{"Size of XP level brackets for growth calculation."},
                "Min: 1");
        configs.addEntry(new Pair<>("xp.requirement.growth.initial.value", 2),
                new String[]{"Initial XP requirement value (Y-offset)."},
                "Min: 1");
        configs.addEntry(new Pair<>("xp.max.level", 100),
                new String[]{"Maximum player level."},
                "Min: 3");

        // Accessibility
        configs.addSection("Accessibility Settings");
        configs.addEntry(new Pair<>("use.accessible.button.textures", false),
                new String[]{"Whether the enchanting table buttons should be plain or textured."}, "");
        configs.addEntry(new Pair<>("obfuscate.new.enchantments", true),
                new String[]{"Whether the enchanting table should show new enchantments in the standard galactic alphabet."}, "");
    }

    public static void assignConfigs() {
        // Anvil
        ANVIL_MAX_ITEM_COST = Math.clamp(CONFIG.getOrDefault("anvil.max.item.repair.cost", 4), 0, Integer.MAX_VALUE);
        ANVIL_REPAIR_BONUS = Math.clamp(CONFIG.getOrDefault("anvil.item.repair.bonus", 12), 0, 100);
        ANVIL_BREAK_CHANCE = Math.clamp(CONFIG.getOrDefault("anvil.break.chance", 0.12), 0.0, 1.0);

        // Enchantment Levels
        ENCHANTMENT_MAX_LEVEL = Math.clamp(CONFIG.getOrDefault("enchantments.max.level", 3), 1, 254);

        // Protection Enchantment
        PROTECTION_CAP = Math.clamp(CONFIG.getOrDefault("protection.cap", 8.0D), 1.0D, Double.MAX_VALUE);
        PROTECTION_DIVISOR = Math.clamp(CONFIG.getOrDefault("protection.divisor", 16.0D), PROTECTION_CAP + 1.0D, Double.MAX_VALUE);

        // Protection Enchantment
        PHYSICAL_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault("physical.protection.strength", 2), 1, Integer.MAX_VALUE);
        FIRE_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault("fire.protection.strength", 2), 1, Integer.MAX_VALUE);
        BLAST_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault("blast.protection.strength", 2), 1, Integer.MAX_VALUE);
        PROJECTILE_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault("projectile.protection.strength", 2), 1, Integer.MAX_VALUE);
        MAGIC_PROTECTION_STRENGTH = Math.clamp(CONFIG.getOrDefault("magic.protection.strength", 2), 1, Integer.MAX_VALUE);
        FEATHER_FALLING_STRENGTH = Math.clamp(CONFIG.getOrDefault("feather.falling.strength", 3), 1, Integer.MAX_VALUE);

        // Unbreaking Enchantment
        UNBREAKING_STRENGTH = Math.clamp(CONFIG.getOrDefault("unbreaking.strength", 1), 1, Integer.MAX_VALUE);
        UNBREAKING_ARMOR_PENALTY_FACTOR = Math.clamp(CONFIG.getOrDefault("unbreaking.armor.penalty.factor", 0.0D), 0.0D, 1.0);

        // XP Growth
        XP_GROWTH_FACTOR = Math.clamp(CONFIG.getOrDefault("xp.requirement.growth.factor", 1), 1, Integer.MAX_VALUE);
        XP_LEVEL_BRACKET_SIZE = Math.clamp(CONFIG.getOrDefault("xp.requirement.growth.bracket.size", 2), 1, Integer.MAX_VALUE);
        XP_GROWTH_Y_OFFSET = Math.clamp(CONFIG.getOrDefault("xp.requirement.growth.initial.value", 2), 1, Integer.MAX_VALUE);
        XP_MAX_LEVEL = Math.clamp(CONFIG.getOrDefault("xp.max.level", 100), 3, Integer.MAX_VALUE);

        // Accessibility
        USE_PLAIN_BACKGROUND = CONFIG.getOrDefault("use.accessible.button.textures", false);
        OBFUSCATE_NEW_ENCHANTMENTS = CONFIG.getOrDefault("obfuscate.new.enchantments", true);
    }

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing config...");
        configs = new ConfigProvider();
        createConfigs();
        CONFIG = SimpleConfig.of(EnchantingOverhauled.MOD_ID).provider(configs).request();
        assignConfigs();
    }
}