package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.MirageEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

/**
 * Generic renderer for {@link MirageEntity} that delegates rendering to the caster's
 * {@link EntityRenderer}, preserving the caster's exact model, textures, layers, and animations.
 */
public class MirageRenderer extends EntityRenderer<MirageEntity> {

    public MirageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            @NotNull MirageEntity entity,
            float entityYaw,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        if (!(entity.level() instanceof ClientLevel clientLevel)) {
            return;
        }

        Entity visualCaster = entity.getOrCreateVisualCaster(clientLevel);
        if (visualCaster == null || visualCaster instanceof MirageEntity) {
            return;
        }

        EntityRenderer<? super Entity> casterRenderer = this.entityRenderDispatcher.getRenderer(visualCaster);
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
                casterRenderer.render(visualCaster, entityYaw, partialTick, poseStack, buffer, packedLight);
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

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MirageEntity entity) {
        if (entity.level() instanceof ClientLevel clientLevel) {
            Entity visualCaster = entity.getOrCreateVisualCaster(clientLevel);
            if (visualCaster != null && !(visualCaster instanceof MirageEntity)) {
                EntityRenderer<? super Entity> casterRenderer = this.entityRenderDispatcher.getRenderer(visualCaster);
                if (!((Object) casterRenderer instanceof MirageRenderer)) {
                    return casterRenderer.getTextureLocation(visualCaster);
                }
            }
        }
        return InventoryMenu.BLOCK_ATLAS;
    }
}