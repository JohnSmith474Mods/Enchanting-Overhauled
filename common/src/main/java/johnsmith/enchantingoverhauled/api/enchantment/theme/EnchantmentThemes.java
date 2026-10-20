package johnsmith.enchantingoverhauled.api.enchantment.theme;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the static {@link ResourceKey} references and lookup utilities for the custom,
 * data-driven {@link EnchantmentTheme} registry.
 * <p>
 * Theme definitions are loaded dynamically from JSON files within
 * {@code data/<namespace>/enchantment_theme/}.
 */
public class EnchantmentThemes {

    /**
     * The {@link ResourceKey} used to identify the custom dynamic registry containing all
     * {@link EnchantmentTheme}s.
     * <p>
     * Note: The namespace is set to {@code minecraft} for compatibility with built-in data systems.
     */
    public static final ResourceKey<Registry<EnchantmentTheme>> THEME_REGISTRY_KEY = ResourceKey
            .createRegistryKey(ResourceLocation.withDefaultNamespace("enchantment_theme"));

    /**
     * The {@link ResourceKey} for the fallback common theme, used when an enchantment does not belong
     * to any specialized thematic tag.
     */
    public static final ResourceKey<EnchantmentTheme> COMMON = createKey("common");

    /**
     * Creates a {@link ResourceKey} for an {@link EnchantmentTheme} under the mod's namespace.
     *
     * @param path The theme path name.
     * @return The constructed {@link ResourceKey}.
     */
    private static ResourceKey<EnchantmentTheme> createKey(String path) {
        return ResourceKey.create(THEME_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    /**
     * Constructs the enchantment {@link TagKey} corresponding to a given {@link EnchantmentTheme} key
     * (formatted as {@code #<namespace>:theme/<path>}).
     *
     * @param themeKey The {@link ResourceKey} of the theme.
     * @return The corresponding enchantment {@link TagKey}.
     */
    public static TagKey<Enchantment> getThemeTag(ResourceKey<EnchantmentTheme> themeKey) {
        ResourceLocation id = themeKey.location();
        return TagKey.create(
                Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "theme/" + id.getPath()));
    }

    /**
     * Checks whether the given theme key is {@code null} or represents the {@link #COMMON} theme.
     *
     * @param themeKey The {@link ResourceKey} to inspect, or {@code null}.
     * @return {@code true} if the key is {@code null} or {@code common}; {@code false} otherwise.
     */
    public static boolean isCommon(@Nullable ResourceKey<EnchantmentTheme> themeKey) {
        if (themeKey == null)
            return true;
        if (themeKey.equals(COMMON))
            return true;
        String path = themeKey.location().getPath();
        return "common".equals(path);
    }

    /**
     * Scans the active {@link EnchantmentTheme} registry to find which theme tag matches the given enchantment.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param enchantment    The enchantment {@link Holder} to look up.
     * @return The {@link ResourceKey} of the matching theme, or {@link #COMMON} if none is found.
     */
    public static ResourceKey<EnchantmentTheme> getThemeKey(@Nullable RegistryAccess registryAccess,
                                                            Holder<Enchantment> enchantment) {
        if (registryAccess != null) {
            var themeRegistryOpt = registryAccess.registry(EnchantmentThemes.THEME_REGISTRY_KEY);
            if (themeRegistryOpt.isPresent()) {
                for (var themeRef : themeRegistryOpt.get().holders().toList()) {
                    ResourceKey<EnchantmentTheme> key = themeRef.key();
                    if (isCommon(key)) {
                        continue;
                    }
                    if (enchantment.is(getThemeTag(key))) {
                        return key;
                    }
                }
            }
        }
        return EnchantmentThemes.COMMON;
    }

    /**
     * Triggers static class initialization to ensure registry keys are created.
     */
    public static void initialize() {
    }
}