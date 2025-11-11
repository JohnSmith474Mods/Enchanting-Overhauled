package johnsmith.mixin.screen;

import johnsmith.accessor.EnchantmentScreenHandlerAccessor;
import johnsmith.item.ItemRegistry;
import johnsmith.lib.EnchantmentLib;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

/**
 * Mixin to {@link EnchantmentScreenHandler} to completely overhaul the enchanting system.
 *
 * <p>This mixin implements the following changes:
 * <ul>
 * <li><b>Adds a Third Slot:</b> An inventory slot (index 2) is added to hold a "source" item
 * (e.g., an Enchanted Book or a custom Tome item).</li>
 * <li><b>Custom GUI Layout:</b> Modifies the coordinates of all slots to fit a new GUI texture.</li>
 * <li><b>New Enchantment Logic ({@link #onContentChanged}):</b>
 * <ul>
 * <li>Completely replaces the vanilla enchantment generation.</li>
 * <li>The three enchantment options are now populated based on a priority system:
 * <ol>
 * <li>Existing enchantments on the target item (for upgrading).</li>
 * <li>Enchantments from the source item (for transferring).</li>
 * <li>New, randomly generated enchantments from the table.</li>
 * </ol>
 * </li>
 * </ul>
 * </li>
 * <li><b>New Button Logic ({@link #onButtonClick}):</b>
 * <ul>
 * <li>Clicking an option corresponding to an existing enchantment will <b>upgrade</b> it.</li>
 * <li>Clicking an option from a source item will <b>transfer</b> it.</li>
 * <li>Clicking a new table enchantment will <b>apply</b> it.</li>
 * <li>A fourth "button" (index 3) is implemented as a <b>reroll</b> function.</li>
 * </ul>
 * </li>
 * <li><b>New Synced Property:</b> Adds {@link #enchantmentSource} as a synced array to inform
 * the client of the origin of each enchantment option (TARGET, SOURCE, or TABLE).</li>
 * <li><b>Probabilistic Upgrading ({@link #rollLevel}):</b> Introduces a system where upgrading
 * an enchantment has a success chance based on the item's enchantability.</li>
 * <li><b>Custom Quick Move:</b> Overrides {@link #quickMove} to handle the new 3-slot layout.</li>
 */
