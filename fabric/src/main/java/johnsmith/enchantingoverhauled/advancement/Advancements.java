package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.advancements.CriteriaTriggers;

public class Advancements {
    public static void initialize() {
        CriteriaTriggers.register(Constants.MOD_ID + ":activate_altar", CriteriaRegistry.ACTIVATE_ALTAR);
    }
}
