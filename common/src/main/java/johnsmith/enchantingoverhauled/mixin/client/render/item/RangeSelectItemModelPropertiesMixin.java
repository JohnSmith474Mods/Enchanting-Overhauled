package johnsmith.enchantingoverhauled.mixin.client.render.item;

import com.mojang.serialization.MapCodec;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.model.property.BowPullProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangeSelectItemModelProperties.class)
public abstract class RangeSelectItemModelPropertiesMixin {

    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void registerCustomProperties(CallbackInfo ci) {
        Constants.LOG.info("AMOGUS");
        ID_MAPPER.put(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "bow_pull"),
                BowPullProperty.CODEC
        );
    }
}