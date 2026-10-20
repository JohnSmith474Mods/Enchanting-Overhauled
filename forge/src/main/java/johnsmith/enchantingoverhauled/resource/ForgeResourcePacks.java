package johnsmith.enchantingoverhauled.resource;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.data.ForgeDataPacks;

import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;

public final class ForgeResourcePacks {
    public static void registerBuiltInResourcePacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        for (ResourcePacks resourcePack : ResourcePacks.values()) {
            ForgeDataPacks.registerBuiltInPack(
                    event,
                    PackType.CLIENT_RESOURCES,
                    Constants.MOD_ID,
                    resourcePack.path,
                    resourcePack.title,
                    resourcePack.packInclusionType().get()
            );
        }
    }

    private ForgeResourcePacks() {}
}