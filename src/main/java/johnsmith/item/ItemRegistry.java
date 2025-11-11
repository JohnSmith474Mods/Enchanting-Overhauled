package johnsmith.item;

import johnsmith.EnchantingOverhauled;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ItemRegistry {
    // Static item declarations.
    public static final Item ENCHANTED_TOME = register(
            "enchanted_tome",
            new EnchantedTomeItem(
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.EPIC)
                            .component(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
                            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            )
    );

    public static Item register(String id, Item item) {
        final Identifier itemKey = new Identifier(EnchantingOverhauled.MOD_ID, id);
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing item registry...");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            // Get the display context from the entries
            var displayContext = entries.getContext();

            Registries.ENCHANTMENT.forEach(enchantment -> {
                if (enchantment.isEnabled(displayContext.enabledFeatures())) {
                    int maxLevel = enchantment.getMaxLevel();
                    int level = (maxLevel == 1) ? 1 : maxLevel + 1;

                    ItemStack stack = new ItemStack(ItemRegistry.ENCHANTED_TOME);
                    RegistryEntry<Enchantment> entry = Registries.ENCHANTMENT.getEntry(enchantment);

                    ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                    builder.add(entry.value(), level); // Use RegistryEntry

                    stack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());

                    // Add the enchanted tome to the Ingredients tab
                    entries.add(stack);
                }
            });
        });
    }
}
