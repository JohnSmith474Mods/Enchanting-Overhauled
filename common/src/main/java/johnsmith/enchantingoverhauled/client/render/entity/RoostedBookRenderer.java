package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
            EnchantedBookModel altarBookModel,
            EnchantedBookModel lecternBookModel,
            ResourceLocation texture,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int outlineColor
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
        int overlay = OverlayTexture.pack(OverlayTexture.u(0.0F), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));

        poseStack.pushPose();
        poseStack.translate(
                (double) roostPos.getX() - entityX,
                (double) roostPos.getY() - entityY,
                (double) roostPos.getZ() - entityZ
        );

        if (state == BookAnimationState.ALTAR_VIGIL) {
            renderOnAltar(entity, altarBookModel, texture, partialTick, poseStack, submitNodeCollector, blockLight, overlay, outlineColor);
        } else {
            renderOnLectern(entity, roostPos, lecternBookModel, texture, poseStack, submitNodeCollector, blockLight, overlay, outlineColor);
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
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            int packedOverlay,
            int outlineColor
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

        BookModel.State bookState = new BookModel.State(time, rightPageFlip, leftPageFlip, openness);
        submitNodeCollector.submitModel(
                bookModel,
                bookState,
                poseStack,
                RenderType.entityCutoutNoCull(texture),
                packedLight,
                packedOverlay,
                0xFFFFFFFF,
                null,
                outlineColor,
                null
        );
        submitNodeCollector.order(1).submitModel(
                bookModel,
                bookState,
                poseStack,
                RenderType.entityGlint(),
                packedLight,
                packedOverlay,
                0xFFFFFFFF,
                null,
                outlineColor,
                null
        );
    }

    private static void renderOnLectern(
            AbstractFlyingBookEntity entity,
            BlockPos roostPos,
            EnchantedBookModel bookModel,
            ResourceLocation texture,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            int packedOverlay,
            int outlineColor
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

        BookModel.State bookState = new BookModel.State(0.0F, 0.1F, 0.9F, 1.2F);
        submitNodeCollector.submitModel(
                bookModel,
                bookState,
                poseStack,
                RenderType.entityCutoutNoCull(texture),
                packedLight,
                packedOverlay,
                0xFFFFFFFF,
                null,
                outlineColor,
                null
        );
        submitNodeCollector.order(1).submitModel(
                bookModel,
                bookState,
                poseStack,
                RenderType.entityGlint(),
                packedLight,
                packedOverlay,
                0xFFFFFFFF,
                null,
                outlineColor,
                null
        );
    }
}