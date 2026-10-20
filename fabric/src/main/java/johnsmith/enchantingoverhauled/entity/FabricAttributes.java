package johnsmith.enchantingoverhauled.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public final class FabricAttributes {
    public static void initialize() {
        FabricDefaultAttributeRegistry.register(Entities.ENCHANTED_TOME.get(), EnchantedTomeEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(Entities.ENCHANTED_BOOK.get(), EnchantedBookEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(Entities.MIRAGE.get(), MirageEntity.createAttributes());
    }

    private FabricAttributes() {}
}
