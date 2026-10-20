package johnsmith.enchantingoverhauled.entity.ai.goal;

import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantRecipe;
import johnsmith.enchantingoverhauled.api.enchantment.recipe.EnchantmentAttachment;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentThemes;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import johnsmith.enchantingoverhauled.api.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.api.tag.Tags;
import johnsmith.enchantingoverhauled.entity.AbstractFlyingBookEntity;
import johnsmith.enchantingoverhauled.entity.EnchantedBookEntity;
import johnsmith.enchantingoverhauled.entity.anim.BookAnimationState;
import johnsmith.enchantingoverhauled.item.EnchantedTomeItem;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.particle.SimpleParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * AI Goal that allows idle {@link AbstractFlyingBookEntity} instances (specifically Tomes)
 * to telekinetically pull nearby fuel/catalyst items and barter with players.
 * <p>
 * Optimized to throttle idle {@link ItemEntity} bounding-box scans to once every 10 ticks
 * and reuse cached nearby item entities during telekinetic pulling.
 */
public class TomeBarterGoal extends Goal {
    private static final int BARTER_DURATION_TICKS = 60;
    private static final int WAIT_FOR_SECOND_ITEM_TICKS = 200;
    private static final int BARTER_FAILURE_COOLDOWN_TICKS = 300;
    private static final double TELEKINESIS_RANGE = 6.0D;

    private static final int IDLE_ITEM_SCAN_INTERVAL_TICKS = 10;
    private static final int ACTIVE_PULL_SCAN_INTERVAL_TICKS = 4;

    private final AbstractFlyingBookEntity book;
    private int barterTicks = 0;
    private int waitTicks = 0;
    private int itemScanCooldown = 0;
    private List<ItemEntity> cachedBarterItems = List.of();

