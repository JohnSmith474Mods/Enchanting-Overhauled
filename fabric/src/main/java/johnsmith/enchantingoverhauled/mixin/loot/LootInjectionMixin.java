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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ReloadableServerRegistries.class, priority = 999)
public abstract class LootInjectionMixin {

    @Inject(method = "reload", at = @At("RETURN"), cancellable = true)
    private static void enchanting_overhauled$injectLoot(
            LayeredRegistryAccess<RegistryLayer> registryAccess,
            List<Registry.PendingTags<?>> postponedTags,
            ResourceManager resourceManager,
            Executor backgroundExecutor,
            // Fix 1: Correct Generic Type <ReloadableServerRegistries.LoadResult>
            CallbackInfoReturnable<CompletableFuture<ReloadableServerRegistries.LoadResult>> cir
    ) {
        CompletableFuture<ReloadableServerRegistries.LoadResult> originalFuture = cir.getReturnValue();

        // Fix 2: The future now yields a LoadResult, not LayeredRegistryAccess
        CompletableFuture<ReloadableServerRegistries.LoadResult> modifiedFuture = originalFuture.thenApply(loadResult -> {
            // Fix 3: Extract the registry access from the LoadResult record
            LayeredRegistryAccess<RegistryLayer> layers = loadResult.layers();
            RegistryAccess compositeAccess = layers.compositeAccess();

            // 1. Reload Injection Map
            FabricLootManager.reload(resourceManager);

            // 2. Iterate and Inject into Loot Tables
            Registry<LootTable> lootRegistry = compositeAccess.lookupOrThrow(Registries.LOOT_TABLE);

            for (Holder.Reference<LootTable> holder : lootRegistry.listElements().toList()) {
                FabricLootManager.tryInject(holder.value(), holder.key().location(), compositeAccess);
            }

            // Fix 4: Return the original 'loadResult' object to satisfy the Future signature
            return loadResult;
        });

        cir.setReturnValue(modifiedFuture);
    }
}