package johnsmith.mixin.client.render.block.entity;

import johnsmith.EnchantingOverhauledClient;
import johnsmith.client.render.entity.model.TomeModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntityRenderer.class)
public class EnchantingTableBlockEntityRendererMixin {

    @Shadow @Final @Mutable
    private BookModel book;

    /**
     * Injects into the renderer's constructor to replace the vanilla book model
     * with our custom one.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(BlockEntityRendererFactory.Context context, CallbackInfo ci) {

        // 1. Get the root ModelPart from our custom layer
        //    (Corrected method name: getLayerModelPart)
        ModelPart tomeRoot = context.getLayerModelPart(EnchantingOverhauledClient.CUSTOM_BOOK_LAYER);

        // 2. Replace the renderer's book model with our new one
        this.book = new TomeModel(tomeRoot);
    }
}