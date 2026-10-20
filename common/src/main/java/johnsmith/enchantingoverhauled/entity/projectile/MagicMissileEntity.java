package johnsmith.enchantingoverhauled.entity.projectile;

import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.api.damagesource.DamageTypes;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellField;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.Entities;
import johnsmith.enchantingoverhauled.entity.MirageEntity;

import johnsmith.enchantmentcore.api.enchantment.spellfield.SpellFieldComponent;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MagicMissileEntity extends ThrowableProjectile {

    private static final EntityDataAccessor<String> DATA_THEME_ID =
            SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_SCHOOL_ID =
            SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_CRIT =
            SynchedEntityData.defineId(MagicMissileEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private SpellFieldComponent spellField;
    private Optional<AiSpellField.AnchorPayload> anchorPayload = Optional.empty();
    private ItemStack weapon = ItemStack.EMPTY;

    public MagicMissileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public MagicMissileEntity(
            Level level,
            LivingEntity owner,
            @Nullable SpellFieldComponent spellField,
            Optional<AiSpellField.AnchorPayload> anchorPayload,
            ItemStack weapon
    ) {
        super(Entities.MAGIC_MISSILE.get(), owner, level);
        this.setNoGravity(true);
        this.spellField = spellField;
        this.anchorPayload = anchorPayload != null ? anchorPayload : Optional.empty();
        this.weapon = weapon != null ? weapon.copy() : ItemStack.EMPTY;

        if (owner instanceof AbstractFlyingBookEntity book) {
            this.setThemeKey(book.getThemeKey());
            this.setSchoolKey(book.getCastSchoolKey().orElse(book.getSchoolKey()));
        } else if (!this.weapon.isEmpty()) {
            ArcaneItemNamingUtil.resolveTheme(this.weapon, level.registryAccess()).ifPresent(this::setThemeKey);
            ArcaneItemNamingUtil.resolveSchool(this.weapon, level.registryAccess()).ifPresent(this::setSchoolKey);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_THEME_ID, EnchantmentThemes.COMMON.location().toString());
        builder.define(DATA_SCHOOL_ID, MagicSchools.ABERRATION.location().toString());
        builder.define(DATA_CRIT, false);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    public boolean isCrit() {
        return this.entityData.get(DATA_CRIT);
    }

    public void setCrit(boolean crit) {
        this.entityData.set(DATA_CRIT, crit);
    }

    public ResourceKey<EnchantmentTheme> getThemeKey() {
        ResourceLocation loc = ResourceLocation.tryParse(this.entityData.get(DATA_THEME_ID));
        return loc != null
                ? ResourceKey.create(EnchantmentThemes.THEME_REGISTRY_KEY, loc)
                : EnchantmentThemes.COMMON;
    }

    public void setThemeKey(ResourceKey<EnchantmentTheme> themeKey) {
        this.entityData.set(DATA_THEME_ID, (themeKey != null ? themeKey : EnchantmentThemes.COMMON).location().toString());
    }

    public ResourceKey<MagicSchool> getSchoolKey() {
        ResourceLocation loc = ResourceLocation.tryParse(this.entityData.get(DATA_SCHOOL_ID));
        return loc != null
                ? ResourceKey.create(MagicSchools.REGISTRY_KEY, loc)
                : MagicSchools.ABERRATION;
    }

    public void setSchoolKey(ResourceKey<MagicSchool> schoolKey) {
        this.entityData.set(DATA_SCHOOL_ID, (schoolKey != null ? schoolKey : MagicSchools.ABERRATION).location().toString());
    }

    public int getSchoolColor() {
        return MagicSchools.getSchool(this.level().registryAccess(), this.getSchoolKey()).getColor() & 0xFFFFFF;
    }

    public int getThemeColor() {
        var regOpt = this.level().registryAccess().registry(EnchantmentThemes.THEME_REGISTRY_KEY);
        if (regOpt.isPresent()) {
            EnchantmentTheme theme = regOpt.get().get(this.getThemeKey());
            if (theme != null && theme.colorCode().isPresent()) {
                return theme.colorCode().get() & 0xFFFFFF;
            }
        }
        return this.getSchoolColor();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Theme", this.getThemeKey().location().toString());
        compound.putString("School", this.getSchoolKey().location().toString());
        compound.putBoolean("Crit", this.isCrit());
        if (!this.weapon.isEmpty()) {
            compound.put("Weapon", this.weapon.saveOptional(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setNoGravity(true);
        if (compound.contains("Theme")) {
            ResourceLocation loc = ResourceLocation.tryParse(compound.getString("Theme"));
            if (loc != null) {
                this.setThemeKey(ResourceKey.create(EnchantmentThemes.THEME_REGISTRY_KEY, loc));
            }
        }
        if (compound.contains("School")) {
            ResourceLocation loc = ResourceLocation.tryParse(compound.getString("School"));
            if (loc != null) {
                this.setSchoolKey(ResourceKey.create(MagicSchools.REGISTRY_KEY, loc));
            }
        }
        if (compound.contains("Crit")) {
            this.setCrit(compound.getBoolean("Crit"));
        }
        if (compound.contains("Weapon")) {
            this.weapon = ItemStack.parseOptional(this.registryAccess(), compound.getCompound("Weapon"));
        }
    }

    public boolean isHeadshot(Entity target, EntityHitResult result) {
        if (!(target instanceof LivingEntity) || target.getBbHeight() < 0.8F) {
            return false;
        }
        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement().scale(2.0D));
        AABB targetBox = target.getBoundingBox();
        Vec3 hitPos = targetBox.clip(start, end).orElseGet(() -> {
            Vec3 vel = this.getDeltaMovement();
            if (vel.horizontalDistanceSqr() > 1.0E-4D) {
                double dx = target.getX() - start.x;
                double dz = target.getZ() - start.z;
                double t = Mth.clamp((dx * vel.x + dz * vel.z) / vel.horizontalDistanceSqr(), 0.0D, 1.5D);
                return start.add(vel.scale(t));
            }
            return result.getLocation();
        });

        double minY = targetBox.minY;
        double maxY = targetBox.maxY;
        double height = target.getBbHeight();
        double eyeY = target.getEyeY();
        double headMinY = Math.max(minY + height * 0.72D, eyeY - height * 0.18D);
        double checkY = Math.max(hitPos.y, result.getLocation().y);
        return checkY >= headMinY && checkY <= maxY + 0.35D;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            if (!(target instanceof Creeper) && !target.getType().is(Tags.IMMUNE_TO_MAGIC_MISSILE)) {
                int difficulty = this.level().getDifficulty().getId();
                if (difficulty > 0) {
                    boolean crit = this.isCrit() || this.isHeadshot(target, result);
                    float damage = difficulty * 2.0F;
                    if (crit) {
                        damage *= 1.5F;
                    }
                    DamageSource source = new DamageSource(
                            this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC_MISSILE),
                            this,
                            this.getOwner()
                    );
                    if (target.hurt(source, damage) && crit && this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(
                                null,
                                target.getX(),
                                target.getEyeY(),
                                target.getZ(),
                                SoundEvents.PLAYER_ATTACK_CRIT,
                                this.getSoundSource(),
                                1.0F,
                                1.1F + (this.random.nextFloat() - 0.5F) * 0.2F
                        );
                        serverLevel.sendParticles(
                                ParticleTypes.CRIT,
                                target.getX(),
                                target.getEyeY(),
                                target.getZ(),
                                12,
                                0.25D,
                                0.25D,
                                0.25D,
                                0.15D
                        );
                    }
                }
            }
            Vec3 impactLocation = result.getLocation() != null ? result.getLocation() : target.position();
            this.deploySpellField(impactLocation);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            this.deploySpellField(result.getLocation());
            this.discard();
        }
    }

    private void deploySpellField(Vec3 hitPos) {
        if (this.spellField == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity owner = this.getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        AiSpellField.deployPayload(
                serverLevel,
                hitPos,
                livingOwner,
                this,
                this.weapon,
                this.spellField,
                this.anchorPayload
        );
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target.getType().is(Tags.IMMUNE_TO_MAGIC_MISSILE) || target instanceof Creeper) {
            return false;
        }
        if (target instanceof MirageEntity mirage && mirage.isCasterOrAlly(this.getOwner())) {
            return false;
        }
        return super.canHitEntity(target);
    }
}