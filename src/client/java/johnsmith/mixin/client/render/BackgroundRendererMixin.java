package johnsmith.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Inject(method = "applyFog(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/BackgroundRenderer$FogType;FZF)V",
                at = @At("TAIL"))
    private static void makeWaterClearWithAquaAffinity(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfo ci,
            // --- Local Variables ---
            @Local CameraSubmersionType cameraSubmersionType, // Capture the submersion type
            @Local Entity entity
    ) {

        // Use the captured local variable
        if (cameraSubmersionType != CameraSubmersionType.WATER) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            ItemStack helmet = livingEntity.getEquippedStack(EquipmentSlot.HEAD);

            // Check if the helmet has Aqua Affinity
            if (EnchantmentHelper.getLevel(Enchantments.AQUA_AFFINITY, helmet) > 0) {

                // Override the fog settings.
                // Setting density to 0 is not an option here,
                // so we set start/end to create a very clear view.
                RenderSystem.setShaderFogStart(-viewDistance); // Start fog behind the player
                RenderSystem.setShaderFogEnd(viewDistance * 2.0f); // End fog very far away
            }
        }
    }
}