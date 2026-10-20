package johnsmith.enchantingoverhauled.structure;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.structure.UpdatingJigsawStructure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public final class ForgeStructureTypes {
    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        STRUCTURE_TYPES.register("updating_jigsaw", () -> UpdatingJigsawStructure.TYPE);
        STRUCTURE_TYPES.register(eventBus);
    }

    private ForgeStructureTypes() {}
}