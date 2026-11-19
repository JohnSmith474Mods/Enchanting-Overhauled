package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.ApplyEntityImpulse;
import net.minecraft.world.item.enchantment.effects.ApplyExhaustion;
import net.minecraft.world.item.enchantment.effects.ChangeItemDamage;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;

/**
 * Adapted datagen definition for Minecraft 1.21.11's {@code minecraft:lunge} spear enchantment.
 * <p>
 * Calibrates impulse and exhaustion using {@link DiminishingReturnsValue} curves so Level 3 matches
 * vanilla's default max level ({@code 1.374} impulse / {@code 12.0} exhaustion) while Level 4
 * (overleveled via tomes) yields a slight, controlled increase ({@code 1.603} / {@code 14.0}).
 */
public class Lunge {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.LUNGE_ENCHANTABLE),
            5,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(5, 8),
            Enchantment.dynamicCost(25, 8),
            2,
            EquipmentSlotGroup.HAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Lunge.DEFINITION.apply(context))
                    .withEffect(
                            EnchantmentEffectComponents.POST_PIERCING_ATTACK,
                            AllOf.entityEffects(
                                    new ChangeItemDamage(
                                            new PointerValue("lunge.post_piercing_attack.damage_item", LevelBasedValue.constant(1.0F))
                                    ),
                                    new ApplyExhaustion(
                                            new PointerValue("lunge.post_piercing_attack.exhaustion", new DiminishingReturnsValue(6.0F, 2.0F, 2.0F))
                                    ),
                                    new ApplyEntityImpulse(
                                            new Vec3(0.0, 0.0, 1.0),
                                            new Vec3(1.0, 0.0, 1.0),
                                            new PointerValue("lunge.post_piercing_attack.impulse", new DiminishingReturnsValue(0.687F, 0.229F, 0.229F))
                                    ),
                                    new PlaySoundEffect(
                                            List.of(SoundEvents.LUNGE_1, SoundEvents.LUNGE_2, SoundEvents.LUNGE_3),
                                            ConstantFloat.of(1.0F),
                                            ConstantFloat.of(1.0F)
                                    )
                            ),
                            AllOfCondition.allOf(
                                    InvertedLootItemCondition.invert(
                                            LootItemEntityPropertyCondition.hasProperties(
                                                    LootContext.EntityTarget.THIS,
                                                    EntityPredicate.Builder.entity().vehicle(EntityPredicate.Builder.entity())
                                            )
                                    ),
                                    LootItemEntityPropertyCondition.hasProperties(
                                            LootContext.EntityTarget.THIS,
                                            EntityPredicate.Builder.entity()
                                                    .flags(EntityFlagsPredicate.Builder.flags().setIsFallFlying(false))
                                    ),
                                    LootItemEntityPropertyCondition.hasProperties(
                                            LootContext.EntityTarget.THIS,
                                            EntityPredicate.Builder.entity()
                                                    .flags(EntityFlagsPredicate.Builder.flags().setIsInWater(false))
                                    )
                            )
                    )
                    .build(AdaptedEnchantments.LUNGE.identifier());
}
