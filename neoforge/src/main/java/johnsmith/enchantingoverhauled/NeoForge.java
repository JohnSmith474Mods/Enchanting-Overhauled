package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.NeoForgeAdvancements;
import johnsmith.enchantingoverhauled.block.NeoForgeBlocks;
import johnsmith.enchantingoverhauled.client.NeoForgeClient;
import johnsmith.enchantingoverhauled.data.NeoForgeDataPacks;
import johnsmith.enchantingoverhauled.entity.NeoForgeAttributes;
import johnsmith.enchantingoverhauled.entity.NeoForgeEntities;
import johnsmith.enchantingoverhauled.item.NeoForgeItemGroups;
import johnsmith.enchantingoverhauled.item.NeoForgeItems;
import johnsmith.enchantingoverhauled.item.enchantment.NeoForgeEnchantmentEffectComponents;
import johnsmith.enchantingoverhauled.item.enchantment.NeoForgeLevelBasedValues;
import johnsmith.enchantingoverhauled.item.enchantment.spellfield.NeoForgeSpellFieldEffects;
import johnsmith.enchantingoverhauled.loot.NeoForgeLootInjector;
import johnsmith.enchantingoverhauled.loot.condition.NeoForgeLootItemConditionTypes;
import johnsmith.enchantingoverhauled.loot.function.NeoForgeLootItemFunctionTypes;
import johnsmith.enchantingoverhauled.menu.NeoForgeMenuTypes;
import johnsmith.enchantingoverhauled.particle.NeoForgeSimpleParticleTypes;
import johnsmith.enchantingoverhauled.platform.network.NeoForgeNetworkRegistry;
import johnsmith.enchantingoverhauled.registry.NeoForgeRegistries;
import johnsmith.enchantingoverhauled.resource.NeoForgeResourcePacks;
import johnsmith.enchantingoverhauled.server.NeoForgeServer;
import johnsmith.enchantingoverhauled.structure.NeoForgeStructureTypes;
import johnsmith.enchantingoverhauled.structure.processor.NeoForgeStructureProcessors;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

@Mod(Constants.MOD_ID)
public class NeoForge {
    public NeoForge(IEventBus eventBus, ModContainer container) {
        // Config init
        Config.MANAGER.init(FMLPaths.CONFIGDIR.get());

        // Common init
        Common.initialize();

        // Platform init
        NeoForgeBlocks.initialize(eventBus);
        NeoForgeEntities.initialize(eventBus);
        NeoForgeItems.initialize(eventBus);
        NeoForgeItemGroups.initialize(eventBus);
        NeoForgeMenuTypes.initialize(eventBus);
        NeoForgeStructureTypes.initialize(eventBus);
        NeoForgeStructureProcessors.initialize(eventBus);
        NeoForgeAdvancements.initialize(eventBus);
        NeoForgeLootInjector.initialize();
        NeoForgeServer.initailize();
        eventBus.addListener(NeoForgeAttributes::initialize);
        eventBus.addListener(NeoForgeNetworkRegistry::initialize);
        eventBus.addListener(NeoForgeLootItemConditionTypes::initialize);
        eventBus.addListener(NeoForgeLootItemFunctionTypes::initialize);
        eventBus.addListener(NeoForgeLevelBasedValues::initialize);
        eventBus.addListener(NeoForgeEnchantmentEffectComponents::initialize);
        eventBus.addListener(NeoForgeSpellFieldEffects::initialize);
        eventBus.addListener(NeoForgeSimpleParticleTypes::initialize);
        eventBus.addListener(NeoForgeRegistries::initialize);

        // Register data packs
        eventBus.addListener(NeoForgeDataPacks::registerBuiltInDataPacks);

        // Register resource packs
        eventBus.addListener(NeoForgeResourcePacks::registerBuiltInResourcePacks);

        // Client init
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClient.initialize(eventBus, container);
        }
    }
}