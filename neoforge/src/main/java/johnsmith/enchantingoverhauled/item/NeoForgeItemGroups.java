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

public final class NeoForgeItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ENCHANTING_GROUP = CREATIVE_MODE_TABS.register(
            ItemGroups.ENCHANTING_GROUP_KEY.location().getPath(),
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.enchanting_overhauled.enchanting_group"))
                    .icon(() -> new ItemStack(Items.ENCHANTED_TOME.get()))
                    .displayItems(ItemGroups::displayItems)
                    .build()
    );

    public static void onBuildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            ItemGroups.populateIngredients(event.getParameters(), event);
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            ItemGroups.populateSpawnEggs(event);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            ItemGroups.populateFunctional(event);
        }
    }

    public static void initialize(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        eventBus.addListener(NeoForgeItemGroups::onBuildCreativeTabs);
    }

    private NeoForgeItemGroups() {}
}