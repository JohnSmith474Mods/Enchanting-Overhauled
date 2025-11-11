package johnsmith.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.LootContextPredicate; // <-- IMPORT THIS
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import johnsmith.EnchantingOverhauled;

import java.util.Optional;

public class ActivateAltarTrigger extends AbstractCriterion<ActivateAltarTrigger.Conditions> {

    // The unique ID for this trigger
    public static final Identifier ID = new Identifier(EnchantingOverhauled.MOD_ID, "activate_altar");

    /**
     * Fires the trigger for the given player.
     * @param player The player to check advancements for.
     */
    public void trigger(ServerPlayerEntity player) {
        // This calls the protected AbstractCriterion.trigger method.
        // The lambda checks our custom 'matches' method.
        this.trigger(player, (conditions) -> conditions.matches(player));
    }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    /**
     * The conditions class for this trigger.
     * It now implements the AbstractCriterion.Conditions interface.
     */
    public static class Conditions implements AbstractCriterion.Conditions {

        // 1. Store the player predicate
        private final Optional<LootContextPredicate> player;

        // 2. Update the Codec to use LootContextPredicate
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        // This is the new way to get the player predicate
                        LootContextPredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player)
                ).apply(instance, Conditions::new)
        );

        // 3. Update the constructor
        public Conditions(Optional<LootContextPredicate> player) {
            this.player = player;
        }

        // 4. Implement the 'player()' method from the interface
        @Override
        public Optional<LootContextPredicate> player() {
            return this.player;
        }

        // 5. Your custom 'matches' check.
        // This is for any *extra* conditions. Since you have none, 'true' is correct.
        // The 'player' predicate itself is checked automatically by the parent 'trigger' method.
        public boolean matches(ServerPlayerEntity player) {
            return true;
        }
    }
}