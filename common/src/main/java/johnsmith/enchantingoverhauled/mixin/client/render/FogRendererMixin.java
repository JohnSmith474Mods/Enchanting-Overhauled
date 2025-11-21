package johnsmith.enchantingoverhauled.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.material.FogType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V",
                at = @At("TAIL"))
    private static void makeWaterClearWithAquaAffinity(
            Camera camera,
            FogRenderer.FogMode fogMode,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfo ci,
            @Local FogType fogType,
            @Local Entity entity
    ) {

        if (fogType != FogType.WATER) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.AQUA_AFFINITY, helmet) > 0) {

                // Override the fog settings.
                RenderSystem.setShaderFogStart(-viewDistance); // Start fog behind the player
                RenderSystem.setShaderFogEnd(viewDistance * 2.0f); // End fog very far away
            }
        }
    }
}