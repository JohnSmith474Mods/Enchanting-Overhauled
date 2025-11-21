package johnsmith.enchantingoverhauled.loot;

import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TomeLootModifier extends LootModifier {
    public static final Supplier<MapCodec<TomeLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(
                    Codec.STRING.fieldOf("rarity").forGetter(m -> m.rarity)
            ).apply(inst, TomeLootModifier::new)));

    private final String rarity;

    public TomeLootModifier(LootItemCondition[] conditionsIn, String rarity) {
        super(conditionsIn);
        this.rarity = rarity;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        double chance = "rare".equalsIgnoreCase(this.rarity)
                ? Config.RARE_LOOT_CHANCE
                : Config.UNCOMMON_LOOT_CHANCE;

        if (context.getRandom().nextFloat() < chance) {
            ItemStack tome = new ItemStack(Items.ENCHANTED_TOME.get());
            tome = EnchantmentLib.enchantTomeRandomly(tome, context.getLevel(), context.getRandom());
            generatedLoot.add(tome);
        }

        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}