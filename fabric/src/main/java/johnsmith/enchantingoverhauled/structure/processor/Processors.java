package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

/**
 * Registration handler for structure processors on the Fabric platform.
 * <p>
 * Defines static fields for each custom processor type and registers them
 * directly with the vanilla {@link BuiltInRegistries#STRUCTURE_PROCESSOR} registry.
 */
public class Processors {

    /**
     * The processor type for aging blocks (mossy/cracked variants).
     */
    public static StructureProcessorType<BlockAgeProcessor> BLOCK_AGE_PROCESSOR;

    /**
     * The processor type for converting materials to desert variants (sandstone).
     */
    public static StructureProcessorType<DesertifyProcessor> DESERT_PROCESSOR;

    /**
     * The processor type for adding overgrowth (leaves) above blocks.
     */
    public static StructureProcessorType<OvergrowthProcessor> OVERGROWTH_PROCESSOR;

    /**
     * The processor type for attaching vines to blocks.
     */
    public static StructureProcessorType<VinesProcessor> VINES_PROCESSOR;

    /**
     * Helper method to register a processor type.
     *
     * @param id   The path for the resource location (namespace is the mod ID).
     * @param type The processor type to register.
     * @param <P>  The class of the structure processor.
     * @return The registered processor type.
     */
    private static <P extends StructureProcessor> StructureProcessorType<P> registerProcessor(String id, StructureProcessorType<P> type) {
        return Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id), type);
    }

    /**
     * Initializes and registers all structure processors.
     * <p>
     * Called during the mod's main initialization phase.
     */
    public static void initialize() {
        BLOCK_AGE_PROCESSOR = registerProcessor("block_age", () -> BlockAgeProcessor.CODEC);
        DESERT_PROCESSOR = registerProcessor("desertify", () -> DesertifyProcessor.CODEC);
        OVERGROWTH_PROCESSOR = registerProcessor("overgrowth", () -> OvergrowthProcessor.CODEC);
        VINES_PROCESSOR = registerProcessor("add_vines", () -> VinesProcessor.CODEC);
    }
}