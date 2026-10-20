package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import johnsmith.enchantmentcore.api.enchantment.spellfield.entity.AnchorVisualConfig;
import johnsmith.enchantmentcore.api.enchantment.spellfield.entity.SpellFieldAnchorEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class AnchorModelRenderer extends EntityRenderer<SpellFieldAnchorEntity> {

    public AnchorModelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            @NotNull SpellFieldAnchorEntity entity,
            float entityYaw,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        renderAnchor(entity, null, partialTick, poseStack, buffer, packedLight);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    public static void renderAnchor(
            SpellFieldAnchorEntity entity,
            @Nullable AnchorVisualConfig config,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        float ageInTicks = entity.tickCount + partialTick;
        float scale = config != null ? config.scale() : 1.0F;
        int tint = config != null ? config.tint() : 0xFFFFFF;
        int frameCount = config != null ? Math.max(1, config.frameCount()) : 1;
        int ticksPerFrame = config != null ? Math.max(1, config.ticksPerFrame()) : 1;
        ResourceLocation texture = config != null ? config.texture().orElse(null) : null;
        String modelPath = config != null && config.modelId().isPresent()
                ? config.modelId().get().getPath()
                : "sigil_ring";

        int r = (tint >> 16) & 0xFF;
        int g = (tint >> 8) & 0xFF;
        int b = tint & 0xFF;

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        if (texture != null) {
            int currentFrame = (entity.tickCount / ticksPerFrame) % frameCount;
            float v0 = (float) currentFrame / (float) frameCount;
            float v1 = (float) (currentFrame + 1) / (float) frameCount;
            renderTexturedGroundSigil(poseStack, buffer, texture, ageInTicks, v0, v1, r, g, b);
        }

        switch (modelPath) {
            case "sphere", "arcane_sphere", "dome" -> renderArcaneSphere(poseStack, buffer, ageInTicks, r, g, b);
            case "pillar", "obelisk", "cylinder" -> renderArcanePillar(poseStack, buffer, ageInTicks, r, g, b);
            default -> renderArcaneGyroRings(poseStack, buffer, ageInTicks, r, g, b);
        }

        poseStack.popPose();
    }

    private static void renderTexturedGroundSigil(
            PoseStack poseStack,
            MultiBufferSource buffer,
            ResourceLocation texture,
            float ageInTicks,
            float v0,
            float v1,
            int r,
            int g,
            int b
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.02F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-ageInTicks * 2.5F));

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int light = LightTexture.FULL_BRIGHT;

        consumer.addVertex(pose, -1.0F, 0.0F, -1.0F).setColor(r, g, b, 230).setUv(0.0F, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, -1.0F, 0.0F, 1.0F).setColor(r, g, b, 230).setUv(0.0F, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, 1.0F).setColor(r, g, b, 230).setUv(1.0F, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 1.0F, 0.0F, -1.0F).setColor(r, g, b, 230).setUv(1.0F, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);

        poseStack.popPose();
    }

    private static void renderArcaneGyroRings(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float ageInTicks,
            int r,
            int g,
            int b
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.05F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-ageInTicks * 3.0F));
        drawRing(poseStack.last().pose(), consumer, 1.0F, 0.06F, 24, r, g, b, 180);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.08F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 4.5F));
        drawRing(poseStack.last().pose(), consumer, 0.65F, 0.045F, 20, 255, 255, 255, 210);
        poseStack.popPose();
    }

    private static void renderArcaneSphere(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float ageInTicks,
            int r,
            int g,
            int b
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        float pulse = 1.0F + Mth.sin(ageInTicks * 0.15F) * 0.06F;

        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.8F, 0.0F);
            poseStack.scale(pulse, pulse, pulse);
            poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * (2.5F + i) + i * 60.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(45.0F + i * 45.0F));
            drawRing(poseStack.last().pose(), consumer, 0.85F, 0.05F, 24, r, g, b, 160);
            poseStack.popPose();
        }
    }

    private static void renderArcanePillar(
            PoseStack poseStack,
            MultiBufferSource buffer,
            float ageInTicks,
            int r,
            int g,
            int b
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(ageInTicks * 4.0F));
        Matrix4f matrix = poseStack.last().pose();

        int sides = 12;
        float radius = 0.45F;
        float height = 2.2F;
        for (int i = 0; i < sides; i++) {
            float a0 = (float) (i * Math.PI * 2.0D / sides);
            float a1 = (float) ((i + 1) * Math.PI * 2.0D / sides);
            float x0 = Mth.cos(a0) * radius;
            float z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius;
            float z1 = Mth.sin(a1) * radius;

            consumer.addVertex(matrix, x0, 0.0F, z0).setColor(r, g, b, 190);
            consumer.addVertex(matrix, x1, 0.0F, z1).setColor(r, g, b, 190);
            consumer.addVertex(matrix, x1, height, z1).setColor(r, g, b, 0);
            consumer.addVertex(matrix, x0, height, z0).setColor(r, g, b, 0);
        }
        poseStack.popPose();
    }

    private static void drawRing(
            Matrix4f matrix,
            VertexConsumer consumer,
            float radius,
            float thickness,
            int segments,
            int r,
            int g,
            int b,
            int a
    ) {
        float inner = Math.max(0.01F, radius - thickness);
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (i * Math.PI * 2.0D / segments);
            float a1 = (float) ((i + 1) * Math.PI * 2.0D / segments);

            float c0 = Mth.cos(a0);
            float s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1);
            float s1 = Mth.sin(a1);

            consumer.addVertex(matrix, c0 * inner, 0.0F, s0 * inner).setColor(r, g, b, a);
            consumer.addVertex(matrix, c0 * radius, 0.0F, s0 * radius).setColor(r, g, b, a);
            consumer.addVertex(matrix, c1 * radius, 0.0F, s1 * radius).setColor(r, g, b, a);
            consumer.addVertex(matrix, c1 * inner, 0.0F, s1 * inner).setColor(r, g, b, a);

            consumer.addVertex(matrix, c1 * inner, 0.0F, s1 * inner).setColor(r, g, b, a);
            consumer.addVertex(matrix, c1 * radius, 0.0F, s1 * radius).setColor(r, g, b, a);
            consumer.addVertex(matrix, c0 * radius, 0.0F, s0 * radius).setColor(r, g, b, a);
            consumer.addVertex(matrix, c0 * inner, 0.0F, s0 * inner).setColor(r, g, b, a);
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SpellFieldAnchorEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}