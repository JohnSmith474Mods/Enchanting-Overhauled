package johnsmith.enchantingoverhauled.client.gui.widget.scribe;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * A data record defining the visual and interactive properties for the {@link ScribeActionWidget}.
 * <p>
 * Encapsulates the text provider and action payload for each button state.
 */
public record ScribeActionWidgetTheme(
        Function<ScribeGridData, Component> textProvider,
        int actionPayload
) {

    public static final ResourceLocation TEX_ENABLED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/button/enabled.png");
    public static final ResourceLocation TEX_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/button/highlighted.png");
    public static final ResourceLocation TEX_DISABLED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/button/disabled.png");

    public static final ScribeActionWidgetTheme START = new ScribeActionWidgetTheme(
            data -> Component.translatable("gui.enchanting_overhauled.scribe.action.start"),
            ScribeInput.ACTION_START
    );

    public static final ScribeActionWidgetTheme ABORT = new ScribeActionWidgetTheme(
            data -> Component.translatable("gui.enchanting_overhauled.scribe.action.abort"),
            ScribeInput.ACTION_ABORT
    );

    public static final ScribeActionWidgetTheme WON = new ScribeActionWidgetTheme(
            data -> Component.translatable("gui.enchanting_overhauled.scribe.action.return"),
            ScribeInput.ACTION_RETURN_TO_INDEX
    );

    public static final ScribeActionWidgetTheme LOST = new ScribeActionWidgetTheme(
            data -> Component.translatable("gui.enchanting_overhauled.scribe.action.accept_loss"),
            ScribeInput.ACTION_ACK_LOSS
    );

    public static final ScribeActionWidgetTheme IDLE = new ScribeActionWidgetTheme(
            data -> Component.translatable(data.isTome() ? "gui.enchanting_overhauled.scribe.action.take_tome" : "gui.enchanting_overhauled.scribe.action.take_book"),
            ScribeInput.ACTION_TAKE_BOOK
    );

}