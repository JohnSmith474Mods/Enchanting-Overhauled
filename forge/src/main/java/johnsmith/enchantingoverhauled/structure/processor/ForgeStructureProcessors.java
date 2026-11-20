package johnsmith.enchantingoverhauled.structure.processor;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.structure.processor.StructureProcessors;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;

public final class ForgeStructureProcessors {
    private static final DeferredRegister<MapCodec<? extends StructureProcessor>> PROCESSORS =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Constants.MOD_ID);

    public static void initialize(BusGroup eventBus) {
        for (StructureProcessors processor : StructureProcessors.values()) {
            PROCESSORS.register(processor.getSerializedName(), processor::codec);
        }
        PROCESSORS.register(eventBus);
    }

    private ForgeStructureProcessors() {}
}