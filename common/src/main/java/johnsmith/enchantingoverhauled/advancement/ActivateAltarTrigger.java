package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * A custom advancement trigger that fires when a player activates an Enchanting Altar.
 * <p>
 * This trigger corresponds to the {@code enchanting_overhauled:activate_altar} criterion ID.
 * It uses the vanilla {@link SimpleCriterionTrigger} system to handle player progress.
 */
public class ActivateAltarTrigger extends SimpleCriterionTrigger<ActivateAltarTrigger.Conditions> {

    /**
     * The unique resource location identifier for this trigger.
     */
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "activate_altar");

    /**
     * Triggers the advancement criterion for the specified player.
     *
     * @param player The player who activated the altar.
     */
    public void trigger(ServerPlayer player) {
        this.trigger(player, (conditions) -> conditions.matches(player));
    }

    /**
     * Gets the codec used to serialize and deserialize the conditions for this trigger.
     *
     * @return The conditions codec.
     */
    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    /**
     * Defines the conditions required to trigger this advancement.
     * <p>
     * Currently, this trigger only checks the standard player predicate, as the specific
     * activation logic is handled by the block interaction event.
     */
    public static class Conditions implements SimpleCriterionTrigger.SimpleInstance {

        private final Optional<ContextAwarePredicate> player;

        /**
         * The codec for serializing these conditions to and from JSON.
         */
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player)
                ).apply(instance, Conditions::new)
        );

        /**
         * Creates a new Conditions instance.
         *
         * @param player The player predicate to check against.
         */
        public Conditions(Optional<ContextAwarePredicate> player) {
            this.player = player;
        }

        /**
         * Gets the player predicate associated with this condition.
         *
         * @return An Optional containing the player predicate.
         */
        @Override
        public Optional<ContextAwarePredicate> player() {
            return this.player;
        }

        /**
         * Checks if the conditions match the given player context.
         *
         * @param player The player to check.
         * @return Always true in this implementation, as the event trigger itself implies success.
         */
        public boolean matches(ServerPlayer player) {
            return true;
        }
    }
}