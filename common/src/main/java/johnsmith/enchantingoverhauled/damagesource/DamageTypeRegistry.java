package johnsmith.enchantingoverhauled.damagesource;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

/**
 * Registry handler for custom {@link DamageType}s introduced by Enchanting Overhauled.
 * <p>
 * This class holds the {@link ResourceKey} references used to retrieve damage types
 * from the dynamic registry when creating a {@link net.minecraft.world.damagesource.DamageSource}.
 */
public class DamageTypeRegistry {

    /**
     * The key for the "Arcane Retribution" damage type.
     * <p>
     * This damage is dealt when a player attempts to use a standard book or standard enchanted book
     * on a {@link johnsmith.enchantingoverhauled.block.DeactivatedEnchantingTableBlock}, causing it to explode.
     * <p>
     * Properties (defined in JSON):
     * <ul>
     * <li><b>Exhaustion:</b> 0.1</li>
     * <li><b>Scaling:</b> When caused by a living non-player entity.</li>
     * <li><b>Death Message:</b> "death.attack.arcane_retribution"</li>
     * </ul>
     */
    public static final ResourceKey<DamageType> ARCANE_RETRIBUTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(Constants.MOD_ID, "arcane_retribution")
    );

    /**
     * Initializes the registry class.
     * <p>
     * Calling this method ensures that the static fields are loaded and initialized
     * by the JVM before they are referenced by other parts of the mod.
     */
    public static void initialize() {
        Constants.LOG.info("Initializing damage type registry...");
    }
}