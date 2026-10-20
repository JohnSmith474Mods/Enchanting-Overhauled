package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.Optional;
import java.util.function.Function;

public class FrostWalker {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
            2,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(10, 10),
            Enchantment.dynamicCost(25, 10),
            4,
            EquipmentSlotGroup.FEET
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(FrostWalker.DEFINITION.apply(context))
                    .exclusiveWith(context.lookup(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.BOOTS_EXCLUSIVE))
                    .withEffect(
                            EnchantmentEffectComponents.DAMAGE_IMMUNITY,
                            DamageImmunity.INSTANCE,
                            DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.BURN_FROM_STEPPING)).tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY)))
                    )
                    .withEffect(
                            EnchantmentEffectComponents.LOCATION_CHANGED,
                            new ReplaceDisk(
                                    new PointerValue("frost_walker.location_changed", new LevelBasedValue.Clamped(new DiminishingReturnsValue(3.0F, 1.0F, 1.0F), 0.0F, 16.0F)),
                                    LevelBasedValue.constant(1.0F),
                                    new Vec3i(0, -1, 0),
                                    Optional.of(BlockPredicate.allOf(
                                            BlockPredicate.matchesTag(new Vec3i(0, 1, 0), BlockTags.AIR),
                                            BlockPredicate.matchesBlocks(Blocks.WATER),
                                            BlockPredicate.matchesFluids(Fluids.WATER),
                                            BlockPredicate.unobstructed()
                                    )),
                                    BlockStateProvider.simple(Blocks.FROSTED_ICE),
                                    Optional.of(GameEvent.BLOCK_PLACE)
                            ),
                            LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(true)))
                    )
                    .build(AdaptedEnchantments.FROST_WALKER.location());
}