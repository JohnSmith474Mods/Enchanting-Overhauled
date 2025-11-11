package johnsmith.lib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import johnsmith.EnchantingOverhauled;
import johnsmith.api.enchantment.theme.accessor.EnchantmentThemeAccessor;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import johnsmith.api.enchantment.theme.power.bonus.AddIfBlockStateBonus;
import johnsmith.api.enchantment.theme.power.bonus.CountItemsInInventoryBonus;
import johnsmith.api.enchantment.theme.power.bonus.MultiplyByBlockStateBonus;
import johnsmith.api.enchantment.theme.power.bonus.PowerBonus;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.api.enchantment.theme.power.PowerProvider;
import johnsmith.config.Config;
import johnsmith.item.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Main library for enchantment-related logic, including power calculation,
 * enchantment generation, and item helpers.
 */
public class EnchantmentLib {

    /**
     * The character length to wrap enchantment descriptions at in tooltips.
     */
    private static final int MAX_TOOLTIP_LINE_LENGTH = 40;

    /**
     * A pre-built Text component for indenting description lines.
     */
    private static final Text TOOLTIP_INDENT = Text.literal("  ");

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
     * Wraps a description Text component at 40 characters, indenting all lines
     * and preserving the original text's style.
     *
     * @param description The base Text component for the description.
     * @return A List of OrderedText, ready to be added to a tooltip.
     */
    public static List<OrderedText> wrapDescription(Text description) {
        List<OrderedText> wrappedLines = new ArrayList<>();
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

            wrappedLines.add(TOOLTIP_INDENT.copy().append(Text.literal(line)).setStyle(descriptionStyle).asOrderedText());
        }

        // Add the final remaining part of the string
        if (!remainingString.isEmpty()) {
            wrappedLines.add(TOOLTIP_INDENT.copy().append(Text.literal(remainingString)).setStyle(descriptionStyle).asOrderedText());
        }

