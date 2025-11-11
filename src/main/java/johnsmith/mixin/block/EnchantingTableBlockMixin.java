package johnsmith.mixin.block;

import johnsmith.accessor.TomeStorageAccessor;
import johnsmith.api.enchantment.theme.EnchantmentTheme;
import johnsmith.block.BlockRegistry;
import johnsmith.api.enchantment.theme.effect.EffectData;
import johnsmith.api.enchantment.theme.effect.ParticleEffectData;
import johnsmith.api.enchantment.theme.effect.SoundEffectData;
import johnsmith.api.enchantment.theme.power.PowerProvider;
import johnsmith.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.item.ItemRegistry;
import johnsmith.lib.EnchantmentLib;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockSettingsAccessor;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.block.EnchantingTableBlock.POWER_PROVIDER_OFFSETS;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends BlockWithEntity {

    public EnchantingTableBlockMixin(Settings settings) {
        super(settings);
    }

    @ModifyArg(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V",
                   at = @At(value = "INVOKE",
                           target = "Lnet/minecraft/block/BlockWithEntity;<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V"),
                index = 0)
    private static AbstractBlock.Settings modifySettings(AbstractBlock.Settings settings) {
        // 1. Modify the *existing* settings object
        settings.strength(0.0F, 1.0F);

        // 2. Use the Accessor to set the package-private field
        ((AbstractBlockSettingsAccessor) settings).setToolRequired(false);

        return settings;
    }

    /**
     * Overrides the onBreak method to change the sound and prevent
     * break particles from spawning.
     */
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // ... existing code ...
        // Play the item frame "pop" sound
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 1.0F, 1.0F);

        // Emit a generic "block change" event instead of "block destroy"
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));

        // We DO NOT call super.onBreak(), which is what spawns particles
        // and plays the default break sound.
        return state;
    }

    /**
     * Overrides afterBreak to change the block and drop the tome
     * with a custom velocity.
     * If the dropped tome is empty, it will be enchanted.
     */
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        // 1. Revert the block to the deactivated state
        world.setBlockState(pos, BlockRegistry.DISTURBED_ENCHANTING_TABLE.getDefaultState(), 3);

        // 2. Drop the Enchanted Tome
        if (!world.isClient) {
            ItemStack stackToDrop = ItemStack.EMPTY;

            // Retrieve Stored Tome
            if (blockEntity instanceof TomeStorageAccessor accessor) {
                stackToDrop = accessor.enchanting_overhauled$getTomeStack();
            }

            // If the stack is still empty, drop a new one
            if (stackToDrop.isEmpty()) {
                stackToDrop = new ItemStack(ItemRegistry.ENCHANTED_TOME);
            }

            // 3. Check if the tome is empty and enchant it if needed
            ItemEnchantmentsComponent enchants = stackToDrop.get(DataComponentTypes.STORED_ENCHANTMENTS);
            if (enchants == null || enchants.isEmpty()) {
                // Use the new Lib method
                stackToDrop = EnchantmentLib.enchantTomeRandomly(stackToDrop, world, world.getRandom());
            }

            // 4. Spawn item gently instead of scattering
            double x = (double)pos.getX() + 0.5;
            double y = (double)pos.getY() + 1.0;
            double z = (double)pos.getZ() + 0.5;

            ItemEntity itemEntity = new ItemEntity(world, x, y, z, stackToDrop);

            // Set velocity to a gentle upward pop
            itemEntity.setVelocity(0.0, 0.1, 0.0);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
        }

        // We do not call super.afterBreak(...)
    }

    /**
     * Overrides the vanilla particle-spawning logic to account
     * for custom power providers and scale particles with power.
     */
    @Inject(method = "randomDisplayTick", at = @At("HEAD"), cancellable = true)
    private void onRandomDisplayTick(BlockState state, World world, BlockPos pos, Random random, CallbackInfo ci) {
        // Cancel the original vanilla particle logic
        ci.cancel();

        // Check if a player is close enough to see effects
        PlayerEntity playerEntity = world.getClosestPlayer(
                (double)pos.getX() + 0.5D,
                (double)pos.getY() + 0.5D,
                (double)pos.getZ() + 0.5D,
                3.0D, // Vanilla book-opening distance
                false
        );
        if (playerEntity == null) {
            return;
        }

        // --- Data-Driven Logic ---
        // 1. Find the dominant theme based on nearby power
        Optional<EnchantmentTheme> dominantThemeOpt = this.getDominantTheme(world, pos);

        // 2. If no theme is dominant, play vanilla particles and return
        if (dominantThemeOpt.isEmpty()) {
            this.spawnVanillaParticles(world, pos, random);
            return;
        }

        // 3. A theme is dominant, get its effects
        EnchantmentTheme theme = dominantThemeOpt.get();
        if (theme.effects().isEmpty()) {
            // This theme is dominant but has no effects, so do nothing.
            // We don't spawn vanilla particles either, to show the theme is active.
            return;
        }
        EffectData effects = theme.effects().get();

        // 4. Spawn particles/sound at the enchanting table itself
        this.spawnThemedEffects(world, pos, random, effects, false);

        // 5. Spawn particles/sound at the power providers
        for(BlockPos providerOffset : POWER_PROVIDER_OFFSETS) {
            BlockPos providerPos = pos.add(providerOffset);

            // Check if this provider block matches the dominant theme
            if (EnchantmentLib.getEnchantingPower(world, providerPos, theme) > 0) {
                // Check if the transmitter block is clear
                BlockPos transmitterPos = pos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
                if (world.getBlockState(transmitterPos).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    this.spawnThemedEffects(world, providerPos, random, effects, true);
                }
            }
        }
    }

    /**
     * Finds the non-DEFAULT theme that is providing the most power to the table.
     *
     * @param world The world.
     * @param pos The BlockPos of the enchanting table.
     * @return An Optional containing the dominant EnchantmentTheme, or empty if no
     * themed providers are found.
     */
    @Unique
    private Optional<EnchantmentTheme> getDominantTheme(World world, BlockPos pos) {
        Map<EnchantmentTheme, Integer> themePower = new HashMap<>();

        // Get the dynamic registry from the world
        DynamicRegistryManager registryAccess = world.getRegistryManager();
        Registry<EnchantmentTheme> themeRegistry = registryAccess.get(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);

        for (BlockPos providerOffset : POWER_PROVIDER_OFFSETS) {
            // Check if the "transmitter" block (air) is clear
            BlockPos transmitterPos = pos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
            if (!world.getBlockState(transmitterPos).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                continue;
            }

            BlockPos providerPos = pos.add(providerOffset);
            BlockState providerState = world.getBlockState(providerPos);
            // Get the RegistryEntry for the block
            RegistryEntry<Block> blockEntry = providerState.getRegistryEntry();

            // Check this block against all non-DEFAULT themes
            // Iterate the dynamic registry instance
            for (EnchantmentTheme theme : themeRegistry) {
                // Skip the default theme, we're only looking for special themes
                // (those with effects)
                if (theme.effects().isEmpty()) {
                    continue;
                }

                // Check this block against the theme's providers
                for (PowerProvider provider : theme.powerProviders()) {
                    // Use .contains() to check if the block entry is in the list
                    if (provider.blocks().contains(blockEntry)) {
                        themePower.merge(theme, provider.power(), Integer::sum);
                        break; // A block can only belong to one theme's provider list
                    }
                }
            }
        }

        // Find and return the theme with the highest total power
        return themePower.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Spawns the vanilla 'enchant' particles from power providers to the table.
     * This is the default logic used when no special theme is dominant.
     */
    @Unique
    private void spawnVanillaParticles(World world, BlockPos pos, Random random) {
        // Get the dynamic registry from the world
        DynamicRegistryManager registryAccess = world.getRegistryManager();
        Registry<EnchantmentTheme> themeRegistry = registryAccess.get(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);

        // Get the DEFAULT theme from the dynamic registry
        EnchantmentTheme defaultTheme = themeRegistry.get(EnchantmentThemeRegistry.DEFAULT);
        if (defaultTheme == null) {
            return; // Should not happen if data is loaded correctly
        }

        for(BlockPos providerOffset : POWER_PROVIDER_OFFSETS) {
            // Check if this block is a default provider
            if (EnchantmentLib.getEnchantingPower(world, pos.add(providerOffset), defaultTheme) > 0) {
                // Check if the transmitter block is clear
                BlockPos transmitterPos = pos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2);
                if (world.getBlockState(transmitterPos).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
                    // Copied from vanilla EnchantingTableBlock.randomDisplayTick
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
     * Spawns particles and plays sounds based on the data-driven EffectData.
     * The sound will play at the exact location the particle spawns.
     *
     * @param world The world.
     * @param pos The BlockPos to spawn effects at (either table or provider).
     * @param random The random source.
     * @param effects The data-driven effect rules.
     * @param atProvider True if spawning at a provider block, false if at the table.
     */
    @Unique
    private void spawnThemedEffects(World world, BlockPos pos, Random random, EffectData effects, boolean atProvider) {
        if (effects.particle().isEmpty() && effects.sound().isEmpty()) {
            return;
        }

        // Use 'chance' as "1 in X"
        int chance = Math.max(1, effects.chance());
        Optional<ParticleEffectData> particleOpt = effects.particle();
        Optional<SoundEffectData> soundOpt = effects.sound();

        for (int i = 0; i < effects.iterations(); i++) {
            if (random.nextInt(chance) != 0) {
                continue;
            }

            // --- 1. Calculate Base Position ---
            // This is the starting point, using the particle's height logic for consistency
            double baseX = (double)pos.getX() + 0.5D;
            double baseY = (double)pos.getY() + (atProvider ? 0.5D : 0.75D); // Table particles spawn higher
            double baseZ = (double)pos.getZ() + 0.5D;

            // These will be the final coordinates, modified by particle offsets if present
            double spawnX = baseX;
            double spawnY = baseY;
            double spawnZ = baseZ;

            // --- 2. Particle Logic (if present) ---
            if (particleOpt.isPresent()) {
                ParticleEffectData p = particleOpt.get();

                // Calculate random offsets and add them to the base position
                double offsetX = p.offset().x + (random.nextDouble() * 2.0 - 1.0) * p.offsetVariance().x;
                double offsetY = p.offset().y + (random.nextDouble() * 2.0 - 1.0) * p.offsetVariance().y;
                double offsetZ = p.offset().z + (random.nextDouble() * 2.0 - 1.0) * p.offsetVariance().z;

                // Update the final spawn coordinates
                spawnX += offsetX;
                spawnY += offsetY;
                spawnZ += offsetZ;

                // Calculate random velocity
                double velX = p.velocity().x + (random.nextDouble() * 2.0 - 1.0) * p.velocityVariance().x;
                double velY = p.velocity().y + (random.nextDouble() * 2.0 - 1.0) * p.velocityVariance().y;
                double velZ = p.velocity().z + (random.nextDouble() * 2.0 - 1.0) * p.velocityVariance().z;

                // Spawn the particle at the final, offset position
                world.addParticle((ParticleEffect)p.effect(), spawnX, spawnY, spawnZ, velX, velY, velZ);
            }

            // --- 3. Sound Logic (if present) ---
            if (soundOpt.isPresent()) {
                SoundEffectData s = soundOpt.get();

                // Calculate random pitch and volume
                float pitch = s.pitch() + (random.nextFloat() * 2.0f - 1.0f) * s.pitchVariance();
                float volume = s.volume() + (random.nextFloat() * 2.0f - 1.0f) * s.volumeVariance();

                // Play the sound at the *exact same* final coordinates
                // (which include the particle's offset, if it existed)
                world.playSound(spawnX, spawnY, spawnZ, s.effect().value(), SoundCategory.BLOCKS, volume, pitch, false);
            }
        }
    }

    /**
     * Overrides the "Pick Block" (middle-click) behavior.
     * This implementation satisfies the creative-mode requirement to pick the stored tome.
     *
     * The requested "enchanting" and "cycling" logic CANNOT be implemented here.
     * - Enchanting requires `World` and `Random` access, but this method only provides `WorldView`.
     * - Cycling logic requires `PlayerEntity`, which this method does not provide.
     */
    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        // 1. Return the stored tome if it exists
        if (blockEntity instanceof TomeStorageAccessor accessor) {
            ItemStack tome = accessor.enchanting_overhauled$getTomeStack();
            if (!tome.isEmpty()) {
                // Creative players will get a copy of this.
                // Survival players will search for this item in their inventory.
                return tome.copy();
            }
        }

        // 2. Fallback: Return a base tome.
        // This handles creative-pick on an empty table and survival-pick.
        // This also prevents the vanilla ENCHANTING_TABLE from ever being picked.
        return EnchantmentLib.getTomeWithDefaultEnchantment();
    }
}