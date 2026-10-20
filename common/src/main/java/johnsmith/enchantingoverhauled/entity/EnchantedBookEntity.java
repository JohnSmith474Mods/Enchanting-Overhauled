package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.entity.ai.goal.CastSpellGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.DashGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.FlyingBookIdleGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.MinionShelvingDespawnGoal;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Flying Enchanted Book entity that spawns naturally in Stronghold Libraries or is summoned
 * from bookshelves by {@link EnchantedTomeEntity}.
 * <p>
 * Optimized to remove redundant {@code WaterAvoidingRandomFlyingGoal}, bound {@code FOLLOW_RANGE}
 * to 32.0D, and throttle target acquisition intervals.
 */
public class EnchantedBookEntity extends AbstractFlyingBookEntity {
    public static final int NATURAL_SPAWN_WEIGHT = 5;
    public static final int NATURAL_SPAWN_MIN_GROUP = 1;
    public static final int NATURAL_SPAWN_MAX_GROUP = 4;

    private static final EntityDataAccessor<Optional<UUID>> DATA_COMMANDER_UUID =
            SynchedEntityData.defineId(EnchantedBookEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private int deescalationTimer = MinionShelvingDespawnGoal.DEESCALATION_DURATION_TICKS;
    private boolean engagedInCombat = false;
    @Nullable
    private BlockPos homeShelfPos;

    public EnchantedBookEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkEnchantedBookSpawnRules(
            EntityType<EnchantedBookEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return super.checkSpawnRules(level, spawnType);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.hasCustomName();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return NATURAL_SPAWN_MAX_GROUP;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COMMANDER_UUID, Optional.empty());
    }

    public Optional<UUID> getCommanderUuid() {
        return this.entityData.get(DATA_COMMANDER_UUID);
    }

