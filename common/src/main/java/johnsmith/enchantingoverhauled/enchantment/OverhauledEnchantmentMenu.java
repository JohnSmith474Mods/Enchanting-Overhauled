package johnsmith.enchantingoverhauled.enchantment;

import com.mojang.datafixers.util.Pair;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.platform.Services;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OverhauledEnchantmentMenu extends AbstractContainerMenu {
    public static final int REROLL_BUTTON = -1;

    public static final int ITEM_TO_ENCHANT_SLOT = 0;
    public static final int LAPIS_SLOT = 1;
    public static final int SOURCE_SLOT = 2;

    private static final int LAST_INVENTORYS_SLOT = 29;
    private static final int LAST_HOTBAR_SLOT = 38;

    private static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("item/empty_slot_lapis_lazuli");

    /** Max. number of slots for new enchantments */
    private static final int MAX_NEW_SLOTS = 3;

    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;

    public final int[] costs;
    public final int[] enchantClue;
    public final int[] levelClue;

    /**
     * Tracks the origin of the enchantment in the corresponding slot
     * The value corresponds to one of the static constants (NONE, TARGET, SOURCE, TABLE).
     */
    public final int[] enchantmentSources;

    /** Tracks the random texture index (0-9) for the target button background. */
    public final int[] targetTextureIndices;

    /** Tracks the random texture index (0-9) for the source button background. */
    public final int[] sourceTextureIndices;

    /** Tracks the random texture index (0-9) for the table button background. */
    public final int[] tableTextureIndices;

    public OverhauledEnchantmentMenu(final int containerId, final Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public OverhauledEnchantmentMenu(final int containerId, final Inventory playerInventory, final ContainerLevelAccess access) {
        super(Services.PLATFORM.getOverhauledEnchantmentMenuType(), containerId);

        enchantSlots = new SimpleContainer(3) {
            public void setChanged() {
                super.setChanged();
                OverhauledEnchantmentMenu.this.slotsChanged(this);
            }
        };

        random = RandomSource.create();
        enchantmentSeed = DataSlot.standalone();
        this.access = access;

        // Item to be enchanted slot
        addSlot(new Slot(enchantSlots, ITEM_TO_ENCHANT_SLOT, 18, 42) {
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(@NotNull final ItemStack stack) {
                return stack.isEnchantable() || stack.is(Items.BOOK);
            }
        });

        // Lapis slot
        addSlot(new Slot(enchantSlots, LAPIS_SLOT, 28, 65) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, OverhauledEnchantmentMenu.EMPTY_SLOT_LAPIS_LAZULI);
            }
        });

        // Source (e.g. ancient tome) slot
        addSlot(new Slot(enchantSlots, SOURCE_SLOT, 8, 65) {
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof EnchantedBookItem;
            }
        });

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 113 + i * 18));
            }
        }

        // Player hotbar
        for (int k = 0; k < 9; ++k) {
            addSlot(new Slot(playerInventory, k, 8 + k * 18, 171));
        }

        enchantmentSources = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];
        targetTextureIndices = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];
        sourceTextureIndices = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];
        tableTextureIndices = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];

        costs = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];
        enchantClue = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];
        levelClue = new int[Config.BOUNDED_MAX_ENCHANTMENTS.get()];

        for (int slot = 0; slot < Config.BOUNDED_MAX_ENCHANTMENTS.get(); slot++) {
            costs[slot] = 0;
            enchantClue[slot] = -1;
            levelClue[slot] = -1;

            addDataSlot(DataSlot.shared(costs, slot));
            addDataSlot(DataSlot.shared(enchantClue, slot));
            addDataSlot(DataSlot.shared(levelClue, slot));

            enchantmentSources[slot] = EnchantmentSource.NONE.getId();
            targetTextureIndices[slot] = random.nextInt(10);
            sourceTextureIndices[slot] = random.nextInt(10);
            tableTextureIndices[slot] = random.nextInt(10);

            addDataSlot(DataSlot.shared(enchantmentSources, slot));
            addDataSlot(DataSlot.shared(targetTextureIndices, slot));
            addDataSlot(DataSlot.shared(sourceTextureIndices, slot));
            addDataSlot(DataSlot.shared(tableTextureIndices, slot));
        }

        addDataSlot(enchantmentSeed).set(playerInventory.player.getEnchantmentSeed());
    }

    public void slotsChanged(@NotNull final Container inventory) {
        if (inventory != enchantSlots) {
            return;
        }

        ItemStack stack = inventory.getItem(ITEM_TO_ENCHANT_SLOT);
        ItemStack source = inventory.getItem(SOURCE_SLOT);

        if (stack.isEmpty() || !stack.isEnchantable()) {
            for (int slot = 0; slot < Config.BOUNDED_MAX_ENCHANTMENTS.get(); slot++) {
                costs[slot] = 0;
                enchantClue[slot] = -1;
                levelClue[slot] = -1;
            }

            return;
        }

        access.execute((level, position) -> {
            int power = 0;

            for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
                BlockPos statePosition = position.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);

                if (level.getBlockState(statePosition).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    power += EnchantmentLib.getAgnosticEnchantingPower(level, position.offset(offset));
                }
            }

            random.setSeed(enchantmentSeed.get());

            for (int slot = 0; slot < Config.BOUNDED_MAX_ENCHANTMENTS.get(); slot++) {
                costs[slot] = (25 - Math.min(power, 24)) * (slot + 1);
                enchantClue[slot] = -1;
                levelClue[slot] = -1;

                if (costs[slot] < slot + 1) {
                    costs[slot] = 0;
                }

                enchantmentSources[slot] = EnchantmentSource.NONE.getId();
                targetTextureIndices[slot] = random.nextInt(10);
                sourceTextureIndices[slot] = random.nextInt(10);
                tableTextureIndices[slot] = random.nextInt(10);
            }

            var map = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
            List<EnchantmentInstance> enchantments = gatherEnchantments(stack, source, level, position);

            for (int slot = 0; slot < enchantments.size(); slot++) {
                EnchantmentInstance instance = enchantments.get(slot);
                enchantClue[slot] = map.getId(instance.enchantment);
                levelClue[slot] = instance.level;
            }

            broadcastChanges();
        });
    }

    private List<EnchantmentInstance> gatherEnchantments(final ItemStack stack, final ItemStack source, final Level level, final BlockPos position) {
        List<EnchantmentInstance> enchantments = new ArrayList<>();
        Set<Enchantment> knownEnchantments = new HashSet<>();

        // Add the enchantments from sources at the top
        handleSource(stack, source, enchantments, knownEnchantments);

        // If less than MAX_NEW_SLOTS enchantments exist at this point, fill up to max. with enchantments from the table
        handleTable(stack, level, position, enchantments, knownEnchantments);

        // Add upgradable enchantments from the current item
        handleCurrent(stack, false, enchantments);

        // Add fully upgraded enchantments from the current item
        handleCurrent(stack, true, enchantments);

        // Adjust current entries using the source to allow higher-level enchantments
        handleOverride(stack, source, enchantments, knownEnchantments);

        return enchantments;
    }

    private void handleCurrent(final ItemStack stack, final boolean fullyUpgraded, final List<EnchantmentInstance> enchantments) {
        if (!stack.isEnchanted()) {
            return;
        }

        for (var entry : EnchantmentLib.getEnchantments(stack).entrySet()) {
            if (enchantments.size() == Config.BOUNDED_MAX_ENCHANTMENTS.get()) {
                return;
            }

            Holder<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();

            if (fullyUpgraded && level < enchantment.value().getMaxLevel()) {
                continue;
            } else if (!fullyUpgraded && level >= enchantment.value().getMaxLevel()) {
                continue;
            }

            if (enchantment.is(EnchantmentTags.CURSE)) {
                continue;
            }

            // There is no need to check / add known enchantments
            // Since the other sources should not add duplicates (using the compatible check)
            enchantmentSources[enchantments.size()] = EnchantmentSource.TARGET.getId();
            enchantments.add(new EnchantmentInstance(enchantment, level));
        }
    }

    private void handleOverride(final ItemStack stack, final ItemStack source, final List<EnchantmentInstance> enchantments, final Set<Enchantment> knownEnchantments) {
        if (source.isEmpty() || stack.is(Items.BOOK)) {
            return;
        }

        for (EnchantmentInstance entry : EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source))) {
            if (EnchantmentHelper.getItemEnchantmentLevel(entry.enchantment, stack) == 0) {
                continue;
            }

            for (int slot = 0; slot < enchantments.size(); slot++) {
                EnchantmentInstance known = enchantments.get(slot);

                if (entry.level > known.level && entry.enchantment.equals(known.enchantment)) {
                    enchantments.set(slot, entry);
                    enchantmentSources[slot] = EnchantmentSource.SOURCE.getId();
                    break;
                }
            }
        }
    }

    private void handleTable(final ItemStack stack, final Level level, final BlockPos position, final List<EnchantmentInstance> enchantments, final Set<Enchantment> knownEnchantments) {
        for (EnchantmentInstance entry : EnchantmentLib.generateEnchantments(random, stack, false, level, position)) {
            if (enchantments.size() == MAX_NEW_SLOTS) {
                break;
            }

            if (!knownEnchantments.add(entry.enchantment.value())) {
                continue;
            }

            if (!isCompatibleWith(stack, entry.enchantment)) {
                continue;
            }

            enchantmentSources[enchantments.size()] = EnchantmentSource.TABLE.getId();
            enchantments.add(entry);
        }
    }

    private void handleSource(final ItemStack stack, final ItemStack source, final List<EnchantmentInstance> enchantments, final Set<Enchantment> knownEnchantments) {
        if (source.isEmpty() || stack.is(Items.BOOK)) {
            return;
        }

        for (EnchantmentInstance entry : EnchantmentLib.getEnchantmentsAsList(EnchantmentLib.getEnchantments(source))) {
            Enchantment enchantment = entry.enchantment.value();

            if (!knownEnchantments.add(enchantment)) {
                continue;
            }

            if (enchantment.canEnchant(stack) && isCompatibleWith(stack, entry.enchantment)) {
                enchantmentSources[enchantments.size()] = EnchantmentSource.SOURCE.getId();
                enchantments.add(entry);
            }
        }
    }

    private boolean isCompatibleWith(final ItemStack stack, final Holder<Enchantment> enchantment) {
        return EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet(), enchantment);
    }

    public boolean clickMenuButton(@NotNull final Player player, int buttonId) {
        if (buttonId == Integer.MIN_VALUE) {
            return false;
        }

        ItemStack stack = enchantSlots.getItem(ITEM_TO_ENCHANT_SLOT);

        if (stack.isEmpty() || !stack.isEnchantable()) {
            return false;
        }

        ItemStack lapis = enchantSlots.getItem(LAPIS_SLOT);
        ItemStack source = enchantSlots.getItem(SOURCE_SLOT);

        ItemStack uncursedStack = EnchantmentLib.removeCursesFrom(stack);
        int occupiedSlots = EnchantmentLib.getEnchantments(uncursedStack).size();

        Optional<Holder<Enchantment>> potentialEnchantment = Optional.empty();
        IdMap<Holder<Enchantment>> map = player.registryAccess().registryOrThrow(Registries.ENCHANTMENT).asHolderIdMap();

        if (buttonId != REROLL_BUTTON) {
            // The cost increases with the number of *existing* enchantments on the item
            Holder<Enchantment> holder = map.byId(enchantClue[buttonId]);

            if (holder != null) {
                potentialEnchantment = Optional.of(holder);
            }
        }

        int rerollCost = occupiedSlots + 1;
        int lapisCost = potentialEnchantment.map(this::calculateEnchantmentCost).orElse(rerollCost);
        int levelCost;

        if (buttonId != REROLL_BUTTON) {
            levelCost = lapisCost;
        } else {
            levelCost = rerollCost;
        }

        if (!player.hasInfiniteMaterials() && (lapis.getCount() < lapisCost || player.experienceLevel < levelCost)) {
            return false;
        }

        if (buttonId == REROLL_BUTTON) {
            return handleReroll(player, lapis, lapisCost, rerollCost);
        }

        //noinspection OptionalIsPresent -> ignore for clarity
        if (potentialEnchantment.isEmpty()) {
            return false;
        }

        return switch (EnchantmentSource.byId(enchantmentSources[buttonId])) {
            case TARGET -> upgradeEnchant(player, buttonId, potentialEnchantment.get(), stack, lapis, lapisCost, levelCost);
            case SOURCE -> transferEnchant(player, buttonId, potentialEnchantment.get(), stack, source, lapis, lapisCost, levelCost);
            case TABLE -> enchant(player, buttonId, potentialEnchantment.get(), stack, lapis, lapisCost, levelCost);
            default -> false;
        };

        // TODO :: check for neoforge events etc.
    }

    private boolean enchant(final Player player, final int buttonId, final Holder<Enchantment> enchantment, final ItemStack stack, final ItemStack lapis, final int lapisCost, final int levelCost) {
        access.execute((level, position) -> {
            ItemStack stackReference = stack;

            if (stack.is(Items.BOOK)) {
                stackReference = stack.transmuteCopy(Items.ENCHANTED_BOOK);
                enchantSlots.setItem(ITEM_TO_ENCHANT_SLOT, stackReference);
            }

            stackReference.enchant(enchantment, rollLevel(enchantment, stack, levelClue[buttonId]));
            performedEnchantment(player, stack, lapis, lapisCost, levelCost);
            player.awardStat(Stats.ENCHANT_ITEM);

            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, stack, levelCost);
            }

            level.playSound(null, position, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, level.getRandom().nextFloat() * 0.1f + 0.9f);
        });

        return true;
    }

    private boolean transferEnchant(final @NotNull Player player, final int buttonId, final Holder<Enchantment> enchantment, final ItemStack stack, final ItemStack source, final ItemStack lapis, final int lapisCost, final int levelCost) {
        if (source.isEmpty()) {
            return false;
        }

        access.execute((level, position) -> {
            ItemStack stackReference = stack;

            if (stack.is(Items.BOOK)) {
                stackReference = stack.transmuteCopy(Items.ENCHANTED_BOOK);
                enchantSlots.setItem(ITEM_TO_ENCHANT_SLOT, stackReference);
            }

            stackReference.enchant(enchantment, rollLevel(enchantment, stack, levelClue[buttonId]));

            if (!source.is(Services.PLATFORM.getEnchantedTome())) {
                enchantSlots.setItem(SOURCE_SLOT, Items.BOOK.getDefaultInstance());
            }

            performedEnchantment(player, stack, lapis, lapisCost, levelCost);

            player.awardStat(Stats.ENCHANT_ITEM);

            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, stack, levelCost);
            }

            level.playSound(null, position, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, level.getRandom().nextFloat() * 0.1f + 0.9f);
        });

        return true;
    }

    private boolean upgradeEnchant(final @NotNull Player player, final int buttonId, final Holder<Enchantment> enchantment, final ItemStack stack, final ItemStack lapis, final int lapisCost, final int levelCost) {
        int enchantmentLevel = levelClue[buttonId];

        if (enchantmentLevel >= enchantment.value().getMaxLevel()) {
            return false;
        }

        access.execute((level, position) -> {
            // Guarantee a level increase of 1 with a chance of a bonus from the roll
            EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.upgrade(enchantment, rollLevel(enchantment, stack, enchantmentLevel + 1)));
            performedEnchantment(player, lapis, stack, lapisCost, levelCost);

            player.awardStat(Stats.ENCHANT_ITEM);

            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, stack, levelCost);
            }

            level.playSound(null, position, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, level.getRandom().nextFloat() * 0.1f + 0.9f);
        });

        return true;
    }

    private int rollLevel(final Holder<Enchantment> enchantment, final ItemStack stack, final int enchantmentLevel) {
        int enchantibility = Math.clamp(stack.getItem().getEnchantmentValue(), 1, 50);
        double probability = Math.clamp(2 * Math.pow(enchantibility / 50d, 2), 0, 1);

        int newLevel = enchantmentLevel;

        while (newLevel < enchantment.value().getMaxLevel()) {
            if (random.nextDouble() < probability) {
                newLevel++;
            } else {
                break;
            }
        }
        return newLevel;
    }

    private boolean handleReroll(@NotNull final Player player, final ItemStack lapis, final int lapisCost, final int rerollCost) {
        boolean isSourceEnchantable = false;
        boolean isTableEnchantable = false;

        for (int enchantmentSource : enchantmentSources) {
            if (enchantmentSource == EnchantmentSource.SOURCE.getId()) {
                isSourceEnchantable = true;
            } else if (enchantmentSource == EnchantmentSource.TABLE.getId()) {
                isTableEnchantable = true;
            }
        }

        if (isSourceEnchantable) {
            return false;
        }

        if (isTableEnchantable) {
            access.execute((level, position) -> {
                performedEnchantment(player, ItemStack.EMPTY, lapis, lapisCost, rerollCost);
                level.playSound(null, position, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1, level.getRandom().nextFloat() * 0.1f + 0.9f);
            });

            return true;
        }

        return false;
    }

    private void performedEnchantment(@NotNull final Player player, final ItemStack enchanted, final ItemStack lapis, final int lapisCost, final int levelCost) {
        if (!player.hasInfiniteMaterials()) {
            lapis.shrink(lapisCost);

            if (lapis.isEmpty()) {
                enchantSlots.setItem(LAPIS_SLOT, ItemStack.EMPTY);
            }
        }

        player.onEnchantmentPerformed(enchanted, player.hasInfiniteMaterials() ? levelCost : 0);
        enchantSlots.setChanged();
        enchantmentSeed.set(player.getEnchantmentSeed());
        slotsChanged(enchantSlots);
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    public void removed(@NotNull final Player player) {
        super.removed(player);
        this.access.execute((level, position) -> this.clearContainer(player, this.enchantSlots));
    }

    public boolean stillValid(@NotNull final Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    /** Slots are shifted by 1 (due to the source slot) compared to the usual enchantment menu */
    public @NotNull ItemStack quickMoveStack(@NotNull final Player player, final int slotIndex) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack movedStack = slot.getItem();
            originalStack = movedStack.copy();

            // Clicked *from* one of the 3 special slots
            if (slotIndex == ITEM_TO_ENCHANT_SLOT || slotIndex == LAPIS_SLOT || slotIndex == SOURCE_SLOT) {
                // Try to move to inventory / hotbar
                if (!moveItemStackTo(movedStack, SOURCE_SLOT + 1, LAST_HOTBAR_SLOT + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Clicked *from* the inventory / hotbar
                if (movedStack.is(Items.LAPIS_LAZULI)) {
                    // Try to move to the Lapis slot
                    if (!moveItemStackTo(movedStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slots.get(SOURCE_SLOT).mayPlace(movedStack)) {
                    // Try to move to the Source slot
                    if (!moveItemStackTo(movedStack, SOURCE_SLOT, SOURCE_SLOT + 1, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slots.get(ITEM_TO_ENCHANT_SLOT).mayPlace(movedStack)) {
                    // Try to move to the item-to-be-enchanted slot
                    if (!moveItemStackTo(movedStack, ITEM_TO_ENCHANT_SLOT, ITEM_TO_ENCHANT_SLOT + 1, true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex > SOURCE_SLOT && slotIndex <= LAST_INVENTORYS_SLOT) {
                    // From inventory to hotbar
                    if (!moveItemStackTo(movedStack, LAST_INVENTORYS_SLOT + 1, LAST_HOTBAR_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex > LAST_INVENTORYS_SLOT && slotIndex < LAST_HOTBAR_SLOT + 1) {
                    // From hotbar to inventory
                    if (!moveItemStackTo(movedStack, SOURCE_SLOT + 1, LAST_INVENTORYS_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (movedStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (movedStack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, movedStack);
        }

        return originalStack;
    }

    /**
     * Calculates the experience <i>cost</i> (not level requirement) for applying
     * or upgrading an enchantment.
     * <ul>
     * <li>Max Level 1 enchantments (e.g., Curses, Silk Touch) cost 3 XP.</li>
     * <li>Others cost 1, 2, or 3 based on their weight (rarity).</li>
     * </ul>
     */
    public int calculateEnchantmentCost(final Holder<Enchantment> enchantment) {
        if (enchantment.value().getMaxLevel() == 1) {
            return 3;
        }

        return enchantment.value().getWeight() % 3 + 1;
    }
}
