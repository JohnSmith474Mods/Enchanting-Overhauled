package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum Blocks implements StringRepresentable {
    DEACTIVATED_ENCHANTING_TABLE(
            "deactivated_enchanting_table",
            DeactivatedEnchantingTableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 1200.0F)
                    .lightLevel(state -> 0)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    ),
    DISTURBED_ENCHANTING_TABLE(
            "disturbed_enchanting_table",
            DeactivatedEnchantingTableBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 1200.0F)
                    .lightLevel(state -> 7)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    private final String key;
    private final Function<BlockBehaviour.Properties, Block> factory;
    private final BlockBehaviour.Properties properties;
    private Block block;

    Blocks(String key, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        this.key = key;
        this.factory = factory;
        this.properties = properties;
    }

    public Block build() {
        if (this.block == null) this.block = this.factory.apply(
                properties.setId(
                        ResourceKey.create(
                                Registries.BLOCK,
                                Identifier.fromNamespaceAndPath(Constants.MOD_ID, key)
                        )
                )
        );
        return this.block;
    }

    @SuppressWarnings("unchecked")
    public <T extends Block> T get() {
        return (T) this.build();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.key;
    }
}