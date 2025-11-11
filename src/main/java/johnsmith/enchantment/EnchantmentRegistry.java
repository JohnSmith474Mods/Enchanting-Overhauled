package johnsmith.enchantment;

import johnsmith.EnchantingOverhauled;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class EnchantmentRegistry {
    // Static enchantment declarations.
    public static final Enchantment MAGIC_PROTECTION = register("magic_protection",
        new MagicProtectionEnchantment(
            Enchantment.properties(
                ItemTags.ARMOR_ENCHANTABLE,
                3,
                4,
                Enchantment.leveledCost(1, 11), // Unused property
                Enchantment.leveledCost(12, 11), // Unused property
                1,
                new EquipmentSlot[] {EquipmentSlot.CHEST}
            )
        )
    );

    private static Enchantment register(String name, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(EnchantingOverhauled.MOD_ID, name), enchantment);
    }

    public static void initialize() {
        EnchantingOverhauled.LOGGER.info("Initializing enchantment registry...");
        // This method's only purpose is to be called from onInitialize to ensure the
        // static RegistryKey fields above are loaded by the JVM.
    }
}
