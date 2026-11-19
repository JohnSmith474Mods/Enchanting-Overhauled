package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.MirageEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic renderer for {@link MirageEntity} that delegates rendering to the caster's
 * {@link EntityRenderer}, preserving the caster's exact model, textures, layers, and animations.
 */
public class MirageRenderer extends EntityRenderer<MirageEntity, MirageRenderer.RenderState> {

    public static class RenderState extends EntityRenderState {
        @Nullable
        public MirageEntity entity;
        public float partialTick = 0.0F;
    }

    public MirageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public @NotNull RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(@NotNull MirageEntity entity, @NotNull RenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
    }

    @Override
    public void submit(
            @NotNull RenderState state,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState cameraRenderState
    ) {
        MirageEntity entity = state.entity;
        if (entity == null || !(entity.level() instanceof ClientLevel clientLevel)) {
            return;
        }
        Entity visualCaster = entity.getOrCreateVisualCaster(clientLevel);
        if (visualCaster == null || visualCaster instanceof MirageEntity) {
            return;
        }

        EntityRenderer<? super Entity, ?> casterRenderer = this.entityRenderDispatcher.getRenderer(visualCaster);
        if ((Object) casterRenderer instanceof MirageRenderer) {
            return;
        }

        float savedYRot = visualCaster.getYRot();
        float savedYRotO = visualCaster.yRotO;
        float savedXRot = visualCaster.getXRot();
        float savedXRotO = visualCaster.xRotO;

        float savedYBodyRot = 0.0F;
        float savedYBodyRotO = 0.0F;
        float savedYHeadRot = 0.0F;
        float savedYHeadRotO = 0.0F;
        int savedHurtTime = 0;
        int savedDeathTime = 0;

        if (visualCaster instanceof LivingEntity livingCaster) {
            savedYBodyRot = livingCaster.yBodyRot;
            savedYBodyRotO = livingCaster.yBodyRotO;
            savedYHeadRot = livingCaster.yHeadRot;
            savedYHeadRotO = livingCaster.yHeadRotO;
            savedHurtTime = livingCaster.hurtTime;
            savedDeathTime = livingCaster.deathTime;

            livingCaster.yBodyRot = entity.yBodyRot;
            livingCaster.yBodyRotO = entity.yBodyRotO;
            livingCaster.yHeadRot = entity.yHeadRot;
            livingCaster.yHeadRotO = entity.yHeadRotO;
            livingCaster.hurtTime = 0;
            livingCaster.deathTime = 0;
        }

        BookAnimationState savedBookState = null;
        if (visualCaster instanceof AbstractFlyingBookEntity bookCaster && bookCaster.isRoosting()) {
            savedBookState = bookCaster.getAnimationState();
        }

        visualCaster.setYRot(entity.getYRot());
        visualCaster.yRotO = entity.yRotO;
        visualCaster.setXRot(entity.getXRot());
        visualCaster.xRotO = entity.xRotO;

        poseStack.pushPose();
        try {
            if (savedBookState == null) {
                renderCaster(casterRenderer, visualCaster, state.partialTick, state.lightCoords, poseStack, submitNodeCollector, cameraRenderState);
            }
        } catch (Throwable ignored) {
        } finally {
            poseStack.popPose();

            visualCaster.setYRot(savedYRot);
            visualCaster.yRotO = savedYRotO;
            visualCaster.setXRot(savedXRot);
            visualCaster.xRotO = savedXRotO;

            if (visualCaster instanceof LivingEntity livingCaster) {
                livingCaster.yBodyRot = savedYBodyRot;
                livingCaster.yBodyRotO = savedYBodyRotO;
                livingCaster.yHeadRot = savedYHeadRot;
                livingCaster.yHeadRotO = savedYHeadRotO;
                livingCaster.hurtTime = savedHurtTime;
                livingCaster.deathTime = savedDeathTime;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Entity, S extends EntityRenderState> void renderCaster(
            EntityRenderer<E, S> renderer,
            Entity visualCaster,
            float partialTick,
            int lightCoords,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        E castEntity = (E) visualCaster;
        S casterState = renderer.createRenderState(castEntity, partialTick);
        casterState.lightCoords = lightCoords;
        renderer.submit(casterState, poseStack, submitNodeCollector, cameraRenderState);
    }
}