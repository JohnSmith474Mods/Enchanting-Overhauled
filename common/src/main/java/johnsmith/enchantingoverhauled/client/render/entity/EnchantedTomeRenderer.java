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

public class EnchantedTomeRenderer extends MobRenderer<EnchantedTomeEntity, FlyingBookEntityModel<EnchantedTomeEntity>> {

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
    public void render(
            @NotNull EnchantedTomeEntity entity,
            float entityYaw,
            float partialTicks,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight
    ) {
        if (RoostedBookRenderer.tryRenderRoosted(entity, this.bookModel, this.getTextureLocation(entity), partialTicks, poseStack, buffer, packedLight)) {
            this.shadowRadius = 0.0F;
            if (this.shouldShowName(entity)) {
                this.renderNameTag(entity, entity.getDisplayName(), poseStack, buffer, packedLight, partialTicks);
            }
        } else {
            this.shadowRadius = 0.3F;
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
        EnchantedBookRenderer.renderOrbitingBarterItems(entity, this.itemRenderer, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected boolean shouldShowName(@NotNull EnchantedTomeEntity entity) {
        return super.shouldShowName(entity) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EnchantedTomeEntity entity) {
        return TEXTURE;
    }
}