package johnsmith.enchantingoverhauled.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantmentAttachment;
import johnsmith.enchantingoverhauled.api.tag.Tags;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

/**
 * A "Super" dispatcher loot function that evaluates recipes and delegates item enchantments
 * to specific strategy functions based on the item's tags and attachment type.
 */
public class EnchantRandomlyFunction extends LootItemConditionalFunction {

    public static final MapCodec<EnchantRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(inst.group(
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter(f -> f.options),
                    Codec.BOOL.optionalFieldOf("treasure", false).forGetter(f -> f.treasure)
            )).apply(inst, EnchantRandomlyFunction::new)
    );

    private final Optional<HolderSet<Enchantment>> options;
    private final boolean treasure;

    public EnchantRandomlyFunction(List<LootItemCondition> conditions, Optional<HolderSet<Enchantment>> options, boolean treasure) {
        super(conditions);
        this.options = options;
        this.treasure = treasure;
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {
        // 1. Evaluate the item's recipe potential first to determine the true output
        Optional<EnchantRecipe> recipeOpt = EnchantRecipe.get(context.getLevel().registryAccess(), stack);

        ItemStack output = stack;
        boolean isStored = stack.has(DataComponents.STORED_ENCHANTMENTS) || stack.is(Items.ENCHANTED_BOOK);

        if (recipeOpt.isPresent()) {
            EnchantRecipe recipe = recipeOpt.get();
            output = new ItemStack(recipe.output().value(), stack.getCount());
            if (recipe.attachment() == EnchantmentAttachment.VOID) return output;
            isStored = recipe.attachment() == EnchantmentAttachment.STORED;
        }

        // 2. Delegate to the strict STORED helpers based on Tags
        if (isStored) {
            if (output.is(Tags.FLEETING_ENCHANTMENT_SOURCE)) {
                return EnchantFleetingSourceFunction.process(output, context, this.options, this.treasure);
            } else if (output.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE)) {
                return EnchantPersistentSourceFunction.process(output, context, this.options, this.treasure);
            } else {
                return EnchantSourceFunction.process(output, context, this.options, this.treasure);
            }
        }
        // 3. Process standard APPLIED items (like Weapons/Armor) mirroring Vanilla's logic with strict limits
        else if (output.isEnchantable()) {
            ItemEnchantments current = output.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

            if (current.size() >= Config.MAX_ENCHANTMENT_AMOUNT.get()) {
                return output;
            }

            List<Holder<Enchantment>> candidates = LootFunctionUtils.getCandidates(context, this.options, this.treasure);
            if (candidates.isEmpty()) return output;

            Holder<Enchantment> chosen = candidates.get(context.getRandom().nextInt(candidates.size()));
            int randomLevel = Mth.nextInt(context.getRandom(), chosen.value().getMinLevel(), chosen.value().getMaxLevel());

            output.enchant(chosen, randomLevel);
        }

        return output;
    }

    @Override
    public LootItemFunctionType<EnchantRandomlyFunction> getType() {
        return LootItemFunctionTypes.ENCHANT_RANDOMLY.type();
    }
}