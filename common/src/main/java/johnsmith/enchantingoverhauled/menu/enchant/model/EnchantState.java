package johnsmith.enchantingoverhauled.menu.enchant.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the structural classification and interactivity status of an enchantment widget
 * in the UI list. Used by the Widget Factory to instantiate the correct UI component.
 */
public enum EnchantState implements StringRepresentable {
    /** Represents a placeholder slot with no enchantment data to display. */
    EMPTY("empty"),

    /** Represents an enchantment option that is blocked or missing a required catalyst. */
    LOCKED("locked"),

    /** Represents a standard, interactable enchantment action (e.g., an upgrade or transfer). */
    AVAILABLE("available"),

    /** Represents an enchantment that has reached its maximum natural level. */
    MAXED("maxed"),

    /** Represents an enchantment that has been pushed past its natural maximum level. */
    OVERLEVELED("overleveled");

    // region Properties
    /** The serialization codec mapping for this enum. */
    public static final Codec<EnchantState> CODEC = StringRepresentable.fromEnum(EnchantState::values);

    /** The internal string ID used for serialization. */
    public final String id;

    EnchantState(String id) { this.id = id; }
    // endregion

    // region Overrides
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
    // endregion
}