package johnsmith.structure.processor;

import com.mojang.serialization.MapCodec;

import johnsmith.EnchantingOverhauled;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;

public class ProcessorRegistry {
    // Static processor declarations.
    public static StructureProcessorType<BlockAgeProcessor> BLOCK_AGE_PROCESSOR = register(BlockAgeProcessor.CODEC,
            "block_age");
    public static StructureProcessorType<OvergrowthProcessor> OVERGROWTH_PROCESSOR = register(
            OvergrowthProcessor.CODEC, "overgrowth");
    public static StructureProcessorType<VinesProcessor> VINES_PROCESSOR = register(VinesProcessor.CODEC,
            "add_vines");
    public static StructureProcessorType<DesertifyProcessor> DESERT_PROCESSOR = register(DesertifyProcessor.CODEC,
            "desertify");

    static <P extends StructureProcessor> StructureProcessorType<P> register(MapCodec<P> codec, String id) {
        final Identifier processorKey = new Identifier(EnchantingOverhauled.MOD_ID, id);
        return Registry.register(Registries.STRUCTURE_PROCESSOR, processorKey, () -> codec);
    };

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing structure processors...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}