package johnsmith.enchantingoverhauled.resource;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.data.FabricDataPacks;

public final class FabricResourcePacks {
    public static void registerBuiltInResourcePacks() {
        for (ResourcePacks resourcePack : ResourcePacks.values()) {
            FabricDataPacks.registerBuiltInPack(
                    Constants.MOD_ID,
                    resourcePack.path,
                    resourcePack.title,
                    resourcePack.packInclusionType().get()
            );
        }
    }

    private FabricResourcePacks() {}
}
