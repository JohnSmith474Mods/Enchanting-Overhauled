package johnsmith.enchantingoverhauled.mixin.client.render.block.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.item.Items;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/**
 * Mixin to dynamically swap the rendered book model and texture on Lecterns
 * based on the item currently stored within it.
 */
@Mixin(LecternRenderer.class)
public class LecternRendererMixin {

    @Unique
    private EnchantedBookModel enchanting_overhauled$enchantedBookModel;

    @Unique
    private EnchantedBookModel enchanting_overhauled$enchantedTomeModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        this.enchanting_overhauled$enchantedBookModel = new EnchantedBookModel(context.bakeLayer(ClientConstants.ENCHANTED_BOOK_LAYER));
        this.enchanting_overhauled$enchantedBookModel.renderedOnLectern = true;

        this.enchanting_overhauled$enchantedTomeModel = new EnchantedBookModel(context.bakeLayer(ClientConstants.ENCHANTED_TOME_LAYER));
        this.enchanting_overhauled$enchantedTomeModel.renderedOnLectern = true;
    }

    /**
     * Resolves the appropriate model variant to render.
     */
    @Unique
    private BookModel enchanting_overhauled$getModel(LecternBlockEntity blockEntity, BookModel defaultModel) {
        ItemStack stack = blockEntity.getBook();
        if (stack.is(Items.ENCHANTED_TOME.get())) {
            return this.enchanting_overhauled$enchantedTomeModel;
        } else if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) || stack.is(Items.ENCHANTED_PAGE.get())) {
            return this.enchanting_overhauled$enchantedBookModel;
        }
        return defaultModel;
    }

    /**
     * Resolves the appropriate material/texture variant to render.
     */
    @Unique
    private Material enchanting_overhauled$getMaterial(LecternBlockEntity blockEntity, Material defaultMaterial) {
        ItemStack stack = blockEntity.getBook();
        if (stack.is(Items.ENCHANTED_TOME.get())) {
            return ClientConstants.ENCHANTED_TOME_MATERIAL;
        } else if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) || stack.is(Items.ENCHANTED_PAGE.get())) {
            return ClientConstants.ENCHANTED_BOOK_MATERIAL;
        }
        return defaultMaterial;
    }

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/BookModel;setupAnim(FFFF)V")
    )
    private void wrapSetupAnim(BookModel instance, float f1, float f2, float f3, float f4, Operation<Void> original, LecternBlockEntity blockEntity) {
        BookModel modelToUse = this.enchanting_overhauled$getModel(blockEntity, instance);
        modelToUse.setupAnim(f1, f2, f3, f4);
    }

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/BookModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
    )
    private void wrapRender(BookModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, Operation<Void> original, LecternBlockEntity blockEntity) {
        BookModel modelToUse = this.enchanting_overhauled$getModel(blockEntity, instance);
        modelToUse.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/BookModel;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
    )
    private void wrapRender(BookModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color, Operation<Void> original, LecternBlockEntity blockEntity, float partialTick, PoseStack poseStackParam, MultiBufferSource bufferSource) {
        BookModel modelToUse = this.enchanting_overhauled$getModel(blockEntity, instance);

        modelToUse.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        if (blockEntity.getBook().hasFoil()) {
            VertexConsumer glintBuffer = bufferSource.getBuffer(RenderType.entityGlint());
            modelToUse.render(poseStack, glintBuffer, packedLight, packedOverlay, color);
        }
    }

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/Material;buffer(Lnet/minecraft/client/renderer/MultiBufferSource;Ljava/util/function/Function;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer wrapBuffer(Material instance, MultiBufferSource bufferSource, Function<ResourceLocation, RenderType> renderType, Operation<VertexConsumer> original, LecternBlockEntity blockEntity) {
        Material materialToUse = this.enchanting_overhauled$getMaterial(blockEntity, instance);
        return materialToUse.buffer(bufferSource, RenderType::entityCutout);
    }
}