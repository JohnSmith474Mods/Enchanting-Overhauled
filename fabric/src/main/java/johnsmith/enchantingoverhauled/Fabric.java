package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.Advancements;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.platform.enchantment.theme.EnchantmentThemeAssignmentLoader;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.config.FabricConfig;
import johnsmith.enchantingoverhauled.enchantment.Enchantments;
import johnsmith.enchantingoverhauled.item.FabricItemGroups;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.structure.processor.Processors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

import net.minecraft.server.packs.PackType;

/**
 * The main entry point for the Fabric version of Enchanting Overhauled.
 * <p>
 * This class implements {@link ModInitializer} and is responsible for bootstrapping
 * the mod on the Fabric loader. It handles the registration of all content (blocks, items,
 * enchantments, etc.), configuration loading, and event listeners.
 */
public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Common.initialize();
        FabricConfig.initialize();
        Blocks.initialize();
        Items.initialize();
        Enchantments.initialize();
        FabricItemGroups.initialize();
        Processors.initialize();
        Advancements.initialize();

        // Register dynamic registry for syncing EnchantmentThemes
        DynamicRegistries.registerSynced(EnchantmentThemeRegistry.THEME_REGISTRY_KEY, EnchantmentTheme.CODEC);

        // Register data pack reload listeners
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new EnchantmentThemeAssignmentLoader());
    }
}