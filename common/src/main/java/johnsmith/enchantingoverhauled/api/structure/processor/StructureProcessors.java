package johnsmith.enchantingoverhauled.api.structure.processor;

import com.mojang.serialization.MapCodec;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Enumeration of custom {@link StructureProcessorType}s introduced by Enchanting Overhauled.
 * <p>
 * Provides deferred factory suppliers for registering custom structure processors across Fabric,
 * Forge, and NeoForge.
 */
public enum StructureProcessors implements StringRepresentable {

    /**
     * Structure processor that places persistent Jungle Leaves above eligible floor blocks ({@link OvergrowthProcessor}).
     */
    OVERGROWTH("overgrowth", create(OvergrowthProcessor.CODEC)),

    /**
     * Structure processor that attaches vines to the sturdy horizontal faces of solid blocks ({@link VinesProcessor}).
     */
    ADD_VINES("add_vines", create(VinesProcessor.CODEC)),

    /**
     * Enhanced rule processor supporting property copying and gradient/weighted outputs ({@link RuleProcessor}).
     */
    RULE("rule", create(RuleProcessor.CODEC)),

    /**
     * Composite processor that chains multiple {@link net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList}s ({@link ProcessorList}).
     */
    LIST_PROCESSOR("processor_list", create(ProcessorList.CODEC));

    /**
     * The registry path key for this structure processor type.
     */
    private final String key;

    /**
     * Deferred factory supplier creating the {@link StructureProcessorType}.
     */
    private final Supplier<StructureProcessorType<?>> factory;

    /**
     * Cached {@link StructureProcessorType} instance once built.
     */
    private StructureProcessorType<?> type;

    /**
     * Constructs a {@code StructureProcessors} enum constant.
     *
     * @param key     The serialized registry path name.
     * @param factory The factory supplier for the processor type.
     */
    StructureProcessors(String key, Supplier<StructureProcessorType<?>> factory) {
        this.key = key;
        this.factory = factory;
    }

    /**
     * Creates a deferred supplier for the specified structure processor codec.
     * <p>
     * The explicit intermediate variable declaration is mandatory. Java type inference
     * fails to resolve nested generic lambdas involving wildcards. Returning the nested
     * lambda directly causes the compiler to incorrectly map the bounded type to the base
     * interface. The declared variable forces the resolution of the specific generic
     * parameter before encapsulation within the wildcard supplier.
     *
     * @param codec The map codec defining the structure processor.
     * @param <P>   The specific structure processor implementation type.
     * @return A supplier containing the typed structure processor type.
     */
    private static <P extends StructureProcessor> Supplier<StructureProcessorType<?>> create(MapCodec<P> codec) {
        StructureProcessorType<P> type = () -> codec;
        return () -> type;
    }

    /**
     * Builds and caches the {@link StructureProcessorType} instance for registration.
     *
     * @return The built {@link StructureProcessorType}.
     */
    public StructureProcessorType<?> build() {
        this.type = this.factory.get();
        return this.type;
    }

    /**
     * Retrieves the built {@link StructureProcessorType} cast to the expected processor type parameter.
     *
     * @param <T> The {@link StructureProcessor} subclass type.
     * @return The registered {@link StructureProcessorType}.
     */
    @SuppressWarnings("unchecked")
    public <T extends StructureProcessor> StructureProcessorType<T> get() {
        return (StructureProcessorType<T>) this.type;
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