        return wrappedLines;
    }

    /**
     * Retrieves the appropriate enchantment component (ENCHANTMENTS or STORED_ENCHANTMENTS)
     * from the given ItemStack.
     *
     * @param stack The ItemStack to check.
     * @return The ItemEnchantmentsComponent, or ItemEnchantmentsComponent.DEFAULT if none is found.
     */
    public static ItemEnchantmentsComponent getEnchantments(ItemStack stack) {
        return (ItemEnchantmentsComponent)stack.getOrDefault(getEnchantmentsComponentType(stack), ItemEnchantmentsComponent.DEFAULT);
    }

    /**
     * Helper method for getting the correct enchantments component type.
     *
     * @param stack The ItemStack.
     * @return {@link DataComponentTypes#STORED_ENCHANTMENTS} for books,
     * {@link DataComponentTypes#ENCHANTMENTS} otherwise.
     */
    private static DataComponentType<ItemEnchantmentsComponent> getEnchantmentsComponentType(ItemStack stack) {
        return stack.getItem() instanceof EnchantedBookItem ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS;
    }

    /**
     * Calculates the enchanting power provided by a block at a given position for a specific theme.
     * <p>
     * This method is fully data-driven. It checks the block against the theme's
     * specific list of {@link PowerProvider} rules and delegates to {@link #calculatePower}
     * if a match is found.
     *
     * @param world The world.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @param theme The EnchantmentTheme to calculate power for.
     * @return The integer power level (0 or the calculated value).
     */
    public static int getEnchantingPower(World world, BlockPos providerPos, EnchantmentTheme theme) {
        BlockState state = world.getBlockState(providerPos);
        RegistryEntry<Block> blockEntry = state.getRegistryEntry();

        // Check the block against the theme's specific power providers.
        for (PowerProvider provider : theme.powerProviders()) {
            // Check if the block state is in the provider's list (which can be a tag or list)
            if (provider.blocks().contains(blockEntry)) {
                // Found a matching provider, calculate its power including bonuses
                return calculatePower(world, providerPos, state, provider);
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
     * @param world The world.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @return The highest integer power level found across all themes (0 if none).
     */
    public static int getAgnosticEnchantingPower(World world, BlockPos providerPos) {
        BlockState state = world.getBlockState(providerPos);
        RegistryEntry<Block> blockEntry = state.getRegistryEntry();
        int maxPower = 0;

        // Get RegistryAccess from the world, then get the dynamic registry
        DynamicRegistryManager registryAccess = world.getRegistryManager();
        Registry<EnchantmentTheme> registry = registryAccess.get(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);

        for (EnchantmentTheme theme : registry) {
            for (PowerProvider provider : theme.powerProviders()) {
                if (provider.blocks().contains(blockEntry)) {
                    // This block is a provider for this theme.
                    // Calculate its full power.
                    int currentPower = calculatePower(world, providerPos, state, provider);

                    if (currentPower > maxPower) {
                        maxPower = currentPower;
                    }
                    // A block can only be in one provider list *per theme*.
                    // We break to check the next theme.
                    break;
                }
            }
        }
        return maxPower;
    }

    /**
     * Helper method to calculate the total power from a single PowerProvider,
     * including all its data-driven bonuses.
     *
     * @param world The world (for BlockEntity access).
     * @param pos The position of the power provider block.
     * @param state The BlockState of the power provider.
     * @param provider The matching PowerProvider rule.
     * @return The total calculated power, including bonuses.
     */
    private static int calculatePower(World world, BlockPos pos, BlockState state, PowerProvider provider) {
        int totalPower = provider.power();
        int basePower = provider.power(); // Store base power for multiplication bonuses

        for (PowerBonus bonus : provider.bonuses()) {
            try {
                // --- Multiply by Block State (e.g., Sea Pickles, Candles) ---
                if (bonus instanceof MultiplyByBlockStateBonus multiplyBonus) {
                    Property<?> prop = state.getBlock().getStateManager().getProperty(multiplyBonus.property());
                    if (prop instanceof IntProperty intProp) {
                        int multiplier = state.get(intProp);
                        // Recalculate total power based on base * multiplier,
                        // rather than just adding (base * multiplier) to total.
                        totalPower = totalPower - basePower + (basePower * multiplier);
                    }

                    // --- Add if Block State (e.g., Lit Candles) ---
                } else if (bonus instanceof AddIfBlockStateBonus addIfBonus) {
                    Property<?> prop = state.getBlock().getStateManager().getProperty(addIfBonus.property());
                    if (prop != null) {
                        String currentValue = state.get(prop).toString();
                        if (currentValue.equalsIgnoreCase(addIfBonus.value())) {
                            totalPower += addIfBonus.bonus();
                        }
                    }

                    // --- Add from Block Entity (e.g., Chiseled Bookshelf, Chests) ---
                } else if (bonus instanceof CountItemsInInventoryBonus countBonus) {
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be == null) continue;

                    int itemCount = 0;

                    // Handle Chiseled Bookshelves (non-standard inventory)
                    if (be instanceof ChiseledBookshelfBlockEntity chiseledShelf) {
                        for (int i = 0; i < chiseledShelf.size(); i++) {
                            ItemStack stack = chiseledShelf.getStack(i);
                            if (countBonus.items().contains(stack.getRegistryEntry())) {
                                itemCount++;
                            }
                        }
                    }
                    // Handle standard Inventories (Chests, Hoppers, etc.)
                    else if (be instanceof Inventory inventory) {
                        for (int i = 0; i < inventory.size(); i++) {
                            ItemStack stack = inventory.getStack(i);
                            if (countBonus.items().contains(stack.getRegistryEntry())) {
                                // This counts *stacks*, not individual items.
                                // To count items, use: itemCount += stack.getCount();
                                itemCount++;
                            }
                        }
                    }
                    // Add other inventory types (e.g., Fabric API) here

                    totalPower += (itemCount * countBonus.bonusPerItem());
                }
            } catch (Exception e) {
                // Log an error if the JSON is malformed (e.g., bad property name)
                EnchantingOverhauled.LOGGER.warn("Failed to calculate power bonus for {}: {}",
                        Registries.BLOCK.getId(state.getBlock()), e.getMessage());
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
     * @param world The world.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @return True if the block is a recognized power provider, false otherwise.
     */
    public static boolean shouldSpawnParticles(World world, BlockPos providerPos) {
        // A block should spawn particles if it provides any amount of power for any theme.
        return getAgnosticEnchantingPower(world, providerPos) > 0;
    }

    /**
     * Creates a copy of the target stack with all Cursed enchantments removed
     * from its 'minecraft:enchantments' or 'minecraft:stored_enchantments' component.
     *
     * @param target The original ItemStack.
     * @return A copy of the ItemStack with curses removed.
     */
    public static ItemStack removeCursesFrom(ItemStack target) {
        DataComponentType<ItemEnchantmentsComponent> componentType = getEnchantmentsComponentType(target);
        ItemEnchantmentsComponent enchants = target.get(componentType);

        if (enchants == null || enchants.isEmpty()) {
            return target;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchants);
        List<RegistryEntry<Enchantment>> cursesToRemove = new ArrayList<>();
        for (RegistryEntry<Enchantment> enchantmentEntry : enchants.getEnchantments()) {
            if (enchantmentEntry.value().isCursed()) {
                cursesToRemove.add(enchantmentEntry);
            }
        }

        for (RegistryEntry<Enchantment> curse : cursesToRemove) {
            builder.remove(entry -> entry.equals(curse));
        }

        ItemStack copy = target.copy();
        copy.set(componentType, builder.build());
        return copy;
    }

    /**
     * Converts the item's enchantment component into a Map of Enchantment to Level (Integer).
     *
     * @param target The ItemStack to deserialize.
     * @return A map containing active enchantments.
     */
    public static Map<Enchantment, Integer> deserializeEnchantments(ItemStack target) {
        ItemEnchantmentsComponent component = EnchantmentHelper.getEnchantments(target);
        Map<Enchantment, Integer> enchantmentMap = component.getEnchantmentsMap().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().value(),
                        entry -> entry.getIntValue()
                ));
        return enchantmentMap;
    }

    /**
     * Converts an ItemEnchantmentsComponent into a List of EnchantmentLevelEntry objects.
     *
     * @param enchantments The component to process.
     * @return A List of entries.
     */
    public static List<EnchantmentLevelEntry> getEnchantmentsAsList(ItemEnchantmentsComponent enchantments) {
        return enchantments.getEnchantmentsMap().stream().map(
                entry -> new EnchantmentLevelEntry(entry.getKey().value(), entry.getIntValue())
        ).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific enchantment entry by its order/position in the item's enchantment list.
     *
     * @param enchantments The enchantment component.
     * @param index The zero-based index of the enchantment.
     * @return The Map.Entry, or null if the index is out of bounds.
     */
    @Nullable
    public static Map.Entry<Enchantment, Integer> getEnchantmentByPosition(ItemEnchantmentsComponent enchantments, int index) {
        if (enchantments == null || index < 0) {
            return null;
        }

        // This is an inefficient way to get by index, but components don't guarantee order.
        // We create a map simply to get an iterator.
        Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.getEnchantmentsMap().stream().collect(Collectors.toMap(
                entry -> entry.getKey().value(),
                entry -> entry.getIntValue()
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
     * @param potentiallyCursedMap A map of enchantments.
     * @return A new map containing only non-cursed enchantments.
     */
    private static Map<Enchantment, Integer> getCurseFreeMap(ItemStack target, Map<Enchantment, Integer> potentiallyCursedMap) {
        Map<Enchantment, Integer> definetlyUncursedMap = Maps.newLinkedHashMap();
        for(Map.Entry<Enchantment, Integer> entry : potentiallyCursedMap.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.isCursed()) {
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
     * @param world The world.
     * @param tablePos The BlockPos of the enchanting table.
     * @param themeKey The RegistryKey of the theme to check for.
     * @return True if a matching themed provider is found, false otherwise.
     */
    public static boolean hasThemedPowerProvider(World world, BlockPos tablePos, RegistryKey<EnchantmentTheme> themeKey) {
        // The DEFAULT theme is always "active" and does not have special providers.
        if (themeKey.equals(EnchantmentThemeRegistry.DEFAULT)) {
            return false;
        }

        // Get DynamicRegistryManager from the world, then get the dynamic registry
        DynamicRegistryManager registryAccess = world.getRegistryManager();
        Registry<EnchantmentTheme> registry = registryAccess.get(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);
        Optional<EnchantmentTheme> themeOpt = registry.getOrEmpty(themeKey);

        if (themeOpt.isEmpty()) {
            return false; // Theme doesn't exist
        }
        EnchantmentTheme theme = themeOpt.get();

        for (BlockPos providerOffset : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            BlockPos transmitterPos = tablePos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);

            // Check if the "transmitter" block (air) is clear
            if (world.getBlockState(transmitterPos).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                BlockPos providerPos = tablePos.add(providerOffset);
                BlockState providerState = world.getBlockState(providerPos);
                RegistryEntry<Block> blockEntry = providerState.getRegistryEntry();

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
     * @param world The world (for checking themed providers).
     * @param pos The pos of the enchanting table (for checking themed providers).
     * @return A list of generated, shuffled enchantment entries.
     */
    public static List<EnchantmentLevelEntry> generateEnchantments(FeatureSet enabledFeatures, Random random, ItemStack target, boolean treasureAllowed, World world, BlockPos pos) {
        List<EnchantmentLevelEntry> enchantments = Lists.newArrayList();
        Item item = target.getItem();

        int enchantability = item.getEnchantability();
        if (enchantability <= 0) {
            return enchantments;
        }

        // Get the list of possible entries, filtered by theme
        List<EnchantmentLevelEntry> possibleEntries = EnchantmentLib.getPossibleEntries(enabledFeatures, target, treasureAllowed, world, pos);

        enchantments.addAll(possibleEntries);

        // Shuffle the final list
        enchantments = EnchantmentLib.weightedSkewedShuffle(enchantments, entry -> entry.getWeight().getValue(), random);

        return enchantments;
    }

    /**
     * Gets a list of all enchantment entries that can possibly be applied to the given item,
     * filtered by the available themed power providers.
     * <p>
     * This method implements the core logic for theme-based enchantment availability.
     *
     * @param enabledFeatures Feature set for the world.
     * @param target The ItemStack.
     * @param treasureAllowed If treasure enchantments are allowed by default.
     * @param world The world (for checking themed providers).
     * @param pos The pos of the enchanting table (for checking themed providers).
     * @return A list of valid enchantment entries (level 1).
     */
    public static List<EnchantmentLevelEntry> getPossibleEntries(FeatureSet enabledFeatures, ItemStack target, boolean treasureAllowed, World world, BlockPos pos) {
        List<EnchantmentLevelEntry> enchantments = Lists.newArrayList();
        boolean isTargetBook = target.isOf(Items.BOOK);

        for (Enchantment enchantment : Registries.ENCHANTMENT) {

            // Get the theme key associated with this enchantment via the Accessor
            RegistryKey<EnchantmentTheme> themeKey = ((EnchantmentThemeAccessor)enchantment).getTheme();
            boolean isThemed = !themeKey.equals(EnchantmentThemeRegistry.DEFAULT);
            boolean themeActive = false;

            if (isThemed) {
                // Check if the enchanting table has power for that theme.
                themeActive = EnchantmentLib.hasThemedPowerProvider(world, pos, themeKey);
                if (!themeActive) {
                    continue; // Skip this enchantment, its theme providers are not present
                }
            }
            // If the theme IS default, it's always available (themeActive remains false).


            // Allow treasure enchantments if:
            // 1. The method parameter allows it (treasureAllowed)
            // 2. OR The enchantment's theme is active (themeActive)
            boolean canApplyTreasure = treasureAllowed || themeActive;

            if (enchantment.isTreasure() && !canApplyTreasure) {
                continue; // It's a treasure enchant, and we're not allowed to apply it.
            }

            // Altered vanilla logic
            if (enchantment.isEnabled(enabledFeatures)
                    && (enchantment.isAvailableForEnchantedBookOffer() || themeActive)
                    && (isTargetBook || enchantment.isAcceptableItem(target))
                    && enchantment.isPrimaryItem(target)
            ) {
                enchantments.add(new EnchantmentLevelEntry(enchantment, 1));
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
    public static <T> List<T> weightedSkewedShuffle(Collection<T> originalList, ToIntFunction<T> weightExtractor, Random random) {

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
     * <p>
     * If the tome already has enchantments, this method does nothing.
     *
     * @param tomeStack The tome ItemStack to enchant. Must be ItemRegistry.ENCHANTED_TOME.
     * @param world The world, used to get enabled features.
     * @param random The random source.
     * @return The enchanted tome ItemStack (or the original stack if it wasn't enchanted).
     */
    public static ItemStack enchantTomeRandomly(ItemStack tomeStack, World world, Random random) {
        // 1. Check if it's the right item
        if (!tomeStack.isOf(ItemRegistry.ENCHANTED_TOME)) {
            return tomeStack;
        }

        // 2. Check if it's already enchanted
        ItemEnchantmentsComponent enchants = tomeStack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return tomeStack;
        }

        // 3. Get all valid, enabled enchantments
        List<RegistryEntry<Enchantment>> enchantmentList = Registries.ENCHANTMENT.streamEntries()
                .filter((entry) -> entry.value().isEnabled(world.getEnabledFeatures()))
                .collect(Collectors.toList());

        if (enchantmentList.isEmpty()) {
            // No enchantments to apply
            return tomeStack;
        }

        // 4. Shuffle the list
        List<RegistryEntry<Enchantment>> mutableList = new ArrayList<>(enchantmentList);
        Util.shuffle(mutableList, random);

        // 5. Get a builder for the STORED_ENCHANTMENTS component
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        // 6. Apply the first TOME_ENCHANTMENT_COUNT enchantments
        int count = 0;
        for (RegistryEntry<Enchantment> entry : mutableList) {
            if (count >= TOME_ENCHANTMENT_COUNT) {
                break;
            }

            Enchantment enchantment = entry.value();

            // 7. Apply custom level logic (Max Level + 1)
            int maxLevel = enchantment.getMaxLevel();
            int newLevel = (maxLevel == 1) ? 1 : maxLevel + 1;

            // 8. Add the enchantment to the builder
            builder.add(entry.value(), newLevel);
            count++;
        }

        // 9. Apply the built component back to the stack
        if (count > 0) {
            tomeStack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        }

        return tomeStack;
    }

    /**
     * Checks if a given tome stack is empty or unenchanted.
     * If it is, it returns a new tome stack with a default enchantment (Protection IV).
     * If the stack is valid and already enchanted, it returns the original stack.
     *
     * @param tomeStack The ItemStack to check.
     * @return An enchanted ItemStack.
     */
    public static ItemStack ensureTomeIsEnchanted(ItemStack tomeStack) {
        // 1. If the stack is null or not a tome, create a new one.
        if (tomeStack == null || !tomeStack.isOf(ItemRegistry.ENCHANTED_TOME)) {
            tomeStack = new ItemStack(ItemRegistry.ENCHANTED_TOME);
        }

        // 2. Check if it's already enchanted
        ItemEnchantmentsComponent enchants = tomeStack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            // 3. It's not enchanted, so apply the default Protection
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            builder.add(Enchantments.PROTECTION, Config.ENCHANTMENT_MAX_LEVEL + 1);
            tomeStack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        }

        // 4. Return the (now guaranteed) enchanted stack
        return tomeStack;
    }

    /**
     * A static helper to get a new Enchanted Tome with the default
     * enchantment (Protection IV).
     * Safe to call from getPickStack.
     *
     * @return A new ItemStack of an Enchanted Tome with Protection IV.
     */
    public static ItemStack getTomeWithDefaultEnchantment() {
        ItemStack tome = new ItemStack(ItemRegistry.ENCHANTED_TOME);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        builder.add(Enchantments.PROTECTION, Config.ENCHANTMENT_MAX_LEVEL + 1);
        tome.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
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