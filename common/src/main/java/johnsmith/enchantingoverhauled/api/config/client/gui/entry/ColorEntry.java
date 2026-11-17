package johnsmith.enchantingoverhauled.api.config.client.gui.entry;

import johnsmith.enchantingoverhauled.api.config.data.Property;
import johnsmith.enchantingoverhauled.api.config.client.gui.ConfigList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * A configuration entry implementation for hexadecimal color properties.
 * <p>
 * This entry renders an {@link EditBox} that accepts hex codes (e.g., "FF0000" or "FFFFFF").
 * It also renders a colored square preview immediately to the left of the input box
 * to visualize the currently selected color.
 */
public class ColorEntry extends OptionEntry<Integer, EditBox> {

    // Regex to allow 0-9, a-f, A-F. Optional # prefix handled in parsing.
    private static final Pattern HEX_PATTERN = Pattern.compile("^#?[0-9a-fA-F]*$");

    /**
     * Constructs a new color configuration entry.
     *
     * @param type           The bounded integer property backing this entry (0x000000 - 0xFFFFFF).
     * @param minecraft      The Minecraft client instance.
     * @param parentList     The parent configuration list containing this entry.
     * @param onValueChanged A callback runnable to execute when the value is modified.
     */
    public ColorEntry(Property.Bounded<Integer> type, Minecraft minecraft, ConfigList parentList, Runnable onValueChanged) {
        super(type, minecraft, parentList, onValueChanged);
        // Initialize with hex string representation
        this.updateWidgetValue();
    }

    /**
     * Creates and configures the edit box widget for this entry.
     * <p>
     * The widget is configured to accept hex characters. The responder parses the
     * hex string into an integer. If the parse is successful and within bounds,
     * the property is updated (which updates the preview box).
     *
     * @return A new {@link EditBox} instance.
     */
    @Override
    protected EditBox createWidget() {
        EditBox box = new EditBox(minecraft.font, 0, 0, 75, 20, Component.empty());
        box.setMaxLength(7); // Enough for "FFFFFF" or "#FFFFFF"

        box.setFilter(s -> HEX_PATTERN.matcher(s).matches());

        box.setResponder(s -> {
            try {
                if (s.isEmpty()) return;

                // Handle optional '#' prefix for parsing
                String cleanHex = s.startsWith("#") ? s.substring(1) : s;
                if (cleanHex.isEmpty()) return;

                int val = Integer.parseInt(cleanHex, 16);
                Property.Bounded<Integer> bounds = (Property.Bounded<Integer>) configType;

                if (val >= bounds.lowerBound && val <= bounds.upperBound) {
                    configType.set(val);
                    box.setTextColor(0xFFFFFF);
                } else {
                    box.setTextColor(0xFF0000);
                }
                updateResetButton();
                this.onValueChanged.run();
            } catch (NumberFormatException ignored) {
                box.setTextColor(0xFF0000);
            }
        });
        return box;
    }

    /**
     * Synchronizes the widget's text with the underlying property value.
     * Converts the integer color to an uppercase 6-digit hex string.
     */
    @Override
    protected void updateWidgetValue() {
        // Format as 6-digit Hex (e.g., FF0000)
        String hexString = String.format("%06X", configType.get());
        this.widget.setValue(hexString);
        this.widget.setTextColor(0xFFFFFF);
    }

    /**
     * Renders the entry, including the parent elements and the color preview box.
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
        // 1. Render label, reset button, and edit box via super
        super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);

        // 2. Render the color preview square
        // We calculate position relative to the widget to ensure layout consistency.
        // OptionEntry sets the widget's X/Y during its render call, so we can rely on that here.
        int boxSize = 20; // Same height as the widget
        int padding = 5;
        int boxX = this.widget.getX() - padding - boxSize;
        int boxY = this.widget.getY();

        // Convert the RGB int to ARGB for rendering (add full alpha 0xFF)
        int color = configType.get() | 0xFF000000;

        // Draw the colored box
        guiGraphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, color);

        // Draw a border around the box for visibility against dark backgrounds
        guiGraphics.renderOutline(boxX, boxY, boxSize, boxSize, 0xFFAAAAAA);
    }
}