package johnsmith.enchantingoverhauled.item;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A custom item representing an Enchanted Tome.
 * <p>
 * The Enchanted Tome is a special variant of an enchanted book used to activate
 * {@link johnsmith.enchantingoverhauled.block.DeactivatedEnchantingTableBlock}s.
 * It extends {@link EnchantedBookItem} to inherit standard enchantment storage
 * behaviors
 * and supports dynamic {@code "<Theme> <Item Name> of <School>"} attribution
 * and naming.
 */
public class EnchantedTomeItem extends EnchantedBookItem {

    /**
     * Constructs a new EnchantedTomeItem.
     *
     * @param properties The item properties (stack size, rarity, etc.).
     */
    public EnchantedTomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        Component baseName = Component.translatable(this.getDescriptionId(stack));
        return ArcaneItemNamingUtil.formatItemName(stack, baseName);
    }

    /**
     * Resolves the dominant {@link EnchantmentTheme} of the given Enchanted Tome
     * stack.
     */
    public static Optional<ResourceKey<EnchantmentTheme>> getTheme(ItemStack stack,
                                                                   @Nullable RegistryAccess registryAccess) {
        return ArcaneItemNamingUtil.resolveTheme(stack, registryAccess);
    }

    public static Optional<ResourceKey<EnchantmentTheme>> getTheme(ItemStack stack) {
        return ArcaneItemNamingUtil.resolveTheme(stack, null);
    }

    /**
     * Resolves the dominant {@link MagicSchool} of the given Enchanted Tome stack.
     */
    public static Optional<ResourceKey<MagicSchool>> getSchool(ItemStack stack,
                                                               @Nullable RegistryAccess registryAccess) {
        return ArcaneItemNamingUtil.resolveSchool(stack, registryAccess);
    }

    public static Optional<ResourceKey<MagicSchool>> getSchool(ItemStack stack) {
        return ArcaneItemNamingUtil.resolveSchool(stack, null);
    }

    /**
     * Enchants a given Enchanted Tome ItemStack prioritizing enchantments that
     * match the specified
     * {@link EnchantmentTheme} and/or {@link MagicSchool}.
     * <p>
     * Follows the Wildcard Rule: if {@code themeKey} is common or
     * {@code schoolKey} is Aberration,
     * it rolls across all eligible enchantments and adopts the dominant Theme and
     * School of the generated Tome.
     */
    public static ItemStack enchantWithThemeAndSchool(
            ItemStack itemStack,
            RegistryAccess registryAccess,
            RandomSource random,
            @Nullable ResourceKey<EnchantmentTheme> themeKey,
            @Nullable ResourceKey<MagicSchool> schoolKey) {
        if (!itemStack.is(Items.ENCHANTED_TOME.get())) {
            return itemStack;
        }

        Registry<Enchantment> registry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> allHolders = registry.holders()
                .map(h -> (Holder<Enchantment>) h)
                .collect(Collectors.toList());

        if (allHolders.isEmpty()) {
            return itemStack;
        }

        boolean hasSpecificTheme = !EnchantmentThemes.isCommon(themeKey);
        boolean hasSpecificSchool = !MagicSchools.isAberration(schoolKey);

        List<Holder<Enchantment>> bothMatches = new ArrayList<>();
        List<Holder<Enchantment>> primaryMatches = new ArrayList<>();
        List<Holder<Enchantment>> fallbackMatches = new ArrayList<>();

        for (Holder<Enchantment> holder : allHolders) {
            boolean themeMatch = hasSpecificTheme && holder.is(EnchantmentThemes.getThemeTag(themeKey));
            boolean schoolMatch = hasSpecificSchool && holder.is(MagicSchools.getSchoolTag(schoolKey));

            if (themeMatch && schoolMatch) {
                bothMatches.add(holder);
            } else if (themeMatch || schoolMatch) {
                primaryMatches.add(holder);
            } else {
                fallbackMatches.add(holder);
            }
        }

        Util.shuffle(bothMatches, random);
        Util.shuffle(primaryMatches, random);
        Util.shuffle(fallbackMatches, random);

        List<Holder<Enchantment>> orderedCandidates = new ArrayList<>(allHolders.size());
        orderedCandidates.addAll(bothMatches);
        orderedCandidates.addAll(primaryMatches);
        orderedCandidates.addAll(fallbackMatches);

        int maxEnchants = Math.min(Config.TOME_ENCHANTMENT_AMOUNT.get(), Config.MAX_ENCHANTMENT_AMOUNT.get());
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        List<Holder<Enchantment>> chosenList = new ArrayList<>();

        for (Holder<Enchantment> candidate : orderedCandidates) {
            if (chosenList.size() >= maxEnchants) {
                break;
            }
            boolean compatible = true;
            for (Holder<Enchantment> existing : chosenList) {
                if (existing.equals(candidate) || !Enchantment.areCompatible(existing, candidate)) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible) {
                continue;
            }

            int maxLevel = candidate.value().getMaxLevel();
            int newLevel = maxLevel;
            if (Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) {
                boolean globalLimitIsOne = Config.ENCHANTMENT_MAX_LEVEL.get() == 1;
                if (maxLevel > 1 || (maxLevel == 1 && globalLimitIsOne)) {
                    newLevel = maxLevel + 1;
                }
            }
            builder.set(candidate, newLevel);
            chosenList.add(candidate);
        }

        if (!chosenList.isEmpty()) {
            itemStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        }

        return itemStack;
    }

    /**
     * Enchants a given Enchanted Tome ItemStack using biome-specific trades.
     * Prefers enchantments from the SPECIAL tag before falling back to the COMMON
     * tag.
     */
    public static ItemStack enchantRandomly(ItemStack itemStack, Level world, RandomSource random,
                                            TagKey<Enchantment> specialTag, TagKey<Enchantment> commonTag) {
        if (!itemStack.is(Items.ENCHANTED_TOME.get())) {
            return itemStack;
        }
        ItemEnchantments enchants = itemStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return itemStack;
        }

        Registry<Enchantment> registry = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        List<Holder<Enchantment>> specialEnchants = registry.getOrCreateTag(specialTag).stream()
                .collect(Collectors.toList());
        List<Holder<Enchantment>> commonEnchants = registry.getOrCreateTag(commonTag).stream()
                .collect(Collectors.toList());

        Util.shuffle(specialEnchants, random);
        Util.shuffle(commonEnchants, random);

        List<Holder<Enchantment>> selected = new ArrayList<>();
        int maxEnchants = Math.min(Config.TOME_ENCHANTMENT_AMOUNT.get(), Config.MAX_ENCHANTMENT_AMOUNT.get());

        for (Holder<Enchantment> ench : specialEnchants) {
            if (selected.size() < maxEnchants) {
                selected.add(ench);
            }
        }
        for (Holder<Enchantment> ench : commonEnchants) {
            if (selected.size() < maxEnchants) {
                selected.add(ench);
            }
        }

        if (selected.isEmpty()) {
            List<Holder<Enchantment>> fallbackEnchants = registry.getOrCreateTag(EnchantmentTags.IN_ENCHANTING_TABLE)
                    .stream().collect(Collectors.toList());
            if (fallbackEnchants.isEmpty()) {
                Constants.LOG.error("Critical failure: No enchantments found in fallback tag for {}", itemStack);
                return itemStack;
            }
            Util.shuffle(fallbackEnchants, random);
            for (Holder<Enchantment> ench : fallbackEnchants) {
                if (selected.size() < maxEnchants) {
                    selected.add(ench);
                }
            }
        }

        ItemEnchantments current = itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(current);

        for (Holder<Enchantment> entry : selected) {
            int maxLevel = entry.value().getMaxLevel();
            int newLevel = maxLevel;

            if (Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) {
                boolean globalLimitIsOne = Config.ENCHANTMENT_MAX_LEVEL.get() == 1;
                if (maxLevel > 1 || (maxLevel == 1 && globalLimitIsOne)) {
                    newLevel = maxLevel + 1;
                }
            }
            builder.set(entry, newLevel);
        }

        itemStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        return itemStack;
    }

    /**
     * Enchants a given Enchanted Tome ItemStack with random, high-level
     * enchantments.
     */
    public static ItemStack enchantRandomly(ItemStack itemStack, Level world, RandomSource random) {
        if (!itemStack.is(Items.ENCHANTED_TOME.get())) {
            return itemStack;
        }
        ItemEnchantments enchants = itemStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return itemStack;
        }

        List<Holder<Enchantment>> enchantmentList = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .holders()
                .map(h -> (Holder<Enchantment>) h)
                .collect(Collectors.toList());

        if (enchantmentList.isEmpty()) {
            return itemStack;
        }

        return applyTomeEnchantments(itemStack, enchantmentList, random);
    }

    /**
     * Applies the "Tome Logic" to a specific list of candidates.
     *
     * @param candidates The list of possible enchantments (as Holders).
     */
    public static ItemStack applyTomeEnchantments(ItemStack itemStack, List<Holder<Enchantment>> candidates,
                                                  RandomSource random) {
        List<Holder<Enchantment>> mutableList = new ArrayList<>(candidates);
        Util.shuffle(mutableList, random);

        ItemEnchantments current = itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(current);

        int count = 0;

        for (Holder<Enchantment> entry : mutableList) {
            if (count >= Math.min(Config.TOME_ENCHANTMENT_AMOUNT.get(), Config.MAX_ENCHANTMENT_AMOUNT.get())) {
                break;
            }
            int maxLevel = entry.value().getMaxLevel();
            int newLevel = maxLevel;

            if (Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) {
                boolean globalLimitIsOne = Config.ENCHANTMENT_MAX_LEVEL.get() == 1;
                if (maxLevel > 1 || (maxLevel == 1 && globalLimitIsOne)) {
                    newLevel = maxLevel + 1;
                }
            }

            builder.set(entry, newLevel);
            count++;
        }

        if (count > 0) {
            itemStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        } else {
            Constants.LOG.warn("Couldn't find any compatible enchantments for {}", itemStack);
        }

        return itemStack;
    }

    /**
     * A static helper to get a new Enchanted Tome with the default enchantment.
     */
    public static ItemStack withDefaultEnchantment(RegistryAccess registryAccess) {
        ItemStack tome = new ItemStack(Items.ENCHANTED_TOME.get());
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        Registry<Enchantment> registry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> protection = registry.getHolderOrThrow(Enchantments.PROTECTION);
        builder.set(protection, protection.value().getMaxLevel() + 1);
        tome.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        return tome;
    }
}