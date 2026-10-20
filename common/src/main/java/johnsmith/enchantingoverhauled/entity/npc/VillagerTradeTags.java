package johnsmith.enchantingoverhauled.entity.npc;

import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.enchantment.Enchantment;

public class VillagerTradeTags {
    public static TagKey<Enchantment> getCommonTag(VillagerType type) {
        if (type == VillagerType.DESERT) return EnchantmentTags.TRADES_DESERT_COMMON;
        if (type == VillagerType.JUNGLE) return EnchantmentTags.TRADES_JUNGLE_COMMON;
        if (type == VillagerType.SAVANNA) return EnchantmentTags.TRADES_SAVANNA_COMMON;
        if (type == VillagerType.SNOW) return EnchantmentTags.TRADES_SNOW_COMMON;
        if (type == VillagerType.SWAMP) return EnchantmentTags.TRADES_SWAMP_COMMON;
        if (type == VillagerType.TAIGA) return EnchantmentTags.TRADES_TAIGA_COMMON;
        return EnchantmentTags.TRADES_PLAINS_COMMON;
    }

    public static TagKey<Enchantment> getSpecialTag(VillagerType type) {
        if (type == VillagerType.DESERT) return EnchantmentTags.TRADES_DESERT_SPECIAL;
        if (type == VillagerType.JUNGLE) return EnchantmentTags.TRADES_JUNGLE_SPECIAL;
        if (type == VillagerType.SAVANNA) return EnchantmentTags.TRADES_SAVANNA_SPECIAL;
        if (type == VillagerType.SNOW) return EnchantmentTags.TRADES_SNOW_SPECIAL;
        if (type == VillagerType.SWAMP) return EnchantmentTags.TRADES_SWAMP_SPECIAL;
        if (type == VillagerType.TAIGA) return EnchantmentTags.TRADES_TAIGA_SPECIAL;
        return EnchantmentTags.TRADES_PLAINS_SPECIAL;
    }
}