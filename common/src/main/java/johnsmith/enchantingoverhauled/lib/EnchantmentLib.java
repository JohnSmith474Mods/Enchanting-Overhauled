package johnsmith.enchantingoverhauled.lib;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.AddIfBlockStateBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.CountItemsInInventoryBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.MultiplyByBlockStateBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.bonus.PowerBonus;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.PowerProvider;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.platform.Services;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Main library for enchantment-related logic in the common module.
 * <p>
 * Handles power calculation via {@link EnchantmentTheme}, enchantment generation filtering,
 * and utilities for manipulating {@link ItemEnchantments} components across platforms.
 */
public class EnchantmentLib {

    /**
     * The character length to wrap enchantment descriptions at in tooltips.
     */
    private static final int MAX_TOOLTIP_LINE_LENGTH = 40;

    /**
     * A pre-built Component for indenting description lines.
     */
    private static final Component TOOLTIP_INDENT = Component.literal("  ");

    /**
     * The number of random enchantments to apply to a newly generated Tome.
     */
    private static final int TOME_ENCHANTMENT_COUNT = 3;

    /**
     * Lookup table for Roman numeral conversion values.
     */
    private static final int[] ROMAN_VALUES = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    /**
     * Lookup table for Roman numeral conversion symbols.
     */
    private static final String[] ROMAN_SYMBOLS = {
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };

    /**
     * Wraps a description Component at 40 characters, indenting all lines
     * and preserving the original text's style.
     *
     * @param description The base Component for the description.
     * @return A List of FormattedCharSequence, ready to be added to a tooltip.
     */
    public static List<FormattedCharSequence> wrapDescription(Component description) {
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        String descriptionString = description.getString();
        Style descriptionStyle = description.getStyle();

        // Do nothing if the description is missing or empty
        if (descriptionString.isEmpty()) {
            return wrappedLines;
        }

        String remainingString = descriptionString;

        // Loop while the remaining string is longer than the max length
        while (remainingString.length() > MAX_TOOLTIP_LINE_LENGTH) {
            String line;
            // Find the last space within the first 40 characters
            int wrapAt = remainingString.lastIndexOf(' ', MAX_TOOLTIP_LINE_LENGTH);

            if (wrapAt <= 0) {
                // No space found, or space is at index 0. Hard wrap.
                line = remainingString.substring(0, MAX_TOOLTIP_LINE_LENGTH);
                remainingString = remainingString.substring(MAX_TOOLTIP_LINE_LENGTH);
            } else {
                // Space found. Wrap at the space.
                line = remainingString.substring(0, wrapAt);
                // Remove the line and the space after it
                remainingString = remainingString.substring(wrapAt + 1);
            }

            wrappedLines.add(TOOLTIP_INDENT.copy().append(Component.literal(line)).setStyle(descriptionStyle).getVisualOrderText());
        }

        // Add the final remaining part of the string
        if (!remainingString.isEmpty()) {
            wrappedLines.add(TOOLTIP_INDENT.copy().append(Component.literal(remainingString)).setStyle(descriptionStyle).getVisualOrderText());
        }

        return wrappedLines;
    }

    /**
     * Retrieves the appropriate enchantment component (ENCHANTMENTS or STORED_ENCHANTMENTS)
     * from the given ItemStack.
     *
     * @param stack The ItemStack to check.
     * @return The ItemEnchantments container, or ItemEnchantments.EMPTY if none is found.
     */
    public static ItemEnchantments getEnchantments(ItemStack stack) {
        return (ItemEnchantments)stack.getOrDefault(getEnchantmentsComponentType(stack), ItemEnchantments.EMPTY);
    }

    /**
     * Helper method for getting the correct enchantments component type.
     *
     * @param stack The ItemStack.
     * @return {@link DataComponents#STORED_ENCHANTMENTS} for books,
     * {@link DataComponents#ENCHANTMENTS} otherwise.
     */
    private static DataComponentType<ItemEnchantments> getEnchantmentsComponentType(ItemStack stack) {
        return stack.getItem() instanceof EnchantedBookItem ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }

