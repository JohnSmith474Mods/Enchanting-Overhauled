package johnsmith.enchantingoverhauled.client.gui.tooltip.scribe;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public record ScribeTooltipContext(
        Holder<Enchantment> ench,
        int level,
        boolean isSource,
        int anchorX,
        int itemY,
        boolean obfuscate
) {}