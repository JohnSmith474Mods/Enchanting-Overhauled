package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration handler for blocks on the NeoForge platform.
 * <p>
 * Uses {@link DeferredRegister} to handle registry events safely.
 */
public class Blocks {
    public static final ResourceKey<Block> DEACTIVATED_TABLE_KEY = ResourceKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "deactivated_enchanting_table")
    );

    public static final ResourceKey<Block> DISTURBED_TABLE_KEY = ResourceKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "disturbed_enchanting_table")
    );

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, Constants.MOD_ID);

    /** Properties for the standard Deactivated Enchanting Table (no light). */
    public static final BlockBehaviour.Properties DEACTIVATED_PROPS = BlockBehaviour.Properties.of()
            .setId(DEACTIVATED_TABLE_KEY)
            .mapColor(MapColor.COLOR_BLACK)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 1200.0F)
            .lightLevel((state) -> 0)
            .sound(SoundType.STONE)
            .noOcclusion();

    /** Properties for the Disturbed Enchanting Table (emits light). */
    public static final BlockBehaviour.Properties DISTURBED_PROPS = BlockBehaviour.Properties.of()
            .setId(DISTURBED_TABLE_KEY)
            .mapColor(MapColor.COLOR_BLACK)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 1200.0F)
            .lightLevel((state) -> 7) // Disturbed emits light
            .sound(SoundType.STONE)
            .noOcclusion();

    public static final DeferredHolder<Block, DeactivatedEnchantingTableBlock> DEACTIVATED_ENCHANTING_TABLE =
            BLOCKS.register("deactivated_enchanting_table", () -> new DeactivatedEnchantingTableBlock(DEACTIVATED_PROPS));

    public static final DeferredHolder<Block, DeactivatedEnchantingTableBlock> DISTURBED_ENCHANTING_TABLE =
            BLOCKS.register("disturbed_enchanting_table", () -> new DeactivatedEnchantingTableBlock(DISTURBED_PROPS));

    /**
     * Initializes the block register.
     *
     * @param eventBus The mod event bus.
     */
    public static void initialize(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}