    public void setCommanderUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_COMMANDER_UUID, Optional.ofNullable(uuid));
    }

    public int getDeescalationTimer() {
        return this.deescalationTimer;
    }

    public void setDeescalationTimer(int ticks) {
        this.deescalationTimer = Math.max(0, ticks);
    }

    public boolean wasEngagedInCombat() {
        return this.engagedInCombat;
    }

    public void setEngagedInCombat(boolean engaged) {
        this.engagedInCombat = engaged;
    }

    public void dismissByCommander() {
        this.setTarget(null);
        this.setAngerTime(0);
        this.setAngryAt(null);
        this.deescalationTimer = 0;
    }

    @Nullable
    public BlockPos getHomeShelfPos() {
        return this.homeShelfPos;
    }

    public void setHomeShelfPos(@Nullable BlockPos pos) {
        this.homeShelfPos = pos;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new DashGoal(this, 1.5D, 3.5F, 24.0F, 15));
        this.goalSelector.addGoal(3, new CastSpellGoal(this, 1.0D, 40, 80, 3.5F, 18.0F));
        this.goalSelector.addGoal(4, new MinionShelvingDespawnGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new FlyingBookIdleGoal(this, 1.0D, 24, 12));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractFlyingBookEntity.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 15, true, false, this::isValidHostileTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 20, true, false, this::isValidHostileTarget));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.getTarget() != null || this.isAngry()) {
                this.engagedInCombat = true;
                this.deescalationTimer = MinionShelvingDespawnGoal.DEESCALATION_DURATION_TICKS;
            } else if (this.deescalationTimer > 0 && (this.getCommanderUuid().isPresent() || this.homeShelfPos != null)) {
                this.deescalationTimer--;
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.ENCHANTED_BOOK_HP.get())
                .add(Attributes.ARMOR, Config.ENCHANTED_BOOK_ARMOR.get())
                .add(Attributes.ARMOR_TOUGHNESS, Config.ENCHANTED_BOOK_ARMOR_TOUGHNESS.get())
                .add(Attributes.FLYING_SPEED, Config.ENCHANTED_BOOK_FLYING_SPEED.get())
                .add(Attributes.MOVEMENT_SPEED, Config.ENCHANTED_BOOK_MOVEMENT_SPEED.get())
                .add(Attributes.FOLLOW_RANGE, Config.ENCHANTED_BOOK_FOLLOW_RANGE.get());
    }

    @Override
    protected ItemStack createInitialBookStack(
            RegistryAccess registryAccess,
            RandomSource random,
            ResourceKey<EnchantmentTheme> themeKey,
            ResourceKey<MagicSchool> schoolKey
    ) {
        return createBookStackForThemeAndSchool(registryAccess, random, themeKey, schoolKey);
    }

    public static ItemStack createBookStackForThemeAndSchool(
            RegistryAccess registryAccess,
            RandomSource random,
            ResourceKey<EnchantmentTheme> themeKey,
            ResourceKey<MagicSchool> schoolKey
    ) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        Registry<Enchantment> registry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> allHolders = registry.holders()
                .map(h -> (Holder<Enchantment>) h)
                .collect(Collectors.toList());
        if (allHolders.isEmpty()) {
            return stack;
        }

        boolean hasSpecificTheme = !EnchantmentThemes.isCommon(themeKey);
        boolean hasSpecificSchool = !MagicSchools.isAberration(schoolKey);

        List<Holder<Enchantment>> bothMatches = new ArrayList<>();
        List<Holder<Enchantment>> primaryMatches = new ArrayList<>();
        List<Holder<Enchantment>> fallbackMatches = new ArrayList<>();

        for (Holder<Enchantment> holder : allHolders) {
            boolean themeMatch = hasSpecificTheme && holder.is(EnchantmentThemes.getThemeTag(themeKey));
            boolean schoolMatch = hasSpecificSchool && holder.is(MagicSchools.getSchoolTag(schoolKey));
            if (themeMatch && schoolMatch) {
                bothMatches.add(holder);
            } else if (themeMatch || schoolMatch) {
                primaryMatches.add(holder);
            } else {
                fallbackMatches.add(holder);
            }
        }

        Util.shuffle(bothMatches, random);
        Util.shuffle(primaryMatches, random);
        Util.shuffle(fallbackMatches, random);

        List<Holder<Enchantment>> ordered = new ArrayList<>(allHolders.size());
        ordered.addAll(bothMatches);
        ordered.addAll(primaryMatches);
        ordered.addAll(fallbackMatches);

        int targetCount = 1 + random.nextInt(2);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        List<Holder<Enchantment>> chosen = new ArrayList<>();

        for (Holder<Enchantment> candidate : ordered) {
            if (chosen.size() >= targetCount) {
                break;
            }
            boolean compatible = true;
            for (Holder<Enchantment> existing : chosen) {
                if (existing.equals(candidate) || !Enchantment.areCompatible(existing, candidate)) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible) {
                continue;
            }
            int level = Mth.nextInt(random, candidate.value().getMinLevel(), candidate.value().getMaxLevel());
            builder.set(candidate, level);
            chosen.add(candidate);
        }

        if (!chosen.isEmpty()) {
            stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        }
        return stack;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.getCommanderUuid().ifPresent(uuid -> compound.putUUID("CommanderUUID", uuid));
        compound.putInt("DeescalationTimer", this.deescalationTimer);
        compound.putBoolean("EngagedInCombat", this.engagedInCombat);
        if (this.homeShelfPos != null) {
            compound.putLong("HomeShelfPos", this.homeShelfPos.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("CommanderUUID")) {
            this.setCommanderUuid(compound.getUUID("CommanderUUID"));
        } else {
            this.setCommanderUuid(null);
        }
        if (compound.contains("DeescalationTimer")) {
            this.deescalationTimer = compound.getInt("DeescalationTimer");
        }
        this.engagedInCombat = compound.getBoolean("EngagedInCombat");
        if (compound.contains("HomeShelfPos")) {
            this.homeShelfPos = BlockPos.of(compound.getLong("HomeShelfPos"));
        } else {
            this.homeShelfPos = null;
        }
    }

    @Override
    protected ResourceKey<LootTable> getHurtLootTable() {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "entities/enchanted_book_hurt"));
    }

    @Override
    protected int getBaseDeathPageCount() {
        return 1;
    }
}