package johnsmith.enchantingoverhauled.item;

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
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A custom item representing an Enchanted Page (a fleeting enchantment source).
 * Supports dynamic {@code "<Theme> <Item Name> of <School>"} attribution and
 * naming.
 */
public class EnchantedPageItem extends EnchantedBookItem {

    /**
     * Constructs a new EnchantedPageItem.
     *
     * @param properties The item properties (stack size, rarity, etc.).
     */
    public EnchantedPageItem(Properties properties) {
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
     * Resolves the dominant {@link EnchantmentTheme} of the given Enchanted Page
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
     * Resolves the dominant {@link MagicSchool} of the given Enchanted Page stack.
     */
    public static Optional<ResourceKey<MagicSchool>> getSchool(ItemStack stack,
                                                               @Nullable RegistryAccess registryAccess) {
        return ArcaneItemNamingUtil.resolveSchool(stack, registryAccess);
    }

    public static Optional<ResourceKey<MagicSchool>> getSchool(ItemStack stack) {
        return ArcaneItemNamingUtil.resolveSchool(stack, null);
    }

    /**
     * Enchants a given Enchanted Page ItemStack with 1 enchantment at level 1,
     * prioritizing
     * enchantments matching the specified {@link EnchantmentTheme} and/or
     * {@link MagicSchool}.
     */
    public static ItemStack enchantWithThemeAndSchool(
            ItemStack itemStack,
            RegistryAccess registryAccess,
            RandomSource random,
            @Nullable ResourceKey<EnchantmentTheme> themeKey,
            @Nullable ResourceKey<MagicSchool> schoolKey) {
        if (!itemStack.is(Items.ENCHANTED_PAGE.get())) {
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

        for (Holder<Enchantment> holder : allHolders) {
            boolean themeMatch = hasSpecificTheme && holder.is(EnchantmentThemes.getThemeTag(themeKey));
            boolean schoolMatch = hasSpecificSchool && holder.is(MagicSchools.getSchoolTag(schoolKey));

            if (themeMatch && schoolMatch) {
                bothMatches.add(holder);
            } else if (themeMatch || schoolMatch) {
                primaryMatches.add(holder);
            }
        }

        Holder<Enchantment> chosen;
        if (!bothMatches.isEmpty()) {
            chosen = bothMatches.get(random.nextInt(bothMatches.size()));
        } else if (!primaryMatches.isEmpty()) {
            chosen = primaryMatches.get(random.nextInt(primaryMatches.size()));
        } else {
            chosen = allHolders.get(random.nextInt(allHolders.size()));
        }

        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        builder.set(chosen, chosen.value().getMinLevel());
        itemStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        return itemStack;
    }

    /**
     * Enchants a given Enchanted Page ItemStack using biome-specific trades.
     * Prefers enchantments from the COMMON tag before falling back to the SPECIAL
     * tag.
     * Restricts the page to 1 enchantment strictly at level 1.
     */
    public static ItemStack enchantRandomly(ItemStack itemStack, Level world, RandomSource random,
                                            TagKey<Enchantment> specialTag, TagKey<Enchantment> commonTag) {
        if (!itemStack.is(Items.ENCHANTED_PAGE.get())) {
            return itemStack;
        }
        ItemEnchantments enchants = itemStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            return itemStack;
        }

        Registry<Enchantment> registry = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        List<Holder<Enchantment>> commonEnchants = registry.getOrCreateTag(commonTag).stream()
                .collect(Collectors.toList());
        List<Holder<Enchantment>> specialEnchants = registry.getOrCreateTag(specialTag).stream()
                .collect(Collectors.toList());

        Util.shuffle(commonEnchants, random);
        Util.shuffle(specialEnchants, random);

        Holder<Enchantment> selected = null;

        if (!commonEnchants.isEmpty()) {
            selected = commonEnchants.get(0);
        } else if (!specialEnchants.isEmpty()) {
            selected = specialEnchants.get(0);
        }

        if (selected == null) {
            List<Holder<Enchantment>> fallbackEnchants = registry.getOrCreateTag(EnchantmentTags.IN_ENCHANTING_TABLE)
                    .stream().collect(Collectors.toList());
            if (fallbackEnchants.isEmpty()) {
                Constants.LOG.error("Critical failure: No enchantments found in fallback tag for {}", itemStack);
                return itemStack;
            }
            Util.shuffle(fallbackEnchants, random);
            selected = fallbackEnchants.get(0);
        }

        ItemEnchantments current = itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(current);
        builder.set(selected, 1);

        itemStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        return itemStack;
    }
}