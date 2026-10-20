package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Blocks block : Blocks.values()) {
            BLOCKS.register(block.getSerializedName(), block::build);
        }
        BLOCKS.register(eventBus);
    }

    private ForgeBlocks() {}
}