package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.MultiplyValue;

import java.util.function.Function;

public class Mending {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(25, 25),
            Enchantment.dynamicCost(75, 25),
            4,
            EquipmentSlotGroup.ANY
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Mending.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.REPAIR_WITH_XP, new MultiplyValue(new PointerValue("mending.repair_with_xp", new DiminishingReturnsValue(4.5F, 1.5F, 1.5F))))
                    .build(AdaptedEnchantments.MENDING.location());
}