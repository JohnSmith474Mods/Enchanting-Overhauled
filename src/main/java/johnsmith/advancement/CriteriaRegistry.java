package johnsmith.advancement;

import johnsmith.EnchantingOverhauled;
import net.minecraft.advancement.criterion.Criteria;

public class CriteriaRegistry {
    // Static criteria trigger declarations.
    public static final ActivateAltarTrigger ACTIVATE_ALTAR = Criteria.register(
            ActivateAltarTrigger.ID.toString(),
            new ActivateAltarTrigger()
    );

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing criteria registry...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}