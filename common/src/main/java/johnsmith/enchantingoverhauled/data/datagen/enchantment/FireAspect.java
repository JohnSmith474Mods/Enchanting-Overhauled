package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.effects.Ignite;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

import java.util.function.Function;

public class FireAspect {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.FIRE_ASPECT_ENCHANTABLE),
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.SWORD_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 20),
            Enchantment.dynamicCost(60, 20),
            4,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(FireAspect.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.POST_ATTACK,
                            EnchantmentTarget.ATTACKER,
                            EnchantmentTarget.VICTIM,
                            new Ignite(new PointerValue("fire_aspect.post_attack.ignite",new DiminishingReturnsValue(5.F, 1.F, 2.F))),
                            DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().isDirect(true))
                    )
                    .build(AdaptedEnchantments.FIRE_ASPECT.location());
}