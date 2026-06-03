package johnsmith.enchantingoverhauled.menu;

import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;

import johnsmith.enchantingoverhauled.tag.Tags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EnchantmentMenu extends AbstractContainerMenu {

    // region Constants
    private static final int TARGET_X_POSITION = 18;
    private static final int TARGET_Y_POSITION = 46 - 4;
    private static final int LAPIS_X_POSITION = 28;
    private static final int LAPIS_Y_POSITION = 73 - 8;
    private static final int SOURCE_X_POSITION = LAPIS_X_POSITION - 18 - 2;
    private static final int SOURCE_Y_POSITION = LAPIS_Y_POSITION;
    private static final int INVENTORY_Y_POSITION = 113;
    private static final int HOTBAR_Y_POSITION = 171;
    private static final int REROLL_BUTTON_ID = 0;
    private static final RandomSource TEXTURE_RANDOM = RandomSource.create();
    // endregion

    // region Fields
    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;
    private final DataSlot enchantmentCount = DataSlot.standalone();
    public final int[] costs;
    public final int[] enchantClue;
    public final int[] levelClue;

    // Overhaul Fields
    public int[] enchantmentSources;
    public final int[] targetTextureIndices;
    public final int[] sourceTextureIndices;
    public final int[] tableTextureIndices;
    // endregion

    public EnchantmentMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(Services.PLATFORM.getEnchantmentMenyType(), containerId);

        // Modified: Size increased to 3 to accommodate Source slot
        this.enchantSlots = new SimpleContainer(3) {
            public void setChanged() {
                super.setChanged();
                EnchantmentMenu.this.slotsChanged(this);
            }
        };

        int maxEnchants = Config.BOUNDED_MAX_ENCHANTMENT_AMOUNT.get();

        this.random = RandomSource.create();
        this.enchantmentSeed = DataSlot.standalone();
        this.costs = new int[maxEnchants];
        this.enchantClue = new int[maxEnchants];
        this.levelClue = new int[maxEnchants];
        this.enchantmentSources = new int[maxEnchants];
        this.targetTextureIndices = new int[maxEnchants];
        this.sourceTextureIndices = new int[maxEnchants];
        this.tableTextureIndices = new int[maxEnchants];
        this.access = access;

        // --- Slot Initialization (Overhauled Positions) ---

        // Slot 0: Target Item
        this.addSlot(new Slot(this.enchantSlots, 0, TARGET_X_POSITION, TARGET_Y_POSITION) {
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 1: Lapis Lazuli
        this.addSlot(new Slot(this.enchantSlots, 1, LAPIS_X_POSITION, LAPIS_Y_POSITION) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Tags.ENCHANTING_FUEL);
            }
        });

        // Slot 2: Source Item (New)
        this.addSlot(new Slot(this.enchantSlots, 2, SOURCE_X_POSITION, SOURCE_Y_POSITION) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.ENCHANTED_BOOK) || stack.is(Services.PLATFORM.getEnchantedTome());
            }
        });

        // Player Inventory
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, INVENTORY_Y_POSITION + i * 18));
            }
        }

        // Player Hotbar
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, HOTBAR_Y_POSITION));
        }

        // --- Data Slots ---


        // Vanilla Data Slots
        for (int i = 0; i < maxEnchants; i++) {
            this.costs[i] = -1;
            this.addDataSlot(DataSlot.shared(this.costs, i));
        }
        this.addDataSlot(this.enchantmentSeed).set(playerInventory.player.getEnchantmentSeed());
        for (int i = 0; i < maxEnchants; i++) {
            this.enchantClue[i] = -1;
            this.addDataSlot(DataSlot.shared(this.enchantClue, i));
        }
        for (int i = 0; i < maxEnchants; i++) {
            this.levelClue[i] = -1;
            this.addDataSlot(DataSlot.shared(this.levelClue, i));
        }

        // Overhaul Data Slots
        this.addDataSlot(this.enchantmentCount);
        for (int i = 0; i < maxEnchants; i++) {
            this.enchantmentSources[i] = -1;
            this.addDataSlot(DataSlot.shared(this.enchantmentSources, i));
        }
        for (int i = 0; i < maxEnchants; i++) {
            this.targetTextureIndices[i] = -1;
            this.addDataSlot(DataSlot.shared(this.targetTextureIndices, i));
        }
        for (int i = 0; i < maxEnchants; i++) {
            this.sourceTextureIndices[i] = -1;
            this.addDataSlot(DataSlot.shared(this.sourceTextureIndices, i));
        }
        for (int i = 0; i < maxEnchants; i++) {
            this.tableTextureIndices[i] = -1;
            this.addDataSlot(DataSlot.shared(this.tableTextureIndices, i));
        }
    }

    // region Core Logic Overrides (Completely Replaced)

    @Override
    public void slotsChanged(@NotNull Container inventory) {
        if (inventory != this.enchantSlots) {
            return;
        }

        ItemStack target = inventory.getItem(0);
        ItemStack source = inventory.getItem(2);
        int slotCount = this.costs.length;

        // Invalid state: Target item is empty or not enchantable
        if (!(!target.isEmpty() && target.isEnchantable())) {
            for(int i = 0; i < slotCount; ++i) {
                this.costs[i] = EnchantmentSource.NONE.getId();
                this.enchantClue[i] = EnchantmentSource.NONE.getId();
                this.levelClue[i] = EnchantmentSource.NONE.getId();
            }
            this.enchantmentCount.set(0);
            return;
        }

        this.access.execute((world, pos) -> {
            // 1. Calculate enchanting power (custom logic)
            int power = 0;
            for (BlockPos providerOffset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                if (world.getBlockState(
                        pos.offset(
                                providerOffset.getX() / 2,
                                providerOffset.getY(),
                                providerOffset.getZ() / 2
                        )).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    power += EnchantmentLib.getAgnosticEnchantingPower(world, pos.offset(providerOffset));
                }
            }

            // 2. Set base level requirements (vanilla-like calculation)
            this.random.setSeed(this.enchantmentSeed.get());

            for (int j = 0; j < slotCount; ++j) {
                this.costs[j] = (25 - Math.min(power, 24)) * (j + 1);
                this.enchantClue[j] = EnchantmentSource.NONE.getId();
                this.levelClue[j] = EnchantmentSource.NONE.getId();
            }

            // 3. Populate enchantment options using the new priority logic
            List<EnchantmentInstance> enchantments =
                    this.setDataSlots(target, source, world.enabledFeatures(), world, pos);
            this.enchantmentCount.set(enchantments.size());

            // 4. Fill the synced arrays with data from the generated list
            int slot = 0;
            for (EnchantmentInstance entry : enchantments) {
                var idMap = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                this.enchantClue[slot] = idMap.getId(entry.enchantment());
                this.levelClue[slot] = entry.level();
                ++slot;
            }

            // 4b. Set random texture indices for client rendering
            for (int k = 0; k < slotCount; ++k) {
                this.targetTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                this.sourceTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
                this.tableTextureIndices[k] = TEXTURE_RANDOM.nextInt(10);
            }

            this.broadcastChanges();
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        ItemStack target = this.enchantSlots.getItem(0);
        ItemStack lapis = this.enchantSlots.getItem(1);
        ItemStack source = this.enchantSlots.getItem(2);
        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);

        int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

        RegistryAccess registryAccess = player.level().registryAccess();
        var idMap = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();

        Optional<Enchantment> enchantment = Optional.empty();

        boolean isReroll = buttonId == REROLL_BUTTON_ID;
        int arrayIndex = isReroll ? -1 : buttonId -1;

        if (!isReroll && (arrayIndex < 0 || arrayIndex >= this.costs.length)) {
            return false;
        }

        if (!isReroll) {
            Holder<Enchantment> holder = idMap.byId(this.enchantClue[arrayIndex]);
            if (holder != null) {
                enchantment = Optional.of(holder.value());
            }
        }

        int rerollCost = occupiedSlots + 1;
        int buttonCost = enchantment.map(this::calculateEnchantmentCost).orElse(rerollCost);

        boolean isNotCreativePlayer = !player.getAbilities().instabuild;
        boolean isNotEnchantableTarget = !target.isEnchantable();
        boolean isTargetEmpty = target.isEmpty();

        int currentLapis = lapis.getCount();
        boolean isLapisInsufficient = currentLapis < buttonCost;
        boolean isLevelInsufficient = isReroll
                ? player.experienceLevel < rerollCost
                : player.experienceLevel < this.costs[arrayIndex];

        if ((isNotCreativePlayer && (isLapisInsufficient || isLevelInsufficient))
                || isTargetEmpty
                || isNotEnchantableTarget
        ) {
            return false;
        }

        if (this.upgradeEnchantment(registryAccess, player, buttonId, arrayIndex, target, isNotCreativePlayer, lapis)) return true;
        if (this.transferEnchantment(registryAccess, player, buttonId, arrayIndex, target, source, isNotCreativePlayer, lapis)) return true;
        if (this.applyEnchantment(registryAccess, player, buttonId, arrayIndex, target, isNotCreativePlayer, lapis)) return true;
        return this.rerollEnchantments(player, buttonId, target, occupiedSlots, isLapisInsufficient, isLevelInsufficient, isNotCreativePlayer, lapis, rerollCost);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            // 1. Moving FROM the Table (Slots 0, 1, 2) TO Player Inventory
            if (slotIndex >= 0 && slotIndex <= 2) {
                // Try moving to Player Inventory (3-30) or Hotbar (30-39)
                // moveItemStackTo end index is exclusive, so we use 39
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 2. Moving FROM Player Inventory TO the Table
            else if (slotIndex >= 3 && slotIndex < 39) {
                // A. Priority: Lapis Lazuli -> Slot 1
                if (itemStack2.is(Tags.ENCHANTING_FUEL)) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                // B. Priority: Enchanted Books (Source) -> Slot 2
                // We check the slot's predicate directly to ensure validity
                else if (this.slots.get(2).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 2, 3, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                // C. Priority: Enchantable Items (Target) -> Slot 0
                // Note: Slot 0 accepts anything physically, so we usually check logic here if needed,
                // but checking moveItemStackTo(0, 1) is sufficient for general behavior.
                else if (!this.slots.get(0).hasItem() && this.slots.get(0).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, true)) {
                        return ItemStack.EMPTY;
                    }
                }
                // D. Internal Swap: Main Inventory <-> Hotbar
                else if (slotIndex < 30) {
                    // Move from Main Inventory (3-30) to Hotbar (30-39)
                    if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Move from Hotbar (30-39) to Main Inventory (3-30)
                    if (!this.moveItemStackTo(itemStack2, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.enchantSlots));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    // region Enchanting Overhauled Logic
    private List<EnchantmentInstance> generateEnchantments(
            FeatureFlagSet enabledFeatures,
            ItemStack target,
            Level world,
            BlockPos pos
    ) {
        this.random.setSeed(this.enchantmentSeed.get());
        return EnchantmentLib.generateEnchantments(enabledFeatures, this.random, target, false, world, pos);
    }

    public List<EnchantmentInstance> setDataSlots(
            ItemStack target,
            ItemStack source,
            FeatureFlagSet featureSet,
            Level world,
            BlockPos pos
    ) {
        List<EnchantmentInstance> list = new ArrayList<>();
        Set<Enchantment> addedEnchantments = new HashSet<>();

        ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
        int arraySize = this.costs.length;

        for (int i = 0; i < arraySize; i++) {
            this.enchantmentSources[i] = EnchantmentSource.NONE.getId();
        }

        this.readEnchantmentsFromTarget(curseFreeTarget, list, arraySize, addedEnchantments);
        this.overwriteTargetEnchantmentsFromSource(target, source, list, addedEnchantments);
        this.appendSourceEnchantments(target, source, list, arraySize, addedEnchantments);
        this.generateEnchantmentsFromTable(target, featureSet, list, arraySize, world, pos);

        return list;
    }

    private void readEnchantmentsFromTarget(
            ItemStack curseFreeTarget,
            List<EnchantmentInstance> list,
            int arraySize,
            Set<Enchantment> addedEnchantments
    ) {
        if (!curseFreeTarget.isEnchanted()) {
            return;
        }

        ItemEnchantments enchants = EnchantmentLib.getEnchantments(curseFreeTarget);
        List<EnchantmentInstance> targetEnchantments = new ArrayList<>();

        // 1. Collect all enchantments into a temporary list
        for (var entry : enchants.entrySet()) {
            targetEnchantments.add(new EnchantmentInstance(entry.getKey(), entry.getIntValue()));
        }

        // 2. Sort the list: Overleveled > Maxed > Upgradable (Sorted by ID)
        targetEnchantments.sort((a, b) -> {
            int maxLevelA = a.enchantment().value().getMaxLevel();
            int maxLevelB = b.enchantment().value().getMaxLevel();

            boolean isOverleveledA = a.level() > maxLevelA;
            boolean isOverleveledB = b.level() > maxLevelB;

            // Priority 1: Overleveled comes first
            if (isOverleveledA != isOverleveledB) {
                return isOverleveledA ? -1 : 1;
            }

            boolean isMaxedA = a.level() == maxLevelA;
            boolean isMaxedB = b.level() == maxLevelB;

            // Priority 2: Maxed comes next
            if (isMaxedA != isMaxedB) {
                return isMaxedA ? -1 : 1;
            }

            // Priority 3: Stable sort by Registry ID (e.g. "minecraft:sharpness")
            // We safely unwrap the key to get the ResourceLocation
            String idA = a.enchantment().unwrapKey().map(k -> k.location().toString()).orElse("");
            String idB = b.enchantment().unwrapKey().map(k -> k.location().toString()).orElse("");

            return idA.compareTo(idB);
        });

        // 3. Add sorted enchantments to the main list
        for (EnchantmentInstance entry : targetEnchantments) {
            if (list.size() >= arraySize) break; // Stop if slots are full

            list.add(entry);
            addedEnchantments.add(entry.enchantment().value());

            // Mark the source for this specific slot index
            this.enchantmentSources[list.size() - 1] = EnchantmentSource.TARGET.getId();
        }
    }

    private void overwriteTargetEnchantmentsFromSource(
            ItemStack target,
            ItemStack source,
            List<EnchantmentInstance> list,
            Set<Enchantment> addedEnchantments
    ) {
        if (source.isEmpty() || target.is(Items.BOOK)) {
            return;
        }

        List<EnchantmentInstance> sourceEnchantments =
                EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source));

        for (EnchantmentInstance sourceEntry : sourceEnchantments) {
            Enchantment sourceEnchant = sourceEntry.enchantment().value();
            int sourceLevel = sourceEntry.level();

            if (addedEnchantments.contains(sourceEnchant) && sourceEnchant.canEnchant(target)) {
                for (int i = 0; i < list.size(); i++) {
                    EnchantmentInstance targetEntry = list.get(i);
                    if (targetEntry.enchantment().value().equals(sourceEnchant)) {
                        int targetLevel = targetEntry.level();
                        if (sourceLevel > targetLevel) {
                            list.set(i, sourceEntry);
                            this.enchantmentSources[i] = EnchantmentSource.SOURCE.getId();
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean isCompatibleWith(List<EnchantmentInstance> currentList, Holder<Enchantment> candidate) {
        for (EnchantmentInstance instance : currentList) {
            Holder<Enchantment> existing = instance.enchantment();
            if (existing.equals(candidate)) continue;
            if (!Enchantment.areCompatible(candidate, existing)) {
                return false;
            }
        }
        return true;
    }

    private void appendSourceEnchantments(
            ItemStack target,
            ItemStack source,
            List<EnchantmentInstance> list,
            int arraySize,
            Set<Enchantment> addedEnchantments
    ) {
        if (list.size() >= arraySize || source.isEmpty() || target.is(Items.BOOK)) {
            return;
        }

        List<EnchantmentInstance> sourceEnchantments =
                EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source));

        for (EnchantmentInstance entry : sourceEnchantments) {
            if (list.size() >= arraySize) break;
            Enchantment sourceEnchant = entry.enchantment().value();
            if (addedEnchantments.add(sourceEnchant)) {
                if (sourceEnchant.canEnchant(target) && this.isCompatibleWith(list, entry.enchantment())) {
                    list.add(entry);
                    this.enchantmentSources[list.size() - 1] = EnchantmentSource.SOURCE.getId();
                }
            }
        }
    }

    private void generateEnchantmentsFromTable(
            ItemStack target,
            FeatureFlagSet featureSet,
            List<EnchantmentInstance> list,
            int arraySize,
            Level world,
            BlockPos pos
    ) {
        if (list.size() < arraySize) {
            int slotToFill = list.size();
            List<EnchantmentInstance> generated =
                    this.generateEnchantments(featureSet, target, world, pos);

            generated.removeIf(generatedEntry -> {
                for (EnchantmentInstance existingEntry : list) {
                    if (existingEntry.enchantment().equals(generatedEntry.enchantment())) {
                        return true;
                    }
                }
                return !this.isCompatibleWith(list, generatedEntry.enchantment());
            });

            int generatedIndex = 0;
            for (int i = slotToFill; i < arraySize; i++) {
                if (generatedIndex < generated.size()) {
                    EnchantmentInstance candidate = generated.get(generatedIndex);
                    generatedIndex++;
                    list.add(candidate);
                    this.enchantmentSources[i] = EnchantmentSource.TABLE.getId();
                } else {
                    break;
                }
            }
        }
    }

    public int rollLevel(ItemStack target, Enchantment enchantment, int currentLevel) {
        int enchantability = Math.clamp(target.get(DataComponents.ENCHANTABLE).value(), 1, 50);
        double successProbability = Math.clamp(2.0D * Math.pow(enchantability / 50.0D, 2.0D), 0.0D, 1.0D);
        int newLevel = currentLevel;

        while (newLevel < enchantment.getMaxLevel()) {
            if (this.random.nextDouble() < successProbability) {
                newLevel++;
            } else {
                break;
            }
        }
        return newLevel;
    }

    // --- Action Sub-Handlers ---

    private boolean upgradeEnchantment(
            RegistryAccess registryAccess,
            Player player,
            int buttonId,
            int arrayIndex,
            ItemStack target,
            boolean isNotCreativePlayer,
            ItemStack lapis
    ) {
        boolean isUpgradeButton = buttonId > REROLL_BUTTON_ID
                && this.enchantmentSources[arrayIndex] == EnchantmentSource.TARGET.getId();

        if (isUpgradeButton) {
            var idMap = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
            Holder<Enchantment> enchantment = idMap.byId(this.enchantClue[arrayIndex]);
            if (enchantment == null) return false;
            int currentLevel = this.levelClue[arrayIndex];

            if (currentLevel >= enchantment.value().getMaxLevel()) {
                return true;
            }

            this.access.execute((world, pos) -> {
                int newLevel = this.rollLevel(target, enchantment.value(), currentLevel + 1);

                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentLib.getEnchantments(target));
                builder.set(enchantment, newLevel);
                target.set(DataComponents.ENCHANTMENTS, builder.toImmutable());

                int experienceLevelCost = this.calculateEnchantmentCost(enchantment.value());
                player.onEnchantmentPerformed(target, isNotCreativePlayer ? experienceLevelCost : 0);

                if (isNotCreativePlayer) {
                    lapis.shrink(arrayIndex);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, arrayIndex);
                }

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        }
        return false;
    }

    private boolean transferEnchantment(
            RegistryAccess registryAccess,
            Player player,
            int buttonId,
            int arrayIndex,
            ItemStack target,
            ItemStack source,
            boolean isNotCreativePlayer,
            ItemStack lapis
    ) {
        boolean isTransferButton = buttonId > REROLL_BUTTON_ID
                && this.enchantmentSources[arrayIndex] == EnchantmentSource.SOURCE.getId();

        if (isTransferButton) {
            this.access.execute((world, pos) -> {
                ItemStack targetCopy = target;
                var idMap = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                Holder<Enchantment> enchantment = idMap.byId(this.enchantClue[arrayIndex]);
                if (enchantment == null) return;

                int level = this.rollLevel(target, enchantment.value(), this.levelClue[arrayIndex]);

                boolean isTargetBook = target.is(Items.BOOK);
                boolean isSourcePersistent = source.is(Services.PLATFORM.getEnchantedTome());

                if (isTargetBook) {
                    targetCopy = new ItemStack(Items.ENCHANTED_BOOK);
                    targetCopy.setCount(1);
                    this.enchantSlots.setItem(0, targetCopy);
                }

                targetCopy.enchant(enchantment, level);

                int experienceCost = this.calculateEnchantmentCost(enchantment.value());
                if (isNotCreativePlayer) {
                    player.onEnchantmentPerformed(target, experienceCost);
                    lapis.shrink(experienceCost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                    if (!isSourcePersistent) {
                        this.enchantSlots.setItem(2, new ItemStack(Items.BOOK));
                    }
                }

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, arrayIndex);
                }

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        }
        return false;
    }

    private boolean applyEnchantment(
            RegistryAccess registryAccess,
            Player player,
            int buttonId,
            int arrayIndex,
            ItemStack target,
            boolean isNotCreativePlayer,
            ItemStack lapis
    ) {
        boolean isApplyButton = buttonId >REROLL_BUTTON_ID
                && this.enchantmentSources[arrayIndex] == EnchantmentSource.TABLE.getId();

        if (isApplyButton) {
            this.access.execute((world, pos) -> {
                ItemStack targetCopy = target;
                var idMap = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                Holder<Enchantment> enchantment = idMap.byId(this.enchantClue[arrayIndex]);
                if (enchantment == null) return;

                int level = this.rollLevel(target, enchantment.value(), this.levelClue[arrayIndex]);
                boolean isTargetBook = target.is(Items.BOOK);

                if (isTargetBook) {
                    targetCopy = new ItemStack(Items.ENCHANTED_BOOK);
                    targetCopy.setCount(1);
                    this.enchantSlots.setItem(0, targetCopy);
                }

                targetCopy.enchant(enchantment, level);

                int experienceCost = this.calculateEnchantmentCost(enchantment.value());
                if (isNotCreativePlayer) {
                    player.onEnchantmentPerformed(target, experienceCost);
                    lapis.shrink(experienceCost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, arrayIndex);
                }

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        }
        return false;
    }

    private boolean rerollEnchantments(
            Player player,
            int buttonId,
            ItemStack target,
            int occupiedSlots,
            boolean isLapisInsufficient,
            boolean isLevelInsufficient,
            boolean isNotCreativePlayer,
            ItemStack lapis,
            int rerollCost
    ) {
        boolean isRerollButton = buttonId == REROLL_BUTTON_ID;
        boolean targetIsEmpty = target.isEmpty();
        boolean targetIsEnchantable = !targetIsEmpty && (target.is(Items.BOOK) || target.isEnchantable());

        boolean targetIsSourceEnchantable = Arrays.stream(this.enchantmentSources)
                .anyMatch(element -> element == EnchantmentSource.SOURCE.getId());
        boolean hasTableSource = Arrays.stream(this.enchantmentSources)
                .anyMatch(source -> source == EnchantmentSource.TABLE.getId());
        int slotCount = this.costs.length;
        boolean canReroll = occupiedSlots < slotCount
                && !targetIsSourceEnchantable
                && targetIsEnchantable
                && hasTableSource;
        boolean canAfford = !((isLapisInsufficient || isLevelInsufficient) && isNotCreativePlayer);

        if (isRerollButton && canReroll && canAfford) {
            this.access.execute((world, pos) -> {
                if (isNotCreativePlayer) {
                    lapis.shrink(rerollCost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }
                player.onEnchantmentPerformed(ItemStack.EMPTY, isNotCreativePlayer ? rerollCost : 0);

                this.enchantSlots.setChanged();
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                world.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        }
        return false;
    }
    // endregion

    // region Accessors
    public int getEnchantmentListLength() {
        return this.enchantmentCount.get();
    }

    public ItemStack getEnchantmentTarget() {
        return this.enchantSlots.getItem(0);
    }

    public int[] getEnchantmentSourceArray() {
        return enchantmentSources;
    }

    public int calculateEnchantmentCost(Enchantment enchantment) {
        if (enchantment.getMaxLevel() == 1) return 3;
        return enchantment.getWeight() % 3 + 1;
    }

    public int[] getTargetTextureIndices() {
        return targetTextureIndices;
    }

    public int[] getSourceTextureIndices() {
        return sourceTextureIndices;
    }

    public int[] getTableTextureIndices() {
        return tableTextureIndices;
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }
    // endregion
}