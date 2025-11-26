package johnsmith.enchantingoverhauled.platform;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.platform.services.IPlatformHelper;

import johnsmith.enchantingoverhauled.structure.processor.Processors;
import johnsmith.enchantingoverhauled.structure.processor.BlockAgeProcessor;
import johnsmith.enchantingoverhauled.structure.processor.DesertifyProcessor;
import johnsmith.enchantingoverhauled.structure.processor.OvergrowthProcessor;
import johnsmith.enchantingoverhauled.structure.processor.VinesProcessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import java.util.Optional;

/**
 * Fabric-specific implementation of the {@link IPlatformHelper} service.
 */
public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Block getDeactivatedEnchantingTable() {
        return Blocks.DEACTIVATED_ENCHANTING_TABLE;
    }

    @Override
    public Block getDisturbedEnchantingTable() {
        return Blocks.DISTURBED_ENCHANTING_TABLE;
    }

    @Override
    public Item getEnchantedTome() {
        return Items.ENCHANTED_TOME;
    }

    @Override
    public Item getDeactivatedEnchantingTableItem() {
        return Items.DEACTIVATED_ENCHANTING_TABLE_ITEM;
    }

    @Override
    public Item getDisturbedEnchantingTableItem() {
        return Items.DISTURBED_ENCHANTING_TABLE_ITEM;
    }

    @Override
    public Optional<Registry<EnchantmentTheme>> getThemeRegistry(RegistryAccess registryAccess) {
        return registryAccess.registry(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);
    }

    @Override
    public StructureProcessorType<BlockAgeProcessor> getBlockAgeProcessor() {
        return Processors.BLOCK_AGE_PROCESSOR;
    }

    @Override
    public StructureProcessorType<DesertifyProcessor> getDesertProcessor() {
        return Processors.DESERT_PROCESSOR;
    }

    @Override
    public StructureProcessorType<OvergrowthProcessor> getOvergrowthProcessor() {
        return Processors.OVERGROWTH_PROCESSOR;
    }

    @Override
    public StructureProcessorType<VinesProcessor> getVinesProcessor() {
        return Processors.VINES_PROCESSOR;
    }
}