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
import net.minecraft.core.component.DataComponents;
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

    /** A tracking set of enchantments already appended or present on the target to prevent duplicates. */
    private final Set<Holder<Enchantment>> addedEnchantments = new HashSet<>();

    /**
     * Tracks all enchantments truly present on {@link #originalTarget} (including concealed curses)
     * so source conflict resolution always inspects the true applied level.
     */
    private final Map<Holder<Enchantment>, EnchantmentInstance> trueTargetEnchantments = new LinkedHashMap<>();

    /**
     * Tracks how many cap-contributing enchantments currently occupy the {@link #maxEnchants} budget.
     */
    private int occupiedCapSlots = 0;

    /**
     * Private constructor forcing the use of the static {@link #begin} factory method.
     */
    private EnchantContextBuilder(ItemStack target, ItemStack source, int power, int baseSeed) {
        this.baseSeed = baseSeed;
        this.originalTarget = target;
        this.source = source;
        this.power = power;
        this.enchantability = Math.max(0, target.isEnchantable() ? Objects.requireNonNull(target.getComponents().get(DataComponents.ENCHANTABLE)).value() : 0);
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
     * Reads all enchantments currently present on the target item, tracking their true levels
     * and cap contributions while respecting {@link Config#CURSE_CONCEALMENT_MODE} for UI slot creation.
     * Visible target enchantments are sorted by priority (Overleveled > Maxed > Upgradable) and inserted
     * into the context list.
     *
     * @return The builder instance for chaining.
     */
    public EnchantContextBuilder withTargetEnchantments() {
        if (!this.originalTarget.isEnchanted()) {
            return this;
        }

        ItemEnchantments enchants = EnchantmentLib.getEnchantments(this.originalTarget);
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
            this.trueTargetEnchantments.put(inst.enchantment, inst);
            this.addedEnchantments.add(inst.enchantment);

            boolean countsTowardCap = EnchantmentLib.countsTowardCap(inst.enchantment);
            boolean concealedOnTarget = EnchantmentLib.isCurseConcealed(inst.enchantment, false);

            if (concealedOnTarget) {
                if (countsTowardCap) {
                    this.occupiedCapSlots++;
                }
                continue;
            }

            if (countsTowardCap && this.occupiedCapSlots >= this.maxEnchants) {
                continue;
            }

            this.contexts.add(this.createInitialContext(inst, EnchantmentSource.TARGET));
            if (countsTowardCap) {
                this.occupiedCapSlots++;
            }
        }

        return this;
    }

    /**
     * Reads the enchantments from the provided source catalyst and applies them to the context.
     * Overwrites existing target context slots if an upgrade/transfer conflict is found, reveals
     * concealed target curses against their true applied level when challenged by an unconcealed
     * source curse, or appends them as new options if functionally compatible and within the cap budget.
     *
     * @return The builder instance for chaining.
     */
    public EnchantContextBuilder withSourceEnchantments() {
        if (this.source.isEmpty() || this.originalTarget.is(Items.BOOK)) {
            return this;
        }

        List<EnchantmentInstance> sourceEnchants = this.getEnchantmentsAsList(EnchantmentLib.getEnchantments(this.source));

        for (EnchantmentInstance srcInst : sourceEnchants) {
            // Skip curses that are concealed on stored enchantment catalysts (STORED or ALL)
            if (EnchantmentLib.isCurseConcealed(srcInst.enchantment, true)) {
                continue;
            }

            boolean matched = false;

            // Try to overwrite existing visible target slots
            for (int i = 0; i < this.contexts.size(); i++) {
                EnchantContext ctx = this.contexts.get(i);
                if (ctx.applied().isPresent() && ctx.applied().get().enchantment.equals(srcInst.enchantment)) {
                    this.contexts.set(i, this.updateContextWithSource(ctx, srcInst));
                    matched = true;
                    break;
                }
            }

            // Check if the enchantment is already present on the target item but was concealed (e.g., APPLIED mode)
            if (!matched && this.trueTargetEnchantments.containsKey(srcInst.enchantment)) {
                EnchantmentInstance appliedInst = this.trueTargetEnchantments.get(srcInst.enchantment);
                PendingEnchantment pending = new PendingEnchantment(Optional.of(srcInst), EnchantmentSource.SOURCE);
                this.contexts.add(new EnchantContext(0, Optional.of(appliedInst), pending, 0, 0, 0));
                matched = true;
            }

            // Append as a new slot if not yet present on the target item, compatible, and within cap constraints
            if (!matched) {
                boolean countsTowardCap = EnchantmentLib.countsTowardCap(srcInst.enchantment);
                boolean hasCapSpace = !countsTowardCap || this.occupiedCapSlots < this.maxEnchants;

                if (hasCapSpace && srcInst.enchantment.value().canEnchant(this.originalTarget) && this.isCompatible(srcInst.enchantment)) {
                    this.contexts.add(this.createSourceOnlyContext(srcInst));
                    this.addedEnchantments.add(srcInst.enchantment);
                    if (countsTowardCap) {
                        this.occupiedCapSlots++;
                    }
                }
            }
        }

        return this;
    }

    /**
     * Randomly generates new vanilla-style enchantment options to fill any remaining empty
     * cap slots up to the configured max enchantment limit. Ensures compatibility with already
     * assigned contexts and concealed target enchantments to prevent mutually exclusive rules.
     *
     * @param world  The active level.
     * @param pos    The block position.
     * @param random The deterministic random generator initialized with the player's seed.
     * @return The builder instance for chaining.
     */
    public EnchantContextBuilder withTableEnchantments(Level world, BlockPos pos, RandomSource random) {
        this.enchantability = EnchantRecipe.get(world.registryAccess(), this.originalTarget)
                .map(EnchantRecipe::enchantability)
                .orElse(originalTarget.isEnchantable() ? Objects.requireNonNull(originalTarget.getComponents().get(DataComponents.ENCHANTABLE)).value() : 0);

        if (this.occupiedCapSlots >= this.maxEnchants || this.enchantability <= 0) {
            return this;
        }

        List<EnchantmentInstance> allPossible = this.getPossibleEntries(this.originalTarget, false, world, pos);
        int maxPower = Config.MAX_ENCHANTING_POWER.get();
        int currentPower = Math.min(this.power + this.enchantability, maxPower);

        EnchantPowerHalvingType halvingType = Config.ENCHANT_POWER_HALVING_TYPE.get();
        if (halvingType == EnchantPowerHalvingType.PER_SLOT) {
            currentPower >>= this.occupiedCapSlots;
        }

        while (this.occupiedCapSlots < this.maxEnchants && currentPower > 0) {
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
            this.occupiedCapSlots++;

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
     * Finalizes the generated contexts, sorting actionable options to the top of the list,
     * assigning their specific slot indexes, and calculating their dynamic resource costs
     * via the provided strategy function.
     *
     * @param costCalculator The strategy function calculating base resource cost for an enchantment.
     * @return The final immutable list of contexts to be dispatched to the UI.
     */
    public List<EnchantContext> build(ToIntFunction<Enchantment> costCalculator) {
        this.contexts.sort(this::compareContextsForDisplay);

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
     * Compares two enchantment contexts to determine their slot and display order in the menu.
     * <p>
     * Strictly orders contexts from top to bottom by source priority:
     * {@link EnchantmentSource#SOURCE} &gt; {@link EnchantmentSource#TARGET} &gt; {@link EnchantmentSource#TABLE}.
     * Within {@code TARGET} contexts, preserves the target sub-priority (Overleveled &gt; Maxed &gt; Upgradable).
     *
     * @param a The first context to compare.
     * @param b The second context to compare.
     * @return A negative integer, zero, or a positive integer as the first context should
     *         appear before, at the same level as, or after the second context.
     */
    private int compareContextsForDisplay(EnchantContext a, EnchantContext b) {
        int sourcePriorityA = getSourcePriority(a.pending().source());
        int sourcePriorityB = getSourcePriority(b.pending().source());

        if (sourcePriorityA != sourcePriorityB) {
            return Integer.compare(sourcePriorityA, sourcePriorityB);
        }

        return 0;
    }

    /**
     * Returns the display priority rank for an enchantment source.
     * Lower values appear higher in the list ({@code SOURCE} &gt; {@code TARGET} &gt; {@code TABLE}).
     *
     * @param source The {@link EnchantmentSource} of the pending enchantment.
     * @return The integer priority rank.
     */
    private static int getSourcePriority(EnchantmentSource source) {
        return switch (source) {
            case SOURCE -> 0;
            case TARGET -> 1;
            case TABLE -> 2;
            case NONE -> 3;
        };
    }

    /**
     * Ensures an incoming candidate enchantment functionally supports being applied alongside
     * all enchantments truly present on the target item (including concealed curses) as well as
     * the existing array of calculated contexts (preventing conflicts like Sharpness + Smite).
     *
     * @param candidate The potential enchantment definition.
     * @return true if compatible, false otherwise.
     */
    private boolean isCompatible(Holder<Enchantment> candidate) {
        for (Holder<Enchantment> existingTarget : this.trueTargetEnchantments.keySet()) {
            if (existingTarget.equals(candidate)) continue;
            if (!Enchantment.areCompatible(candidate, existingTarget)) {
                return false;
            }
        }
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
        int enchantability = target.isEnchantable() ? Objects.requireNonNull(target.getComponents().get(DataComponents.ENCHANTABLE)).value() : 0;

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
        Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        for (Holder<Enchantment> enchantmentHolder : registry.listElements().toList()) {
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

        Optional<Registry<EnchantmentTheme>> registryOpt = level.registryAccess().lookup(EnchantmentThemes.THEME_REGISTRY_KEY);
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