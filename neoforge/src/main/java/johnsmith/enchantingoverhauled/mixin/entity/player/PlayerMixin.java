package johnsmith.enchantingoverhauled.mixin.entity.player;

import johnsmith.enchantingoverhauled.config.Config;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to {@link Player} to adjust core mechanics related to experience and tool efficiency.
 * <p>
 * This class implements:
 * <ul>
 * <li><b>Efficiency Scaling:</b> Adjusts the calculation of the Efficiency enchantment boost to
 * remain viable even if the maximum enchantment level is capped at 1 or 2.</li>
 * <li><b>XP Curve:</b> Flattens or modifies the experience requirement per level to support
 * the mod's new progression system (often linearizing it).</li>
 * <li><b>Level Cap:</b> Enforces a hard limit on the player's experience level.</li>
 * </ul>
 */
@Mixin(Player.class)
public class PlayerMixin {

    @Shadow
    public int experienceLevel;

    /**
     * Overrides the XP required to reach the next level.
     * <p>
     * Vanilla uses a curve that gets exponentially steeper. This mod uses a "bracketed"
     * or linear growth system defined by the config to make higher levels more attainable
     * for frequent enchanting.
     *
     * @param cir Callback to set the return value (the XP amount).
     */
    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    private void onGetNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        int currentLevel = this.experienceLevel;
        // Calculate step based on bracket size
        int step = (int) Math.round(Math.floor((double) currentLevel / Config.BOUNDED_XP_LEVEL_BRACKET_SIZE.get()));
        // Linear growth formula: (Slope * BracketStep) + InitialOffset
        cir.setReturnValue((Config.BOUNDED_XP_GROWTH_FACTOR.get() * Math.clamp(step, 1, Integer.MAX_VALUE)) + Config.BOUNDED_XP_GROWTH_Y_OFFSET.get());
    }

    /**
     * Prevents the player from gaining levels beyond the configured maximum.
     *
     * @param levels The amount of levels being added.
     * @param ci Callback info to cancel the event if the cap is reached.
     */
    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void onAddExperienceLevels(int levels, CallbackInfo ci) {
        if (Config.BOUNDED_XP_MAX_LEVEL.get() <= 0) {
            return;
        }
        if (levels > 0 && this.experienceLevel >= Config.BOUNDED_XP_MAX_LEVEL.get()) {
            ci.cancel();
        }
    }
}