    /**
     * Calculates the enchanting power provided by a block at a given position for a specific theme.
     * <p>
     * This method is fully data-driven. It checks the block against the theme's
     * specific list of {@link PowerProvider} rules and delegates to {@link #calculatePower}
     * if a match is found.
     *
     * @param level The level to check.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @param theme The EnchantmentTheme to calculate power for.
     * @return The integer power level (0 or the calculated value).
     */
    public static int getEnchantingPower(Level level, BlockPos providerPos, EnchantmentTheme theme) {
        BlockState state = level.getBlockState(providerPos);
        Holder<Block> blockEntry = state.getBlockHolder();

        // Check the block against the theme's specific power providers.
        for (PowerProvider provider : theme.powerProviders()) {
            // Check if the block state is in the provider's list (which can be a tag or list)
            if (provider.blocks().contains(blockEntry)) {
                // Found a matching provider, calculate its power including bonuses
                return calculatePower(level, providerPos, state, provider);
            }
        }

        // No provider found for this theme
        return 0;
    }

    /**
     * Calculates the highest enchanting power provided by a block at a given position,
     * regardless of theme.
     * <p>
     * This method checks the block against *all* registered themes and returns the
     * maximum power value found, including all bonus calculations.
     *
     * @param level The level to check.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @return The highest integer power level found across all themes (0 if none).
     */
    public static int getAgnosticEnchantingPower(Level level, BlockPos providerPos) {
        BlockState state = level.getBlockState(providerPos);
        Holder<Block> blockEntry = state.getBlockHolder();
        int maxPower = 0;

        // Get RegistryAccess from the world, then get the dynamic registry
        RegistryAccess registryAccess = level.registryAccess();
        Optional<Registry<EnchantmentTheme>> registryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (registryOpt.isPresent()) {
            Registry<EnchantmentTheme> registry = registryOpt.get();
            for (EnchantmentTheme theme : registry) {
                for (PowerProvider provider : theme.powerProviders()) {
                    if (provider.blocks().contains(blockEntry)) {
                        // This block is a provider for this theme.
                        // Calculate its full power.
                        int currentPower = calculatePower(level, providerPos, state, provider);

                        if (currentPower > maxPower) {
                            maxPower = currentPower;
                        }
                        // A block can only be in one provider list *per theme*.
                        // We break to check the next theme.
                        break;
                    }
                }
            }
        } else {
            Constants.LOG.warn("");
        }
        return maxPower;
    }

