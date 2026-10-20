package johnsmith.enchantingoverhauled.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import johnsmith.enchantingoverhauled.Config;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

/**
 * Enchants untagged standard catalysts (like Enchanted Books).
 * Exclusively writes to the STORED_ENCHANTMENTS component and enforces global limits.
 */
public class EnchantSourceFunction extends LootItemConditionalFunction {

    public static final MapCodec<EnchantSourceFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(inst.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(f -> f.options),
                    Codec.BOOL.optionalFieldOf("treasure", false).forGetter(f -> f.treasure)
            )).apply(inst, EnchantSourceFunction::new)
    );

    private final Optional<HolderSet<Enchantment>> options;
    private final boolean treasure;

    protected EnchantSourceFunction(List<LootItemCondition> conditions, Optional<HolderSet<Enchantment>> options, boolean treasure) {
        super(conditions);
        this.options = options;
        this.treasure = treasure;
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        return process(stack, context, this.options, this.treasure);
    }

    public static ItemStack process(ItemStack stack, LootContext context, Optional<HolderSet<Enchantment>> options, boolean allowTreasure) {
        ItemEnchantments current = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (current.size() >= Config.MAX_ENCHANTMENT_AMOUNT.get()) {
            return stack;
        }

        List<Holder<Enchantment>> candidates = LootFunctionUtils.getCandidates(context, options, allowTreasure);
        if (candidates.isEmpty()) return stack;

        Holder<Enchantment> chosen = candidates.get(context.getRandom().nextInt(candidates.size()));

        int minLevel = chosen.value().getMinLevel();
        // Enchantment.getMaxLevel() is already safely clamped by the Core EnchantmentMixin!
        int maxLevel = chosen.value().getMaxLevel();
        int randomLevel = Mth.nextInt(context.getRandom(), minLevel, maxLevel);

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
        mutable.set(chosen, randomLevel);

        stack.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }

    @Override
    public LootItemFunctionType<EnchantSourceFunction> getType() {
        return LootItemFunctionTypes.ENCHANT_SOURCE.type();
    }
}