package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.RemoveBinomial;

import java.util.function.Function;

public class Unbreaking {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(5, 8),
            Enchantment.dynamicCost(55, 8),
            2,
            EquipmentSlotGroup.ANY
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Unbreaking.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.ITEM_DAMAGE,
                            new RemoveBinomial(new PointerValue("unbreaking.item_damage", new DiminishingReturnsValue(0.4F, 0.125F, 0.0F))))
                    .build(AdaptedEnchantments.UNBREAKING.location());
}