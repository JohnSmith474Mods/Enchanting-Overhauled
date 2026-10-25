package johnsmith.enchantingoverhauled.loot;

import johnsmith.enchantingoverhauled.api.accessor.LootTableAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import johnsmith.enchantingoverhauled.api.loot.LootInjection;
import johnsmith.enchantingoverhauled.api.loot.LootInjections;
import johnsmith.enchantingoverhauled.server.AdminSchoolValidator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LootInjectionHandler {

    public static void applyInjections(MinecraftServer server) {
        if (server == null) return;

        DifficultyContext.setServer(server);
        ArcaneItemNamingUtil.setServerRegistryAccess(server.registryAccess());
        AdminSchoolValidator.validateAndAlert(server);

        HolderLookup.Provider reloadableAccess = (HolderLookup.Provider) server.reloadableRegistries().lookup();

        Optional<? extends HolderLookup.RegistryLookup<LootInjection>> injectionRegistryOpt = reloadableAccess.lookup(LootInjections.REGISTRY_KEY);
        if (injectionRegistryOpt.isEmpty()) {
            injectionRegistryOpt = server.registryAccess().lookup(LootInjections.REGISTRY_KEY);
        }
        if (injectionRegistryOpt.isEmpty()) return;

        HolderLookup.RegistryLookup<LootInjection> injectionRegistry = injectionRegistryOpt.get();
        HolderLookup.RegistryLookup<LootTable> lootTableRegistry = reloadableAccess.lookupOrThrow(Registries.LOOT_TABLE);

        for (Holder.Reference<LootInjection> injectionHolder : injectionRegistry.listElements().toList()) {
            LootInjection injection = injectionHolder.value();
            for (ResourceKey<LootTable> targetKey : injection.targets()) {
                Optional<Holder.Reference<LootTable>> optionalTable = lootTableRegistry.get(targetKey);
                if (optionalTable.isPresent() && optionalTable.get().value() instanceof LootTableAccessor accessor) {
                    List<LootPool> existingPools = accessor.enchanting_overhauled$getPools();
                    List<LootPool> newPools = new ArrayList<>(existingPools);

                    for (ResourceKey<LootTable> injectedKey : injection.injections()) {
                        LootPool pool = LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(NestedLootTable.lootTableReference(injectedKey))
                                .build();
                        newPools.add(pool);
                    }

                    accessor.enchanting_overhauled$setPools(List.copyOf(newPools));
                }
            }
        }
    }
}