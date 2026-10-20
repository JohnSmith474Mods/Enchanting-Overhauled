package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricBlocks {
    public static void initialize() {
        for (Blocks block : Blocks.values()) {
            Registry.register(
                    BuiltInRegistries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, block.getSerializedName()),
                    block.build()
            );
        }
    }
}