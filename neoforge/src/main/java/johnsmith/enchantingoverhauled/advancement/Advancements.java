package johnsmith.enchantingoverhauled.advancement;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Handles the deferred registration of advancement criteria triggers on the NeoForge platform.
 */
public class Advancements {

    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, Constants.MOD_ID);

    /**
     * A holder for the Activate Altar trigger, registered under the mod ID.
     */
    public static final DeferredHolder<CriterionTrigger<?>, ActivateAltarTrigger> ACTIVATE_ALTAR =
            TRIGGERS.register("activate_altar", () -> CriteriaRegistry.ACTIVATE_ALTAR);

    /**
     * Initializes the deferred register and attaches it to the mod event bus.
     *
     * @param eventBus The event bus to register the triggers with.
     */
    public static void initialize(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}