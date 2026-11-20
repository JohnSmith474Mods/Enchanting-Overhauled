package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

/**
 * Holds client-only constants, such as texture atlas sprites and layer locations.
 */
public class ClientConstants {
    public static final ModelLayerLocation ENCHANTED_BOOK_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_book"), "main");
    public static final ModelLayerLocation ENCHANTED_TOME_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome"), "main");

    public static final SpriteId ENCHANTED_BOOK_MATERIAL = new SpriteId(
            TextureAtlas.LOCATION_BLOCKS,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/enchanted_book")
    );

    public static final SpriteId ENCHANTED_TOME_MATERIAL = new SpriteId(
            TextureAtlas.LOCATION_BLOCKS,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity/enchanted_tome")
    );
}