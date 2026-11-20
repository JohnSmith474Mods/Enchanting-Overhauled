package johnsmith.enchantingoverhauled.entity.npc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.enchantment.Enchantment;

public class VillagerTradeTags {
    private static final TagKey<Enchantment> TRADES_DESERT_SPECIAL = specialTag("trades/desert_special");
    private static final TagKey<Enchantment> TRADES_JUNGLE_SPECIAL = specialTag("trades/jungle_special");
    private static final TagKey<Enchantment> TRADES_PLAINS_SPECIAL = specialTag("trades/plains_special");
    private static final TagKey<Enchantment> TRADES_SAVANNA_SPECIAL = specialTag("trades/savanna_special");
    private static final TagKey<Enchantment> TRADES_SNOW_SPECIAL = specialTag("trades/snow_special");
    private static final TagKey<Enchantment> TRADES_SWAMP_SPECIAL = specialTag("trades/swamp_special");
    private static final TagKey<Enchantment> TRADES_TAIGA_SPECIAL = specialTag("trades/taiga_special");

    private static TagKey<Enchantment> specialTag(String path) {
        return TagKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace(path));
    }

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
        if (type.is(VillagerType.DESERT)) return TRADES_DESERT_SPECIAL;
        if (type.is(VillagerType.JUNGLE)) return TRADES_JUNGLE_SPECIAL;
        if (type.is(VillagerType.SAVANNA)) return TRADES_SAVANNA_SPECIAL;
        if (type.is(VillagerType.SNOW)) return TRADES_SNOW_SPECIAL;
        if (type.is(VillagerType.SWAMP)) return TRADES_SWAMP_SPECIAL;
        if (type.is(VillagerType.TAIGA)) return TRADES_TAIGA_SPECIAL;
        return TRADES_PLAINS_SPECIAL;
    }
}