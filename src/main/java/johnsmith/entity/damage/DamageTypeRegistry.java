package johnsmith.entity.damage;

import johnsmith.EnchantingOverhauled;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class DamageTypeRegistry {
    // The RegistryKey for our new damage type
    public static final RegistryKey<DamageType> ARCANE_RETRIBUTION = RegistryKey.of(
            RegistryKeys.DAMAGE_TYPE,
            new Identifier(EnchantingOverhauled.MOD_ID, "arcane_retribution")
    );

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing damage type registry...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}