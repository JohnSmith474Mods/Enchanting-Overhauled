package johnsmith.enchantingoverhauled.menu.enchant.controller;

import com.google.common.collect.Lists;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.collection.WeightedSort;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.PowerProvider;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantContext;
import johnsmith.enchantingoverhauled.menu.enchant.model.PendingEnchantment;
import johnsmith.enchantingoverhauled.api.config.type.EnchantPowerHalvingType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * A fluent builder that manages the complex generation pipeline of generating available
 * enchantment options for the Enchanting Altar menu.
 * <p>
 * Evaluates the existing Target enchantments, overlaps and resolves Source catalyst enchantments,
 * and randomly generates missing slots via the Table, returning a deterministic List of {@link EnchantContext}.
 */
public class EnchantContextBuilder {

    // region Fields & Initialization
    /** The deterministic seed bound to the player's current enchantment cycle. */
    private final int baseSeed;

    /** The original item stack placed in the target slot. */
    private final ItemStack originalTarget;

    /** A copy of the target item stack with all cursed enchantments explicitly removed. */
    private final ItemStack curseFreeTarget;

    /** The catalyst item stack placed in the source slot (typically an Enchanted Book). */
    private final ItemStack source;

    /** The total enchanting power contributed by nearby bookshelves/blocks. */
    private final int power;

    /** The resolved base enchantability of the target item (or its recipe override). */
    private int enchantability;

    /** The configured maximum limit of active enchantments the target item can hold. */
    private final int maxEnchants;

    /** The internal sequential list of generated contexts being actively built. */
    private final List<EnchantContext> contexts = new ArrayList<>();

    /** A tracking set of enchantments already appended to prevent generation duplicates. */
    private final Set<Holder<Enchantment>> addedEnchantments = new HashSet<>();

    /**
     * Private constructor forcing the use of the static {@link #begin} factory method.
     */
    private EnchantContextBuilder(ItemStack target, ItemStack source, int power, int baseSeed) {
        this.baseSeed = baseSeed;
        this.originalTarget = target;
        this.curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        this.source = source;
        this.power = power;
        this.enchantability = Math.max(0, target.getItem().getEnchantmentValue());
        this.maxEnchants = Config.MAX_ENCHANTMENT_AMOUNT.get();
    }

    /**
     * Initializes a new pipeline builder instance.
     *
     * @param target   The target item to be enchanted.
     * @param source   The secondary catalyst item (e.g., a Tome).
     * @param power    The calculated enchanting power derived from nearby bookshelves.
     * @param baseSeed The player's current deterministic enchantment seed.
     * @return A new builder instance.
     */
    public static EnchantContextBuilder begin(ItemStack target, ItemStack source, int power, int baseSeed) {
        return new EnchantContextBuilder(target, source, power, baseSeed);
    }
    // endregion

    // region Pipeline Operations
    /**
     * Reads all non-cursed enchantments currently present on the target item,
     * sorts them by priority (Overleveled > Maxed > Upgradable), and inserts them
     * into the front of the context list.
     *
     * @return The builder instance for chaining.
     */
    public EnchantContextBuilder withTargetEnchantments() {
        if (!this.curseFreeTarget.isEnchanted()) {
            return this;
        }

        ItemEnchantments enchants = EnchantmentLib.getEnchantments(this.curseFreeTarget);
        List<EnchantmentInstance> targetEnchantments = new ArrayList<>();

        for (var entry : enchants.entrySet()) {
            targetEnchantments.add(new EnchantmentInstance(entry.getKey(), entry.getIntValue()));
        }

        targetEnchantments.sort((a, b) -> {
            int maxLevelA = a.enchantment.value().getMaxLevel();
            int maxLevelB = b.enchantment.value().getMaxLevel();

            boolean isOverleveledA = a.level > maxLevelA;
            boolean isOverleveledB = b.level > maxLevelB;

            if (isOverleveledA != isOverleveledB) return isOverleveledA ? -1 : 1;

            boolean isMaxedA = a.level == maxLevelA;
            boolean isMaxedB = b.level == maxLevelB;

            if (isMaxedA != isMaxedB) return isMaxedA ? -1 : 1;

            String idA = a.enchantment.unwrapKey().map(k -> k.location().toString()).orElse("");
            String idB = b.enchantment.unwrapKey().map(k -> k.location().toString()).orElse("");

            return idA.compareTo(idB);
        });

        for (EnchantmentInstance inst : targetEnchantments) {
            if (this.contexts.size() >= this.maxEnchants) break;
            this.contexts.add(this.createInitialContext(inst, EnchantmentSource.TARGET));
            this.addedEnchantments.add(inst.enchantment);
        }

        return this;
    }

