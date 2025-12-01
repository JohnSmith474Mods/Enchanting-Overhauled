package johnsmith.enchantingoverhauled.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import johnsmith.enchantingoverhauled.accessor.TomeStorageAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.EffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.ParticleEffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.SoundEffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.PowerProvider;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentMenu;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import johnsmith.enchantingoverhauled.mixin.accessor.AbstractBlockSettingsAccessor;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends BaseEntityBlock {

    @Shadow
    @Final
    public static List<BlockPos> BOOKSHELF_OFFSETS;

    public EnchantingTableBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @ModifyArg(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/BaseEntityBlock;<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"),
            index = 0)
    private static BlockBehaviour.Properties modifySettings(BlockBehaviour.Properties properties) {
        ((AbstractBlockSettingsAccessor) properties).setToolRequired(false);
        return properties;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(
            @NotNull Level level,
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState,
            @NotNull Player player
    ) {
        if (Config.BINARY_MINEABLE_ENCHANTING_TABLE.get()) {
            this.spawnDestroyParticles(level, player, blockPos, blockState);
            if (blockState.is(BlockTags.GUARDED_BY_PIGLINS)) {
                PiglinAi.angerNearbyPiglins(player, false);
            }

            level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(player, blockState));
            return blockState;
        }
        level.playSound(null, blockPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState));
        return blockState;
    }

    @Override
    public void playerDestroy(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull BlockPos blockPos,
            @NotNull BlockState blockState,
            BlockEntity blockEntity,
            @NotNull ItemStack tool
    ) {
        if (Config.BINARY_MINEABLE_ENCHANTING_TABLE.get()) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            dropResources(blockState, level, blockPos, blockEntity, player, tool);
            return;
        }

        level.setBlock(blockPos, Services.PLATFORM.getDisturbedEnchantingTable().defaultBlockState(), 3);

        if (!level.isClientSide()) {
            ItemStack stackToDrop = ItemStack.EMPTY;

            if (blockEntity instanceof TomeStorageAccessor accessor) {
                stackToDrop = accessor.enchanting_overhauled$getTomeStack();
            }

            if (stackToDrop.isEmpty()) {
                stackToDrop = new ItemStack(Services.PLATFORM.getEnchantedTome());
            }

            ItemEnchantments enchants = stackToDrop.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchants == null || enchants.isEmpty()) {
                stackToDrop = EnchantmentLib.enchantTomeRandomly(stackToDrop, level, level.getRandom());
            }

            double x = (double)blockPos.getX() + 0.5;
            double y = (double)blockPos.getY() + 1.0;
            double z = (double)blockPos.getZ() + 0.5;

            ItemEntity itemEntity = new ItemEntity(level, x, y, z, stackToDrop);
            itemEntity.setDeltaMovement(0.0, 0.1, 0.0);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    /**
     * Overrides the vanilla particle-spawning logic.
     * Now ALWAYS spawns vanilla glyphs, then optionally overlays theme effects.
     */
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void onRandomDisplayTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
        // Cancel the original vanilla particle logic
        ci.cancel();

        // Check if a player is close enough to see effects
        Player playerEntity = level.getNearestPlayer(
                (double)blockPos.getX() + 0.5D,
                (double)blockPos.getY() + 0.5D,
                (double)blockPos.getZ() + 0.5D,
                3.0D, // Vanilla book-opening distance
                false
        );
        if (playerEntity == null) {
            return;
        }

        // 1. Always spawn vanilla particles (Glyphs) for any valid provider
        this.enchantingOverhauled$spawnAlwaysVanillaParticles(level, blockPos, randomSource);

        // 2. Find the dominant theme based on nearby power
        Optional<EnchantmentTheme> dominantThemeOpt = this.enchantingOverhauled$getDominantTheme(level, blockPos);

        // 3. If a dominant theme is found (and it has effects), spawn them
        if (dominantThemeOpt.isPresent()) {
            EnchantmentTheme theme = dominantThemeOpt.get();
            // getDominantTheme only returns themes with !effects.isEmpty(), so this is safe
            EffectData effects = theme.effects().get();

            // Spawn particles/sound at the enchanting table itself
            this.enchantingOverhauled$spawnThemedEffects(level, blockPos, randomSource, effects, false);

            // Spawn particles/sound at the power providers
            for(BlockPos providerOffset : BOOKSHELF_OFFSETS) {
                BlockPos providerPos = blockPos.offset(providerOffset);

                // Check if this provider block matches the dominant theme
                if (EnchantmentLib.getEnchantingPower(level, providerPos, theme) > 0) {
                    // Check if the transmitter block is clear
                    BlockPos transmitterPos = blockPos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
                    if (level.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                        this.enchantingOverhauled$spawnThemedEffects(level, providerPos, randomSource, effects, true);
                    }
                }
            }
        }
    }

    /**
     * Spawns the vanilla 'enchant' particles for ANY block that provides power of ANY theme.
     */
    @Unique
    private void enchantingOverhauled$spawnAlwaysVanillaParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        RegistryAccess registryAccess = level.registryAccess();
        Optional<Registry<EnchantmentTheme>> themeRegistryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (themeRegistryOpt.isEmpty()) return;
        Registry<EnchantmentTheme> themeRegistry = themeRegistryOpt.get();

        for(BlockPos providerOffset : BOOKSHELF_OFFSETS) {
            BlockPos providerPos = blockPos.offset(providerOffset);
            boolean providesPower = false;

            // Inefficient but reliable: Check if this block provides power
            for (EnchantmentTheme theme : themeRegistry) {
                if (EnchantmentLib.getEnchantingPower(level, providerPos, theme) > 0) {
                    providesPower = true;
                    break;
                }
            }

            if (providesPower) {
                BlockPos transmitterPos = blockPos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
                if (level.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    level.addParticle(ParticleTypes.ENCHANT,
                            (double)blockPos.getX() + 0.5D,
                            (double)blockPos.getY() + 2.0D,
                            (double)blockPos.getZ() + 0.5D,
                            (double)((float)providerOffset.getX() + randomSource.nextFloat()) - 0.5D,
                            (double)((float)providerOffset.getY() - randomSource.nextFloat() - 1.0F),
                            (double)((float)providerOffset.getZ() + randomSource.nextFloat()) - 0.5D
                    );
                }
            }
        }
    }

    /**
     * Finds the theme that is providing the most power to the table.
     * Skips themes that do not have any effects defined.
     *
     * @param level The world.
     * @param blockPos The BlockPos of the enchanting table.
     * @return An Optional containing the dominant EnchantmentTheme.
     */
    @Unique
    private Optional<EnchantmentTheme> enchantingOverhauled$getDominantTheme(Level level, BlockPos blockPos) {
        Map<EnchantmentTheme, Integer> themePower = new HashMap<>();

        RegistryAccess registryAccess = level.registryAccess();
        Optional<Registry<EnchantmentTheme>> themeRegistryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (themeRegistryOpt.isEmpty()) return Optional.empty();

        Registry<EnchantmentTheme> themeRegistry = themeRegistryOpt.get();

        for (BlockPos providerOffset : BOOKSHELF_OFFSETS) {
            BlockPos transmitterPos = blockPos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
            if (!level.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                continue;
            }

            BlockPos providerPos = blockPos.offset(providerOffset);
            BlockState providerState = level.getBlockState(providerPos);
            Holder<Block> blockEntry = providerState.getBlockHolder();

            for (EnchantmentTheme theme : themeRegistry) {
                // Only consider themes that actually have effects to play
                if (theme.effects().isEmpty()) {
                    continue;
                }

                for (PowerProvider provider : theme.powerProviders()) {
                    if (provider.blocks().contains(blockEntry)) {
                        themePower.merge(theme, provider.power(), Integer::sum);
                        break;
                    }
                }
            }
        }

        return themePower.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    @Unique
    private void enchantingOverhauled$spawnThemedEffects(Level level, BlockPos blockPos, RandomSource randomSource, EffectData effectData, boolean atProvider) {
        if (effectData.particle().isEmpty() && effectData.sound().isEmpty()) {
            return;
        }

        int chance = Math.max(1, effectData.chance());
        Optional<ParticleEffectData> particleOpt = effectData.particle();
        Optional<SoundEffectData> soundOpt = effectData.sound();

        for (int i = 0; i < effectData.iterations(); i++) {
            if (randomSource.nextInt(chance) != 0) {
                continue;
            }

            double baseX = (double)blockPos.getX() + 0.5D;
            double baseY = (double)blockPos.getY() + (atProvider ? 0.5D : 0.75D);
            double baseZ = (double)blockPos.getZ() + 0.5D;

            double spawnX = baseX;
            double spawnY = baseY;
            double spawnZ = baseZ;

            if (particleOpt.isPresent()) {
                ParticleEffectData p = particleOpt.get();

                double offsetX = p.offset().x + (randomSource.nextDouble() * 2.0 - 1.0) * p.offsetVariance().x;
                double offsetY = p.offset().y + (randomSource.nextDouble() * 2.0 - 1.0) * p.offsetVariance().y;
                double offsetZ = p.offset().z + (randomSource.nextDouble() * 2.0 - 1.0) * p.offsetVariance().z;

                spawnX += offsetX;
                spawnY += offsetY;
                spawnZ += offsetZ;

                double velX = p.velocity().x + (randomSource.nextDouble() * 2.0 - 1.0) * p.velocityVariance().x;
                double velY = p.velocity().y + (randomSource.nextDouble() * 2.0 - 1.0) * p.velocityVariance().y;
                double velZ = p.velocity().z + (randomSource.nextDouble() * 2.0 - 1.0) * p.velocityVariance().z;

                level.addParticle((ParticleOptions) p.effect(), spawnX, spawnY, spawnZ, velX, velY, velZ);
            }

            if (soundOpt.isPresent()) {
                SoundEffectData s = soundOpt.get();

                float pitch = s.pitch() + (randomSource.nextFloat() * 2.0f - 1.0f) * s.pitchVariance();
                float volume = s.volume() + (randomSource.nextFloat() * 2.0f - 1.0f) * s.volumeVariance();

                level.playLocalSound(spawnX, spawnY, spawnZ, s.effect().value(), SoundSource.BLOCKS, volume, pitch, false);
            }
        }
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader levelReader, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        BlockEntity blockEntity = levelReader.getBlockEntity(blockPos);

        if (blockEntity instanceof TomeStorageAccessor accessor) {
            ItemStack tome = accessor.enchanting_overhauled$getTomeStack();
            if (!tome.isEmpty()) {
                return tome.copy();
            }
        }
        RegistryAccess registryAccess = levelReader.registryAccess();
        return EnchantmentLib.getTomeWithDefaultEnchantment(registryAccess);
    }

    /**
     * Dynamic override for Block Strength (Hardness).
     * Replaces properties.strength(0.0F, ...)
     */
    @Override
    public float getDestroyProgress(
            @NotNull BlockState state,
            @NotNull Player player,
            @NotNull BlockGetter level,
            @NotNull BlockPos pos
    ) {
        // If Custom Mode is Active (Config is FALSE)
        if (!Config.BINARY_MINEABLE_ENCHANTING_TABLE.get()) {
            // Return 1.0F to indicate the block breaks instantly (Hardness 0 behavior)
            return 1.0F;
        }

        // If Vanilla Mode is Active (Config is TRUE)
        // Delegate to super, which calculates based on the cached 5.0F hardness
        return super.getDestroyProgress(state, player, level, pos);
    }

    @ModifyReturnValue(method = "getMenuProvider", at = @At("RETURN"))
    private MenuProvider overrideMenu(final MenuProvider original, @Local(argsOnly = true) final Level level, @Local(argsOnly = true) final BlockPos position) {
        return new SimpleMenuProvider((id, inventory, player) -> new OverhauledEnchantmentMenu(id, inventory, ContainerLevelAccess.create(level, position)), original.getDisplayName());
    }
}