package johnsmith.enchantment;

import johnsmith.config.Config;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.tag.DamageTypeTags;

public class MagicProtectionEnchantment extends ProtectionEnchantment {
    public MagicProtectionEnchantment(Properties properties) {
        super(properties, ProtectionEnchantment.Type.ALL);
    }

    /**
     * Overrides the protection logic to only protect against magic damage.
     */
    @Override
    public int getProtectionAmount(int level, DamageSource source) {
        // We ignore the vanilla logic completely and substitute our own.

        // DamageTypeTags.BYPASSES_INVULNERABILITY is now DamageTypeTags.BYPASSES_ENCHANTMENTS
        // You should also check for BYPASSES_EFFECTS
        if (source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return 0;
        }

        // This covers Potions, Thorns, Wither, etc.
        if (source.isOf(DamageTypes.MAGIC) || source.isOf(DamageTypes.INDIRECT_MAGIC)) {
            return level * Config.MAGIC_PROTECTION_STRENGTH;
        }

        // Optionally add other "magic-like" damage types:
        if (source.isIn(DamageTypeTags.IS_PROJECTILE) && source.getSource() == null) {
            // Projectiles with no entity source (e.g., Shulker Bullet, Breeze Wind Charge)
            return level * Config.MAGIC_PROTECTION_STRENGTH;
        }

        if (source.isOf(DamageTypes.SONIC_BOOM) || source.isOf(DamageTypes.DRAGON_BREATH)) {
            return level * Config.MAGIC_PROTECTION_STRENGTH;
        }

        // Return 0 for all other damage types (fire, fall, explosion, physical).
        return 0;
    }

    /**
     * Overrides canAccept to allow this enchantment to exist alongside
     * other ProtectionEnchantments (except vanilla Protection).
     */
    @Override
    public boolean canAccept(Enchantment other) {
        return !(other instanceof ProtectionEnchantment);
    }
}
