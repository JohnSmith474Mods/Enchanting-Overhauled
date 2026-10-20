package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;
import johnsmith.enchantmentcore.api.enchantment.value.ProbabilisticValue;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.SetValue;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.function.Function;

public class Infinity {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(Tags.ALL_RANGED),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.constantCost(20),
            Enchantment.constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Infinity.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.AMMO_USE,
                            new SetValue(new ProbabilisticValue(new PointerValue("infinity.ammo_use", new LevelBasedValue.Fraction(new DiminishingReturnsValue(4.F, 1.F, 1.F), new LevelBasedValue.Constant(10))))),
                            MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.ALL_AMMUNITION)))
                    .build(AdaptedEnchantments.INFINITY.location());
}