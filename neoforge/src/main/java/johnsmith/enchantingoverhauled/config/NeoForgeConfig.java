package johnsmith.enchantingoverhauled.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

/**
 * NeoForge implementation of the configuration system.
 * <p>
 * This class utilizes NeoForge's {@link ModConfigSpec} to define the structure of the
 * configuration file (toml). It maps the abstract configuration keys defined in {@link Config}
 * to concrete, persistent values managed by the loader.
 */
public class NeoForgeConfig {

    /**
     * The specification for the common configuration, defining the structure and default values.
     */
    public static final ModConfigSpec COMMON_SPEC;

    /**
     * The instance of the Common configuration holder, containing the actual config value objects.
     */
    public static final Common COMMON;

    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder()
                .configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    /**
     * Inner class holding the configuration values.
     * <p>
     * Defines the structure of the config file, organizing settings into categories
     * and applying bounds/comments to specific entries.
     */
    public static class Common {

        // --- Anvil Settings ---

        /** Config value for the maximum repair cost. */
        public final ModConfigSpec.IntValue ANVIL_MAX_ITEM_COST;
        /** Config value for the repair bonus percentage. */
        public final ModConfigSpec.IntValue ANVIL_REPAIR_BONUS;
        /** Config value for the anvil break chance. */
        public final ModConfigSpec.DoubleValue ANVIL_BREAK_CHANCE;

        // --- Enchantment Level Settings ---

        /** Config value for the maximum natural enchantment level. */
        public final ModConfigSpec.IntValue ENCHANTMENT_MAX_LEVEL;
        /** Config value for whether tomes can exceed natural limits. */
        public final ModConfigSpec.BooleanValue TOMES_HAVE_GREATER_ENCHANTMENTS;

        // --- Protection Enchantment Settings ---

        /** Config value for the protection cap. */
        public final ModConfigSpec.DoubleValue PROTECTION_CAP;
        /** Config value for the protection divisor. */
        public final ModConfigSpec.DoubleValue PROTECTION_DIVISOR;
        /** Config value for Physical Protection strength. */
        public final ModConfigSpec.IntValue PHYSICAL_PROTECTION_STRENGTH;
        /** Config value for Fire Protection strength. */
        public final ModConfigSpec.IntValue FIRE_PROTECTION_STRENGTH;
        /** Config value for Blast Protection strength. */
        public final ModConfigSpec.IntValue BLAST_PROTECTION_STRENGTH;
        /** Config value for Projectile Protection strength. */
        public final ModConfigSpec.IntValue PROJECTILE_PROTECTION_STRENGTH;
        /** Config value for Feather Falling strength. */
        public final ModConfigSpec.IntValue FEATHER_FALLING_STRENGTH;
        /** Config value for Magic Protection strength. */
        public final ModConfigSpec.IntValue MAGIC_PROTECTION_STRENGTH;

        // --- Damage Enchantment Settings ---

        /** Config value for Sharpness initial damage. */
        public final ModConfigSpec.DoubleValue SHARPNESS_INITIAL_DAMAGE;
        /** Config value for Sharpness diminishing returns. */
        public final ModConfigSpec.DoubleValue SHARPNESS_DIMINISHING_RETURNS;
        /** Config value for Sharpness minimum increment. */
        public final ModConfigSpec.DoubleValue SHARPNESS_MINIMUM_DAMAGE_INCREMENT;
        /** Config value for Smite multiplier. */
        public final ModConfigSpec.DoubleValue SMITE_MULTIPLIER;
        /** Config value for Bane of Arthropods multiplier. */
        public final ModConfigSpec.DoubleValue EXTERMINATION_MULTIPLIER;

        // --- Loot Enchantment Settings ---

