package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import johnsmith.enchantingoverhauled.entity.projectile.MagicMissileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class MagicMissileRenderer extends EntityRenderer<MagicMissileEntity> {

    public MagicMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(@NotNull MagicMissileEntity entity, @NotNull BlockPos pos) {
        return 15;
    }

    @Override
    public void render(
            @NotNull MagicMissileEntity entity,
            float entityYaw,
            float partialTicks,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        float ageInTicks = entity.tickCount + partialTicks;
        float pulse = 0.9F + Mth.sin(ageInTicks * 0.6F) * 0.15F;

        int schoolColor = entity.getSchoolColor();
        int sr = (schoolColor >> 16) & 0xFF;
        int sg = (schoolColor >> 8) & 0xFF;
        int sb = schoolColor & 0xFF;

        int themeColor = entity.getThemeColor();
        int tr = (themeColor >> 16) & 0xFF;
        int tg = (themeColor >> 8) & 0xFF;
        int tb = themeColor & 0xFF;

        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(ageInTicks * 24.0F));
        poseStack.scale(pulse, pulse, pulse);

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();

        renderCrossedFins(matrix, consumer, 0.22F, 0.08F, sr, sg, sb, 165);
        renderCrossedFins(matrix, consumer, 0.14F, 0.045F, tr, tg, tb, 220);
        renderCrossedFins(matrix, consumer, 0.09F, 0.025F, 255, 255, 255, 255);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void renderCrossedFins(
            Matrix4f matrix,
            VertexConsumer consumer,
            float length,
            float halfWidth,
            int r,
            int g,
            int b,
            int a
    ) {
        drawDoubleSidedQuad(matrix, consumer, -length, -halfWidth, 0.0F, length, halfWidth, 0.0F, r, g, b, a);
        drawDoubleSidedQuad(matrix, consumer, -length, 0.0F, -halfWidth, length, 0.0F, halfWidth, r, g, b, a);
        drawDoubleSidedQuad(matrix, consumer, 0.0F, -halfWidth, -halfWidth, 0.0F, halfWidth, halfWidth, r, g, b, a);
    }

    private static void drawDoubleSidedQuad(
            Matrix4f matrix,
            VertexConsumer consumer,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            int r,
            int g,
            int b,
            int a
    ) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);

        consumer.addVertex(matrix, x0, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y0, z0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MagicMissileEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}