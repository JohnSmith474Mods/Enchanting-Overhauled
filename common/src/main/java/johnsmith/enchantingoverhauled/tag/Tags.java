package johnsmith.enchantingoverhauled.tag;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class Tags {
    public static final TagKey<Item> ENCHANTING_FUEL = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanting_fuel")
    );
}