    /**
     * Reads the enchantments from the provided source catalyst and applies them to the context.
     * Overwrites existing target context slots if an upgrade/transfer conflict is found, or appends
     * them as new options if they are functionally compatible with the item.
     *
     * @return The builder instance for chaining.
     */
    public EnchantContextBuilder withSourceEnchantments() {
        if (this.source.isEmpty() || this.originalTarget.is(Items.BOOK)) {
            return this;
        }

        List<EnchantmentInstance> sourceEnchants = this.getEnchantmentsAsList(EnchantmentLib.getEnchantments(this.source));

        for (EnchantmentInstance srcInst : sourceEnchants) {
            boolean matched = false;

            // Try to overwrite existing slots
            for (int i = 0; i < this.contexts.size(); i++) {
                EnchantContext ctx = this.contexts.get(i);
                if (ctx.applied().isPresent() && ctx.applied().get().enchantment.equals(srcInst.enchantment)) {
                    this.contexts.set(i, this.updateContextWithSource(ctx, srcInst));
                    matched = true;
                    break;
                }
            }

            // Append as a new slot if compatible
            if (!matched && this.contexts.size() < this.maxEnchants && srcInst.enchantment.value().canEnchant(this.originalTarget) && this.isCompatible(srcInst.enchantment)) {
                this.contexts.add(this.createSourceOnlyContext(srcInst));
                this.addedEnchantments.add(srcInst.enchantment);
            }
        }

        return this;
    }

    /**
     * Randomly generates new vanilla-style enchantment options to fill any remaining empty
     * slots up to the configured max enchantment limit. Ensures compatibility with already
     * assigned contexts to prevent applying mutually exclusive rules (e.g. Sharpness and Smite).
     *
     * @param world  The active level.
     * @param pos    The block position.
     * @param random The deterministic random generator initialized with the player's seed.
     * @return The builder instance for chaining.
     */
    public EnchantContextBuilder withTableEnchantments(Level world, BlockPos pos, RandomSource random) {
        Item item = this.originalTarget.getItem();
        this.enchantability = EnchantRecipe.get(world.registryAccess(), this.originalTarget)
                .map(EnchantRecipe::enchantability)
                .orElse(item.getEnchantmentValue());

        if (this.contexts.size() >= this.maxEnchants || this.enchantability <= 0) {
            return this;
        }

        List<EnchantmentInstance> allPossible = this.getPossibleEntries(this.originalTarget, false, world, pos);
        int maxPower = Config.MAX_ENCHANTING_POWER.get();
        int currentPower = Math.min(this.power + this.enchantability, maxPower);

        EnchantPowerHalvingType halvingType = Config.ENCHANT_POWER_HALVING_TYPE.get();
        if (halvingType == EnchantPowerHalvingType.PER_SLOT) {
            currentPower >>= this.contexts.size();
        }

        while (this.contexts.size() < this.maxEnchants && currentPower > 0) {
            final int activePower = currentPower;
            List<EnchantmentInstance> eligible = allPossible.stream()
                    .filter(inst -> !this.addedEnchantments.contains(inst.enchantment))
                    .filter(inst -> this.isCompatible(inst.enchantment))
                    .filter(inst -> activePower >= inst.enchantment.value().getMinCost(inst.level))
                    .collect(Collectors.toList());

            if (eligible.isEmpty()) {
                break;
            }

            List<EnchantmentInstance> shuffled = WeightedSort.skewedShuffle(
                    eligible,
                    entry -> entry.getWeight().asInt(),
                    random
            );

            EnchantmentInstance chosen = shuffled.getFirst();
            this.contexts.add(this.createInitialContext(chosen, EnchantmentSource.TABLE));
            this.addedEnchantments.add(chosen.enchantment);

            // Halve the available power for the next generated slot
            if (halvingType != EnchantPowerHalvingType.NONE) {
                currentPower /= 2;
            }
        }
        return this;
    }
    // endregion

