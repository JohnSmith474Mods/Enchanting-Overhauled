package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.Advancements;
import johnsmith.enchantingoverhauled.advancement.CriteriaRegistry;
import johnsmith.enchantingoverhauled.api.config.io.*;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.value.EnchantmentValueRegistry;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.item.FabricItemGroups;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.structure.processor.Processors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

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
        // 1. Global Config Init (Simplified)
        // No need to reference ScopedConfig manually
        Config.MANAGER.initialize(FabricLoader.getInstance().getConfigDir());
        Common.initialize();

        // Common Registries
        EnchantmentValueRegistry.initialize();
        EnchantmentEffectComponentRegistry.initialize();
        // Fabric Registries
        Blocks.initialize();
        Items.initialize();
        FabricItemGroups.initialize();
        Processors.initialize();
        Advancements.initialize();

        // Register dynamic registry for syncing EnchantmentThemes
        DynamicRegistries.registerSynced(EnchantmentThemeRegistry.THEME_REGISTRY_KEY, EnchantmentTheme.CODEC);
    }
}