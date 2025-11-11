package johnsmith.mixin.accessor;

import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnchantingTableBlockEntity.class)
public interface EnchantingTableBlockEntityAccessor {
    @Accessor("RANDOM")
    Random getRandom();
}