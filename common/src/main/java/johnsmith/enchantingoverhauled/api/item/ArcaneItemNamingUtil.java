package johnsmith.enchantingoverhauled.api.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class responsible for resolving and formatting dynamic arcane item and entity names
 * based on their dominant {@link EnchantmentTheme} and {@link MagicSchool}.
 * <p>
 * Supports both explicit attribution (persisted via {@link DataComponents#CUSTOM_DATA} NBT keys)
 * and automatic weighted derivation from an item's stored or applied enchantments, producing
 * localized names such as {@code "<Theme> <Item Name> of <School>"} or {@code "Aberrant <Item Name>"}.
 */
public final class ArcaneItemNamingUtil {

    /**
     * The custom NBT key used within {@link DataComponents#CUSTOM_DATA} to store an item's
     * explicit {@link EnchantmentTheme} resource location.
     */
    public static final String NBT_THEME_KEY = Constants.MOD_ID + ":theme";

    /**
     * The custom NBT key used within {@link DataComponents#CUSTOM_DATA} to store an item's
     * explicit {@link MagicSchool} resource location.
     */
    public static final String NBT_SCHOOL_KEY = Constants.MOD_ID + ":school";

    /**
     * Weak reference to the active server-side {@link RegistryAccess}, used as a fallback
     * when formatting item names outside of a client level context.
     */
    private static volatile WeakReference<RegistryAccess> serverRegistryAccessRef = new WeakReference<>(null);

    /**
     * Supplier providing the active client-side {@link RegistryAccess} when running on the logical client.
     */
    private static volatile Supplier<RegistryAccess> clientRegistryAccessSupplier = () -> null;

    /**
     * Fallback RGB color map for built-in enchantment themes when dynamic registry lookup is unavailable.
     */
    private static final Map<String, Integer> FALLBACK_THEME_COLORS = Map.ofEntries(
            Map.entry("alpine", 65535),
            Map.entry("arcane", 16711935),
            Map.entry("arthropod", 12829635),
            Map.entry("force", 6319233),
            Map.entry("fortune", 16245878),
            Map.entry("infernal", 16727808),
            Map.entry("kinetic", 16355922),
            Map.entry("marine", 7206849),
            Map.entry("sculk", 6584170),
            Map.entry("soul", 5756390),
            Map.entry("trial", 8091250)
    );

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ArcaneItemNamingUtil() {}

    /**
     * Updates the weak reference to the server's active {@link RegistryAccess}.
     *
     * @param registryAccess The server's {@link RegistryAccess}, or {@code null} to clear.
     */
    public static void setServerRegistryAccess(@Nullable RegistryAccess registryAccess) {
        serverRegistryAccessRef = new WeakReference<>(registryAccess);
    }

    /**
     * Sets the supplier used to retrieve the client's active {@link RegistryAccess}.
     *
     * @param supplier A non-null supplier returning the client's {@link RegistryAccess} or {@code null}.
     */
    public static void setClientRegistryAccessSupplier(Supplier<RegistryAccess> supplier) {
        if (supplier != null) {
            clientRegistryAccessSupplier = supplier;
        }
    }

    /**
     * Retrieves the currently active {@link RegistryAccess}, preferring the logical client's
     * registry access if available and falling back to the server's registry access.
     *
     * @return The active {@link RegistryAccess}, or {@code null} if neither is available.
     */
    @Nullable
    public static RegistryAccess getActiveRegistryAccess() {
        RegistryAccess clientAccess = clientRegistryAccessSupplier.get();
        if (clientAccess != null) {
            return clientAccess;
        }
        return serverRegistryAccessRef.get();
    }

    /**
     * Writes explicit {@link EnchantmentTheme} and {@link MagicSchool} attribution keys into
     * the given {@link ItemStack}'s {@link DataComponents#CUSTOM_DATA} component.
     * <p>
     * Passing {@code null} for either key removes that specific attribution tag from the item stack.
     *
     * @param stack     The {@link ItemStack} to modify.
     * @param themeKey  The explicit {@link EnchantmentTheme} key to assign, or {@code null} to remove.
     * @param schoolKey The explicit {@link MagicSchool} key to assign, or {@code null} to remove.
     */
    public static void setExplicitAttribution(
            ItemStack stack,
            @Nullable ResourceKey<EnchantmentTheme> themeKey,
            @Nullable ResourceKey<MagicSchool> schoolKey
    ) {
        if (stack.isEmpty()) return;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (themeKey != null) {
            tag.putString(NBT_THEME_KEY, themeKey.location().toString());
        } else {
            tag.remove(NBT_THEME_KEY);
        }
        if (schoolKey != null) {
            tag.putString(NBT_SCHOOL_KEY, schoolKey.location().toString());
        } else {
            tag.remove(NBT_SCHOOL_KEY);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Resolves the dominant {@link EnchantmentTheme} for the given {@link ItemStack}.
     * <p>
     * First checks for an explicit non-common theme stored in {@link DataComponents#CUSTOM_DATA}.
     * If none is found (or if the item has enchantments that override a default common tag),
     * it tallies the enchantment levels per theme across the item's enchantments and returns
     * the non-common theme with the highest cumulative level weight, falling back to
     * {@link EnchantmentThemes#COMMON}.
     *
     * @param stack          The {@link ItemStack} to inspect.
     * @param registryAccess The {@link RegistryAccess} used to look up theme tags, or {@code null} to use the active fallback.
     * @return An {@link Optional} containing the resolved {@link EnchantmentTheme} key, or empty if unenchanted and untagged.
     */
    public static Optional<ResourceKey<EnchantmentTheme>> resolveTheme(ItemStack stack, @Nullable RegistryAccess registryAccess) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        RegistryAccess effectiveAccess = registryAccess != null ? registryAccess : getActiveRegistryAccess();
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            enchantments = EnchantmentLib.getEnchantments(stack);
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains(NBT_THEME_KEY)) {
            ResourceLocation id = ResourceLocation.tryParse(customData.copyTag().getString(NBT_THEME_KEY));
            if (id != null) {
                ResourceKey<EnchantmentTheme> explicitKey = ResourceKey.create(EnchantmentThemes.THEME_REGISTRY_KEY, id);
                if (!EnchantmentThemes.isCommon(explicitKey) || enchantments.isEmpty()) {
                    return Optional.of(explicitKey);
                }
            }
        }

        if (enchantments.isEmpty()) {
            return Optional.empty();
        }

        Map<ResourceKey<EnchantmentTheme>, Integer> specialWeights = new LinkedHashMap<>();
        int commonWeight = 0;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> enchHolder = entry.getKey();
            int level = Math.max(1, entry.getIntValue());
            ResourceKey<EnchantmentTheme> themeKey = EnchantmentThemes.getThemeKey(effectiveAccess, enchHolder);
            if (EnchantmentThemes.isCommon(themeKey)) {
                commonWeight += level;
            } else {
                specialWeights.merge(themeKey, level, Integer::sum);
            }
        }

        if (!specialWeights.isEmpty()) {
            return specialWeights.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
        }

        if (commonWeight > 0) {
            return Optional.of(EnchantmentThemes.COMMON);
        }

        return Optional.empty();
    }

    /**
     * Resolves the dominant {@link MagicSchool} for the given {@link ItemStack}.
     * <p>
     * First checks for an explicit non-aberration school stored in {@link DataComponents#CUSTOM_DATA}.
     * Otherwise, tallies the enchantment levels per school across the item's enchantments and returns
     * the standard school with the highest cumulative level weight, falling back to
     * {@link MagicSchools#ABERRATION}.
     *
     * @param stack          The {@link ItemStack} to inspect.
     * @param registryAccess The {@link RegistryAccess} used to look up school tags, or {@code null} to use the active fallback.
     * @return An {@link Optional} containing the resolved {@link MagicSchool} key, or empty if unenchanted and untagged.
     */
    public static Optional<ResourceKey<MagicSchool>> resolveSchool(ItemStack stack, @Nullable RegistryAccess registryAccess) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        RegistryAccess effectiveAccess = registryAccess != null ? registryAccess : getActiveRegistryAccess();
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            enchantments = EnchantmentLib.getEnchantments(stack);
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains(NBT_SCHOOL_KEY)) {
            ResourceLocation id = ResourceLocation.tryParse(customData.copyTag().getString(NBT_SCHOOL_KEY));
            if (id != null) {
                ResourceKey<MagicSchool> explicitKey = ResourceKey.create(MagicSchools.REGISTRY_KEY, id);
                if (!MagicSchools.isAberration(explicitKey) || enchantments.isEmpty()) {
                    return Optional.of(explicitKey);
                }
            }
        }

        if (enchantments.isEmpty()) {
            return Optional.empty();
        }

        Map<ResourceKey<MagicSchool>, Integer> standardSchoolWeights = new LinkedHashMap<>();
        int aberrationWeight = 0;

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> enchHolder = entry.getKey();
            int level = Math.max(1, entry.getIntValue());
            ResourceKey<MagicSchool> schoolKey = MagicSchools.getSchoolKey(effectiveAccess, enchHolder);
            if (MagicSchools.isAberration(schoolKey)) {
                aberrationWeight += level;
            } else {
                standardSchoolWeights.merge(schoolKey, level, Integer::sum);
            }
        }

        if (!standardSchoolWeights.isEmpty()) {
            return standardSchoolWeights.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
        }

        if (aberrationWeight > 0) {
            return Optional.of(MagicSchools.ABERRATION);
        }

        return Optional.empty();
    }

    /**
     * Formats the display name of an arcane item (such as an Enchanted Book, Enchanted Page, or Enchanted Tome)
     * or entity using its resolved {@link EnchantmentTheme} prefix and {@link MagicSchool} suffix.
     * <p>
     * Depending on whether the resolved theme is special (non-common) and whether the resolved school
     * is standard or aberrant, formats the name using one of four translation templates:
     * <ul>
     *   <li>{@code item.enchanting_overhauled.named.themed_school} ({@code "<Theme> <Base Name> of <School>"})</li>
     *   <li>{@code item.enchanting_overhauled.named.school_only} ({@code "<Base Name> of <School>"})</li>
     *   <li>{@code item.enchanting_overhauled.named.aberrant_themed} ({@code "Aberrant <Theme> <Base Name>"})</li>
     *   <li>{@code item.enchanting_overhauled.named.aberrant_only} ({@code "Aberrant <Base Name>"})</li>
     * </ul>
     *
     * @param stack        The {@link ItemStack} whose enchantments and attribution determine the name.
     * @param baseItemName The unformatted base {@link Component} name of the item or entity.
     * @return The formatted {@link Component} with colored theme and school segments, or {@code baseItemName} if unattributed.
     */
    public static Component formatItemName(ItemStack stack, Component baseItemName) {
        if (stack == null || stack.isEmpty()) {
            return baseItemName;
        }

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        boolean hasExplicit = customData != null && (customData.contains(NBT_THEME_KEY) || customData.contains(NBT_SCHOOL_KEY));
        if (stored.isEmpty() && !hasExplicit) {
            return baseItemName;
        }

        RegistryAccess registryAccess = getActiveRegistryAccess();
        Optional<ResourceKey<EnchantmentTheme>> themeKeyOpt = resolveTheme(stack, registryAccess);
        Optional<ResourceKey<MagicSchool>> schoolKeyOpt = resolveSchool(stack, registryAccess);

        if (themeKeyOpt.isEmpty() && schoolKeyOpt.isEmpty()) {
            return baseItemName;
        }

        ResourceKey<EnchantmentTheme> themeKey = themeKeyOpt.orElse(EnchantmentThemes.COMMON);
        ResourceKey<MagicSchool> schoolKey = schoolKeyOpt.orElse(MagicSchools.ABERRATION);

        boolean hasSpecialTheme = !EnchantmentThemes.isCommon(themeKey);
        boolean isAberrant = MagicSchools.isAberration(schoolKey);

        if (isAberrant) {
            int aberrantColor = MagicSchools.getSchool(registryAccess, MagicSchools.ABERRATION).getColor() & 0xFFFFFF;
            MutableComponent aberrantPrefix = Component.translatable("magic_school.enchanting_overhauled.aberrant_prefix")
                    .withStyle(Style.EMPTY.withColor(aberrantColor));

            if (hasSpecialTheme) {
                MutableComponent themeComp = getFormattedThemeComponent(themeKey, registryAccess);
                return Component.translatable(
                        "item.enchanting_overhauled.named.aberrant_themed",
                        aberrantPrefix,
                        themeComp,
                        baseItemName.copy()
                );
            } else {
                return Component.translatable(
                        "item.enchanting_overhauled.named.aberrant_only",
                        aberrantPrefix,
                        baseItemName.copy()
                );
            }
        } else {
            MutableComponent schoolComp = getFormattedSchoolComponent(schoolKey, registryAccess);
            if (hasSpecialTheme) {
                MutableComponent themeComp = getFormattedThemeComponent(themeKey, registryAccess);
                return Component.translatable(
                        "item.enchanting_overhauled.named.themed_school",
                        themeComp,
                        baseItemName.copy(),
                        schoolComp
                );
            } else {
                return Component.translatable(
                        "item.enchanting_overhauled.named.school_only",
                        baseItemName.copy(),
                        schoolComp
                );
            }
        }
    }

    /**
     * Builds a localized, color-styled {@link MutableComponent} representing the given {@link EnchantmentTheme}.
     *
     * @param themeKey       The {@link ResourceKey} of the theme to format.
     * @param registryAccess The {@link RegistryAccess} to query for theme metadata and color, or {@code null}.
     * @return A styled {@link MutableComponent} for the theme name.
     */
    public static MutableComponent getFormattedThemeComponent(ResourceKey<EnchantmentTheme> themeKey, @Nullable RegistryAccess registryAccess) {
        ResourceLocation id = themeKey.location();
        String translationKey = "enchantment_theme." + id.getNamespace() + "." + id.getPath();
        int color = FALLBACK_THEME_COLORS.getOrDefault(id.getPath(), 0xFFFFFF);
        MutableComponent label = null;

        if (registryAccess != null) {
            Optional<Registry<EnchantmentTheme>> regOpt = registryAccess.registry(EnchantmentThemes.THEME_REGISTRY_KEY);
            if (regOpt.isPresent()) {
                EnchantmentTheme theme = regOpt.get().get(themeKey);
                if (theme != null) {
                    if (theme.colorCode().isPresent()) {
                        color = theme.colorCode().get();
                    }
                    String rawText = theme.name().getString();
                    String translated = Component.translatable(translationKey).getString();
                    if (!translated.equals(translationKey)) {
                        label = Component.translatable(translationKey);
                    } else if (!rawText.isEmpty()) {
                        label = Component.literal(capitalizeWords(rawText));
                    }
                }
            }
        }

        if (label == null) {
            String translated = Component.translatable(translationKey).getString();
            if (!translated.equals(translationKey)) {
                label = Component.translatable(translationKey);
            } else {
                label = Component.literal(capitalizeWords(id.getPath()));
            }
        }

        return label.withStyle(Style.EMPTY.withColor(color & 0xFFFFFF));
    }

    /**
     * Builds a localized, color-styled {@link MutableComponent} representing the given {@link MagicSchool}.
     *
     * @param schoolKey      The {@link ResourceKey} of the magic school to format.
     * @param registryAccess The {@link RegistryAccess} to query for school metadata and color, or {@code null}.
     * @return A styled {@link MutableComponent} for the magic school name.
     */
    public static MutableComponent getFormattedSchoolComponent(ResourceKey<MagicSchool> schoolKey, @Nullable RegistryAccess registryAccess) {
        MagicSchool school = MagicSchools.getSchool(registryAccess, schoolKey);
        int color = school.getColor() & 0xFFFFFF;
        String translationKey = school.translationKey();
        MutableComponent label;
        if (translationKey != null && !translationKey.isEmpty()) {
            label = Component.translatable(translationKey);
        } else {
            ResourceLocation id = schoolKey.location();
            label = Component.translatable("magic_school." + id.getNamespace() + "." + id.getPath());
        }
        return label.withStyle(Style.EMPTY.withColor(color));
    }

    /**
     * Replaces underscores with spaces and capitalizes the first letter of each whitespace-separated word.
     *
     * @param input The raw string to capitalize.
     * @return The capitalized title-case string, or an empty string if {@code input} is null or empty.
     */
    private static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] parts = input.replace('_', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1));
            }
        }
        return sb.toString();
    }
}