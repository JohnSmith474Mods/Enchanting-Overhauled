package johnsmith.enchantingoverhauled.loot;

import johnsmith.enchantingoverhauled.api.accessor.LootTableAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import johnsmith.enchantingoverhauled.api.loot.LootInjection;
import johnsmith.enchantingoverhauled.api.loot.LootInjections;
import johnsmith.enchantingoverhauled.server.AdminSchoolValidator;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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

        RegistryAccess registryAccess = server.reloadableRegistries().get();

        Optional<Registry<LootInjection>> injectionRegistryOpt = registryAccess.registry(LootInjections.REGISTRY_KEY);
        if (injectionRegistryOpt.isEmpty()) return;

        Registry<LootInjection> injectionRegistry = injectionRegistryOpt.get();
        Registry<LootTable> lootTableRegistry = registryAccess.registryOrThrow(Registries.LOOT_TABLE);

        for (LootInjection injection : injectionRegistry) {
            for (ResourceKey<LootTable> targetKey : injection.targets()) {
                Optional<LootTable> optionalTable = lootTableRegistry.getOptional(targetKey);

                if (optionalTable.isPresent() && optionalTable.get() instanceof LootTableAccessor accessor) {
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