        /** Config value for Fortune initial limit. */
        public final ModConfigSpec.DoubleValue FORTUNE_INITIAL_LIMIT;
        /** Config value for Fortune diminishing returns. */
        public final ModConfigSpec.DoubleValue FORTUNE_DIMINISHING_RETURNS;
        /** Config value for Fortune minimum increment. */
        public final ModConfigSpec.DoubleValue FORTUNE_MINIMUM_INCREMENT;
        /** Config value for Looting initial limit. */
        public final ModConfigSpec.DoubleValue LOOTING_INITIAL_LIMIT;
        /** Config value for Looting diminishing returns. */
        public final ModConfigSpec.DoubleValue LOOTING_DIMINISHING_RETURNS;
        /** Config value for Looting minimum increment. */
        public final ModConfigSpec.DoubleValue LOOTING_MINIMUM_INCREMENT;

        // --- Unbreaking Enchantment Settings ---

        /** Config value for Unbreaking strength. */
        public final ModConfigSpec.IntValue UNBREAKING_STRENGTH;
        /** Config value for Unbreaking armor penalty. */
        public final ModConfigSpec.DoubleValue UNBREAKING_ARMOR_PENALTY_FACTOR;

        // --- XP Growth Settings ---

        /** Config value for XP growth factor (slope). */
        public final ModConfigSpec.IntValue XP_GROWTH_FACTOR;
        /** Config value for XP bracket size. */
        public final ModConfigSpec.IntValue XP_LEVEL_BRACKET_SIZE;
        /** Config value for XP initial offset. */
        public final ModConfigSpec.IntValue XP_GROWTH_Y_OFFSET;
        /** Config value for maximum XP level. */
        public final ModConfigSpec.IntValue XP_MAX_LEVEL;

        // --- Loot Settings ---

        /** Config value for epic loot injection chance. */
        public final ModConfigSpec.DoubleValue EPIC_LOOT_CHANCE;
        /** Config value for rare loot injection chance. */
        public final ModConfigSpec.DoubleValue RARE_LOOT_CHANCE;
        /** Config value for uncommon loot injection chance. */
        public final ModConfigSpec.DoubleValue UNCOMMON_LOOT_CHANCE;
        /** Config value for common loot injection chance. */
        public final ModConfigSpec.DoubleValue COMMON_LOOT_CHANCE;

        // --- Accessibility Settings ---

        /** Config value for using plain button textures. */
        public final ModConfigSpec.BooleanValue USE_PLAIN_BACKGROUND;
        /** Config value for obfuscating new enchantments. */
        public final ModConfigSpec.BooleanValue OBFUSCATE_NEW_ENCHANTMENTS;