    public TomeBarterGoal(AbstractFlyingBookEntity book) {
        this.book = book;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.book.getTarget() != null || this.book.isAngry() || this.book.getBarterCooldown() > 0) {
            return false;
        }
        if (this.hasBothInputs() || this.hasPartialInput()) {
            return true;
        }
        if (this.itemScanCooldown > 0) {
            this.itemScanCooldown--;
            return false;
        }
        this.cachedBarterItems = this.findNearbyBarterItemEntities();
        if (!this.cachedBarterItems.isEmpty()) {
            return true;
        }
        this.itemScanCooldown = IDLE_ITEM_SCAN_INTERVAL_TICKS;
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.book.getTarget() != null || this.book.isAngry() || this.book.getBarterCooldown() > 0) {
            return false;
        }
        if (this.hasBothInputs()) {
            return this.barterTicks < BARTER_DURATION_TICKS;
        }
        if (this.hasPartialInput()) {
            return this.waitTicks < WAIT_FOR_SECOND_ITEM_TICKS;
        }
        return this.hasAnyValidCachedItem();
    }

    @Override
    public void start() {
        this.barterTicks = 0;
        this.waitTicks = 0;
        this.itemScanCooldown = 0;
        this.book.getNavigation().stop();
        this.book.setAnimationState(BookAnimationState.BARTERING);
    }

    @Override
    public void stop() {
        if (this.book.getTarget() != null
                || this.book.isAngry()
                || this.book.getBarterCooldown() > 0
                || this.waitTicks >= WAIT_FOR_SECOND_ITEM_TICKS) {
            this.book.ejectBarterItems();
        }
        this.barterTicks = 0;
        this.waitTicks = 0;
        this.cachedBarterItems = List.of();
        if (this.book.getAnimationState() == BookAnimationState.BARTERING) {
            this.book.setAnimationState(BookAnimationState.IDLE);
        }
    }

    @Override
    public void tick() {
        this.book.getNavigation().stop();
        this.book.setDeltaMovement(this.book.getDeltaMovement().scale(0.6D));
        this.book.setAnimationState(BookAnimationState.BARTERING);

        Player nearestPlayer = this.book.level().getNearestPlayer(this.book, 8.0D);
        if (nearestPlayer != null) {
            this.book.getLookControl().setLookAt(nearestPlayer, 30.0F, 30.0F);
        }

        if (!this.hasBothInputs()) {
            this.pullNearbyItemsTelekinetically();
            this.waitTicks++;
            return;
        }

        this.waitTicks = 0;
        this.barterTicks++;

        if (this.barterTicks % 15 == 0) {
            this.book.level().playSound(
                    null,
                    this.book.getX(),
                    this.book.getY(),
                    this.book.getZ(),
                    SoundEvents.BOOK_PAGE_TURN,
                    SoundSource.NEUTRAL,
                    0.8F,
                    1.0F + this.book.getRandom().nextFloat() * 0.3F
            );
        }

        if (this.barterTicks >= BARTER_DURATION_TICKS && this.book.level() instanceof ServerLevel serverLevel) {
            this.completeBarter(serverLevel, nearestPlayer);
        }
    }

    private void pullNearbyItemsTelekinetically() {
        if (this.book.getBarterCooldown() > 0) {
            return;
        }
        boolean needsFuel = this.book.getBarterFuel().isEmpty();
        boolean needsCatalyst = this.book.getBarterCatalyst().isEmpty();
        if (!needsFuel && !needsCatalyst) {
            return;
        }

        // Refresh the nearby ItemEntity list every 4 ticks while applying pull velocity every tick
        if (--this.itemScanCooldown <= 0 || this.cachedBarterItems.isEmpty()) {
            this.itemScanCooldown = ACTIVE_PULL_SCAN_INTERVAL_TICKS;
            this.cachedBarterItems = this.findNearbyBarterItemEntities();
        }

        for (ItemEntity itemEntity : this.cachedBarterItems) {
            if (!itemEntity.isAlive()) {
                continue;
            }
            ItemStack stack = itemEntity.getItem();
            boolean matchFuel = needsFuel && isValidBarterFuel(stack);
            boolean matchCatalyst = needsCatalyst && isValidBarterCatalyst(this.book.level().registryAccess(), stack);
            if (!matchFuel && !matchCatalyst) {
                continue;
            }

            this.book.getLookControl().setLookAt(itemEntity, 30.0F, 30.0F);
            Vec3 toBook = this.book.position().add(0.0D, 0.4D, 0.0D).subtract(itemEntity.position());
            double dist = toBook.length();

            if (dist < 1.15D) {
                ItemStack single = stack.split(1);
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
                if (matchFuel) {
                    this.book.setBarterFuel(single);
                    needsFuel = false;
                } else {
                    this.book.setBarterCatalyst(single);
                    needsCatalyst = false;
                }
                this.book.level().playSound(
                        null,
                        this.book.getX(),
                        this.book.getY(),
                        this.book.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.NEUTRAL,
                        0.6F,
                        1.2F
                );
                if (!needsFuel && !needsCatalyst) {
                    break;
                }
            } else {
                Vec3 pullVel = toBook.normalize().scale(0.18D);
                itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().scale(0.5D).add(pullVel));
                itemEntity.hasImpulse = true;
            }
        }
    }

    private boolean hasAnyValidCachedItem() {
        for (ItemEntity item : this.cachedBarterItems) {
            if (item.isAlive() && !item.getItem().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void completeBarter(ServerLevel level, Player recipient) {
        ItemStack fuel = this.book.getBarterFuel();
        ItemStack catalyst = this.book.getBarterCatalyst();
        if (fuel.isEmpty() || catalyst.isEmpty()) {
            return;
        }

        ItemStack result = this.craftBarterOutput(level, catalyst);
        if (result.isEmpty()) {
            this.book.ejectBarterItems();
            this.book.setBarterCooldown(BARTER_FAILURE_COOLDOWN_TICKS);
            level.playSound(
                    null,
                    this.book.getX(),
                    this.book.getY(),
                    this.book.getZ(),
                    SoundEvents.LLAMA_SPIT,
                    SoundSource.NEUTRAL,
                    0.9F,
                    0.9F + (this.book.getRandom().nextFloat() - 0.5F) * 0.2F
            );
            level.sendParticles(
                    ParticleTypes.SMOKE,
                    this.book.getX(),
                    this.book.getY() + 0.45D,
                    this.book.getZ(),
                    14,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.02D
            );
            return;
        }

        this.book.setBarterFuel(ItemStack.EMPTY);
        this.book.setBarterCatalyst(ItemStack.EMPTY);

        double spawnX = this.book.getX();
        double spawnY = this.book.getY() + 0.35D;
        double spawnZ = this.book.getZ();
        ItemEntity outputEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, result);

        if (recipient != null) {
            Vec3 dir = recipient.position().add(0.0D, 0.5D, 0.0D).subtract(this.book.position()).normalize().scale(0.25D);
            outputEntity.setDeltaMovement(dir.x, Math.max(0.1D, dir.y), dir.z);
        } else {
            outputEntity.setDeltaMovement(0.0D, 0.15D, 0.0D);
        }
        outputEntity.setDefaultPickUpDelay();
        level.addFreshEntity(outputEntity);

        level.playSound(
                null,
                this.book.getX(),
                this.book.getY(),
                this.book.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F + (this.book.getRandom().nextFloat() - 0.5F) * 0.2F
        );
        level.sendParticles(
                SimpleParticleTypes.SGA_RUNE.type(),
                this.book.getX(),
                this.book.getY() + 0.5D,
                this.book.getZ(),
                24,
                0.35D,
                0.35D,
                0.35D,
                0.08D
        );
    }

    private ItemStack craftBarterOutput(ServerLevel level, ItemStack catalyst) {
        RegistryAccess registryAccess = level.registryAccess();
        Optional<EnchantRecipe> recipeOpt = EnchantRecipe.get(registryAccess, catalyst);

        if (recipeOpt.isPresent()) {
            EnchantRecipe recipe = recipeOpt.get();
            ItemStack output = new ItemStack(recipe.output().value(), 1);
            if (recipe.attachment() == EnchantmentAttachment.VOID) {
                return output;
            }
            if (output.is(Items.ENCHANTED_PAGE.get())) {
                return this.book.createShedPageStack(level);
            }
            if (output.is(Items.ENCHANTED_TOME.get())) {
                ItemStack enchanted = EnchantedTomeItem.enchantWithThemeAndSchool(
                        output,
                        registryAccess,
                        this.book.getRandom(),
                        this.book.getThemeKey(),
                        this.book.getSchoolKey()
                );
                ArcaneItemNamingUtil.setExplicitAttribution(enchanted, this.book.getThemeKey(), this.book.getSchoolKey());
                return enchanted.isEnchanted() ? enchanted : ItemStack.EMPTY;
            }
            if (output.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
                ItemStack bookOut = EnchantedBookEntity.createBookStackForThemeAndSchool(
                        registryAccess,
                        this.book.getRandom(),
                        this.book.getThemeKey(),
                        this.book.getSchoolKey()
                );
                ArcaneItemNamingUtil.setExplicitAttribution(bookOut, this.book.getThemeKey(), this.book.getSchoolKey());
                return bookOut.isEnchanted() ? bookOut : ItemStack.EMPTY;
            }

            boolean stored = recipe.attachment() == EnchantmentAttachment.STORED;
            if (this.imbueFromBookIdentity(level, output, stored) && output.isEnchanted()) {
                return output;
            }
            return ItemStack.EMPTY;
        }

        ItemStack copy = catalyst.copyWithCount(1);
        boolean stored = copy.has(DataComponents.STORED_ENCHANTMENTS);
        if (this.imbueFromBookIdentity(level, copy, stored) && copy.isEnchanted()) {
            return copy;
        }
        return ItemStack.EMPTY;
    }

    private boolean imbueFromBookIdentity(ServerLevel level, ItemStack target, boolean stored) {
        ItemStack mainHand = this.book.getMainHandItem();
        ItemEnchantments bookEnchants = EnchantmentLib.getEnchantments(mainHand);
        ItemEnchantments existingTargetEnchants = EnchantmentLib.getEnchantments(target);

        List<Holder<Enchantment>> compatibleFromBook = new ArrayList<>();
        for (Holder<Enchantment> holder : bookEnchants.keySet()) {
            if (isEnchantmentApplicable(holder, target, existingTargetEnchants, stored)) {
                compatibleFromBook.add(holder);
            }
        }

        if (!compatibleFromBook.isEmpty()) {
            Holder<Enchantment> chosen = compatibleFromBook.get(this.book.getRandom().nextInt(compatibleFromBook.size()));
            int lvl = Math.max(1, Math.min(bookEnchants.getLevel(chosen), chosen.value().getMaxLevel()));
            applyEnchantmentToStack(target, chosen, lvl, stored);
            return true;
        }

        Registry<Enchantment> registry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ResourceKey<EnchantmentTheme> themeKey = this.book.getThemeKey();
        ResourceKey<MagicSchool> schoolKey = this.book.getSchoolKey();
        boolean hasSpecificTheme = !EnchantmentThemes.isCommon(themeKey);
        boolean hasSpecificSchool = !MagicSchools.isAberration(schoolKey);

        List<Holder<Enchantment>> bothMatches = new ArrayList<>();
        List<Holder<Enchantment>> primaryMatches = new ArrayList<>();
        List<Holder<Enchantment>> wildcardMatches = new ArrayList<>();

        for (Holder.Reference<Enchantment> holder : registry.holders().toList()) {
            if (!isEnchantmentApplicable(holder, target, existingTargetEnchants, stored)) {
                continue;
            }
            boolean themeMatch = hasSpecificTheme && holder.is(EnchantmentThemes.getThemeTag(themeKey));
            boolean schoolMatch = hasSpecificSchool && holder.is(MagicSchools.getSchoolTag(schoolKey));
            if (themeMatch && schoolMatch) {
                bothMatches.add(holder);
            } else if (themeMatch || schoolMatch) {
                primaryMatches.add(holder);
            } else if (!hasSpecificTheme && !hasSpecificSchool) {
                wildcardMatches.add(holder);
            }
        }

        List<Holder<Enchantment>> pool;
        if (!bothMatches.isEmpty()) {
            pool = bothMatches;
        } else if (!primaryMatches.isEmpty()) {
            pool = primaryMatches;
        } else if (!wildcardMatches.isEmpty()) {
            pool = wildcardMatches;
        } else {
            return false;
        }

        Holder<Enchantment> chosen = pool.get(this.book.getRandom().nextInt(pool.size()));
        int lvl = chosen.value().getMinLevel();
        applyEnchantmentToStack(target, chosen, lvl, stored);
        return true;
    }

    private static boolean isEnchantmentApplicable(
            Holder<Enchantment> candidate,
            ItemStack target,
            ItemEnchantments existing,
            boolean stored
    ) {
        if (!stored && !candidate.value().canEnchant(target)) {
            return false;
        }
        return EnchantmentHelper.isEnchantmentCompatible(existing.keySet(), candidate);
    }

    private static void applyEnchantmentToStack(
            ItemStack target,
            Holder<Enchantment> enchantment,
            int level,
            boolean stored
    ) {
        if (stored) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                    target.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
            );
            mutable.set(enchantment, level);
            target.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        } else {
            target.enchant(enchantment, level);
        }
    }

    private boolean hasBothInputs() {
        return !this.book.getBarterFuel().isEmpty() && !this.book.getBarterCatalyst().isEmpty();
    }

    private boolean hasPartialInput() {
        return !this.book.getBarterFuel().isEmpty() || !this.book.getBarterCatalyst().isEmpty();
    }

    private List<ItemEntity> findNearbyBarterItemEntities() {
        if (this.book.getBarterCooldown() > 0) {
            return List.of();
        }
        boolean needsFuel = this.book.getBarterFuel().isEmpty();
        boolean needsCatalyst = this.book.getBarterCatalyst().isEmpty();
        AABB box = this.book.getBoundingBox().inflate(TELEKINESIS_RANGE);
        return this.book.level().getEntitiesOfClass(ItemEntity.class, box, item -> {
            if (!item.isAlive() || item.getItem().isEmpty()) {
                return false;
            }
            ItemStack stack = item.getItem();
            return (needsFuel && isValidBarterFuel(stack))
                    || (needsCatalyst && isValidBarterCatalyst(this.book.level().registryAccess(), stack));
        });
    }

    public static boolean isValidBarterFuel(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Tags.ENCHANTING_FUEL);
    }

    public static boolean isValidBarterCatalyst(RegistryAccess registryAccess, ItemStack stack) {
        if (stack.isEmpty() || isValidBarterFuel(stack)) {
            return false;
        }
        if (EnchantRecipe.get(registryAccess, stack).isPresent()) {
            return true;
        }
        return stack.isEnchantable() && !stack.isEnchanted();
    }
}