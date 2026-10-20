package johnsmith.enchantingoverhauled.entity;

import johnsmith.enchantingoverhauled.Constants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ForgeEntities {

    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.MOD_ID);

    public static void initialize(IEventBus eventBus) {
        for (Entities entity : Entities.values()) {
            ENTITIES.register(entity.getSerializedName(), entity::build);
        }
        ENTITIES.register(eventBus);
        eventBus.addListener(ForgeEntities::registerSpawnPlacements);
    }

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                Entities.ENCHANTED_BOOK.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EnchantedBookEntity::checkEnchantedBookSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }

    private ForgeEntities() {}
}