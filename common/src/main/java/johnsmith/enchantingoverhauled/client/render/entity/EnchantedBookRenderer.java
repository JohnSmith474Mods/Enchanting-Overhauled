package johnsmith.enchantingoverhauled.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.ClientConstants;
import johnsmith.enchantingoverhauled.client.render.entity.layer.EntityGlintLayer;
import johnsmith.enchantingoverhauled.client.render.entity.model.EnchantedBookModel;
import johnsmith.enchantingoverhauled.client.render.entity.model.FlyingBookEntityModel;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.EnchantedBookEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EnchantedBookRenderer extends MobRenderer<EnchantedBookEntity, FlyingBookEntityModel<EnchantedBookEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/enchanted_book.png");

    private final ItemRenderer itemRenderer;
    private final EnchantedBookModel bookModel;

    public EnchantedBookRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new FlyingBookEntityModel<>(new EnchantedBookModel(context.bakeLayer(ClientConstants.ENCHANTED_BOOK_LAYER))),
                0.3F
        );
        this.itemRenderer = context.getItemRenderer();
        this.bookModel = (EnchantedBookModel) this.model.getInternalModel();
        this.addLayer(new EntityGlintLayer<>(this));
    }

    @Override
    public void render(
            @NotNull EnchantedBookEntity entity,
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
        renderOrbitingBarterItems(entity, this.itemRenderer, partialTicks, poseStack, buffer, packedLight);
    }

    public static void renderOrbitingBarterItems(
            AbstractFlyingBookEntity entity,
            ItemRenderer itemRenderer,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        ItemStack fuel = entity.getBarterFuel();
        ItemStack catalyst = entity.getBarterCatalyst();
        if (fuel.isEmpty() && catalyst.isEmpty()) {
            return;
        }

        float ageInTicks = entity.tickCount + partialTicks;
        boolean hasBoth = !fuel.isEmpty() && !catalyst.isEmpty();
        float angularSpeed = hasBoth ? 0.16F : 0.09F;
        float baseAngle = ageInTicks * angularSpeed;
        float radius = hasBoth ? 0.52F : 0.6F;
        float centerY = entity.getBbHeight() * 0.55F;

        if (!fuel.isEmpty()) {
            renderSingleOrbitingItem(entity, itemRenderer, fuel, baseAngle, radius, centerY, ageInTicks, poseStack, buffer, packedLight);
        }
        if (!catalyst.isEmpty()) {
            float offsetAngle = baseAngle + (float) Math.PI;
            renderSingleOrbitingItem(entity, itemRenderer, catalyst, offsetAngle, radius, centerY, ageInTicks + 15.0F, poseStack, buffer, packedLight);
        }
    }

    private static void renderSingleOrbitingItem(
            AbstractFlyingBookEntity entity,
            ItemRenderer itemRenderer,
            ItemStack stack,
            float angle,
            float radius,
            float centerY,
            float bobTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        float ox = Mth.cos(angle) * radius;
        float oz = Mth.sin(angle) * radius;
        float oy = centerY + Mth.sin(bobTicks * 0.15F) * 0.08F;

        poseStack.translate(ox, oy, oz);
        poseStack.mulPose(Axis.YP.rotation(-angle + (float) (Math.PI * 0.5D)));
        poseStack.scale(0.45F, 0.45F, 0.45F);

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
    }

    @Override
    protected boolean shouldShowName(@NotNull EnchantedBookEntity entity) {
        return super.shouldShowName(entity) && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EnchantedBookEntity entity) {
        return TEXTURE;
    }
}