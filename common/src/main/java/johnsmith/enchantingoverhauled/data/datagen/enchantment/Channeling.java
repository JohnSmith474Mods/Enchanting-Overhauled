package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantmentcore.api.enchantment.value.DiminishingReturnsValue;
import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.item.enchantment.effects.SummonEntityEffect;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.EnchantmentLevelProvider;

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
            new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
            new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Channeling.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM,
                            AllOf.entityEffects(
                                    new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
                                    new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
                            ),
                            AllOfCondition.allOf(
                                    LootItemRandomChanceCondition.randomChance(
                                            EnchantmentLevelProvider.forEnchantmentLevel(
                                                    new PointerValue("channeling.post_attack_chance", new DiminishingReturnsValue(0.45F, 0.15F, 0.1F))
                                            )
                                    ),
                                    WeatherCheck.weather().setThundering(true),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, net.minecraft.advancements.critereon.EntityPredicate.Builder.entity().located(net.minecraft.advancements.critereon.LocationPredicate.Builder.location().setCanSeeSky(true))),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, net.minecraft.advancements.critereon.EntityPredicate.Builder.entity().of(EntityType.TRIDENT))
                            )
                    )
                    .withEffect(EnchantmentEffectComponents.HIT_BLOCK,
                            AllOf.entityEffects(
                                    new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()), false),
                                    new PlaySoundEffect(SoundEvents.TRIDENT_THUNDER, ConstantFloat.of(5.0F), ConstantFloat.of(1.0F))
                            ),
                            AllOfCondition.allOf(
                                    LootItemRandomChanceCondition.randomChance(
                                            EnchantmentLevelProvider.forEnchantmentLevel(
                                                    new PointerValue("channeling.hit_block_chance", new DiminishingReturnsValue(0.45F, 0.15F, 0.1F))
                                            )
                                    ),
                                    WeatherCheck.weather().setThundering(true),
                                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, net.minecraft.advancements.critereon.EntityPredicate.Builder.entity().of(EntityType.TRIDENT)),
                                    LocationCheck.checkLocation(net.minecraft.advancements.critereon.LocationPredicate.Builder.location().setCanSeeSky(true)),
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.LIGHTNING_ROD)
                            )
                    )
                    .build(AdaptedEnchantments.CHANNELING.location());
}