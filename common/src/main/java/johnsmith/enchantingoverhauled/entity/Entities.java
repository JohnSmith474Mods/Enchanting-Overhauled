package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.entity.projectile.MagicMissileEntity;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import org.jetbrains.annotations.NotNull;

public enum Entities implements StringRepresentable {
    ENCHANTED_TOME(
            "enchanted_tome",
            EntityType.Builder
                    .of(EnchantedTomeEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.6F)
                    .eyeHeight(0.3F)
                    .clientTrackingRange(8)
                    .fireImmune()
    ),
    ENCHANTED_BOOK(
            "enchanted_book",
            EntityType.Builder
                    .of(EnchantedBookEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.6F)
                    .eyeHeight(0.3F)
                    .clientTrackingRange(8)
    ),
    MIRAGE(
            "mirage",
            EntityType.Builder
                    .<MirageEntity>of(MirageEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .eyeHeight(0.3F)
                    .clientTrackingRange(8)
    ),
    MAGIC_MISSILE(
            "magic_missile",
            EntityType.Builder
                    .<MagicMissileEntity>of(MagicMissileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
    );

    private final String key;
    private final EntityType.Builder<?> builder;
    private EntityType<?> entity;

    Entities(String key, EntityType.Builder<?> builder) {
        this.key = key;
        this.builder = builder;
    }

    public EntityType<?> build() {
        if (this.entity == null) {
            this.entity = this.builder.build(key);
        }
        return this.entity;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.key;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> EntityType<T> get() {
        return (EntityType<T>) this.build();
    }
}