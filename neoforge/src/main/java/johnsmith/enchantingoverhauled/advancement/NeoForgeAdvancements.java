package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeAdvancements {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Criteria criterion : Criteria.values()) {
            TRIGGERS.register(criterion.getSerializedName(), criterion::build);
        }
        TRIGGERS.register(eventBus);
    }

    private NeoForgeAdvancements() {}
}