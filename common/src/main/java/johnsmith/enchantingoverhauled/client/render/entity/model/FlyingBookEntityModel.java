package johnsmith.enchantingoverhauled.client.render.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class FlyingBookEntityModel<T extends AbstractFlyingBookEntity> extends EntityModel<T> {

    private static final float VANILLA_MODEL_ORIGIN_Y = 24.0F / 16.0F;
    private static final float HITBOX_CENTER_Y = 0.3F;
    private static final float DEFAULT_HITBOX_Y_OFFSET = VANILLA_MODEL_ORIGIN_Y - HITBOX_CENTER_Y;

    private final BookModel internalModel;
    private float currentX;
    private float currentY = DEFAULT_HITBOX_Y_OFFSET;
    private float currentZ;
    private final Quaternionf renderRotation = new Quaternionf();

    public FlyingBookEntityModel(BookModel internalModel) {
        this.internalModel = internalModel;
    }

    public BookModel getInternalModel() {
        return this.internalModel;
    }

    @Override
    public void setupAnim(
            @NotNull T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        float partialTick = Mth.clamp(ageInTicks - (float) entity.tickCount, 0.0F, 1.0F);
        BookAnimationState state = entity.getAnimationState();

        float baseX = Mth.lerp(partialTick, entity.animXO, entity.animX);
        float baseY = Mth.lerp(partialTick, entity.animYO, entity.animY);
        float baseZ = Mth.lerp(partialTick, entity.animZO, entity.animZ);
        float currentOpenness = Mth.lerp(partialTick, entity.animOpennessO, entity.animOpenness);

        float phaseOffset = (entity.getId() * 1.37F);
        float bobY = computeHoverBob(state, ageInTicks, phaseOffset);

        float chantShakeX = 0.0F;
        float chantShakeY = 0.0F;
        if (state.isChanting()) {
            float castProgress = Mth.clamp(((float) entity.getClientStateTicks() + partialTick) / 40.0F, 0.0F, 1.0F);
            bobY -= Mth.sin(castProgress * (float) Math.PI * 0.5F) * 0.14F;
            float tremorAmp = castProgress * castProgress * 0.025F;
            chantShakeX = Mth.sin(ageInTicks * 2.4F) * tremorAmp;
            chantShakeY = Mth.cos(ageInTicks * 3.1F) * tremorAmp;
        }

        float recoilZ = 0.0F;
        float recoilPitch = 0.0F;
        if (entity.hurtTime > 0) {
            float hurtProgress = Math.max(0.0F, (float) entity.hurtTime - partialTick) / (float) Math.max(1, entity.hurtDuration);
            float spring = Mth.sin(hurtProgress * (float) Math.PI * 2.5F) * hurtProgress;
            recoilZ = -0.14F * spring;
            recoilPitch = -22.0F * Mth.DEG_TO_RAD * spring;
            currentOpenness = Mth.clamp(currentOpenness - 0.35F * hurtProgress, 0.0F, 1.0F);
        }

        this.currentX = baseX + chantShakeX;
        this.currentY = baseY + bobY + chantShakeY;
        this.currentZ = baseZ + recoilZ;

        this.renderRotation.set(entity.animRotationO).slerp(entity.animRotation, partialTick);

        if (state != BookAnimationState.RESTING
                && state != BookAnimationState.LECTERN_ROOST
                && state != BookAnimationState.ALTAR_VIGIL
                && state != BookAnimationState.CHANTING_DEFENSIVE
                && state != BookAnimationState.DASHING) {
            Vec3 vel = entity.getDeltaMovement();
            float bodyYawRad = Mth.lerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
            float sinYaw = Mth.sin(bodyYawRad);
            float cosYaw = Mth.cos(bodyYawRad);

            double forwardVel = -vel.x * sinYaw + vel.z * cosYaw;
            double lateralVel = vel.x * cosYaw + vel.z * sinYaw;

            float bankPitch = Mth.clamp((float) forwardVel * 0.55F, -0.35F, 0.35F);
            float bankRoll = Mth.clamp((float) lateralVel * 0.55F, -0.35F, 0.35F);
            float ambientSway = Mth.cos((ageInTicks + phaseOffset) * 0.08F) * 0.03F;

            this.renderRotation.rotateLocalX(bankPitch + recoilPitch);
            this.renderRotation.rotateLocalZ(bankRoll + ambientSway);
        } else if (recoilPitch != 0.0F) {
            this.renderRotation.rotateLocalX(recoilPitch);
        }

        float flip = Mth.lerp(partialTick, entity.oPageFlip, entity.pageFlip);
        float rightPageFlip = Mth.clamp(Mth.frac(flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float leftPageFlip = Mth.clamp(Mth.frac(flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);

        if (state == BookAnimationState.DASHING || state == BookAnimationState.SCOUTING || state == BookAnimationState.RESTING) {
            rightPageFlip = 0.0F;
            leftPageFlip = 1.0F;
        }

        if (this.internalModel instanceof EnchantedBookModel enchantedBookModel) {
            enchantedBookModel.renderedOnLectern = false;
        }
        this.internalModel.setupAnim(ageInTicks, rightPageFlip, leftPageFlip, currentOpenness);
    }

    private static float computeHoverBob(BookAnimationState state, float ageInTicks, float phaseOffset) {
        if (state.isChanting()) {
            return Mth.sin((ageInTicks + phaseOffset) * 0.2F) * 0.035F;
        }
        return switch (state) {
            case RESTING, LECTERN_ROOST, ALTAR_VIGIL, DASHING -> 0.0F;
            case BARTERING -> Mth.sin((ageInTicks + phaseOffset) * 0.2F) * 0.035F;
            case TARGET_ACQUISITION -> Mth.sin((ageInTicks + phaseOffset) * 0.15F) * 0.04F;
            default -> Mth.sin((ageInTicks + phaseOffset) * 0.11F) * 0.055F;
        };
    }

    @Override
    public void renderToBuffer(
            @NotNull PoseStack poseStack,
            @NotNull VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        poseStack.pushPose();
        poseStack.translate(this.currentX, this.currentY, this.currentZ);
        poseStack.mulPose(this.renderRotation);
        this.internalModel.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}