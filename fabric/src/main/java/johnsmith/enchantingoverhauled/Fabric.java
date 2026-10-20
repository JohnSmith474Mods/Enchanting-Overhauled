package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.FabricAdvancements;
import johnsmith.enchantingoverhauled.block.FabricBlocks;
import johnsmith.enchantingoverhauled.data.FabricDataPacks;
import johnsmith.enchantingoverhauled.entity.FabricAttributes;
import johnsmith.enchantingoverhauled.entity.FabricEntities;
import johnsmith.enchantingoverhauled.item.FabricItems;
import johnsmith.enchantingoverhauled.item.FabricItemGroups;
import johnsmith.enchantingoverhauled.item.enchantment.FabricLevelBasedValues;
import johnsmith.enchantingoverhauled.item.enchantment.FabricEnchantmentEffectComponents;
import johnsmith.enchantingoverhauled.item.enchantment.spellfield.FabricSpellFieldEffects;
import johnsmith.enchantingoverhauled.loot.FabricLootInjector;
import johnsmith.enchantingoverhauled.loot.condition.FabricLootItemConditionTypes;
import johnsmith.enchantingoverhauled.loot.function.FabricLootItemFunctionTypes;
import johnsmith.enchantingoverhauled.menu.FabricMenuTypes;
import johnsmith.enchantingoverhauled.particle.FabricSimpleParticleTypes;
import johnsmith.enchantingoverhauled.platform.network.FabricNetworkRegistry;
import johnsmith.enchantingoverhauled.registry.FabricRegistries;
import johnsmith.enchantingoverhauled.resource.FabricResourcePacks;
import johnsmith.enchantingoverhauled.server.FabricServer;
import johnsmith.enchantingoverhauled.structure.FabricStructureTypes;
import johnsmith.enchantingoverhauled.structure.processor.FabricStructureProcessors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Config init
        Config.MANAGER.init(FabricLoader.getInstance().getConfigDir());

        // Common init
        Common.initialize();

        // Platform init
        FabricBlocks.initialize();
        FabricEntities.initialize();
        FabricAttributes.initialize();
        FabricItems.initialize();
        FabricItemGroups.initialize();
        FabricMenuTypes.initialize();
        FabricStructureTypes.initialize();
        FabricStructureProcessors.initialize();
        FabricAdvancements.initialize();
        FabricNetworkRegistry.initialize();
        FabricLootInjector.initialize();
        FabricLootItemConditionTypes.initialize();
        FabricLootItemFunctionTypes.initialize();
        FabricLevelBasedValues.initialize();
        FabricEnchantmentEffectComponents.initialize();
        FabricSpellFieldEffects.initialize();
        FabricSimpleParticleTypes.initialize();
        FabricRegistries.initialize();
        FabricServer.initialize();

        // Register data packs
        FabricDataPacks.registerBuiltInDataPacks();

        // Register resource packs
        FabricResourcePacks.registerBuiltInResourcePacks();
    }
}