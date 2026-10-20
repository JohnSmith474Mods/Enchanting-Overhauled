package johnsmith.enchantingoverhauled.api.enchantment.spellfield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.loot.condition.CasterHostileCondition;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.MirageEntity;

import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;
import johnsmith.enchantmentcore.api.enchantment.spellfield.entity.AnchorVisualConfig;
import johnsmith.enchantmentcore.api.enchantment.spellfield.entity.SpellFieldAnchorEntity;
import johnsmith.enchantmentcore.api.registry.EnchantmentCoreEntities;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A data-driven record defining an AI spell field that can be selected and cast by flying arcane book
 * entities ({@link AbstractFlyingBookEntity}).
 * <p>
 * Encapsulates selection weight, tactical {@link SpellRole}, intended {@link SpellTarget},
 * delivery {@link CastType}, minimum world {@link DifficultyGate}, {@code mobGriefing} requirement,
 * school/theme affinities, optional persistent {@link AnchorPayload}, and the underlying
 * {@link SpellFieldComponent} from Enchantment Core.
 *
 * @param weight         The base selection weight of this spell (must be positive to be selectable).
 * @param role           The tactical {@link SpellRole} (Offensive, Defensive, or Supportive).
 * @param target         The intended {@link SpellTarget} category (Self, Ally, or Hostile).
 * @param castType       The delivery {@link CastType} (Projectile or Direct).
 * @param difficultyGate The minimum world {@link DifficultyGate} required for AI entities to cast this spell.
 * @param griefing       Whether this spell modifies blocks or causes griefing and therefore requires {@code mobGriefing} to be enabled.
 * @param schools        An optional {@link HolderSet} of {@link MagicSchool}s associated with this spell.
 * @param theme          The {@link EnchantmentTheme} key required to cast this spell (defaults to {@link EnchantmentThemes#COMMON}).
 * @param anchor         An optional {@link AnchorPayload} if the spell persists over time via a {@link SpellFieldAnchorEntity} or attached tick loop.
 * @param spellField     The underlying {@link SpellFieldComponent} defining the volumes, entity/block/volume effects, and visuals.
 * @see AiSpellFields
 */
public record AiSpellField(
        int weight,
        SpellRole role,
        SpellTarget target,
        CastType castType,
        DifficultyGate difficultyGate,
        boolean griefing,
        Optional<HolderSet<MagicSchool>> schools,
        ResourceKey<EnchantmentTheme> theme,
        Optional<AnchorPayload> anchor,
        SpellFieldComponent spellField
) {

    /**
     * Lazily initialized codec so referencing {@code AiSpellField.CODEC} during Forge's
     * {@code DataPackRegistryEvent.NewRegistry} does not prematurely trigger
     * {@link SpellFieldComponent} class initialization before {@code NewRegistryEvent.fill()}
     * populates Enchantment Core's custom registry suppliers.
     */
    public static final Codec<AiSpellField> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("weight", 10).forGetter(AiSpellField::weight),
            SpellRole.CODEC.fieldOf("role").forGetter(AiSpellField::role),
            SpellTarget.CODEC.optionalFieldOf("target").forGetter(field -> Optional.of(field.target())),
            CastType.CODEC.optionalFieldOf("cast_type").forGetter(field -> Optional.of(field.castType())),
            DifficultyGate.CODEC.optionalFieldOf("difficulty_gate", DifficultyGate.EASY).forGetter(AiSpellField::difficultyGate),
            Codec.BOOL.optionalFieldOf("griefing", false).forGetter(AiSpellField::griefing),
            RegistryCodecs.homogeneousList(MagicSchools.REGISTRY_KEY).optionalFieldOf("schools").forGetter(AiSpellField::schools),
            ResourceKey.codec(EnchantmentThemes.THEME_REGISTRY_KEY).optionalFieldOf("theme", EnchantmentThemes.COMMON).forGetter(AiSpellField::theme),
            AnchorPayload.CODEC.optionalFieldOf("anchor").forGetter(AiSpellField::anchor),
            Codec.lazyInitialized(() -> SpellFieldComponent.CODEC.codec()).fieldOf("spell_field").forGetter(AiSpellField::spellField)
    ).apply(instance, (weight, role, targetOpt, castTypeOpt, difficultyGate, griefing, schools, theme, anchor, spellField) -> {
        SpellTarget resolvedTarget = targetOpt.orElseGet(role::getDefaultTarget);
        CastType resolvedCastType = castTypeOpt.orElseGet(() ->
                resolvedTarget == SpellTarget.SELF ? CastType.DIRECT : role.getDefaultCastType()
        );
        return new AiSpellField(
                weight,
                role,
                resolvedTarget,
                resolvedCastType,
                difficultyGate,
                griefing,
                schools,
                theme,
                anchor,
                spellField
        );
    })));

    /**
     * Convenience constructor defaulting {@code griefing} to {@code false}.
     *
     * @param weight         The base selection weight.
     * @param role           The tactical spell role.
     * @param target         The intended target category.
     * @param castType       The delivery cast type.
     * @param difficultyGate The minimum world difficulty gate.
     * @param schools        The optional set of associated magic schools.
     * @param theme          The required enchantment theme key.
     * @param anchor         The optional anchor configuration.
     * @param spellField     The spell field component payload.
     */
    public AiSpellField(
            int weight,
            SpellRole role,
            SpellTarget target,
            CastType castType,
            DifficultyGate difficultyGate,
            Optional<HolderSet<MagicSchool>> schools,
            ResourceKey<EnchantmentTheme> theme,
            Optional<AnchorPayload> anchor,
            SpellFieldComponent spellField
    ) {
        this(
                weight,
                role,
                target,
                castType,
                difficultyGate,
                false,
                schools,
                theme,
                anchor,
                spellField
        );
    }

    /**
     * Convenience constructor defaulting {@code difficultyGate} to {@link DifficultyGate#EASY}
     * and {@code griefing} to {@code false}.
     *
     * @param weight     The base selection weight.
     * @param role       The tactical spell role.
     * @param target     The intended target category.
     * @param castType   The delivery cast type.
     * @param schools    The optional set of associated magic schools.
     * @param theme      The required enchantment theme key.
     * @param anchor     The optional anchor configuration.
     * @param spellField The spell field component payload.
     */
    public AiSpellField(
            int weight,
            SpellRole role,
            SpellTarget target,
            CastType castType,
            Optional<HolderSet<MagicSchool>> schools,
            ResourceKey<EnchantmentTheme> theme,
            Optional<AnchorPayload> anchor,
            SpellFieldComponent spellField
    ) {
        this(
                weight,
                role,
                target,
                castType,
                DifficultyGate.EASY,
                false,
                schools,
                theme,
                anchor,
                spellField
        );
    }

    /**
     * Convenience constructor deriving {@code castType} from {@code target} and {@code role},
     * and defaulting {@code griefing} to {@code false}.
     *
     * @param weight         The base selection weight.
     * @param role           The tactical spell role.
     * @param target         The intended target category.
     * @param difficultyGate The minimum world difficulty gate.
     * @param schools        The optional set of associated magic schools.
     * @param theme          The required enchantment theme key.
     * @param anchor         The optional anchor configuration.
     * @param spellField     The spell field component payload.
     */
    public AiSpellField(
            int weight,
            SpellRole role,
            SpellTarget target,
            DifficultyGate difficultyGate,
            Optional<HolderSet<MagicSchool>> schools,
            ResourceKey<EnchantmentTheme> theme,
            Optional<AnchorPayload> anchor,
            SpellFieldComponent spellField
    ) {
        this(
                weight,
                role,
                target,
                target == SpellTarget.SELF ? CastType.DIRECT : role.getDefaultCastType(),
                difficultyGate,
                false,
                schools,
                theme,
                anchor,
                spellField
        );
    }

    /**
     * Convenience constructor deriving {@code castType} from {@code target} and {@code role},
     * and defaulting {@code difficultyGate} to {@link DifficultyGate#EASY} and {@code griefing} to {@code false}.
     *
     * @param weight     The base selection weight.
     * @param role       The tactical spell role.
     * @param target     The intended target category.
     * @param schools    The optional set of associated magic schools.
     * @param theme      The required enchantment theme key.
     * @param anchor     The optional anchor configuration.
     * @param spellField The spell field component payload.
     */
    public AiSpellField(
            int weight,
            SpellRole role,
            SpellTarget target,
            Optional<HolderSet<MagicSchool>> schools,
            ResourceKey<EnchantmentTheme> theme,
            Optional<AnchorPayload> anchor,
            SpellFieldComponent spellField
    ) {
        this(
                weight,
                role,
                target,
                target == SpellTarget.SELF ? CastType.DIRECT : role.getDefaultCastType(),
                DifficultyGate.EASY,
                false,
                schools,
                theme,
                anchor,
                spellField
        );
    }

    /**
     * Convenience constructor deriving both {@code target} and {@code castType} from {@code role},
     * and defaulting {@code difficultyGate} to {@link DifficultyGate#EASY} and {@code griefing} to {@code false}.
     *
     * @param weight     The base selection weight.
     * @param role       The tactical spell role.
     * @param schools    The optional set of associated magic schools.
     * @param theme      The required enchantment theme key.
     * @param anchor     The optional anchor configuration.
     * @param spellField The spell field component payload.
     */
    public AiSpellField(
            int weight,
            SpellRole role,
            Optional<HolderSet<MagicSchool>> schools,
            ResourceKey<EnchantmentTheme> theme,
            Optional<AnchorPayload> anchor,
            SpellFieldComponent spellField
    ) {
        this(
                weight,
                role,
                role.getDefaultTarget(),
                role.getDefaultCastType(),
                DifficultyGate.EASY,
                false,
                schools,
                theme,
                anchor,
                spellField
        );
    }


    /**
     * Checks whether the given world {@link Difficulty} satisfies this spell's {@link #difficultyGate}.
     *
     * @param difficulty The current world {@link Difficulty}, or {@code null}.
     * @return {@code true} if the difficulty gate is met; {@code false} otherwise.
     */
    public boolean isDifficultyMet(@Nullable Difficulty difficulty) {
        return this.difficultyGate.isMet(difficulty);
    }

    /**
     * Checks whether a caster with the given {@code entityTheme} is compatible with this spell's theme.
     * <p>
     * Compatibility holds if either the spell's theme or the entity's theme is {@link EnchantmentThemes#COMMON},
     * or if both themes match exactly.
     *
     * @param entityTheme The caster's {@link EnchantmentTheme} key, or {@code null}.
     * @return {@code true} if the themes are compatible; {@code false} otherwise.
     */
    public boolean isThemeCompatible(@Nullable ResourceKey<EnchantmentTheme> entityTheme) {
        if (EnchantmentThemes.isCommon(this.theme) || EnchantmentThemes.isCommon(entityTheme)) {
            return true;
        }
        return this.theme.equals(entityTheme);
    }

    /**
     * Checks whether both this spell and the caster share the exact same non-common {@link EnchantmentTheme}.
     *
     * @param entityTheme The caster's {@link EnchantmentTheme} key, or {@code null}.
     * @return {@code true} if both themes are non-common and equal; {@code false} otherwise.
     */
    public boolean hasExactNonCommonThemeMatch(@Nullable ResourceKey<EnchantmentTheme> entityTheme) {
        return !EnchantmentThemes.isCommon(this.theme)
                && entityTheme != null
                && this.theme.equals(entityTheme);
    }

    /**
     * Evaluates the {@link SchoolAffinity} between this spell's allowed {@link #schools} and the caster's
     * {@code entitySchool}.
     *
     * @param registryAccess The active {@link RegistryAccess} used to check school cycle tags, or {@code null}.
     * @param entitySchool   The caster's {@link MagicSchool} key, or {@code null}.
     * @return The resolved {@link SchoolAffinity} tier.
     */
    public SchoolAffinity evaluateSchoolAffinity(
            @Nullable RegistryAccess registryAccess,
            @Nullable ResourceKey<MagicSchool> entitySchool
    ) {
        if (this.schools.isEmpty() || MagicSchools.isAberration(entitySchool)) {
            return SchoolAffinity.UNRESTRICTED;
        }

        HolderSet<MagicSchool> set = this.schools.get();
        boolean cycleMatch = false;

        for (Holder<MagicSchool> holder : set) {
            Optional<ResourceKey<MagicSchool>> candidateKeyOpt = holder.unwrapKey();
            if (candidateKeyOpt.isEmpty()) {
                continue;
            }
            ResourceKey<MagicSchool> candidateKey = candidateKeyOpt.get();
            if (candidateKey.equals(entitySchool)) {
                return SchoolAffinity.PRIMARY_SCHOOL;
            }
            if (!cycleMatch && MagicSchools.areInSameCycle(registryAccess, entitySchool, candidateKey)) {
                cycleMatch = true;
            }
        }

        return cycleMatch ? SchoolAffinity.CYCLE_SIBLING : SchoolAffinity.INCOMPATIBLE;
    }

    /**
     * Resolves the specific {@link MagicSchool} identity under which this spell is cast by an entity
     * with {@code entitySchool} (used for tinting chanting particles and projectiles).
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param entitySchool   The caster's own {@link MagicSchool} key, or {@code null}.
     * @return An {@link Optional} containing the resolved {@link MagicSchool} key, or empty if unrestricted.
     */
    public Optional<ResourceKey<MagicSchool>> resolveSpellSchool(
            @Nullable RegistryAccess registryAccess,
            @Nullable ResourceKey<MagicSchool> entitySchool
    ) {
        if (this.schools.isEmpty()) {
            return Optional.empty();
        }
        HolderSet<MagicSchool> set = this.schools.get();
        ResourceKey<MagicSchool> firstSchool = null;
        ResourceKey<MagicSchool> cycleSibling = null;

        for (Holder<MagicSchool> holder : set) {
            Optional<ResourceKey<MagicSchool>> candidateKeyOpt = holder.unwrapKey();
            if (candidateKeyOpt.isEmpty()) {
                continue;
            }
            ResourceKey<MagicSchool> candidateKey = candidateKeyOpt.get();
            if (firstSchool == null) {
                firstSchool = candidateKey;
            }
            if (entitySchool != null && candidateKey.equals(entitySchool)) {
                return Optional.of(candidateKey);
            }
            if (cycleSibling == null
                    && entitySchool != null
                    && MagicSchools.areInSameCycle(registryAccess, entitySchool, candidateKey)) {
                cycleSibling = candidateKey;
            }
        }

        if (cycleSibling != null) {
            return Optional.of(cycleSibling);
        }
        return Optional.ofNullable(firstSchool);
    }

    /**
     * Calculates the effective selection weight of this spell for a caster with the given
     * {@code entityTheme} and {@code entitySchool}.
     * <p>
     * Applies a {@code 3x} multiplier for a primary school match, {@code 2x} for a cycle sibling match,
     * and an additional {@code 3x} multiplier for an exact non-common theme match. Returns {@code 0}
     * if the theme or school is incompatible.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param entityTheme    The caster's {@link EnchantmentTheme} key, or {@code null}.
     * @param entitySchool   The caster's {@link MagicSchool} key, or {@code null}.
     * @return The non-negative effective selection weight.
     */
    public int calculateEffectiveWeight(
            @Nullable RegistryAccess registryAccess,
            @Nullable ResourceKey<EnchantmentTheme> entityTheme,
            @Nullable ResourceKey<MagicSchool> entitySchool
    ) {
        if (this.weight <= 0 || !this.isThemeCompatible(entityTheme)) {
            return 0;
        }
        SchoolAffinity schoolAffinity = this.evaluateSchoolAffinity(registryAccess, entitySchool);
        if (schoolAffinity == SchoolAffinity.INCOMPATIBLE) {
            return 0;
        }

        int multiplier = switch (schoolAffinity) {
            case PRIMARY_SCHOOL -> 3;
            case CYCLE_SIBLING -> 2;
            case UNRESTRICTED -> 1;
            case INCOMPATIBLE -> 0;
        };

        if (this.hasExactNonCommonThemeMatch(entityTheme)) {
            multiplier *= 3;
        }

        return this.weight * multiplier;
    }


    /**
     * Deploys this spell field at the specified world position.
     *
     * @param serverLevel   The executing {@link ServerLevel}.
     * @param position      The world coordinates where the spell field is deployed.
     * @param owner         The caster {@link LivingEntity}, or {@code null}.
     * @param spatialSource The spatial reference entity (such as the target or impact projectile).
     * @param weapon        The catalyst {@link ItemStack} held by the caster.
     */
    public void deployAt(
            ServerLevel serverLevel,
            Vec3 position,
            @Nullable LivingEntity owner,
            Entity spatialSource,
            ItemStack weapon
    ) {
        deployPayload(serverLevel, position, owner, spatialSource, weapon, this.spellField, this.anchor);
    }

    /**
     * Deploys a {@link SpellFieldComponent} payload at the given world position, either attaching it
     * directly to a flying book, spawning a persistent {@link SpellFieldAnchorEntity} (with hostility tags
     * for {@link CasterHostileCondition}), or executing an immediate one-shot application within a
     * {@link MirageEntity#withCasterContext(Entity, Runnable)} block.
     *
     * @param serverLevel   The executing {@link ServerLevel}.
     * @param position      The world coordinates for deployment.
     * @param owner         The caster {@link LivingEntity}, or {@code null}.
     * @param spatialSource The spatial reference entity.
     * @param weapon        The catalyst {@link ItemStack} held by the caster.
     * @param spellField    The {@link SpellFieldComponent} to apply, or {@code null}.
     * @param anchorPayload The optional {@link AnchorPayload} governing duration and tick phases.
     */
    public static void deployPayload(
            ServerLevel serverLevel,
            Vec3 position,
            @Nullable LivingEntity owner,
            Entity spatialSource,
            ItemStack weapon,
            @Nullable SpellFieldComponent spellField,
            Optional<AnchorPayload> anchorPayload
    ) {
        if (spellField == null) {
            return;
        }
        ItemStack stack = weapon != null ? weapon : ItemStack.EMPTY;
        EnchantedItemInUse context = new EnchantedItemInUse(stack, EquipmentSlot.MAINHAND, owner);

        if (anchorPayload != null && anchorPayload.isPresent()) {
            AnchorPayload anchorData = anchorPayload.get();
            if (spatialSource instanceof AbstractFlyingBookEntity book && anchorData.visualConfig().isEmpty()) {
                book.attachSpellField(spellField, anchorData, stack, owner != null ? owner : book);
                return;
            }

            SpellFieldAnchorEntity anchor = new SpellFieldAnchorEntity(EnchantmentCoreEntities.SPELL_FIELD_ANCHOR, serverLevel);
            anchor.setPos(position.x, position.y, position.z);
            if (owner != null) {
                anchor.setYRot(owner.getYRot());
                anchor.setXRot(0.0F);
                anchor.setYHeadRot(owner.getYHeadRot());
            }
            if (owner instanceof AbstractFlyingBookEntity book) {
                if (book.getAngryAt() != null) {
                    anchor.addTag(CasterHostileCondition.ANGRY_AT_TAG_PREFIX + book.getAngryAt());
                }
                LivingEntity currentTarget = book.getTarget();
                if (currentTarget != null) {
                    anchor.addTag(CasterHostileCondition.ANGRY_AT_TAG_PREFIX + currentTarget.getUUID());
                }
            } else if (owner instanceof Mob mob && mob.getTarget() != null) {
                anchor.addTag(CasterHostileCondition.ANGRY_AT_TAG_PREFIX + mob.getTarget().getUUID());
            }
            anchor.setAnchorData(
                    anchorData.duration(),
                    anchorData.tickRate(),
                    anchorData.activationDelay(),
                    anchorData.activePhase(),
                    anchorData.restPhase(),
                    spellField,
                    1,
                    context.itemStack(),
                    context.inSlot(),
                    context.owner()
            );
            anchorData.visualConfig().ifPresent(anchor::setVisualConfig);
            serverLevel.addFreshEntity(anchor);

            if (spatialSource instanceof LivingEntity living
                    && living.isAlive()
                    && !(living instanceof Player)
                    && (owner == null || living == owner || (owner instanceof AbstractFlyingBookEntity book && !book.isValidHostileTarget(living)))) {
                anchor.startRiding(living, true);
            }
        } else {
            // Prefer the actual caster (owner) over the spatial target so CasterHostileCondition resolves the caster
            Entity effectiveCaster = owner != null ? owner : spatialSource;
            MirageEntity.withCasterContext(
                    effectiveCaster,
                    () -> spellField.apply(serverLevel, 1, context, spatialSource, position)
            );
        }
    }

    /**
     * Represents the degree of compatibility between a caster's {@link MagicSchool} and an {@link AiSpellField}.
     */
    public enum SchoolAffinity {
        /**
         * The caster's school is neither in the spell's school set nor in the same magic cycle.
         */
        INCOMPATIBLE,

        /**
         * The spell has no school restriction, or the caster is Aberrant (wildcard).
         */
        UNRESTRICTED,

        /**
         * The caster's school belongs to the same magic cycle as one of the spell's schools.
         */
        CYCLE_SIBLING,

        /**
         * The caster's school directly matches one of the spell's schools.
         */
        PRIMARY_SCHOOL
    }

    /**
     * Configuration record defining the lifecycle and visual parameters for a persistent
     * {@link SpellFieldAnchorEntity} deployed by an {@link AiSpellField}.
     *
     * @param duration        Total lifetime of the anchor in ticks (default: {@code 100}).
     * @param tickRate        Interval in ticks between spell field applications (default: {@code 1}).
     * @param activationDelay Initial delay in ticks before the anchor begins applying effects (default: {@code 0}).
     * @param activePhase     Duration in ticks of each active pulsing phase, or {@code 0} for continuous (default: {@code 0}).
     * @param restPhase       Duration in ticks of each dormant phase between active phases (default: {@code 0}).
     * @param visualConfig    Optional visual configuration for rendering the anchor entity.
     */
    public record AnchorPayload(
            int duration,
            int tickRate,
            int activationDelay,
            int activePhase,
            int restPhase,
            Optional<AnchorVisualConfig> visualConfig
    ) {
        /**
         * The codec responsible for serializing and deserializing {@link AnchorPayload} definitions from JSON.
         */
        public static final Codec<AnchorPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("duration", 100).forGetter(AnchorPayload::duration),
                Codec.INT.optionalFieldOf("tick_rate", 1).forGetter(AnchorPayload::tickRate),
                Codec.INT.optionalFieldOf("activation_delay", 0).forGetter(AnchorPayload::activationDelay),
                Codec.INT.optionalFieldOf("active_phase", 0).forGetter(AnchorPayload::activePhase),
                Codec.INT.optionalFieldOf("rest_phase", 0).forGetter(AnchorPayload::restPhase),
                AnchorVisualConfig.CODEC.optionalFieldOf("visual_config").forGetter(AnchorPayload::visualConfig)
        ).apply(instance, AnchorPayload::new));
    }
}