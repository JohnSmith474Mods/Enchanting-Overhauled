package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class FabricAdvancements {
    public static void initialize() {
        for (Criteria criterion : Criteria.values()) {
            Registry.register(
                    BuiltInRegistries.TRIGGER_TYPES,
                    Identifier.fromNamespaceAndPath(Constants.MOD_ID, criterion.getSerializedName()),
                    criterion.build()
            );
        }
    }
}
