package johnsmith.enchantingoverhauled.data.datagen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class RegistryLookup {
    public static final Function<BootstrapContext<?>, HolderGetter<Item>> ITEM = context -> context.lookup(Registries.ITEM);
    public static final Function<BootstrapContext<?>, HolderGetter<Block>> BLOCK = context -> context.lookup(Registries.BLOCK);
    public static final Function<BootstrapContext<?>, HolderGetter<EntityType<?>>> ENTITY_TYPE = context -> context.lookup(Registries.ENTITY_TYPE);
}
