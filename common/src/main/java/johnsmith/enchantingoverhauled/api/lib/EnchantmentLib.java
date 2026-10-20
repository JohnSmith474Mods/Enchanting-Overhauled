package johnsmith.enchantingoverhauled.api.lib;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.AddIfBlockStateBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.CountItemsInInventoryBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.MultiplyByBlockStateBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.PowerBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.PowerProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

/**
 * Main library for enchantment-related logic in the common module.
 * <p>
 * Handles power calculation via {@link EnchantmentTheme}, enchantment generation filtering,
 * and utilities for manipulating {@link ItemEnchantments} components across platforms.
 */
public class EnchantmentLib {

    /**
     * Retrieves the appropriate enchantment component ({@link DataComponents#ENCHANTMENTS} or
     * {@link DataComponents#STORED_ENCHANTMENTS}) from the given {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to check.
     * @return The {@link ItemEnchantments} container, or {@link ItemEnchantments#EMPTY} if none is found.
     */
    public static ItemEnchantments getEnchantments(ItemStack stack) {
        return stack.getOrDefault(getEnchantmentsComponentType(stack), ItemEnchantments.EMPTY);
    }

    /**
     * Helper method for resolving the appropriate enchantments data component type on an item stack.
     *
     * @param stack The {@link ItemStack} to inspect.
     * @return {@link DataComponents#STORED_ENCHANTMENTS} if present on the stack,
     *         {@link DataComponents#ENCHANTMENTS} otherwise.
     */
    private static DataComponentType<ItemEnchantments> getEnchantmentsComponentType(ItemStack stack) {
        return stack.has(DataComponents.STORED_ENCHANTMENTS) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    /**
     * Calculates the highest enchanting power provided by a block at a given position,
     * regardless of theme.
     * <p>
     * If the block at {@code providerPos} is a {@link Blocks#STRUCTURE_VOID}, it acts as a debug
     * block and grants the maximum configured enchanting power ({@link Config#MAX_ENCHANTING_POWER}).
     *
     * @param level       The {@link Level} to check.
     * @param providerPos The {@link BlockPos} of the potential power-providing block.
     * @return The highest integer power level found across all registered themes ({@code 0} if none).
     */
    public static int getAgnosticEnchantingPower(Level level, BlockPos providerPos) {
        BlockState state = level.getBlockState(providerPos);

        // Debug block: Structure Void grants the maximum configured enchanting power
        if (state.is(Blocks.STRUCTURE_VOID)) {
            return Config.MAX_ENCHANTING_POWER.get();
        }

        Holder<Block> blockEntry = state.getBlockHolder();
        int maxPower = 0;

        Optional<Registry<EnchantmentTheme>> registryOpt = level.registryAccess().registry(EnchantmentThemes.THEME_REGISTRY_KEY);
        if (registryOpt.isPresent()) {
            Registry<EnchantmentTheme> registry = registryOpt.get();

            for (EnchantmentTheme theme : registry) {
                for (PowerProvider provider : theme.powerProviders()) {
                    if (provider.blocks().contains(blockEntry)) {
                        int currentPower = calculatePower(level, providerPos, state, provider);
                        if (currentPower > maxPower) {
                            maxPower = currentPower;
                        }
                        break;
                    }
                }
            }
        }
        return maxPower;
    }

    /**
     * Calculates the total enchanting power contributed by a single {@link PowerProvider} at the
     * given position, evaluating all of its data-driven {@link PowerBonus} rules (such as block state
     * multipliers, conditional property additions, and container inventory item counts).
     *
     * @param level    The {@link Level} containing the block.
     * @param pos      The {@link BlockPos} of the power-providing block.
     * @param state    The {@link BlockState} at {@code pos}.
     * @param provider The {@link PowerProvider} rule definition to evaluate.
     * @return The total computed integer enchanting power.
     */
    public static int calculatePower(Level level, BlockPos pos, BlockState state, PowerProvider provider) {
        int totalPower = provider.power();
        int basePower = provider.power();

        for (PowerBonus bonus : provider.bonuses()) {
            try {
                if (bonus instanceof MultiplyByBlockStateBonus(String property)) {
                    Property<?> prop = state.getBlock().getStateDefinition().getProperty(property);
                    if (prop instanceof IntegerProperty intProp) {
                        int multiplier = state.getValue(intProp);
                        totalPower = totalPower - basePower + (basePower * multiplier);
                    }
                } else if (bonus instanceof AddIfBlockStateBonus(String property, String value, int bonus1)) {
                    Property<?> prop = state.getBlock().getStateDefinition().getProperty(property);
                    if (prop != null) {
                        String currentValue = state.getValue(prop).toString();
                        if (currentValue.equalsIgnoreCase(value)) {
                            totalPower += bonus1;
                        }
                    }
                } else if (bonus instanceof CountItemsInInventoryBonus(
                        net.minecraft.core.HolderSet<Item> items, int bonusPerItem
                )) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be == null) continue;

                    int itemCount = 0;

                    if (be instanceof Container inventory) {
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            ItemStack stack = inventory.getItem(i);
                            if (items.contains(stack.getItemHolder())) {
                                itemCount += stack.getCount();
                            }
                        }
                    }

                    totalPower += (itemCount * bonusPerItem);
                }
            } catch (Exception e) {
                Constants.LOG.warn(
                        "Failed to calculate power bonus for {}: {}",
                        BuiltInRegistries.BLOCK.getId(state.getBlock()),
                        e.getMessage()
                );
            }
        }
        return totalPower;
    }

    /**
     * Creates a copy of the target stack with all cursed enchantments (those tagged with
     * {@link EnchantmentTags#CURSE}) removed from its {@code minecraft:enchantments} or
     * {@code minecraft:stored_enchantments} component.
     *
     * @param target The original {@link ItemStack}.
     * @return A copy of the {@link ItemStack} with all curses removed, or {@code target} if unenchanted.
     */
    public static ItemStack removeCursesFrom(ItemStack target) {
        DataComponentType<ItemEnchantments> componentType = getEnchantmentsComponentType(target);
        ItemEnchantments enchants = target.get(componentType);

        if (enchants == null || enchants.isEmpty()) {
            return target;
        }

        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(enchants);
        List<Holder<Enchantment>> cursesToRemove = new ArrayList<>();

        for (Holder<Enchantment> enchantmentEntry : enchants.keySet()) {
            if (enchantmentEntry.is(EnchantmentTags.CURSE)) {
                cursesToRemove.add(enchantmentEntry);
            }
        }

        for (Holder<Enchantment> curse : cursesToRemove) {
            builder.removeIf(entry -> entry.equals(curse));
        }

        ItemStack copy = target.copy();
        copy.set(componentType, builder.toImmutable());
        return copy;
    }
}