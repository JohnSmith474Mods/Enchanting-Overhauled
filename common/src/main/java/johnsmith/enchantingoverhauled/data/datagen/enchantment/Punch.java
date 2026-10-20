package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.function.Function;

public class Punch {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(Tags.ALL_RANGED),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(12, 20),
            Enchantment.dynamicCost(37, 20),
            4,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Punch.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.KNOCKBACK,
                            new AddValue(new PointerValue("punch.knockback", new DiminishingReturnsValue(1.5F, 0.5F, 0.5F))),
                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS))
                    )
                    .build(AdaptedEnchantments.PUNCH.location());
}