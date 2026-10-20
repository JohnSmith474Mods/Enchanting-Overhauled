package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.gui.screen.ForgeMenuScreens;
import johnsmith.enchantingoverhauled.client.particle.ForgeSimpleParticleTypes;
import johnsmith.enchantingoverhauled.client.render.entity.ForgeEntityRenderers;
import johnsmith.enchantingoverhauled.client.render.entity.model.ForgeModelLayers;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

public class ForgeClient {

    public static void initialize(IEventBus eventBus, ModLoadingContext context) {
        // Common init
        Common.initialize();

        // Platform init
        eventBus.addListener(ForgeMenuScreens::initialize);
        eventBus.addListener(ForgeSimpleParticleTypes::initialize);
        eventBus.addListener(ForgeModelLayers::initialize);
        eventBus.addListener(ForgeEntityRenderers::initialize);

        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> Config.MANAGER.createScreen(parent)
                )
        );
    }
}