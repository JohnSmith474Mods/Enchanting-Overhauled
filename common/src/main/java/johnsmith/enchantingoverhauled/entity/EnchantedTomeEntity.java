package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.client.particle.SgaRuneParticle;
import johnsmith.enchantingoverhauled.entity.ai.goal.CastSpellGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.DashGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.FlyingBookIdleGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.TomeBarterGoal;
import johnsmith.enchantingoverhauled.entity.ai.goal.TomeSummonMinionsGoal;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.item.EnchantedTomeItem;
import johnsmith.enchantingoverhauled.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Commander flying book entity that can roost on deactivated Enchanting Altars, summon
 * {@link EnchantedBookEntity} minions from bookshelves, and barter with players.
 * <p>
 * Assigned a unique persistent UUID upon spawn for command targeting, never despawns naturally,
 * and is completely impervious to explosions.
 */
public class EnchantedTomeEntity extends AbstractFlyingBookEntity {
    public static final int ALTAR_DECEPTION_DURATION_TICKS = 1200;
    private static final double LOCAL_TOME_SPAWN_CHECK_RADIUS = 48.0D;

    private int boredomTicks = 0;
    private int altarDeceptionTicks = ALTAR_DECEPTION_DURATION_TICKS;
    private boolean altarDeceptionCountdownActive = false;
    private int altarAbandonCooldown = 0;

    public EnchantedTomeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setUUID(UUID.randomUUID());
        this.setPersistenceRequired();
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        this.setUUID(UUID.randomUUID());
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean ignoreExplosion(@NotNull Explosion explosion) {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || super.isInvulnerableTo(source);
    }

    public int getBoredomTicks() {
        return this.boredomTicks;
    }

    public void setBoredomTicks(int ticks) {
        this.boredomTicks = Math.max(0, ticks);
    }

    public int getAltarDeceptionTicks() {
        return this.altarDeceptionTicks;
    }

    public boolean isAltarDeceptionExpired() {
        return this.altarDeceptionCountdownActive && this.altarDeceptionTicks <= 0;
    }

    public int getAltarAbandonCooldown() {
        return this.altarAbandonCooldown;
    }

    public void resetAltarDeception() {
        this.altarDeceptionTicks = ALTAR_DECEPTION_DURATION_TICKS;
        this.altarDeceptionCountdownActive = false;
    }

    public boolean isInAltarDeceptionGracePeriod() {
        if (this.isAngry() || this.altarAbandonCooldown > 0) {
            return false;
        }
        if (this.isAltarDeceptionExpired()) {
            return false;
        }
        BookAnimationState state = this.getAnimationState();
        if (state == BookAnimationState.ALTAR_VIGIL || state == BookAnimationState.BARTERING) {
            return true;
        }
        BlockPos roost = this.getRoostPos();
        return roost != null && this.isLoadedAltarBlock(roost);
    }

