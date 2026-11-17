package johnsmith.enchantingoverhauled.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.effect.FluidVisibilityEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V",
            at = @At("TAIL"))
    private static void applyLevelBasedWaterVisibility(
            Camera camera,
            FogRenderer.FogMode fogMode,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfo ci,
            @Local FogType fogType,
            @Local Entity entity
    ) {
        // 1. Quick check: Are we actually in a fog type that warrants checking fluids?
        if (fogType == FogType.NONE || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(helmet);

        if (enchantments.isEmpty()) return;

        // 2. Retrieve the actual FluidState from the world using the camera position
        FluidState fluidState = entity.level().getFluidState(camera.getBlockPosition());

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            FluidVisibilityEffect effect = entry.getKey().value().effects().get(EnchantmentEffectComponentRegistry.CLEAR_WATER_VISION);

            // 3. Check if the current fluid matches the effect's configuration
            if (effect != null && fluidState.is(effect.fluids())) {
                int level = entry.getValue();

                // 4. Calculate dynamic values
                float start = effect.fogStart().calculate(level);
                float endMultiplier = effect.fogEndMultiplier().calculate(level);

                // 5. Apply Fog Settings
                RenderSystem.setShaderFogStart(start);
                RenderSystem.setShaderFogEnd(viewDistance * endMultiplier);
                return;
            }
        }
    }
}