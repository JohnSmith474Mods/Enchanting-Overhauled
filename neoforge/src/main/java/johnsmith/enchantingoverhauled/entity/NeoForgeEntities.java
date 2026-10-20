package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Entities entity : Entities.values()) {
            ENTITIES.register(entity.getSerializedName(), entity::build);
        }
        ENTITIES.register(eventBus);
        eventBus.addListener(NeoForgeEntities::registerSpawnPlacements);
    }

    private static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                Entities.ENCHANTED_BOOK.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EnchantedBookEntity::checkEnchantedBookSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    private NeoForgeEntities() {}
}