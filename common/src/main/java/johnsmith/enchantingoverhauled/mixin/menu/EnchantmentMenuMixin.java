package johnsmith.enchantingoverhauled.mixin.menu;

import johnsmith.enchantingoverhauled.accessor.EnchantmentMenuAccessor;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

/**
 * Mixin to {@link EnchantmentMenu} to completely overhaul the enchanting system.
 * <p>
 * This mixin implements the following major changes:
 * <ul>
 * <li><b>Third Inventory Slot:</b> Adds a slot (index 2) for "Source" items (e.g., Enchanted Books, Tomes).</li>
 * <li><b>Custom Layout:</b> Repositions all slots to match the overhauled GUI texture.</li>
 * <li><b>Logic Overhaul:</b> Replaces vanilla RNG enchantment generation with a deterministic priority system:
 * <ol>
 * <li><b>Upgrade:</b> Existing enchantments on the target item.</li>
 * <li><b>Transfer:</b> Enchantments from the source item.</li>
 * <li><b>New:</b> Random enchantments from the table (if space allows).</li>
 * </ol>
 * </li>
 * <li><b>Interaction Overhaul:</b> Clicking menu buttons now triggers specific actions (Upgrade/Transfer/Apply/Reroll)
 * instead of just applying a random set.</li>
 * <li><b>Synced Data:</b> Synchronizes the "source" of each enchantment option (Target vs Source vs Table) to the client for rendering.</li>
 * </ul>
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu implements EnchantmentMenuAccessor {

    // region Shadow Fields

    /**
     * The internal inventory of the enchanting table.
     * <p>
     * Vanilla size: 2 (Item, Lapis).
     * Overhauled size: 3 (Item, Lapis, Source).
     */
    @Shadow
    @Final
    private Container enchantSlots;

    /**
     * The experience level requirement for each of the 3 slots.
     * In this overhaul, these values are still calculated but used differently for UI display.
     */
    @Shadow
    @Final
    public int[] costs;

    /**
     * Context providing access to the world and block position.
     */
    @Shadow
    @Final
    private ContainerLevelAccess access;

    /**
     * The seed used for vanilla enchantment generation.
     * We continue to update this to maintain vanilla consistency where applicable.
     */
    @Shadow
    @Final
    private DataSlot enchantmentSeed;

    /**
     * Called when the inventory changes. We shadow this to trigger it manually after operations.
     */
    @Shadow
    public abstract void slotsChanged(@NotNull Container inventory);

    /**
     * Generates a list of random enchantments based on the seed.
     * We shadow this to use it as a fallback for filling empty slots.
     */
    @Shadow
    protected abstract List<EnchantmentInstance> getEnchantmentList(FeatureFlagSet enabledFeatures, ItemStack stack, int slot, int cost);

    @Shadow
    @Final
    private RandomSource random;

    /**
     * The registry ID of the enchantment shown in the tooltip (vanilla "clue").
     * We use this to sync the actual enchantment available for selection.
     */
    @Shadow
    @Final
    public int[] enchantClue;

    /**
     * The level of the enchantment shown in the tooltip.
     * We use this to sync the specific level available for selection.
     */
    @Shadow
    @Final
    public int[] levelClue;
    // endregion

    // region Constants
    @Unique private static final int TARGET_X_POSITION = 18;
    @Unique private static final int TARGET_Y_POSITION = 46 - 4;
    @Unique private static final int LAPIS_X_POSITION = 28;
    @Unique private static final int LAPIS_Y_POSITION = 73 - 8;
    @Unique private static final int SOURCE_X_POSITION = LAPIS_X_POSITION - 18 - 2;
    @Unique private static final int SOURCE_Y_POSITION = LAPIS_Y_POSITION;
    @Unique private static final int INVENTORY_Y_POSITION = 113;
    @Unique private static final int HOTBAR_Y_POSITION = 171;
    // endregion

    // region New Fields
    @Unique
    private static final RandomSource TEXTURE_RANDOM = RandomSource.create();

    /**
     * Tracks the origin of the enchantment in the corresponding slot (0, 1, or 2).
     * The value corresponds to one of the static constants (NONE, TARGET, SOURCE, TABLE).
     * This array is synced to the client via {@link #addAdditionalProperties}.
     */
    @Unique
    public int[] enchantmentSource;

    /** Tracks the random texture index (0-9) for the target button background. */
    @Unique
    public final int[] targetTextureIndices = new int[3];

    /** Tracks the random texture index (0-9) for the source button background. */
    @Unique
    public final int[] sourceTextureIndices = new int[3];

    /** Tracks the random texture index (0-9) for the table button background. */
    @Unique
    public final int[] tableTextureIndices = new int[3];

    /** Indicates the enchantment slot is empty or invalid. */
    @Unique private static final int NONE = -1;
    /** Indicates the enchantment option comes from the target item itself (an upgrade). */
    @Unique private static final int TARGET = 0;
    /** Indicates the enchantment option comes from the source item (a transfer). */
    @Unique private static final int SOURCE = 1;
    /** Indicates the enchantment option is newly generated by the table. */
    @Unique private static final int TABLE = 2;
    /** The ID of the custom "Reroll" button. */
    @Unique private static final int REROLL_BUTTON_INDEX = 3;
    // endregion

    // region Constructor

    protected EnchantmentMenuMixin(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    /**
     * Modifies the handler's internal inventory size from 2 to 3 to accommodate the new source item slot.
     */
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            constant = @Constant(intValue = 2, ordinal = 0))
    private int modifyInventorySize(int originalSize) {
        return 3;
    }

    /**
     * Injects at the end of the constructor to:
     * <ol>
     * <li>Add the new {@link Slot} for the source item (inventory index 2, slot list index 38).</li>
     * <li>Initialize the {@link #enchantmentSource} array.</li>
     * <li>Register the {@link #enchantmentSource} array elements as synced properties (DataSlots).</li>
     * </ol>
     */
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("RETURN"))
    private void addAdditionalProperties(int syncId, Inventory playerInventory, ContainerLevelAccess context, CallbackInfo ci) {
        // Add source/book slot at the custom coordinates
        this.addSlot(new Slot(this.enchantSlots, 2, SOURCE_X_POSITION, SOURCE_Y_POSITION) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof EnchantedBookItem;
            }
        });

        this.enchantmentSource = new int[]{NONE, NONE, NONE};

        // Add source array to synced data
        this.addDataSlot(DataSlot.shared(this.enchantmentSource, 0));
        this.addDataSlot(DataSlot.shared(this.enchantmentSource, 1));
        this.addDataSlot(DataSlot.shared(this.enchantmentSource, 2));

        // Add texture index properties to synced data
        this.addDataSlot(DataSlot.shared(this.targetTextureIndices, 0));
        this.addDataSlot(DataSlot.shared(this.targetTextureIndices, 1));
        this.addDataSlot(DataSlot.shared(this.targetTextureIndices, 2));
        this.addDataSlot(DataSlot.shared(this.sourceTextureIndices, 0));
        this.addDataSlot(DataSlot.shared(this.sourceTextureIndices, 1));
        this.addDataSlot(DataSlot.shared(this.sourceTextureIndices, 2));
        this.addDataSlot(DataSlot.shared(this.tableTextureIndices, 0));
        this.addDataSlot(DataSlot.shared(this.tableTextureIndices, 1));
        this.addDataSlot(DataSlot.shared(this.tableTextureIndices, 2));
    }
    // endregion

    // region GUI Position Modifiers

    /**
     * Redirects the Lapis slot creation to replace it with a version that respects our custom layout.
     */
    @Redirect(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/EnchantmentMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;",
                    ordinal = 1
            )
    )
    private Slot modifyLapisSlot(EnchantmentMenu instance, Slot originalSlot) {
        Slot newLapisSlot = new Slot(this.enchantSlots, 1, LAPIS_X_POSITION, LAPIS_Y_POSITION) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }
        };
        return this.addSlot(newLapisSlot);
    }

    /** Modifies the X-coordinate for the target item slot. */
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            constant = @Constant(intValue = 15, ordinal = 0))
    private int modifyTargetXPosition(int originalX) {
        return TARGET_X_POSITION;
    }

    /** Modifies the Y-coordinate for the target item slot. */
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            constant = @Constant(intValue = 47, ordinal = 0))
    private int modifyTargetYPosition(int originalY) {
        return TARGET_Y_POSITION;
    }

    /** Modifies the Y-coordinate for the player's main inventory. */
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            constant = @Constant(intValue = 84, ordinal = 0))
    private int modifyInventoryYPosition(int originalY) {
        return INVENTORY_Y_POSITION;
    }

    /** Modifies the Y-coordinate for the player's hotbar. */
    @ModifyConstant(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            constant = @Constant(intValue = 142, ordinal = 0))
    private int modifyHotbarYPosition(int originalY) {
        return HOTBAR_Y_POSITION;
    }

    /**
     * Helper method to generate a list of new, random enchantments for the target item.
     * This implementation delegates to our custom {@link EnchantmentLib} to handle theme-based logic.
     *
     * @param enabledFeatures The feature set of the world.
     * @param target The item to generate enchantments for.
     * @param world The level (for accessing blocks/themes).
     * @param pos The position of the table.
     */
    @Unique
    private List<EnchantmentInstance> generateEnchantments(FeatureFlagSet enabledFeatures, ItemStack target, Level world, BlockPos pos) {
        this.random.setSeed((long)(this.enchantmentSeed.get()));
        // Use EnchantmentLib logic
        return EnchantmentLib.generateEnchantments(enabledFeatures, this.random, target, false, world, pos);
    }
    // endregion

    // region Update Data

    /**
     * Overrides the default enchantment calculation logic.
     * <p>
     * This method is called whenever the inventory changes. It calculates the enchanting power
     * from nearby bookshelves (using custom logic) and then calls {@link #setDataSlots}
     * to populate the three enchantment options based on the new priority system.
     *
     * @param inventory The inventory that changed.
     * @param ci Callback info (used to cancel the original method).
     */
    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    public void slotsChanged(Container inventory, CallbackInfo ci) {
        // Guard clause: Only run for the handler's own inventory
        if (inventory != this.enchantSlots) {
            ci.cancel();
            return;
        }

        ItemStack target = inventory.getItem(0);
        ItemStack source = inventory.getItem(2);

        // Invalid state: Target item is empty or not enchantable
        if (!(!target.isEmpty() && target.isEnchantable())) {
            for(int i = 0; i < REROLL_BUTTON_INDEX; ++i) {
                this.costs[i] = 0;
                this.enchantClue[i] = NONE;
                this.levelClue[i] = NONE;
            }
            ci.cancel();
            return;
        }

        this.access.execute((world, pos) -> {
            // 1. Calculate enchanting power (custom logic)
            int power = 0;
            for (BlockPos providerOffset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                // Check if transmitter (air) is valid tag.
                // Note: Logic matches custom requirement where air blocks must be tagged.
                if (world.getBlockState(pos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    power += EnchantmentLib.getAgnosticEnchantingPower(world, pos.offset(providerOffset));
                }
            }

            // 2. Set base level requirements (vanilla-like calculation)
            this.random.setSeed((long) this.enchantmentSeed.get());

            for (int j = 0; j < REROLL_BUTTON_INDEX; ++j) {
                this.costs[j] = (25 - Math.min(power, 24)) * (j + 1);
                this.enchantClue[j] = NONE;
                this.levelClue[j] = NONE;
                if (this.costs[j] < j + 1) {
                    this.costs[j] = 0;
                }
            }

            // 3. Populate enchantment options using the new priority logic
            List<EnchantmentInstance> enchantments = this.setDataSlots(target, source, world.enabledFeatures(), world, pos);

            // 4. Fill the synced arrays with data from the generated list
            int slot = 0;
            for (EnchantmentInstance entry : enchantments) {
                this.enchantClue[slot] = BuiltInRegistries.ENCHANTMENT.getId(entry.enchantment);
                this.levelClue[slot] = entry.level;
                ++slot;
            }

            // 4b. Set random texture indices for client rendering
            for (int k = 0; k < REROLL_BUTTON_INDEX; ++k) {
                this.targetTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                this.sourceTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                this.tableTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
            }

            this.broadcastChanges();
        });

        ci.cancel(); // Cancel the original method
    }

    /**
     * Populates the three enchantment option slots based on a priority system:
     * <ol>
     * <li><b>Priority 1:</b> Existing enchantments on the {@code target} item (for upgrading).
     * Sets source to {@link #TARGET}.</li>
     * <li><b>Priority 2:</b> Enchantments from the {@code source} item (for transferring).
     * Sets source to {@link #SOURCE}.</li>
     * <li><b>Priority 3:</b> New enchantments generated from the table.
     * Sets source to {@link #TABLE}.</li>
     * </ol>
     *
     * @param target The item being enchanted.
     * @param source The item in the source slot (e.g., book, tome).
     * @param featureSet The world's enabled features.
     * @return A list of {@link EnchantmentInstance}s, max size 3, to be displayed.
     */
    @Unique
    public List<EnchantmentInstance> setDataSlots(ItemStack target, ItemStack source, FeatureFlagSet featureSet, Level world, BlockPos pos) {
        List<EnchantmentInstance> list = new ArrayList<>();
        Set<Enchantment> addedEnchantments = new HashSet<>();

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int arraySize = this.costs.length; // Should be 3

        // Reset all enchantment sources
        for (int i = 0; i < arraySize; i++) {
            this.enchantmentSource[i] = NONE;
        }

        // Priority 1: Add all the target's current enchantments
        this.readEnchantmentsFromTarget(curseFreeTarget, list, arraySize, addedEnchantments);

        // Priority 2: Check source item for any higher-level upgrades and overwrite
        this.overwriteTargetEnchantmentsFromSource(target, source, list, addedEnchantments);

        // Priority 3: Check source item for any *new* enchantments and append if space
        this.appendSourceEnchantments(target, source, list, arraySize, addedEnchantments);

        // Priority 4: Fill remaining slots from the table
        this.generateEnchantmentsFromTable(target, featureSet, list, arraySize, world, pos);

        return list;
    }

    /**
     * Fills the enchantment list with existing enchantments from the target item.
     * <p>
     * This is Priority 1. These enchantments are added to the list first,
     * and their source is marked as {@link #TARGET}.
     *
     * @param curseFreeTarget   The target item (with curses removed to prevent upgrading them).
     * @param list              The master list of enchantments to populate.
     * @param arraySize         The maximum number of slots (e.g., 3).
     * @param addedEnchantments A set used to track added enchantments to prevent duplicates.
     */
    @Unique
    private void readEnchantmentsFromTarget(ItemStack curseFreeTarget, List<EnchantmentInstance> list, int arraySize, Set<Enchantment> addedEnchantments) {
        if (curseFreeTarget.isEnchanted()) {
            ItemEnchantments enchants = EnchantmentLib.getEnchantments(curseFreeTarget);

            for (var entry : enchants.entrySet()) {
                if (list.size() >= arraySize) break; // Stop if slots are full

                Enchantment enchantment = entry.getKey().value();
                int level = entry.getIntValue();

                // Add to list, mark in set, and set source
                list.add(new EnchantmentInstance(enchantment, level));
                addedEnchantments.add(enchantment);
                this.enchantmentSource[list.size() - 1] = TARGET;
            }
        }
    }

    /**
     * Checks the source item for enchantments that are a higher level than
     * enchantments already on the target item and overwrites them in the list.
     * <p>
     * This is Priority 2. It allows a source item to upgrade an existing
     * enchantment even if all 3 slots are already full.
     *
     * @param target            The target item (for compatibility check).
     * @param source            The source item.
     * @param list              The master list (already populated with TARGET enchants).
     * @param addedEnchantments The set of enchants already processed.
     */
    @Unique
    private void overwriteTargetEnchantmentsFromSource(ItemStack target, ItemStack source, List<EnchantmentInstance> list, Set<Enchantment> addedEnchantments) {
        // Only proceed if a source item exists and target is not a book
        if (source.isEmpty() || target.is(Items.BOOK)) {
            return;
        }

        List<EnchantmentInstance> sourceEnchantments = EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source));

        for (EnchantmentInstance sourceEntry : sourceEnchantments) {
            Enchantment sourceEnchant = sourceEntry.enchantment;
            int sourceLevel = sourceEntry.level;

            // Only check enchantments that are already on the target
            if (addedEnchantments.contains(sourceEnchant) && sourceEnchant.canEnchant(target)) {

                // Find the matching enchantment in the list
                for (int i = 0; i < list.size(); i++) {
                    EnchantmentInstance targetEntry = list.get(i);
                    if (targetEntry.enchantment.equals(sourceEnchant)) {
                        int targetLevel = targetEntry.level;

                        // If source is strictly higher, replace it in the list
                        if (sourceLevel > targetLevel) {
                            list.set(i, sourceEntry); // Replace with the source (higher level) entry
                            this.enchantmentSource[i] = SOURCE; // Mark the source as SOURCE
                        }
                        break; // Found the matching enchant, move to the next source enchant
                    }
                }
            }
        }
    }

    /**
     * Fills the *remaining* enchantment slots with *new*, applicable enchantments
     * from the source item.
     * <p>
     * This is Priority 3. Enchantments are added only if they are compatible
     * with the target item and have not already been added from the target itself.
     * Their source is marked as {@link #SOURCE}.
     *
     * @param target            The target item (used to check compatibility).
     * @param source            The source item (e.g., Enchanted Book, Tome).
     * @param list              The master list of enchantments to populate.
     * @param arraySize         The maximum number of slots (e.g., 3).
     * @param addedEnchantments A set used to track added enchantments to prevent duplicates.
     */
    @Unique
    private void appendSourceEnchantments(ItemStack target, ItemStack source, List<EnchantmentInstance> list, int arraySize, Set<Enchantment> addedEnchantments) {
        // Only proceed if we have space, a source item exists, and target is not a book
        if (list.size() >= arraySize || source.isEmpty() || target.is(Items.BOOK)) {
            return;
        }

        List<EnchantmentInstance> sourceEnchantments = EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source));

        for (EnchantmentInstance entry : sourceEnchantments) {
            if (list.size() >= arraySize) break; // Stop if slots are full

            Enchantment sourceEnchant = entry.enchantment;

            // Check if acceptable AND not already in the list (using the Set)
            // .add() returns true if the item was successfully added (i.e., not a duplicate)
            if (sourceEnchant.canEnchant(target) && addedEnchantments.add(sourceEnchant)) {
                list.add(entry);
                this.enchantmentSource[list.size() - 1] = SOURCE;
            }
        }
    }

    /**
     * Fills the remaining enchantment slots with newly generated enchantments from the table.
     * <p>
     * This is Priority 4. It uses the standard table logic (via EnchantmentLib) to
     * generate potential enchantments, then fills any empty slots in our list,
     * ensuring no duplicates are added.
     */
    @Unique
    private void generateEnchantmentsFromTable(ItemStack target, FeatureFlagSet featureSet, List<EnchantmentInstance> list, int arraySize, Level world, BlockPos pos) {
        // Only proceed if we have space
        if (list.size() < arraySize) {
            int slotToFill = list.size();

            // Generate new enchantments from the table using custom logic
            List<EnchantmentInstance> generated = this.generateEnchantments(featureSet, target, world, pos);

            // Filter out enchantments that are already in our list
            generated.removeIf(generatedEntry -> {
                for (EnchantmentInstance existingEntry : list) {
                    if (existingEntry.enchantment.equals(generatedEntry.enchantment)) {
                        return true;
                    }
                }
                return false;
            });

            int generatedIndex = 0;
            for (int i = slotToFill; i < arraySize; i++) {
                // Check if there are still unique, generated enchantments available
                if (generatedIndex < generated.size()) {
                    // Get the next available enchantment in order
                    EnchantmentInstance candidate = generated.get(generatedIndex);
                    generatedIndex++;

                    // Add it to the list
                    list.add(candidate);

                    // Set the source ID for this slot
                    this.enchantmentSource[i] = TABLE;
                } else {
                    // Stop filling if we run out of generated enchantments
                    break;
                }
            }
        }
    }
    // endregion

    // region Level calculation
    /**
     * Calculates the resulting level of an enchantment when upgrading.
     * <p>
     * This uses a probabilistic system based on item enchantability. For each potential
     * level increase (up to the max), a "roll" is performed. A higher enchantability
     * gives a higher chance of success for each roll.
     *
     * @param target The item being enchanted.
     * @param enchantment The enchantment being upgraded.
     * @param currentLevel The current level of the enchantment.
     * @return The new, potentially higher, level.
     */
    @Unique
    public int rollLevel(ItemStack target, Enchantment enchantment, int currentLevel) {
        // 1. Clamp enchantability between 1 and 50.
        int enchantability = Math.clamp(target.getItem().getEnchantmentValue(), 1, 50);

        // 2. Calculate success probability.
        // This scales enchantability (1-50) to a 0.0-1.0 range, then doubles its square
        // to widen the gap between enchantability scores.
        double successProbability = Math.clamp(2.0D * Math.pow(enchantability / 50.0D, 2.0D), 0.0D, 1.0D);

        int newLevel = currentLevel;

        // 3. Loop *while* we are below the max level.
        while (newLevel < enchantment.getMaxLevel()) {
            // 4. Roll the dice. A low roll (less than probability) is a success.
            if (this.random.nextDouble() < successProbability) {
                newLevel++; // Success, Try for next level
            } else {
                break; // Failure, stop trying and exit
            }
        }

        return newLevel;
    }
    // endregion

    // region Click handling
    /**
     * Overrides the default button click logic to handle the new enchanting system.
     * <p>
     * This method handles four distinct actions:
     * <ol>
     * <li><b>Upgrade (Button 0-2):</b> If the clicked slot corresponds to an existing enchantment
     * (source == {@link #TARGET}), it attempts to upgrade it using {@link #rollLevel}.</li>
     * <li><b>Transfer (Button 0-2):</b> If the slot is for an enchantment from a source item
     * (source == {@link #SOURCE}), it transfers it to the target.</li>
     * <li><b>Apply (Button 0-2):</b> If the slot is for a new, table-generated enchantment
     * (source == {@link #TABLE}), it applies it to the target.</li>
     * <li><b>Reroll (Button 3):</b> If button 3 is clicked, it consumes lapis and experience
     * to reroll the enchantment options.</li>
     * <li><b>Invalid:</b> If none of the above, or costs are not met, do nothing.</li>
     * </ol>
     *
     * @param player The player who clicked.
     * @param buttonId The ID of the button pressed (0-2 for options, 3 for reroll).
     * @param cir Callback info (to set the return value and cancel).
     */
    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    public void clickMenuButton(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        ItemStack target = this.enchantSlots.getItem(0);
        ItemStack lapis = this.enchantSlots.getItem(1);
        ItemStack source = this.enchantSlots.getItem(2);
        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);

        int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

        // Determine if this is a standard button or reroll
        Optional<Enchantment> enchantment = buttonId < REROLL_BUTTON_INDEX ? Optional.ofNullable(BuiltInRegistries.ENCHANTMENT.byId(this.enchantClue[buttonId])) : Optional.empty();

        int rerollCost = occupiedSlots + 1;
        int buttonCost = enchantment.map(this::calculateEnchantmentCost).orElse(rerollCost);
        int i = buttonId + 1; // Used for stats/criteria

        // Invalid States / Cost Checks
        boolean isNotCreativePlayer = !player.getAbilities().instabuild;
        boolean isNotEnchantableTarget = !target.isEnchantable();
        boolean isTargetEmpty = target.isEmpty();

        int currentLapis = lapis.getCount();
        boolean isLapisInsufficient = currentLapis < buttonCost;
        boolean isLevelInsufficient = buttonId < REROLL_BUTTON_INDEX ? player.experienceLevel < this.costs[buttonId] : player.experienceLevel < rerollCost;

        if ((isNotCreativePlayer && (isLapisInsufficient || isLevelInsufficient))
                || isTargetEmpty
                || isNotEnchantableTarget
        ) {
            cir.setReturnValue(false);
            return;
        }

        // Delegate to specific handlers
        if (this.upgradeEnchantment(player, buttonId, cir, target, isNotCreativePlayer, lapis, i)) return;
        if (this.transferEnchantment(player, buttonId, cir, target, source, isNotCreativePlayer, lapis, i)) return;
        if (this.applyEnchantment(player, buttonId, cir, target, isNotCreativePlayer, lapis, i)) return;
        if (this.rerollEnchantments(player, buttonId, cir, target, occupiedSlots, isLapisInsufficient, isLevelInsufficient, isNotCreativePlayer, lapis, rerollCost)) return;

        // Fallthrough (Invalid Click)
        cir.setReturnValue(false);
    }

    /**
     * Handles the logic for upgrading an existing enchantment on the target item.
     * Triggered when the clicked slot's source is {@link #TARGET}.
     *
     * @return true if the click was handled (or explicitly ignored as invalid), false otherwise.
     */
    @Unique
    private boolean upgradeEnchantment(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, boolean isNotCreativePlayer, ItemStack lapis, int i) {
        boolean isUpgradeButton = buttonId < REROLL_BUTTON_INDEX && this.enchantmentSource[buttonId] == TARGET;

        if (isUpgradeButton) {
            Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.byId(this.enchantClue[buttonId]);
            int currentLevel = this.levelClue[buttonId]; // Get CURRENT level

            // Exit if enchantment is already at max level
            if (enchantment != null && currentLevel >= enchantment.getMaxLevel()) {
                cir.setReturnValue(false);
                return true; // Action was "handled" by doing nothing
            }

            this.access.execute((world, pos) -> {
                if (enchantment == null) return;

                // Calculate the new level. Calling with currentLevel + 1
                // guarantees an increase of at least one level, with a
                // chance to roll for more via rollLevel's logic.
                int newLevel = this.rollLevel(target, enchantment, currentLevel + 1);

                // Create a new component builder from the existing enchantments
                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentLib.getEnchantments(target));
                builder.set(enchantment, newLevel);
                target.set(DataComponents.ENCHANTMENTS, builder.toImmutable());

                // Apply costs
                int experienceLevelCost = this.calculateEnchantmentCost(enchantment);
                player.onEnchantmentPerformed(target, isNotCreativePlayer ? experienceLevelCost : 0);

                if (isNotCreativePlayer) {
                    lapis.shrink(i); // Upgrade Lapis cost uses 'i' (buttonId + 1)
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }

                // Stats, criteria, and client/sound updates
                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, i);
                }

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots); // Refresh enchantment options
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });

            cir.setReturnValue(true);
            return true;
        }
        return false;
    }

    /**
     * Handles the logic for transferring an enchantment from the source item.
     * Triggered when the clicked slot's source is {@link #SOURCE}.
     *
     * @return true if the click was handled, false otherwise.
     */
    @Unique
    private boolean transferEnchantment(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, ItemStack source, boolean isNotCreativePlayer, ItemStack lapis, int i) {
        boolean isTransferButton = buttonId < REROLL_BUTTON_INDEX && this.enchantmentSource[buttonId] == SOURCE;

        if (isTransferButton) {
            this.access.execute((world, pos) -> {
                ItemStack targetCopy = target;
                Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.byId(this.enchantClue[buttonId]);
                if (enchantment == null) return;

                // Roll for the final level
                int level = this.rollLevel(target, enchantment, this.levelClue[buttonId]);

                boolean isTargetBook = target.is(Items.BOOK);
                boolean isSourcePersistent = source.is(Services.PLATFORM.getEnchantedTome());

                // If enchanting a book, swap it for an enchanted book
                if (isTargetBook) {
                    targetCopy = new ItemStack(Items.ENCHANTED_BOOK);
                    targetCopy.setCount(1);
                    this.enchantSlots.setItem(0, targetCopy);
                }

                // Apply the new enchantment
                targetCopy.enchant(enchantment, level);

                // Apply costs
                int experienceCost = this.calculateEnchantmentCost(enchantment);
                if (isNotCreativePlayer) {
                    player.onEnchantmentPerformed(target, experienceCost);
                    lapis.shrink(experienceCost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                    // If the enchantment came from the source slot, consume the source item
                    // unless it is a persistent item (like the custom Tome)
                    if (!isSourcePersistent) {
                        this.enchantSlots.setItem(2, new ItemStack(Items.BOOK));
                    }
                }

                // Stats, criteria, and client/sound updates
                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, i);
                }

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });

            cir.setReturnValue(true);
            return true;
        }
        return false;
    }

    /**
     * Handles the logic for applying a new, table-generated enchantment.
     * Triggered when the clicked slot's source is {@link #TABLE}.
     *
     * @return true if the click was handled, false otherwise.
     */
    @Unique
    private boolean applyEnchantment(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, boolean isNotCreativePlayer, ItemStack lapis, int i) {
        boolean isApplyButton = buttonId < REROLL_BUTTON_INDEX && this.enchantmentSource[buttonId] == TABLE;

        if (isApplyButton) {
            this.access.execute((world, pos) -> {
                ItemStack targetCopy = target;
                Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.byId(this.enchantClue[buttonId]);
                if (enchantment == null) return;

                int level = this.rollLevel(target, enchantment, this.levelClue[buttonId]);
                boolean isTargetBook = target.is(Items.BOOK);

                if (isTargetBook) {
                    targetCopy = new ItemStack(Items.ENCHANTED_BOOK);
                    targetCopy.setCount(1);
                    this.enchantSlots.setItem(0, targetCopy);
                }

                targetCopy.enchant(enchantment, level);

                int experienceCost = this.calculateEnchantmentCost(enchantment);
                if (isNotCreativePlayer) {
                    player.onEnchantmentPerformed(target, experienceCost);
                    lapis.shrink(experienceCost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, i);
                }

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });

            cir.setReturnValue(true);
            return true;
        }
        return false;
    }

    /**
     * Handles the logic for rerolling the available enchantments.
     * Triggered when the clicked slot is {@link #REROLL_BUTTON_INDEX}.
     *
     * @return true if the click was handled, false otherwise.
     */
    @Unique
    private boolean rerollEnchantments(Player player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, int occupiedSlots, boolean isLapisInsufficient, boolean isLevelInsufficient, boolean isNotCreativePlayer, ItemStack lapis, int rerollCost) {
        boolean isRerollButton = buttonId == REROLL_BUTTON_INDEX;
        boolean targetIsEmpty = target.isEmpty();
        boolean targetIsEnchantable = !targetIsEmpty && (target.is(Items.BOOK) || target.isEnchantable());
        // Logic to determine if a reroll is allowed
        boolean targetIsSourceEnchantable = Arrays.stream(this.enchantmentSource).anyMatch(element -> element == SOURCE);
        boolean hasTableSource = Arrays.stream(this.enchantmentSource).anyMatch(source -> source == TABLE);
        boolean canReroll = occupiedSlots < REROLL_BUTTON_INDEX && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;
        boolean canAfford = !((isLapisInsufficient || isLevelInsufficient) && isNotCreativePlayer);

        if (isRerollButton && canReroll && canAfford) {
            this.access.execute((world, pos) -> {
                if (isNotCreativePlayer) {
                    lapis.shrink(rerollCost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }
                // Apply experience cost
                player.onEnchantmentPerformed(ItemStack.EMPTY, isNotCreativePlayer ? rerollCost : 0);

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots); // Refresh enchantment options
                world.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });

            cir.setReturnValue(true);
            return true;
        }
        return false;
    }
    // endregion

    // region Inventory handling
    /**
     * Overrides the default quick-move (shift-click) logic to work with the new 3-slot layout.
     * <p>
     * Slot Map:
     * <ul>
     * <li>0: Target Item</li>
     * <li>1: Lapis Lazuli</li>
     * <li>2-28: Player Inventory (27 slots)</li>
     * <li>29-37: Player Hotbar (9 slots)</li>
     * <li>38: Source Item (newly added slot)</li>
     * </ul>
     *
     * @param player The player who shift-clicked.
     * @param slotIndex The index of the slot that was clicked.
     * @param cir Callback info (to set the return value and cancel).
     */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    public void quickMoveStack(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            // Case 1: Clicked *from* one of the 3 special slots (0, 1, or 38)
            if (slotIndex == 0 || slotIndex == 1 || slotIndex == 38) {
                // Try to move to inventory/hotbar (slots 2-37)
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            }
            // Case 2: Clicked *from* the inventory/hotbar (slots 2-37)
            else {
                // Try to move to Lapis Slot (1)
                if (itemStack2.is(Items.LAPIS_LAZULI)) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
                // Try to move to Source Slot (38)
                else if (this.slots.get(38).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 38, 39, true)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
                // Try to move to Target Slot (0)
                else if (this.slots.get(0).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, true)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
                // If it couldn't be moved to a special slot, handle inventory/hotbar swapping
                else if (slotIndex >= 2 && slotIndex < 29) { // From main inventory
                    if (!this.moveItemStackTo(itemStack2, 29, 38, false)) { // To hotbar
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                } else if (slotIndex >= 29 && slotIndex < 38) { // From hotbar
                    if (!this.moveItemStackTo(itemStack2, 2, 29, false)) { // To main inventory
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            slot.onTake(player, itemStack2);
        }

        cir.setReturnValue(itemStack);
    }
    // endregion

    // region Accessor Implementations
    @Unique
    @Override
    public ItemStack getEnchantmentTarget() {
        return this.enchantSlots.getItem(0);
    }

    @Unique
    @Override
    public ItemStack getEnchantmentSource() {
        return this.enchantSlots.getItem(2);
    }

    @Unique
    @Override
    public int[] getEnchantmentSourceArray() {
        return this.enchantmentSource;
    }

    @Unique
    @Override
    public int[] getTargetTextureIndices() {
        return this.targetTextureIndices;
    }

    @Unique
    @Override
    public int[] getSourceTextureIndices() {
        return this.sourceTextureIndices;
    }

    @Unique
    @Override
    public int[] getTableTextureIndices() {
        return this.tableTextureIndices;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calculates the experience <i>cost</i> (not level requirement) for applying
     * or upgrading an enchantment.
     * <ul>
     * <li>Max Level 1 enchantments (e.g., Curses, Silk Touch) cost 3 XP.</li>
     * <li>Others cost 1, 2, or 3 based on their weight (rarity).</li>
     * </ul>
     */
    @Unique
    @Override
    public int calculateEnchantmentCost(Enchantment enchantment) {
        if (enchantment.getMaxLevel() == 1) return 3;
        // Cost is based on rarity (weight mod 3), clamped to a minimum of 1.
        return Math.clamp(enchantment.getWeight() % 4, 1, Integer.MAX_VALUE);
    }
    // endregion
}