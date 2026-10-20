package johnsmith.enchantingoverhauled.client.render.entity;

import johnsmith.enchantingoverhauled.entity.Entities;

import net.minecraftforge.client.event.EntityRenderersEvent;

public final class ForgeEntityRenderers {
    public static void initialize(EntityRenderersEvent.RegisterRenderers event) {
        for (Entities entity : Entities.values()) {
            event.registerEntityRenderer(entity.get(), EntityRenderers.getRenderer(entity));
        }
    }

    private ForgeEntityRenderers() {}
}