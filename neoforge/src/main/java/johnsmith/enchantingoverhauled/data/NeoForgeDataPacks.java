package johnsmith.enchantingoverhauled.data;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantmentcore.api.config.PackInclusionType;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.JarContentsPackResources;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class NeoForgeDataPacks {
    public static void registerBuiltInDataPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        for (DataPacks datapack : DataPacks.values()) {
            registerBuiltInPack(
                    event,
                    PackType.SERVER_DATA,
                    Constants.MOD_ID,
                    datapack.path,
                    datapack.title,
                    datapack.packInclusionType().get()
            );
        }
    }

    public static void registerBuiltInPack(
            AddPackFindersEvent event,
            PackType packType,
            String namespace,
            String directory,
            String name,
            PackInclusionType inclusionType
    ) {
        JarContents contents = ModList.get().getModFileById(Constants.MOD_ID).getFile().getContents();
        String prefix = "resourcepacks/" + directory;
        if (contents.containsFile(prefix + "/pack.mcmeta")) {
            boolean isRequired = inclusionType == PackInclusionType.REQUIRED;
            boolean isDefaultActive = inclusionType != PackInclusionType.OPTIONAL;

            PackSelectionConfig selectionConfig = new PackSelectionConfig(isRequired, Pack.Position.TOP, false);

            PackSource packSource = new PackSource() {
                @Override
                public @NotNull Component decorate(@NotNull Component component) {
                    return PackSource.BUILT_IN.decorate(component);
                }

                @Override
                public boolean shouldAddAutomatically() {
                    return isDefaultActive;
                }
            };

            Pack.ResourcesSupplier resourcesSupplier = BuiltInPackSource.fromName(
                    locationInfo -> new JarContentsPackResources(locationInfo, contents, prefix)
            );
            PackLocationInfo info = new PackLocationInfo(
                    namespace + ":" + directory,
                    Component.literal(name),
                    packSource,
                    Optional.empty()
            );
            Pack pack = Pack.readMetaAndCreate(info, resourcesSupplier, packType, selectionConfig);

            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        }
    }

    private NeoForgeDataPacks() {}
}