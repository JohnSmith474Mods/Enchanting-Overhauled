package johnsmith.enchantingoverhauled.client.gui.widget.scribe;

import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGameState;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;

import java.util.function.Supplier;

/**
 * A factory utility that translates an evaluated {@link ScribeGameState}
 * into the corresponding concrete {@link ScribeActionWidget} subclass implementation.
 */
public class ScribeWidgetFactory {

    /**
     * Instantiates the correct ScribeActionWidget object based on the state's classification.
     *
     * @param gridData   The synced container data backing the state
     * @param lapisCount Provider for the available lapis
     * @param isCreative Provider indicating if the player is bypassing costs
     * @return A constructed instance of a subclass of {@link ScribeActionWidget}.
     */
    public static ScribeActionWidget create(ScribeGridData gridData, Supplier<Integer> lapisCount, Supplier<Boolean> isCreative) {
        ScribeGameState state = gridData.getGameState();

        ScribeActionWidgetTheme theme = switch (state) {
            case READY, SATURATED, MAXIMIZED -> ScribeActionWidgetTheme.START;
            case PENDING, PLAYING -> ScribeActionWidgetTheme.ABORT;
            case WON -> ScribeActionWidgetTheme.WON;
            case LOST -> ScribeActionWidgetTheme.LOST;
            case MISSING_SOURCE -> ScribeActionWidgetTheme.IDLE;
        };

        return new ScribeActionWidget(gridData, lapisCount, isCreative, theme);
    }
}