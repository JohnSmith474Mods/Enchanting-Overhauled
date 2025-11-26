package johnsmith.enchantingoverhauled.block;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.accessor.TomeStorageAccessor;
import johnsmith.enchantingoverhauled.advancement.CriteriaRegistry;
import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.damagesource.DamageTypeRegistry;
import johnsmith.enchantingoverhauled.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A block representing an inactive or disturbed Enchanting Table.
 * <p>
 * This block serves two main purposes:
 * <ol>
 * <li><b>Activation Ritual:</b> Players must right-click this block with an {@code EnchantedTome}
 * to transform it into a functional vanilla {@link net.minecraft.world.level.block.EnchantingTableBlock}.
 * This process triggers visual effects (lightning, particles) and grants an advancement.</li>
 * <li><b>Punishment:</b> If a player attempts to activate the table with a standard book or
 * standard enchanted book, the table rejects it violently, causing an explosion and dealing
 * {@code ARCANE_RETRIBUTION} damage.</li>
 * </ol>
 */
public class DeactivatedEnchantingTableBlock extends Block {
    /**
     * The bounding box for the block, matching the vanilla Enchanting Table shape (12 pixels high).
     */
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    /**
     * Constructs a new DeactivatedEnchantingTableBlock.
     *
     * @param properties The block properties.
     */
    public DeactivatedEnchantingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
        return RenderShape.MODEL;
    }

    /**
     * Handles the activation logic when the player right-clicks the block.
     *
     * @param blockState The current state of the block.
     * @param level The level the block is in.
     * @param pos The position of the block.
     * @param player The player interacting with the block.
     * @param hit The hit result of the interaction.
     * @return {@link InteractionResult#SUCCESS} if activated, {@link InteractionResult#CONSUME} if punished, or {@link InteractionResult#PASS}.
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull BlockHitResult hit) {
        ItemStack itemStack = player.getMainHandItem();

        // 1. Check if the player is right-clicking with an Enchanted Tome
        if (itemStack.is(Services.PLATFORM.getEnchantedTome())) {
            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;
                // Replace this block with a vanilla Enchanting Table
                serverLevel.setBlock(pos, Blocks.ENCHANTING_TABLE.defaultBlockState(), 3);

                // Store the Tome inside the new Enchanting Table's BlockEntity
                BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
                if (blockEntity instanceof TomeStorageAccessor accessor) {
                    ItemStack tomeCopy = itemStack.copy();
                    tomeCopy.setCount(1);
                    accessor.enchanting_overhauled$setTomeStack(tomeCopy);
                }

                // Consume the Tome from the player's hand
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }

                boolean shouldCreateActivationEffects = Config.BINARY_ACTIVATION_EFFECTS.get();
                if (shouldCreateActivationEffects) {
                    // Spawn activation effects (Sound, Lightning, Particles, Glowing Effect)
                    serverLevel.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                    LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                    if (lightningBolt != null) {
                        lightningBolt.moveTo(Vec3.atBottomCenterOf(pos));
                        serverLevel.addFreshEntity(lightningBolt);
                    }

                    AABB areaOfEffect = new AABB(pos).inflate(3.0F);
                    List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, areaOfEffect);
                    for (LivingEntity entity : nearbyEntities) {
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30 * 20));
                    }

                    serverLevel.sendParticles(
                            ParticleTypes.SOUL,
                            pos.getX() + 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5,
                            30,
                            0.5,
                            0.3,
                            0.5,
                            0.0
                    );
                }
            }

            // Trigger the activation advancement
            if (!level.isClientSide && player instanceof ServerPlayer) {
                Constants.LOG.info("Attempting to trigger advancement for " + player.getName().getString());
                CriteriaRegistry.ACTIVATE_ALTAR.trigger((ServerPlayer) player);
            }

            return InteractionResult.SUCCESS;
        }


        // 2. Check for incorrect book types (Punishment Logic)
        boolean isWrongBook = itemStack.is(Items.BOOK)
                || itemStack.is(Items.WRITABLE_BOOK)
                || itemStack.is(Items.WRITTEN_BOOK)
                || itemStack.is(Items.ENCHANTED_BOOK);
        if (isWrongBook) {
            boolean isArcaneRetributionActivate = Config.BINARY_ARCANE_RETRIBUTION.get();
            if (isArcaneRetributionActivate) {
                if (!level.isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) level;

                    // Consume the item
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }

                    // Special effects for Enchanted Books (Vex sounds + Soul particles)
                    if (itemStack.is(Items.ENCHANTED_BOOK)) {
                        serverLevel.playSound(null, pos, SoundEvents.VEX_DEATH, SoundSource.BLOCKS, 2.0F, 0.0F);
                        serverLevel.sendParticles(
                                ParticleTypes.SOUL,
                                pos.getX() + 0.5,
                                pos.getY() + 1.0,
                                pos.getZ() + 0.5,
                                1500,
                                4.0F,
                                4.0F,
                                4.0F,
                                0.0
                        );
                    }

                    // Destroy the table immediately
                    serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                    // Retrieve the custom damage type
                    DamageSource arcaneRetribution = new DamageSource(
                            level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypeRegistry.ARCANE_RETRIBUTION)
                    );

                    // Create an explosion at the block's location
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

                return InteractionResult.CONSUME;

            } else {
                player.displayClientMessage(Component.translatable("block.enchanting_overhauled.deactivated_enchanting_table.wrong_catalyst"), true);
            }
        }

        return InteractionResult.PASS;
    }
}