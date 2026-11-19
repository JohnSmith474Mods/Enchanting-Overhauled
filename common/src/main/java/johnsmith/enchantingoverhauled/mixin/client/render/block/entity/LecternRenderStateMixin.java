package johnsmith.enchantingoverhauled.mixin.client.render.block.entity;

import johnsmith.enchantingoverhauled.client.render.blockentity.LecternRenderStateAccessor;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LecternRenderState.class)
public class LecternRenderStateMixin implements LecternRenderStateAccessor {

    @Unique
    private int enchanting_overhauled$bookKind = BOOK_KIND_DEFAULT;

    @Unique
    private boolean enchanting_overhauled$hasFoil = false;

    @Override
    public int enchanting_overhauled$getBookKind() {
        return this.enchanting_overhauled$bookKind;
    }

    @Override
    public void enchanting_overhauled$setBookKind(int bookKind) {
        this.enchanting_overhauled$bookKind = bookKind;
    }

    @Override
    public boolean enchanting_overhauled$hasFoil() {
        return this.enchanting_overhauled$hasFoil;
    }

    @Override
    public void enchanting_overhauled$setHasFoil(boolean hasFoil) {
        this.enchanting_overhauled$hasFoil = hasFoil;
    }
}
