package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.EffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.ParticleEffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.SoundEffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.client.particle.SgaRuneParticle;
import johnsmith.enchantingoverhauled.entity.ai.goal.TomeBarterGoal;
import johnsmith.enchantingoverhauled.entity.ai.control.BookMoveControl;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.item.EnchantedPageItem;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for flying arcane book entities ({@link EnchantedTomeEntity} and {@link EnchantedBookEntity}).
 * <p>
 * Optimized to bound 3D A* node evaluation via {@code setMaxVisitedNodesMultiplier(0.25F)}
 * and guard altar-vigil player lookups so they only run when the entity is in {@link BookAnimationState#ALTAR_VIGIL}.
 */
public abstract class AbstractFlyingBookEntity extends Monster {
    private static final EntityDataAccessor<Byte> ANIMATION_STATE =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> DATA_THEME_ID =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_SCHOOL_ID =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_CAST_SCHOOL_ID =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<ItemStack> DATA_BARTER_FUEL =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_BARTER_CATALYST =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ANGER_TIME =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_ROOST_POS =
            SynchedEntityData.defineId(AbstractFlyingBookEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private static final ResourceLocation CLOSED_COVER_ARMOR_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "closed_cover_shield");
    private static final AttributeModifier CLOSED_COVER_ARMOR_MODIFIER =
            new AttributeModifier(CLOSED_COVER_ARMOR_ID, 8.0D, AttributeModifier.Operation.ADD_VALUE);

    private int serverStateTicks;
    private boolean bookInitialized = false;
    private int hurtDashCooldown = 0;
    private boolean pendingHurtDash = false;
    private int barterCooldown = 0;
    private int altarPlayerCheckCooldown = 0;
    private boolean cachedAltarPlayerNearby = false;
    private final List<AttachedSpellFieldInstance> activeSpellFields = new ArrayList<>();
    @Nullable
    private UUID angryAt;

    public float animX, animXO;
    public float animY = 1.2F, animYO = 1.2F;
    public float animZ, animZO;
    public float animOpenness = 1.0F, animOpennessO = 1.0F;
    public float flutterPhase;

    public float pageFlip;
    public float oPageFlip;
    public float pageFlipT;
    public float pageFlipA;

    public int altarTime;
    public float altarFlip;
    public float altarOFlip;
    public float altarFlipT;
    public float altarFlipA;
    public float altarOpen;
    public float altarOOpen;
    public float altarRot;
    public float altarORot;
    public float altarTRot;

    public Quaternionf animRotation = new Quaternionf();
    public Quaternionf animRotationO = new Quaternionf();
    private BookAnimationState clientLastState = BookAnimationState.IDLE;
    private int clientStateTicks;

    protected AbstractFlyingBookEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new BookMoveControl(this, 20, true);
        this.setDropChance(EquipmentSlot.MAINHAND, this.getMainHandDropChance());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, (byte) BookAnimationState.IDLE.ordinal());
        builder.define(DATA_THEME_ID, EnchantmentThemes.COMMON.location().toString());
        builder.define(DATA_SCHOOL_ID, MagicSchools.ABERRATION.location().toString());
        builder.define(DATA_CAST_SCHOOL_ID, "");
        builder.define(DATA_BARTER_FUEL, ItemStack.EMPTY);
        builder.define(DATA_BARTER_CATALYST, ItemStack.EMPTY);
        builder.define(DATA_ANGER_TIME, 0);
        builder.define(DATA_ROOST_POS, Optional.empty());
    }

    @Override
    public int getMaxHeadXRot() {
        return 85;
    }

    public BookAnimationState getAnimationState() {
        byte ordinal = this.entityData.get(ANIMATION_STATE);
        BookAnimationState[] values = BookAnimationState.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BookAnimationState.IDLE;
        }
        return values[ordinal];
    }

    public void setAnimationState(BookAnimationState state) {
        if (this.getAnimationState() != state) {
            this.entityData.set(ANIMATION_STATE, (byte) state.ordinal());
            this.serverStateTicks = 0;
        }
    }

    public boolean isRoosting() {
        BookAnimationState state = this.getAnimationState();
        return state == BookAnimationState.RESTING
                || state == BookAnimationState.ALTAR_VIGIL
                || state == BookAnimationState.LECTERN_ROOST;
    }

    public int getServerStateTicks() {
        return this.serverStateTicks;
    }

    public int getClientStateTicks() {
        return this.clientStateTicks;
    }

    @Nullable
    public BlockPos getRoostPos() {
        return this.entityData.get(DATA_ROOST_POS).orElse(null);
    }

    public void setRoostPos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_ROOST_POS, Optional.ofNullable(pos));
    }

    public ResourceKey<EnchantmentTheme> getThemeKey() {
        String raw = this.entityData.get(DATA_THEME_ID);
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return EnchantmentThemes.COMMON;
        }
        return ResourceKey.create(EnchantmentThemes.THEME_REGISTRY_KEY, id);
    }

    public void setThemeKey(@Nullable ResourceKey<EnchantmentTheme> themeKey) {
        ResourceKey<EnchantmentTheme> effective = themeKey != null ? themeKey : EnchantmentThemes.COMMON;
        this.entityData.set(DATA_THEME_ID, effective.location().toString());
    }

    public ResourceKey<MagicSchool> getSchoolKey() {
        String raw = this.entityData.get(DATA_SCHOOL_ID);
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return MagicSchools.ABERRATION;
        }
        return ResourceKey.create(MagicSchools.REGISTRY_KEY, id);
    }

    public void setSchoolKey(@Nullable ResourceKey<MagicSchool> schoolKey) {
        ResourceKey<MagicSchool> effective = schoolKey != null ? schoolKey : MagicSchools.ABERRATION;
        this.entityData.set(DATA_SCHOOL_ID, effective.location().toString());
    }

    public Optional<ResourceKey<MagicSchool>> getCastSchoolKey() {
        String raw = this.entityData.get(DATA_CAST_SCHOOL_ID);
        if (raw == null || raw.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation id = ResourceLocation.tryParse(raw);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.of(ResourceKey.create(MagicSchools.REGISTRY_KEY, id));
    }

    public void setCastSchoolKey(@Nullable ResourceKey<MagicSchool> schoolKey) {
        this.entityData.set(DATA_CAST_SCHOOL_ID, schoolKey != null ? schoolKey.location().toString() : "");
    }

    public Optional<EnchantmentTheme> getTheme() {
        var regOpt = this.level().registryAccess().registry(EnchantmentThemes.THEME_REGISTRY_KEY);
        if (regOpt.isPresent()) {
            return Optional.ofNullable(regOpt.get().get(this.getThemeKey()));
        }
        return Optional.empty();
    }

    public MagicSchool getSchool() {
        return MagicSchools.getSchool(this.level().registryAccess(), this.getSchoolKey());
    }

    public int getSchoolColor() {
        return this.getSchool().getColor() & 0xFFFFFF;
    }

    public int getCastSchoolColor() {
        return this.getCastSchoolKey()
                .map(key -> MagicSchools.getSchool(this.level().registryAccess(), key).getColor() & 0xFFFFFF)
                .orElseGet(this::getSchoolColor);
    }

    public int getActiveParticleColor() {
        BookAnimationState state = this.getAnimationState();
        if (state == BookAnimationState.CHANTING) {
            return 0xFFFFFF;
        }
        if (state.isChanting() || this.getCastSchoolKey().isPresent()) {
            return this.getCastSchoolColor();
        }
        return this.getSchoolColor();
    }

    public int getThemeColor() {
        return this.getTheme()
                .flatMap(EnchantmentTheme::colorCode)
                .orElse(0xFFFFFF) & 0xFFFFFF;
    }

    public ItemStack getBarterFuel() {
        return this.entityData.get(DATA_BARTER_FUEL);
    }

    public void setBarterFuel(ItemStack stack) {
        this.entityData.set(DATA_BARTER_FUEL, stack.copy());
    }

    public ItemStack getBarterCatalyst() {
        return this.entityData.get(DATA_BARTER_CATALYST);
    }

    public void setBarterCatalyst(ItemStack stack) {
        this.entityData.set(DATA_BARTER_CATALYST, stack.copy());
    }

    public int getBarterCooldown() {
        return this.barterCooldown;
    }

    public void setBarterCooldown(int ticks) {
        this.barterCooldown = Math.max(0, ticks);
    }

    public void attachSpellField(
            SpellFieldComponent spellField,
            AiSpellField.AnchorPayload anchorData,
            ItemStack weapon,
            @Nullable LivingEntity owner
    ) {
        if (spellField == null || anchorData == null) {
            return;
        }
        this.activeSpellFields.removeIf(existing -> existing.spellField == spellField);
        this.activeSpellFields.add(new AttachedSpellFieldInstance(
                spellField,
                anchorData,
                weapon != null ? weapon.copy() : ItemStack.EMPTY,
                owner != null ? owner : this
        ));
    }

    private void tickAttachedSpellFields(ServerLevel serverLevel) {
        if (this.activeSpellFields.isEmpty()) {
            return;
        }
        Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        var iterator = this.activeSpellFields.iterator();
        while (iterator.hasNext()) {
            AttachedSpellFieldInstance instance = iterator.next();
            AiSpellField.AnchorPayload data = instance.anchorData;
            if (instance.age >= data.duration()) {
                iterator.remove();
                continue;
            }
            if (instance.age >= data.activationDelay()) {
                int activeTicks = instance.age - data.activationDelay();
                boolean inActivePhase = true;
                if (data.activePhase() > 0 && data.restPhase() > 0) {
                    int cycleLength = data.activePhase() + data.restPhase();
                    inActivePhase = (activeTicks % cycleLength) < data.activePhase();
                }
                int tickRate = Math.max(1, data.tickRate());
                if (inActivePhase && activeTicks % tickRate == 0) {
                    LivingEntity effectiveOwner = instance.owner != null && instance.owner.isAlive() ? instance.owner : this;
                    EnchantedItemInUse context = new EnchantedItemInUse(instance.weapon, EquipmentSlot.MAINHAND, effectiveOwner);
                    instance.spellField.apply(serverLevel, 1, context, this, center);
                }
            }
            instance.age++;
        }
    }

    private static final class AttachedSpellFieldInstance {
        private final SpellFieldComponent spellField;
        private final AiSpellField.AnchorPayload anchorData;
        private final ItemStack weapon;
        @Nullable
        private final LivingEntity owner;
        private int age = 0;

        private AttachedSpellFieldInstance(
                SpellFieldComponent spellField,
                AiSpellField.AnchorPayload anchorData,
                ItemStack weapon,
                @Nullable LivingEntity owner
        ) {
            this.spellField = spellField;
            this.anchorData = anchorData;
            this.weapon = weapon;
            this.owner = owner;
        }
    }

    public void ejectBarterItems() {
        if (!this.level().isClientSide()) {
            Player nearestPlayer = this.level().getNearestPlayer(this, 8.0D);
            ItemStack fuel = this.getBarterFuel();
            if (!fuel.isEmpty()) {
                this.spawnRefundedItem(fuel.copy(), nearestPlayer);
                this.setBarterFuel(ItemStack.EMPTY);
            }
            ItemStack catalyst = this.getBarterCatalyst();
            if (!catalyst.isEmpty()) {
                this.spawnRefundedItem(catalyst.copy(), nearestPlayer);
                this.setBarterCatalyst(ItemStack.EMPTY);
            }
        }
    }

    private void spawnRefundedItem(ItemStack stack, @Nullable Player recipient) {
        if (stack.isEmpty() || this.level().isClientSide()) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.35D, this.getZ(), stack);
        if (recipient != null) {
            Vec3 dir = recipient.position().add(0.0D, 0.5D, 0.0D).subtract(this.position()).normalize().scale(0.22D);
            itemEntity.setDeltaMovement(dir.x, Math.max(0.1D, dir.y), dir.z);
        } else {
            itemEntity.setDeltaMovement(0.0D, 0.15D, 0.0D);
        }
        itemEntity.setDefaultPickUpDelay();
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        if (!(this instanceof EnchantedTomeEntity)
                || this.barterCooldown > 0
                || this.isAngryAt(player)
                || this.getTarget() != null) {
            return super.mobInteract(player, hand);
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return super.mobInteract(player, hand);
        }
        if (this.getBarterFuel().isEmpty() && TomeBarterGoal.isValidBarterFuel(held)) {
            if (!this.level().isClientSide()) {
                ItemStack single = held.copyWithCount(1);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.setBarterFuel(single);
                this.playSound(SoundEvents.ITEM_PICKUP, 0.7F, 1.2F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        if (this.getBarterCatalyst().isEmpty()
                && TomeBarterGoal.isValidBarterCatalyst(this.level().registryAccess(), held)) {
            if (!this.level().isClientSide()) {
                ItemStack single = held.copyWithCount(1);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.setBarterCatalyst(single);
                this.playSound(SoundEvents.ITEM_PICKUP, 0.7F, 1.2F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    public int getAngerTime() {
        return this.entityData.get(DATA_ANGER_TIME);
    }

    public void setAngerTime(int ticks) {
        this.entityData.set(DATA_ANGER_TIME, Math.max(0, ticks));
    }

    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    public void setAngryAt(@Nullable UUID uuid) {
        this.angryAt = uuid;
    }

    public boolean isAngry() {
        return this.getAngerTime() > 0;
    }

    public boolean isAngryAt(LivingEntity entity) {
        if (entity == null) return false;
        if (this.getTarget() == entity) return true;
        return this.isAngry() && entity.getUUID().equals(this.angryAt);
    }

    public static boolean isPlayerWearingEnchantedArmor(Player player) {
        if (player == null) return false;
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!armorStack.isEmpty() && armorStack.isEnchanted()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerPacified(Player player) {
        if (player == null) return false;
        if (this.isAngryAt(player)) return false;
        return isPlayerWearingEnchantedArmor(player);
    }

    public boolean isBookAlly(@Nullable Entity entity) {
        return entity != null && entity.getType().is(Tags.ENCHANTED_TOME_ALLIES);
    }

    public boolean isAvoidCollateralEntity(@Nullable Entity entity) {
        return entity != null && entity.getType().is(Tags.ENCHANTED_TOME_AVOID_COLLATERAL);
    }

    public boolean isNeutralEntity(@Nullable Entity entity) {
        return entity != null && (entity.getType().is(Tags.ENCHANTED_TOME_NEUTRAL) || this.isAvoidCollateralEntity(entity));
    }

    @Override
    public boolean isAlliedTo(@NotNull Entity entity) {
        if (this.isBookAlly(entity)) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    public boolean isValidHostileTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this) {
            return false;
        }
        if (this.isBookAlly(entity) || this.isNeutralEntity(entity)) {
            return false;
        }
        if (this.isAngryAt(entity)) {
            return true;
        }
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
            if (this instanceof EnchantedTomeEntity tome && tome.isInAltarDeceptionGracePeriod()) {
                return false;
            }
            return !this.isPlayerPacified(player);
        }
        return entity instanceof Mob;
    }

    public void provoke(@Nullable LivingEntity attacker, int angerTicks) {
        if (attacker == null || this.isBookAlly(attacker) || this.isNeutralEntity(attacker)) {
            return;
        }
        if (attacker instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        this.setAngerTime(Math.max(this.getAngerTime(), angerTicks));
        this.setAngryAt(attacker.getUUID());
        this.setTarget(attacker);

        if (!this.level().isClientSide()) {
            AABB rallyBox = this.getBoundingBox().inflate(16.0D);
            List<Mob> allies = this.level().getEntitiesOfClass(
                    Mob.class,
                    rallyBox,
                    ally -> ally != this && ally.isAlive() && this.isBookAlly(ally)
            );
            for (Mob ally : allies) {
                if (ally instanceof AbstractFlyingBookEntity bookAlly) {
                    bookAlly.setAngerTime(Math.max(bookAlly.getAngerTime(), angerTicks));
                    bookAlly.setAngryAt(attacker.getUUID());
                }
                if (ally.getTarget() == null) {
                    ally.setTarget(attacker);
                }
            }
        }
    }

    public boolean isCoverClosed() {
        BookAnimationState state = this.getAnimationState();
        if (state == BookAnimationState.ALTAR_VIGIL) {
            return this.altarOpen < 0.2F;
        }
        if (state == BookAnimationState.DASHING) {
            return this.serverStateTicks >= 4 || this.animOpenness < 0.2F;
        }
        return state == BookAnimationState.RESTING
                || state == BookAnimationState.SCOUTING
                || this.animOpenness < 0.2F;
    }

    public boolean isWideOpen() {
        BookAnimationState state = this.getAnimationState();
        if (state == BookAnimationState.ALTAR_VIGIL) {
            return this.altarOpen > 0.8F;
        }
        return state.isChanting()
                || this.animOpenness > 0.8F;
    }

    public boolean isInitialized() {
        return this.bookInitialized;
    }

    public int getHurtDashCooldown() {
        return this.hurtDashCooldown;
    }

    public void setHurtDashCooldown(int ticks) {
        this.hurtDashCooldown = Math.max(0, ticks);
    }

    public boolean consumePendingHurtDash() {
        if (this.pendingHurtDash) {
            this.pendingHurtDash = false;
            return true;
        }
        return false;
    }

    protected float getMainHandDropChance() {
        return 0.18F;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.initializeBookIdentityAndStack(level.registryAccess(), level.getRandom());
        return data;
    }

    public void initializeWithThemeAndSchool(
            RegistryAccess registryAccess,
            RandomSource random,
            @Nullable ResourceKey<EnchantmentTheme> themeKey,
            @Nullable ResourceKey<MagicSchool> schoolKey
    ) {
        this.setThemeKey(themeKey != null ? themeKey : EnchantmentThemes.COMMON);
        this.setSchoolKey(schoolKey != null ? schoolKey : MagicSchools.ABERRATION);
        this.bookInitialized = false;
        this.initializeBookIdentityAndStack(registryAccess, random);
    }

    public void initializeBookIdentityAndStack(RegistryAccess registryAccess, RandomSource random) {
        if (this.bookInitialized && !this.getMainHandItem().isEmpty()) {
            return;
        }

        ResourceKey<EnchantmentTheme> assignedTheme = this.getThemeKey();
        ResourceKey<MagicSchool> assignedSchool = this.getSchoolKey();

        if (!this.bookInitialized
                && EnchantmentThemes.isCommon(assignedTheme)
                && MagicSchools.isAberration(assignedSchool)) {
            assignedTheme = rollRandomTheme(registryAccess, random);
            assignedSchool = rollRandomSchool(registryAccess, random);
        }

        ItemStack existingStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        boolean hadExistingStack = !existingStack.isEmpty();
        if (!hadExistingStack) {
            existingStack = this.createInitialBookStack(registryAccess, random, assignedTheme, assignedSchool);
        }

        Optional<ResourceKey<EnchantmentTheme>> resolvedTheme = ArcaneItemNamingUtil.resolveTheme(existingStack, registryAccess);
        Optional<ResourceKey<MagicSchool>> resolvedSchool = ArcaneItemNamingUtil.resolveSchool(existingStack, registryAccess);

        if (EnchantmentThemes.isCommon(assignedTheme) && resolvedTheme.isPresent()) {
            assignedTheme = resolvedTheme.get();
        }
        if (MagicSchools.isAberration(assignedSchool) && resolvedSchool.isPresent()) {
            assignedSchool = resolvedSchool.get();
        }
        if (hadExistingStack) {
            if (resolvedTheme.isPresent() && !EnchantmentThemes.isCommon(resolvedTheme.get())) {
                assignedTheme = resolvedTheme.get();
            }
            if (resolvedSchool.isPresent() && !MagicSchools.isAberration(resolvedSchool.get())) {
                assignedSchool = resolvedSchool.get();
            }
        }

        this.setThemeKey(assignedTheme);
        this.setSchoolKey(assignedSchool);
        ArcaneItemNamingUtil.setExplicitAttribution(existingStack, assignedTheme, assignedSchool);
        this.setItemSlot(EquipmentSlot.MAINHAND, existingStack);
        this.setDropChance(EquipmentSlot.MAINHAND, this.getMainHandDropChance());
        this.bookInitialized = true;
    }

    public static ResourceKey<EnchantmentTheme> rollRandomTheme(RegistryAccess registryAccess, RandomSource random) {
        var regOpt = registryAccess.registry(EnchantmentThemes.THEME_REGISTRY_KEY);
        if (regOpt.isPresent()) {
            List<ResourceKey<EnchantmentTheme>> keys = regOpt.get().registryKeySet().stream().toList();
            if (!keys.isEmpty()) {
                return keys.get(random.nextInt(keys.size()));
            }
        }
        return EnchantmentThemes.COMMON;
    }

    public static ResourceKey<EnchantmentTheme> rollPreferredTheme(
            RegistryAccess registryAccess,
            RandomSource random,
            @Nullable ResourceKey<EnchantmentTheme> preferredTheme
    ) {
        if (preferredTheme != null && random.nextFloat() < 0.72F) {
            return preferredTheme;
        }
        return rollRandomTheme(registryAccess, random);
    }

    public static ResourceKey<MagicSchool> rollRandomSchool(RegistryAccess registryAccess, RandomSource random) {
        var regOpt = registryAccess.registry(MagicSchools.REGISTRY_KEY);
        if (regOpt.isPresent()) {
            List<ResourceKey<MagicSchool>> keys = regOpt.get().registryKeySet().stream()
                    .filter(k -> !MagicSchools.isAberration(k))
                    .toList();
            if (!keys.isEmpty()) {
                return keys.get(random.nextInt(keys.size()));
            }
        }
        return MagicSchools.ABERRATION;
    }

    public static ResourceKey<MagicSchool> rollPreferredCycleSchool(
            RegistryAccess registryAccess,
            RandomSource random,
            @Nullable ResourceKey<MagicSchool> referenceSchool
    ) {
        var regOpt = registryAccess.registry(MagicSchools.REGISTRY_KEY);
        if (regOpt.isEmpty()) {
            return referenceSchool != null ? referenceSchool : MagicSchools.ABERRATION;
        }
        List<ResourceKey<MagicSchool>> allSchools = regOpt.get().registryKeySet().stream()
                .filter(k -> !MagicSchools.isAberration(k))
                .toList();
        if (allSchools.isEmpty()) {
            return MagicSchools.ABERRATION;
        }
        if (referenceSchool == null || MagicSchools.isAberration(referenceSchool)) {
            return allSchools.get(random.nextInt(allSchools.size()));
        }
        List<ResourceKey<MagicSchool>> cycleSchools = allSchools.stream()
                .filter(candidate -> MagicSchools.areInSameCycle(registryAccess, referenceSchool, candidate))
                .toList();
        if (!cycleSchools.isEmpty() && random.nextFloat() < 0.75F) {
            return cycleSchools.get(random.nextInt(cycleSchools.size()));
        }
        return allSchools.get(random.nextInt(allSchools.size()));
    }

    protected abstract ItemStack createInitialBookStack(
            RegistryAccess registryAccess,
            RandomSource random,
            ResourceKey<EnchantmentTheme> themeKey,
            ResourceKey<MagicSchool> schoolKey
    );

    @Override
    public @NotNull Component getName() {
        Component custom = this.getCustomName();
        if (custom != null) {
            return custom;
        }
        ItemStack held = this.getMainHandItem();
        if (!held.isEmpty()) {
            return ArcaneItemNamingUtil.formatItemName(held, this.getTypeName());
        }
        return super.getName();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        // Cap 3D A* node expansion to 25% of vanilla's FOLLOW_RANGE*16 budget
        navigation.setMaxVisitedNodesMultiplier(0.25F);
        return navigation;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Theme", this.getThemeKey().location().toString());
        compound.putString("School", this.getSchoolKey().location().toString());
        compound.putBoolean("BookInitialized", this.bookInitialized);
        compound.putInt("AngerTime", this.getAngerTime());
        compound.putInt("BarterCooldown", this.barterCooldown);
        if (this.angryAt != null) {
            compound.putUUID("AngryAt", this.angryAt);
        }
        BlockPos roost = this.getRoostPos();
        if (roost != null) {
            compound.putLong("RoostPos", roost.asLong());
        }
        if (!this.getBarterFuel().isEmpty()) {
            compound.put("BarterFuel", this.getBarterFuel().saveOptional(this.registryAccess()));
        }
        if (!this.getBarterCatalyst().isEmpty()) {
            compound.put("BarterCatalyst", this.getBarterCatalyst().saveOptional(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Theme")) {
            ResourceLocation themeLoc = ResourceLocation.tryParse(compound.getString("Theme"));
            if (themeLoc != null) {
                this.setThemeKey(ResourceKey.create(EnchantmentThemes.THEME_REGISTRY_KEY, themeLoc));
            }
        }
        if (compound.contains("School")) {
            ResourceLocation schoolLoc = ResourceLocation.tryParse(compound.getString("School"));
            if (schoolLoc != null) {
                this.setSchoolKey(ResourceKey.create(MagicSchools.REGISTRY_KEY, schoolLoc));
            }
        }
        this.bookInitialized = compound.getBoolean("BookInitialized");
        this.setAngerTime(compound.getInt("AngerTime"));
        this.barterCooldown = compound.getInt("BarterCooldown");
        if (compound.hasUUID("AngryAt")) {
            this.angryAt = compound.getUUID("AngryAt");
        } else {
            this.angryAt = null;
        }
        if (compound.contains("RoostPos")) {
            this.setRoostPos(BlockPos.of(compound.getLong("RoostPos")));
        } else {
            this.setRoostPos(null);
        }
        if (compound.contains("BarterFuel")) {
            this.setBarterFuel(ItemStack.parseOptional(this.registryAccess(), compound.getCompound("BarterFuel")));
        } else {
            this.setBarterFuel(ItemStack.EMPTY);
        }
        if (compound.contains("BarterCatalyst")) {
            this.setBarterCatalyst(ItemStack.parseOptional(this.registryAccess(), compound.getCompound("BarterCatalyst")));
        } else {
            this.setBarterCatalyst(ItemStack.EMPTY);
        }
        this.setDropChance(EquipmentSlot.MAINHAND, this.getMainHandDropChance());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && this.isCoverClosed()) {
            Entity direct = source.getDirectEntity();
            if (direct instanceof AbstractArrow || source.is(DamageTypeTags.IS_PROJECTILE)) {
                if (direct != null) {
                    direct.setDeltaMovement(direct.getDeltaMovement().scale(-0.5D));
                }
                this.level().playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.SHIELD_BLOCK,
                        this.getSoundSource(),
                        0.8F,
                        1.1F + this.random.nextFloat() * 0.2F
                );
                return false;
            }
            amount *= 0.5F;
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.ARMOR_EQUIP_IRON.value(),
                    this.getSoundSource(),
                    0.7F,
                    0.9F + this.random.nextFloat() * 0.2F
            );
        }

        boolean damageApplied = super.hurt(source, amount);
        if (damageApplied && !this.level().isClientSide && this.isAlive()) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            if (source.getEntity() instanceof LivingEntity attacker) {
                this.provoke(attacker, 600);
                if (BookMoveControl.isTargetRanged(attacker) || !BookMoveControl.canAscendFromMelee(this, attacker)) {
                    if (this.hurtDashCooldown <= 0) {
                        this.pendingHurtDash = true;
                    }
                } else {
                    Vec3 vel = this.getDeltaMovement();
                    this.setDeltaMovement(vel.x, Math.max(vel.y, 0.24D), vel.z);
                }
            }

            float yield = amount * 0.05F;
            if (source.getEntity() instanceof LivingEntity attacker) {
                var registry = this.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                var looting = registry.getHolder(Enchantments.LOOTING);
                if (looting.isPresent()) {
                    int lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(looting.get(), attacker.getMainHandItem());
                    yield += lootingLevel * 0.15F;
                }
            }
            if (this.isWideOpen()) {
                yield *= 2.0F;
            }
            int guaranteedDrops = (int) yield;
            float remainder = yield - guaranteedDrops;
            int totalDrops = guaranteedDrops + (this.random.nextFloat() < remainder ? 1 : 0);

            if (totalDrops > 0) {
                for (int i = 0; i < totalDrops; i++) {
                    ItemStack pageDrop = this.createShedPageStack(serverLevel);
                    if (!pageDrop.isEmpty()) {
                        this.spawnAtLocation(pageDrop);
                    }
                }
                serverLevel.sendParticles(
                        SimpleParticleTypes.SGA_RUNE.type(),
                        this.getX(),
                        this.getY() + this.getBbHeight() * 0.5D,
                        this.getZ(),
                        12,
                        0.3D,
                        0.3D,
                        0.3D,
                        0.05D
                );
            }
        }
        return damageApplied;
    }

    public ItemStack createShedPageStack(ServerLevel level) {
        ItemStack page = new ItemStack(Items.ENCHANTED_PAGE.get());
        ItemStack mainHand = this.getMainHandItem();
        if (!mainHand.isEmpty()) {
            ItemEnchantments stored = mainHand.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (stored.isEmpty()) {
                stored = EnchantmentLib.getEnchantments(mainHand);
            }
            if (!stored.isEmpty()) {
                List<Holder<Enchantment>> holders = new ArrayList<>(stored.keySet());
                Holder<Enchantment> chosen = holders.get(this.random.nextInt(holders.size()));
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                mutable.set(chosen, chosen.value().getMinLevel());
                page.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
                return page;
            }
        }
        return EnchantedPageItem.enchantWithThemeAndSchool(
                page,
                level.registryAccess(),
                this.random,
                this.getThemeKey(),
                this.getSchoolKey()
        );
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer) {
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean hitByPlayer) {
        super.dropCustomDeathLoot(level, damageSource, hitByPlayer);
        this.ejectBarterItems();
        int lootingLevel = 0;
        if (damageSource.getEntity() instanceof LivingEntity attacker) {
            var registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            var looting = registry.getHolder(Enchantments.LOOTING);
            if (looting.isPresent()) {
                lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(looting.get(), attacker.getMainHandItem());
            }
        }
        int totalDrops = this.getBaseDeathPageCount() + (lootingLevel > 0 ? this.random.nextInt(lootingLevel + 1) : 0);
        for (int i = 0; i < totalDrops; i++) {
            ItemStack page = this.createShedPageStack(level);
            if (!page.isEmpty()) {
                this.spawnAtLocation(page);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        BookAnimationState state = this.getAnimationState();
        // Only run altar-vigil animation & player proximity checks when actually in ALTAR_VIGIL
        if (state == BookAnimationState.ALTAR_VIGIL) {
            this.tickAltarVigilAnimation();
        }

        float targetOpenness = state.openness;
        float opennessLerpSpeed = 0.15F;
        if (state == BookAnimationState.DASHING) {
            int dashTick = this.level().isClientSide() ? this.clientStateTicks : this.serverStateTicks;
            if (dashTick < 4) {
                targetOpenness = 1.0F;
                opennessLerpSpeed = 0.55F;
            } else {
                targetOpenness = 0.0F;
                opennessLerpSpeed = 0.65F;
            }
        }
        this.animOpennessO = this.animOpenness;
        this.animOpenness = Mth.lerp(opennessLerpSpeed, this.animOpenness, targetOpenness);

        if (!this.level().isClientSide()) {
            this.serverStateTicks++;
            if (this.hurtDashCooldown > 0) {
                this.hurtDashCooldown--;
            }
            if (this.barterCooldown > 0) {
                this.barterCooldown--;
            }
            if (!this.bookInitialized) {
                this.initializeBookIdentityAndStack(this.level().registryAccess(), this.random);
            }
            if (this.getAngerTime() > 0) {
                this.setAngerTime(this.getAngerTime() - 1);
                if (this.getAngerTime() == 0) {
                    this.angryAt = null;
                }
            }
            LivingEntity currentTarget = this.getTarget();
            if (currentTarget != null && (this.isBookAlly(currentTarget) || this.isNeutralEntity(currentTarget))) {
                this.setTarget(null);
            }
            if (!this.isRoosting() && state != BookAnimationState.DASHING) {
                if (this.getTarget() != null
                        || state == BookAnimationState.TARGET_ACQUISITION
                        || state.isChanting()
                        || state == BookAnimationState.BARTERING) {
                    this.setYRot(this.yHeadRot);
                    this.yBodyRot = this.yHeadRot;
                }
            }
            if (state.isChanting() && this.isAlive()) {
                this.tickServerChantingSounds();
            }
            if (this.isAlive() && this.level() instanceof ServerLevel serverLevel) {
                this.tickAttachedSpellFields(serverLevel);
            }
            this.updateClosedCoverArmorAttribute();
        } else {
            if (state != this.clientLastState) {
                this.clientStateTicks = 0;
                this.clientLastState = state;
            } else {
                this.clientStateTicks++;
            }

            if (!this.isRoosting() && state != BookAnimationState.DASHING) {
                if (state == BookAnimationState.TARGET_ACQUISITION
                        || state.isChanting()
                        || state == BookAnimationState.BARTERING) {
                    this.setYRot(this.yHeadRot);
                    this.yBodyRot = this.yHeadRot;
                }
            }

            float lerpSpeed = state == BookAnimationState.DASHING ? 0.35F : 0.15F;
            this.animXO = this.animX;
            this.animX = Mth.lerp(lerpSpeed, this.animX, state.xOffset);
            this.animYO = this.animY;
            this.animY = Mth.lerp(lerpSpeed, this.animY, state.yOffset);
            this.animZO = this.animZ;
            this.animZ = Mth.lerp(lerpSpeed, this.animZ, state.zOffset);
            this.flutterPhase += state.flutterSpeed;

            this.tickPageFlipAnimation(state);

            Quaternionf targetRot = state.getTargetRotation(this.getXRot());
            this.animRotationO.set(this.animRotation);
            this.animRotation.slerp(targetRot, lerpSpeed);

            if (this.isAlive() && !this.isRoosting()) {
                this.tickClientSchoolParticles(state);
                this.tickClientThemeEffects(state);
            }
        }
    }

    private void tickServerChantingSounds() {
        float progress = Mth.clamp((float) this.serverStateTicks / 40.0F, 0.0F, 1.0F);
        if (this.serverStateTicks == 1) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    this.getSoundSource(),
                    0.75F,
                    0.75F + this.random.nextFloat() * 0.1F
            );
        }
        if (this.serverStateTicks % 6 == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.BOOK_PAGE_TURN,
                    this.getSoundSource(),
                    0.65F,
                    0.95F + progress * 0.65F
            );
        }
        if (this.serverStateTicks % 10 == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    this.getSoundSource(),
                    0.85F,
                    0.85F + progress * 0.75F
            );
        }
    }

    private void tickPageFlipAnimation(BookAnimationState state) {
        this.oPageFlip = this.pageFlip;
        if (state.flutterChance > 0.0F && this.random.nextFloat() < state.flutterChance) {
            float prevTarget = this.pageFlipT;
            do {
                this.pageFlipT += (float) (this.random.nextInt(4) - this.random.nextInt(4));
            } while (prevTarget == this.pageFlipT);
        }
        float delta = (this.pageFlipT - this.pageFlip) * 0.4F;
        delta = Mth.clamp(delta, -0.2F, 0.2F);
        this.pageFlipA += (delta - this.pageFlipA) * 0.9F;
        this.pageFlip += this.pageFlipA;
    }

    private void tickAltarVigilAnimation() {
        this.altarOOpen = this.altarOpen;
        this.altarORot = this.altarRot;

        BlockPos roost = this.getRoostPos();
        double cx = roost != null ? roost.getX() + 0.5D : this.getX();
        double cy = roost != null ? roost.getY() + 0.5D : this.getY();
        double cz = roost != null ? roost.getZ() + 0.5D : this.getZ();

        // Throttle server-side getNearestPlayer lookup to once every 5 ticks
        if (this.level().isClientSide() || --this.altarPlayerCheckCooldown <= 0) {
            this.altarPlayerCheckCooldown = 5;
            Player player = this.level().getNearestPlayer(cx, cy, cz, 3.0D, false);
            if (player != null) {
                double d0 = player.getX() - cx;
                double d1 = player.getZ() - cz;
                this.altarTRot = (float) Mth.atan2(d1, d0);
                this.cachedAltarPlayerNearby = true;
            } else {
                this.cachedAltarPlayerNearby = false;
            }
        }

        if (this.cachedAltarPlayerNearby) {
            this.altarOpen += 0.1F;
            if (this.altarOpen < 0.5F || this.random.nextInt(40) == 0) {
                float f1 = this.altarFlipT;
                do {
                    this.altarFlipT += (float) (this.random.nextInt(4) - this.random.nextInt(4));
                } while (f1 == this.altarFlipT);
            }
        } else {
            this.altarTRot -= 0.02F;
            this.altarOpen -= 0.1F;
        }

        while (this.altarRot >= (float) Math.PI) {
            this.altarRot -= ((float) Math.PI * 2.0F);
        }
        while (this.altarRot < -(float) Math.PI) {
            this.altarRot += ((float) Math.PI * 2.0F);
        }
        while (this.altarTRot >= (float) Math.PI) {
            this.altarTRot -= ((float) Math.PI * 2.0F);
        }
        while (this.altarTRot < -(float) Math.PI) {
            this.altarTRot += ((float) Math.PI * 2.0F);
        }

        float f2 = this.altarTRot - this.altarRot;
        while (f2 >= (float) Math.PI) {
            f2 -= ((float) Math.PI * 2.0F);
        }
        while (f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2.0F);
        }

        this.altarRot += f2 * 0.4F;
        this.altarOpen = Mth.clamp(this.altarOpen, 0.0F, 1.0F);
        this.altarTime++;
        this.altarOFlip = this.altarFlip;
        float f = (this.altarFlipT - this.altarFlip) * 0.4F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        this.altarFlipA += (f - this.altarFlipA) * 0.9F;
        this.altarFlip += this.altarFlipA;
    }

    private void updateClosedCoverArmorAttribute() {
        AttributeInstance armorAttr = this.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;
        boolean closed = this.isCoverClosed();
        boolean hasModifier = armorAttr.hasModifier(CLOSED_COVER_ARMOR_ID);
        if (closed && !hasModifier) {
            armorAttr.addTransientModifier(CLOSED_COVER_ARMOR_MODIFIER);
        } else if (!closed && hasModifier) {
            armorAttr.removeModifier(CLOSED_COVER_ARMOR_ID);
        }
    }

    public Vec3 getChantingDirection() {
        if (this.getAnimationState() == BookAnimationState.CHANTING_DEFENSIVE) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }
        Vec3 look = this.getLookAngle();
        if (look.lengthSqr() < 0.0001D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }
        return look.normalize();
    }

    public Vec3 getChantingFocalPoint() {
        Vec3 look = this.getChantingDirection();
        double floatBob = Math.sin((this.tickCount + this.getId() * 5) * 0.18D) * 0.06D;
        double forwardDist = 0.75D;
        return new Vec3(
                this.getX() + look.x * forwardDist,
                this.getY() + this.getBbHeight() * 0.55D + look.y * forwardDist + floatBob,
                this.getZ() + look.z * forwardDist
        );
    }

    private void tickClientSchoolParticles(BookAnimationState state) {
        if (!state.shouldGenerateParticles || this.isRoosting()) {
            return;
        }
        int schoolColor = this.getSchoolColor();
        double centerX = this.getX();
        double centerY = this.getY() + this.getBbHeight() * 0.55D;
        double centerZ = this.getZ();

        if (state.isChanting()) {
            int chantColor = state == BookAnimationState.CHANTING ? 0xFFFFFF : this.getCastSchoolColor();
            float progress = Mth.clamp((float) this.clientStateTicks / 40.0F, 0.0F, 1.0F);
            Vec3 look = this.getChantingDirection();
            Vec3 focus = this.getChantingFocalPoint();

            Vec3 worldUp = Math.abs(look.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = look.cross(worldUp).normalize();
            Vec3 planeUp = right.cross(look).normalize();

            double radius = Mth.lerp(progress, 0.95D, 0.14D);
            double angularSpeed = Mth.lerp(progress, 0.18D, 0.52D);
            double baseAngle = this.clientStateTicks * angularSpeed;

            int arms = 2;
            for (int i = 0; i < arms; i++) {
                double angle = baseAngle + i * Math.PI;
                double localX = Math.cos(angle) * radius;
                double localY = Math.sin(angle) * radius;
                double axialWave = Math.sin(angle * 2.0D + this.clientStateTicks * 0.15D) * 0.06D * (1.0D - progress * 0.6D);

                Vec3 spawnPos = focus
                        .add(right.scale(localX))
                        .add(planeUp.scale(localY))
                        .add(look.scale(axialWave));
                Vec3 vel = focus.subtract(spawnPos).scale(0.095D);
                SgaRuneParticle.spawnTinted(this.level(), spawnPos.x, spawnPos.y, spawnPos.z, vel.x, vel.y, vel.z, chantColor);
            }

            if (progress > 0.65F) {
                double jitter = (1.0D - progress) * 0.22D + 0.04D;
                double rx = (this.random.nextDouble() - 0.5D) * jitter;
                double ry = (this.random.nextDouble() - 0.5D) * jitter;
                Vec3 corePos = focus.add(right.scale(rx)).add(planeUp.scale(ry));
                SgaRuneParticle.spawnTinted(this.level(), corePos.x, corePos.y, corePos.z, 0.0D, 0.005D, 0.0D, chantColor);
            }
        } else if (state == BookAnimationState.BARTERING) {
            double angle = (this.tickCount * 0.25D) + (this.random.nextDouble() * Math.PI * 2.0D);
            double radius = 0.45D;
            double px = centerX + Math.cos(angle) * radius;
            double py = centerY + (this.random.nextDouble() - 0.5D) * 0.2D;
            double pz = centerZ + Math.sin(angle) * radius;
            SgaRuneParticle.spawnTinted(this.level(), px, py, pz, 0.0D, 0.02D, 0.0D, schoolColor);
        } else if (state == BookAnimationState.DASHING) {
            if (this.clientStateTicks == 4) {
                Vec3 backDir = this.getDeltaMovement().lengthSqr() > 0.001D
                        ? this.getDeltaMovement().normalize().scale(-1.0D)
                        : this.getLookAngle().scale(-1.0D);
                for (int i = 0; i < 8; i++) {
                    double angle = i * (Math.PI * 2.0D / 8.0D);
                    double rx = Math.cos(angle) * 0.22D;
                    double rz = Math.sin(angle) * 0.22D;
                    SgaRuneParticle.spawnTinted(
                            this.level(),
                            centerX + backDir.x * 0.2D + rx,
                            centerY + backDir.y * 0.2D,
                            centerZ + backDir.z * 0.2D + rz,
                            backDir.x * 0.08D + rx * 0.08D,
                            backDir.y * 0.08D,
                            backDir.z * 0.08D + rz * 0.08D,
                            schoolColor
                    );
                }
            } else if (this.clientStateTicks > 4 && this.clientStateTicks % 2 == 0) {
                SgaRuneParticle.spawnTinted(this.level(), centerX, centerY, centerZ, 0.0D, 0.0D, 0.0D, schoolColor);
            }
        } else if (this.random.nextInt(3) == 0) {
            double dx = (this.random.nextDouble() - 0.5D) * this.getBbWidth();
            double dy = (this.random.nextDouble() - 0.5D) * this.getBbHeight();
            double dz = (this.random.nextDouble() - 0.5D) * this.getBbWidth();
            SgaRuneParticle.spawnTinted(
                    this.level(),
                    centerX + dx,
                    centerY + dy,
                    centerZ + dz,
                    0.0D,
                    0.01D,
                    0.0D,
                    schoolColor
            );
        }
    }

    private void tickClientThemeEffects(BookAnimationState state) {
        if (this.isRoosting()) {
            return;
        }
        Optional<EnchantmentTheme> themeOpt = this.getTheme();
        if (themeOpt.isEmpty() || themeOpt.get().effects().isEmpty()) {
            return;
        }
        EffectData effectData = themeOpt.get().effects().get();
        if (effectData.particle().isEmpty() && effectData.sound().isEmpty()) {
            return;
        }

        int chance = Math.max(4, effectData.chance() * 2);
        int iterations = Math.min(4, Math.max(1, effectData.iterations() / 8));
        Optional<ParticleEffectData> particleOpt = effectData.particle();
        Optional<SoundEffectData> soundOpt = effectData.sound();

        for (int i = 0; i < iterations; i++) {
            if (this.random.nextInt(chance) != 0) {
                continue;
            }
            double spawnX = this.getX();
            double spawnY = this.getY() + this.getBbHeight() * 0.5D;
            double spawnZ = this.getZ();

            if (particleOpt.isPresent() && particleOpt.get().effect() instanceof ParticleOptions particleOptions) {
                ParticleEffectData p = particleOpt.get();
                double offsetX = p.offset().x * 0.5D + (this.random.nextDouble() * 2.0D - 1.0D) * p.offsetVariance().x * 0.6D;
                double offsetY = p.offset().y * 0.5D + (this.random.nextDouble() * 2.0D - 1.0D) * p.offsetVariance().y * 0.6D;
                double offsetZ = p.offset().z * 0.5D + (this.random.nextDouble() * 2.0D - 1.0D) * p.offsetVariance().z * 0.6D;

                double velX = p.velocity().x + (this.random.nextDouble() * 2.0D - 1.0D) * p.velocityVariance().x;
                double velY = p.velocity().y + (this.random.nextDouble() * 2.0D - 1.0D) * p.velocityVariance().y;
                double velZ = p.velocity().z + (this.random.nextDouble() * 2.0D - 1.0D) * p.velocityVariance().z;

                this.level().addParticle(particleOptions, spawnX + offsetX, spawnY + offsetY, spawnZ + offsetZ, velX, velY, velZ);
            }

            if (soundOpt.isPresent() && this.random.nextInt(4) == 0) {
                SoundEffectData s = soundOpt.get();
                float pitch = s.pitch() + (this.random.nextFloat() * 2.0F - 1.0F) * s.pitchVariance();
                float volume = Math.max(0.05F, (s.volume() + (this.random.nextFloat() * 2.0F - 1.0F) * s.volumeVariance()) * 0.5F);
                this.level().playLocalSound(spawnX, spawnY, spawnZ, s.effect().value(), SoundSource.HOSTILE, volume, pitch, false);
            }
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        var effect = effectInstance.getEffect();
        if (effect.equals(MobEffects.POISON)
                || effect.equals(MobEffects.REGENERATION)
                || effect.equals(MobEffects.OOZING)
                || effect.equals(MobEffects.INFESTED)) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public void playAmbientSound() {
        if (this.isRoosting()) {
            return;
        }
        super.playAmbientSound();
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public float getVoicePitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.isRoosting()) {
            return null;
        }
        float enchantmentSoundWeight = 0.16F;
        if (this.random.nextFloat() < enchantmentSoundWeight) {
            return SoundEvents.ENCHANTMENT_TABLE_USE;
        }
        return SoundEvents.BOOK_PAGE_TURN;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BOOK_PAGE_TURN;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BOOK_PAGE_TURN;
    }

    protected abstract ResourceKey<LootTable> getHurtLootTable();

    protected abstract int getBaseDeathPageCount();
}