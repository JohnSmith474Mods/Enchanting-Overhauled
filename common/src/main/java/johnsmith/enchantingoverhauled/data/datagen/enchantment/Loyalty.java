package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.function.Function;

public class Loyalty {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(12, 7),
            Enchantment.constantCost(50),
            2,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Loyalty.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, new AddValue(new PointerValue("loyalty.trident_return_acceleration", new DiminishingReturnsValue(2.0F, 0.5F, 1.0F))))
                    .build(AdaptedEnchantments.LOYALTY.location());
}