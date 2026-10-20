package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds client-only constants, such as texture atlas materials and layer locations.
 */
public class ClientConstants {
    public static final ModelLayerLocation ENCHANTED_BOOK_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_book"), "main");
    public static final ModelLayerLocation ENCHANTED_TOME_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome"), "main");

    public static final Material ENCHANTED_BOOK_MATERIAL = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "entity/enchanted_book")
    );

    public static final Material ENCHANTED_TOME_MATERIAL = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "entity/enchanted_tome")
    );
}