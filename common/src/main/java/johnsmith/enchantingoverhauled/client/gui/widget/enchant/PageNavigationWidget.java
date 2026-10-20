package johnsmith.enchantingoverhauled.client.gui.widget.enchant;

import com.mojang.blaze3d.systems.RenderSystem;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.client.gui.screen.enchant.MenuLayoutConfig;
import johnsmith.enchantingoverhauled.menu.enchant.EnchantmentMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Handles the rendering and interaction for the pagination controls (Next/Previous page buttons)
 * located in the Enchantment Screen.
 * <p>
 * Calculates bounds, manages hover states, and delegates scroll/click inputs to modify
 * the active visible offset of the enchantment list.
 */
public class PageNavigationWidget {

    // region Constants
    private static final ResourceLocation PAGE_FORWARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/forward.png");
    private static final ResourceLocation PAGE_FORWARD_HIGHLIGHTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/forward.png");
    private static final ResourceLocation PAGE_FORWARD_DISABLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/forward.png");
    private static final ResourceLocation PAGE_BACKWARD_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/enabled/backward.png");
    private static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/highlighted/backward.png");
    private static final ResourceLocation PAGE_BACKWARD_DISABLED_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/enchanting_table/button/disabled/backward.png");
    // endregion

    // region Fields
    private final EnchantmentMenu menu;
    private final MenuLayoutConfig layout;
    private final IntSupplier offsetSupplier;
    private final IntConsumer offsetUpdater;
    private final Runnable onPageChanged;

    private int alignX, alignY;
    private boolean isPrevHovered, isNextHovered;
    // endregion

    // region Initialization
    /**
     * Constructs a new PageNavigationWidget.
     *
     * @param menu           The active EnchantmentMenu containing context data.
     * @param layout         The layout configuration dictating positioning.
     * @param offsetSupplier A supplier providing the current list offset.
     * @param offsetUpdater  A consumer accepting the new list offset upon navigation.
     * @param onPageChanged  A callback executed when a navigation event occurs.
     */
    public PageNavigationWidget(EnchantmentMenu menu, MenuLayoutConfig layout, IntSupplier offsetSupplier, IntConsumer offsetUpdater, Runnable onPageChanged) {
        this.menu = menu;
        this.layout = layout;
        this.offsetSupplier = offsetSupplier;
        this.offsetUpdater = offsetUpdater;
        this.onPageChanged = onPageChanged;
    }
    // endregion

    // region Rendering & Input
    /**
     * Updates the alignment origin for the widget buttons.
     *
     * @param alignX The parent screen's left position.
     * @param alignY The parent screen's top position.
     */
    public void updatePosition(int alignX, int alignY) {
        this.alignX = alignX;
        this.alignY = alignY;
    }

    /**
     * Renders the previous and next page buttons to the screen, updating their hover
     * and enabled states based on the current context array size and offset.
     *
     * @param context     The GUI graphics context.
     * @param mouseX      The current mouse X coordinate.
     * @param mouseY      The current mouse Y coordinate.
     * @param partialTick The partial tick time.
     */
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        int totalEnchants = this.menu.getActiveContexts().size();
        if (totalEnchants <= layout.pageSize()) return;

        RenderSystem.enableBlend();
        boolean hasTargetItem = !this.menu.getEnchantmentTarget().isEmpty();

        // --- Backward Button (<) ---
        int prevX = alignX + layout.backwardXOffset();
        int prevY = alignY + layout.backwardYOffset();
        this.isPrevHovered = mouseX >= prevX && mouseY >= prevY && mouseX < prevX + layout.backwardButtonWidth() && mouseY < prevY + layout.backwardButtonHeight();

        boolean canGoBack = hasTargetItem && this.offsetSupplier.getAsInt() > 0;
        ResourceLocation prevTex = PAGE_BACKWARD_DISABLED_TEXTURE;
        if (canGoBack) {
            prevTex = this.isPrevHovered ? PAGE_BACKWARD_HIGHLIGHTED_TEXTURE : PAGE_BACKWARD_TEXTURE;
        }
        context.blit(prevTex, prevX, prevY, 0, 0, layout.backwardButtonWidth(), layout.backwardButtonHeight(), layout.backwardButtonWidth(), layout.backwardButtonHeight());

