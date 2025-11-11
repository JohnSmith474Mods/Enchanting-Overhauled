package johnsmith.item;

import johnsmith.EnchantingOverhauled;
import johnsmith.block.BlockRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantingOverhauledItemGroups {
    // Define the ID and Key for your new tab
    public static final Identifier GROUP_ID = new Identifier(EnchantingOverhauled.MOD_ID, "enchanting_group");
    public static final RegistryKey<ItemGroup> ENCHANTING_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, GROUP_ID);

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing item groups...");
        // Register the new item group
        Registry.register(Registries.ITEM_GROUP, ENCHANTING_GROUP_KEY, FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.enchanting_overhauled.enchanting_group"))
                .icon(() -> new ItemStack(ItemRegistry.ENCHANTED_TOME)) // Set the icon
                .entries((displayContext, entries) -> {

                    // 1. Add the deactivated table
                    entries.add(BlockRegistry.DEACTIVATED_ENCHANTING_TABLE);
                    entries.add(BlockRegistry.DISTURBED_ENCHANTING_TABLE);

                    // 2. Add all enchantments with custom levels
                    Registries.ENCHANTMENT.forEach(enchantment -> {
                        // Check if the enchantment is enabled
                        if (enchantment.isEnabled(displayContext.enabledFeatures())) {

                            // Apply your level logic: (max == 1) ? 1 : max + 1
                            int maxLevel = enchantment.getMaxLevel();
                            int level = (maxLevel == 1) ? 1 : maxLevel + 1;

                            // Create an enchanted book stack
                            ItemStack stack = new ItemStack(ItemRegistry.ENCHANTED_TOME);

                            // Get the enchantment's registry entry
                            RegistryEntry<Enchantment> entry = Registries.ENCHANTMENT.getEntry(enchantment);

                            // Build the stored enchantments component
                            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
                            builder.add(entry.value(), level);

                            // Apply the component to the stack
                            stack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());

                            // Add the book to the tab
                            entries.add(stack);
                        }
                    });
                })
                .build());
    }
}
