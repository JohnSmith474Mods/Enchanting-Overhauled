package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.api.accessor.LootTableAccessor;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(LootTable.class)
public abstract class LootTableMixin implements LootTableAccessor {

    @Mutable
    @Shadow
    @Final
    private List<LootPool> pools;

    @Override
    public List<LootPool> enchanting_overhauled$getPools() {
        return this.pools;
    }

    @Override
    public void enchanting_overhauled$setPools(List<LootPool> pools) {
        this.pools = pools;
    }
}