package johnsmith.enchantingoverhauled;

import johnsmith.enchantingoverhauled.advancement.ForgeAdvancements;
import johnsmith.enchantingoverhauled.block.ForgeBlocks;
import johnsmith.enchantingoverhauled.client.ForgeClient;
import johnsmith.enchantingoverhauled.data.ForgeDataPacks;
import johnsmith.enchantingoverhauled.entity.ForgeAttributes;
import johnsmith.enchantingoverhauled.entity.ForgeEntities;
import johnsmith.enchantingoverhauled.item.ForgeItemGroups;
import johnsmith.enchantingoverhauled.item.ForgeItems;
import johnsmith.enchantingoverhauled.item.enchantment.ForgeEnchantmentEffectComponents;
import johnsmith.enchantingoverhauled.item.enchantment.ForgeLevelBasedValues;
import johnsmith.enchantingoverhauled.item.enchantment.spellfield.ForgeSpellFieldEffects;
import johnsmith.enchantingoverhauled.loot.ForgeLootInjector;
import johnsmith.enchantingoverhauled.loot.condition.ForgeLootItemConditionTypes;
import johnsmith.enchantingoverhauled.loot.function.ForgeLootItemFunctionTypes;
import johnsmith.enchantingoverhauled.menu.ForgeMenuTypes;
import johnsmith.enchantingoverhauled.particle.ForgeSimpleParticleTypes;
import johnsmith.enchantingoverhauled.platform.network.ForgeNetworkRegistry;
import johnsmith.enchantingoverhauled.registry.ForgeRegistries;
import johnsmith.enchantingoverhauled.resource.ForgeResourcePacks;
import johnsmith.enchantingoverhauled.server.ForgeServer;
import johnsmith.enchantingoverhauled.structure.ForgeStructureTypes;
import johnsmith.enchantingoverhauled.structure.processor.ForgeStructureProcessors;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(Constants.MOD_ID)
public class Forge {
    public Forge() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Config init
        Config.MANAGER.init(FMLPaths.CONFIGDIR.get());

        // Common init
        Common.initialize();

        // Platform init
        ForgeBlocks.initialize(eventBus);
        ForgeEntities.initialize(eventBus);
        ForgeItems.initialize(eventBus);
        ForgeItemGroups.initialize(eventBus);
        ForgeMenuTypes.initialize(eventBus);
        ForgeStructureTypes.initialize(eventBus);
        ForgeStructureProcessors.initialize(eventBus);
        ForgeAdvancements.initialize(eventBus);
        ForgeLootInjector.initialize();
        ForgeNetworkRegistry.initialize();
        ForgeServer.initialize();
        eventBus.addListener(ForgeAttributes::initialize);
        eventBus.addListener(ForgeLootItemConditionTypes::initialize);
        eventBus.addListener(ForgeLootItemFunctionTypes::initialize);
        eventBus.addListener(ForgeLevelBasedValues::initialize);
        eventBus.addListener(ForgeEnchantmentEffectComponents::initialize);
        eventBus.addListener(ForgeSpellFieldEffects::initialize);
        eventBus.addListener(ForgeSimpleParticleTypes::initialize);
        eventBus.addListener(ForgeRegistries::initialize);

        // Register data packs
        eventBus.addListener(ForgeDataPacks::registerBuiltInDataPacks);

        // Register resource packs
        eventBus.addListener(ForgeResourcePacks::registerBuiltInResourcePacks);

        // Client init
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeClient.initialize(eventBus, ModLoadingContext.get());
        }
    }
}