package johnsmith.enchantingoverhauled.api.enchantment.spellfield;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the minimum world {@link Difficulty} required for an AI entity to select and cast an {@link AiSpellField}.
 */
public enum DifficultyGate implements StringRepresentable {

    /**
     * The spell can be cast on Easy, Normal, and Hard difficulties.
     */
    EASY("easy", Difficulty.EASY),

    /**
     * The spell can only be cast on Normal and Hard difficulties.
     */
    NORMAL("normal", Difficulty.NORMAL),

    /**
     * The spell can only be cast on Hard difficulty.
     */
    HARD("hard", Difficulty.HARD);

    /**
     * The serialization codec for encoding and decoding {@link DifficultyGate} values by name.
     */
    public static final Codec<DifficultyGate> CODEC = StringRepresentable.fromEnum(DifficultyGate::values);

    /**
     * The serialized string identifier for this difficulty gate.
     */
    private final String id;

    /**
     * The minimum vanilla {@link Difficulty} required to satisfy this gate.
     */
    private final Difficulty minimumDifficulty;

    /**
     * Constructs a {@code DifficultyGate} enum constant.
     *
     * @param id                The serialized string identifier.
     * @param minimumDifficulty The minimum required {@link Difficulty}.
     */
    DifficultyGate(String id, Difficulty minimumDifficulty) {
        this.id = id;
        this.minimumDifficulty = minimumDifficulty;
    }

    /**
     * Retrieves the minimum world {@link Difficulty} required for this gate.
     *
     * @return The minimum {@link Difficulty}.
     */
    public Difficulty getMinimumDifficulty() {
        return this.minimumDifficulty;
    }

    /**
     * Checks whether the given {@link Difficulty} meets or exceeds this gate's minimum difficulty.
     *
     * @param difficulty The {@link Difficulty} to evaluate, or {@code null}.
     * @return {@code true} if {@code difficulty} is non-null and its ID is at least {@link #minimumDifficulty}; {@code false} otherwise.
     */
    public boolean isMet(@Nullable Difficulty difficulty) {
        return difficulty != null && difficulty.getId() >= this.minimumDifficulty.getId();
    }

    /**
     * Checks whether the difficulty of the given {@link Level} meets or exceeds this gate's minimum difficulty.
     *
     * @param level The {@link Level} whose difficulty should be evaluated, or {@code null}.
     * @return {@code true} if the level's difficulty satisfies this gate; {@code false} otherwise.
     */
    public boolean isMet(@Nullable Level level) {
        return level != null && this.isMet(level.getDifficulty());
    }

    /**
     * Retrieves the serialized string representation of this difficulty gate.
     *
     * @return The serialized name string.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}