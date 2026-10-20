package johnsmith.enchantingoverhauled.client;

import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.api.enchantment.value.DifficultyContext;
import johnsmith.enchantingoverhauled.api.item.ArcaneItemNamingUtil;
import johnsmith.enchantingoverhauled.client.tooltip.OverhauledTooltipFormatter;

import johnsmith.enchantmentcore.api.client.gui.EnchantmentCoreWidgets;
import johnsmith.enchantmentcore.api.tooltip.TooltipFormatterRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Difficulty;

public class Common {
    public static void initialize() {
        ArcaneItemNamingUtil.setClientRegistryAccessSupplier(() -> {
            Minecraft instance = Minecraft.getInstance();
            return instance.level != null ? instance.level.registryAccess() : null;
        });

        DifficultyContext.setClientFallback(() -> {
            Minecraft instance = Minecraft.getInstance();
            return instance.level != null ? instance.level.getDifficulty() : Difficulty.NORMAL;
        });

        Config.MANAGER.setScreenFactory(EnchantmentCoreWidgets.createScreenFactory(Config.MANAGER));

        TooltipFormatterRegistry.registerFormatter(new OverhauledTooltipFormatter());
    }
}