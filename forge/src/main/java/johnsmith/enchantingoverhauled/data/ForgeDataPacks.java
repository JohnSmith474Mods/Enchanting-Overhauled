package johnsmith.enchantingoverhauled.data;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantmentcore.api.config.PackInclusionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class ForgeDataPacks {

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
        Path path = ModList.get().getModFileById(Constants.MOD_ID).getFile().findResource("resourcepacks", directory);
        if (Files.exists(path)) {
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

            Pack.ResourcesSupplier resourcesSupplier = new PathPackResources.PathResourcesSupplier(path);
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

    private ForgeDataPacks() {}
}