package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class AdaptedEnchantments {
    public static final ResourceKey<Enchantment> AQUA_AFFINITY = key("aqua_affinity");
    public static final ResourceKey<Enchantment> BANE_OF_ARTHROPODS = key("bane_of_arthropods");
    public static final ResourceKey<Enchantment> BINDING_CURSE = key("binding_curse");
    public static final ResourceKey<Enchantment> BLAST_PROTECTION = key("blast_protection");
    public static final ResourceKey<Enchantment> BREACH = key("breach");
    public static final ResourceKey<Enchantment> CHANNELING = key("channeling");
    public static final ResourceKey<Enchantment> DENSITY = key("density");
    public static final ResourceKey<Enchantment> DEPTH_STRIDER = key("depth_strider");
    public static final ResourceKey<Enchantment> EFFICIENCY = key("efficiency");
    public static final ResourceKey<Enchantment> FEATHER_FALLING = key("feather_falling");
    public static final ResourceKey<Enchantment> FIRE_ASPECT = key("fire_aspect");
    public static final ResourceKey<Enchantment> FIRE_PROTECTION = key("fire_protection");
    public static final ResourceKey<Enchantment> FLAME = key("flame");
    public static final ResourceKey<Enchantment> FORTUNE = key("fortune");
    public static final ResourceKey<Enchantment> FROST_WALKER = key("frost_walker");
    public static final ResourceKey<Enchantment> IMPALING = key("impaling");
    public static final ResourceKey<Enchantment> INFINITY = key("infinity");
    public static final ResourceKey<Enchantment> KNOCKBACK = key("knockback");
    public static final ResourceKey<Enchantment> LOOTING = key("looting");
    public static final ResourceKey<Enchantment> LOYALTY = key("loyalty");
    public static final ResourceKey<Enchantment> LUCK_OF_THE_SEA = key("luck_of_the_sea");
    public static final ResourceKey<Enchantment> LURE = key("lure");
    public static final ResourceKey<Enchantment> MENDING = key("mending");
    public static final ResourceKey<Enchantment> MULTISHOT = key("multishot");
    public static final ResourceKey<Enchantment> PIERCING = key("piercing");
    public static final ResourceKey<Enchantment> POWER = key("power");
    public static final ResourceKey<Enchantment> PROJECTILE_PROTECTION = key("projectile_protection");
    public static final ResourceKey<Enchantment> PROTECTION = key("protection");
    public static final ResourceKey<Enchantment> PUNCH = key("punch");
    public static final ResourceKey<Enchantment> QUICK_CHARGE = key("quick_charge");
    public static final ResourceKey<Enchantment> RESPIRATION = key("respiration");
    public static final ResourceKey<Enchantment> RIPTIDE = key("riptide");
    public static final ResourceKey<Enchantment> SHARPNESS = key("sharpness");
    public static final ResourceKey<Enchantment> SILK_TOUCH = key("silk_touch");
    public static final ResourceKey<Enchantment> SMITE = key("smite");
    public static final ResourceKey<Enchantment> SOUL_SPEED = key("soul_speed");
    public static final ResourceKey<Enchantment> SWEEPING_EDGE = key("sweeping_edge");
    public static final ResourceKey<Enchantment> SWIFT_SNEAK = key("swift_sneak");
    public static final ResourceKey<Enchantment> THORNS = key("thorns");
    public static final ResourceKey<Enchantment> UNBREAKING = key("unbreaking");
    public static final ResourceKey<Enchantment> VANISHING_CURSE = key("vanishing_curse");
    public static final ResourceKey<Enchantment> WIND_BURST = key("wind_burst");

    static final int ENCHANTMENT_MAX_LEVEL = 3;

    private static ResourceKey<Enchantment> key(String key) {
        return ResourceKey.create(
                Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key)
        );
    }
}
