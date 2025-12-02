package johnsmith.enchantingoverhauled.platform.services;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.enchantment.OverhauledEnchantmentMenu;
import johnsmith.enchantingoverhauled.structure.processor.BlockAgeProcessor;
import johnsmith.enchantingoverhauled.structure.processor.DesertifyProcessor;
import johnsmith.enchantingoverhauled.structure.processor.OvergrowthProcessor;
import johnsmith.enchantingoverhauled.structure.processor.VinesProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import java.util.Optional;

/**
 * Abstraction interface for platform-specific functionality.
 * <p>
 * This interface defines methods that must be implemented differently on Fabric and NeoForge,
 * such as registry object retrieval (due to different registration systems like DeferredRegister vs Registry),
 * loader checks, and environment detection.
 */
public interface IPlatformHelper {

    /**
     * Gets the name of the current mod loader.
     *
     * @return "Fabric" or "NeoForge".
     */
    String getPlatformName();

    /**
     * Checks if another mod with the given ID is currently loaded.
     *
     * @param modId The mod ID to check.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Checks if the game is running in a development environment (IDE).
     *
     * @return True if in development, false if in production.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the environment name as a string for logging purposes.
     *
     * @return "development" or "production".
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Gets the Deactivated Enchanting Table block.
     */
    Block getDeactivatedEnchantingTable();

    /**
     * Gets the Disturbed Enchanting Table block.
     */
    Block getDisturbedEnchantingTable();

    /**
     * Gets the Enchanted Tome item.
     */
    Item getEnchantedTome();

    /**
     * Gets the Deactivated Enchanting Table block item.
     */
    Item getDeactivatedEnchantingTableItem();

    /**
     * Gets the Disturbed Enchanting Table block item.
     */
    Item getDisturbedEnchantingTableItem();

    /**
     * Retrieves the {@link EnchantmentTheme} registry from the provided access object.
     * <p>
     * This abstracts the difference in how dynamic/synced registries are accessed or wrapped
     * on different platforms, ensuring safe retrieval on both client and server.
     *
     * @param registryAccess The world's registry access.
     * @return An optional containing the registry if present.
     */
    Optional<Registry<EnchantmentTheme>> getThemeRegistry(RegistryAccess registryAccess);

    /**
     * Gets the registered type for the Block Age structure processor.
     */
    StructureProcessorType<BlockAgeProcessor> getBlockAgeProcessor();

    /**
     * Gets the registered type for the Desertify structure processor.
     */
    StructureProcessorType<DesertifyProcessor> getDesertProcessor();

    /**
     * Gets the registered type for the Overgrowth structure processor.
     */
    StructureProcessorType<OvergrowthProcessor> getOvergrowthProcessor();

    /**
     * Gets the registered type for the Vines structure processor.
     */
    StructureProcessorType<VinesProcessor> getVinesProcessor();

    MenuType<OverhauledEnchantmentMenu> getOverhauledEnchantmentMenuType();
}