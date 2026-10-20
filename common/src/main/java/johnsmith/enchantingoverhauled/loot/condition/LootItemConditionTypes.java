package johnsmith.enchantingoverhauled.loot.condition;

import com.mojang.serialization.MapCodec;

import johnsmith.enchantingoverhauled.api.loot.condition.CasterHostileCondition;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import org.jetbrains.annotations.NotNull;

/**
 * Central registry for custom {@link LootItemConditionType}s introduced by Enchanting Overhauled.
 * <p>
 * Instantiates {@link LootItemConditionType} without eager registration.
 */
public enum LootItemConditionTypes implements StringRepresentable {
    CASTER_HOSTILE("caster_hostile", CasterHostileCondition.CODEC);

    private final String id;
    private final MapCodec<? extends LootItemCondition> codec;
    private final LootItemConditionType type;

    LootItemConditionTypes(String id, MapCodec<? extends LootItemCondition> codec) {
        this.id = id;
        this.codec = codec;
        this.type = new LootItemConditionType(codec);
    }

    public LootItemConditionType type() {
        return this.type;
    }

    public MapCodec<? extends LootItemCondition> codec() {
        return this.codec;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}