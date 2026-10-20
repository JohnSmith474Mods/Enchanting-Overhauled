package johnsmith.enchantingoverhauled.api.enchantment.spellfield.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import johnsmith.enchantingoverhauled.entity.MirageEntity;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.SpellFieldEntityEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.effect.SpellFieldVolumeEffect;
import johnsmith.enchantmentcore.api.enchantment.spellfield.math.GlobalVolume;
import johnsmith.enchantmentcore.api.registry.LevelBasedKeyProvider;
import johnsmith.enchantmentcore.api.enchantment.spellfield.entity.SpellFieldAnchorEntity;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A spell field effect that summons a synchronized orbital formation of illusory {@link MirageEntity} decoys
 * around the target or caster entity.
 * <p>
 * Implements both {@link SpellFieldEntityEffect} and {@link SpellFieldVolumeEffect} so it can be
 * used in either {@code "entity_effects"} or {@code "volume_effects"} (including self-targeted
 * spell fields where {@code context.owner()} is excluded from {@code entity_effects}).
 *
 * @param count         Number of mirage decoys to summon into the formation (default: {@code 3}).
 * @param duration      Lifetime of the summoned mirages in ticks (default: {@code 240}).
 * @param radius        Orbital radius around the subject, or {@code <= 0} to auto-scale with entity size.
 * @param redirectAggro Whether nearby hostile mobs targeting the subject should redirect aggro to a mirage.
 * @param entityFilter  Optional predicate filtering which target entities can receive the mirage formation.
 */
