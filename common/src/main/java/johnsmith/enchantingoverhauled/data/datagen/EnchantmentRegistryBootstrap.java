package johnsmith.enchantingoverhauled.data.datagen;

import johnsmith.enchantingoverhauled.data.datagen.enchantment.*;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentRegistryBootstrap {
    public static void bootstrap(BootstrapContext<Enchantment> context) {
        context.register(AdaptedEnchantments.AQUA_AFFINITY, AquaAffinity.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.BANE_OF_ARTHROPODS, BaneOfArthropods.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.BINDING_CURSE, BindingCurse.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.BLAST_PROTECTION, BlastProtection.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.BREACH, Breach.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.CHANNELING, Channeling.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.DENSITY, Density.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.DEPTH_STRIDER, DepthStrider.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.EFFICIENCY, Efficiency.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.FEATHER_FALLING, FeatherFalling.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.FIRE_ASPECT, FireAspect.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.FIRE_PROTECTION, FireProtection.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.FLAME, Flame.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.FORTUNE, Fortune.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.FROST_WALKER, FrostWalker.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.IMPALING, Impaling.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.INFINITY, Infinity.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.KNOCKBACK, Knockback.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.LOOTING, Looting.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.LOYALTY, Loyalty.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.LUCK_OF_THE_SEA, LuckOfTheSea.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.LURE, Lure.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.MENDING, Mending.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.MULTISHOT, Multishot.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.PIERCING, Piercing.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.POWER, Power.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.PROJECTILE_PROTECTION, ProjectileProtection.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.PROTECTION, Protection.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.PUNCH, Punch.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.QUICK_CHARGE, QuickCharge.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.RESPIRATION, Respiration.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.RIPTIDE, Riptide.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.SHARPNESS, Sharpness.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.SILK_TOUCH, SilkTouch.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.SMITE, Smite.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.SOUL_SPEED, SoulSpeed.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.SWEEPING_EDGE, SweepingEdge.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.SWIFT_SNEAK, SwiftSneak.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.THORNS, Thorns.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.UNBREAKING, Unbreaking.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.VANISHING_CURSE, VanishingCurse.ENCHANTMENT.apply(context));
        context.register(AdaptedEnchantments.WIND_BURST, WindBurst.ENCHANTMENT.apply(context));
    }
}