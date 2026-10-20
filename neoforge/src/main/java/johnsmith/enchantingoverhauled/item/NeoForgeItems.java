package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Items item : Items.values()) {
            ITEMS.register(item.getSerializedName(), item::build);
        }
        ITEMS.register(eventBus);
    }

    private NeoForgeItems() {}
}