public record SummonMirageEffect(
        LevelBasedValue count,
        LevelBasedValue duration,
        LevelBasedValue radius,
        boolean redirectAggro,
        Optional<ContextAwarePredicate> entityFilter
) implements SpellFieldEntityEffect, SpellFieldVolumeEffect {

    /**
     * JSON field key for the mirage count parameter.
     */
    public static final String COUNT = "count";

    /**
     * JSON field key for the mirage duration parameter.
     */
    public static final String DURATION = "duration";

    /**
     * JSON field key for the orbital radius parameter.
     */
    public static final String RADIUS = "radius";

    /**
     * JSON field key for the aggro redirection flag.
     */
    public static final String REDIRECT_AGGRO = "redirect_aggro";

    /**
     * JSON field key for the optional entity filter predicate.
     */
    public static final String ENTITY_FILTER = "entity_filter";

    /**
     * Registry path identifier for this spell field effect ({@code "summon_mirage"}).
     */
    public static final String KEY = "summon_mirage";

    /**
     * Provides the set of JSON property keys that hold {@link LevelBasedValue}s for data transformation.
     */
    public static final LevelBasedKeyProvider KEY_PROVIDER = () -> Set.of(COUNT, DURATION, RADIUS);

    /**
     * The {@link MapCodec} responsible for serializing and deserializing {@link SummonMirageEffect} instances.
     */
    public static final MapCodec<SummonMirageEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf(COUNT, LevelBasedValue.constant(3.0F)).forGetter(SummonMirageEffect::count),
            LevelBasedValue.CODEC.optionalFieldOf(DURATION, LevelBasedValue.constant((float) MirageEntity.DEFAULT_MAX_LIFETIME_TICKS)).forGetter(SummonMirageEffect::duration),
            LevelBasedValue.CODEC.optionalFieldOf(RADIUS, LevelBasedValue.constant(0.0F)).forGetter(SummonMirageEffect::radius),
            Codec.BOOL.optionalFieldOf(REDIRECT_AGGRO, true).forGetter(SummonMirageEffect::redirectAggro),
            ContextAwarePredicate.CODEC.optionalFieldOf(ENTITY_FILTER).forGetter(SummonMirageEffect::entityFilter)
    ).apply(instance, SummonMirageEffect::new));

    /**
     * Convenience constructor with auto-scaled orbital radius ({@code 0.0F}), aggro redirection enabled,
     * and no entity filter.
     *
     * @param count    The number of mirages to summon.
     * @param duration The lifetime of the mirages in ticks.
     */
    public SummonMirageEffect(LevelBasedValue count, LevelBasedValue duration) {
        this(count, duration, LevelBasedValue.constant(0.0F), true, Optional.empty());
    }

    /**
     * Convenience constructor using the default mirage lifetime ({@link MirageEntity#DEFAULT_MAX_LIFETIME_TICKS}).
     *
     * @param count The number of mirages to summon.
     */
    public SummonMirageEffect(LevelBasedValue count) {
        this(count, LevelBasedValue.constant((float) MirageEntity.DEFAULT_MAX_LIFETIME_TICKS));
    }

    /**
     * Applies this effect to a specific target entity within the spell field volume.
     *
     * @param level            The executing {@link ServerLevel}.
     * @param enchantmentLevel The enchantment level of the spell field.
     * @param context          The item-in-use context.
     * @param spatialReference The spatial reference entity.
     * @param epicenter        The epicenter coordinates of the spell field.
     * @param victim           The target entity receiving the mirage formation.
     * @param volumeScalar     The topological scalar at the victim's position.
     */
    @Override
    public void apply(
            ServerLevel level,
            int enchantmentLevel,
            EnchantedItemInUse context,
            Entity spatialReference,
            Vec3 epicenter,
            Entity victim,
            float volumeScalar
    ) {
        if (victim == null || !victim.isAlive() || victim instanceof MirageEntity || victim instanceof SpellFieldAnchorEntity) {
            return;
        }
        if (this.entityFilter.isPresent()
                && !this.entityFilter.get().matches(SpellFieldEntityEffect.createLootContext(level, victim, enchantmentLevel))) {
            return;
        }
        this.spawnMirageFormation(level, enchantmentLevel, victim, volumeScalar);
    }

    /**
     * Applies this effect as a volume-level effect, resolving the subject entity from
     * {@code spatialReference} or {@code context.owner()}.
     *
     * @param level            The executing {@link ServerLevel}.
     * @param enchantmentLevel The enchantment level of the spell field.
     * @param context          The item-in-use context.
     * @param spatialReference The spatial reference entity.
     * @param epicenter        The epicenter coordinates of the spell field.
     * @param volumes          The list of global volumes comprising the spell field.
     */
    @Override
    public void apply(
            ServerLevel level,
            int enchantmentLevel,
            EnchantedItemInUse context,
            Entity spatialReference,
            Vec3 epicenter,
            List<GlobalVolume> volumes
    ) {
        Entity subject = resolveVolumeSubject(context, spatialReference);
        if (subject == null || !subject.isAlive()) {
            return;
        }
        if (this.entityFilter.isPresent()
                && !this.entityFilter.get().matches(SpellFieldEntityEffect.createLootContext(level, subject, enchantmentLevel))) {
            return;
        }
        this.spawnMirageFormation(level, enchantmentLevel, subject, 1.0F);
    }

    /**
     * Resolves the subject entity for volume-based application, preferring a valid living
     * {@code spatialReference} (excluding anchors and mirages) before falling back to {@code context.owner()}.
     *
     * @param context          The item-in-use context.
     * @param spatialReference The spatial reference entity, or {@code null}.
     * @return The resolved subject {@link Entity}, or {@code null} if none is valid.
     */
    @Nullable
    private static Entity resolveVolumeSubject(EnchantedItemInUse context, @Nullable Entity spatialReference) {
        if (spatialReference != null
                && spatialReference.isAlive()
                && !(spatialReference instanceof SpellFieldAnchorEntity)
                && !(spatialReference instanceof MirageEntity)) {
            return spatialReference;
        }
        Entity owner = context.owner();
        if (owner != null && owner.isAlive() && !(owner instanceof MirageEntity)) {
            return owner;
        }
        return null;
    }

    /**
     * Calculates the scaled mirage count, lifetime, and orbital radius, and delegates formation
     * spawning to {@link MirageEntity#summonFormation}.
     *
     * @param level            The executing {@link ServerLevel}.
     * @param enchantmentLevel The enchantment level used to evaluate {@link LevelBasedValue}s.
     * @param subject          The entity to be mirrored by the summoned mirages.
     * @param scalar           The volume scalar multiplying the calculated mirage count.
     */
    private void spawnMirageFormation(ServerLevel level, int enchantmentLevel, Entity subject, float scalar) {
        if (scalar <= 0.0001F) {
            return;
        }
        float calculatedCount = this.count.calculate(enchantmentLevel) * scalar;
        int spawnAmount = (int) calculatedCount;
        if (level.random.nextFloat() < (calculatedCount - spawnAmount)) {
            spawnAmount++;
        }
        if (spawnAmount <= 0) {
            return;
        }

        int lifetimeTicks = Math.max(20, Math.round(this.duration.calculate(enchantmentLevel)));
        float orbitRadius = Math.max(0.0F, this.radius.calculate(enchantmentLevel));
        MirageEntity.summonFormation(
                level,
                subject,
                spawnAmount,
                lifetimeTicks,
                orbitRadius,
                this.redirectAggro
        );
    }

    /**
     * Retrieves the {@link MapCodec} for this spell field effect.
     *
     * @return The {@link #CODEC} instance.
     */
    @Override
    public MapCodec<? extends SpellFieldEntityEffect> codec() {
        return CODEC;
    }
}