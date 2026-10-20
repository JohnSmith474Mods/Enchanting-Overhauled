package johnsmith.enchantingoverhauled.api.loot.condition;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.MirageEntity;
import johnsmith.enchantingoverhauled.loot.condition.LootItemConditionTypes;
import johnsmith.enchantmentcore.api.enchantment.spellfield.entity.SpellFieldAnchorEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link LootItemCondition} that evaluates to {@code true} if the spell field's caster
 * (or an active {@link SpellFieldAnchorEntity} / nearby {@link AbstractFlyingBookEntity})
 * is actively hostile towards the target entity ({@link LootContextParams#THIS_ENTITY}).
 * <p>
 * Allows offensive spell fields to harm players wearing enchanted armor when the caster
 * has been provoked or is actively targeting them in self-defense, while also allowing
 * offensive spell fields to harm hostile non-player mobs wearing enchanted armor.
 */
public record CasterHostileCondition() implements LootItemCondition {

    /**
     * Singleton instance of {@link CasterHostileCondition}.
     */
    public static final CasterHostileCondition INSTANCE = new CasterHostileCondition();

    /**
     * Unit {@link MapCodec} returning {@link #INSTANCE} when deserializing from JSON.
     */
    public static final MapCodec<CasterHostileCondition> CODEC = MapCodec.unit(INSTANCE);

    /**
     * Entity tag prefix applied to {@link SpellFieldAnchorEntity} instances to record the UUID
     * of the target entity the caster was angry at when deploying the anchor.
     */
    public static final String ANGRY_AT_TAG_PREFIX = Constants.MOD_ID + ":angry_at_";

    /**
     * Creates a {@link LootItemCondition.Builder} that supplies {@link #INSTANCE}.
     *
     * @return A condition builder for {@link CasterHostileCondition}.
     */
    public static Builder builder() {
        return () -> INSTANCE;
    }

    /**
     * Retrieves the registered {@link LootItemConditionType} for this condition.
     *
     * @return The {@link LootItemConditionTypes#CASTER_HOSTILE} condition type.
     */
    @Override
    public @NotNull LootItemConditionType getType() {
        return LootItemConditionTypes.CASTER_HOSTILE.type();
    }

    /**
     * Evaluates whether the spell caster is actively hostile toward {@link LootContextParams#THIS_ENTITY}.
     * <p>
     * Checks the thread-local caster context from {@link MirageEntity#getCurrentCasterContext()},
     * the {@link LootContextParams#ATTACKING_ENTITY} parameter (unwrapping projectiles and mirages),
     * nearby tagged {@link SpellFieldAnchorEntity} instances within 16 blocks, and nearby hostile
     * {@link AbstractFlyingBookEntity} instances within 48 blocks.
     *
     * @param context The {@link LootContext} containing the target entity and level state.
     * @return {@code true} if the caster or a nearby spell anchor/flying book is hostile toward the target entity;
     *         {@code false} otherwise.
     */
    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            return false;
        }

        Entity caster = MirageEntity.getCurrentCasterContext();
        if (caster == null || caster == target) {
            caster = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        }
        if (caster instanceof Projectile projectile && projectile.getOwner() != null) {
            caster = projectile.getOwner();
        }
        if (caster instanceof MirageEntity mirage) {
            caster = mirage.getCaster();
        }

        if (caster instanceof AbstractFlyingBookEntity bookCaster) {
            return bookCaster.isAngryAt(target)
                    || bookCaster.getTarget() == target
                    || bookCaster.isValidHostileTarget(target);
        }

        if (caster instanceof Mob mobCaster && mobCaster != target) {
            return mobCaster.getTarget() == target;
        }

        ServerLevel level = context.getLevel();
        String angryTag = ANGRY_AT_TAG_PREFIX + target.getUUID();

        AABB anchorSearchBox = target.getBoundingBox().inflate(16.0D);
        List<SpellFieldAnchorEntity> nearbyAnchors = level.getEntitiesOfClass(
                SpellFieldAnchorEntity.class,
                anchorSearchBox,
                anchor -> anchor.isAlive() && anchor.getTags().contains(angryTag)
        );
        if (!nearbyAnchors.isEmpty()) {
            return true;
        }

        AABB bookSearchBox = target.getBoundingBox().inflate(48.0D);
        List<AbstractFlyingBookEntity> hostileBooks = level.getEntitiesOfClass(
                AbstractFlyingBookEntity.class,
                bookSearchBox,
                book -> book.isAlive()
                        && (book.isAngryAt(target)
                        || book.getTarget() == target
                        || book.isValidHostileTarget(target))
        );
        return !hostileBooks.isEmpty();
    }
}