package johnsmith.enchantingoverhauled.menu.scribe;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGameState;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeAction;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeRules;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeStrategy;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeInput;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeSlot;
import johnsmith.enchantingoverhauled.menu.MenuTypes;
import johnsmith.enchantingoverhauled.api.config.type.ScribeVariant;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * The server-side Container Menu bridging the inventory and Scribe logic.
 * <p>
 * Operates as a pure, thin controller mapping inventory state checks to {@link ScribeRules}
 * and triggering mutations via the {@link ScribeAction} command pattern.
 */
public class ScribeMenu extends AbstractContainerMenu {

    private final Container scribeContainer = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            ScribeMenu.this.slotsChanged(this);
        }
    };

    private final ScribeGridData gridData;
    private final ScribeStrategy gameController;
    private final ContainerLevelAccess access;

    private ItemStack consumedSource = ItemStack.EMPTY;
    private boolean lossPenaltyTriggered = false;

    public ScribeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public ScribeMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(MenuTypes.SCRIBE.get(), containerId);
        this.access = access;

        // Initialize synchronized native data and the logic controller
        ContainerData rawData = new SimpleContainerData(ScribeGridData.TOTAL_CELLS + 6);
        this.gridData = new ScribeGridData(rawData);

        this.addSlot(ScribeSlot.create(ScribeSlot.SOURCE, this.scribeContainer, this.gridData));
        this.addSlot(ScribeSlot.create(ScribeSlot.FUEL, this.scribeContainer, this.gridData));

        // Add a ghost slot for the target book so the client can read its item stack
        Container targetContainer = new SimpleContainer(1);
        this.addSlot(new Slot(targetContainer, 0, -1000, -1000) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) { return false; }

            @Override
            public boolean mayPickup(@NotNull Player playerIn) { return false; }
        });

        this.addPlayerInventory(playerInventory);

        if (Config.SCRIBE_MINIGAME_VARIANT.get() == ScribeVariant.SUPERPOSITION) {
            this.gameController = new ScribeStrategy.Superposition(this.gridData);
        } else {
            this.gameController = new ScribeStrategy.Classic(this.gridData);
        }

        this.addDataSlots(rawData);
        this.gridData.setGameState(ScribeGameState.MISSING_SOURCE);
        this.gridData.setSelectedEnchantmentIndex(-1);
        this.updateMenuState();
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 146 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 204));
        }
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        if (container == this.scribeContainer) {
            this.updateMenuState();
        }
    }

    /**
     * Evaluates item insertion/removal and sets up the game board difficulty dynamically.
     */
    private void updateMenuState() {
        if (this.gridData.getGameState().ordinal() >= ScribeGameState.PENDING.ordinal()) return;

        this.access.execute((level, pos) -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LecternBlockEntity lectern) {
                ItemStack book = lectern.getBook();
                this.slots.get(2).set(book); // Sync target book to client

                boolean isTome = book.is(Items.ENCHANTED_TOME.get());
                this.gridData.setIsTome(isTome);

                ItemStack sourceStack = this.scribeContainer.getItem(0);
                if (sourceStack.isEmpty()) {
                    this.gridData.setSelectedEnchantmentIndex(-1);
                } else {
                    int selectedId = this.gridData.getSelectedEnchantmentIndex();
                    if (selectedId >= 0) {
                        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                        boolean found = false;
                        for (var entry : EnchantmentLib.getEnchantments(sourceStack).entrySet()) {
                            if (registry.getId(entry.getKey().value()) == selectedId) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            this.gridData.setSelectedEnchantmentIndex(-1);
                        }
                    }
                }

                // Delegate condition checks to the ScribeRules logic layer
                ScribeGameState newState = ScribeRules.evaluateState(level, pos, sourceStack, this.gridData);
                if (newState == ScribeGameState.READY) {
                    ScribeRules.calculateDifficulty(book, this.gridData);
                    ScribeRules.calculateCosts(this.gridData);
                }
                this.gridData.setGameState(newState);
            } else {
                this.gridData.setGameState(ScribeGameState.MISSING_SOURCE);
                this.slots.get(2).set(ItemStack.EMPTY);
            }
        });
    }

    /**
     * Native interaction payload interceptor.
     * Decodes the payload and passes it to the isolated game controller or command pattern actions.
     */
    @Override
    public boolean clickMenuButton(@NotNull Player player, int payload) {
        ScribeGameState stateBefore = this.gridData.getGameState();
        ScribeInput input = ScribeInput.decode(payload);

        if (input.action() == ScribeInput.ACTION_TAKE_BOOK) {
            return new ScribeAction.TakeBook().execute(player, this.scribeContainer, this.gridData, this.access);
        }

        if (input.action() == ScribeInput.ACTION_SELECT_ENCHANTMENT) {
            if (stateBefore.ordinal() < ScribeGameState.PENDING.ordinal()) {
                this.gridData.setSelectedEnchantmentIndex(input.x());
                this.updateMenuState();
            }
            return true;
        }

        if (input.action() == ScribeInput.ACTION_ACK_LOSS) {
            if (this.gridData.getGameState() == ScribeGameState.LOST && !this.lossPenaltyTriggered) {
                this.lossPenaltyTriggered = true;
                new ScribeAction.AcknowledgeLoss().execute(player, this.scribeContainer, this.gridData, this.access);
            }
            return true;
        }

        // Intercept Start to deduct entry fee before processing rules
        if (input.action() == ScribeInput.ACTION_START && stateBefore == ScribeGameState.READY) {
            int lapisCost = this.gridData.getLapisCost();
            ItemStack lapis = this.scribeContainer.getItem(1);
            ItemStack source = this.scribeContainer.getItem(0);

            // Fail out silently if client desynchronized and button shouldn't have worked
            if (!player.isCreative() && lapis.getCount() < lapisCost) {
                return true;
            }

            this.consumedSource = source.copy();

            // CRITICAL: Set state to PENDING before modifying the container to prevent slotsChanged from wiping the selected enchantment index!
            this.gridData.setGameState(ScribeGameState.PENDING);

            new ScribeAction.Start(this.consumedSource).execute(player, this.scribeContainer, this.gridData, this.access);
            return true; // Bypass core processor since state was explicitly overridden natively
        }

        // Delegate logic and explicitly route the post-evaluation callback back to the main server thread
        java.util.concurrent.CompletableFuture<Void> future = this.gameController.processInput(input);
        if (future != null) {
            Runnable evaluateState = () -> {
                ScribeGameState stateAfter = this.gridData.getGameState();

                if (stateBefore != ScribeGameState.LOST && stateAfter == ScribeGameState.LOST) {
                    new ScribeAction.Lose(this.consumedSource).execute(player, this.scribeContainer, this.gridData, this.access);
                }

                if (stateBefore != ScribeGameState.WON && stateAfter == ScribeGameState.WON) {
                    new ScribeAction.Win(this.consumedSource).execute(player, this.scribeContainer, this.gridData, this.access);
                }

                if (stateBefore != stateAfter && stateAfter == ScribeGameState.MISSING_SOURCE) {
                    this.updateMenuState();
                }
            };

            // Execute safely on the main tick loop, or immediately if on the client
            if (player.level().isClientSide) {
                future.thenRun(evaluateState);
            } else {
                future.thenRunAsync(evaluateState, player.getServer());
            }
        }

        return true;
    }

    public void removed(@NotNull Player player) {
        super.removed(player);
        this.clearContainer(player, this.scribeContainer);

        // Ensure the loss penalty is applied if the user forcefully closes the UI before acknowledging
        if (this.gridData.getGameState() == ScribeGameState.LOST && !this.lossPenaltyTriggered) {
            this.lossPenaltyTriggered = true;
            new ScribeAction.AcknowledgeLoss().execute(player, this.scribeContainer, this.gridData, this.access);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();

            if (index == 0 || index == 1 || index == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.slots.get(0).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.slots.get(1).mayPlace(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39) {
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
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, Blocks.LECTERN);
    }

    public ScribeGridData getGridData() {
        return this.gridData;
    }
}