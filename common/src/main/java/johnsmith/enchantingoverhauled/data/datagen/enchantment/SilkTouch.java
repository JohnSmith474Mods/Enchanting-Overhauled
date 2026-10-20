package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponents;
import johnsmith.enchantingoverhauled.api.enchantment.effect.SilkTouchEffect;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.SetValue;

import java.util.function.Function;

public class SilkTouch {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.MINING_LOOT_ENCHANTABLE),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.constantCost(15),
            Enchantment.constantCost(65),
            8,
            EquipmentSlotGroup.MAINHAND
    );

    @SuppressWarnings("unchecked")
    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(SilkTouch.DEFINITION.apply(context))
                    .exclusiveWith(context.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.MINING_EXCLUSIVE))
                    .withSpecialEffect((DataComponentType<SilkTouchEffect>) EnchantmentEffectComponents.SILK_TOUCH.get(), new SilkTouchEffect(new PointerValue("silk_touch.chance", new DiminishingReturnsValue(.4F, .1F, 0.F))))
                    .withEffect(net.minecraft.world.item.enchantment.EnchantmentEffectComponents.BLOCK_EXPERIENCE, new SetValue(LevelBasedValue.constant(0.0F)))
                    .build(AdaptedEnchantments.SILK_TOUCH.location());
}