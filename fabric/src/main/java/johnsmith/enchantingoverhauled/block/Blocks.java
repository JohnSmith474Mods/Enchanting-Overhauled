package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Registration handler for blocks on the Fabric platform.
 * <p>
 * Defines the block properties and registers the instances with the vanilla registry.
 */
public class Blocks {

    /** Properties for the standard Deactivated Enchanting Table (no light). */
    public static final BlockBehaviour.Properties DEACTIVATED_PROPS = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 1200.0F)
            .lightLevel((state) -> 0)
            .sound(SoundType.STONE)
            .noOcclusion();

    /** Properties for the Disturbed Enchanting Table (emits light). */
    public static final BlockBehaviour.Properties DISTURBED_PROPS = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 1200.0F)
            .lightLevel((state) -> 7)
            .sound(SoundType.STONE)
            .noOcclusion();

    public static final Block DEACTIVATED_ENCHANTING_TABLE = new DeactivatedEnchantingTableBlock(DEACTIVATED_PROPS);
    public static final Block DISTURBED_ENCHANTING_TABLE = new DeactivatedEnchantingTableBlock(DISTURBED_PROPS);

    /**
     * Registers the blocks. Called during mod initialization.
     */
    public static void initialize() {
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Constants.MOD_ID, "deactivated_enchanting_table"), DEACTIVATED_ENCHANTING_TABLE);
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(Constants.MOD_ID, "disturbed_enchanting_table"), DISTURBED_ENCHANTING_TABLE);
    }
}