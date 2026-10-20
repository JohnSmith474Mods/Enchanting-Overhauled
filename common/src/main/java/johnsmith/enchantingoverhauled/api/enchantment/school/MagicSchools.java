package johnsmith.enchantingoverhauled.api.enchantment.school;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry key holder and utility class for querying {@link MagicSchool} definitions,
 * school enchantment tags, and school cycle relationships.
 */
public class MagicSchools {

    /**
     * The {@link ResourceKey} identifying the custom dynamic registry for {@link MagicSchool} definitions.
     */
    public static final ResourceKey<Registry<MagicSchool>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("magic_school"));

    /**
     * The fallback {@link ResourceKey} representing the unassigned "Aberration" school,
     * applied when an enchantment does not belong to any registered school tag.
     */
    public static final ResourceKey<MagicSchool> ABERRATION = createKey("aberration");

    /**
     * The default RGB color ({@code 0xFFAA00}, gold/amber) associated with {@link #ABERRATION}.
     */
    public static final int ABERRATION_COLOR = 0xFFAA00;

    /**
     * Built-in fallback map ensuring {@link #ABERRATION} always resolves to a valid {@link MagicSchool}
     * instance even if queried without datapack registry access.
     */
    public static final Map<ResourceKey<MagicSchool>, MagicSchool> BUILTIN_FALLBACKS = Map.of(
            ABERRATION, new MagicSchool(
                    "aberration",
                    "magic_school.enchanting_overhauled.aberration",
                    Optional.of(ABERRATION_COLOR)
            )
    );

    /**
     * Helper method to create a {@link MagicSchool} {@link ResourceKey} under the mod's namespace.
     *
     * @param name The path name of the magic school.
     * @return The constructed {@link ResourceKey}.
     */
    private static ResourceKey<MagicSchool> createKey(String name) {
        return ResourceKey.create(REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    /**
     * Constructs the enchantment {@link TagKey} corresponding to the given {@link MagicSchool} key
     * (formatted as {@code #<namespace>:school/<path>}).
     *
     * @param schoolKey The {@link ResourceKey} of the magic school.
     * @return The corresponding enchantment {@link TagKey}.
     */
    public static TagKey<Enchantment> getSchoolTag(ResourceKey<MagicSchool> schoolKey) {
        ResourceLocation id = schoolKey.location();
        return TagKey.create(
                Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "school/" + id.getPath())
        );
    }

    /**
     * Constructs a magic school cycle {@link TagKey} for the given cycle identifier
     * (formatted as {@code #<namespace>:cycle/<path>}).
     *
     * @param cycleId The {@link ResourceLocation} of the cycle.
     * @return The corresponding magic school {@link TagKey}.
     */
    public static TagKey<MagicSchool> getCycleTag(ResourceLocation cycleId) {
        return TagKey.create(
                REGISTRY_KEY,
                ResourceLocation.fromNamespaceAndPath(cycleId.getNamespace(), "cycle/" + cycleId.getPath())
        );
    }

    /**
     * Checks whether the given {@link MagicSchool} tag key represents a school cycle tag
     * (i.e., its path starts with {@code "cycle/"}).
     *
     * @param tagKey The {@link TagKey} to inspect.
     * @return {@code true} if the tag key is a cycle tag; {@code false} otherwise.
     */
    public static boolean isCycleTag(TagKey<MagicSchool> tagKey) {
        return tagKey.location().getPath().startsWith("cycle/");
    }

    /**
     * Retrieves all cycle {@link TagKey}s to which the specified {@link MagicSchool} belongs.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param schoolKey      The {@link ResourceKey} of the school to query, or {@code null}.
     * @return An immutable list of matching cycle {@link TagKey}s, or an empty list if none or aberrant.
     */
    public static List<TagKey<MagicSchool>> getCycleTags(
            @Nullable RegistryAccess registryAccess,
            @Nullable ResourceKey<MagicSchool> schoolKey
    ) {
        if (registryAccess == null || schoolKey == null || isAberration(schoolKey)) {
            return List.of();
        }
        Optional<Registry<MagicSchool>> regOpt = registryAccess.registry(REGISTRY_KEY);
        if (regOpt.isEmpty()) {
            return List.of();
        }
        Optional<Holder.Reference<MagicSchool>> holderOpt = regOpt.get().getHolder(schoolKey);
        if (holderOpt.isEmpty()) {
            return List.of();
        }
        return holderOpt.get().tags()
                .filter(MagicSchools::isCycleTag)
                .toList();
    }

    /**
     * Determines whether two {@link MagicSchool}s belong to the same magic cycle
     * (e.g., Vitality, Cognition, or Material).
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param firstSchool    The first {@link MagicSchool} key.
     * @param secondSchool   The second {@link MagicSchool} key.
     * @return {@code true} if both schools are non-aberrant and share at least one {@code cycle/*} tag; {@code false} otherwise.
     */
    public static boolean areInSameCycle(
            @Nullable RegistryAccess registryAccess,
            @Nullable ResourceKey<MagicSchool> firstSchool,
            @Nullable ResourceKey<MagicSchool> secondSchool
    ) {
        if (registryAccess == null
                || firstSchool == null
                || secondSchool == null
                || isAberration(firstSchool)
                || isAberration(secondSchool)) {
            return false;
        }
        if (firstSchool.equals(secondSchool)) {
            return true;
        }
        Optional<Registry<MagicSchool>> regOpt = registryAccess.registry(REGISTRY_KEY);
        if (regOpt.isEmpty()) {
            return false;
        }
        Registry<MagicSchool> registry = regOpt.get();
        Optional<Holder.Reference<MagicSchool>> firstHolder = registry.getHolder(firstSchool);
        Optional<Holder.Reference<MagicSchool>> secondHolder = registry.getHolder(secondSchool);
        if (firstHolder.isEmpty() || secondHolder.isEmpty()) {
            return false;
        }
        return firstHolder.get().tags()
                .filter(MagicSchools::isCycleTag)
                .anyMatch(secondHolder.get()::is);
    }

    /**
     * Checks whether the given school key is {@code null} or represents {@link #ABERRATION}.
     *
     * @param schoolKey The {@link ResourceKey} to check.
     * @return {@code true} if the key is {@code null} or {@code aberration}; {@code false} otherwise.
     */
    public static boolean isAberration(@Nullable ResourceKey<MagicSchool> schoolKey) {
        return schoolKey == null
                || schoolKey.equals(ABERRATION)
                || schoolKey.location().getPath().equals("aberration");
    }

    /**
     * Resolves the {@link MagicSchool} definition for the given key from the dynamic registry,
     * falling back to {@link #BUILTIN_FALLBACKS} or a synthesized default instance if unavailable.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param schoolKey      The {@link ResourceKey} of the magic school to resolve.
     * @return The resolved or fallback {@link MagicSchool} instance.
     */
    public static MagicSchool getSchool(@Nullable RegistryAccess registryAccess, ResourceKey<MagicSchool> schoolKey) {
        if (registryAccess != null) {
            Optional<Registry<MagicSchool>> regOpt = registryAccess.registry(REGISTRY_KEY);
            if (regOpt.isPresent()) {
                MagicSchool found = regOpt.get().get(schoolKey);
                if (found != null) {
                    return found;
                }
            }
        }
        ResourceLocation id = schoolKey.location();
        return BUILTIN_FALLBACKS.getOrDefault(
                schoolKey,
                new MagicSchool(
                        id.getPath(),
                        "magic_school." + id.getNamespace() + "." + id.getPath(),
                        Optional.empty()
                )
        );
    }

    /**
     * Resolves a canonical bound {@link Holder.Reference} from the active enchantment registry
     * so tag lookups succeed even if a direct or unbound holder was passed.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param enchantment    The enchantment holder to canonicalize.
     * @return The bound enchantment holder if found, or the original holder.
     */
    private static Holder<Enchantment> resolveCanonicalHolder(@Nullable RegistryAccess registryAccess, Holder<Enchantment> enchantment) {
        if (registryAccess != null && enchantment.unwrapKey().isPresent()) {
            Optional<Registry<Enchantment>> enchRegOpt = registryAccess.registry(Registries.ENCHANTMENT);
            if (enchRegOpt.isPresent()) {
                Optional<Holder.Reference<Enchantment>> bound = enchRegOpt.get().getHolder(enchantment.unwrapKey().get());
                if (bound.isPresent()) {
                    return bound.get();
                }
            }
        }
        return enchantment;
    }

    /**
     * Finds the primary {@link MagicSchool} key assigned to the given enchantment via its school tag,
     * or returns {@link #ABERRATION} if no registered school tag contains the enchantment.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param enchantment    The enchantment {@link Holder} to inspect.
     * @return The matching {@link MagicSchool} key, or {@link #ABERRATION} if unassigned.
     */
    public static ResourceKey<MagicSchool> getSchoolKey(@Nullable RegistryAccess registryAccess, Holder<Enchantment> enchantment) {
        if (registryAccess != null) {
            Holder<Enchantment> targetHolder = resolveCanonicalHolder(registryAccess, enchantment);
            var schoolRegistryOpt = registryAccess.registry(REGISTRY_KEY);
            if (schoolRegistryOpt.isPresent()) {
                for (var schoolRef : schoolRegistryOpt.get().holders().toList()) {
                    ResourceKey<MagicSchool> key = schoolRef.key();
                    if (isAberration(key)) {
                        continue;
                    }
                    if (targetHolder.is(getSchoolTag(key))) {
                        return key;
                    }
                }
            }
        }
        return ABERRATION;
    }

    /**
     * Returns all non-aberrant {@link MagicSchool} keys whose school tags contain the given enchantment.
     * Used by {@link johnsmith.enchantingoverhauled.server.AdminSchoolValidator} to detect unassigned
     * or multi-school enchantments.
     *
     * @param registryAccess The active {@link RegistryAccess}, or {@code null}.
     * @param enchantment    The enchantment {@link Holder} to inspect.
     * @return A list of all matching {@link MagicSchool} resource keys.
     */
    public static List<ResourceKey<MagicSchool>> getMatchingSchools(@Nullable RegistryAccess registryAccess, Holder<Enchantment> enchantment) {
        List<ResourceKey<MagicSchool>> matches = new ArrayList<>();
        if (registryAccess != null) {
            Holder<Enchantment> targetHolder = resolveCanonicalHolder(registryAccess, enchantment);
            var schoolRegistryOpt = registryAccess.registry(REGISTRY_KEY);
            if (schoolRegistryOpt.isPresent()) {
                for (var schoolRef : schoolRegistryOpt.get().holders().toList()) {
                    ResourceKey<MagicSchool> key = schoolRef.key();
                    if (isAberration(key)) {
                        continue;
                    }
                    if (targetHolder.is(getSchoolTag(key))) {
                        matches.add(key);
                    }
                }
            }
        }
        return matches;
    }

    /**
     * Triggers static class initialization to ensure registry keys are created.
     */
    public static void initialize() {}
}