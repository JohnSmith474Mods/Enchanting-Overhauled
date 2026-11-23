package johnsmith.enchantingoverhauled.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;

public class EntityGlintLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    public EntityGlintLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            int packedLight,
            @NotNull S renderState,
            float yRot,
            float xRot
    ) {
        submitNodeCollector.order(1).submitModel(
                this.getParentModel(),
                renderState,
                poseStack,
                RenderTypes.patternedShieldGlint(),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF,
                null,
                renderState.outlineColor
        );
    }
}