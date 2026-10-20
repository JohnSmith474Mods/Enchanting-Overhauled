package johnsmith.enchantingoverhauled.data.datagen.spellfield;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellFields;
import johnsmith.enchantingoverhauled.api.loot.condition.CasterHostileCondition;

import johnsmith.enchantmentcore.api.enchantment.spellfield.math.DistanceMetric;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.FieldTopology;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.ConstantScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.function.LinearScalingFunction;
import johnsmith.enchantmentcore.api.enchantment.spellfield.shape.SpellFieldVolume;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DefaultSpellFields {
    public static final ResourceKey<AiSpellField> OBLITERATION = spellKey("detonate");
    public static final ResourceKey<AiSpellField> ABJURATION = spellKey("deflect_projectiles");
    public static final ResourceKey<AiSpellField> PERPETUATION = spellKey("heal");
    public static final ResourceKey<AiSpellField> COERCION = spellKey("hold");
    public static final ResourceKey<AiSpellField> DECEPTION = spellKey("illusory_copy");
    public static final ResourceKey<AiSpellField> DIVINATION = spellKey("forecast_lightning");
    public static final ResourceKey<AiSpellField> TRANSLATION = spellKey("gust");
    public static final ResourceKey<AiSpellField> CONJURATION = spellKey("icy_tomb");
    public static final ResourceKey<AiSpellField> TRANSMUTATION = spellKey("sinkhole");

    /**
     * Positive target filter ({@code true} = entity CAN be harmed by offensive spell effects):
     * <ul>
     *   <li>Entity is NOT in {@link Tags#ENCHANTED_TOME_ALLIES}, {@link Tags#ENCHANTED_TOME_NEUTRAL},
     *       or {@link Tags#ENCHANTED_TOME_AVOID_COLLATERAL}.</li>
     *   <li>AND entity either is NOT wearing enchanted armor OR the caster is actively hostile towards them.</li>
     * </ul>
     * Use this for {@code entity_filter} on {@code SpellFieldEntityEffect}s (e.g., {@code ApplyPotionEffect},
     * {@code AbsoluteStasisEffect}, {@code DamageEffect}, {@code ImpulseEffect}).
     */
    public static final Supplier<ContextAwarePredicate> CAN_HARM = () -> ContextAwarePredicate.create(
            canHarmCondition().build()
    );

    /**
     * Immunity filter ({@code true} = entity MUST NOT be harmed / is immune to explosion damage):
     * <ul>
     *   <li>Evaluates to {@code true} for Allies, Neutrals (e.g., Villagers), Avoid-Collateral mobs,
     *       and pacified players wearing enchanted armor.</li>
     *   <li>Evaluates to {@code false} for valid hostile targets (e.g., Zombies and provoked players).</li>
     * </ul>
     * Use this for {@code ExplosionDefinition}'s immunity predicate where {@code true} grants immunity.
     */
    public static final Supplier<ContextAwarePredicate> DO_NOT_HARM = () -> ContextAwarePredicate.create(
            InvertedLootItemCondition.invert(canHarmCondition()).build()
    );

    public static LootItemCondition.Builder canHarmCondition() {
        return AllOfCondition.allOf(
                noneOf(
                        Tags.ENCHANTED_TOME_ALLIES,
                        Tags.ENCHANTED_TOME_NEUTRAL,
                        Tags.ENCHANTED_TOME_AVOID_COLLATERAL
                ),
                AnyOfCondition.anyOf(
                        () -> new InvertedLootItemCondition(hasEnchantedArmor()),
                        CasterHostileCondition.builder()
                )
        );
    }

    @SafeVarargs
    public static LootItemCondition.Builder noneOf(TagKey<EntityType<?>>... tags) {
        LootItemCondition.Builder[] conditions = new LootItemCondition.Builder[tags.length];
        for (int i = 0; i < tags.length; i++) {
            conditions[i] = InvertedLootItemCondition.invert(
                    LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(tags[i])
                    )
            );
        }
        return AllOfCondition.allOf(conditions);
    }

    public static LootItemCondition hasEnchantedArmor() {
        ItemPredicate.Builder enchantedItem = ItemPredicate.Builder.item()
                .withSubPredicate(
                        ItemSubPredicates.ENCHANTMENTS,
                        ItemEnchantmentsPredicate.enchantments(
                                List.of(new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1)))
                        )
                );
        return AnyOfCondition.anyOf(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().head(enchantedItem))
                ),
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().chest(enchantedItem))
                ),
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().legs(enchantedItem))
                ),
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(enchantedItem))
                )
        ).build();
    }

    public static final Supplier<ContextAwarePredicate> NON_PLAYER_FILTER = () -> ContextAwarePredicate.create(
            InvertedLootItemCondition.invert(
                    LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(EntityType.PLAYER)
                    )
            ).build()
    );

    public static final Supplier<ContextAwarePredicate> PLAYER_ONLY_FILTER = () -> ContextAwarePredicate.create(
            LootItemEntityPropertyCondition.hasProperties(
                    LootContext.EntityTarget.THIS,
                    EntityPredicate.Builder.entity().of(EntityType.PLAYER)
            ).build()
    );

    public static HolderSet<MagicSchool> schoolSet(BootstrapContext<AiSpellField> context, ResourceKey<MagicSchool> schoolKey) {
        HolderGetter<MagicSchool> schools = context.lookup(MagicSchools.REGISTRY_KEY);
        return HolderSet.direct(schools.getOrThrow(schoolKey));
    }

    public static Holder<SoundEvent> soundHolder(SoundEvent soundEvent) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent);
    }

    public static SpellFieldVolume linearSphereVolume(float radius) {
        return new SpellFieldVolume(
                Optional.empty(),
                Optional.empty(),
                Optional.of(new FieldTopology(
                        LevelBasedValue.constant(radius),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        new LinearScalingFunction(),
                        ConstantScalingFunction.NONE,
                        ConstantScalingFunction.NONE,
                        DistanceMetric.EUCLIDEAN
                ))
        );
    }

    private static ResourceKey<AiSpellField> spellKey(String name) {
        return ResourceKey.create(AiSpellFields.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }
}