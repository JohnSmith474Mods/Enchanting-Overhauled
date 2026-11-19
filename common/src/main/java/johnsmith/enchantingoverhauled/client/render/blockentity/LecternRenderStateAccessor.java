package johnsmith.enchantingoverhauled.client.render.blockentity;

/**
 * Duck interface implemented on {@link net.minecraft.client.renderer.blockentity.state.LecternRenderState}
 * via mixin to carry custom book model/material/foil metadata from {@code extractRenderState} to {@code submit}.
 */
public interface LecternRenderStateAccessor {

    int BOOK_KIND_DEFAULT = 0;
    int BOOK_KIND_ENCHANTED_BOOK = 1;
    int BOOK_KIND_ENCHANTED_TOME = 2;

    int enchanting_overhauled$getBookKind();

    void enchanting_overhauled$setBookKind(int bookKind);

    boolean enchanting_overhauled$hasFoil();

    void enchanting_overhauled$setHasFoil(boolean hasFoil);
}
