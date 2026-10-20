package johnsmith.enchantingoverhauled.mixin.client;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantmentcore.api.config.PackInclusionType;

import net.minecraft.client.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Options.class)
public abstract class OptionsMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("EnchantingOverhauled/OptionsMixin");

    @Shadow
    public List<String> resourcePacks;

    @Shadow
    public abstract void save();

    @Inject(method = "load", at = @At("RETURN"))
    private void applyDefaultResourcePacks(CallbackInfo ci) {
        LOGGER.info("OptionsMixin: Intercepted Options.load()");

        PackInclusionType type = Config.ENCHANTING_ALTAR_DATA_INCLUSION_TYPE.get();
        LOGGER.info("OptionsMixin: ENCHANTING_ALTAR_DATA_INCLUSION_TYPE evaluated as: {}", type);

        if (type == PackInclusionType.OPTIONAL) {
            LOGGER.info("OptionsMixin: Inclusion type is OPTIONAL. Halting injection sequence.");
            return;
        }

        // Standardized cross-platform Pack ID
        String packId = Constants.MOD_ID + ":" + Constants.ENCHANTING_ALTAR_ASSETS_DIRECTORY;
        LOGGER.info("OptionsMixin: Target pack ID: {}", packId);
        LOGGER.info("OptionsMixin: Active resource packs prior to injection: {}", this.resourcePacks);

        if (!this.resourcePacks.contains(packId)) {
            LOGGER.info("OptionsMixin: Pack ID not present in active repository. Executing injection.");
            this.resourcePacks.add(packId);
            LOGGER.info("OptionsMixin: Injection successful. Updated active resource packs: {}", this.resourcePacks);
        } else {
            LOGGER.info("OptionsMixin: Pack ID already exists in active repository. Bypassing injection.");
        }
    }
}