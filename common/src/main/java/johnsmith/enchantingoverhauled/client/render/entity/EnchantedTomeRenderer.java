package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.entity.layer.EntityGlintLayer;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.client.render.entity.model.FlyingBookEntityModel;
import johnsmith.enchantingoverhauled.entity.EnchantedTomeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EnchantedTomeRenderer extends MobRenderer<EnchantedTomeEntity, FlyingBookEntityModel.RenderState, FlyingBookEntityModel<FlyingBookEntityModel.RenderState>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/enchanted_tome.png");

    private final ItemRenderer itemRenderer;
    private final EnchantedBookModel bookModel;

    public EnchantedTomeRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FlyingBookEntityModel<>(new EnchantedBookModel(context.bakeLayer(ClientConstants.ENCHANTED_TOME_LAYER))),
                0.3F
        );
        this.itemRenderer = context.getItemRenderer();
        this.bookModel = (EnchantedBookModel) this.model.getInternalModel();
        this.addLayer(new EntityGlintLayer<>(this));
    }

    @Override
    public FlyingBookEntityModel.@NotNull RenderState createRenderState() {
        return new FlyingBookEntityModel.RenderState();
    }

    @Override
    public void extractRenderState(@NotNull EnchantedTomeEntity entity, FlyingBookEntityModel.@NotNull RenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        FlyingBookEntityModel.extractBookRenderState(entity, state, partialTick);
    }

    @Override
    public void render(
            FlyingBookEntityModel.@NotNull RenderState state,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        if (state.entity != null && RoostedBookRenderer.tryRenderRoosted(state.entity, this.bookModel, this.getTextureLocation(state), state.partialTick, poseStack, buffer, packedLight)) {
            this.shadowRadius = 0.0F;
            if (state.nameTag != null) {
                this.renderNameTag(state, state.nameTag, poseStack, buffer, packedLight);
            }
        } else {
            this.shadowRadius = 0.3F;
            super.render(state, poseStack, buffer, packedLight);
        }
        if (state.entity != null) {
            EnchantedBookRenderer.renderOrbitingBarterItems(state.entity, this.itemRenderer, state.partialTick, poseStack, buffer, packedLight);
        }
    }

    @Override
    protected boolean shouldShowName(@NotNull EnchantedTomeEntity entity, double distanceToCameraSq) {
        return super.shouldShowName(entity, distanceToCameraSq) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(FlyingBookEntityModel.@NotNull RenderState state) {
        return TEXTURE;
    }
}