    // region Finalization
    /**
     * Finalizes the generated contexts, assigning their specific slot indexes and calculating
     * their dynamic resource costs via the provided strategy function.
     *
     * @param costCalculator The strategy function calculating base resource cost for an enchantment.
     * @return The final immutable list of contexts to be dispatched to the UI.
     */
    public List<EnchantContext> build(ToIntFunction<Enchantment> costCalculator) {
        List<EnchantContext> finalized = new ArrayList<>();
        int maxPower = Config.MAX_ENCHANTING_POWER.get();
        int effectivePower = this.power + Math.max(0, this.enchantability);

        for (int i = 0; i < this.contexts.size(); i++) {
            EnchantContext ctx = this.contexts.get(i);
            int slotXpRequirement = (maxPower - Math.min(effectivePower, maxPower - 1)) * (i + 1);
            int enchantmentXpRequirement = this.calculateEnchantmentXpRequirement(ctx, i);
            int xpRequirement = slotXpRequirement + enchantmentXpRequirement;
            int cost = ctx.pending().enchantment().map(e -> costCalculator.applyAsInt(e.enchantment.value())).orElse(1);

            finalized.add(new EnchantContext(i, ctx.applied(), ctx.pending(), xpRequirement, cost, cost));
        }

        return finalized;
    }
    // endregion

    // region Internal Helpers
    /**
     * Calculates the intrinsic XP level requirement for the enchantment in the given slot
     * using its configured {@code min_cost} and {@code max_cost} at the target effective level,
     * bounded by {@link Config#GLOBAL_ENCHANTMENT_MIN_COST} and {@link Config#GLOBAL_ENCHANTMENT_MAX_COST}.
     */
    private int calculateEnchantmentXpRequirement(EnchantContext ctx, int slotIndex) {
        if (ctx.pending().enchantment().isEmpty()) {
            return 0;
        }

        EnchantmentInstance pendingInst = ctx.pending().enchantment().get();
        Enchantment enchantment = pendingInst.enchantment.value();

        int effectiveLevel = ctx.applied()
                .map(appliedInst -> Math.max(appliedInst.level + 1, pendingInst.level))
                .orElse(pendingInst.level);

        int globalMin = Config.GLOBAL_ENCHANTMENT_MIN_COST.get();
        int globalMax = Math.max(globalMin, Config.GLOBAL_ENCHANTMENT_MAX_COST.get());

        int minCost = Math.clamp(enchantment.getMinCost(effectiveLevel), globalMin, globalMax);
        int maxCost = Math.clamp(enchantment.getMaxCost(effectiveLevel), minCost, globalMax);

        if (maxCost <= 0) {
            return 0;
        }

        RandomSource slotRandom = this.createDeterministicSlotRandom(pendingInst.enchantment, effectiveLevel, slotIndex);
        return Mth.clamp(
                minCost + slotRandom.nextInt(maxCost),
                minCost,
                maxCost
        );
    }

    /**
     * Creates a dedicated deterministic {@link RandomSource} seeded by the player's enchantment seed,
     * the enchantment's registry key hash, and the effective level being evaluated.
     * Guarantees that container slot manipulations cannot reroll XP requirements.
     */
    private RandomSource createDeterministicSlotRandom(Holder<Enchantment> enchantment, int effectiveLevel, int slotIndex) {
        int enchHash = enchantment.unwrapKey()
                .map(key -> key.location().hashCode())
                .orElse(slotIndex);
        long mixedSeed = ((long) this.baseSeed << 32) ^ ((long) enchHash * 31L + effectiveLevel);
        return RandomSource.create(mixedSeed);
    }

    /**
     * Instantiates a preliminary context wrapping the provided instance and its visual source origin.
     *
     * @param inst   The enchantment instance.
     * @param source The origin classifying the incoming context.
     * @return A new {@link EnchantContext} mapped to index 0 (updated dynamically later).
     */
    private EnchantContext createInitialContext(EnchantmentInstance inst, EnchantmentSource source) {
        PendingEnchantment pending = new PendingEnchantment(Optional.of(inst), source);
        Optional<EnchantmentInstance> applied = source == EnchantmentSource.TARGET ? Optional.of(inst) : Optional.empty();
        return new EnchantContext(0, applied, pending, 0, 0, 0);
    }

    /**
     * Produces a new context representing a structural conflict where an existing target enchantment
     * is being challenged by an incoming source catalyst.
     *
     * @param old     The previous context currently applied to the target.
     * @param srcInst The new incoming instance trying to overwrite/upgrade it.
     * @return A merged {@link EnchantContext}.
     */
    private EnchantContext updateContextWithSource(EnchantContext old, EnchantmentInstance srcInst) {
        PendingEnchantment pending = new PendingEnchantment(Optional.of(srcInst), EnchantmentSource.SOURCE);
        return new EnchantContext(0, old.applied(), pending, 0, 0, 0);
    }

    /**
     * Instantiates a new context exclusively mapped to the source, bypassing the target.
     * Used for pure transfers of a new enchantment the item does not yet possess.
     *
     * @param srcInst The source instance.
     * @return The new {@link EnchantContext}.
     */
    private EnchantContext createSourceOnlyContext(EnchantmentInstance srcInst) {
        PendingEnchantment pending = new PendingEnchantment(Optional.of(srcInst), EnchantmentSource.SOURCE);
        return new EnchantContext(0, Optional.empty(), pending, 0, 0, 0);
    }

