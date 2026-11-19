package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.Constants;

import johnsmith.enchantingoverhauled.api.structure.processor.StructureProcessors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class FabricStructureProcessors {
    public static void initialize() {
        for (StructureProcessors processorEnum : StructureProcessors.values()) {
            Registry.register(
                    BuiltInRegistries.STRUCTURE_PROCESSOR,
                    Identifier.fromNamespaceAndPath(Constants.MOD_ID, processorEnum.getSerializedName()),
                    processorEnum.build()
            );
        }
    }
}