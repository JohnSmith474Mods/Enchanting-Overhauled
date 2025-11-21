package johnsmith.enchantingoverhauled.enchantment;

import johnsmith.enchantingoverhauled.config.Config;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.jetbrains.annotations.NotNull;

/**
 * A custom enchantment that provides protection specifically against magical damage.
 * <p>
 * Unlike vanilla Protection (which covers everything) or Fire/Blast/Projectile protection,
 * this enchantment targets damage sources tagged as magic, indirect magic, or specific
 * magic-like projectiles (e.g., Dragon Breath, Sonic Boom).
 */
public class MagicProtectionEnchantment extends ProtectionEnchantment {

    /**
     * Constructs a new MagicProtectionEnchantment.
     *
     * @param definition The enchantment definition properties (weight, max level, etc.).
     */
    public MagicProtectionEnchantment(EnchantmentDefinition definition) {
        super(definition, ProtectionEnchantment.Type.ALL);
    }

    /**
     * Calculates the damage reduction provided by this enchantment against a specific source.
     * <p>
     * This implementation ignores the vanilla {@code Type.ALL} logic and instead checks
     * if the damage source is magical or magic-adjacent.
     *
     * @param level The level of the enchantment.
     * @param source The source of the incoming damage.
     * @return The amount of damage reduction to apply.
     */
    @Override
    public int getDamageProtection(int level, DamageSource source) {
        // Ignore damage that bypasses enchantments
        if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return 0;
        }

        // Standard magic damage types (Potions, Thorns, Wither, etc.)
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
            return level * Config.MAGIC_PROTECTION_STRENGTH;
        }

        // Entity-less projectiles (e.g., Shulker Bullets, Breeze Wind Charges)
        if (source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() == null) {
            return level * Config.MAGIC_PROTECTION_STRENGTH;
        }

        // Specific magic-like attacks
        if (source.is(DamageTypes.SONIC_BOOM) || source.is(DamageTypes.DRAGON_BREATH)) {
            return level * Config.MAGIC_PROTECTION_STRENGTH;
        }

        return 0;
    }

    /**
     * Determines if this enchantment is compatible with another enchantment.
     * <p>
     * Allows this enchantment to stack with other Protection types (Fire, Blast, Projectile, Feather Falling),
     * but NOT with the base vanilla Protection enchantment, as they conceptually overlap.
     *
     * @param other The other enchantment to check.
     * @return True if they are compatible, false otherwise.
     */
    @Override
    public boolean checkCompatibility(@NotNull Enchantment other) {
        return !(other instanceof ProtectionEnchantment);
    }
}