package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.item.Items.ENCHANTING_TABLE;

/**
 * Registration handler for Creative Mode Tabs on the NeoForge platform.
 * <p>
 * Registers the custom tab and listens for the {@link BuildCreativeModeTabContentsEvent}
 * to modify vanilla tabs.
 */
public class NeoForgeItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENCHANTING_GROUP = CREATIVE_MODE_TABS.register(
            Constants.MOD_ID,
            () -> CreativeModeTab.builder()
                    .title(Component.literal(Constants.MOD_NAME))
                    .icon(() -> new ItemStack(Items.ENCHANTED_TOME.get()))
                    .displayItems(ItemGroups::displayItems) // Call common logic
                    .build()
    );

    /**
     * Event handler for populating creative mode tabs.
     * <p>
     * Adds items to Ingredients and Functional Blocks tabs and removes the vanilla
     * Enchanting Table from view to encourage using the mod's system.
     *
     * @param event The event context.
     */
    public static void onBuildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            ItemGroups.populateIngredients(event.getParameters(), event);
        }

        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ItemGroups.populateFunctional(event.getParameters(), event);

            // Fix: Safely remove the vanilla enchanting table from this tab.
            // 1. Iterate the search entries (view-only) to find the specific ItemStack instance.
            List<ItemStack> toRemove = new ArrayList<>();
            for (ItemStack stack : event.getSearchEntries()) {
                if (stack.is(ENCHANTING_TABLE)) {
                    toRemove.add(stack);
                }
            }

            // 2. Use the event's remove method to modify the underlying collection.
            for (ItemStack stack : toRemove) {
                event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        }
    }

    /**
     * Initializes the creative tab register and event listener.
     *
     * @param eventBus The mod event bus.
     */
    public static void initialize(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        eventBus.addListener(NeoForgeItemGroups::onBuildCreativeTabs);
    }
}