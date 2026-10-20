package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponents;
import johnsmith.enchantingoverhauled.api.enchantment.effect.VanishingCurseEffect;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Function;

public class VanishingCurse {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.constantCost(25),
            Enchantment.constantCost(50),
            8,
            EquipmentSlotGroup.ANY
    );

    @SuppressWarnings("unchecked")
    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(VanishingCurse.DEFINITION.apply(context))
                    .withEffect(net.minecraft.world.item.enchantment.EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                    .withSpecialEffect(
                            (DataComponentType<VanishingCurseEffect>) EnchantmentEffectComponents.VANISHING_CHANCE.get(),
                            new VanishingCurseEffect(new PointerValue("vanishing_curse.vanishing_chance", new DiminishingReturnsValue(.4F, .1F, 0.0F)))
                    ).build(AdaptedEnchantments.VANISHING_CURSE.location());
}