package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.client.particle.SgaRuneParticle;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A generic illusory decoy entity that mirrors its caster's model, animations, and motion.
 * Has 1 HP, 0 armor, and immediately shatters/despawns upon taking any damage.
 * <p>
 * Optimized to eliminate per-second full-entity NBT serialization/network broadcasts,
 * bypass {@link Mob#serverAiStep()} overhead, and skip per-tick voxel raycasts when
 * orbiting in unobstructed air.
 */
public class MirageEntity extends Mob {

    private static final ThreadLocal<Entity> CURRENT_CASTER_CONTEXT = new ThreadLocal<>();

    private static final EntityDataAccessor<Integer> DATA_CASTER_ID =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_CASTER_UUID =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_CASTER_TYPE =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<CompoundTag> DATA_CASTER_DATA =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> DATA_MIRAGE_INDEX =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MIRAGE_TOTAL =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_CASTER_WIDTH =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CASTER_HEIGHT =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CASTER_EYE_HEIGHT =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ORBIT_RADIUS =
            SynchedEntityData.defineId(MirageEntity.class, EntityDataSerializers.FLOAT);

    public static final int DEFAULT_MAX_LIFETIME_TICKS = 240;
    private static final double AGGRO_REDIRECT_RANGE = 16.0D;

    private int maxLifetimeTicks = DEFAULT_MAX_LIFETIME_TICKS;
    private boolean formationInitialized = false;

    @Nullable
    private Entity pendingContextCaster;

    @Nullable
    private Entity clientVisualProxy;
    @Nullable
    private String clientVisualProxyType;

    public MirageEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
        this.pendingContextCaster = CURRENT_CASTER_CONTEXT.get();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 0.5D);
    }

    public static void withCasterContext(@Nullable Entity caster, Runnable action) {
        Entity previous = CURRENT_CASTER_CONTEXT.get();
        CURRENT_CASTER_CONTEXT.set(caster);
        try {
            action.run();
        } finally {
            if (previous != null) {
                CURRENT_CASTER_CONTEXT.set(previous);
            } else {
                CURRENT_CASTER_CONTEXT.remove();
            }
        }
    }

    /**
     * Spawns a synchronized orbital formation of {@link MirageEntity} decoys around {@code caster},
     * replacing any existing mirages owned by the same caster and optionally redirecting nearby
     * hostile aggro onto the newly spawned decoys.
     *
     * @param serverLevel   The executing server level.
     * @param caster        The entity being mirrored by the mirages.
     * @param count         The number of mirages to summon into the formation.
     * @param lifetimeTicks The duration in ticks before the mirages expire.
     * @param orbitRadius   Custom orbital radius, or {@code <= 0.0F} to scale automatically with caster size.
     * @param redirectAggro Whether nearby mobs targeting {@code caster} should switch aggro to a mirage.
     * @return The list of spawned {@link MirageEntity} instances.
     */
    public static List<MirageEntity> summonFormation(
            ServerLevel serverLevel,
            Entity caster,
            int count,
            int lifetimeTicks,
            float orbitRadius,
            boolean redirectAggro
    ) {
        if (caster == null || !caster.isAlive() || count <= 0) {
            return List.of();
        }
        if (caster instanceof MirageEntity existingMirage) {
            caster = existingMirage.getCaster();
            if (caster == null || !caster.isAlive()) {
                return List.of();
            }
        }

        UUID casterUuid = caster.getUUID();
        AABB searchBox = caster.getBoundingBox().inflate(24.0D);
        List<MirageEntity> oldMirages = serverLevel.getEntitiesOfClass(
                MirageEntity.class,
                searchBox,
                m -> m.isAlive() && m.getCasterUuid().map(casterUuid::equals).orElse(false)
        );
        for (MirageEntity old : oldMirages) {
            old.discard();
        }

        List<MirageEntity> spawnedMirages = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            MirageEntity mirage = Entities.MIRAGE.<MirageEntity>get().create(serverLevel);
            if (mirage == null) {
                continue;
            }
            mirage.initializeFormationMember(caster, i, count, lifetimeTicks, orbitRadius);
            Vec3 initialPos = mirage.computeFormationTargetPos(caster);
            mirage.moveTo(initialPos.x, initialPos.y, initialPos.z, caster.getYRot(), caster.getXRot());
            mirage.formationInitialized = true;

            if (serverLevel.addFreshEntity(mirage)) {
                mirage.spawnArrivalParticles(serverLevel);
                spawnedMirages.add(mirage);
            }
        }

        if (!spawnedMirages.isEmpty()) {
            serverLevel.playSound(
                    null,
                    caster.getX(),
                    caster.getY() + caster.getBbHeight() * 0.5D,
                    caster.getZ(),
                    SoundEvents.ILLUSIONER_MIRROR_MOVE,
                    caster.getSoundSource(),
                    0.95F,
                    1.0F + (serverLevel.random.nextFloat() - 0.5F) * 0.15F
            );
            if (redirectAggro) {
                redirectHostileAggroToFormation(serverLevel, caster, spawnedMirages);
            }
        }

        return spawnedMirages;
    }

    private static void redirectHostileAggroToFormation(
            ServerLevel serverLevel,
            Entity caster,
            List<MirageEntity> mirages
    ) {
        if (mirages.isEmpty()) {
            return;
        }
        AABB aggroBox = caster.getBoundingBox().inflate(AGGRO_REDIRECT_RANGE);
        List<Mob> nearbyMobs = serverLevel.getEntitiesOfClass(
                Mob.class,
                aggroBox,
                mob -> mob.isAlive()
                        && !(mob instanceof MirageEntity)
                        && mob != caster
                        && mob.getTarget() == caster
        );
        for (int i = 0; i < nearbyMobs.size(); i++) {
            Mob attacker = nearbyMobs.get(i);
            MirageEntity decoy = mirages.get((i + serverLevel.random.nextInt(mirages.size())) % mirages.size());
            attacker.setTarget(decoy);
        }
    }

    public static Entity getCurrentCasterContext() {
        return CURRENT_CASTER_CONTEXT.get();
    }

    /**
     * Directly initializes this mirage's caster binding, formation slot, lifetime, and orbital radius
     * without requiring a secondary entity search pass.
     */
    public void initializeFormationMember(
            Entity caster,
            int index,
            int total,
            int lifetimeTicks,
            float orbitRadius
    ) {
        this.pendingContextCaster = null;
        this.maxLifetimeTicks = Math.max(1, lifetimeTicks);
        this.setMirageIndex(index);
        this.setMirageTotal(total);
        this.setOrbitRadius(orbitRadius);
        this.bindToCaster(caster);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CASTER_ID, -1);
        builder.define(DATA_CASTER_UUID, Optional.empty());
        builder.define(DATA_CASTER_TYPE, "");
        builder.define(DATA_CASTER_DATA, new CompoundTag());
        builder.define(DATA_MIRAGE_INDEX, 0);
        builder.define(DATA_MIRAGE_TOTAL, 3);
        builder.define(DATA_CASTER_WIDTH, 0.6F);
        builder.define(DATA_CASTER_HEIGHT, 0.6F);
        builder.define(DATA_CASTER_EYE_HEIGHT, 0.3F);
        builder.define(DATA_ORBIT_RADIUS, 0.0F);
    }

    @Override
    protected void registerGoals() {
        // Mirages do not run independent AI goals; their motion is driven directly by their caster.
    }

    public int getCasterEntityId() {
        return this.entityData.get(DATA_CASTER_ID);
    }

    public void setCasterEntityId(int id) {
        this.entityData.set(DATA_CASTER_ID, id);
    }

    public Optional<UUID> getCasterUuid() {
        return this.entityData.get(DATA_CASTER_UUID);
    }

    public void setCasterUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_CASTER_UUID, Optional.ofNullable(uuid));
    }

    public String getCasterTypeString() {
        return this.entityData.get(DATA_CASTER_TYPE);
    }

    public CompoundTag getCasterDataSnapshot() {
        return this.entityData.get(DATA_CASTER_DATA);
    }

    public int getMirageIndex() {
        return this.entityData.get(DATA_MIRAGE_INDEX);
    }

    public void setMirageIndex(int index) {
        this.entityData.set(DATA_MIRAGE_INDEX, Math.max(0, index));
    }

    public int getMirageTotal() {
        return Math.max(1, this.entityData.get(DATA_MIRAGE_TOTAL));
    }

    public void setMirageTotal(int total) {
        this.entityData.set(DATA_MIRAGE_TOTAL, Math.max(1, total));
    }

    public float getOrbitRadius() {
        return this.entityData.get(DATA_ORBIT_RADIUS);
    }

    public void setOrbitRadius(float radius) {
        this.entityData.set(DATA_ORBIT_RADIUS, Math.max(0.0F, radius));
    }

    public int getMaxLifetimeTicks() {
        return this.maxLifetimeTicks;
    }

    public void setMaxLifetimeTicks(int ticks) {
        this.maxLifetimeTicks = Math.max(1, ticks);
    }

    public float getSyncedWidth() {
        return Math.max(0.2F, this.entityData.get(DATA_CASTER_WIDTH));
    }

    public float getSyncedHeight() {
        return Math.max(0.2F, this.entityData.get(DATA_CASTER_HEIGHT));
    }

    public float getSyncedEyeHeight() {
        return Math.max(0.1F, this.entityData.get(DATA_CASTER_EYE_HEIGHT));
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_CASTER_WIDTH.equals(key)
                || DATA_CASTER_HEIGHT.equals(key)
                || DATA_CASTER_EYE_HEIGHT.equals(key)
                || DATA_CASTER_ID.equals(key)) {
            this.refreshDimensions();
        }
        if (DATA_CASTER_TYPE.equals(key) || DATA_CASTER_DATA.equals(key)) {
            this.clientVisualProxy = null;
        }
    }

    @Override
    public @NotNull EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        return EntityDimensions.scalable(this.getSyncedWidth(), this.getSyncedHeight())
                .withEyeHeight(this.getSyncedEyeHeight());
    }

    @Override
    public int getArmorValue() {
        return 0;
    }

    @Nullable
    public Entity getCaster() {
        int id = this.getCasterEntityId();
        if (id >= 0) {
            Entity byId = this.level().getEntity(id);
            if (byId != null && byId.isAlive() && !(byId instanceof MirageEntity)) {
                return byId;
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            Optional<UUID> uuidOpt = this.getCasterUuid();
            if (uuidOpt.isPresent()) {
                Entity byUuid = serverLevel.getEntity(uuidOpt.get());
                if (byUuid != null && byUuid.isAlive() && !(byUuid instanceof MirageEntity)) {
                    if (this.getCasterEntityId() != byUuid.getId()) {
                        this.setCasterEntityId(byUuid.getId());
                    }
                    return byUuid;
                }
            }
        }
        return null;
    }

    public void bindToCaster(Entity caster) {
        if (caster == null || caster instanceof MirageEntity) {
            return;
        }
        this.setCasterEntityId(caster.getId());
        this.setCasterUuid(caster.getUUID());

        ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(caster.getType());
        this.entityData.set(DATA_CASTER_TYPE, typeKey.toString());

        this.entityData.set(DATA_CASTER_WIDTH, caster.getBbWidth());
        this.entityData.set(DATA_CASTER_HEIGHT, caster.getBbHeight());
        this.entityData.set(DATA_CASTER_EYE_HEIGHT, caster.getEyeHeight());
        this.refreshDimensions();

        if (caster.hasCustomName()) {
            this.setCustomName(caster.getCustomName());
            this.setCustomNameVisible(caster.isCustomNameVisible());
        }

        // Capture the fallback visual NBT snapshot once on bind rather than re-serializing every second
        this.updateCasterVisualSnapshot(caster);
        this.syncStateFromCaster(caster);
    }

    private void updateCasterVisualSnapshot(Entity caster) {
        try {
            CompoundTag tag = new CompoundTag();
            caster.saveWithoutId(tag);
            tag.remove("Pos");
            tag.remove("Motion");
            tag.remove("Rotation");
            tag.remove("UUID");
            tag.remove("Health");
            tag.remove("HurtTime");
            tag.remove("DeathTime");
            tag.remove("Passengers");
            tag.remove("Leash");
            // Strip bulky non-visual data to keep SynchedEntityData packets small
            tag.remove("Brain");
            tag.remove("Memories");
            tag.remove("Attributes");
            tag.remove("ActiveEffects");
            tag.remove("Inventory");
            tag.remove("EnderItems");
            tag.remove("recipeBook");
            this.entityData.set(DATA_CASTER_DATA, tag);
        } catch (Exception ignored) {
        }
    }

    private boolean tryAutoBindCasterOnServer(ServerLevel serverLevel) {
        Entity contextCaster = this.pendingContextCaster != null ? this.pendingContextCaster : CURRENT_CASTER_CONTEXT.get();
        this.pendingContextCaster = null;
        if (contextCaster != null && !(contextCaster instanceof MirageEntity)) {
            if (!contextCaster.isAlive()) {
                this.discard();
                return false;
            }
            this.bindToCaster(contextCaster);
            this.assignFormationSlot(serverLevel, contextCaster);
            return true;
        }

        AABB searchBox = this.getBoundingBox().inflate(8.0D);
        List<LivingEntity> candidates = serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity.isAlive() && !(entity instanceof MirageEntity)
        );
        if (candidates.isEmpty()) {
            return false;
        }
        candidates.sort(Comparator.comparingDouble(this::distanceToSqr));
        LivingEntity nearestCaster = candidates.getFirst();
        this.bindToCaster(nearestCaster);
        this.assignFormationSlot(serverLevel, nearestCaster);
        return true;
    }

    private void assignFormationSlot(ServerLevel serverLevel, Entity caster) {
        UUID casterUuid = caster.getUUID();
        AABB searchBox = caster.getBoundingBox().inflate(24.0D);
        List<MirageEntity> existingMirages = serverLevel.getEntitiesOfClass(
                MirageEntity.class,
                searchBox,
                m -> m != this && m.isAlive() && m.getCasterUuid().map(casterUuid::equals).orElse(false)
        );

        // Discard stale mirages from an earlier cast so a new cast cleanly refreshes the formation
        for (MirageEntity old : existingMirages) {
            if (old.tickCount > 2) {
                old.discard();
            }
        }

        List<MirageEntity> currentBatch = serverLevel.getEntitiesOfClass(
                MirageEntity.class,
                searchBox,
                m -> m.isAlive() && m.tickCount <= 2 && m.getCasterUuid().map(casterUuid::equals).orElse(false)
        );
        currentBatch.sort(Comparator.comparingInt(Entity::getId));

        int total = Math.max(1, currentBatch.size());
        for (int i = 0; i < currentBatch.size(); i++) {
            MirageEntity m = currentBatch.get(i);
            m.setMirageIndex(i);
            m.setMirageTotal(total);
        }
    }

    private void spawnArrivalParticles(ServerLevel serverLevel) {
        double centerY = this.getY() + this.getBbHeight() * 0.5D;
        serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                this.getX(),
                centerY,
                this.getZ(),
                8,
                0.2D,
                0.25D,
                0.2D,
                0.02D
        );
        serverLevel.sendParticles(
                SimpleParticleTypes.SGA_RUNE.type(),
                this.getX(),
                centerY,
                this.getZ(),
                12,
                0.25D,
                0.25D,
                0.25D,
                0.04D
        );
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
            if (this.getCasterUuid().isEmpty()) {
                if (!this.tryAutoBindCasterOnServer(serverLevel)) {
                    if (this.tickCount > 5) {
                        this.discard();
                        return;
                    }
                }
            }

            Entity caster = this.getCaster();
            if (caster == null || !caster.isAlive()) {
                this.shatterMirage(serverLevel);
                return;
            }

            if (this.tickCount >= this.maxLifetimeTicks) {
                this.shatterMirage(serverLevel);
                return;
            }

            if (!this.formationInitialized) {
                this.formationInitialized = true;
                Vec3 initialPos = this.computeFormationTargetPos(caster);
                this.moveTo(initialPos.x, initialPos.y, initialPos.z, caster.getYRot(), caster.getXRot());
                this.spawnArrivalParticles(serverLevel);
            } else {
                this.tickServerMotionFollow(caster);
            }

            this.syncStateFromCaster(caster);
        }

        super.tick();

        if (this.level().isClientSide()) {
            Entity caster = this.getCaster();
            if (caster != null) {
                this.syncStateFromCaster(caster);
                if (caster instanceof AbstractFlyingBookEntity book) {
                    this.tickClientBookMirageParticles(book);
                }
            } else if (this.clientVisualProxy != null) {
                this.clientVisualProxy.tickCount = this.tickCount;
                this.syncStateToProxy(this.clientVisualProxy);
            }
        }
    }

    private void tickServerMotionFollow(Entity caster) {
        boolean flyingCaster = caster instanceof AbstractFlyingBookEntity
                || caster.isNoGravity()
                || (caster instanceof LivingEntity le && le.isFallFlying());
        this.setNoGravity(flyingCaster);

        Vec3 desiredPos = this.computeFormationTargetPos(caster);
        Vec3 toDesired = desiredPos.subtract(this.position());
        Vec3 casterVel = caster.getDeltaMovement();

        if (toDesired.lengthSqr() > 64.0D) {
            this.moveTo(desiredPos.x, desiredPos.y, desiredPos.z, caster.getYRot(), caster.getXRot());
            this.setDeltaMovement(casterVel);
            return;
        }

        Vec3 springVel = toDesired.scale(0.28D);
        if (springVel.lengthSqr() > 0.64D) {
            springVel = springVel.normalize().scale(0.8D);
        }

        Vec3 combinedVel = casterVel.add(springVel);
        if (!flyingCaster && caster.onGround()) {
            combinedVel = new Vec3(combinedVel.x, casterVel.y, combinedVel.z);
        }
        this.setDeltaMovement(combinedVel);
    }

    private Vec3 computeFormationTargetPos(Entity caster) {
        int index = this.getMirageIndex();
        int total = this.getMirageTotal();
        double baseAngle = ((double) index / (double) total) * (Math.PI * 2.0D);
        double orbitAngle = baseAngle + (this.tickCount * 0.045D);
        float configuredRadius = this.getOrbitRadius();
        double radius = configuredRadius > 0.0F
                ? (double) configuredRadius
                : Math.max(1.65D, caster.getBbWidth() * 2.2D);

        double offsetX = Math.cos(orbitAngle) * radius;
        double offsetZ = Math.sin(orbitAngle) * radius;
        double offsetY = 0.0D;

        if (caster instanceof AbstractFlyingBookEntity || caster.isNoGravity()) {
            offsetY = Math.sin(this.tickCount * 0.1D + index * 2.1D) * 0.22D;
        }

        Vec3 casterCenter = caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D);
        Vec3 desiredCenter = casterCenter.add(offsetX, offsetY, offsetZ);

        // Fast-path: skip the ClipContext voxel raycast when both the midpoint and destination blocks are air
        Level level = this.level();
        ChunkSource chunkSource = level.getChunkSource();
        BlockPos destBlock = BlockPos.containing(desiredCenter);
        BlockPos midBlock = BlockPos.containing(
                casterCenter.x + offsetX * 0.5D,
                casterCenter.y + offsetY * 0.5D,
                casterCenter.z + offsetZ * 0.5D
        );
        if (isAirInLoadedChunk(chunkSource, destBlock) && isAirInLoadedChunk(chunkSource, midBlock)) {
            return new Vec3(caster.getX() + offsetX, caster.getY() + offsetY, caster.getZ() + offsetZ);
        }

        HitResult hit = level.clip(new ClipContext(
                casterCenter,
                desiredCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        if (hit.getType() != HitResult.Type.MISS) {
            Vec3 toHit = hit.getLocation().subtract(casterCenter);
            double safeDist = Math.max(0.0D, toHit.length() - (this.getBbWidth() * 0.6D));
            Vec3 safeCenter = toHit.lengthSqr() > 0.001D
                    ? casterCenter.add(toHit.normalize().scale(safeDist))
                    : casterCenter;
            return new Vec3(safeCenter.x, safeCenter.y - caster.getBbHeight() * 0.5D, safeCenter.z);
        }

        return new Vec3(caster.getX() + offsetX, caster.getY() + offsetY, caster.getZ() + offsetZ);
    }

    private static boolean isAirInLoadedChunk(ChunkSource chunkSource, BlockPos pos) {
        LevelChunk chunk = chunkSource.getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ())
        );
        return chunk != null && chunk.getBlockState(pos).isAir();
    }

    private void syncStateFromCaster(Entity caster) {
        this.setYRot(caster.getYRot());
        this.yRotO = caster.yRotO;
        this.setXRot(caster.getXRot());
        this.xRotO = caster.xRotO;
        this.setPose(caster.getPose());
        this.setShiftKeyDown(caster.isShiftKeyDown());
        this.setSprinting(caster.isSprinting());
        this.setSwimming(caster.isSwimming());

        if (caster instanceof LivingEntity livingCaster) {
            this.yBodyRot = livingCaster.yBodyRot;
            this.yBodyRotO = livingCaster.yBodyRotO;
            this.yHeadRot = livingCaster.yHeadRot;
            this.yHeadRotO = livingCaster.yHeadRotO;
            this.swinging = livingCaster.swinging;
            this.swingTime = livingCaster.swingTime;
            this.swingingArm = livingCaster.swingingArm;
            this.attackAnim = livingCaster.attackAnim;
            this.oAttackAnim = livingCaster.oAttackAnim;
            this.walkAnimation.setSpeed(livingCaster.walkAnimation.speed());
        } else {
            this.yBodyRot = caster.getYRot();
            this.yBodyRotO = caster.yRotO;
            this.yHeadRot = caster.getYRot();
            this.yHeadRotO = caster.yRotO;
        }
    }

    private void syncStateToProxy(Entity proxy) {
        proxy.setYRot(this.getYRot());
        proxy.yRotO = this.yRotO;
        proxy.setXRot(this.getXRot());
        proxy.xRotO = this.xRotO;
        proxy.setPose(this.getPose());
        proxy.setShiftKeyDown(this.isShiftKeyDown());
        proxy.setSprinting(this.isSprinting());
        proxy.setSwimming(this.isSwimming());

        if (proxy instanceof LivingEntity livingProxy) {
            livingProxy.yBodyRot = this.yBodyRot;
            livingProxy.yBodyRotO = this.yBodyRotO;
            livingProxy.yHeadRot = this.yHeadRot;
            livingProxy.yHeadRotO = this.yHeadRotO;
            livingProxy.swinging = this.swinging;
            livingProxy.swingTime = this.swingTime;
            livingProxy.swingingArm = this.swingingArm;
            livingProxy.attackAnim = this.attackAnim;
            livingProxy.oAttackAnim = this.oAttackAnim;
            livingProxy.walkAnimation.setSpeed(this.walkAnimation.speed());
        }
    }

    private void tickClientBookMirageParticles(AbstractFlyingBookEntity book) {
        BookAnimationState state = book.getAnimationState();
        if (!state.shouldGenerateParticles || book.isRoosting()) {
            return;
        }
        int color = book.getActiveParticleColor();
        double centerX = this.getX();
        double centerY = this.getY() + this.getBbHeight() * 0.55D;
        double centerZ = this.getZ();

        if (state.isChanting()) {
            float progress = Mth.clamp((float) book.getClientStateTicks() / 40.0F, 0.0F, 1.0F);
            Vec3 look = book.getChantingDirection();
            double floatBob = Math.sin((this.tickCount + this.getId() * 5) * 0.18D) * 0.06D;
            Vec3 focus = new Vec3(
                    centerX + look.x * 0.75D,
                    centerY + look.y * 0.75D + floatBob,
                    centerZ + look.z * 0.75D
            );
            Vec3 worldUp = Math.abs(look.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = look.cross(worldUp).normalize();
            Vec3 planeUp = right.cross(look).normalize();

            double radius = Mth.lerp(progress, 0.95D, 0.14D);
            double angularSpeed = Mth.lerp(progress, 0.18D, 0.52D);
            double baseAngle = book.getClientStateTicks() * angularSpeed;

            for (int i = 0; i < 2; i++) {
                double angle = baseAngle + i * Math.PI;
                Vec3 spawnPos = focus
                        .add(right.scale(Math.cos(angle) * radius))
                        .add(planeUp.scale(Math.sin(angle) * radius));
                Vec3 vel = focus.subtract(spawnPos).scale(0.095D);
                SgaRuneParticle.spawnTinted(this.level(), spawnPos.x, spawnPos.y, spawnPos.z, vel.x, vel.y, vel.z, color);
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
                    color
            );
        }
    }

    @Nullable
    public Entity getOrCreateVisualCaster(Level clientLevel) {
        Entity liveCaster = this.getCaster();
        if (liveCaster != null && !(liveCaster instanceof MirageEntity)) {
            return liveCaster;
        }

        String rawType = this.getCasterTypeString();
        if (rawType == null || rawType.isEmpty()) {
            return null;
        }

        if (this.clientVisualProxy == null || !rawType.equals(this.clientVisualProxyType)) {
            this.clientVisualProxyType = rawType;
            Optional<EntityType<?>> typeOpt = EntityType.byString(rawType);
            if (typeOpt.isPresent() && typeOpt.get() != Entities.MIRAGE.get()) {
                Entity created = typeOpt.get().create(clientLevel);
                if (created != null && !(created instanceof MirageEntity)) {
                    CompoundTag snapshot = this.getCasterDataSnapshot();
                    if (!snapshot.isEmpty()) {
                        try {
                            created.load(snapshot);
                        } catch (Exception ignored) {
                        }
                    }
                    this.clientVisualProxy = created;
                }
            }
        }

        if (this.clientVisualProxy != null) {
            this.syncStateToProxy(this.clientVisualProxy);
        }
        return this.clientVisualProxy;
    }

    public boolean isCasterOrAlly(@Nullable Entity other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        Optional<UUID> casterUuidOpt = this.getCasterUuid();
        if (casterUuidOpt.isPresent() && casterUuidOpt.get().equals(other.getUUID())) {
            return true;
        }
        if (other instanceof MirageEntity otherMirage) {
            return casterUuidOpt.isPresent() && casterUuidOpt.equals(otherMirage.getCasterUuid());
        }
        Entity caster = this.getCaster();
        if (caster instanceof AbstractFlyingBookEntity book && other instanceof LivingEntity livingOther) {
            return !book.isValidHostileTarget(livingOther);
        }
        return caster != null && caster.isAlliedTo(other);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.isInvulnerableTo(source) || amount <= 0.0F) {
            return false;
        }
        if (!this.level().isClientSide()) {
            if (this.level() instanceof ServerLevel serverLevel) {
                this.shatterMirage(serverLevel);
            } else {
                this.discard();
            }
        }
        return true;
    }

    public void shatterMirage(ServerLevel serverLevel) {
        if (!this.isAlive() || this.isRemoved()) {
            return;
        }
        double centerX = this.getX();
        double centerY = this.getY() + this.getBbHeight() * 0.5D;
        double centerZ = this.getZ();

        serverLevel.playSound(
                null,
                centerX,
                centerY,
                centerZ,
                SoundEvents.ILLUSIONER_MIRROR_MOVE,
                this.getSoundSource(),
                0.9F,
                1.25F + (this.random.nextFloat() - 0.5F) * 0.2F
        );
        serverLevel.sendParticles(
                ParticleTypes.POOF,
                centerX,
                centerY,
                centerZ,
                14,
                this.getBbWidth() * 0.35D,
                this.getBbHeight() * 0.35D,
                this.getBbWidth() * 0.35D,
                0.04D
        );
        serverLevel.sendParticles(
                SimpleParticleTypes.SGA_RUNE.type(),
                centerX,
                centerY,
                centerZ,
                12,
                this.getBbWidth() * 0.35D,
                this.getBbHeight() * 0.35D,
                this.getBbWidth() * 0.35D,
                0.05D
        );
        this.discard();
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(@NotNull Entity entity) {
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.getCasterUuid().ifPresent(uuid -> compound.putUUID("CasterUUID", uuid));
        compound.putString("CasterType", this.getCasterTypeString());
        compound.put("CasterData", this.getCasterDataSnapshot().copy());
        compound.putInt("MirageIndex", this.getMirageIndex());
        compound.putInt("MirageTotal", this.getMirageTotal());
        compound.putInt("MaxLifetimeTicks", this.maxLifetimeTicks);
        compound.putFloat("OrbitRadius", this.getOrbitRadius());
        compound.putFloat("CasterWidth", this.getSyncedWidth());
        compound.putFloat("CasterHeight", this.getSyncedHeight());
        compound.putFloat("CasterEyeHeight", this.getSyncedEyeHeight());
        compound.putBoolean("FormationInitialized", this.formationInitialized);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("CasterUUID")) {
            this.setCasterUuid(compound.getUUID("CasterUUID"));
        }
        if (compound.contains("CasterType")) {
            this.entityData.set(DATA_CASTER_TYPE, compound.getString("CasterType"));
        }
        if (compound.contains("CasterData")) {
            this.entityData.set(DATA_CASTER_DATA, compound.getCompound("CasterData"));
        }
        if (compound.contains("MirageIndex")) {
            this.setMirageIndex(compound.getInt("MirageIndex"));
        }
        if (compound.contains("MirageTotal")) {
            this.setMirageTotal(compound.getInt("MirageTotal"));
        }
        if (compound.contains("MaxLifetimeTicks")) {
            this.maxLifetimeTicks = compound.getInt("MaxLifetimeTicks");
        }
        if (compound.contains("OrbitRadius")) {
            this.setOrbitRadius(compound.getFloat("OrbitRadius"));
        }
        if (compound.contains("CasterWidth")) {
            this.entityData.set(DATA_CASTER_WIDTH, compound.getFloat("CasterWidth"));
        }
        if (compound.contains("CasterHeight")) {
            this.entityData.set(DATA_CASTER_HEIGHT, compound.getFloat("CasterHeight"));
        }
        if (compound.contains("CasterEyeHeight")) {
            this.entityData.set(DATA_CASTER_EYE_HEIGHT, compound.getFloat("CasterEyeHeight"));
        }
        this.formationInitialized = compound.getBoolean("FormationInitialized");
        this.refreshDimensions();
    }
}