package johnsmith.entity.damage;

import johnsmith.EnchantingOverhauled;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class DamageTypeTagRegistry {
    // Static damage type tag declarations.
    public static final TagKey<DamageType> PHYSICAL_DAMAGE = TagKey.of(
            RegistryKeys.DAMAGE_TYPE,
            new Identifier(EnchantingOverhauled.MOD_ID, "physical_damage")
    );

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing damage type tag registry...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}
