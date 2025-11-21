package johnsmith.enchantingoverhauled.damagesource;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

/**
 * Registry handler for custom {@link DamageType} tags.
 * <p>
 * These tags are used to categorize damage sources for the overhauled Protection enchantment logic.
 */
public class DamageTypeTagRegistry {

    /**
     * A tag identifying damage types considered "Physical".
     * <p>
     * This tag is used by the modified {@link net.minecraft.world.item.enchantment.ProtectionEnchantment}
     * (Type.ALL) to determine if it should reduce the incoming damage. It generally includes
     * melee attacks, falling blocks, and other non-elemental, non-magic sources.
     * <p>
     * JSON Location: {@code data/enchanting_overhauled/tags/damage_type/is_physical_damage.json}
     */
    public static final TagKey<DamageType> PHYSICAL_DAMAGE = TagKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(Constants.MOD_ID, "physical_damage")
    );

    /**
     * Initializes the tag registry class.
     * <p>
     * Calling this method ensures that the static fields are loaded and initialized
     * by the JVM before they are referenced.
     */
    public static void initialize() {
        Constants.LOG.info("Initializing damage type tag registry...");
    }
}