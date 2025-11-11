package johnsmith.structure.processor;

import johnsmith.EnchantingOverhauled;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.util.Identifier;

public class ProcessorLists {
    // Static processor list declarations.
    public static final RegistryKey<StructureProcessorList> DESERT_PROCESSOR_LIST = key(
            "library_ruins/desert_processor");
    public static final RegistryKey<StructureProcessorList> FOREST_PROCESSOR_LIST = key(
            "library_ruins/forest_processor");
    public static final RegistryKey<StructureProcessorList> JUNGLE_PROCESSOR_LIST = key(
            "library_ruins/jungle_processor");
    public static final RegistryKey<StructureProcessorList> STANDARD_PROCESSOR_LIST = key(
            "library_ruins/standard_processor");
    public static final RegistryKey<StructureProcessorList> SWAMP_PROCESSOR_LIST = key(
            "library_ruins/swamp_processor");

    // Wrapper method to create a Registry Key for the PROCESSOR_LIST registry.
    private static RegistryKey<StructureProcessorList> key(String path) {
        return RegistryKey.of(RegistryKeys.PROCESSOR_LIST, new Identifier(EnchantingOverhauled.MOD_ID, path));
    }

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing structure processor lists...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}
