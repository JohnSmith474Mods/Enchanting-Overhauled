package johnsmith.enchantingoverhauled.mixin.entity.npc;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.entity.npc.EnchantedPageTrade;
import johnsmith.enchantingoverhauled.entity.npc.EnchantedTomeTrade;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Shadow
    public abstract VillagerData getVillagerData();

    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void enchanting_overhauled$modifyLibrarianTrades(ServerLevel level, CallbackInfo ci) {
        VillagerData data = this.getVillagerData();
        if (data.profession().is(VillagerProfession.LIBRARIAN)) {
            Villager villager = (Villager) (Object) this;
            MerchantOffers offers = villager.getOffers();
            int villagerLevel = data.level();
            int index = Math.max(0, villagerLevel - 1);

            if (Config.DISABLE_ENCHANTED_BOOK_TRADES.get() && Config.ENCHANTED_BOOK_TRADE_LEVELS.get().contains(villagerLevel)) {
                offers.removeIf(offer -> offer.getResult().is(Items.ENCHANTED_BOOK));
            }

            if (Config.ENABLE_ENCHANTED_PAGE_TRADES.get() && Config.ENCHANTED_PAGE_TRADE_LEVELS.get().contains(villagerLevel)) {
                List<Integer> xpList = Config.ENCHANTED_PAGE_TRADE_XP.get();
                int xp = xpList.isEmpty() ? 2 : xpList.get(Math.min(index, xpList.size() - 1));

                MerchantOffer pageOffer = new EnchantedPageTrade(xp).getOffer(level, villager, villager.getRandom());
                if (pageOffer != null) {
                    offers.add(pageOffer);
                }
            }

            if (Config.ENABLE_ENCHANTED_TOME_TRADES.get() && Config.ENCHANTED_TOME_TRADE_LEVELS.get().contains(villagerLevel)) {
                List<Integer> xpList = Config.ENCHANTED_TOME_TRADE_XP.get();
                int xp = xpList.isEmpty() ? 0 : xpList.get(Math.min(index, xpList.size() - 1));

                MerchantOffer tomeOffer = new EnchantedTomeTrade(xp).getOffer(level, villager, villager.getRandom());
                if (tomeOffer != null) {
                    offers.add(tomeOffer);
                }
            }
        }
    }
}