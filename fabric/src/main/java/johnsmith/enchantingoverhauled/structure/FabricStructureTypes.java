package johnsmith.enchantingoverhauled.structure;

import johnsmith.enchantingoverhauled.Constants;

import johnsmith.enchantingoverhauled.api.structure.UpdatingJigsawStructure;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class FabricStructureTypes {
    public static void initialize() {
        Registry.register(
                BuiltInRegistries.STRUCTURE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "updating_jigsaw"),
                UpdatingJigsawStructure.TYPE
        );
    }
}
