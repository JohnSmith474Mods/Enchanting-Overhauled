package johnsmith;

import johnsmith.advancement.CriteriaRegistry;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeAssignmentLoader;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.block.BlockRegistry;
import johnsmith.config.Config;
import johnsmith.config.SimpleConfig;
import johnsmith.enchantment.EnchantmentRegistry;
import johnsmith.entity.damage.DamageTypeRegistry;
import johnsmith.entity.damage.DamageTypeTagRegistry;
import johnsmith.item.EnchantingOverhauledItemGroups;
import johnsmith.item.ItemRegistry;
import johnsmith.loot.LootTableInjector;
import johnsmith.structure.processor.ProcessorLists;
import johnsmith.structure.processor.ProcessorRegistry;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;

import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EnchantingOverhauled implements ModInitializer {
	public static final String MOD_ID = "enchanting_overhauled";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Enchanting Overhauled!");

        // Register our Enchantment Theme registry
        DynamicRegistries.registerSynced(
            EnchantmentThemeRegistry.THEME_REGISTRY_KEY,
            EnchantmentTheme.CODEC
        );

        Config.initialize();

        // Register our config to reload on data pack reload
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            private static final Identifier ID = new Identifier(EnchantingOverhauled.MOD_ID, "config_reloader");

            @Override
            public Identifier getFabricId() {
                return ID;
            }

            @Override
            public CompletableFuture<Void> reload(
                    Synchronizer synchronizer,
                    ResourceManager manager,
                    Profiler prepareProfiler,
                    Profiler applyProfiler,
                    Executor prepareExecutor,
                    Executor applyExecutor
            ) { // 1. Load data on the worker thread (prepareExecutor)
                CompletableFuture<SimpleConfig> loadFuture = CompletableFuture.supplyAsync(() -> {
                    LOGGER.info("Reloading config from disk...");
                    // This re-reads the file, using the existing 'configs' provider for defaults
                    return SimpleConfig.of(EnchantingOverhauled.MOD_ID).provider(Config.configs).request();
                }, prepareExecutor);

                // 2. Pass the loaded data to the synchronizer.
                // 3. Apply the data on the main thread (applyExecutor) after the synchronizer gives the signal.
                return loadFuture.thenCompose(synchronizer::whenPrepared)
                        .thenAcceptAsync(newConfig -> { // 'newConfig' is the SimpleConfig from loadFuture
                            // This code runs on the main thread ('applyExecutor')
                            Config.CONFIG = newConfig;
                            Config.assignConfigs();
                            LOGGER.info("Config reload complete.");
                        }, applyExecutor);
            }
        });

        CriteriaRegistry.initialize();

        EnchantmentThemeRegistry.initialize();

        EnchantmentRegistry.initialize();

		ItemRegistry.initialize();

        DamageTypeRegistry.initialize();

        DamageTypeTagRegistry.initialize();

        BlockRegistry.initialize();

        LootTableInjector.initialize();

        EnchantingOverhauledItemGroups.initialize();

        // Register the theme loader to listen to data pack reload events
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new EnchantmentThemeAssignmentLoader());

		ProcessorRegistry.initialize();

		ProcessorLists.initialize();

        LOGGER.info("Enchanting Overhauled initialized!");
	}
}