package johnsmith.enchantingoverhauled.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import johnsmith.enchantingoverhauled.item.EnchantedTomeItem;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

/**
 * Enchants Persistent Sources (like Enchanted Tomes), using custom library logic to
 * heavily stack multiple overleveled STORED enchantments onto the item.
 */
public class EnchantPersistentSourceFunction extends LootItemConditionalFunction {

    public static final MapCodec<EnchantPersistentSourceFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(inst.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(f -> f.options),
                    Codec.BOOL.optionalFieldOf("treasure", false).forGetter(f -> f.treasure)
            )).apply(inst, EnchantPersistentSourceFunction::new)
    );

    private final Optional<HolderSet<Enchantment>> options;
    private final boolean treasure;

    protected EnchantPersistentSourceFunction(List<LootItemCondition> conditions, Optional<HolderSet<Enchantment>> options, boolean treasure) {
        super(conditions);
        this.options = options;
        this.treasure = treasure;
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        return process(stack, context, this.options, this.treasure);
    }

    public static ItemStack process(ItemStack stack, LootContext context, Optional<HolderSet<Enchantment>> options, boolean allowTreasure) {
        List<Holder<Enchantment>> candidates = LootFunctionUtils.getCandidates(context, options, allowTreasure);

        // EnchantmentLib.applyTomeEnchantments inherently writes strictly to STORED_ENCHANTMENTS
        return EnchantedTomeItem.applyTomeEnchantments(stack, candidates, context.getRandom());
    }

    @Override
    public LootItemFunctionType<EnchantPersistentSourceFunction> getType() {
        return LootItemFunctionTypes.ENCHANT_PERSISTENT_SOURCE.type();
    }
}