package johnsmith.enchantingoverhauled.client.render.entity;

import johnsmith.enchantingoverhauled.entity.Entities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class FabricEntityRenderers {
    public static void initialize() {
        for (Entities entity : Entities.values()) {
            EntityRendererRegistry.register(entity.get(), EntityRenderers.getRenderer(entity));
        }
    }

    private FabricEntityRenderers() {}
}
