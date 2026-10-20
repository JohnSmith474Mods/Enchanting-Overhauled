package johnsmith.enchantingoverhauled.api.structure.processor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A composite {@link StructureProcessor} that sequentially delegates block processing across
 * a list of referenced {@link StructureProcessorList} holders.
 * <p>
 * Allows modular processor lists (such as roof, wall, frame, and mossify processor lists)
 * to be composed and reused cleanly within a single parent processor list JSON file.
 */
public class ProcessorList extends StructureProcessor {

    /**
     * The {@link MapCodec} for serializing and deserializing a composite {@link ProcessorList} from JSON.
     */
    public static final MapCodec<ProcessorList> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    StructureProcessorType.LIST_CODEC.listOf().fieldOf("processors").forGetter(p -> p.processorLists)
            ).apply(instance, ProcessorList::new)
    );

    /**
     * The ordered list of nested {@link StructureProcessorList} holders to execute sequentially.
     */
    private final List<Holder<StructureProcessorList>> processorLists;

    /**
     * Constructs a new composite {@code ProcessorList}.
     *
     * @param processorLists The list of {@link StructureProcessorList} holders to chain.
     */
    public ProcessorList(List<Holder<StructureProcessorList>> processorLists) {
        this.processorLists = processorLists;
    }

    /**
     * Sequentially pipes {@code blockInfoOut} through every processor in each nested {@link StructureProcessorList}.
     * If any processor returns {@code null} (removing the block), processing halts immediately and returns {@code null}.
     *
     * @param level        The level reader.
     * @param offset       The template offset position.
     * @param pos          The structure pivot position.
     * @param blockInfoIn  The unrotated original block info from the template.
     * @param blockInfoOut The current transformed block info in world coordinates.
     * @param settings     The structure placement settings.
     * @return The final processed {@link StructureTemplate.StructureBlockInfo}, or {@code null} if removed.
     */
    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfoIn, StructureTemplate.StructureBlockInfo blockInfoOut, StructurePlaceSettings settings) {
        StructureTemplate.StructureBlockInfo currentBlockInfo = blockInfoOut;

        for (Holder<StructureProcessorList> listHolder : this.processorLists) {
            for (StructureProcessor processor : listHolder.value().list()) {
                currentBlockInfo = processor.processBlock(level, offset, pos, blockInfoIn, currentBlockInfo, settings);

                if (currentBlockInfo == null) {
                    return null;
                }
            }
        }

        return currentBlockInfo;
    }

    /**
     * Retrieves the registered {@link StructureProcessorType} for this composite processor.
     *
     * @return {@link StructureProcessors#LIST_PROCESSOR}.
     */
    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessors.LIST_PROCESSOR.get();
    }
}