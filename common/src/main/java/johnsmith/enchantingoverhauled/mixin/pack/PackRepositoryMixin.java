package johnsmith.enchantingoverhauled.mixin.pack;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    @ModifyVariable(method = "setSelected", at = @At("HEAD"), argsOnly = true)
    private Collection<String> enforceDataPackOrder(Collection<String> packIds) {
        List<String> sortedPacks = new ArrayList<>(packIds);

        String libraryPackId = null;
        String altarPackId = null;

        for (String id : sortedPacks) {
            if (id.contains(Constants.MATCH_ENCHANTED_LIBRARY)) {
                libraryPackId = id;
            } else if (id.contains(Constants.MATCH_ENCHANTING_ALTAR) && id.contains(Constants.MATCH_DATA)) {
                altarPackId = id;
            }
        }

        if (libraryPackId != null && altarPackId != null) {
            int libraryIndex = sortedPacks.indexOf(libraryPackId);
            int altarIndex = sortedPacks.indexOf(altarPackId);

            if (libraryIndex > altarIndex) {
                Constants.LOG.info("PackRepositoryMixin: Reordering data packs. Moving '{}' after '{}'.", altarPackId, libraryPackId);
                sortedPacks.remove(altarPackId);
                sortedPacks.add(sortedPacks.indexOf(libraryPackId) + 1, altarPackId);
            }
        }

        return sortedPacks;
    }
}