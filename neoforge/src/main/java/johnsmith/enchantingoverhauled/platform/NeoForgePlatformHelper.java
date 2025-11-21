package johnsmith.enchantingoverhauled.platform;

import johnsmith.enchantingoverhauled.api.enchantment.theme.EnchantmentTheme;
import johnsmith.enchantingoverhauled.api.enchantment.theme.registry.EnchantmentThemeRegistry;
import johnsmith.enchantingoverhauled.block.Blocks;
import johnsmith.enchantingoverhauled.config.NeoForgeConfig;
import johnsmith.enchantingoverhauled.enchantment.Enchantments;
import johnsmith.enchantingoverhauled.item.Items;
import johnsmith.enchantingoverhauled.platform.services.IPlatformHelper;
import johnsmith.enchantingoverhauled.structure.processor.*;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Optional;

/**
 * NeoForge-specific implementation of the {@link IPlatformHelper} service.
 */
public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Block getDeactivatedEnchantingTable() {
        return Blocks.DEACTIVATED_ENCHANTING_TABLE.get();
    }

    @Override
    public Block getDisturbedEnchantingTable() {
        return Blocks.DISTURBED_ENCHANTING_TABLE.get();
    }

    @Override
    public Item getEnchantedTome() {
        return Items.ENCHANTED_TOME.get();
    }

    @Override
    public Item getDeactivatedEnchantingTableItem() {
        return Items.DEACTIVATED_ENCHANTING_TABLE_ITEM.get();
    }

    @Override
    public Item getDisturbedEnchantingTableItem() {
        return Items.DISTURBED_ENCHANTING_TABLE_ITEM.get();
    }

    @Override
    public Enchantment getMagicProtectionEnchantment() {
        return Enchantments.MAGIC_PROTECTION.get();
    }

    @Override
    public Optional<Registry<EnchantmentTheme>> getThemeRegistry(RegistryAccess registryAccess) {
        return registryAccess.registry(EnchantmentThemeRegistry.THEME_REGISTRY_KEY);
    }

    @Override
    public StructureProcessorType<BlockAgeProcessor> getBlockAgeProcessor() {
        return Processors.BLOCK_AGE_PROCESSOR.get();
    }

    @Override
    public StructureProcessorType<DesertifyProcessor> getDesertProcessor() {
        return Processors.DESERT_PROCESSOR.get();
    }

    @Override
    public StructureProcessorType<OvergrowthProcessor> getOvergrowthProcessor() {
        return Processors.OVERGROWTH_PROCESSOR.get();
    }

    @Override
    public StructureProcessorType<VinesProcessor> getVinesProcessor() {
        return Processors.VINES_PROCESSOR.get();
    }

    @Override
    public void saveConfig() {
        NeoForgeConfig.saveConfig();
    }
}