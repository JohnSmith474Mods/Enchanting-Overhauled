package johnsmith.enchantingoverhauled.mixin.entity.player;

import johnsmith.enchantingoverhauled.Config;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Shadow
    public int experienceLevel;

    @Inject(method = "getXpNeededForNextLevel", at = @At("HEAD"), cancellable = true)
    private void onGetNextLevelExperience(CallbackInfoReturnable<Integer> cir) {
        if (!Config.ENABLE_XP_MODIFICATIONS.get()) return;
        int currentLevel = this.experienceLevel;
        int step = (int) Math.round(Math.floor((double) currentLevel / Config.XP_LEVEL_BRACKET_SIZE.get()));
        cir.setReturnValue((Config.XP_GROWTH_FACTOR.get() * Math.clamp(step, 1, Integer.MAX_VALUE)) + Config.XP_GROWTH_Y_OFFSET.get());
    }

    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void onAddExperienceLevels(int levels, CallbackInfo ci) {
        if (!Config.CAP_MAX_XP_LEVEL.get() || !Config.ENABLE_XP_MODIFICATIONS.get()) {
            return;
        }
        if (levels > 0 && this.experienceLevel >= Config.XP_MAX_LEVEL.get()) {
            ci.cancel();
        }
    }
}