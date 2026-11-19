package johnsmith.enchantingoverhauled.entity.npc;

import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.enchantment.Enchantment;

public class VillagerTradeTags {
    public static TagKey<Enchantment> getCommonTag(Holder<VillagerType> type) {
        if (type.is(VillagerType.DESERT)) return EnchantmentTags.TRADES_DESERT_COMMON;
        if (type.is(VillagerType.JUNGLE)) return EnchantmentTags.TRADES_JUNGLE_COMMON;
        if (type.is(VillagerType.SAVANNA)) return EnchantmentTags.TRADES_SAVANNA_COMMON;
        if (type.is(VillagerType.SNOW)) return EnchantmentTags.TRADES_SNOW_COMMON;
        if (type.is(VillagerType.SWAMP)) return EnchantmentTags.TRADES_SWAMP_COMMON;
        if (type.is(VillagerType.TAIGA)) return EnchantmentTags.TRADES_TAIGA_COMMON;
        return EnchantmentTags.TRADES_PLAINS_COMMON;
    }

    public static TagKey<Enchantment> getSpecialTag(Holder<VillagerType> type) {
        if (type.is(VillagerType.DESERT)) return EnchantmentTags.TRADES_DESERT_SPECIAL;
        if (type.is(VillagerType.JUNGLE)) return EnchantmentTags.TRADES_JUNGLE_SPECIAL;
        if (type.is(VillagerType.SAVANNA)) return EnchantmentTags.TRADES_SAVANNA_SPECIAL;
        if (type.is(VillagerType.SNOW)) return EnchantmentTags.TRADES_SNOW_SPECIAL;
        if (type.is(VillagerType.SWAMP)) return EnchantmentTags.TRADES_SWAMP_SPECIAL;
        if (type.is(VillagerType.TAIGA)) return EnchantmentTags.TRADES_TAIGA_SPECIAL;
        return EnchantmentTags.TRADES_PLAINS_SPECIAL;
    }
}