        // --- Forward Button (>) ---
        int nextX = alignX + layout.forwardXOffset();
        int nextY = alignY + layout.forwardYOffset();
        this.isNextHovered = mouseX >= nextX && mouseY >= nextY && mouseX < nextX + layout.forwardButtonWidth() && mouseY < nextY + layout.forwardButtonHeight();

        boolean canGoForward = hasTargetItem && (this.offsetSupplier.getAsInt() + layout.pageSize() < totalEnchants);
        ResourceLocation nextTex = PAGE_FORWARD_DISABLED_TEXTURE;
        if (canGoForward) {
            nextTex = this.isNextHovered ? PAGE_FORWARD_HIGHLIGHTED_TEXTURE : PAGE_FORWARD_TEXTURE;
        }
        context.blit(nextTex, nextX, nextY, 0, 0, layout.forwardButtonWidth(), layout.forwardButtonHeight(), layout.forwardButtonWidth(), layout.forwardButtonHeight());

        RenderSystem.disableBlend();
    }

    /**
     * Handles left-click interactions for navigating the pages.
     *
     * @param mouseX The mouse X coordinate.
     * @param mouseY The mouse Y coordinate.
     * @param button The mouse button pressed (0 for left).
     * @return true if the event was consumed, false otherwise.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        int totalEnchants = this.menu.getActiveContexts().size();
        if (totalEnchants <= layout.pageSize()) return false;
        boolean hasTargetItem = !this.menu.getEnchantmentTarget().isEmpty();

        if (this.isPrevHovered) {
            return navigatePage(hasTargetItem, totalEnchants, -1);
        }
        if (this.isNextHovered) {
            return navigatePage(hasTargetItem, totalEnchants, 1);
        }
        return false;
    }

    /**
     * Handles mouse wheel scrolling for navigating the pages.
     *
     * @param mouseX           The mouse X coordinate.
     * @param mouseY           The mouse Y coordinate.
     * @param horizontalAmount The horizontal scroll delta.
     * @param verticalAmount   The vertical scroll delta.
     * @return true if the event was consumed, false otherwise.
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalEnchants = this.menu.getActiveContexts().size();
        if (totalEnchants <= layout.pageSize()) return false;
        boolean hasTargetItem = !this.menu.getEnchantmentTarget().isEmpty();

        if (verticalAmount > 0) {
            return navigatePage(hasTargetItem, totalEnchants, -1);
        } else if (verticalAmount < 0) {
            return navigatePage(hasTargetItem, totalEnchants, 1);
        }
        return false;
    }
    // endregion

    // region Logic
    /**
     * Executes the page navigation logic, modifying the scroll offset and triggering UI updates.
     *
     * @param hasTargetItem Whether a target item is present in the table.
     * @param totalEnchants The total number of valid enchantments.
     * @param direction     The direction to navigate (-1 for back, 1 for forward).
     * @return true if navigation was successful, false if blocked.
     */
    private boolean navigatePage(boolean hasTargetItem, int totalEnchants, int direction) {
        int currentOffset = this.offsetSupplier.getAsInt();
        boolean canGoBack = hasTargetItem && currentOffset > 0;
        boolean canGoForward = hasTargetItem && (currentOffset + layout.pageSize() < totalEnchants);

        if ((direction < 0 && canGoBack) || (direction > 0 && canGoForward)) {
            int step = Config.SCROLL_STEP_SIZE.get();
            int newOffset;

            if (direction > 0) {
                int maxOffset = Math.max(0, totalEnchants - layout.pageSize());
                newOffset = Math.min(currentOffset + step, maxOffset);
            } else {
                newOffset = Math.max(currentOffset - step, 0);
            }

            this.offsetUpdater.accept(newOffset);
            this.onPageChanged.run();

            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        }
        return false;
    }
    // endregion
}