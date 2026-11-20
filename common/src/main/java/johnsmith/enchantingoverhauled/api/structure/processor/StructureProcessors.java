package johnsmith.enchantingoverhauled.api.structure.processor;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

import org.jetbrains.annotations.NotNull;

/**
 * Enumeration of custom {@link StructureProcessor} codecs introduced by Enchanting Overhauled.
 * <p>
 * Provides codec references for registering custom structure processors across Fabric,
 * Forge, and NeoForge.
 */
public enum StructureProcessors implements StringRepresentable {

    /**
     * Structure processor that places persistent Jungle Leaves above eligible floor blocks ({@link OvergrowthProcessor}).
     */
    OVERGROWTH("overgrowth", OvergrowthProcessor.CODEC),

    /**
     * Structure processor that attaches vines to the sturdy horizontal faces of solid blocks ({@link VinesProcessor}).
     */
    ADD_VINES("add_vines", VinesProcessor.CODEC),

    /**
     * Enhanced rule processor supporting property copying and gradient/weighted outputs ({@link RuleProcessor}).
     */
    RULE("rule", RuleProcessor.CODEC),

    /**
     * Composite processor that chains multiple {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList}s ({@link ProcessorList}).
     */
    LIST_PROCESSOR("processor_list", ProcessorList.CODEC);

    /**
     * The registry path key for this structure processor type.
     */
    private final String key;

    /**
     * The {@link MapCodec} for this structure processor.
     */
    private final MapCodec<? extends StructureProcessor> codec;

    /**
     * Constructs a {@code StructureProcessors} enum constant.
     *
     * @param key   The serialized registry path name.
     * @param codec The {@link MapCodec} for the processor.
     */
    StructureProcessors(String key, MapCodec<? extends StructureProcessor> codec) {
        this.key = key;
        this.codec = codec;
    }

    /**
     * Retrieves the {@link MapCodec} for registration.
     *
     * @return The {@link MapCodec} for this structure processor.
     */
    public MapCodec<? extends StructureProcessor> codec() {
        return this.codec;
    }

    /**
     * Retrieves the serialized registry path name for this structure processor type.
     *
     * @return The string key.
     */
    @Override
    public @NotNull String getSerializedName() {
        return this.key;
    }
}