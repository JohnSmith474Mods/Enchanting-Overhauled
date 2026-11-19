package johnsmith.enchantingoverhauled.mixin.client.render.block.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.MaterialSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {

    @Shadow @Final
    private MaterialSet materials;

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
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EnchantTableRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    private <S> void wrapSubmitModel(
            SubmitNodeCollector collector,
            Model<? super S> model,
            S modelState,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packedOverlay,
            int tintedColor,
            TextureAtlasSprite sprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original
    ) {
        RenderType tomeRenderType = ClientConstants.ENCHANTED_TOME_MATERIAL.renderType(RenderType::entityCutout);
        TextureAtlasSprite tomeSprite = this.materials.get(ClientConstants.ENCHANTED_TOME_MATERIAL);
        original.call(collector, model, modelState, poseStack, tomeRenderType, packedLight, packedOverlay, tintedColor, tomeSprite, outlineColor, crumblingOverlay);

        collector.order(1).submitModel(model, modelState, poseStack, RenderType.entityGlint(), packedLight, packedOverlay, tintedColor, null, outlineColor, crumblingOverlay);
    }
}