package johnsmith.enchantingoverhauled.client.gui.widget.enchant;

import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantRenderState;

/**
 * A simple factory utility that translates an evaluated {@link EnchantRenderState}
 * into the corresponding concrete {@link EnchantWidget} subclass implementation.
 */
public class EnchantWidgetFactory {

    /**
     * Instantiates the correct EnchantmentWidget object based on the state's classification.
     *
     * @param x                  The initial X position on the screen.
     * @param y                  The initial Y position on the screen.
     * @param state              The data state containing the primary state enum mapping.
     * @param texIndex           The deterministic texture variation index.
     * @param usePlainBackground Whether plain textures are enabled.
     * @param onPress            The callback invoked when the widget is clicked.
     * @return A constructed instance of a subclass of {@link EnchantWidget}.
     */
    public static EnchantWidget createFrom(int x, int y, EnchantRenderState state, int texIndex, boolean usePlainBackground, Runnable onPress) {
        return switch (state.state()) {
            case EMPTY -> new EnchantWidget.Empty(x, y, state, texIndex, usePlainBackground, onPress);
            case AVAILABLE -> new EnchantWidget.Available(x, y, state, texIndex, usePlainBackground, onPress);
            case LOCKED -> new EnchantWidget.Locked(x, y, state, texIndex, usePlainBackground, onPress);
            case MAXED -> new EnchantWidget.Maxed(x, y, state, texIndex, usePlainBackground, onPress);
            case OVERLEVELED -> new EnchantWidget.Overleveled(x, y, state, texIndex, usePlainBackground, onPress);
        };
    }
}