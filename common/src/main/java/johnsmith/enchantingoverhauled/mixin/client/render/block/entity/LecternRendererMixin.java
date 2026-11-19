package johnsmith.enchantingoverhauled.mixin.client.render.block.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.blockentity.LecternRenderStateAccessor;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.item.Items;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to dynamically swap the rendered book model and texture on Lecterns
 * based on the item currently stored within it.
 */
@Mixin(LecternRenderer.class)
public class LecternRendererMixin {

    @Shadow @Final
    private MaterialSet materials;

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

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/level/block/entity/LecternBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/LecternRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL")
    )
    private void enchanting_overhauled$extractBookMetadata(
            LecternBlockEntity blockEntity,
            LecternRenderState state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfo ci
    ) {
        if (state instanceof LecternRenderStateAccessor accessor) {
            ItemStack stack = blockEntity.getBook();
            int bookKind = LecternRenderStateAccessor.BOOK_KIND_DEFAULT;
            if (stack.is(Items.ENCHANTED_TOME.get())) {
                bookKind = LecternRenderStateAccessor.BOOK_KIND_ENCHANTED_TOME;
            } else if (stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK) || stack.is(Items.ENCHANTED_PAGE.get())) {
                bookKind = LecternRenderStateAccessor.BOOK_KIND_ENCHANTED_BOOK;
            }
            accessor.enchanting_overhauled$setBookKind(bookKind);
            accessor.enchanting_overhauled$setHasFoil(stack.hasFoil());
        }
    }

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/LecternRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    @SuppressWarnings("unchecked")
    private <S> void enchanting_overhauled$wrapSubmitModel(
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
            Operation<Void> original,
            @Local(argsOnly = true) LecternRenderState renderState
    ) {
        Model<? super S> modelToUse = model;
        RenderType renderTypeToUse = renderType;
        TextureAtlasSprite spriteToUse = sprite;
        boolean hasFoil = false;

        if (renderState instanceof LecternRenderStateAccessor accessor) {
            int bookKind = accessor.enchanting_overhauled$getBookKind();
            hasFoil = accessor.enchanting_overhauled$hasFoil();
            Material materialToUse = null;
            if (bookKind == LecternRenderStateAccessor.BOOK_KIND_ENCHANTED_TOME) {
                modelToUse = (Model<? super S>) (BookModel) this.enchanting_overhauled$enchantedTomeModel;
                materialToUse = ClientConstants.ENCHANTED_TOME_MATERIAL;
            } else if (bookKind == LecternRenderStateAccessor.BOOK_KIND_ENCHANTED_BOOK) {
                modelToUse = (Model<? super S>) (BookModel) this.enchanting_overhauled$enchantedBookModel;
                materialToUse = ClientConstants.ENCHANTED_BOOK_MATERIAL;
            }
            if (materialToUse != null) {
                renderTypeToUse = materialToUse.renderType(RenderType::entityCutout);
                spriteToUse = this.materials.get(materialToUse);
            }
        }

        original.call(collector, modelToUse, modelState, poseStack, renderTypeToUse, packedLight, packedOverlay, tintedColor, spriteToUse, outlineColor, crumblingOverlay);

        if (hasFoil) {
            collector.order(1).submitModel(modelToUse, modelState, poseStack, RenderType.entityGlint(), packedLight, packedOverlay, tintedColor, null, outlineColor, crumblingOverlay);
        }
    }
}