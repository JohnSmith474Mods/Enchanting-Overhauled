package johnsmith.enchantingoverhauled.client.render.entity;

import johnsmith.enchantingoverhauled.entity.Entities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class NeoForgeEntityRenderers {
    public static void initialize(EntityRenderersEvent.RegisterRenderers event) {
        for (Entities entity : Entities.values()) {
            event.registerEntityRenderer(entity.get(), EntityRenderers.getRenderer(entity));
        }
    }

    private NeoForgeEntityRenderers() {}
}