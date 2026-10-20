package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.DamageEntity;
import net.minecraft.world.item.enchantment.effects.DamageItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;

import java.util.function.Function;

public class Thorns {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 20),
            Enchantment.dynamicCost(60, 20),
            8,
            EquipmentSlotGroup.ARMOR
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Thorns.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.POST_ATTACK,
                            EnchantmentTarget.VICTIM,
                            EnchantmentTarget.ATTACKER,
                            AllOf.entityEffects(
                                    new DamageEntity(new PointerValue("thorns.post_attack.damage_entity.min_damage", LevelBasedValue.constant(1.0F)),
                                                     new PointerValue("thorns.post_attack.damage_entity.max_damage", LevelBasedValue.constant(5.0F)),
                                                     context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.THORNS)),
                                    new DamageItem(new PointerValue("thorns.post_attack.damage_item", LevelBasedValue.constant(2.0F)))
                            ),
                            LootItemRandomChanceCondition.randomChance(EnchantmentLevelProvider.forEnchantmentLevel(new PointerValue("thorns.random_chance", new DiminishingReturnsValue(0.20F, 0.05F, 0.00F))))
                    )
                    .build(AdaptedEnchantments.THORNS.location());
}