@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantingScreenHandlerMixin extends ScreenHandler implements EnchantmentScreenHandlerAccessor {

    // region Shadow Fields
    @Shadow
    @Final
    private Inventory inventory;
    @Shadow
    public abstract int getLapisCount();
    @Shadow
    @Final
    public int[] enchantmentPower;
    @Shadow
    @Final
    private ScreenHandlerContext context;
    @Shadow
    @Final
    private Property seed;
    @Shadow
    public abstract void onContentChanged(Inventory inventory);
    @Shadow
    protected abstract List<EnchantmentLevelEntry> generateEnchantments(FeatureSet enabledFeatures, ItemStack stack, int slot, int level);
    @Shadow
    @Final
    private Random random;
    @Shadow
    @Final
    public int[] enchantmentId;
    @Shadow
    @Final
    public int[] enchantmentLevel;
    // endregion

    // region Constants
    @Unique
    private static final int TARGET_X_POSITION = 18;
    @Unique
    private static final int TARGET_Y_POSITION = 46 - 4;
    @Unique
    private static final int LAPIS_X_POSITION = 28;
    @Unique
    private static final int LAPIS_Y_POSITION = 73 - 8;
    @Unique
    private static final int SOURCE_X_POSITION = LAPIS_X_POSITION - 18 - 2;
    @Unique
    private static final int SOURCE_Y_POSITION = LAPIS_Y_POSITION;
    @Unique
    private static final int INVENTORY_Y_POSITION = 113;
    @Unique
    private static final int HOTBAR_Y_POSITION = 171;
    // endregion

    // region New Fields
    @Unique
    private static final Random TEXTURE_RANDOM = Random.create();
    /**
     * Tracks the origin of the enchantment in the corresponding slot (0, 1, or 2).
     * The value corresponds to one of the static constants (NONE, TARGET, SOURCE, TABLE).
     * This array is synced to the client via {@link #addAdditionalProperties}.
     */
    @Unique
    public int[] enchantmentSource;
    /**
     * Tracks the random texture index (0-9) for the target button in the corresponding slot.
     */
    @Unique
    public final int[] targetTextureIndices = new int[3];

    /**
     * Tracks the random texture index (0-9) for the source button in the corresponding slot.
     */
    @Unique
    public final int[] sourceTextureIndices = new int[3];

    /**
     * Tracks the random texture index (0-9) for the table button in the corresponding slot.
     */
    @Unique
    public final int[] tableTextureIndices = new int[3];

    /** Indicates the enchantment slot is empty or invalid. */
    @Unique
    private static final int NONE = -1;
    /** Indicates the enchantment option comes from the target item itself (an upgrade). */
    @Unique
    private static final int TARGET = 0;
    /** Indicates the enchantment option comes from the source item (a transfer). */
    @Unique
    private static final int SOURCE = 1;
    /** Indicates the enchantment option is newly generated by the table. */
    @Unique
    private static final int TABLE = 2;
    @Unique
    private static final int REROLL_BUTTON_INDEX = 3;
    // endregion

    // region Constructor
    /**
     * Internal constructor matching the superclass.
     */
    protected EnchantingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    /**
     * Modifies the handler's internal inventory size from 2 to 3 to accommodate the new source item slot.
     */
    @ModifyConstant(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            constant = @Constant(intValue = 2, ordinal = 0))
    private int modifyInventorySize(int originalSize) {
        return 3;
    }

    /**
     * Injects at the end of the constructor to:
     * <ol>
     * <li>Add the new {@link Slot} for the source item (inventory index 2, slot list index 38).</li>
     * <li>Initialize the {@link #enchantmentSource} array.</li>
     * <li>Register the {@link #enchantmentSource} array elements as synced properties.</li>
     * </ol>
     */
    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At("RETURN"))
    private void addAdditionalProperties(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        // Add book slot
        this.addSlot(new Slot(this.inventory, 2, SOURCE_X_POSITION, SOURCE_Y_POSITION) {
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof EnchantedBookItem;
            }
        });

        this.enchantmentSource = new int[]{NONE, NONE, NONE};

        // Add source array
        this.addProperty(Property.create(this.enchantmentSource, 0));
        this.addProperty(Property.create(this.enchantmentSource, 1));
        this.addProperty(Property.create(this.enchantmentSource, 2));
        // Add texture index properties
        this.addProperty(Property.create(this.targetTextureIndices, 0));
        this.addProperty(Property.create(this.targetTextureIndices, 1));
        this.addProperty(Property.create(this.targetTextureIndices, 2));
        this.addProperty(Property.create(this.sourceTextureIndices, 0));
        this.addProperty(Property.create(this.sourceTextureIndices, 1));
        this.addProperty(Property.create(this.sourceTextureIndices, 2));
        this.addProperty(Property.create(this.tableTextureIndices, 0));
        this.addProperty(Property.create(this.tableTextureIndices, 1));
        this.addProperty(Property.create(this.tableTextureIndices, 2));
    } // endregion

    // region GUI Position Modifiers
    /**
     * Redirects the second call to addSlot() in the EnchantmentScreenHandler constructor
     * to replace the lapis slot with a version that does not override getBackgroundSprite().
     */
    @Redirect(
            method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/EnchantmentScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
                    ordinal = 1
            )
    )
    private Slot modifyLapisSlot(EnchantmentScreenHandler instance, Slot originalSlot) {
        // Create the new slot as requested, using the shadowed inventory field
        // This new anonymous class only overrides canInsert()
        Slot newLapisSlot = new Slot(this.inventory, 1, LAPIS_X_POSITION, LAPIS_Y_POSITION) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }
        };

        // Call the original addSlot method (via the instance) with our new slot
        // This effectively replaces the original slot with our new one
        return this.addSlot(newLapisSlot);
    }

    /**
     * Modifies the X-coordinate for the target item slot.
     */
    @ModifyConstant(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            constant = @Constant(intValue = 15, ordinal = 0))
    private int modifyTargetXPosition(int originalX) {
        return TARGET_X_POSITION;
    }

    /**
     * Modifies the Y-coordinate for the target item slot.
     */
    @ModifyConstant(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            constant = @Constant(intValue = 47, ordinal = 0))
    private int modifyTargetYPosition(int originalY) {
        return TARGET_Y_POSITION;
    }

    /**
     * Modifies the Y-coordinate for the player's main inventory.
     */
    @ModifyConstant(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            constant = @Constant(intValue = 84, ordinal = 0))
    private int modifyInventoryYPosition(int originalY) {
        return INVENTORY_Y_POSITION;
    }

    /**
     * Modifies the Y-coordinate for the player's hotbar.
     */
    @ModifyConstant(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            constant = @Constant(intValue = 142, ordinal = 0))
    private int modifyHotbarYPosition(int originalY) {
        return HOTBAR_Y_POSITION;
    }

    /**
     * Helper method to generate a list of new, random enchantments for the target item.
     * This implementation delegates to a custom {@link EnchantmentLib}.
     *
     * @param enabledFeatures The feature set of the world.
     * @param target The item to generate enchantments for.
     */
    @Unique
    private List<EnchantmentLevelEntry> generateEnchantments(FeatureSet enabledFeatures, ItemStack target, World world, BlockPos pos) {
        this.random.setSeed((long)(this.seed.get()));

        return EnchantmentLib.generateEnchantments(enabledFeatures, this.random, target, false, world, pos);
    } // endregion

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
    @Inject(method = "onContentChanged(Lnet/minecraft/inventory/Inventory;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void onContentChanged(Inventory inventory, CallbackInfo ci) {
        // Guard clause: Only run for the handler's own inventory
        if (!(inventory == this.inventory)) {
            ci.cancel();
            return;
        }

        ItemStack target = inventory.getStack(0);
        ItemStack source = inventory.getStack(2);

        // Invalid state: Target item is empty or not enchantable
        if (!(!target.isEmpty() && target.isEnchantable())) {
            for(int i = 0; i < REROLL_BUTTON_INDEX; ++i) {
                // Clear all enchantment options
                this.enchantmentPower[i] = 0;
                this.enchantmentId[i] = NONE;
                this.enchantmentLevel[i] = NONE;
            }
            ci.cancel();
            return;
        }

        // Valid state: Generate new enchantment options
        this.context.run(
                (world, pos) -> {
                    // 1. Calculate enchanting power (custom logic)
                    int power = 0;
                    for (BlockPos providerOffset : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
                        // First, check if the transmitter (air) block is clear
                        if (world.getBlockState(pos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                            // If so, get the power from the provider block itself
                            power += EnchantmentLib.getAgnosticEnchantingPower(world, pos.add(providerOffset));
                        }
                    }

                    // 2. Set base level requirements (vanilla-like)
                    this.random.setSeed((long) this.seed.get());

                    for (int j = 0; j < REROLL_BUTTON_INDEX; ++j) {
                        this.enchantmentPower[j] = (25 - Math.min(power, 24)) * (j + 1);
                        this.enchantmentId[j] = NONE;
                        this.enchantmentLevel[j] = NONE;
                        if (this.enchantmentPower[j] < j + 1) {
                            this.enchantmentPower[j] = 0;
                        }
                    }

                    // 3. Populate enchantment options using the new priority logic
                    List<EnchantmentLevelEntry> enchantments = this.setDataSlots(target, source, world.getEnabledFeatures(), world, pos);

                    // 4. Fill the synced arrays with data from the generated list
                    int slot = 0;
                    for (EnchantmentLevelEntry entry : enchantments) {
                        this.enchantmentId[slot] = Registries.ENCHANTMENT.getRawId(entry.enchantment);
                        this.enchantmentLevel[slot] = entry.level;
                        ++slot;
                    }

                    // 4b. Set random texture indices for client
                    for (int k = 0; k < REROLL_BUTTON_INDEX; ++k) {
                        this.targetTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                        this.sourceTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                        this.tableTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                    }

                    // 5. Send updates to the client
                    this.sendContentUpdates();
                }
        );
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
     * @return A list of {@link EnchantmentLevelEntry}s, max size 3, to be displayed.
     */
    @Unique
    public List<EnchantmentLevelEntry> setDataSlots(ItemStack target, ItemStack source, FeatureSet featureSet, World world, BlockPos pos) {       // A list to hold the final enchantments
        List<EnchantmentLevelEntry> list = new ArrayList<>();
        // A set to efficiently track enchantments already added to avoid duplicates
        Set<Enchantment> addedEnchantments = new HashSet<>();

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int arraySize = this.enchantmentPower.length; // Should be 3

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
     * @param curseFreeTarget     The target item (with curses removed to prevent upgrading them).
     * @param list                The master list of enchantments to populate.
     * @param arraySize           The maximum number of slots (e.g., 3).
     * @param addedEnchantments   A set used to track added enchantments to prevent duplicates.
     */
    @Unique
    private void readEnchantmentsFromTarget(ItemStack curseFreeTarget, List<EnchantmentLevelEntry> list, int arraySize, Set<Enchantment> addedEnchantments) {
        // 1. Get enchantments from target (for upgrading)
        if (curseFreeTarget.hasEnchantments()) {
            ItemEnchantmentsComponent enchants = curseFreeTarget.getEnchantments();
            // Iterate over the item's enchantments
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : enchants.getEnchantmentsMap()) {
                if (list.size() >= arraySize) break; // Stop if slots are full

                Enchantment enchantment = entry.getKey().value();
                int level = entry.getValue();

                // Add to list, mark in set, and set source
                list.add(new EnchantmentLevelEntry(enchantment, level));
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
    private void overwriteTargetEnchantmentsFromSource(ItemStack target, ItemStack source, List<EnchantmentLevelEntry> list, Set<Enchantment> addedEnchantments) {
        // Only proceed if a source item exists and target is not a book
        if (source.isEmpty() || target.isOf(Items.BOOK)) {
            return;
        }

        List<EnchantmentLevelEntry> sourceEnchantments = EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source));

        for (EnchantmentLevelEntry sourceEntry : sourceEnchantments) {
            Enchantment sourceEnchant = sourceEntry.enchantment;
            int sourceLevel = sourceEntry.level;

            // Only check enchantments that are already on the target
            if (addedEnchantments.contains(sourceEnchant) && sourceEnchant.isAcceptableItem(target)) {

                // Find the matching enchantment in the list
                for (int i = 0; i < list.size(); i++) {
                    EnchantmentLevelEntry targetEntry = list.get(i);
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
     * @param target              The target item (used to check compatibility).
     * @param source              The source item (e.g., Enchanted Book, Tome).
     * @param list                The master list of enchantments to populate.
     * @param arraySize           The maximum number of slots (e.g., 3).
     * @param addedEnchantments   A set used to track added enchantments to prevent duplicates.
     */
    @Unique
    private void appendSourceEnchantments(ItemStack target, ItemStack source, List<EnchantmentLevelEntry> list, int arraySize, Set<Enchantment> addedEnchantments) {
        // Only proceed if we have space, a source item exists, and target is not a book
        if (list.size() >= arraySize || source.isEmpty() || target.isOf(Items.BOOK)) {
            return;
        }

        List<EnchantmentLevelEntry> sourceEnchantments = EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source));

        for (EnchantmentLevelEntry entry : sourceEnchantments) {
            if (list.size() >= arraySize) break; // Stop if slots are full

            Enchantment sourceEnchant = entry.enchantment;

            // Check if acceptable AND not already in the list (using the Set)
            // .add() returns true if the item was successfully added (i.e., not a duplicate)
            if (sourceEnchant.isAcceptableItem(target) && addedEnchantments.add(sourceEnchant)) {
                list.add(entry);
                this.enchantmentSource[list.size() - 1] = SOURCE;
            }
        }
    }

    /**
     * Fills the remaining enchantment slots with newly generated enchantments from the table.
     * (Full method Javadoc is correct)
     */
    @Unique
    private void generateEnchantmentsFromTable(ItemStack target, FeatureSet featureSet, List<EnchantmentLevelEntry> list, int arraySize, World world, BlockPos pos) {
        // 3. Generate new enchantments from the table
        // Only proceed if we have space
        if (list.size() < arraySize) {
            int slotToFill = list.size(); // The index we are trying to fill

            // MODIFIED: Pass world and pos
            List<EnchantmentLevelEntry> generated = this.generateEnchantments(featureSet, target, world, pos);

            generated.removeIf(generatedEntry -> {
                // Iterate through the existing enchantments
                for (EnchantmentLevelEntry existingEntry : list) {
                    // If the enchantment types match, return true to remove it
                    if (existingEntry.enchantment.equals(generatedEntry.enchantment)) {
                        return true;
                    }
                }
                // No match found, return false to keep it
                return false;
            });

            int generatedIndex = 0;
            for (int i = slotToFill; i < arraySize; i++) {
                // Check if there are still unique, generated enchantments available
                if (generatedIndex < generated.size()) {
                    // Get the next available enchantment in order
                    EnchantmentLevelEntry candidate = generated.get(generatedIndex);
                    generatedIndex++; // Consume this enchantment

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
    } // endregion

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
        int enchantability = Math.clamp(target.getItem().getEnchantability(), 1, 50);

        // 2. Calculate success probability.
        // This scales enchantability (1-50) to a 0.0-1.0 range, then doubles its square
        // to widen the gap between enchantability scores.
        // E.g: e=15 -> 2(15/50)^2 = 18% success chance i.e. for netherite tools
        //      e=25 -> 2(25/50)^2 = 50% success chance i.e. for golden armor
        //      e=50 -> 2(50/50)^2 = 100% success chance
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
    } // endregion

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
    @Inject(method = "onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z",
            at = @At("HEAD"),
            cancellable = true)
    public void onButtonClick(PlayerEntity player, int buttonId, CallbackInfoReturnable<Boolean> cir) {
        ItemStack target = this.inventory.getStack(0);
        ItemStack lapis = this.inventory.getStack(1);
        ItemStack source = this.inventory.getStack(2);
        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);

        int occupiedSlots = curseFreeTarget.getEnchantments().getSize();
        Optional<Enchantment> enchantment = buttonId < REROLL_BUTTON_INDEX ? Optional.of(Enchantment.byRawId(buttonId)) : Optional.empty();
        int rerollCost = occupiedSlots + 1;
        int buttonCost = enchantment.map(this::calculateEnchantmentCost).orElse(rerollCost);
        int i = buttonId + 1; // Used for stats/criteria

        // Invalid States / Cost Checks
        boolean isNotCreativePlayer = !player.isCreative();
        boolean isNotEnchantableTarget = !target.getItem().isEnchantable(target);
        boolean isTargetEmpty = target.isEmpty();
        // Check costs for either enchanting (button 0-2) or rerolling (button 3)
        boolean isLapisInsufficient = this.getLapisCount() < buttonCost;
        boolean isLevelInsufficient = buttonId < REROLL_BUTTON_INDEX ? player.experienceLevel < this.enchantmentPower[buttonId] : player.experienceLevel < rerollCost;

        if ((isNotCreativePlayer && (isLapisInsufficient || isLevelInsufficient))
                || isTargetEmpty
                || isNotEnchantableTarget
        ) {
            cir.setReturnValue(false);
            return;
        }

        // Handle different button actions based on source

        // 1. Upgrade Enchantment
        if (this.upgradeEnchantment(player, buttonId, cir, target, isNotCreativePlayer, lapis, i)) {
            return;
        }

        // 2. Transfer Enchantment
        if (this.transferEnchantment(player, buttonId, cir, target, source, isNotCreativePlayer, lapis, i)) {
            return;
        }

        // 3. Apply Enchantment
        if (this.applyEnchantment(player, buttonId, cir, target, isNotCreativePlayer, lapis, i)) {
            return;
        }

        // 4. Reroll Enchantments
        if (this.rerollEnchantments(player, buttonId, cir, target, occupiedSlots, isLapisInsufficient, isLevelInsufficient, isNotCreativePlayer, lapis, rerollCost)) {
            return;
        }

        // Fallthrough (Invalid Click)
        cir.setReturnValue(false); // No valid action was taken
    }

    /**
     * Handles the logic for upgrading an existing enchantment on the target item.
     * Triggered when the clicked slot's source is {@link #TARGET}.
     * <p>
     * This method corrects logical errors from the provided refactor, such as
     * the double call to {@code rollLevel} and ensures the check is based on
     * the enchantment source, not just slot occupancy.
     *
     * @return true if the click was handled (or explicitly ignored as invalid), false otherwise.
     */
    @Unique
    private boolean upgradeEnchantment(PlayerEntity player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, boolean isNotCreativePlayer, ItemStack lapis, int i) {
        // Check if this is an upgrade action
        boolean isUpgradeButton = buttonId < REROLL_BUTTON_INDEX && this.enchantmentSource[buttonId] == TARGET;

        if (isUpgradeButton) {
            Enchantment enchantment = Enchantment.byRawId(this.enchantmentId[buttonId]);
            int currentLevel = this.enchantmentLevel[buttonId]; // Get CURRENT level

            // Exit if enchantment is already at max level
            assert enchantment != null;
            if (currentLevel >= enchantment.getMaxLevel()) {
                cir.setReturnValue(false);
                return true; // Action was "handled" by doing nothing
            }

            // Execute enchantment upgrade logic
            this.context.run(
                    (world, pos) -> {
                        // Calculate the new level. Calling with currentLevel + 1
                        // guarantees an increase of at least one level, with a
                        // chance to roll for more via rollLevel's logic.
                        int newLevel = this.rollLevel(target, enchantment, currentLevel + 1);

                        // Create a new component builder from the existing enchantments
                        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(target.getEnchantments());
                        builder.set(enchantment, newLevel);
                        target.set(DataComponentTypes.ENCHANTMENTS, builder.build());

                        // Calculate experience level cost
                        int experienceLevelCost = this.calculateEnchantmentCost(enchantment);

                        // Note: Upgrade cost is applied regardless of creative mode (0 if creative)
                        player.applyEnchantmentCosts(target, isNotCreativePlayer ? experienceLevelCost : 0);

                        if (isNotCreativePlayer) {
                            // Upgrade Lapis cost uses 'i' (buttonId + 1)
                            lapis.decrement(i);
                            if (lapis.isEmpty()) {
                                this.inventory.setStack(1, ItemStack.EMPTY);
                            }
                        }

                        // Stats, criteria, and client/sound updates
                        player.incrementStat(Stats.ENCHANT_ITEM);
                        if (player instanceof ServerPlayerEntity) {
                            // Criteria uses 'i'
                            Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, target, i);
                        }

                        this.inventory.markDirty();
                        this.seed.set(player.getEnchantmentTableSeed());
                        this.onContentChanged(this.inventory); // Refresh enchantment options
                        world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                    }
            );

            cir.setReturnValue(true); // Click was successful
            return true;
        }
        return false; // Not an upgrade button
    }

    /**
     * Handles the logic for transferring an enchantment from the source item.
     * Triggered when the clicked slot's source is {@link #SOURCE}.
     *
     * @return true if the click was handled, false otherwise.
     */
    @Unique
    private boolean transferEnchantment(PlayerEntity player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, ItemStack source, boolean isNotCreativePlayer, ItemStack lapis, int i) {
        boolean isTransferButton = buttonId < REROLL_BUTTON_INDEX && this.enchantmentSource[buttonId] == SOURCE;

        if (isTransferButton) {
            this.context.run(
                    (world, pos) -> {
                        ItemStack targetCopy = target;
                        Enchantment enchantment = Enchantment.byRawId(this.enchantmentId[buttonId]);
                        // Roll for the final level. For new enchants, this can roll *up* from the base level.
                        assert enchantment != null;
                        int level = this.rollLevel(target, enchantment, this.enchantmentLevel[buttonId]);

                        boolean isTargetBook = target.isOf(Items.BOOK);
                        boolean isSourcePersistent = source.isOf(ItemRegistry.ENCHANTED_TOME);

                        // If enchanting a book, swap it for an enchanted book
                        if (isTargetBook) {
                            targetCopy = target.copyComponentsToNewStack(Items.ENCHANTED_BOOK, 1);
                            this.inventory.setStack(0, targetCopy);
                        }

                        // Apply the new enchantment
                        targetCopy.addEnchantment(enchantment, level);

                        // Apply costs
                        int experienceCost = this.calculateEnchantmentCost(enchantment);
                        if (isNotCreativePlayer) {
                            // Note: Apply/Transfer cost is only applied if not in creative
                            player.applyEnchantmentCosts(target, experienceCost);
                            // Apply/Transfer Lapis cost uses enchantmentPower
                            lapis.decrement(experienceCost);
                            if (lapis.isEmpty()) {
                                this.inventory.setStack(1, ItemStack.EMPTY);
                            }
                            // If the enchantment came from the source slot, consume the source item
                            if (!isSourcePersistent) {
                                this.inventory.setStack(2, new ItemStack(Items.BOOK));
                            }
                        }

                        // Stats, criteria, and client/sound updates
                        player.incrementStat(Stats.ENCHANT_ITEM);
                        if (player instanceof ServerPlayerEntity) {
                            Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, target, i); // Uses i
                        }

                        this.inventory.markDirty();
                        this.seed.set(player.getEnchantmentTableSeed());
                        this.onContentChanged(this.inventory);
                        world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                    }
            );

            cir.setReturnValue(true); // Click was successful
            return true;
        }
        return false; // Not a transfer button
    }

    /**
     * Handles the logic for applying a new, table-generated enchantment.
     * Triggered when the clicked slot's source is {@link #TABLE}.
     *
     * @return true if the click was handled, false otherwise.
     */
    @Unique
    private boolean applyEnchantment(PlayerEntity player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, boolean isNotCreativePlayer, ItemStack lapis, int i) {
        boolean isApplyButton = buttonId < REROLL_BUTTON_INDEX && this.enchantmentSource[buttonId] == TABLE;

        if (isApplyButton) {
            this.context.run(
                    (world, pos) -> {
                        ItemStack targetCopy = target;
                        Enchantment enchantment = Enchantment.byRawId(this.enchantmentId[buttonId]);
                        // Roll for the final level. For new enchants, this can roll *up* from the base level.
                        assert enchantment != null;
                        int level = this.rollLevel(target, enchantment, this.enchantmentLevel[buttonId]);

                        boolean isTargetBook = target.isOf(Items.BOOK);

                        // If enchanting a book, swap it for an enchanted book
                        if (isTargetBook) {
                            targetCopy = target.copyComponentsToNewStack(Items.ENCHANTED_BOOK, 1);
                            this.inventory.setStack(0, targetCopy);
                        }

                        // Apply the new enchantment
                        targetCopy.addEnchantment(enchantment, level);

                        // Apply costs
                        int experienceCost = this.calculateEnchantmentCost(enchantment);
                        if (isNotCreativePlayer) {
                            // Note: Apply/Transfer cost is only applied if not in creative
                            player.applyEnchantmentCosts(target, experienceCost);
                            // Apply/Transfer Lapis cost uses enchantmentPower
                            lapis.decrement(experienceCost);
                            if (lapis.isEmpty()) {
                                this.inventory.setStack(1, ItemStack.EMPTY);
                            }
                        }

                        // Stats, criteria, and client/sound updates
                        player.incrementStat(Stats.ENCHANT_ITEM);
                        if (player instanceof ServerPlayerEntity) {
                            Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) player, target, i); // Uses i
                        }

                        this.inventory.markDirty();
                        this.seed.set(player.getEnchantmentTableSeed());
                        this.onContentChanged(this.inventory);
                        world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                    }
            );

            cir.setReturnValue(true); // Click was successful
            return true;
        }
        return false; // Not an apply button
    }

    /**
     * Handles the logic for rerolling the available enchantments.
     * Triggered when the clicked slot is {@link #REROLL_BUTTON_INDEX}.
     *
     * @return true if the click was handled, false otherwise.
     */
    @Unique
    private boolean rerollEnchantments(PlayerEntity player, int buttonId, CallbackInfoReturnable<Boolean> cir, ItemStack target, int occupiedSlots, boolean isLapisInsufficient, boolean isLevelInsufficient, boolean isNotCreativePlayer, ItemStack lapis, int rerollCost) {
        boolean isRerollButton = buttonId == REROLL_BUTTON_INDEX; // Hardcoded reroll button index
        boolean targetIsEmpty = target.isEmpty();
        boolean targetIsEnchantable = !targetIsEmpty && (target.isOf(Items.BOOK) || target.isEnchantable());
        boolean targetIsSourceEnchantable = Arrays.stream(this.enchantmentSource).anyMatch(element -> element == SOURCE);
        boolean hasTableSource = Arrays.stream(this.enchantmentSource).anyMatch(source -> source == TABLE);
        boolean canReroll = occupiedSlots < REROLL_BUTTON_INDEX && !targetIsSourceEnchantable && targetIsEnchantable && hasTableSource;
        boolean cannotAfford = (isLapisInsufficient || isLevelInsufficient) && isNotCreativePlayer;

        if (isRerollButton && canReroll && !cannotAfford) {
            this.context.run(
                    (world, pos) -> {
                        // Reroll cost is based on number of occupied slots + 1
                        if (isNotCreativePlayer) {
                            lapis.decrement(rerollCost);
                            if (lapis.isEmpty()) {
                                this.inventory.setStack(1, ItemStack.EMPTY);
                            }
                        }
                        // Apply experience cost
                        player.applyEnchantmentCosts(ItemStack.EMPTY, isNotCreativePlayer ? rerollCost : 0);

                        // Client/sound updates
                        this.inventory.markDirty();
                        this.seed.set(player.getEnchantmentTableSeed());
                        this.onContentChanged(this.inventory); // Refresh enchantment options
                        world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                    }
            );

            cir.setReturnValue(true);
            return true;
        }
        return false; // Not a reroll button or cannot reroll
    } // endregion

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
    @Inject(method = "quickMove(Lnet/minecraft/entity/player/PlayerEntity;I)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true)
    public void quickMove(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();

            // Case 1: Clicked *from* one of the 3 special slots (0, 1, or 38)
            if (slotIndex == 0 || slotIndex == 1 || slotIndex == 38) {
                // Try to move to inventory/hotbar (slots 2-37)
                if (!this.insertItem(itemStack2, 2, 38, true)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
            }
            // Case 2: Clicked *from* the inventory/hotbar (slots 2-37)
            else {
                // Try to move to Lapis Slot (1)
                if (itemStack2.isOf(Items.LAPIS_LAZULI)) {
                    if (!this.insertItem(itemStack2, 1, 2, true)) { // targetRange: slot 1
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
                // Try to move to Source Slot (38)
                // This check relies on the slot's updated canInsert method
                else if (((Slot)this.slots.get(38)).canInsert(itemStack2)) {
                    if (!this.insertItem(itemStack2, 38, 39, true)) { // targetRange: slot 38
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
                // Try to move to Target Slot (0)
                else if (((Slot)this.slots.getFirst()).canInsert(itemStack2)) {
                    if (!this.insertItem(itemStack2, 0, 1, true)) { // targetRange: slot 0
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }

                // If it couldn't be moved to a special slot, handle inventory/hotbar swapping
                else if (slotIndex >= 2 && slotIndex < 29) { // From main inventory (2-28)
                    // Try to move to hotbar (slots 29-37)
                    if (!this.insertItem(itemStack2, 29, 38, false)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                } else if (slotIndex >= 29 && slotIndex < 38) { // From hotbar (29-37)
                    // Try to move to main inventory (slots 2-28)
                    if (!this.insertItem(itemStack2, 2, 29, false)) {
                        cir.setReturnValue(ItemStack.EMPTY);
                        return;
                    }
                }
            }

            // Standard slot update logic
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            slot.onTakeItem(player, itemStack2);
        }

        cir.setReturnValue(itemStack);
        return;
    } // endregion

    // region Accessor Implementations
    /**
     * {@inheritDoc}
     */
    @Unique
    @Override
    public ItemStack getEnchantmentTarget() {
        return this.inventory.getStack(0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Gets the stack from inventory index 2, which is managed by slot index 38.
     */
    @Unique
    @Override
    public ItemStack getEnchantmentSource() {
        return this.inventory.getStack(2);
    }

    /**
     * {@inheritDoc}
     */
    @Unique
    @Override
    public int[] getEnchantmentSourceArray() {
        return this.enchantmentSource;
    }

    /**
     * {@inheritDoc}
     */
    @Unique
    @Override
    public int[] getTargetTextureIndices() {
        return this.targetTextureIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Unique
    @Override
    public int[] getSourceTextureIndices() {
        return this.sourceTextureIndices;
    }

    /**
     * {@inheritDoc}
     */
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
    } // endregion
}
