package johnsmith.enchantingoverhauled.mixin.accessor;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

// Target the *inner class* Settings
@Mixin(BlockBehaviour.Properties.class)
public interface AbstractBlockSettingsAccessor {

    // This creates a public setter for the package-private 'toolRequired' field
    @Mutable
    @Accessor("requiresCorrectToolForDrops")
    void setToolRequired(boolean toolRequired);
}