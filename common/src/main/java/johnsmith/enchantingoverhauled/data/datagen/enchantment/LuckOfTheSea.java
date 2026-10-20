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

public class LuckOfTheSea {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.FISHING_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(15, 9),
            Enchantment.dynamicCost(65, 9),
            4,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(LuckOfTheSea.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.FISHING_LUCK_BONUS, new AddValue(new PointerValue("luck_of_the_sea.fishing_luck_bonus", new DiminishingReturnsValue(1.5F, 0.5F, 0.5F))))
                    .build(AdaptedEnchantments.LUCK_OF_THE_SEA.location());
}