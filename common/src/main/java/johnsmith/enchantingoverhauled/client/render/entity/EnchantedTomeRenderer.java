package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.entity.layer.EntityGlintLayer;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.client.render.entity.model.FlyingBookEntityModel;
import johnsmith.enchantingoverhauled.entity.EnchantedTomeEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EnchantedTomeRenderer extends MobRenderer<EnchantedTomeEntity, FlyingBookEntityModel.RenderState, FlyingBookEntityModel<FlyingBookEntityModel.RenderState>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/enchanted_tome.png");

    private final ItemModelResolver itemModelResolver;
    private final EnchantedBookModel bookModel;
    private final EnchantedBookModel lecternBookModel;

    public EnchantedTomeRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FlyingBookEntityModel<>(new EnchantedBookModel(context.bakeLayer(ClientConstants.ENCHANTED_TOME_LAYER))),
                0.3F
        );
        this.itemModelResolver = context.getItemModelResolver();
        this.bookModel = (EnchantedBookModel) this.model.getInternalModel();
        this.lecternBookModel = new EnchantedBookModel(context.bakeLayer(ClientConstants.ENCHANTED_TOME_LAYER));
        this.lecternBookModel.renderedOnLectern = true;
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
    public void submit(
            FlyingBookEntityModel.@NotNull RenderState state,
            @NotNull PoseStack poseStack,
            @NotNull SubmitNodeCollector submitNodeCollector,
            @NotNull CameraRenderState cameraRenderState
    ) {
        if (state.entity != null && RoostedBookRenderer.tryRenderRoosted(state.entity, this.bookModel, this.lecternBookModel, this.getTextureLocation(state), state.partialTick, poseStack, submitNodeCollector, state.outlineColor)) {
            this.shadowRadius = 0.0F;
            if (state.nameTag != null) {
                this.submitNameTag(state, poseStack, submitNodeCollector, cameraRenderState);
            }
        } else {
            this.shadowRadius = 0.3F;
            super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
        }
        if (state.entity != null) {
            EnchantedBookRenderer.renderOrbitingBarterItems(state.entity, this.itemModelResolver, state.partialTick, poseStack, submitNodeCollector, state.lightCoords, state.outlineColor);
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