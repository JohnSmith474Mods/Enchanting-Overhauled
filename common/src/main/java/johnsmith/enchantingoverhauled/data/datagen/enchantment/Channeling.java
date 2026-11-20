package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.data.datagen.RegistryLookup;
import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.item.enchantment.effects.SummonEntityEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;

import java.util.List;
import java.util.function.Function;

public class Channeling {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
            1,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.constantCost(25),
            Enchantment.constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
    );

    public static final AllOf.EntityEffects EFFECTS = AllOf.entityEffects(
            new SummonEntityEffect(HolderSet.direct(EntityTypes.LIGHTNING_BOLT.builtInRegistryHolder()), false),
            new PlaySoundEffect(List.of(SoundEvents.TRIDENT_THUNDER), ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Channeling.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM,
                            AllOf.entityEffects(
                                    new SummonEntityEffect(HolderSet.direct(EntityTypes.LIGHTNING_BOLT.builtInRegistryHolder()), false),
                                    new PlaySoundEffect(List.of(SoundEvents.TRIDENT_THUNDER), ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
                            ),
                            AllOfCondition.allOf(
                                    LootItemRandomChanceCondition.randomChance(
                                            EnchantmentLevelProvider.forEnchantmentLevel(
                                                    new PointerValue("channeling.post_attack_chance", new DiminishingReturnsValue(0.45F, 0.15F, 0.1F))
                                            )
                                    ),
                                    WeatherCheck.weather().setThundering(true),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setCanSeeSky(true))),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().of(RegistryLookup.ENTITY_TYPE.apply(context), EntityTypes.TRIDENT))
                            )
                    )
                    .withEffect(EnchantmentEffectComponents.HIT_BLOCK,
                            AllOf.entityEffects(
                                    new SummonEntityEffect(HolderSet.direct(EntityTypes.LIGHTNING_BOLT.builtInRegistryHolder()), false),
                                    new PlaySoundEffect(List.of(SoundEvents.TRIDENT_THUNDER), ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
                            ),
                            AllOfCondition.allOf(
                                    LootItemRandomChanceCondition.randomChance(
                                            EnchantmentLevelProvider.forEnchantmentLevel(
                                                    new PointerValue("channeling.hit_block_chance", new DiminishingReturnsValue(0.45F, 0.15F, 0.1F))
                                            )
                                    ),
                                    WeatherCheck.weather().setThundering(true),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(RegistryLookup.ENTITY_TYPE.apply(context), EntityTypes.TRIDENT)),
                                    LocationCheck.checkLocation(
                                            LocationPredicate.Builder.location()
                                                    .setCanSeeSky(true)
                                                    .setBlock(BlockPredicate.Builder.block().of(RegistryLookup.BLOCK.apply(context), BlockTags.LIGHTNING_RODS))
                                    )
                            )
                    )
                    .build(AdaptedEnchantments.CHANNELING.identifier());
}