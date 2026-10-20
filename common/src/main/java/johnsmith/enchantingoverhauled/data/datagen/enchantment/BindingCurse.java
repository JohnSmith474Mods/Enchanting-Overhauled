package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.enchantment.effect.BindingCurseEffect;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponents;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Function;

public class BindingCurse {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.EQUIPPABLE_ENCHANTABLE),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.constantCost(25),
            Enchantment.constantCost(50),
            8,
            EquipmentSlotGroup.ARMOR
    );

    @SuppressWarnings("unchecked")
    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(BindingCurse.DEFINITION.apply(context))
                    .withEffect(net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
                    .withSpecialEffect((DataComponentType<BindingCurseEffect>) EnchantmentEffectComponents.BINDING_CHANCE.get(),
                            new BindingCurseEffect(new PointerValue("binding_curse.binding_chance", new DiminishingReturnsValue(.4F, .1F, 0.0F)))
                    ).build(AdaptedEnchantments.BINDING_CURSE.location());
}