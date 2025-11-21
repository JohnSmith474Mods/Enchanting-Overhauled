package johnsmith.enchantingoverhauled.mixin.block;

import johnsmith.enchantingoverhauled.accessor.TomeStorageAccessor;
import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.EffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.ParticleEffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.effect.SoundEffectData;
import johnsmith.enchantingoverhauled.api.enchantment.theme.power.PowerProvider;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
        properties.strength(0.0F, 1.0F);
        ((AbstractBlockSettingsAccessor) properties).setToolRequired(false);
        return properties;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        world.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
        return state;
    }

    @Override
    public void playerDestroy(
            Level world,
            @NotNull Player player,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            BlockEntity blockEntity,
            @NotNull ItemStack tool
    ) {
        world.setBlock(pos, Services.PLATFORM.getDisturbedEnchantingTable().defaultBlockState(), 3);

        if (!world.isClientSide) {
            ItemStack stackToDrop = ItemStack.EMPTY;

            if (blockEntity instanceof TomeStorageAccessor accessor) {
                stackToDrop = accessor.enchanting_overhauled$getTomeStack();
            }

            if (stackToDrop.isEmpty()) {
                stackToDrop = new ItemStack(Services.PLATFORM.getEnchantedTome());
            }

            ItemEnchantments enchants = stackToDrop.get(DataComponents.STORED_ENCHANTMENTS);
            if (enchants == null || enchants.isEmpty()) {
                stackToDrop = EnchantmentLib.enchantTomeRandomly(stackToDrop, world, world.getRandom());
            }

            double x = (double)pos.getX() + 0.5;
            double y = (double)pos.getY() + 1.0;
            double z = (double)pos.getZ() + 0.5;

            ItemEntity itemEntity = new ItemEntity(world, x, y, z, stackToDrop);
            itemEntity.setDeltaMovement(0.0, 0.1, 0.0);
            itemEntity.setDefaultPickUpDelay();
            world.addFreshEntity(itemEntity);
        }
    }

    /**
     * Overrides the vanilla particle-spawning logic.
     * Now ALWAYS spawns vanilla glyphs, then optionally overlays theme effects.
     */
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void onRandomDisplayTick(BlockState state, Level world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // Cancel the original vanilla particle logic
        ci.cancel();

        // Check if a player is close enough to see effects
        Player playerEntity = world.getNearestPlayer(
                (double)pos.getX() + 0.5D,
                (double)pos.getY() + 0.5D,
                (double)pos.getZ() + 0.5D,
                3.0D, // Vanilla book-opening distance
                false
        );
        if (playerEntity == null) {
            return;
        }

        // 1. Always spawn vanilla particles (Glyphs) for any valid provider
        this.enchantingOverhauled$spawnAlwaysVanillaParticles(world, pos, random);

        // 2. Find the dominant theme based on nearby power
        Optional<EnchantmentTheme> dominantThemeOpt = this.enchantingOverhauled$getDominantTheme(world, pos);

        // 3. If a dominant theme is found (and it has effects), spawn them
        if (dominantThemeOpt.isPresent()) {
            EnchantmentTheme theme = dominantThemeOpt.get();
            // getDominantTheme only returns themes with !effects.isEmpty(), so this is safe
            EffectData effects = theme.effects().get();

            // Spawn particles/sound at the enchanting table itself
            this.enchantingOverhauled$spawnThemedEffects(world, pos, random, effects, false);

            // Spawn particles/sound at the power providers
            for(BlockPos providerOffset : BOOKSHELF_OFFSETS) {
                BlockPos providerPos = pos.offset(providerOffset);

                // Check if this provider block matches the dominant theme
                if (EnchantmentLib.getEnchantingPower(world, providerPos, theme) > 0) {
                    // Check if the transmitter block is clear
                    BlockPos transmitterPos = pos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
                    if (world.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                        this.enchantingOverhauled$spawnThemedEffects(world, providerPos, random, effects, true);
                    }
                }
            }
        }
    }

    /**
     * Spawns the vanilla 'enchant' particles for ANY block that provides power of ANY theme.
     */
    @Unique
    private void enchantingOverhauled$spawnAlwaysVanillaParticles(Level world, BlockPos pos, RandomSource random) {
        RegistryAccess registryAccess = world.registryAccess();
        Optional<Registry<EnchantmentTheme>> themeRegistryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (themeRegistryOpt.isEmpty()) return;
        Registry<EnchantmentTheme> themeRegistry = themeRegistryOpt.get();

        for(BlockPos providerOffset : BOOKSHELF_OFFSETS) {
            BlockPos providerPos = pos.offset(providerOffset);
            boolean providesPower = false;

            // Inefficient but reliable: Check if this block provides power
            for (EnchantmentTheme theme : themeRegistry) {
                if (EnchantmentLib.getEnchantingPower(world, providerPos, theme) > 0) {
                    providesPower = true;
                    break;
                }
            }

            if (providesPower) {
                BlockPos transmitterPos = pos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
                if (world.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    world.addParticle(ParticleTypes.ENCHANT,
                            (double)pos.getX() + 0.5D,
                            (double)pos.getY() + 2.0D,
                            (double)pos.getZ() + 0.5D,
                            (double)((float)providerOffset.getX() + random.nextFloat()) - 0.5D,
                            (double)((float)providerOffset.getY() - random.nextFloat() - 1.0F),
                            (double)((float)providerOffset.getZ() + random.nextFloat()) - 0.5D
                    );
                }
            }
        }
    }

    /**
     * Finds the theme that is providing the most power to the table.
     * Skips themes that do not have any effects defined.
     *
     * @param world The world.
     * @param pos The BlockPos of the enchanting table.
     * @return An Optional containing the dominant EnchantmentTheme.
     */
    @Unique
    private Optional<EnchantmentTheme> enchantingOverhauled$getDominantTheme(Level world, BlockPos pos) {
        Map<EnchantmentTheme, Integer> themePower = new HashMap<>();

        RegistryAccess registryAccess = world.registryAccess();
        Optional<Registry<EnchantmentTheme>> themeRegistryOpt = Services.PLATFORM.getThemeRegistry(registryAccess);

        if (themeRegistryOpt.isEmpty()) return Optional.empty();

        Registry<EnchantmentTheme> themeRegistry = themeRegistryOpt.get();

        for (BlockPos providerOffset : BOOKSHELF_OFFSETS) {
            BlockPos transmitterPos = pos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
            if (!world.getBlockState(transmitterPos).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                continue;
            }

            BlockPos providerPos = pos.offset(providerOffset);
            BlockState providerState = world.getBlockState(providerPos);
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
    private void enchantingOverhauled$spawnThemedEffects(Level world, BlockPos pos, RandomSource random, EffectData effects, boolean atProvider) {
        if (effects.particle().isEmpty() && effects.sound().isEmpty()) {
            return;
        }

        int chance = Math.max(1, effects.chance());
        Optional<ParticleEffectData> particleOpt = effects.particle();
        Optional<SoundEffectData> soundOpt = effects.sound();

        for (int i = 0; i < effects.iterations(); i++) {
            if (random.nextInt(chance) != 0) {
                continue;
            }

            double baseX = (double)pos.getX() + 0.5D;
            double baseY = (double)pos.getY() + (atProvider ? 0.5D : 0.75D);
            double baseZ = (double)pos.getZ() + 0.5D;

            double spawnX = baseX;
            double spawnY = baseY;
            double spawnZ = baseZ;

            if (particleOpt.isPresent()) {
                ParticleEffectData p = particleOpt.get();

                double offsetX = p.offset().x + (random.nextDouble() * 2.0 - 1.0) * p.offsetVariance().x;
                double offsetY = p.offset().y + (random.nextDouble() * 2.0 - 1.0) * p.offsetVariance().y;
                double offsetZ = p.offset().z + (random.nextDouble() * 2.0 - 1.0) * p.offsetVariance().z;

                spawnX += offsetX;
                spawnY += offsetY;
                spawnZ += offsetZ;

                double velX = p.velocity().x + (random.nextDouble() * 2.0 - 1.0) * p.velocityVariance().x;
                double velY = p.velocity().y + (random.nextDouble() * 2.0 - 1.0) * p.velocityVariance().y;
                double velZ = p.velocity().z + (random.nextDouble() * 2.0 - 1.0) * p.velocityVariance().z;

                world.addParticle((ParticleOptions) p.effect(), spawnX, spawnY, spawnZ, velX, velY, velZ);
            }

            if (soundOpt.isPresent()) {
                SoundEffectData s = soundOpt.get();

                float pitch = s.pitch() + (random.nextFloat() * 2.0f - 1.0f) * s.pitchVariance();
                float volume = s.volume() + (random.nextFloat() * 2.0f - 1.0f) * s.volumeVariance();

                world.playLocalSound(spawnX, spawnY, spawnZ, s.effect().value(), SoundSource.BLOCKS, volume, pitch, false);
            }
        }
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader world, @NotNull BlockPos pos, @NotNull BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof TomeStorageAccessor accessor) {
            ItemStack tome = accessor.enchanting_overhauled$getTomeStack();
            if (!tome.isEmpty()) {
                return tome.copy();
            }
        }
        return EnchantmentLib.getTomeWithDefaultEnchantment();
    }
}