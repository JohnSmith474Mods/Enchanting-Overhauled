package johnsmith.enchantingoverhauled.menu.scribe.controller;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.damagesource.DamageTypes;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

/**
 * A sealed interface defining the concrete mutation operations that can be performed
 * by the Scribe Minigame upon the player's inventory and the world.
 * <p>
 * Acts as the Command Pattern interface for menu interactions. Each valid implementation
 * manages its own specific logic for modifying items, deducting resource costs,
 * playing sounds, and validating side-effects.
 */
public sealed interface ScribeAction permits
        ScribeAction.Start,
        ScribeAction.Win,
        ScribeAction.Lose,
        ScribeAction.AcknowledgeLoss,
        ScribeAction.TakeBook
{

    /**
     * Executes the action, modifying the container and the player's state.
     *
     * @param player    The player executing the action.
     * @param container The active container holding the fuel and source.
     * @param gridData  The synchronized data matrix.
     * @param access    The container's level access to mutate blocks.
     * @return true if the action was successfully performed, false otherwise.
     */
    boolean execute(Player player, Container container, ScribeGridData gridData, ContainerLevelAccess access);

    /**
     * Action representing the start of a Scribe minigame.
     * Deducts the entry lapis and conditionally consumes the source based on Abortion Refund Policies.
     */
    record Start(ItemStack source) implements ScribeAction {
        @Override
        public boolean execute(Player player, Container container, ScribeGridData gridData, ContainerLevelAccess access) {
            if (!player.isCreative()) {
                if (!Config.SCRIBE_REFUND_ENCHANT_FUEL.get()) {
                    container.getItem(1).shrink(gridData.getLapisCost());
                }
                if (ScribeRules.isProcessedAtStart(source)) {
                    ScribeRules.processSourceInPlace(player.level(), container, gridData, source);
                }
            }
            return true;
        }
    }

    /**
     * Action representing a victory.
     * Rewards experience, evaluates tome conversions, and merges the targeted enchantment into the lectern book.
     */
    record Win(ItemStack consumedSource) implements ScribeAction {
        @Override
        public boolean execute(Player player, Container container, ScribeGridData gridData, ContainerLevelAccess access) {
            if (!player.isCreative()) {
                if (Config.SCRIBE_REFUND_ENCHANT_FUEL.get()) {
                    container.getItem(1).shrink(gridData.getLapisCost());
                }
                if (!ScribeRules.isProcessedAtStart(consumedSource)) {
                    ScribeRules.processSourceInPlace(player.level(), container, gridData, consumedSource);
                }
                int xpReward = gridData.getXpReward();
                if (xpReward > 0) {
                    player.giveExperiencePoints(xpReward);
                }
            }

            access.execute((level, pos) -> {
                this.applyScribeReward(level, pos, consumedSource, gridData);

                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof LecternBlockEntity lectern) {
                    boolean isTome = lectern.getBook().is(Items.ENCHANTED_TOME.get());
                    gridData.setIsTome(isTome);
                }
            });

            return true;
        }

        private void applyScribeReward(Level level, BlockPos pos, ItemStack sourceStack, ScribeGridData gridData) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof LecternBlockEntity lectern)) return;

            ItemStack book = lectern.getBook();
            ItemEnchantments sourceEnchants = EnchantmentLib.getEnchantments(sourceStack);
            if (sourceEnchants.isEmpty()) return;

            int selectedId = gridData.getSelectedEnchantmentIndex();
            Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Holder<Enchantment> sourceEnch = null;
            int sourceLevel = 0;

            for (var entry : sourceEnchants.entrySet()) {
                if (registry.getId(entry.getKey().value()) == selectedId) {
                    sourceEnch = entry.getKey();
                    sourceLevel = entry.getIntValue();
                    break;
                }
            }

            if (sourceEnch == null) return;

            ItemEnchantments targetEnchants = EnchantmentLib.getEnchantments(book);
            int targetLevel = targetEnchants.getLevel(sourceEnch);
            int vanillaMax = sourceEnch.value().getMaxLevel();

            boolean isTome = book.is(Items.ENCHANTED_TOME.get());
            int projectedLevel = targetLevel == 0 ? sourceLevel : targetLevel + sourceLevel;

            boolean shouldUpgradeToTome = isTome;
            if (!isTome) {
                if (targetLevel == 0) {
                    shouldUpgradeToTome = ScribeRules.willConvertOnAppend(targetEnchants.size() + 1);
                } else {
                    shouldUpgradeToTome = ScribeRules.willConvertOnUpgrade(projectedLevel, vanillaMax);
                }
            }

            int absoluteMax = (shouldUpgradeToTome && Config.CAN_TOMES_HAVE_GREATER_ENCHANTMENTS.get()) ? vanillaMax + 1 : vanillaMax;
            int newLevel = Math.min(projectedLevel, absoluteMax);

            ItemStack newBook = book.copy();
            if (shouldUpgradeToTome && !isTome) {
                newBook = new ItemStack(Items.ENCHANTED_TOME.get());
                newBook.set(DataComponents.STORED_ENCHANTMENTS, targetEnchants);
            }

            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(EnchantmentLib.getEnchantments(newBook));
            mutable.set(sourceEnch, newLevel);
            newBook.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());

            lectern.setBook(newBook);
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * Action representing a loss.
     * Deducts lapis if refunded, and terminates the hostage source item entirely.
     */
    record Lose(ItemStack consumedSource) implements ScribeAction {
        @Override
        public boolean execute(Player player, Container container, ScribeGridData gridData, ContainerLevelAccess access) {
            if (!player.isCreative()) {
                if (Config.SCRIBE_REFUND_ENCHANT_FUEL.get()) {
                    container.getItem(1).shrink(gridData.getLapisCost());
                }
                if (!ScribeRules.isProcessedAtStart(consumedSource) && !consumedSource.is(Tags.PERSISTENT_ENCHANTMENT_SOURCE)) {
                    // Terminate the hostage source completely
                    container.setItem(0, ItemStack.EMPTY);
                }
            }
            return true;
        }
    }

    /**
     * Action representing the player acknowledging they lost the game.
     * Triggers the Arcane Retribution penalty and forcibly kicks them from the menu.
     */
    record AcknowledgeLoss() implements ScribeAction {
        @Override
        public boolean execute(Player player, Container container, ScribeGridData gridData, ContainerLevelAccess access) {
            access.execute((level, pos) -> this.triggerLossPenalty(level, pos, player));

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.closeContainer();
            }
            return true;
        }

        private void triggerLossPenalty(Level level, BlockPos pos, Player player) {
            boolean isArcaneRetributionActivate = Config.LETHAL_SCRIBING.get();
            if (isArcaneRetributionActivate) {
                if (!level.isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) level;

                    // Vex sounds + Soul particles for catastrophic explosion
                    serverLevel.playSound(null, pos, SoundEvents.VEX_DEATH, SoundSource.BLOCKS, 2.0F, 0.0F);
                    serverLevel.sendParticles(
                            ParticleTypes.SOUL,
                            pos.getX() + 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5,
                            1500,
                            4.0F, 4.0F, 4.0F,
                            0.0
                    );

                    // Destroy the lectern immediately
                    serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                    // Retrieve the custom damage type
                    DamageSource arcaneRetribution = new DamageSource(
                            level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ARCANE_RETRIBUTION)
                    );

                    serverLevel.explode(
                            (Entity) null,
                            arcaneRetribution,
                            null,
                            pos.getCenter(),
                            5.0F,
                            true,
                            Level.ExplosionInteraction.BLOCK
                    );
                }
            } else {
                player.displayClientMessage(Component.translatable("block.enchanting_overhauled.deactivated_enchanting_table.wrong_catalyst"), true);
            }
        }
    }

    /**
     * Action representing the player extracting the target book from the lectern directly through the UI.
     */
    record TakeBook() implements ScribeAction {
        @Override
        public boolean execute(Player player, Container container, ScribeGridData gridData, ContainerLevelAccess access) {
            if (!player.mayBuild()) {
                return false;
            }
            access.execute((level, pos) -> {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof LecternBlockEntity lectern) {
                    ItemStack book = lectern.getBook().copy();
                    lectern.clearContent();
                    LecternBlock.resetBookState(player, level, pos, level.getBlockState(pos), false);

                    if (!player.getInventory().add(book)) {
                        player.drop(book, false);
                    }
                    if (player instanceof ServerPlayer sp) {
                        sp.closeContainer();
                    }
                }
            });
            return true;
        }
    }
}