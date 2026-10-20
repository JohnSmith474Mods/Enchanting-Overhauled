package johnsmith.enchantingoverhauled.api.damagesource;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

/**
 * Registry key holder for custom {@link DamageType}s introduced by Enchanting Overhauled.
 * <p>
 * This class holds the static {@link ResourceKey} references used to resolve damage types
 * from the dynamic {@link Registries#DAMAGE_TYPE} registry when constructing a
 * {@link net.minecraft.world.damagesource.DamageSource}.
 */
public class DamageTypes {

    /**
     * Damage type dealt when a player triggers an explosion by using an invalid book catalyst
     * on a Deactivated Enchanting Altar or by failing a lethal game of Scribe.
     * <p>
     * Corresponds to {@code enchanting_overhauled:arcane_retribution}.
     */
    public static final ResourceKey<DamageType> ARCANE_RETRIBUTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "arcane_retribution")
    );

    /**
     * Damage type dealt by {@link johnsmith.enchantingoverhauled.entity.projectile.MagicMissileEntity}
     * projectiles fired by flying arcane book entities.
     * <p>
     * Corresponds to {@code enchanting_overhauled:magic_missile}.
     */
    public static final ResourceKey<DamageType> MAGIC_MISSILE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "magic_missile")
    );

    /**
     * Triggers static class initialization to ensure all {@link ResourceKey} constants are created.
     */
    public static void initialize() {}
}