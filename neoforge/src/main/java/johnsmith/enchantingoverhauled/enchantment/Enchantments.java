package johnsmith.enchantingoverhauled.enchantment;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration handler for enchantments on the NeoForge platform.
 * <p>
 * Uses {@link DeferredRegister} to handle registry events safely.
 */
public class Enchantments {

    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(BuiltInRegistries.ENCHANTMENT, Constants.MOD_ID);

    /**
     * A holder for the Magic Protection enchantment instance.
     */
    public static final DeferredHolder<Enchantment, MagicProtectionEnchantment> MAGIC_PROTECTION =
            ENCHANTMENTS.register("magic_protection", () -> new MagicProtectionEnchantment(
                    Enchantment.definition(
                            net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE,
                            3, // Weight
                            3, // Max Level
                            Enchantment.dynamicCost(1, 11), // Min Cost
                            Enchantment.dynamicCost(12, 11), // Max Cost
                            1, // Anvil Cost
                            EquipmentSlot.CHEST // Applicable Slots
                    )
            ));

    /**
     * Initializes the enchantment register.
     *
     * @param eventBus The mod event bus.
     */
    public static void initialize(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}