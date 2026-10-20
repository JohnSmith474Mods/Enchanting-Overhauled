package johnsmith.enchantingoverhauled.data;

import johnsmith.enchantingoverhauled.Constants;

import johnsmith.enchantmentcore.api.config.PackInclusionType;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FabricDataPacks {
    public static void registerBuiltInDataPacks() {
        for (DataPacks datapack : DataPacks.values()) {
            registerBuiltInPack(
                    Constants.MOD_ID,
                    datapack.path,
                    datapack.title,
                    datapack.packInclusionType().get()
            );
        }
    }

    public static void registerBuiltInPack(String namespace, String directory, String name, PackInclusionType inclusionType) {
        ResourcePackActivationType activationType = switch (inclusionType) {
            case REQUIRED -> ResourcePackActivationType.ALWAYS_ENABLED;
            case ACTIVE -> ResourcePackActivationType.DEFAULT_ENABLED;
            case OPTIONAL -> ResourcePackActivationType.NORMAL;
        };

        FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(container -> {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    ResourceLocation.fromNamespaceAndPath(namespace, directory),
                    container,
                    Component.literal(name),
                    activationType
            );
        });
    }

    private FabricDataPacks() {}
}
