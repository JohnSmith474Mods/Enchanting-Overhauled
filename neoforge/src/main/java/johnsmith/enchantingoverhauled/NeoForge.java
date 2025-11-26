package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.Advancements;
import johnsmith.enchantingoverhauled.advancement.CriteriaRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.value.EnchantmentValueRegistry;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.client.NeoForgeClient;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.datagen.NeoForgeLootModifiersProvider;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.item.NeoForgeItemGroups;
import johnsmith.enchantingoverhauled.loot.NeoForgeLootModifiers;
import johnsmith.enchantingoverhauled.structure.processor.Processors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.LevelResource;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

@Mod(Constants.MOD_ID)
public class NeoForge {

    public NeoForge(IEventBus eventBus, ModContainer container) {
        // 1. Global Config
        Config.MANAGER.initialize(FMLPaths.CONFIGDIR.get());

        // 2. Common & Network Init
        Common.initialize();

        // 4. Registries
        Blocks.initialize(eventBus);
        Items.initialize(eventBus);
        NeoForgeItemGroups.initialize(eventBus);
        Processors.initialize(eventBus);
        Advancements.initialize(eventBus);
        NeoForgeLootModifiers.initialize(eventBus);

        // 5. Mod Bus Events
        eventBus.addListener(this::onRegister);
        eventBus.addListener(this::onDataPackRegistry);
        eventBus.addListener(this::gatherData);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClient.initialize(eventBus);
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (client, parent) -> Config.MANAGER.createScreen(parent));
        }
    }

    /**
     * Handles registration of vanilla registry content that doesn't use DeferredRegister.
     */
    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(BuiltInRegistries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE.key())) {
            EnchantmentValueRegistry.initialize();
        }
        if (event.getRegistryKey().equals(BuiltInRegistries.ENCHANTMENT_ENTITY_EFFECT_TYPE.key())) {
            EnchantmentEffectComponentRegistry.initialize();
        }
    }

    public void onDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(EnchantmentThemeRegistry.THEME_REGISTRY_KEY, EnchantmentTheme.CODEC, EnchantmentTheme.CODEC);
    }

    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
            event.includeServer(),
            (net.minecraft.data.DataProvider.Factory<NeoForgeLootModifiersProvider>) output ->
                new NeoForgeLootModifiersProvider(output, event.getLookupProvider())
        );
    }
}