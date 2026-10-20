package johnsmith.enchantingoverhauled.entity;

import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class NeoForgeAttributes {
    public static void initialize(EntityAttributeCreationEvent event) {
        event.put(Entities.ENCHANTED_TOME.get(), EnchantedTomeEntity.createAttributes().build());
        event.put(Entities.ENCHANTED_BOOK.get(), EnchantedBookEntity.createAttributes().build());
        event.put(Entities.MIRAGE.get(), MirageEntity.createAttributes().build());
    }

    private NeoForgeAttributes() {}
}