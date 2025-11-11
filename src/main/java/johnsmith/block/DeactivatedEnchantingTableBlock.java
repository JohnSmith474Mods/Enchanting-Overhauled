package johnsmith.block;

import johnsmith.EnchantingOverhauled;
import johnsmith.accessor.TomeStorageAccessor;
import johnsmith.advancement.CriteriaRegistry;
import johnsmith.entity.damage.DamageTypeRegistry;
import johnsmith.item.ItemRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items; // <-- Import
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.List;
import java.util.Optional;

public class DeactivatedEnchantingTableBlock extends Block {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    public DeactivatedEnchantingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack itemStack = player.getMainHandStack();

        // Check if the player is right-clicking with the Enchanted Tome
        if (itemStack.isOf(ItemRegistry.ENCHANTED_TOME)) {
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                serverWorld.setBlockState(pos, Blocks.ENCHANTING_TABLE.getDefaultState(), 3);

                BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
                if (blockEntity instanceof TomeStorageAccessor accessor) {
                    ItemStack tomeCopy = itemStack.copy();
                    tomeCopy.setCount(1);
                    accessor.enchanting_overhauled$setTomeStack(tomeCopy);
                }

                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                serverWorld.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                LightningEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (lightningBolt != null) {
                    lightningBolt.setPosition(Vec3d.ofBottomCenter(pos));
                    serverWorld.spawnEntity(lightningBolt);
                }

                Box aoe = new Box(pos).expand(3.0);
                List<LivingEntity> nearbyEntities = serverWorld.getNonSpectatingEntities(LivingEntity.class, aoe);
                for (LivingEntity entity : nearbyEntities) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 30 * 20)); // 30 seconds
                }

                serverWorld.spawnParticles(
                        ParticleTypes.SOUL,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        30, 0.5, 0.3, 0.5, 0.0
                );
            }

            if (!world.isClient && player instanceof ServerPlayerEntity) {
                EnchantingOverhauled.LOGGER.info("Attempting to trigger advancement for " + player.getName().getString());
                CriteriaRegistry.ACTIVATE_ALTAR.trigger((ServerPlayerEntity) player);
            }

            return ActionResult.SUCCESS;
        }

        // --- 2. Check for the WRONG books ---
        boolean isWrongBook = itemStack.isOf(Items.BOOK) ||
                itemStack.isOf(Items.WRITABLE_BOOK) || // Book and Quill
                itemStack.isOf(Items.WRITTEN_BOOK) ||
                itemStack.isOf(Items.ENCHANTED_BOOK);

        if (isWrongBook) {
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;

                // Consume the item
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                if (itemStack.isOf(Items.ENCHANTED_BOOK)) {
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_VEX_DEATH, SoundCategory.BLOCKS, 2.0F, 0.0F);
                    serverWorld.spawnParticles(
                            ParticleTypes.SOUL,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            1500, 4.0, 4.0, 4.0, 0.0
                    );
                }

                serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState());

                // 1. Create a new custom ExplosionBehavior
                ExplosionBehavior customBehavior = new ExplosionBehavior() {
                    /**
                     * This prevents the explosion from breaking *any* blocks.
                     */
                    @Override
                    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
                        return true;
                    }

                    /**
                     * This multiplies the knockback force.
                     * 1.0f is default. 2.0f is double knockback.
                     */
                    @Override
                    public float getKnockbackModifier(Entity entity) {
                        return 2.5F;
                    }

                    // This is also needed to ensure non-block entities (like air) don't stop the explosion
                    @Override
                    public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                        if (blockState.isAir() && fluidState.isEmpty()) {
                            return Optional.empty();
                        }
                        return Optional.of(0.0f); // Treat all blocks as having 0 resistance (but canDestroyBlock stops them)
                    }
                };

                // Get the DamageSource from our custom DamageType
                DamageSource customDamageSource = new DamageSource(
                        world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypeRegistry.ARCANE_RETRIBUTION)
                );

                // Use Explosion.DestructionType.KEEP to prevent block damage
                serverWorld.createExplosion(
                        (Entity) null, // No specific entity source
                        customDamageSource,
                        customBehavior, // No special behavior
                        pos.toCenterPos(), // Explode above the center of the block
                        5.0f,
                        true, // Create fire
                        World.ExplosionSourceType.BLOCK // This translates to DestructionType.KEEP
                );
            }
            return ActionResult.CONSUME; // Consume the action so the book isn't placed
        }

        // --- 3. If it's not a book, do nothing ---
        return ActionResult.PASS;
    }
}