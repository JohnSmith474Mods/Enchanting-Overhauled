package johnsmith.enchantingoverhauled.mixin.client.render.block.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;

import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {

    @Shadow @Final @Mutable
    private BookModel bookModel; // Yarn: book, MojMap: bookModel

    /**
     * Injects into the renderer's constructor to replace the vanilla book model
     * with our custom one.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(BlockEntityRendererProvider.Context context, CallbackInfo ci) {

        // 1. Get the root ModelPart from our custom layer
        ModelPart tomeRoot = context.bakeLayer(ClientConstants.ENCHANTED_TOME_LAYER);

        // 2. Replace the renderer's book model with our new one
        this.bookModel = new EnchantedBookModel(tomeRoot);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/level/block/entity/EnchantingTableBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;buffer(Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer swapMaterial(Material instance, MultiBufferSource bufferSource, java.util.function.Function<ResourceLocation, RenderType> renderType, Operation<VertexConsumer> original) {
        return ClientConstants.ENCHANTED_TOME_MATERIAL.buffer(bufferSource, RenderType::entityCutout);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/level/block/entity/EnchantingTableBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/BookModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
    )
    private void wrapRender(BookModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, Operation<Void> original, @Local(argsOnly = true) MultiBufferSource multiBufferSource) {
        original.call(instance, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        VertexConsumer glintBuffer = multiBufferSource.getBuffer(RenderType.entityGlint());
        instance.render(poseStack, glintBuffer, packedLight, packedOverlay, color);
    }
}