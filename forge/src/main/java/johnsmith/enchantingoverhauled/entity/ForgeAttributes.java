package johnsmith.enchantingoverhauled.entity;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

public final class ForgeAttributes {

    public static void initialize(EntityAttributeCreationEvent event) {
        event.put(Entities.ENCHANTED_TOME.get(), EnchantedTomeEntity.createAttributes().build());
        event.put(Entities.ENCHANTED_BOOK.get(), EnchantedBookEntity.createAttributes().build());
        event.put(Entities.MIRAGE.get(), MirageEntity.createAttributes().build());
    }

    private ForgeAttributes() {}
}