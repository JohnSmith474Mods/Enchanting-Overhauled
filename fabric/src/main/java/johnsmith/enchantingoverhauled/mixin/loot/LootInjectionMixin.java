package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.platform.loot.FabricLootManager;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ReloadableServerRegistries.class, priority = 999, remap = false)
public abstract class LootInjectionMixin {

    @Inject(method = "reload", at = @At("RETURN"), cancellable = true)
    private static void enchanting_overhauled$injectLoot(
            LayeredRegistryAccess<RegistryLayer> registries,
            ResourceManager resourceManager,
            Executor backgroundExecutor,
            CallbackInfoReturnable<CompletableFuture<LayeredRegistryAccess<RegistryLayer>>> cir
    ) {
        CompletableFuture<LayeredRegistryAccess<RegistryLayer>> originalFuture = cir.getReturnValue();

        CompletableFuture<LayeredRegistryAccess<RegistryLayer>> modifiedFuture = originalFuture.thenApply(layeredAccess -> {
            RegistryAccess registryAccess = layeredAccess.compositeAccess();

            // 1. Reload Injection Map
            FabricLootManager.reload(resourceManager);

            // 2. Iterate and Inject into Loot Tables
            Registry<LootTable> lootRegistry = registryAccess.registryOrThrow(Registries.LOOT_TABLE);

            for (Holder.Reference<LootTable> holder : lootRegistry.holders().toList()) {
                // FabricLootManager.tryInject now accepts registryAccess to find tags for EnchantRandomlyFunction
                FabricLootManager.tryInject(holder.value(), holder.key().location(), registryAccess);
            }

            return layeredAccess;
        });

        cir.setReturnValue(modifiedFuture);
    }
}