package johnsmith.mixin.accessor;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

// Target the *inner class* Settings
@Mixin(AbstractBlock.Settings.class)
public interface AbstractBlockSettingsAccessor {

    // This creates a public setter for the package-private 'toolRequired' field
    @Mutable
    @Accessor("toolRequired")
    void setToolRequired(boolean toolRequired);
}