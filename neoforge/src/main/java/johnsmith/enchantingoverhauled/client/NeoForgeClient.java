package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.gui.screen.NeoForgeMenuScreens;
import johnsmith.enchantingoverhauled.client.particle.NeoForgeSimpleParticleTypes;
import johnsmith.enchantingoverhauled.client.render.entity.NeoForgeEntityRenderers;
import johnsmith.enchantingoverhauled.client.render.entity.model.NeoForgeModelLayers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class NeoForgeClient {

    public static void initialize(IEventBus eventBus, ModContainer container) {
        // Common init
        Common.initialize();

        // Platform init
        eventBus.addListener(NeoForgeMenuScreens::initialize);
        eventBus.addListener(NeoForgeSimpleParticleTypes::initialize);
        eventBus.addListener(NeoForgeModelLayers::initialize);
        eventBus.addListener(NeoForgeEntityRenderers::initialize);

        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (client, parent) -> Config.MANAGER.createScreen(parent)
        );
    }
}