    private boolean isLoadedAltarBlock(BlockPos pos) {
        LevelChunk chunk = this.level().getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );
        if (chunk == null) {
            return false;
        }
        BlockState blockState = chunk.getBlockState(pos);
        return blockState.is(Blocks.DEACTIVATED_ENCHANTING_TABLE.build())
                || blockState.is(Blocks.DISTURBED_ENCHANTING_TABLE.build());
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (spawnType == MobSpawnType.NATURAL) {
            return false;
        }
        if (!super.checkSpawnRules(level, spawnType)) {
            return false;
        }
        if (spawnType == MobSpawnType.STRUCTURE
                || spawnType == MobSpawnType.CHUNK_GENERATION) {
            AABB checkBox = this.getBoundingBox().inflate(LOCAL_TOME_SPAWN_CHECK_RADIUS, 32.0D, LOCAL_TOME_SPAWN_CHECK_RADIUS);
            List<EnchantedTomeEntity> nearbyUnnamedTomes = level.getEntitiesOfClass(
                    EnchantedTomeEntity.class,
                    checkBox,
                    tome -> tome != this && tome.isAlive() && !tome.hasCustomName()
            );
            int count = nearbyUnnamedTomes.size();
            if (count >= 3) {
                return false;
            }
            if (count == 2) {
                return this.random.nextFloat() < 0.04F;
            }
            if (count == 1) {
                return this.random.nextFloat() < 0.12F;
            }
        }
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TomeSummonMinionsGoal(this, 1.2D));
        this.goalSelector.addGoal(3, new DashGoal(this, 1.5D, 3.5F, 24.0F, 15));
        this.goalSelector.addGoal(4, new CastSpellGoal(this, 1.0D, 40, 80, 3.5F, 18.0F));
        this.goalSelector.addGoal(5, new TomeBarterGoal(this));
        this.goalSelector.addGoal(6, new FlyingBookIdleGoal(this, 1.0D, 24, 12));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, AbstractFlyingBookEntity.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 15, true, false, this::isValidHostileTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 20, true, false, this::isValidHostileTarget));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.isAlive()) {
            this.tickServerAltarDeception();
            this.tickServerCommanderDismissal();
            // Allow unroosted Tomes in open terrain to accumulate daytime boredom and scout
            if (!this.isRoosting()
                    && this.getRoostPos() == null
                    && this.getTarget() == null
                    && !this.isAngry()
                    && this.level().isDay()
                    && this.getAnimationState() == BookAnimationState.IDLE) {
                this.boredomTicks++;
            }
        } else if (this.level().isClientSide() && this.isAlive()) {
            this.tickClientCommanderRuneTransfer();
        }
    }

    private void tickServerAltarDeception() {
        if (this.altarAbandonCooldown > 0) {
            this.altarAbandonCooldown--;
        }
        if (this.isAngry()) {
            this.altarDeceptionTicks = 0;
            this.altarDeceptionCountdownActive = false;
            return;
        }

        BlockPos roost = this.getRoostPos();
        BookAnimationState state = this.getAnimationState();
        boolean onAltarOrBartering = state == BookAnimationState.ALTAR_VIGIL
                || (state == BookAnimationState.BARTERING && this.altarDeceptionCountdownActive)
                || (roost != null && this.isLoadedAltarBlock(roost));

        if (!onAltarOrBartering) {
            if (this.altarAbandonCooldown <= 0) {
                this.resetAltarDeception();
            }
            return;
        }

        double cx = roost != null ? roost.getX() + 0.5D : this.getX();
        double cy = roost != null ? roost.getY() + 0.5D : this.getY();
        double cz = roost != null ? roost.getZ() + 0.5D : this.getZ();
        boolean checkPlayerThisTick = (this.tickCount + this.getId()) % 5 == 0;

        if (!this.altarDeceptionCountdownActive) {
            if (checkPlayerThisTick) {
                Player facingPlayer = this.level().getNearestPlayer(cx, cy, cz, 3.0D, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                if (facingPlayer != null) {
                    this.altarDeceptionCountdownActive = true;
                    this.altarDeceptionTicks = ALTAR_DECEPTION_DURATION_TICKS;
                }
            }
        } else {
            if (checkPlayerThisTick) {
                Player nearbyPlayer = this.level().getNearestPlayer(cx, cy, cz, 16.0D, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                if (nearbyPlayer == null) {
                    this.resetAltarDeception();
                    return;
                }
            }
            if (this.altarDeceptionTicks > 0) {
                this.altarDeceptionTicks--;
            }
            if (this.altarDeceptionTicks <= 0) {
                this.altarAbandonCooldown = 600;
                this.setRoostPos(null);
                if (state == BookAnimationState.ALTAR_VIGIL) {
                    this.setAnimationState(BookAnimationState.IDLE);
                }
            }
        }
    }

    private void tickServerCommanderDismissal() {
        if ((this.tickCount + this.getId()) % 10 != 0) {
            return;
        }
        if (this.getTarget() != null || this.isAngry()) {
            return;
        }

        UUID tomeId = this.getUUID();
        AABB searchBox = this.getBoundingBox().inflate(28.0D);
        List<EnchantedBookEntity> minions = this.level().getEntitiesOfClass(
                EnchantedBookEntity.class,
                searchBox,
                book -> book.isAlive() && book.getCommanderUuid().map(tomeId::equals).orElse(false)
        );
        if (minions.isEmpty()) {
            return;
        }

        boolean tomeSecure = this.level().isDay()
                || this.isRoosting()
                || this.getAnimationState() == BookAnimationState.BARTERING;

        for (EnchantedBookEntity minion : minions) {
            if (minion.getTarget() != null || minion.isAngry()) {
                continue;
            }
            if ((tomeSecure || minion.wasEngagedInCombat()) && minion.getDeescalationTimer() > 0) {
                minion.dismissByCommander();
            }
        }
    }

    private void tickClientCommanderRuneTransfer() {
        // Throttle client-side entity lookup to once every 12 ticks (staggered by Tome ID)
        if ((this.tickCount + this.getId()) % 12 != 0) {
            return;
        }
        UUID tomeId = this.getUUID();
        AABB searchBox = this.getBoundingBox().inflate(28.0D);
        List<EnchantedBookEntity> minions = this.level().getEntitiesOfClass(
                EnchantedBookEntity.class,
                searchBox,
                book -> book.isAlive() && book.getCommanderUuid().map(tomeId::equals).orElse(false)
        );
        if (minions.isEmpty()) {
            return;
        }

        int schoolColor = this.getSchoolColor();
        Vec3 tomeCenter = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D);

        for (EnchantedBookEntity minion : minions) {
            Vec3 minionCenter = minion.position().add(0.0D, minion.getBbHeight() * 0.55D, 0.0D);
            boolean reverse = this.random.nextInt(3) == 0;
            if (reverse) {
                SgaRuneParticle.spawnTransferRune(this.level(), minionCenter, tomeCenter, minion.getSchoolColor());
            } else {
                SgaRuneParticle.spawnTransferRune(this.level(), tomeCenter, minionCenter, schoolColor);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.ENCHANTED_TOME_HP.get())
                .add(Attributes.ARMOR, Config.ENCHANTED_TOME_ARMOR.get())
                .add(Attributes.ARMOR_TOUGHNESS, Config.ENCHANTED_TOME_ARMOR_TOUGHNESS.get())
                .add(Attributes.FLYING_SPEED, Config.ENCHANTED_TOME_FLYING_SPEED.get())
                .add(Attributes.MOVEMENT_SPEED, Config.ENCHANTED_TOME_MOVEMENT_SPEED.get())
                .add(Attributes.FOLLOW_RANGE, Config.ENCHANTED_TOME_FOLLOW_RANGE.get());
    }

    @Override
    protected ItemStack createInitialBookStack(
            RegistryAccess registryAccess,
            RandomSource random,
            ResourceKey<EnchantmentTheme> themeKey,
            ResourceKey<MagicSchool> schoolKey
    ) {
        return EnchantedTomeItem.enchantWithThemeAndSchool(
                new ItemStack(Items.ENCHANTED_TOME.get()),
                registryAccess,
                random,
                themeKey,
                schoolKey
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BoredomTicks", this.boredomTicks);
        compound.putInt("AltarDeceptionTicks", this.altarDeceptionTicks);
        compound.putBoolean("AltarDeceptionActive", this.altarDeceptionCountdownActive);
        compound.putInt("AltarAbandonCooldown", this.altarAbandonCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPersistenceRequired();
        this.boredomTicks = compound.getInt("BoredomTicks");
        if (compound.contains("AltarDeceptionTicks")) {
            this.altarDeceptionTicks = compound.getInt("AltarDeceptionTicks");
        }
        this.altarDeceptionCountdownActive = compound.getBoolean("AltarDeceptionActive");
        this.altarAbandonCooldown = compound.getInt("AltarAbandonCooldown");
    }

    @Override
    protected ResourceKey<LootTable> getHurtLootTable() {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "entities/enchanted_tome_hurt"));
    }

    @Override
    protected int getBaseDeathPageCount() {
        return 1 + this.random.nextInt(3);
    }
}