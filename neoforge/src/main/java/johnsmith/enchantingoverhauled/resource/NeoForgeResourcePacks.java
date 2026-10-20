package johnsmith.enchantingoverhauled.resource;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.data.NeoForgeDataPacks;

import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public final class NeoForgeResourcePacks {
    public static void registerBuiltInResourcePacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        for (ResourcePacks resourcePack : ResourcePacks.values()) {
            NeoForgeDataPacks.registerBuiltInPack(
                    event,
                    PackType.CLIENT_RESOURCES,
                    Constants.MOD_ID,
                    resourcePack.path,
                    resourcePack.title,
                    resourcePack.packInclusionType().get()
            );
        }
    }

    private NeoForgeResourcePacks() {}
}