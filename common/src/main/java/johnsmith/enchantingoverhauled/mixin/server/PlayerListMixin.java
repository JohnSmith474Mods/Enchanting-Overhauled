package johnsmith.enchantingoverhauled.mixin.server;

import johnsmith.enchantingoverhauled.server.AdminSchoolValidator;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to {@link PlayerList} to alert operators upon login if any enchantments resolve to
 * {@code ABERRATION} (unassigned) or are assigned to multiple Schools of Magic.
 */
@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void enchanting_overhauled$onPlayerLogin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        if (player != null && player.server != null) {
            ArcaneItemNamingUtil.setServerRegistryAccess(player.server.registryAccess());
            AdminSchoolValidator.validateAndAlertPlayer(player);
        }
    }
}