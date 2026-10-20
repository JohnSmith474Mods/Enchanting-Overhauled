package johnsmith.enchantingoverhauled.mixin.item;

import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to vanilla {@link EnchantedBookItem} to apply dynamic
 * {@code "<Theme> <Item Name> of <School>"}
 * (and {@code "Aberrant"} fallback) naming based on its stored enchantments.
 */
@Mixin(EnchantedBookItem.class)
public abstract class EnchantedBookItemMixin extends Item {

    public EnchantedBookItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Component baseName = Component.translatable(this.getDescriptionId(stack));
        return ArcaneItemNamingUtil.formatItemName(stack, baseName);
    }
}