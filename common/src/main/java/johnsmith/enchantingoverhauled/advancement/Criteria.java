package johnsmith.enchantingoverhauled.advancement;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

/**
 * Registry handler for custom advancement criteria triggers.
 * <p>
 * Holds the singleton instances of custom triggers used by the mod.
 */
public enum Criteria implements StringRepresentable {
    ACTIVATE_ALTAR("activate_altar", ActivateAltarTrigger::new);

    private final String id;
    private final Supplier<SimpleCriterionTrigger<?>> factory;
    private SimpleCriterionTrigger<?> criterion;

    Criteria(String id, Supplier<SimpleCriterionTrigger<?>> factory) {
        this.id = id;
        this.factory = factory;
    }

    public SimpleCriterionTrigger<?> build() {
        if (this.criterion == null) this.criterion = factory.get();
        return this.criterion;
    }

    public SimpleCriterionTrigger<?> get() {
        return this.build();
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}