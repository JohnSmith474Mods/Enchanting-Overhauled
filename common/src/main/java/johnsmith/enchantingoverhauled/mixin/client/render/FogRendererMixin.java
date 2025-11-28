package johnsmith.enchantingoverhauled.mixin.client.render;

import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.effect.FluidVisibilityEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Map;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * Intercepts the call to updateBuffer inside setupFog.
     * The arguments for updateBuffer are:
     * 0: ByteBuffer buffer
     * 1: int position
     * 2: Vector4f fogColor
     * 3: float environmentalStart  <-- We want to modify this
     * 4: float environmentalEnd    <-- And this
     * 5: float renderDistanceStart
     * 6: float renderDistanceEnd
     * 7: float skyEnd
     * 8: float cloudEnd
     */
    @ModifyArgs(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
            )
    )
    private void modifyFogParameters(Args args, Camera camera, int renderDistance, boolean isFoggy, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level) {
        Entity entity = camera.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // 1. Check Fluid State at camera position
        FluidState fluidState = level.getFluidState(camera.getBlockPosition());
        if (fluidState.isEmpty()) {
            return;
        }

        // 2. Check for Helmet Enchantments
        ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        ItemEnchantments enchantments = helmet.getEnchantments();

        if (enchantments.isEmpty()) return;

        for (Map.Entry<Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey().value();

            // 3. Retrieve Custom Effect
            FluidVisibilityEffect effect = enchantment.effects().get(EnchantmentEffectComponentRegistry.CLEAR_WATER_VISION);

            // 4. Apply if Fluid Matches
            if (effect != null && fluidState.is(effect.fluids())) {
                int lvl = entry.getValue();

                // 5. Calculate New Values
                float newStart = effect.fogStart().calculate(lvl);

                // Convert render distance (chunks) to blocks for the end calculation
                float viewDistanceBlocks = renderDistance * 16.0F;
                float newEnd = viewDistanceBlocks * effect.fogEndMultiplier().calculate(lvl);

                // 6. Modify the arguments passed to updateBuffer
                args.set(3, newStart); // environmentalStart
                args.set(4, newEnd);   // environmentalEnd
                return;
            }
        }
    }
}