        /**
         * Constructs the configuration structure.
         *
         * @param builder The builder used to define the config nodes.
         */
        Common(ModConfigSpec.Builder builder) {
            // Anvil
            builder.comment("Anvil Settings").push("Anvil Settings");
            ANVIL_MAX_ITEM_COST = builder
                    .comment("The maximum repair item cost for an item.")
                    .defineInRange("ANVIL_MAX_ITEM_COST", Config.ANVIL_MAX_ITEM_COST_DEFAULT, Config.ANVIL_MAX_ITEM_COST_FLOOR, Config.ANVIL_MAX_ITEM_COST_CEILING);
            ANVIL_REPAIR_BONUS = builder
                    .comment("Durability bonus (as percentage) added when combining damaged items.", "Vanilla Minecraft uses 12%.")
                    .defineInRange("ANVIL_REPAIR_BONUS", Config.ANVIL_REPAIR_BONUS_DEFAULT, Config.ANVIL_REPAIR_BONUS_FLOOR, Config.ANVIL_REPAIR_BONUS_CEILING);
            ANVIL_BREAK_CHANCE = builder
                    .comment("Chance for the anvil to take damage on use. Vanilla is 12% (0.12).")
                    .defineInRange("ANVIL_BREAK_CHANCE", Config.ANVIL_BREAK_CHANCE_DEFAULT, Config.ANVIL_BREAK_CHANCE_FLOOR, Config.ANVIL_BREAK_CHANCE_CEILING);
            builder.pop();

            // Enchantment Levels
            builder.comment("Enchantment Level Settings").push("Enchantment Level Settings");
            ENCHANTMENT_MAX_LEVEL = builder
                    .comment("The maximum level an enchantment can be *naturally* obtained at (e.g., enchanting table).")
                    .defineInRange("ENCHANTMENT_MAX_LEVEL", Config.ENCHANTMENT_MAX_LEVEL_DEFAULT, Config.ENCHANTMENT_MAX_LEVEL_FLOOR, Config.ENCHANTMENT_MAX_LEVEL_CEILING);
            TOMES_HAVE_GREATER_ENCHANTMENTS = builder
                    .comment("Whether enchanted tomes can contain enchantments of a higher level than naturally possible.")
                    .define("TOMES_HAVE_GREATER_ENCHANTMENTS", Config.TOMES_HAVE_GREATER_ENCHANTMENTS_DEFAULT);
            builder.pop();

            // Protection Enchantment
            builder.comment("Protection Enchantment Settings").push("Protection Enchantment Settings");
            PROTECTION_CAP = builder
                    .comment("The maximum effective points you can get from enchantments.", "Vanilla default is 20.")
                    .defineInRange("PROTECTION_CAP", Config.PROTECTION_CAP_DEFAULT, Config.PROTECTION_CAP_FLOOR, Config.PROTECTION_CAP_CEILING);
            PROTECTION_DIVISOR = builder
                    .comment("The value used to calculate damage reduction from protection points. (Value / Divisor)", "This value *WILL* be higher than 'PROTECTION_CAP'.")
                    .defineInRange("PROTECTION_DIVISOR", Config.PROTECTION_DIVISOR_DEFAULT, Config.PROTECTION_DIVISOR_FLOOR, Config.PROTECTION_DIVISOR_CEILING);
            PHYSICAL_PROTECTION_STRENGTH = builder
                    .defineInRange("PHYSICAL_PROTECTION_STRENGTH", Config.PHYSICAL_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
            FIRE_PROTECTION_STRENGTH = builder
                    .defineInRange("FIRE_PROTECTION_STRENGTH", Config.FIRE_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
            BLAST_PROTECTION_STRENGTH = builder
                    .defineInRange("BLAST_PROTECTION_STRENGTH", Config.BLAST_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
            PROJECTILE_PROTECTION_STRENGTH = builder
                    .defineInRange("PROJECTILE_PROTECTION_STRENGTH", Config.PROJECTILE_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
            MAGIC_PROTECTION_STRENGTH = builder
                    .defineInRange("MAGIC_PROTECTION_STRENGTH", Config.MAGIC_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
            FEATHER_FALLING_STRENGTH = builder
                    .defineInRange("FEATHER_FALLING_STRENGTH", Config.FEATHER_FALLING_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING);
            builder.pop();

            // Damage Enchantment
            builder.comment("Damage Enchantment Settings").push("Damage Enchantment Settings");
            SHARPNESS_INITIAL_DAMAGE = builder
                    .comment("Initial damage bonus applied by Sharpness.")
                    .defineInRange("SHARPNESS_INITIAL_DAMAGE", Config.SHARPNESS_INITIAL_DAMAGE_DEFAULT, Config.SHARPNESS_INITIAL_DAMAGE_FLOOR, Config.SHARPNESS_INITIAL_DAMAGE_CEILING);
            SHARPNESS_DIMINISHING_RETURNS = builder
                    .comment("How much the damage bonus decreases per subsequent level.")
                    .defineInRange("SHARPNESS_DIMINISHING_RETURNS", Config.SHARPNESS_DIMINISHING_RETURNS_DEFAULT, Config.SHARPNESS_DIMINISHING_RETURNS_FLOOR, Config.SHARPNESS_DIMINISHING_RETURNS_CEILING);
            SHARPNESS_MINIMUM_DAMAGE_INCREMENT = builder
                    .comment("The minimum damage added per level.")
                    .defineInRange("SHARPNESS_MINIMUM_DAMAGE_INCREMENT", Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_DEFAULT, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_FLOOR, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_CEILING);
            SMITE_MULTIPLIER = builder
                    .comment("Multiplier applied to damage for Smite.")
                    .defineInRange("SMITE_MULTIPLIER", Config.DAMAGE_MULTIPLIER_DEFAULT, Config.DAMAGE_MULTIPLIER_FLOOR, Config.DAMAGE_MULTIPLIER_CEILING);
            EXTERMINATION_MULTIPLIER = builder
                    .comment("Multiplier applied to damage for Bane of Arthropods.")
                    .defineInRange("EXTERMINATION_MULTIPLIER", Config.DAMAGE_MULTIPLIER_DEFAULT, Config.DAMAGE_MULTIPLIER_FLOOR, Config.DAMAGE_MULTIPLIER_CEILING);
            builder.pop();

            // Loot Enchantment
            builder.comment("Loot Enchantment Settings").push("Loot Enchantment Settings");
            FORTUNE_INITIAL_LIMIT = builder.defineInRange("FORTUNE_INITIAL_LIMIT", Config.FORTUNE_INITIAL_LIMIT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING);
            FORTUNE_DIMINISHING_RETURNS = builder.defineInRange("FORTUNE_DIMINISHING_RETURNS", Config.FORTUNE_DIMINISHING_RETURNS_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING);
            FORTUNE_MINIMUM_INCREMENT = builder.defineInRange("FORTUNE_MINIMUM_INCREMENT", Config.FORTUNE_MINIMUM_INCREMENT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING);
            LOOTING_INITIAL_LIMIT = builder.defineInRange("LOOTING_INITIAL_LIMIT", Config.LOOTING_INITIAL_LIMIT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING);
            LOOTING_DIMINISHING_RETURNS = builder.defineInRange("LOOTING_DIMINISHING_RETURNS", Config.LOOTING_DIMINISHING_RETURNS_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING);
            LOOTING_MINIMUM_INCREMENT = builder.defineInRange("LOOTING_MINIMUM_INCREMENT", Config.LOOTING_MINIMUM_INCREMENT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING);
            builder.pop();

            // Unbreaking Enchantment
            builder.comment("Unbreaking Enchantment Settings").push("Unbreaking Enchantment Settings");
            UNBREAKING_STRENGTH = builder
                    .comment("A multiplier for Unbreaking's effectiveness.")
                    .defineInRange("UNBREAKING_STRENGTH", Config.UNBREAKING_STRENGTH_DEFAULT, Config.UNBREAKING_STRENGTH_FLOOR, Config.UNBREAKING_STRENGTH_CEILING);
            UNBREAKING_ARMOR_PENALTY_FACTOR = builder
                    .comment("The chance for unbreaking to not take effect on armor.")
                    .defineInRange("UNBREAKING_ARMOR_PENALTY_FACTOR", Config.UNBREAKING_ARMOR_PENALTY_FACTOR_DEFAULT, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_FLOOR, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_CEILING);
            builder.pop();

            // XP Growth
            builder.comment("XP Growth Settings").push("XP Growth Settings");
            XP_GROWTH_FACTOR = builder
                    .comment("The slope of the XP curve.")
                    .defineInRange("XP_GROWTH_FACTOR", Config.XP_GROWTH_FACTOR_DEFAULT, Config.XP_GROWTH_FACTOR_FLOOR, Config.XP_GROWTH_FACTOR_CEILING);
            XP_LEVEL_BRACKET_SIZE = builder
                    .comment("The bracket size for the XP growth. Values greater than 1 create a step function.")
                    .defineInRange("XP_LEVEL_BRACKET_SIZE", Config.XP_LEVEL_BRACKET_SIZE_DEFAULT, Config.XP_LEVEL_BRACKET_SIZE_FLOOR, Config.XP_LEVEL_BRACKET_SIZE_CEILING);
            XP_GROWTH_Y_OFFSET = builder
                    .comment("Initial XP requirement value (Y-offset).")
                    .defineInRange("XP_GROWTH_Y_OFFSET", Config.XP_GROWTH_Y_OFFSET_DEFAULT, Config.XP_GROWTH_Y_OFFSET_FLOOR, Config.XP_GROWTH_Y_OFFSET_CEILING);
            XP_MAX_LEVEL = builder
                    .comment("Maximum player level.")
                    .defineInRange("XP_MAX_LEVEL", Config.XP_MAX_LEVEL_DEFAULT, Config.XP_MAX_LEVEL_FLOOR, Config.XP_MAX_LEVEL_CEILING);
            builder.pop();


            builder.comment("Loot Settings").push("Loot Settings");
            EPIC_LOOT_CHANCE = builder
                    .comment("Chance for an Enchanted Tome to appear in epic loot chests.")
                    .defineInRange("EPIC_LOOT_CHANCE", Config.EPIC_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING);
            RARE_LOOT_CHANCE = builder
                    .comment("Chance for an Enchanted Tome to appear in rare loot chests.")
                    .defineInRange("RARE_LOOT_CHANCE", Config.RARE_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING);
            UNCOMMON_LOOT_CHANCE = builder
                    .comment("Chance for an Enchanted Tome to appear in uncommon loot chests.")
                    .defineInRange("UNCOMMON_LOOT_CHANCE", Config.UNCOMMON_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING);
            COMMON_LOOT_CHANCE = builder
                    .comment("Chance for an Enchanted Tome to appear in common loot chests.")
                    .defineInRange("COMMON_LOOT_CHANCE", Config.COMMON_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING);
            builder.pop();

            // Accessibility
            builder.comment("Accessibility Settings").push("Accessibility Settings");
            USE_PLAIN_BACKGROUND = builder
                    .comment("Whether the enchanting table buttons should be plain or textured.")
                    .define("USE_PLAIN_BACKGROUND", Config.USE_PLAIN_BACKGROUND_DEFAULT);
            OBFUSCATE_NEW_ENCHANTMENTS = builder
                    .comment("Whether the enchanting table should show new enchantments in the standard galactic alphabet.")
                    .define("OBFUSCATE_NEW_ENCHANTMENTS", Config.OBFUSCATE_NEW_ENCHANTMENTS_DEFAULT);
            builder.pop();
        }
    }

    /**
     * Synchronizes the configuration values from the loaded NeoForge spec to the
     * static fields in the {@link Config} class.
     * <p>
     * This must be called during the {@link net.neoforged.fml.event.config.ModConfigEvent.Loading}
     * or {@link net.neoforged.fml.event.config.ModConfigEvent.Reloading} events.
     */
    public static void loadConfig() {
        // Anvil
        Config.ANVIL_MAX_ITEM_COST = COMMON.ANVIL_MAX_ITEM_COST.get();
        Config.ANVIL_REPAIR_BONUS = COMMON.ANVIL_REPAIR_BONUS.get();
        Config.ANVIL_BREAK_CHANCE = COMMON.ANVIL_BREAK_CHANCE.get();

        // Enchantment Levels
        Config.ENCHANTMENT_MAX_LEVEL = COMMON.ENCHANTMENT_MAX_LEVEL.get();
        Config.TOMES_HAVE_GREATER_ENCHANTMENTS = COMMON.TOMES_HAVE_GREATER_ENCHANTMENTS.get();

        // Protection Enchantments
        Config.PROTECTION_CAP = COMMON.PROTECTION_CAP.get();
        double divisor = COMMON.PROTECTION_DIVISOR.get();
        if (divisor <= Config.PROTECTION_CAP) {
            divisor = Config.PROTECTION_CAP + 0.5D;
        }
        Config.PROTECTION_DIVISOR = divisor;
        Config.PHYSICAL_PROTECTION_STRENGTH = COMMON.PHYSICAL_PROTECTION_STRENGTH.get();
        Config.FIRE_PROTECTION_STRENGTH = COMMON.FIRE_PROTECTION_STRENGTH.get();
        Config.BLAST_PROTECTION_STRENGTH = COMMON.BLAST_PROTECTION_STRENGTH.get();
        Config.PROJECTILE_PROTECTION_STRENGTH = COMMON.PROJECTILE_PROTECTION_STRENGTH.get();
        Config.MAGIC_PROTECTION_STRENGTH = COMMON.MAGIC_PROTECTION_STRENGTH.get();
        Config.FEATHER_FALLING_STRENGTH = COMMON.FEATHER_FALLING_STRENGTH.get();

        // Damage Enchantment
        Config.SHARPNESS_INITIAL_DAMAGE = COMMON.SHARPNESS_INITIAL_DAMAGE.get().floatValue();
        Config.SHARPNESS_DIMINISHING_RETURNS = COMMON.SHARPNESS_DIMINISHING_RETURNS.get().floatValue();
        Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT = COMMON.SHARPNESS_MINIMUM_DAMAGE_INCREMENT.get().floatValue();
        Config.SMITE_MULTIPLIER = COMMON.SMITE_MULTIPLIER.get().floatValue();
        Config.EXTERMINATION_MULTIPLIER = COMMON.EXTERMINATION_MULTIPLIER.get().floatValue();

        // Loot Enchantment
        Config.FORTUNE_INITIAL_LIMIT = COMMON.FORTUNE_INITIAL_LIMIT.get().floatValue();
        Config.FORTUNE_DIMINISHING_RETURNS = COMMON.FORTUNE_DIMINISHING_RETURNS.get().floatValue();
        Config.FORTUNE_MINIMUM_INCREMENT = COMMON.FORTUNE_MINIMUM_INCREMENT.get().floatValue();
        Config.LOOTING_INITIAL_LIMIT = COMMON.LOOTING_INITIAL_LIMIT.get().floatValue();
        Config.LOOTING_DIMINISHING_RETURNS = COMMON.LOOTING_DIMINISHING_RETURNS.get().floatValue();
        Config.LOOTING_MINIMUM_INCREMENT = COMMON.LOOTING_MINIMUM_INCREMENT.get().floatValue();

        // Unbreaking Enchantment
        Config.UNBREAKING_STRENGTH = COMMON.UNBREAKING_STRENGTH.get();
        Config.UNBREAKING_ARMOR_PENALTY_FACTOR = COMMON.UNBREAKING_ARMOR_PENALTY_FACTOR.get();

        // XP Growth
        Config.XP_GROWTH_FACTOR = COMMON.XP_GROWTH_FACTOR.get();
        Config.XP_LEVEL_BRACKET_SIZE = COMMON.XP_LEVEL_BRACKET_SIZE.get();
        Config.XP_GROWTH_Y_OFFSET = COMMON.XP_GROWTH_Y_OFFSET.get();
        Config.XP_MAX_LEVEL = COMMON.XP_MAX_LEVEL.get();

        // Loot
        Config.EPIC_LOOT_CHANCE = COMMON.EPIC_LOOT_CHANCE.get().floatValue();
        Config.RARE_LOOT_CHANCE = COMMON.RARE_LOOT_CHANCE.get().floatValue();
        Config.UNCOMMON_LOOT_CHANCE = COMMON.UNCOMMON_LOOT_CHANCE.get().floatValue();
        Config.COMMON_LOOT_CHANCE = COMMON.COMMON_LOOT_CHANCE.get().floatValue();

        // Accessibility
        Config.USE_PLAIN_BACKGROUND = COMMON.USE_PLAIN_BACKGROUND.get();
        Config.OBFUSCATE_NEW_ENCHANTMENTS = COMMON.OBFUSCATE_NEW_ENCHANTMENTS.get();
    }

    /**
     * Pushes the values from the static fields in the {@link Config} class back into
     * the NeoForge configuration objects and persists them to disk.
     * <p>
     * This is typically called when settings are changed via the in-game configuration GUI.
     */
    public static void saveConfig() {
        // Anvil
        COMMON.ANVIL_MAX_ITEM_COST.set(Config.ANVIL_MAX_ITEM_COST);
        COMMON.ANVIL_REPAIR_BONUS.set(Config.ANVIL_REPAIR_BONUS);
        COMMON.ANVIL_BREAK_CHANCE.set(Config.ANVIL_BREAK_CHANCE);

        // Enchantment Levels
        COMMON.ENCHANTMENT_MAX_LEVEL.set(Config.ENCHANTMENT_MAX_LEVEL);
        COMMON.TOMES_HAVE_GREATER_ENCHANTMENTS.set(Config.TOMES_HAVE_GREATER_ENCHANTMENTS);

        // Protection Enchantments
        COMMON.PROTECTION_CAP.set(Config.PROTECTION_CAP);
        COMMON.PROTECTION_DIVISOR.set(Config.PROTECTION_DIVISOR);
        COMMON.PHYSICAL_PROTECTION_STRENGTH.set(Config.PHYSICAL_PROTECTION_STRENGTH);
        COMMON.FIRE_PROTECTION_STRENGTH.set(Config.FIRE_PROTECTION_STRENGTH);
        COMMON.BLAST_PROTECTION_STRENGTH.set(Config.BLAST_PROTECTION_STRENGTH);
        COMMON.PROJECTILE_PROTECTION_STRENGTH.set(Config.PROJECTILE_PROTECTION_STRENGTH);
        COMMON.MAGIC_PROTECTION_STRENGTH.set(Config.MAGIC_PROTECTION_STRENGTH);
        COMMON.FEATHER_FALLING_STRENGTH.set(Config.FEATHER_FALLING_STRENGTH);

        // Damage Enchantment (Float -> Double conversion)
        COMMON.SHARPNESS_INITIAL_DAMAGE.set(Config.SHARPNESS_INITIAL_DAMAGE.doubleValue());
        COMMON.SHARPNESS_DIMINISHING_RETURNS.set(Config.SHARPNESS_DIMINISHING_RETURNS.doubleValue());
        COMMON.SHARPNESS_MINIMUM_DAMAGE_INCREMENT.set(Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT.doubleValue());
        COMMON.SMITE_MULTIPLIER.set(Config.SMITE_MULTIPLIER.doubleValue());
        COMMON.EXTERMINATION_MULTIPLIER.set(Config.EXTERMINATION_MULTIPLIER.doubleValue());

        // Loot Enchantment
        COMMON.FORTUNE_INITIAL_LIMIT.set(Config.FORTUNE_INITIAL_LIMIT.doubleValue());
        COMMON.FORTUNE_DIMINISHING_RETURNS.set(Config.FORTUNE_DIMINISHING_RETURNS.doubleValue());
        COMMON.FORTUNE_MINIMUM_INCREMENT.set(Config.FORTUNE_MINIMUM_INCREMENT.doubleValue());
        COMMON.LOOTING_INITIAL_LIMIT.set(Config.LOOTING_INITIAL_LIMIT.doubleValue());
        COMMON.LOOTING_DIMINISHING_RETURNS.set(Config.LOOTING_DIMINISHING_RETURNS.doubleValue());
        COMMON.LOOTING_MINIMUM_INCREMENT.set(Config.LOOTING_MINIMUM_INCREMENT.doubleValue());

        // Unbreaking Enchantment
        COMMON.UNBREAKING_STRENGTH.set(Config.UNBREAKING_STRENGTH);
        COMMON.UNBREAKING_ARMOR_PENALTY_FACTOR.set(Config.UNBREAKING_ARMOR_PENALTY_FACTOR);

        // XP Growth
        COMMON.XP_GROWTH_FACTOR.set(Config.XP_GROWTH_FACTOR);
        COMMON.XP_LEVEL_BRACKET_SIZE.set(Config.XP_LEVEL_BRACKET_SIZE);
        COMMON.XP_GROWTH_Y_OFFSET.set(Config.XP_GROWTH_Y_OFFSET);
        COMMON.XP_MAX_LEVEL.set(Config.XP_MAX_LEVEL);

        // Loot
        COMMON.EPIC_LOOT_CHANCE.set(Config.EPIC_LOOT_CHANCE.doubleValue());
        COMMON.RARE_LOOT_CHANCE.set(Config.RARE_LOOT_CHANCE.doubleValue());
        COMMON.UNCOMMON_LOOT_CHANCE.set(Config.UNCOMMON_LOOT_CHANCE.doubleValue());
        COMMON.COMMON_LOOT_CHANCE.set(Config.COMMON_LOOT_CHANCE.doubleValue());

        // Accessibility
        COMMON.USE_PLAIN_BACKGROUND.set(Config.USE_PLAIN_BACKGROUND);
        COMMON.OBFUSCATE_NEW_ENCHANTMENTS.set(Config.OBFUSCATE_NEW_ENCHANTMENTS);

        // Force the spec to write the changes to the .toml file
        COMMON_SPEC.save();
    }
}