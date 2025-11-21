package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.Advancements;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.client.NeoForgeClient;
import johnsmith.enchantingoverhauled.client.gui.config.ConfigScreen;
import johnsmith.enchantingoverhauled.config.NeoForgeConfig;
import johnsmith.enchantingoverhauled.datagen.NeoForgeLootModifiersProvider;
import johnsmith.enchantingoverhauled.enchantment.Enchantments;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.item.NeoForgeItemGroups;
import johnsmith.enchantingoverhauled.loot.NeoForgeLootModifiers;
import johnsmith.enchantingoverhauled.platform.enchantment.theme.EnchantmentThemeAssignmentLoader;
import johnsmith.enchantingoverhauled.structure.processor.Processors;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

/**
 * The main entry point for the NeoForge version of Enchanting Overhauled.
 * <p>
 * Annotated with {@code @Mod}, this class is constructed by the NeoForge loader.
 * It registers the mod's configuration, event listeners, and content registries
 * to the mod event bus.
 */
@Mod(Constants.MOD_ID)
public class NeoForge {

    /**
     * Constructs the main mod class and registers event listeners.
     *
     * @param eventBus The mod-specific event bus for registration events.
     * @param container The mod container instance.
     */
    public NeoForge(IEventBus eventBus, ModContainer container) {
        Common.initialize();

        // Register Config
        container.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.COMMON_SPEC);
        eventBus.addListener(this::onConfigLoad);

        // Register Content
        Blocks.initialize(eventBus);
        Items.initialize(eventBus);
        Enchantments.initialize(eventBus);
        NeoForgeItemGroups.initialize(eventBus);
        Processors.initialize(eventBus);
        Advancements.initialize(eventBus);
        NeoForgeLootModifiers.initialize(eventBus);

        // Register Data Pack Registries and Listeners
        eventBus.addListener(this::onDataPackRegistry);
        eventBus.addListener(this::gatherData);
        EVENT_BUS.addListener(this::onAddReloadListeners);

        // Client-side specific initialization
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClient.initialize(eventBus);

            // Register the Config Screen Factory for the "Mods" menu
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (client, parent) -> new ConfigScreen(parent));
        }
    }

    /**
     * Event listener for config loading.
     * <p>
     * Synchronizes the loaded TOML config values into the common {@link johnsmith.enchantingoverhauled.config.Config} class.
     *
     * @param event The config load event.
     */
    public void onConfigLoad(final ModConfigEvent.Loading event) {
        NeoForgeConfig.loadConfig();
    }

    /**
     * Event listener for registering custom data pack registries.
     * <p>
     * Registers the {@link EnchantmentTheme} registry so it can be synced to clients.
     *
     * @param event The new registry event.
     */
    public void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        // Registers the codec for Server (Data pack loading) and Client (Syncing)
        event.dataPackRegistry(EnchantmentThemeRegistry.THEME_REGISTRY_KEY, EnchantmentTheme.CODEC, EnchantmentTheme.CODEC);
    }

    /**
     * Event listener for adding resource reload listeners.
     * <p>
     * Registers listeners that parse JSON data files (Theme Assignments, Loot Injections) when data packs are reloaded.
     *
     * @param event The add reload listener event.
     */
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new EnchantmentThemeAssignmentLoader());
    }

    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                event.includeServer(),
                (net.minecraft.data.DataProvider.Factory<NeoForgeLootModifiersProvider>) output ->
                        new NeoForgeLootModifiersProvider(output, event.getLookupProvider())
        );
    }
}