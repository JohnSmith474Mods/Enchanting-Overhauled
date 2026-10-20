package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class RoostedBookRenderer {

    private RoostedBookRenderer() {}

    public static boolean tryRenderRoosted(
            AbstractFlyingBookEntity entity,
            EnchantedBookModel bookModel,
            ResourceLocation texture,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        BookAnimationState state = entity.getAnimationState();
        if (state != BookAnimationState.ALTAR_VIGIL && state != BookAnimationState.LECTERN_ROOST) {
            return false;
        }

        BlockPos roostPos = entity.getRoostPos();
        if (roostPos == null) {
            return false;
        }

        double entityX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double entityZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        int blockLight = LevelRenderer.getLightColor(entity.level(), roostPos.above());
        int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);

        poseStack.pushPose();
        poseStack.translate(
                (double) roostPos.getX() - entityX,
                (double) roostPos.getY() - entityY,
                (double) roostPos.getZ() - entityZ
        );

        if (state == BookAnimationState.ALTAR_VIGIL) {
            renderOnAltar(entity, bookModel, texture, partialTick, poseStack, buffer, blockLight, overlay);
        } else {
            renderOnLectern(entity, roostPos, bookModel, texture, poseStack, buffer, blockLight, overlay);
        }

        poseStack.popPose();
        return true;
    }

    private static void renderOnAltar(
            AbstractFlyingBookEntity entity,
            EnchantedBookModel bookModel,
            ResourceLocation texture,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float time = (float) entity.altarTime + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);

        float deltaRot = entity.altarRot - entity.altarORot;
        while (deltaRot >= (float) Math.PI) {
            deltaRot -= ((float) Math.PI * 2.0F);
        }
        while (deltaRot < -(float) Math.PI) {
            deltaRot += ((float) Math.PI * 2.0F);
        }

        float interpolatedRot = entity.altarORot + deltaRot * partialTick;
        poseStack.mulPose(Axis.YP.rotation(-interpolatedRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

        float flip = Mth.lerp(partialTick, entity.altarOFlip, entity.altarFlip);
        float rightPageFlip = Mth.clamp(Mth.frac(flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float leftPageFlip = Mth.clamp(Mth.frac(flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float openness = Mth.lerp(partialTick, entity.altarOOpen, entity.altarOpen);

        bookModel.renderedOnLectern = false;
        bookModel.setupAnim(time, rightPageFlip, leftPageFlip, openness);

        VertexConsumer baseConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        bookModel.renderToBuffer(poseStack, baseConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        VertexConsumer glintConsumer = buffer.getBuffer(RenderType.entityGlint());
        bookModel.renderToBuffer(poseStack, glintConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
    }

    private static void renderOnLectern(
            AbstractFlyingBookEntity entity,
            BlockPos roostPos,
            EnchantedBookModel bookModel,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        BlockState blockState = entity.level().getBlockState(roostPos);
        Direction facing = blockState.hasProperty(LecternBlock.FACING)
                ? blockState.getValue(LecternBlock.FACING)
                : Direction.NORTH;

        poseStack.translate(0.5F, 1.0625F, 0.5F);
        float yRot = facing.getClockWise().toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
        poseStack.translate(0.0F, -0.125F, 0.0F);

        bookModel.renderedOnLectern = true;
        bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);

        VertexConsumer baseConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        bookModel.renderToBuffer(poseStack, baseConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        VertexConsumer glintConsumer = buffer.getBuffer(RenderType.entityGlint());
        bookModel.renderToBuffer(poseStack, glintConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
        bookModel.renderedOnLectern = false;
    }
}