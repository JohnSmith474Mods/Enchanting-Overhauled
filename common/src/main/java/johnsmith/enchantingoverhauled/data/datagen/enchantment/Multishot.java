package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;
import johnsmith.enchantmentcore.api.enchantment.value.PolynomialValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;

import java.util.function.Function;

public class Multishot {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(Tags.ALL_RANGED),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.constantCost(20),
            Enchantment.constantCost(50),
            4,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Multishot.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT,
                            new AddValue(new PointerValue("multishot.projectile_count", new PolynomialValue(1.F, 1.F, 1.F, 0.F))),
                            LootItemRandomChanceCondition.randomChance(EnchantmentLevelProvider.forEnchantmentLevel(new PointerValue("multishot.projectile_count.random_chance", new DiminishingReturnsValue(0.45F, 0.15F, 0.1F)))))
                    .withEffect(EnchantmentEffectComponents.PROJECTILE_SPREAD, new AddValue(new PointerValue("multishot.projectile_spread", new PolynomialValue(-5.F, 1.F, 25.F, 0.0F))))
                    .build(AdaptedEnchantments.MULTISHOT.location());
}