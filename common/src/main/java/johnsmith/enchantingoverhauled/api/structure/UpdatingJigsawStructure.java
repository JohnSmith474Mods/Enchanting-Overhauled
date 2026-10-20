package johnsmith.enchantingoverhauled.api.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A custom {@link Structure} implementation extending the behavior of vanilla's {@link JigsawStructure}
 * by performing a post-placement neighbor shape update pass across all placed structure pieces.
 * <p>
 * When complex multi-piece structures (such as Scribe's Rise) assemble walls, stairs, fences, or glass panes
 * across jigsaw piece boundaries or via custom rule processors, vanilla template placement does not always
 * reconcile connected block states (e.g., corner stairs or connected walls). This structure runs a forward,
 * backward, and final forward pass of {@link Block#updateFromNeighbourShapes} after placement to ensure
 * all connecting block states are properly resolved.
 */
public class UpdatingJigsawStructure extends Structure {

    /**
     * The {@link MapCodec} used to serialize and deserialize {@link UpdatingJigsawStructure} definitions from JSON,
     * including range validation accounting for terrain adaptation padding.
     */
    public static final MapCodec<UpdatingJigsawStructure> CODEC = RecordCodecBuilder.<UpdatingJigsawStructure>mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(JigsawStructure.MIN_DEPTH, JigsawStructure.MAX_DEPTH).fieldOf("size").forGetter(structure -> structure.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
                    Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(structure -> structure.poolAliases),
                    DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(structure -> structure.dimensionPadding),
                    LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(structure -> structure.liquidSettings)
            ).apply(instance, UpdatingJigsawStructure::new)
    ).validate(UpdatingJigsawStructure::verifyRange);

    /**
     * The registered {@link StructureType} singleton for {@code enchanting_overhauled:updating_jigsaw}.
     */
    public static final StructureType<UpdatingJigsawStructure> TYPE = () -> CODEC;

    /**
     * The starting template pool holder for jigsaw generation.
     */
    private final Holder<StructureTemplatePool> startPool;

    /**
     * Optional target jigsaw block name to anchor the starting piece.
     */
    private final Optional<ResourceLocation> startJigsawName;

    /**
     * The maximum jigsaw recursion depth ({@code size}).
     */
    private final int maxDepth;

    /**
     * The height provider determining the starting Y coordinate.
     */
    private final HeightProvider startHeight;

    /**
     * Whether to apply the legacy village bounding-box expansion hack.
     */
    private final boolean useExpansionHack;

    /**
     * Optional heightmap type onto which the starting position should be projected.
     */
    private final Optional<Heightmap.Types> projectStartToHeightmap;

    /**
     * Maximum allowed block distance from the structure's starting center.
     */
    private final int maxDistanceFromCenter;

    /**
     * List of template pool alias bindings for remapping pools during generation.
     */
    private final List<PoolAliasBinding> poolAliases;

    /**
     * Vertical padding applied to the top and bottom of the dimension's buildable height.
     */
    private final DimensionPadding dimensionPadding;

    /**
     * Liquid handling settings during template placement (e.g., waterlogging behavior).
     */
    private final LiquidSettings liquidSettings;

    /**
     * Constructs a new {@code UpdatingJigsawStructure}.
     *
     * @param settings                The base structure settings (biomes, spawn overrides, generation step, terrain adaptation).
     * @param startPool               The starting template pool.
     * @param startJigsawName         Optional target jigsaw block name for the start piece.
     * @param maxDepth                Maximum jigsaw branching depth.
     * @param startHeight             Height provider for the starting Y level.
     * @param useExpansionHack        Whether to enable the legacy expansion hack.
     * @param projectStartToHeightmap Optional heightmap projection type.
     * @param maxDistanceFromCenter   Maximum distance from the center of the structure.
     * @param poolAliases             List of pool alias bindings.
     * @param dimensionPadding        Dimension boundary padding.
     * @param liquidSettings          Liquid placement settings.
     */
    public UpdatingJigsawStructure(
            StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName,
            int maxDepth,
            HeightProvider startHeight,
            boolean useExpansionHack,
            Optional<Heightmap.Types> projectStartToHeightmap,
            int maxDistanceFromCenter,
            List<PoolAliasBinding> poolAliases,
            DimensionPadding dimensionPadding,
            LiquidSettings liquidSettings
    ) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    /**
     * Validates that the structure's {@link #maxDistanceFromCenter} plus any terrain adaptation padding
     * does not exceed {@code 128} blocks.
     *
     * @param structure The structure instance to validate.
     * @return A {@link DataResult} indicating success or an error message if the range exceeds {@code 128}.
     */
    private static DataResult<UpdatingJigsawStructure> verifyRange(UpdatingJigsawStructure structure) {
        int padding = switch (structure.terrainAdaptation()) {
            case NONE -> 0;
            case BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE -> 12;
        };
        return structure.maxDistanceFromCenter + padding > 128
                ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128")
                : DataResult.success(structure);
    }

    /**
     * Computes the starting generation point and assembles the jigsaw pieces via {@link JigsawPlacement#addPieces}.
     *
     * @param context The world generation context.
     * @return An {@link Optional} containing the {@link GenerationStub} if placement succeeded.
     */
    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int y = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), y, chunkPos.getMinBlockZ());

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.maxDepth,
                blockPos,
                this.useExpansionHack,
                this.projectStartToHeightmap,
                this.maxDistanceFromCenter,
                PoolAliasLookup.create(this.poolAliases, blockPos, context.seed()),
                this.dimensionPadding,
                this.liquidSettings
        );
    }

    /**
     * Executed after the structure pieces have been placed into the chunk.
     * Collects all block positions within the intersection of the current chunk and each placed piece's
     * bounding box, then performs three passes (forward, backward, forward) of neighbor shape updates
     * to resolve connected stairs, walls, fences, and panes.
     *
     * @param level            The world generation level.
     * @param structureManager The structure manager.
     * @param chunkGenerator   The active chunk generator.
     * @param random           The random source.
     * @param boundingBox      The current generation bounding box.
     * @param chunkPos         The chunk coordinates being populated.
     * @param piecesContainer  The container holding the placed structure pieces.
     */
    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        super.afterPlace(level, structureManager, chunkGenerator, random, boundingBox, chunkPos, piecesContainer);

        BoundingBox chunkBox = new BoundingBox(
                chunkPos.getMinBlockX(),
                level.getMinBuildHeight(),
                chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(),
                level.getMaxBuildHeight() - 1,
                chunkPos.getMaxBlockZ()
        );

        List<BlockPos> structureBounds = new ArrayList<>();

        piecesContainer.pieces().forEach(piece -> {
            BoundingBox pieceBox = piece.getBoundingBox();

            if (pieceBox.intersects(chunkBox)) {
                BlockPos.betweenClosed(
                        Math.max(pieceBox.minX(), chunkBox.minX()),
                        Math.max(pieceBox.minY(), chunkBox.minY()),
                        Math.max(pieceBox.minZ(), chunkBox.minZ()),
                        Math.min(pieceBox.maxX(), chunkBox.maxX()),
                        Math.min(pieceBox.maxY(), chunkBox.maxY()),
                        Math.min(pieceBox.maxZ(), chunkBox.maxZ())
                ).forEach(pos -> structureBounds.add(pos.immutable()));
            }
        });

        for (int i = 0; i < structureBounds.size(); i++) {
            updateNode(level, structureBounds.get(i));
        }

        for (int i = structureBounds.size() - 1; i >= 0; i--) {
            updateNode(level, structureBounds.get(i));
        }

        for (int i = 0; i < structureBounds.size(); i++) {
            updateNode(level, structureBounds.get(i));
        }
    }

    /**
     * Evaluates {@link Block#updateFromNeighbourShapes} for the block at {@code pos} and updates
     * the block state in the level if its connected shape changed.
     *
     * @param level The world generation level.
     * @param pos   The block position to update.
     */
    private void updateNode(WorldGenLevel level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        BlockState updatedState = Block.updateFromNeighbourShapes(currentState, level, pos);

        if (currentState != updatedState) {
            level.setBlock(pos, updatedState, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Retrieves the {@link StructureType} for this structure.
     *
     * @return {@link #TYPE}.
     */
    @Override
    public StructureType<?> type() {
        return TYPE;
    }
}