package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Blocks block : Blocks.values()) {
            BLOCKS.register(block.getSerializedName(), block::build);
        }
        BLOCKS.register(eventBus);
    }

    private NeoForgeBlocks() {}
}