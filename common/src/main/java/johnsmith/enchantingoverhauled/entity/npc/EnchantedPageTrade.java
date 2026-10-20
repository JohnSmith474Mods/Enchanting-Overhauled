package johnsmith.enchantingoverhauled.entity.npc;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.item.EnchantedPageItem;
import johnsmith.enchantingoverhauled.item.Items;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record EnchantedPageTrade(
        int xp
) implements VillagerTrades.ItemListing {
    @Override
    public @Nullable MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (!(trader instanceof Villager villager)) {
            return null;
        }

        VillagerType type = villager.getVillagerData().getType();
        TagKey<Enchantment> commonTag = VillagerTradeTags.getCommonTag(type);
        TagKey<Enchantment> specialTag = VillagerTradeTags.getSpecialTag(type);

        ItemStack pageStack = EnchantedPageItem.enchantRandomly(
                new ItemStack(Items.ENCHANTED_PAGE.get()),
                trader.level(),
                random,
                specialTag,
                commonTag
        );

        int cost = Mth.clamp(
                Config.ENCHANTED_PAGE_TRADE_MIN_COST_MODIFIER.get() + random.nextInt(Config.ENCHANTED_PAGE_TRADE_MAX_COST_MODIFIER.get()),
                1,
                Config.ENCHANTED_PAGE_TRADE_MAX_COST_MODIFIER.get()
        );

        ItemEnchantments enchants = pageStack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchants != null && !enchants.isEmpty()) {
            Holder<Enchantment> appliedEnchantment = enchants.keySet().iterator().next();
            if (appliedEnchantment.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                cost *= 2;
            }
        }

        cost = Math.min(cost, 64);

        Optional<ItemCost> secondaryCost = Config.ENABLE_ENCHANTED_PAGE_SECONDARY_COST.get()
                ? Optional.of(new ItemCost(Config.ENCHANTED_PAGE_SECONDARY_COST.get(), 1))
                : Optional.empty();

        return new MerchantOffer(
                new ItemCost(net.minecraft.world.item.Items.EMERALD, cost),
                secondaryCost,
                pageStack,
                Config.ENCHANTED_PAGE_TRADE_AMOUNT.get(),
                xp,
                Config.ENCHANTED_PAGE_TRADE_DEMAND_MARKUP.get()
        );
    }
}