    /**
     * Ensures an incoming candidate enchantment functionally supports being applied alongside
     * the existing array of calculated enchantments (preventing conflicts like Sharpness + Smite).
     *
     * @param candidate The potential enchantment definition.
     * @return true if compatible, false otherwise.
     */
    private boolean isCompatible(Holder<Enchantment> candidate) {
        for (EnchantContext ctx : this.contexts) {
            if (ctx.pending().enchantment().isEmpty()) continue;
            Holder<Enchantment> existing = ctx.pending().enchantment().get().enchantment;
            if (existing.equals(candidate)) continue;
            if (!Enchantment.areCompatible(candidate, existing)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts an ItemEnchantments component into a List of EnchantmentInstance objects.
     *
     * @param enchantments The component to process.
     * @return A List of entries.
     */
    public List<EnchantmentInstance> getEnchantmentsAsList(ItemEnchantments enchantments) {
        // EnchantmentInstance now takes Holder<Enchantment> in its constructor
        return enchantments.entrySet().stream().map(
                entry -> new EnchantmentInstance(entry.getKey(), entry.getIntValue())
        ).collect(Collectors.toList());
    }

    /**
     * Generates a randomized list of possible enchantments for an item based on its enchantability,
     * filtered by available themed power providers.
     */
    public List<EnchantmentInstance> generateEnchantments(RandomSource random, ItemStack target, boolean treasureAllowed, Level level, BlockPos pos) {
        List<EnchantmentInstance> enchantments = Lists.newArrayList();
        Item item = target.getItem();
        int enchantability = item.getEnchantmentValue();

        Optional<EnchantRecipe> recipe = EnchantRecipe.get(level.registryAccess(), target);
        if (recipe.isPresent()) {
            enchantability = recipe.get().enchantability();
        }

        if (enchantability <= 0) {
            return enchantments;
        }

        List<EnchantmentInstance> possibleEntries = this.getPossibleEntries(target, treasureAllowed, level, pos);
        enchantments.addAll(possibleEntries);
        enchantments = WeightedSort.skewedShuffle(enchantments, entry -> entry.getWeight().asInt(), random);
        return enchantments;
    }

    /**
     * Gets a list of all enchantment entries that can possibly be applied to the given item,
     * filtered by the available themed power providers.
     */
    public List<EnchantmentInstance> getPossibleEntries(ItemStack target, boolean treasureAllowed, Level level, BlockPos pos) {
        List<EnchantmentInstance> enchantments = Lists.newArrayList();
        boolean hasRecipe = EnchantRecipe.get(level.registryAccess(), target).isPresent();

        // Use the registry from the level to iterate holders
        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        for (Holder<Enchantment> enchantmentHolder : registry.holders().toList()) {
            Enchantment enchantment = enchantmentHolder.value();
            ResourceKey<EnchantmentTheme> themeKey = EnchantmentThemes.getThemeKey(level.registryAccess(), enchantmentHolder);

            boolean isThemed = !themeKey.equals(EnchantmentThemes.COMMON);
            boolean themeActive = false;

            if (isThemed) {
                themeActive = this.hasThemedPowerProvider(level, pos, themeKey);
                if (!themeActive) {
                    continue;
                }
            }

            boolean canApplyTreasure = treasureAllowed || themeActive;

            if (enchantmentHolder.is(EnchantmentTags.TREASURE) && !canApplyTreasure) {
                continue;
            }

            if ((enchantmentHolder.is(EnchantmentTags.IN_ENCHANTING_TABLE) || themeActive)
                    && (hasRecipe || enchantment.canEnchant(target))
            ) {
                enchantments.add(new EnchantmentInstance(enchantmentHolder, 1));
            }
        }

        return enchantments;
    }

    /**
     * Checks if an enchanting table has any valid power providers for a *specific theme* nearby.
     */
    public boolean hasThemedPowerProvider(Level level, BlockPos tablePos, ResourceKey<EnchantmentTheme> themeKey) {
        if (themeKey.equals(EnchantmentThemes.COMMON)) {
            return false;
        }

        Optional<Registry<EnchantmentTheme>> registryOpt = level.registryAccess().registry(EnchantmentThemes.THEME_REGISTRY_KEY);
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

                if (providerState.is(Blocks.STRUCTURE_VOID)) {
                    return true;
                }

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
    // endregion
}