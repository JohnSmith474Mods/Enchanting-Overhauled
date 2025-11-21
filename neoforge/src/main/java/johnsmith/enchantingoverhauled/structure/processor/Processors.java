package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registration handler for structure processors on the NeoForge platform.
 * <p>
 * Uses {@link DeferredRegister} to handle the registration of custom
 * {@link StructureProcessorType}s safely on the mod event bus.
 */
public class Processors {

    private static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Constants.MOD_ID);

    /**
     * The processor type for aging blocks (mossy/cracked variants).
     */
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BlockAgeProcessor>> BLOCK_AGE_PROCESSOR =
            PROCESSORS.register("block_age", () -> () -> BlockAgeProcessor.CODEC);

    /**
     * The processor type for converting materials to desert variants (sandstone).
     */
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<DesertifyProcessor>> DESERT_PROCESSOR =
            PROCESSORS.register("desertify", () -> () -> DesertifyProcessor.CODEC);

    /**
     * The processor type for adding overgrowth (leaves) above blocks.
     */
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<OvergrowthProcessor>> OVERGROWTH_PROCESSOR =
            PROCESSORS.register("overgrowth", () -> () -> OvergrowthProcessor.CODEC);

    /**
     * The processor type for attaching vines to blocks.
     */
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<VinesProcessor>> VINES_PROCESSOR =
            PROCESSORS.register("add_vines", () -> () -> VinesProcessor.CODEC);

    /**
     * Initializes the processor registry.
     *
     * @param eventBus The mod event bus to attach the register to.
     */
    public static void initialize(IEventBus eventBus) {
        PROCESSORS.register(eventBus);
    }
}