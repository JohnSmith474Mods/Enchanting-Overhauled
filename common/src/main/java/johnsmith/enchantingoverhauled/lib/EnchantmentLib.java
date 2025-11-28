package johnsmith.enchantingoverhauled.lib;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.effect.EnchantmentEffectComponentRegistry;
import johnsmith.enchantingoverhauled.api.enchantment.effect.SilkTouchEffect;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
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
     * Tag for enchantments that should NOT be incremented by Tomes even if the global max level is clamped.
     * E.g. Mending, Infinity, Silk Touch.
     */
    public static final TagKey<Enchantment> SINGLE_LEVEL_ENCHANTMENT = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "single_level_enchantment")
    );

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

        if (descriptionString.isEmpty()) {
            return wrappedLines;
        }

        String remainingString = descriptionString;

        while (remainingString.length() > MAX_TOOLTIP_LINE_LENGTH) {
            String line;
            int wrapAt = remainingString.lastIndexOf(' ', MAX_TOOLTIP_LINE_LENGTH);

            if (wrapAt <= 0) {
                line = remainingString.substring(0, MAX_TOOLTIP_LINE_LENGTH);
                remainingString = remainingString.substring(MAX_TOOLTIP_LINE_LENGTH);
            } else {
                line = remainingString.substring(0, wrapAt);
                remainingString = remainingString.substring(wrapAt + 1);
            }

            wrappedLines.add(TOOLTIP_INDENT.copy().append(Component.literal(line)).setStyle(descriptionStyle).getVisualOrderText());
        }

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
        if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            return DataComponents.STORED_ENCHANTMENTS;
        }
        return DataComponents.ENCHANTMENTS;
    }

    /**
     * Calculates the enchanting power provided by a block at a given position for a specific theme.
     *
     * @param level The level to check.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @param theme The EnchantmentTheme to calculate power for.
     * @return The integer power level (0 or the calculated value).
     */
    public static int getEnchantingPower(Level level, BlockPos providerPos, EnchantmentTheme theme) {
        BlockState state = level.getBlockState(providerPos);
        Holder<Block> blockEntry = state.getBlockHolder();

        for (PowerProvider provider : theme.powerProviders()) {
            if (provider.blocks().contains(blockEntry)) {
                return calculatePower(level, providerPos, state, provider);
            }
        }

        return 0;
    }

    /**
     * Calculates the highest enchanting power provided by a block at a given position,
     * regardless of theme.
     *
     * @param level The level to check.
     * @param providerPos The BlockPos of the potential power-providing block.
     * @return The highest integer power level found across all themes (0 if none).
     */
    public static int getAgnosticEnchantingPower(Level level, BlockPos providerPos) {
        BlockState state = level.getBlockState(providerPos);
        Holder<Block> blockEntry = state.getBlockHolder();
        int maxPower = 0;

        RegistryAccess registryAccess = level.registryAccess();
        Optional<Registry<EnchantmentTheme>> registryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

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
     * Helper method to calculate the total power from a single PowerProvider,
     * including all its data-driven bonuses.
     */
    private static int calculatePower(Level level, BlockPos pos, BlockState state, PowerProvider provider) {
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
                Constants.LOG.warn("Failed to calculate power bonus for {}: {}",
                        BuiltInRegistries.BLOCK.getId(state.getBlock()), e.getMessage());
            }
        }
        return totalPower;
    }

    /**
     * Client-safe check to see if a block *could* provide power for *any* theme.
     */
    public static boolean shouldSpawnParticles(Level level, BlockPos providerPos) {
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

        // Iterate keys which are now Holder<Enchantment>
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

    /**
     * Converts the item's enchantment component into a Map of Enchantment Holder to Level (Integer).
     *
     * @param target The ItemStack to deserialize.
     * @return A map containing active enchantments and their levels.
     */
    public static Map<Holder<Enchantment>, Integer> deserializeEnchantments(ItemStack target) {
        ItemEnchantments component = EnchantmentHelper.getEnchantmentsForCrafting(target);
        return component.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // Key is now Holder<Enchantment>
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
        // EnchantmentInstance now takes Holder<Enchantment> in its constructor
        return enchantments.entrySet().stream().map(
                entry -> new EnchantmentInstance(entry.getKey(), entry.getIntValue())
        ).collect(Collectors.toList());
    }

    /**
     * Retrieves a specific enchantment entry by its order/position in the item's enchantment list.
     *
     * @param enchantments The enchantment component.
     * @param index The zero-based index of the enchantment to retrieve.
     * @return The Map.Entry of the enchantment holder, or null if the index is out of bounds.
     */
    @Nullable
    public static Map.Entry<Holder<Enchantment>, Integer> getEnchantmentByPosition(ItemEnchantments enchantments, int index) {
        if (enchantments == null || index < 0) {
            return null;
        }

        Iterator<Map.Entry<Holder<Enchantment>, Integer>> iterator = enchantments.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                Object2IntMap.Entry::getIntValue
        )).entrySet().iterator();


        for (int i = 0; i < index; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            } else {
                return null;
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
    private static Map<Holder<Enchantment>, Integer> getCurseFreeMap(ItemStack target, Map<Holder<Enchantment>, Integer> potentiallyCursedMap) {
        Map<Holder<Enchantment>, Integer> definetlyUncursedMap = Maps.newLinkedHashMap();
        for(Map.Entry<Holder<Enchantment>, Integer> entry : potentiallyCursedMap.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!enchantment.is(EnchantmentTags.CURSE)) {
                definetlyUncursedMap.put(enchantment, entry.getValue());
            }
        }
        return definetlyUncursedMap;
    }

    /**
     * Checks if an enchanting table has any valid power providers for a *specific theme* nearby.
     */
    public static boolean hasThemedPowerProvider(Level level, BlockPos tablePos, ResourceKey<EnchantmentTheme> themeKey) {
        if (themeKey.equals(EnchantmentThemeRegistry.DEFAULT)) {
            return false;
        }

        RegistryAccess registryAccess = level.registryAccess();
        Optional<Registry<EnchantmentTheme>> registryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (registryOpt.isEmpty()) return false;

        Optional<EnchantmentTheme> themeOpt = registryOpt.get().getOptional(themeKey);

        if (themeOpt.isEmpty()) {
            return false;
        }
        EnchantmentTheme theme = themeOpt.get();

        for (BlockPos providerOffset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            BlockPos transmitterPos = tablePos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);

            if (level.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                BlockPos providerPos = tablePos.offset(providerOffset);
                BlockState providerState = level.getBlockState(providerPos);
                Holder<Block> blockEntry = providerState.getBlockHolder();

                for (PowerProvider provider : theme.powerProviders()) {
                    if (provider.blocks().contains(blockEntry)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Generates a randomized list of possible enchantments for an item based on its enchantability,
     * filtered by available themed power providers.
     */
    public static List<EnchantmentInstance> generateEnchantments(FeatureFlagSet enabledFeatures, RandomSource random, ItemStack target, boolean treasureAllowed, Level level, BlockPos pos) {
        List<EnchantmentInstance> enchantments = Lists.newArrayList();

        int enchantability = 0;
        var enchantableComponent = target.get(DataComponents.ENCHANTABLE);
        if (enchantableComponent != null) {
            enchantability = enchantableComponent.value();
        }

        if (enchantability <= 0) {
            return enchantments;
        }

        List<EnchantmentInstance> possibleEntries = EnchantmentLib.getPossibleEntries(enabledFeatures, target, treasureAllowed, level, pos);

        enchantments.addAll(possibleEntries);

        enchantments = EnchantmentLib.weightedSkewedShuffle(
                enchantments,
                entry -> entry.enchantment().value().getWeight(),
                random
        );

        return enchantments;
    }

    /**
     * Scans the theme registry to find which Theme Tag matches this enchantment.
     * Returns the ResourceKey of the matching theme, or DEFAULT if none found.
     */
    public static ResourceKey<EnchantmentTheme> getThemeKey(RegistryAccess registryAccess, Holder<Enchantment> enchantment) {
        var themeRegistryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);
        if (themeRegistryOpt.isEmpty()) return EnchantmentThemeRegistry.DEFAULT;

        // Iterate all registered Themes
        // FIX: .holders() -> .listElements()
        for (var themeRef : themeRegistryOpt.get().listElements().toList()) {
            ResourceLocation id = themeRef.key().location();
            // Construct the expected Tag: e.g., #enchanting_overhauled:theme/marine
            TagKey<Enchantment> tag = TagKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "theme/" + id.getPath())
            );

            // Check if the enchantment has this tag
            if (enchantment.is(tag)) {
                return themeRef.key();
            }
        }
        return EnchantmentThemeRegistry.DEFAULT;
    }

    /**
     * Gets a list of all enchantment entries that can possibly be applied to the given item,
     * filtered by the available themed power providers.
     */
    public static List<EnchantmentInstance> getPossibleEntries(FeatureFlagSet enabledFeatures, ItemStack target, boolean treasureAllowed, Level level, BlockPos pos) {
        List<EnchantmentInstance> enchantments = Lists.newArrayList();
        boolean isTargetBook = target.is(Items.BOOK);

        // Use the registry from the level to iterate holders
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        for (Holder<Enchantment> enchantmentHolder : registry.listElements().toList()) {
            Enchantment enchantment = enchantmentHolder.value();

            // UPDATED: Use the new helper instead of the accessor
            ResourceKey<EnchantmentTheme> themeKey = getThemeKey(level.registryAccess(), enchantmentHolder);

            boolean isThemed = !themeKey.equals(EnchantmentThemeRegistry.DEFAULT);
            boolean themeActive = false;

            if (isThemed) {
                themeActive = EnchantmentLib.hasThemedPowerProvider(level, pos, themeKey);
                if (!themeActive) {
                    continue;
                }
            }

            boolean canApplyTreasure = treasureAllowed || themeActive;

            if (enchantmentHolder.is(EnchantmentTags.TREASURE) && !canApplyTreasure) {
                continue;
            }

            if ((enchantmentHolder.is(EnchantmentTags.IN_ENCHANTING_TABLE) || themeActive)
                    && (isTargetBook || enchantment.canEnchant(target))
            ) {
                enchantments.add(new EnchantmentInstance(enchantmentHolder, 1));
            }
        }

        return enchantments;
    }

    public static <T> List<T> weightedSkewedShuffle(Collection<T> originalList, ToIntFunction<T> weightExtractor, RandomSource random) {
        List<WeightedSortItem<T>> weightedList = new ArrayList<>(originalList.size());

        for (T item : originalList) {
            double weight = (double)weightExtractor.applyAsInt(item);

            if (weight <= 0) {
                weightedList.add(new WeightedSortItem<>(item, Double.NEGATIVE_INFINITY));
                continue;
            }

            double randomFactor = 1.0 - random.nextDouble();
            double sortKey = weight / randomFactor;

            weightedList.add(new WeightedSortItem<>(item, sortKey));
        }

        Collections.sort(weightedList);

        return weightedList.stream()
                .map(weightedItem -> weightedItem.item)
                .collect(Collectors.toList());
    }

    private static class WeightedSortItem<T> implements Comparable<WeightedSortItem<T>> {
        final T item;
        final double sortKey;

        WeightedSortItem(T item, double sortKey) {
            this.item = item;
            this.sortKey = sortKey;
        }

        @Override
        public int compareTo(WeightedSortItem<T> other) {
            return Double.compare(other.sortKey, this.sortKey);
        }
    }

    /**
     * Enchants a given Enchanted Tome ItemStack with 3 random, high-level enchantments.
     */
    public static ItemStack enchantTomeRandomly(ItemStack tomeStack, Level world, RandomSource random) {
        if (!tomeStack.is(Services.PLATFORM.getEnchantedTome())) {
            return tomeStack;
        }

        ItemEnchantments enchants = tomeStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return tomeStack;
        }

        // Retrieve holders from the world registry
        List<Holder<Enchantment>> enchantmentList = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .listElements()
                .map(h -> (Holder<Enchantment>) h)
                .collect(Collectors.toList());

        if (enchantmentList.isEmpty()) {
            return tomeStack;
        }

        return applyTomeEnchantments(tomeStack, enchantmentList, random);
    }

    /**
     * Applies the "Tome Logic" to a specific list of candidates.
     *
     * @param candidates The list of possible enchantments (as Holders).
     */
    public static ItemStack applyTomeEnchantments(ItemStack tomeStack, List<Holder<Enchantment>> candidates, RandomSource random) {
        List<Holder<Enchantment>> mutableList = new ArrayList<>(candidates);
        Util.shuffle(mutableList, random);

        ItemEnchantments current = tomeStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(current);

        int count = 0;
        for (Holder<Enchantment> entry : mutableList) {
            if (count >= TOME_ENCHANTMENT_COUNT) {
                break;
            }

            int maxLevel = entry.value().getMaxLevel();
            int newLevel = maxLevel;

            if (Config.BINARY_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) {
                boolean isInherentlySingleLevel = entry.is(SINGLE_LEVEL_ENCHANTMENT);
                boolean globalLimitIsOne = Config.BOUNDED_ENCHANTMENT_MAX_LEVEL.get() == 1;

                if (maxLevel > 1 || (maxLevel == 1 && globalLimitIsOne && !isInherentlySingleLevel)) {
                    newLevel = maxLevel + 1;
                }
            }

            // Pass the holder to the builder
            builder.set(entry, newLevel);
            count++;
        }

        if (count > 0) {
            tomeStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        } else {
            Constants.LOG.warn("Couldn't find any compatible enchantments for {}", tomeStack);
        }

        return tomeStack;
    }

    /**
     * Checks if a given tome stack is empty or unenchanted.
     * <p>
     * NOTE: Requires RegistryAccess to lookup default enchantments (Protection).
     *
     * @param registryAccess Access to the registry to find Protection.
     * @param tomeStack The ItemStack to check.
     */
    public static ItemStack ensureTomeIsEnchanted(RegistryAccess registryAccess, ItemStack tomeStack) {
        if (tomeStack == null || !tomeStack.is(Services.PLATFORM.getEnchantedTome())) {
            tomeStack = new ItemStack(Services.PLATFORM.getEnchantedTome());
        }

        ItemEnchantments enchants = tomeStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

            // Resolve the Protection enchantment holder from the registry
            Registry<Enchantment> registry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            Holder<Enchantment> protection = registry.getOrThrow(Enchantments.PROTECTION);

            builder.set(protection, Config.BOUNDED_ENCHANTMENT_MAX_LEVEL.get() + 1);
            tomeStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        }

        return tomeStack;
    }

    /**
     * A static helper to get a new Enchanted Tome with the default enchantment.
     * <p>
     * NOTE: Requires RegistryAccess.
     */
    public static ItemStack getTomeWithDefaultEnchantment(RegistryAccess registryAccess) {
        ItemStack tome = new ItemStack(Services.PLATFORM.getEnchantedTome());
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        Registry<Enchantment> registry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> protection = registry.getOrThrow(Enchantments.PROTECTION);

        builder.set(protection, Config.BOUNDED_ENCHANTMENT_MAX_LEVEL.get() + 1);
        tome.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        return tome;
    }

    /**
     * Converts an integer to its Roman numeral representation.
     */
    public static String toRoman(int number) {
        if (number < 1 || number > 3999) {
            return String.valueOf(number);
        }

        StringBuilder sb = new StringBuilder();
        int remaining = number;

        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (remaining >= ROMAN_VALUES[i]) {
                sb.append(ROMAN_SYMBOLS[i]);
                remaining -= ROMAN_VALUES[i];
            }
        }

        return sb.toString();
    }

    /**
     * Scans the tool for any enchantment that provides the custom Silk Touch effect.
     * Returns the effect instance and the level of the enchantment providing it.
     */
    public static Optional<Map.Entry<SilkTouchEffect, Integer>> getSilkTouchEffect(ItemStack stack, net.minecraft.core.RegistryAccess registryAccess) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) return Optional.empty();

        Registry<Enchantment> registry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int level = entry.getIntValue();

            // Check if this enchantment has our custom component
            SilkTouchEffect effect = holder.value().effects().get(EnchantmentEffectComponentRegistry.SILK_TOUCH);
            if (effect != null) {
                return Optional.of(Map.entry(effect, level));
            }
        }
        return Optional.empty();
    }
}