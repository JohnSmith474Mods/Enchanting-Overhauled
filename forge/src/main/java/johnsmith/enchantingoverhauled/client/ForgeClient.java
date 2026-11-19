package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.client.gui.screen.ForgeMenuScreens;
import johnsmith.enchantingoverhauled.client.particle.ForgeSimpleParticleTypes;
import johnsmith.enchantingoverhauled.client.render.entity.ForgeEntityRenderers;
import johnsmith.enchantingoverhauled.client.render.entity.model.ForgeModelLayers;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ForgeClient {

    public static void initialize(BusGroup eventBus, ModLoadingContext context) {
        // Common init
        Common.initialize();

        // Platform init
        FMLClientSetupEvent.getBus(eventBus).addListener(ForgeMenuScreens::initialize);
        RegisterParticleProvidersEvent.getBus(eventBus).addListener(ForgeSimpleParticleTypes::initialize);
        EntityRenderersEvent.RegisterLayerDefinitions.getBus(eventBus).addListener(ForgeModelLayers::initialize);
        EntityRenderersEvent.RegisterRenderers.getBus(eventBus).addListener(ForgeEntityRenderers::initialize);

        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (client, parent) -> Config.MANAGER.createScreen(parent)
                )
        );
    }
}