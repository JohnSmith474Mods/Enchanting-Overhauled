package johnsmith.enchantingoverhauled.mixin.client.render.block.entity;

import johnsmith.enchantingoverhauled.client.render.entity.model.TomeModel;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantTableRenderer.class)
public class EnchantTableRendererMixin {

    @Shadow @Final @Mutable
    private BookModel bookModel; // Yarn: book, MojMap: bookModel

    /**
     * Injects into the renderer's constructor to replace the vanilla book model
     * with our custom one.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(BlockEntityRendererProvider.Context context, CallbackInfo ci) {

        // 1. Get the root ModelPart from our custom layer
        //    (Yarn: getLayerModelPart -> MojMap: bakeLayer)
        ModelPart tomeRoot = context.bakeLayer(TomeModel.LAYER_LOCATION);

        // 2. Replace the renderer's book model with our new one
        this.bookModel = new TomeModel(tomeRoot);
    }
}