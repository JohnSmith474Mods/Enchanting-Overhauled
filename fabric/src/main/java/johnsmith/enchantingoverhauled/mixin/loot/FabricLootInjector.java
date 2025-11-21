package johnsmith.enchantingoverhauled.mixin.loot;

import johnsmith.enchantingoverhauled.platform.loot.FabricLootManager;

import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ReloadableServerRegistries.class, priority = 999, remap = false)
public abstract class FabricLootInjector {

    @Inject(method = "method_58279", at = @At("RETURN"))
    private static <T> void enchanting_overhauled$onLoadLootTables(LootDataType<T> type, ResourceManager resourceManager, RegistryOps<JsonElement> ops, CallbackInfoReturnable<WritableRegistry<?>> cir) {
        if (type == LootDataType.TABLE) {
            FabricLootManager.reload(resourceManager);
            Registry<LootTable> registry = (Registry<LootTable>) cir.getReturnValue();
            for (Holder.Reference<LootTable> holder : registry.holders().toList()) {
                FabricLootManager.tryInject(holder.value(), holder.key().location());
            }
        }
    }
}