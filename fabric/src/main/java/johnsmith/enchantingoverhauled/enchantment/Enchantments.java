package johnsmith.enchantingoverhauled.enchantment;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Registration handler for enchantments on the Fabric platform.
 * <p>
 * Holds the singleton instance of the custom enchantment and registers it with the
 * vanilla registry during initialization.
 */
public class Enchantments {

    /**
     * The singleton instance of the Magic Protection enchantment.
     * <p>
     * Configured to apply to Chest armor, with a max level of 3.
     */
    public static final Enchantment MAGIC_PROTECTION = new MagicProtectionEnchantment(
            Enchantment.definition(
                    net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE,
                    3, // Weight
                    3, // Max Level
                    Enchantment.dynamicCost(1, 11), // Min Cost
                    Enchantment.dynamicCost(12, 11), // Max Cost
                    1, // Anvil Cost
                    EquipmentSlot.CHEST // Applicable Slots
            )
    );

    /**
     * Registers the enchantments. Called during mod initialization.
     */
    public static void initialize() {
        Registry.register(BuiltInRegistries.ENCHANTMENT, new ResourceLocation(Constants.MOD_ID, "magic_protection"), MAGIC_PROTECTION);
    }
}