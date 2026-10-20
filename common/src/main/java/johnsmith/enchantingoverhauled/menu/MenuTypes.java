package johnsmith.enchantingoverhauled.menu;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import org.jetbrains.annotations.NotNull;

public enum MenuTypes implements StringRepresentable {
    ENCHANT("enchantment_menu"),
    SCRIBE("scribe_menu");

    private final String key;
    private MenuType<?> menuType;

    MenuTypes(String key) {
        this.key = key;
    }

    public MenuType<?> build(MenuType<?> menuType) {
        if (this.menuType == null) this.menuType = menuType;
        return this.menuType;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractContainerMenu> MenuType<T> get() {
        return (MenuType<T>) this.menuType;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.key;
    }
}