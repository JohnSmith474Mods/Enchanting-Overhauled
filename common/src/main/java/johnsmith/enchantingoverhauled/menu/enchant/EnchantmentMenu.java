package johnsmith.enchantingoverhauled.menu.enchant;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.EnchantmentSource;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantContext;
import johnsmith.enchantingoverhauled.menu.enchant.controller.EnchantAction;
import johnsmith.enchantingoverhauled.menu.enchant.controller.EnchantActionFactory;
import johnsmith.enchantingoverhauled.menu.enchant.controller.EnchantContextBuilder;
import johnsmith.enchantingoverhauled.menu.enchant.controller.EnchantCostCalculator;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantSlot;
import johnsmith.enchantingoverhauled.menu.MenuTypes;
import johnsmith.enchantingoverhauled.network.EnchantmentMenuSyncPayload;
import johnsmith.enchantingoverhauled.platform.Services;
import johnsmith.enchantingoverhauled.api.tag.Tags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentMenu extends AbstractContainerMenu {

    // region Constants
    private static final int INVENTORY_Y_POSITION = 113;
    private static final int HOTBAR_Y_POSITION = 171;
    private static final int REROLL_BUTTON_ID = 0;
    // endregion

    // region Fields
    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;
    private final Player player;

    // Aggregated State
    private List<EnchantContext> activeContexts = new ArrayList<>();
    // endregion

    public EnchantmentMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }
    
    public EnchantmentMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(MenuTypes.ENCHANT.get(), containerId);
        this.player = playerInventory.player;
        this.access = access;
        this.random = RandomSource.create();
        this.enchantmentSeed = DataSlot.standalone();

        this.enchantSlots = new SimpleContainer(3) {
            public void setChanged() {
                super.setChanged();
                EnchantmentMenu.this.slotsChanged(this);
            }
        };

        this.addSlot(EnchantSlot.create(EnchantSlot.TARGET, this.enchantSlots));
        this.addSlot(EnchantSlot.create(EnchantSlot.FUEL, this.enchantSlots));
        this.addSlot(EnchantSlot.create(EnchantSlot.SOURCE, this.enchantSlots));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, INVENTORY_Y_POSITION + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, HOTBAR_Y_POSITION));
        }

        this.addDataSlot(this.enchantmentSeed).set(playerInventory.player.getEnchantmentSeed());
    }

    // region Core Overrides

    @Override
    public void slotsChanged(@NotNull Container inventory) {
        if (inventory != this.enchantSlots) {
            return;
        }

        this.access.execute((world, pos) -> {
            ItemStack target = inventory.getItem(EnchantSlot.TARGET.index());
            ItemStack source = inventory.getItem(EnchantSlot.SOURCE.index());

            boolean isEnchantable = target.isEnchantable() || EnchantRecipe.get(world.registryAccess(), target).isPresent();

            if (!target.isEmpty() && isEnchantable) {
                int power = this.calculateEnchantingPower(world, pos);

                this.random.setSeed(this.enchantmentSeed.get());
                this.activeContexts = EnchantContextBuilder.begin(target, source, power, this.enchantmentSeed.get())
                        .withTargetEnchantments()
                        .withSourceEnchantments()
                        .withTableEnchantments(world, pos, this.random)
                        .build(EnchantCostCalculator.DEFAULT::calculateCost);

                Constants.LOG.debug("EnchantmentMenu: Context Pipeline generated {} active contexts.", this.activeContexts.size());
                for (EnchantContext ctx : this.activeContexts) {
                    Constants.LOG.debug(" - {}", ctx);
                }
            } else {
                Constants.LOG.debug("EnchantmentMenu: Target invalid. Clearing contexts.");
                this.activeContexts = new ArrayList<>();
            }

            if (this.player instanceof ServerPlayer serverPlayer) {
                EnchantmentMenuSyncPayload payload = new EnchantmentMenuSyncPayload(this.containerId, this.activeContexts);
                Services.NETWORK.sendToClient(payload, serverPlayer);
            }

            this.broadcastChanges();
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        Constants.LOG.debug("EnchantmentMenu: Received click on button ID: {}", buttonId);
        if (buttonId == REROLL_BUTTON_ID) {
            ItemStack target = this.enchantSlots.getItem(EnchantSlot.TARGET.index());

            boolean isEnchantable = target.isEnchantable() || EnchantRecipe.get(player.level().registryAccess(), target).isPresent();

            if (target.isEmpty() || !isEnchantable) {
                return false;
            }

            boolean targetIsSourceEnchantable = this.activeContexts.stream().anyMatch(ctx -> ctx.pending().source() == EnchantmentSource.SOURCE);
            boolean hasTableSource = this.activeContexts.stream().anyMatch(ctx -> ctx.pending().source() == EnchantmentSource.TABLE);

            ItemStack curseFreeTarget = EnchantmentLib.removeCursesFrom(target);
            int occupiedSlots = EnchantmentLib.getEnchantments(curseFreeTarget).size();

            boolean canReroll = occupiedSlots < Config.MAX_ENCHANTMENT_AMOUNT.get() && !targetIsSourceEnchantable && hasTableSource;
            if (!canReroll) return false;

            int rerollCost = occupiedSlots + 1;
            EnchantAction action = new EnchantAction.Reroll(rerollCost, rerollCost);

            if (action.execute(player, this.enchantSlots)) {
                this.enchantmentSeed.set(player.getEnchantmentSeed());
                this.slotsChanged(this.enchantSlots);
                this.broadcastChanges();
                return true;
            }
            return false;
        }

        int listIndex = buttonId - 1;
        if (listIndex < 0 || listIndex >= this.activeContexts.size()) {
            Constants.LOG.debug("EnchantmentMenu: Button ID out of bounds or no active contexts.");
            return false;
        }

        EnchantContext context = this.activeContexts.get(listIndex);
        EnchantAction action = EnchantActionFactory.createFrom(context);

        Constants.LOG.debug("EnchantmentMenu: Evaluated Action: {}", action.getClass().getSimpleName());

        if (action.execute(player, this.enchantSlots)) {
            this.enchantmentSeed.set(player.getEnchantmentSeed());
            this.slotsChanged(this.enchantSlots);
            this.broadcastChanges();
            return true;
        }

        return false;
    }
    // endregion

    // region Context Generation Pipeline

    private int calculateEnchantingPower(Level world, BlockPos pos) {
        int power = 0;
        for (BlockPos providerOffset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (world.getBlockState(pos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                power += EnchantmentLib.getAgnosticEnchantingPower(world, pos.offset(providerOffset));
            }
        }
        return power;
    }
    // endregion

    // region Inventory Operations

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (slotIndex >= 0 && slotIndex <= 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex >= 3 && slotIndex < 39) {
                if (itemStack2.is(Tags.ENCHANTING_FUEL)) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, true)) return ItemStack.EMPTY;
                } else if (this.slots.get(2).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 2, 3, true)) return ItemStack.EMPTY;
                } else if (!this.slots.get(0).hasItem() && this.slots.get(0).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, true)) return ItemStack.EMPTY;
                } else if (slotIndex < 30) {
                    if (!this.moveItemStackTo(itemStack2, 30, 39, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(itemStack2, 3, 30, false)) return ItemStack.EMPTY;
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
    // endregion

    // region Accessors
    public List<EnchantContext> getActiveContexts() {
        return this.activeContexts;
    }

    public void setActiveContexts(List<EnchantContext> contexts) {
        this.activeContexts = contexts;
    }

    public ItemStack getEnchantmentTarget() {
        return this.enchantSlots.getItem(EnchantSlot.TARGET.index());
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }
    // endregion
}