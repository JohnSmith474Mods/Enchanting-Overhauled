package johnsmith.enchantingoverhauled.client.render.entity.model;

import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class FlyingBookEntityModel<S extends FlyingBookEntityModel.RenderState> extends EntityModel<S> {

    public static class RenderState extends LivingEntityRenderState {
        @Nullable
        public AbstractFlyingBookEntity entity;
        public float partialTick = 0.0F;
        public BookAnimationState animationState = BookAnimationState.IDLE;
        public float animX = 0.0F;
        public float animY = 1.2F;
        public float animZ = 0.0F;
        public float openness = 1.0F;
        public float flutterPhase = 0.0F;
        public float rightPageFlip = 0.0F;
        public float leftPageFlip = 0.0F;
        public Quaternionf animRotation = new Quaternionf();
    }

    private final BookModel internalModel;

    public FlyingBookEntityModel(BookModel internalModel) {
        super(internalModel.root(), RenderType::entityCutoutNoCull);
        this.internalModel = internalModel;
    }

    public BookModel getInternalModel() {
        return this.internalModel;
    }

    public static void extractBookRenderState(AbstractFlyingBookEntity entity, RenderState state, float partialTick) {
        state.entity = entity;
        state.partialTick = partialTick;
        BookAnimationState animState = entity.getAnimationState();
        state.animationState = animState;

        state.animX = Mth.lerp(partialTick, entity.animXO, entity.animX);
        float baseAnimY = Mth.lerp(partialTick, entity.animYO, entity.animY);
        float flutter = entity.flutterPhase + animState.flutterSpeed * partialTick;
        state.flutterPhase = flutter;
        state.animY = baseAnimY + (animState.flutterSpeed > 0.0F ? Mth.sin(flutter) * 0.05F : 0.0F);
        state.animZ = Mth.lerp(partialTick, entity.animZO, entity.animZ);

        state.openness = Mth.lerp(partialTick, entity.animOpennessO, entity.animOpenness);
        float flip = Mth.lerp(partialTick, entity.oPageFlip, entity.pageFlip);
        state.rightPageFlip = Mth.clamp(Mth.frac(flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        state.leftPageFlip = Mth.clamp(Mth.frac(flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);

        state.animRotation = new Quaternionf(entity.animRotationO)
                .slerp(entity.animRotation, partialTick)
                .normalize();
    }

    @Override
    public void setupAnim(@NotNull S state) {
        super.setupAnim(state);

        if (this.internalModel instanceof EnchantedBookModel enchantedBookModel) {
            enchantedBookModel.renderedOnLectern = false;
        }

        float bookTime = state.flutterPhase * 50.0F;
        this.internalModel.setupAnim(bookTime, state.rightPageFlip, state.leftPageFlip, state.openness);

        ModelPart root = this.root();
        root.x = state.animX * 16.0F;
        root.y = state.animY * 16.0F;
        root.z = state.animZ * 16.0F;

        // ModelPart#translateAndRotate reconstructs rotations via new Quaternionf().rotationZYX(zRot, yRot, xRot).
        // JOML's getEulerAnglesZYX is the inverse of rotationXYZ (not rotationZYX) and lacks a gimbal-lock guard
        // at yRot = +/-90 deg, which caused entityPitch in chanting/targeting/bartering/dashing states to tilt the book sideways.
        Quaternionf q = state.animRotation;
        float x = q.x();
        float y = q.y();
        float z = q.z();
        float w = q.w();

        float sinY = 2.0F * (y * w - x * z);
        if (sinY >= 0.99999F) {
            root.yRot = (float) (Math.PI * 0.5D);
            root.zRot = 0.0F;
            root.xRot = (float) Mth.atan2(2.0F * (x * y - z * w), 1.0F - 2.0F * (x * x + z * z));
        } else if (sinY <= -0.99999F) {
            root.yRot = (float) (-Math.PI * 0.5D);
            root.zRot = 0.0F;
            root.xRot = (float) Mth.atan2(-2.0F * (x * y - z * w), 1.0F - 2.0F * (x * x + z * z));
        } else {
            root.yRot = (float) Math.asin(sinY);
            root.xRot = (float) Mth.atan2(2.0F * (y * z + x * w), 1.0F - 2.0F * (x * x + y * y));
            root.zRot = (float) Mth.atan2(2.0F * (x * y + z * w), 1.0F - 2.0F * (y * y + z * z));
        }
    }
}