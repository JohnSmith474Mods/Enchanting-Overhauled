package johnsmith.enchantingoverhauled.api.config.type;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

/**
 * Determines where custom thematic coloring and level formatting rules should be applied
 * within a container GUI (such as the Enchanting Altar screen or Scribe screen).
 */
public enum FormatApplicationMode implements StringRepresentable {
    /**
     * Custom formatting is disabled in both the GUI widgets and their hover tooltips.
     */
    NONE("none"),

    /**
     * Custom formatting is applied only to the on-screen list/button widgets.
     */
    WIDGET("widget"),

    /**
     * Custom formatting is applied only to the hover tooltips inside the GUI.
     */
    TOOLTIP("tooltip"),

    /**
     * Custom formatting is applied to both the on-screen widgets and their hover tooltips.
     */
    ALL("all");

    /**
     * The serialization codec for encoding and decoding {@link FormatApplicationMode} values by name.
     */
    public static final Codec<FormatApplicationMode> CODEC = StringRepresentable.fromEnum(FormatApplicationMode::values);

    /**
     * The serialized string identifier for this application mode.
     */
    private final String id;

    /**
     * Constructs a {@code FormatApplicationMode} enum constant.
     *
     * @param id The serialized string identifier.
     */
    FormatApplicationMode(String id) {
        this.id = id;
    }

    /**
     * Retrieves the serialized string representation of this format application mode.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}