    /**
     * Helper method to calculate the total power from a single PowerProvider,
     * including all its data-driven bonuses.
     *
     * @param level The level (for BlockEntity access).
     * @param pos The position of the power provider block.
     * @param state The BlockState of the power provider.
     * @param provider The matching PowerProvider rule.
     * @return The total calculated power, including bonuses.
     */
    private static int calculatePower(Level level, BlockPos pos, BlockState state, PowerProvider provider) {
        int totalPower = provider.power();
        int basePower = provider.power(); // Store base power for multiplication bonuses

        for (PowerBonus bonus : provider.bonuses()) {
            try {
                // --- Multiply by Block State (e.g., Sea Pickles, Candles) ---
                if (bonus instanceof MultiplyByBlockStateBonus(String property)) {
                    Property<?> prop = state.getBlock().getStateDefinition().getProperty(property);
                    if (prop instanceof IntegerProperty intProp) {
                        int multiplier = state.getValue(intProp);
                        // Recalculate total power based on base * multiplier,
                        // rather than just adding (base * multiplier) to total.
                        totalPower = totalPower - basePower + (basePower * multiplier);
                    }

                    // --- Add if Block State (e.g., Lit Candles) ---
                } else if (bonus instanceof AddIfBlockStateBonus(String property, String value, int bonus1)) {
                    Property<?> prop = state.getBlock().getStateDefinition().getProperty(property);
                    if (prop != null) {
                        String currentValue = state.getValue(prop).toString();
                        if (currentValue.equalsIgnoreCase(value)) {
                            totalPower += bonus1;
                        }
                    }

                    // --- Add from Block Entity (e.g., Chiseled Bookshelf, Chests) ---
                } else if (bonus instanceof CountItemsInInventoryBonus(
                        net.minecraft.core.HolderSet<Item> items, int bonusPerItem
                )) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be == null) continue;

                    int itemCount = 0;

                    // Handle standard Inventories (Chests, Hoppers, etc.)
                    if (be instanceof Container inventory) {
                        for (int i = 0; i < inventory.getContainerSize(); i++) {
                            ItemStack stack = inventory.getItem(i);
                            if (items.contains(stack.getItemHolder())) {
                                // This counts individual items, not stacks.
                                itemCount += stack.getCount();
                            }
                        }
                    }
                    // Add other inventory types (e.g., Fabric API) here

                    totalPower += (itemCount * bonusPerItem);
                }
            } catch (Exception e) {
                // Log an error if the JSON is malformed (e.g., bad property name)
                Constants.LOG.warn("Failed to calculate power bonus for {}: {}",
                        BuiltInRegistries.BLOCK.getId(state.getBlock()), e.getMessage());
            }
        }
        return totalPower;
    }

    /**
     * Client-safe check to see if a block *could* provide power for *any* theme.
     * <p>
     * This method is used client-side for particle spawning. It iterates all
     * registered themes (including DEFAULT) to see if this block is a power provider.
     *
     * @param level The level to check.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @return True if the block is a recognized power provider, false otherwise.
     */
    public static boolean shouldSpawnParticles(Level level, BlockPos providerPos) {
        // A block should spawn particles if it provides any amount of power for any theme.
        return getAgnosticEnchantingPower(level, providerPos) > 0;
    }

    /**
     * Creates a copy of the target stack with all Cursed enchantments removed
     * from its 'minecraft:enchantments' or 'minecraft:stored_enchantments' component.
     *
     * @param target The original ItemStack.
     * @return A copy of the ItemStack with curses removed.
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
            if (enchantmentEntry.value().isCurse()) {
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

    /**
     * Converts the item's enchantment component into a Map of Enchantment to Level (Integer).
     *
     * @param target The ItemStack to deserialize.
     * @return A map containing active enchantments and their levels.
     */
    public static Map<Enchantment, Integer> deserializeEnchantments(ItemStack target) {
        ItemEnchantments component = EnchantmentHelper.getEnchantmentsForCrafting(target);
        return component.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().value(),
                        Object2IntMap.Entry::getIntValue
                ));
    }

    /**
     * Converts an ItemEnchantments component into a List of EnchantmentInstance objects.
     *
     * @param enchantments The component to process.
     * @return A List of entries.
     */
    public static List<EnchantmentInstance> getEnchantmentsAsList(ItemEnchantments enchantments) {
        return enchantments.entrySet().stream().map(
                entry -> new EnchantmentInstance(entry.getKey().value(), entry.getIntValue())
        ).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific enchantment entry by its order/position in the item's enchantment list.
     *
     * @param enchantments The enchantment component.
     * @param index The zero-based index of the enchantment to retrieve.
     * @return The Map.Entry of the enchantment, or null if the index is out of bounds.
     */
    @Nullable
    public static Map.Entry<Enchantment, Integer> getEnchantmentByPosition(ItemEnchantments enchantments, int index) {
        if (enchantments == null || index < 0) {
            return null;
        }

        // This is an inefficient way to get by index, but components don't guarantee order.
        // We create a map simply to get an iterator.
        Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().value(),
                Object2IntMap.Entry::getIntValue
        )).entrySet().iterator();


        for (int i = 0; i < index; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            } else {
                return null; // Index out of bounds
            }
        }

        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    /**
     * Creates a new map containing only the non-cursed enchantments from the input map.
     *
     * @param target The ItemStack (unused, but kept for signature).
     * @param potentiallyCursedMap A map of enchantments to levels.
     * @return A new map containing only non-cursed enchantments.
     */
    private static Map<Enchantment, Integer> getCurseFreeMap(ItemStack target, Map<Enchantment, Integer> potentiallyCursedMap) {
        Map<Enchantment, Integer> definetlyUncursedMap = Maps.newLinkedHashMap();
        for(Map.Entry<Enchantment, Integer> entry : potentiallyCursedMap.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.isCurse()) {
                definetlyUncursedMap.put(enchantment, entry.getValue());
            }
        }
        return definetlyUncursedMap;
    }

    /**
     * Checks if an enchanting table has any valid power providers for a *specific theme* nearby.
     * <p>
     * This check is used to determine if themed enchantments should be generated.
     * This method explicitly ignores the DEFAULT theme.
     *
     * @param level The level to check.
     * @param tablePos The BlockPos of the enchanting table.
     * @param themeKey The ResourceKey of the theme to check for.
     * @return True if a matching themed provider is found, false otherwise.
     */
    public static boolean hasThemedPowerProvider(Level level, BlockPos tablePos, ResourceKey<EnchantmentTheme> themeKey) {
        // The DEFAULT theme is always "active" and does not have special providers.
        if (themeKey.equals(EnchantmentThemeRegistry.DEFAULT)) {
            return false;
        }

        // Get DynamicRegistryManager from the world, then get the dynamic registry
        RegistryAccess registryAccess = level.registryAccess();
        Optional<Registry<EnchantmentTheme>> registryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (registryOpt.isEmpty()) return false;

        Optional<EnchantmentTheme> themeOpt = registryOpt.get().getOptional(themeKey);

        if (themeOpt.isEmpty()) {
            return false; // Theme doesn't exist
        }
        EnchantmentTheme theme = themeOpt.get();

        for (BlockPos providerOffset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            BlockPos transmitterPos = tablePos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);

            // Check if the "transmitter" block (air) is clear
            if (level.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                BlockPos providerPos = tablePos.offset(providerOffset);
                BlockState providerState = level.getBlockState(providerPos);
                Holder<Block> blockEntry = providerState.getBlockHolder();

                // Check this block against the theme's providers
                for (PowerProvider provider : theme.powerProviders()) {
                    if (provider.blocks().contains(blockEntry)) {
                        return true; // Found a matching provider
                    }
                }
            }
        }
        return false; // No matching providers found
    }

    /**
     * Generates a randomized list of possible enchantments for an item based on its enchantability,
     * filtered by available themed power providers.
     *
     * @param enabledFeatures Feature set for the world.
     * @param random The random source.
     * @param target The ItemStack to be enchanted.
     * @param treasureAllowed If treasure enchantments should be included (base setting).
     * @param level The level (for checking themed providers).
     * @param pos The pos of the enchanting table (for checking themed providers).
     * @return A list of generated, shuffled enchantment entries.
     */
    public static List<EnchantmentInstance> generateEnchantments(FeatureFlagSet enabledFeatures, RandomSource random, ItemStack target, boolean treasureAllowed, Level level, BlockPos pos) {
        List<EnchantmentInstance> enchantments = Lists.newArrayList();
        Item item = target.getItem();

        int enchantability = item.getEnchantmentValue();
        if (enchantability <= 0) {
            return enchantments;
        }

        // Get the list of possible entries, filtered by theme
        List<EnchantmentInstance> possibleEntries = EnchantmentLib.getPossibleEntries(enabledFeatures, target, treasureAllowed, level, pos);

        enchantments.addAll(possibleEntries);

        // Shuffle the final list
        enchantments = EnchantmentLib.weightedSkewedShuffle(enchantments, entry -> entry.getWeight().asInt(), random);

        return enchantments;
    }

    /**
     * Gets a list of all enchantment entries that can possibly be applied to the given item,
     * filtered by the available themed power providers.
     * <p>
     * This method implements the core logic for theme-based enchantment availability,
     * checking against the {@link EnchantmentThemeAccessor} to filter options.
     *
     * @param enabledFeatures Feature set for the world.
     * @param target The ItemStack.
     * @param treasureAllowed If treasure enchantments are allowed by default.
     * @param level The level (for checking themed providers).
     * @param pos The pos of the enchanting table (for checking themed providers).
     * @return A list of valid enchantment entries (level 1).
     */
    public static List<EnchantmentInstance> getPossibleEntries(FeatureFlagSet enabledFeatures, ItemStack target, boolean treasureAllowed, Level level, BlockPos pos) {
        List<EnchantmentInstance> enchantments = Lists.newArrayList();
        boolean isTargetBook = target.is(Items.BOOK);

        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {

            // Get the theme key associated with this enchantment via the Accessor
            ResourceKey<EnchantmentTheme> themeKey = ((EnchantmentThemeAccessor)enchantment).enchanting_overhauled$getTheme();
            boolean isThemed = !themeKey.equals(EnchantmentThemeRegistry.DEFAULT);
            boolean themeActive = false;

            if (isThemed) {
                // Check if the enchanting table has power for that theme.
                themeActive = EnchantmentLib.hasThemedPowerProvider(level, pos, themeKey);
                if (!themeActive) {
                    continue; // Skip this enchantment, its theme providers are not present
                }
            }
            // If the theme IS default, it's always available (themeActive remains false).


            // Allow treasure enchantments if:
            // 1. The method parameter allows it (treasureAllowed)
            // 2. OR The enchantment's theme is active (themeActive)
            boolean canApplyTreasure = treasureAllowed || themeActive;

            if (enchantment.isTreasureOnly() && !canApplyTreasure) {
                continue; // It's a treasure enchant, and we're not allowed to apply it.
            }

            // Altered vanilla logic
            if (enchantment.isEnabled(enabledFeatures)
                    && (enchantment.isTradeable() || themeActive)
                    && (isTargetBook || enchantment.canEnchant(target))
            ) {
                enchantments.add(new EnchantmentInstance(enchantment, 1));
            }
        }

        return enchantments;
    }

    /**
     * Shuffles a list, skewing items with higher weights toward the front.
     *
     * @param <T> The type of item in the list.
     * @param originalList The list of items to shuffle.
     * @param weightExtractor A function to get the weight (int) of an item.
     * @param random A Random instance.
     * @return A new list, shuffled and skewed.
     */
    public static <T> List<T> weightedSkewedShuffle(Collection<T> originalList, ToIntFunction<T> weightExtractor, RandomSource random) {

        List<WeightedSortItem<T>> weightedList = new ArrayList<>(originalList.size());

        for (T item : originalList) {
            double weight = (double)weightExtractor.applyAsInt(item);

            if (weight <= 0) {
                weightedList.add(new WeightedSortItem<>(item, Double.NEGATIVE_INFINITY));
                continue;
            }

            // Create a sort key by dividing weight by a random fraction
            double randomFactor = 1.0 - random.nextDouble();
            double sortKey = weight / randomFactor;

            weightedList.add(new WeightedSortItem<>(item, sortKey));
        }

        // Sort by the (high) sortKey in descending order
        Collections.sort(weightedList);

        // Convert back to the original item type
        return weightedList.stream()
                .map(weightedItem -> weightedItem.item)
                .collect(Collectors.toList());
    }

    /**
     * Helper class for weighted shuffling.
     * @param <T> The type of item being sorted.
     */
    private static class WeightedSortItem<T> implements Comparable<WeightedSortItem<T>> {
        final T item;
        final double sortKey;

        WeightedSortItem(T item, double sortKey) {
            this.item = item;
            this.sortKey = sortKey;
        }

        /**
         * Compares in descending order of sort key.
         */
        @Override
        public int compareTo(WeightedSortItem<T> other) {
            return Double.compare(other.sortKey, this.sortKey);
        }
    }

    /**
     * Enchants a given Enchanted Tome ItemStack with 3 random, high-level enchantments.
     * This logic is mirrored from the loot table injection.
     */
    public static ItemStack enchantTomeRandomly(ItemStack tomeStack, Level world, RandomSource random) {
        // 1. Check if it's the right item
        if (!tomeStack.is(Services.PLATFORM.getEnchantedTome())) {
            return tomeStack;
        }

        // 2. Check if it's already enchanted
        ItemEnchantments enchants = tomeStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return tomeStack;
        }

        // 3. Get all valid, enabled enchantments
        List<Enchantment> enchantmentList = BuiltInRegistries.ENCHANTMENT.stream()
                .filter((entry) -> entry.isEnabled(world.enabledFeatures()))
                .toList();

        if (enchantmentList.isEmpty()) {
            return tomeStack;
        }

        return applyTomeEnchantments(tomeStack, enchantmentList, random);
    }

    /**
     * Applies the "Tome Logic" (Random Selection + Configurable Over-leveling)
     * to a specific list of candidates.
     * * @param tomeStack The stack to modify.
     * @param candidates The list of possible enchantments.
     * @param random The random source.
     * @return The modified stack.
     */
    public static ItemStack applyTomeEnchantments(ItemStack tomeStack, List<Enchantment> candidates, RandomSource random) {
        // 1. Shuffle the list
        List<Enchantment> mutableList = new ArrayList<>(candidates);
        Util.shuffle(mutableList, random);

        // 2. Get a builder for the STORED_ENCHANTMENTS component
        // Use the existing component if present, or empty if not
        ItemEnchantments current = tomeStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(current);

        // 3. Apply the first TOME_ENCHANTMENT_COUNT enchantments
        int count = 0;
        for (Enchantment entry : mutableList) {
            if (count >= TOME_ENCHANTMENT_COUNT) {
                break;
            }

            // 4. Apply custom level logic
            int maxLevel = entry.getMaxLevel();
            int newLevel = maxLevel;

            // Only apply the +1 level bonus if the config is enabled
            if (Config.TOMES_HAVE_GREATER_ENCHANTMENTS) {
                // Prevent boosting single-level enchantments (like Mending/Infinity)
                if (maxLevel > 1) {
                    newLevel = maxLevel + 1;
                }
            }

            // 5. Add the enchantment to the builder
            builder.set(entry, newLevel);
            count++;
        }

        // 6. Apply the built component back to the stack
        if (count > 0) {
            tomeStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        } else {
            Constants.LOG.warn("Couldn't find any compatible enchantments for {}", tomeStack);
        }

        return tomeStack;
    }

    /**
     * Checks if a given tome stack is empty or unenchanted.
     * If it is, it returns a new tome stack with a default enchantment (Protection IV).
     * If the stack is valid and already enchanted, it returns the original stack.
     * <p>
     * Uses {@link Services#PLATFORM} to retrieve the correct tome item.
     *
     * @param tomeStack The ItemStack to check.
     * @return An enchanted ItemStack.
     */
    public static ItemStack ensureTomeIsEnchanted(ItemStack tomeStack) {
        // 1. If the stack is null or not a tome, create a new one.
        if (tomeStack == null || !tomeStack.is(Services.PLATFORM.getEnchantedTome())) {
            tomeStack = new ItemStack(Services.PLATFORM.getEnchantedTome());
        }

        // 2. Check if it's already enchanted
        ItemEnchantments enchants = tomeStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            // 3. It's not enchanted, so apply the default Protection
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            builder.set(Enchantments.PROTECTION, Config.ENCHANTMENT_MAX_LEVEL + 1);
            tomeStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        }

        // 4. Return the (now guaranteed) enchanted stack
        return tomeStack;
    }

    /**
     * A static helper to get a new Enchanted Tome with the default
     * enchantment (Protection IV).
     * <p>
     * Uses {@link Services#PLATFORM} to create the correct item stack.
     *
     * @return A new ItemStack of an Enchanted Tome with Protection IV.
     */
    public static ItemStack getTomeWithDefaultEnchantment() {
        ItemStack tome = new ItemStack(Services.PLATFORM.getEnchantedTome());
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        builder.set(Enchantments.PROTECTION, Config.ENCHANTMENT_MAX_LEVEL + 1);
        tome.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        return tome;
    }

    /**
     * Converts an integer to its Roman numeral representation.
     * Supports numbers from 1 to 3999. Numbers outside this range
     * will be returned as a standard string.
     *
     * @param number The integer to convert.
     * @return The Roman numeral as a String.
     */
    public static String toRoman(int number) {
        // Fallback for numbers outside the supported range
        if (number < 1 || number > 3999) {
            return String.valueOf(number);
        }

        StringBuilder sb = new StringBuilder();
        int remaining = number;

        // Loop through the values from largest to smallest
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            // Greedily subtract the largest possible value
            while (remaining >= ROMAN_VALUES[i]) {
                sb.append(ROMAN_SYMBOLS[i]);
                remaining -= ROMAN_VALUES[i];
            }
        }

        return sb.toString();
    }
}