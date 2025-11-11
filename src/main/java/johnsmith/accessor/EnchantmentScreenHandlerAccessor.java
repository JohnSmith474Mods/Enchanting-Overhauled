package johnsmith.accessor;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface EnchantmentScreenHandlerAccessor {
    public abstract ItemStack getEnchantmentTarget();

    public abstract ItemStack getEnchantmentSource();

    public abstract int[] getEnchantmentSourceArray();

    public abstract int calculateEnchantmentCost(Enchantment enchantment);

    public int[] getTargetTextureIndices();

    public int[] getSourceTextureIndices();

    public int[] getTableTextureIndices();
}
