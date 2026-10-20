package johnsmith.enchantingoverhauled.menu.enchant.controller;

import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantmentAttachment;
import johnsmith.enchantingoverhauled.menu.enchant.model.EnchantSlot;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

/**
 * A sealed interface defining the concrete mutation operations that can be performed
 * by the Enchanting Altar upon the player's inventory.
 * <p>
 * Acts as the Command Pattern interface for menu interactions. Each valid implementation
 * manages its own specific logic for modifying the target item, deducting resource costs,
 * and handling source catalyst consumption.
 */
public sealed interface EnchantAction permits
        EnchantAction.Upgrade,
        EnchantAction.Transfer,
        EnchantAction.Apply,
        EnchantAction.Reroll,
        EnchantAction.None
{

    // region Core Execution
    /**
     * Executes the enchantment action, modifying the container and player state.
     *
     * @param player    The player executing the action.
     * @param inventory The active inventory container holding the target, fuel, and source.
     * @return true if the action was successfully performed, false if it failed (e.g., due to insufficient funds).
     */
    boolean execute(Player player, Container inventory);
    // endregion

    // region Internal Helpers
    /**
     * Helper method to consume the catalyst source item.
     * Evaluates item tags to determine if the source is persistent, fleeting, or standard.
     * Persistent items are ignored. Fleeting items are completely reverted to their recipe inputs.
     * Standard untagged items selectively lose the utilized enchantment, reverting only if emptied.
     *
     * @param player              The player executing the action.
     * @param inventory           The active inventory container.
     * @param consumedEnchantment The specific enchantment to extract.
     */
    private static void consumeSourceEnchantment(Player player, Container inventory, Holder<Enchantment> consumedEnchantment) {
        int sourceSlot = EnchantSlot.SOURCE.index();
        ItemStack source = inventory.getItem(sourceSlot);

        if (source.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE)) {
            return;
        }

        boolean isFleeting = source.is(Tags.FLEETING_ENCHANTMENT_SOURCE);
        ItemEnchantments enchants = EnchantmentLib.getEnchantments(source);

        if (isFleeting) {
            revertSourceItem(player, inventory, sourceSlot, source);
        } else {
            if (enchants.size() <= 1) {
                revertSourceItem(player, inventory, sourceSlot, source);
            } else {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchants);
                mutable.removeIf(holder -> holder.equals(consumedEnchantment));
                source.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            }
        }
    }

    /**
     * Attempts to devolve a consumed enchantment source back to its recipe input,
     * stripping its stored data components as a fallback.
     */
    private static void revertSourceItem(Player player, Container inventory, int sourceSlot, ItemStack source) {
        Optional<EnchantRecipe> recipeOpt = EnchantRecipe.reverse(player.level().registryAccess(), source);
        if (recipeOpt.isPresent()) {
            Optional<Holder<net.minecraft.world.item.Item>> firstInput = recipeOpt.get().input().stream().findFirst();
            if (firstInput.isPresent()) {
                inventory.setItem(sourceSlot, new ItemStack(firstInput.get().value()));
                return;
            }
        }

        ItemStack copy = source.copy();
        copy.remove(DataComponents.STORED_ENCHANTMENTS);
        inventory.setItem(sourceSlot, copy);
    }

    /**
     * Attempts to probabilistically roll for a higher enchantment level based on the
     * target item's inherent enchantability value.
     *
     * @param player       The player executing the action.
     * @param target       The item being enchanted.
     * @param enchantment  The enchantment definition to roll for.
     * @param currentLevel The baseline level to start rolling from.
     * @return The newly rolled level, guaranteed to be at least {@code currentLevel}.
     */
    private static int rollLevel(Player player, ItemStack target, Enchantment enchantment, int currentLevel) {
        int enchantability = target.getItem().getEnchantmentValue();
        Optional<EnchantRecipe> recipeOpt = EnchantRecipe.get(player.level().registryAccess(), target);
        if (recipeOpt.isPresent()) {
            enchantability = recipeOpt.get().enchantability();
        }

        enchantability = Math.clamp(enchantability, 1, 50);
        double successProbability = Math.clamp(2.0D * Math.pow(enchantability / 50.0D, 2.0D), 0.0D, 1.0D);

        int newLevel = currentLevel;

        while (newLevel < enchantment.getMaxLevel()) {
            if (player.getRandom().nextDouble() < successProbability) {
                newLevel++;
            } else {
                break;
            }
        }
        return newLevel;
    }

    /**
     * Triggers the vanilla statistics, advancements, and sound effects for successfully enchanting an item.
     *
     * @param player    The player executing the action.
     * @param target    The item that was successfully enchanted.
     * @param slotIndex The UI slot index clicked.
     */
    private static void awardStatsAndPlaySound(Player player, ItemStack target, int slotIndex) {
        player.awardStat(Stats.ENCHANT_ITEM);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, target, slotIndex);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, player.getRandom().nextFloat() * 0.1F + 0.9F);
    }
    // endregion

    // region Implementations
    /**
     * Action representing an upgrade to an existing enchantment on the target item.
     * <p>
     * Increments the current level and rolls for probabilistic extra levels. Optionally
     * consumes a catalyst from the source slot depending on the configured rule set.
     *
     * @param instance     The enchantment instance detailing the type and newly proposed level.
     * @param currentLevel The current level of the enchantment on the target item before upgrading.
     * @param xpCost       The experience level cost required to perform the upgrade.
     * @param lapisCost    The amount of Lapis Lazuli required to perform the upgrade.
     * @param slotIndex    The UI slot index clicked, used for triggering criteria/stats.
     * @param usesSource   Whether this upgrade relies on consuming the source catalyst book.
     */
    record Upgrade(EnchantmentInstance instance, int currentLevel, int xpCost, int lapisCost, int slotIndex, boolean usesSource) implements EnchantAction {
        @Override
        public boolean execute(Player player, Container inventory) {
            ItemStack target = inventory.getItem(EnchantSlot.TARGET.index());
            ItemStack lapis = inventory.getItem(EnchantSlot.FUEL.index());
            ItemStack source = inventory.getItem(EnchantSlot.SOURCE.index());
            boolean isNotCreative = !player.getAbilities().instabuild;

            if (target.isEmpty() || (isNotCreative && (player.experienceLevel < this.xpCost || lapis.getCount() < this.lapisCost))) {
                return false;
            }

            int newLevel = rollLevel(player, target, this.instance.enchantment.value(), this.currentLevel + 1);

            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentLib.getEnchantments(target));
            builder.set(this.instance.enchantment, newLevel);
            target.set(DataComponents.ENCHANTMENTS, builder.toImmutable());

            if (isNotCreative) {
                player.onEnchantmentPerformed(target, this.xpCost);
                lapis.shrink(this.lapisCost);

                if (lapis.isEmpty()) {
                    inventory.setItem(EnchantSlot.FUEL.index(), ItemStack.EMPTY);
                }

                // Consume the source selectively if it was utilized as a catalyst
                if (this.usesSource && !source.isEmpty()) {
                    consumeSourceEnchantment(player, inventory, this.instance.enchantment);
                }
            }

            awardStatsAndPlaySound(player, target, this.slotIndex);
            return true;
        }
    }

    /**
     * Action representing the transfer of an enchantment from a source book to the target item.
     * <p>
     * Specifically avoids rolling a new level, strictly transferring the level held by the source.
     *
     * @param instance  The enchantment instance detailing the type and level to be transferred.
     * @param xpCost    The experience level cost required to perform the transfer.
     * @param lapisCost The amount of Lapis Lazuli required to perform the transfer.
     * @param slotIndex The UI slot index clicked, used for triggering criteria/stats.
     */
    record Transfer(EnchantmentInstance instance, int xpCost, int lapisCost, int slotIndex) implements EnchantAction {
        @Override
        public boolean execute(Player player, Container inventory) {
            ItemStack target = inventory.getItem(EnchantSlot.TARGET.index());
            ItemStack lapis = inventory.getItem(EnchantSlot.FUEL.index());
            ItemStack source = inventory.getItem(EnchantSlot.SOURCE.index());
            boolean isNotCreative = !player.getAbilities().instabuild;

            if (target.isEmpty() || source.isEmpty() || (isNotCreative && (player.experienceLevel < this.xpCost || lapis.getCount() < this.lapisCost))) {
                return false;
            }

            int newLevel = this.instance.level;
            ItemStack targetCopy = target;

            Optional<EnchantRecipe> recipeOpt = EnchantRecipe.get(player.level().registryAccess(), target);
            if (recipeOpt.isPresent()) {
                EnchantRecipe recipe = recipeOpt.get();
                targetCopy = new ItemStack(recipe.output());
                targetCopy.setCount(1);
                inventory.setItem(EnchantSlot.TARGET.index(), targetCopy);

                DataComponentType<ItemEnchantments> comp = recipe.attachment() == EnchantmentAttachment.STORED ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(targetCopy.getOrDefault(comp, ItemEnchantments.EMPTY));
                mutable.set(this.instance.enchantment, newLevel);
                if (recipe.attachment() != EnchantmentAttachment.VOID)
                    targetCopy.set(comp, mutable.toImmutable());
            } else {
                targetCopy.enchant(this.instance.enchantment, newLevel);
            }

            if (isNotCreative) {
                player.onEnchantmentPerformed(targetCopy, this.xpCost);
                lapis.shrink(this.lapisCost);

                if (lapis.isEmpty()) {
                    inventory.setItem(EnchantSlot.FUEL.index(), ItemStack.EMPTY);
                }

                if (!source.isEmpty()) {
                    consumeSourceEnchantment(player, inventory, this.instance.enchantment);
                }
            }

            awardStatsAndPlaySound(player, targetCopy, this.slotIndex);
            return true;
        }
    }

    /**
     * Action representing a newly generated enchantment randomly supplied by the table itself.
     * <p>
     * Applies the base level and rolls for probabilistic extra levels based on enchantability.
     *
     * @param instance  The newly generated enchantment instance to apply.
     * @param xpCost    The experience level cost required to perform the application.
     * @param lapisCost The amount of Lapis Lazuli required to perform the application.
     * @param slotIndex The UI slot index clicked, used for triggering criteria/stats.
     */
    record Apply(EnchantmentInstance instance, int xpCost, int lapisCost, int slotIndex) implements EnchantAction {
        @Override
        public boolean execute(Player player, Container inventory) {
            ItemStack target = inventory.getItem(EnchantSlot.TARGET.index());
            ItemStack lapis = inventory.getItem(EnchantSlot.FUEL.index());
            boolean isNotCreative = !player.getAbilities().instabuild;

            if (target.isEmpty() || (isNotCreative && (player.experienceLevel < this.xpCost || lapis.getCount() < this.lapisCost))) {
                return false;
            }

            int newLevel = rollLevel(player, target, this.instance.enchantment.value(), this.instance.level);
            ItemStack targetCopy = target;

            Optional<EnchantRecipe> recipeOpt = EnchantRecipe.get(player.level().registryAccess(), target);
            if (recipeOpt.isPresent()) {
                EnchantRecipe recipe = recipeOpt.get();
                targetCopy = new ItemStack(recipe.output());
                targetCopy.setCount(1);
                inventory.setItem(EnchantSlot.TARGET.index(), targetCopy);

                DataComponentType<ItemEnchantments> comp = recipe.attachment() == EnchantmentAttachment.STORED ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(targetCopy.getOrDefault(comp, ItemEnchantments.EMPTY));
                mutable.set(this.instance.enchantment, newLevel);
                if (recipe.attachment() != EnchantmentAttachment.VOID)
                    targetCopy.set(comp, mutable.toImmutable());
            } else {
                targetCopy.enchant(this.instance.enchantment, newLevel);
            }

            if (isNotCreative) {
                player.onEnchantmentPerformed(targetCopy, this.xpCost);
                lapis.shrink(this.lapisCost);

                if (lapis.isEmpty()) {
                    inventory.setItem(EnchantSlot.FUEL.index(), ItemStack.EMPTY);
                }
            }

            awardStatsAndPlaySound(player, targetCopy, this.slotIndex);
            return true;
        }
    }

    /**
     * Action representing a manual re-roll of the table's deterministic seed.
     * <p>
     * Consumes Lapis Lazuli based on the number of enchantments already on the target.
     *
     * @param xpCost    The experience level cost required to reroll the table's seed.
     * @param lapisCost The amount of Lapis Lazuli required to reroll the table's seed.
     */
    record Reroll(int xpCost, int lapisCost) implements EnchantAction {
        @Override
        public boolean execute(Player player, Container inventory) {
            ItemStack target = inventory.getItem(EnchantSlot.TARGET.index());
            ItemStack lapis = inventory.getItem(EnchantSlot.FUEL.index());
            boolean isNotCreative = !player.getAbilities().instabuild;

            if (target.isEmpty() || (isNotCreative && (player.experienceLevel < this.xpCost || lapis.getCount() < this.lapisCost))) {
                return false;
            }

            if (isNotCreative) {
                lapis.shrink(this.lapisCost);
                if (lapis.isEmpty()) {
                    inventory.setItem(EnchantSlot.FUEL.index(), ItemStack.EMPTY);
                }
            }

            player.onEnchantmentPerformed(ItemStack.EMPTY, isNotCreative ? this.xpCost : 0);
            player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, player.getRandom().nextFloat() * 0.1F + 0.9F);
            return true;
        }
    }

    /**
     * A non-action representing a slot that is visually present but functionally disabled.
     * Returns false upon execution attempt.
     */
    record None() implements EnchantAction {
        @Override
        public boolean execute(Player player, Container inventory) {
            return false;
        }
    }
    // endregion
}