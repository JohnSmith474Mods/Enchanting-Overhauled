package johnsmith.enchantingoverhauled.client.render.entity;

import johnsmith.enchantingoverhauled.entity.Entities;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public final class EntityRenderers {
    @SuppressWarnings("rawtypes")
    public static EntityRendererProvider<? extends Entity> getRenderer(Entities type) {
        return switch (type) {
            case ENCHANTED_TOME -> (EntityRendererProvider) EnchantedTomeRenderer::new;
            case ENCHANTED_BOOK -> (EntityRendererProvider) EnchantedBookRenderer::new;
            case MIRAGE -> (EntityRendererProvider) MirageRenderer::new;
            case MAGIC_MISSILE -> (EntityRendererProvider) MagicMissileRenderer::new;
        };
    }

    private EntityRenderers() {}
}
