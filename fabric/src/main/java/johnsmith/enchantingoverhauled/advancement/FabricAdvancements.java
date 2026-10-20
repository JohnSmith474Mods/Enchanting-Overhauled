package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.advancements.CriteriaTriggers;

public class FabricAdvancements {
    public static void initialize() {
        for (Criteria criterion : Criteria.values()) {
            CriteriaTriggers.register(Constants.MOD_ID + ":" + criterion.getSerializedName(), criterion.build());
        }
    }
}
