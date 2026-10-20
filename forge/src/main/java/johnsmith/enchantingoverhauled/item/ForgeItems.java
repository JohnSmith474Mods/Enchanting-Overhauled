package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeItems {

    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Items item : Items.values()) {
            ITEMS.register(item.getSerializedName(), item::build);
        }
        ITEMS.register(eventBus);
    }

    private ForgeItems() {}
}