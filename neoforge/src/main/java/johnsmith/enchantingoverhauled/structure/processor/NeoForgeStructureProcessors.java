package johnsmith.enchantingoverhauled.structure.processor;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.structure.processor.StructureProcessors;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeStructureProcessors {

    private static final DeferredRegister<StructureProcessorType<?>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (StructureProcessors processor : StructureProcessors.values()) {
            PROCESSORS.register(processor.getSerializedName(), processor::build);
        }
        PROCESSORS.register(eventBus);
    }

    private NeoForgeStructureProcessors() {}
}