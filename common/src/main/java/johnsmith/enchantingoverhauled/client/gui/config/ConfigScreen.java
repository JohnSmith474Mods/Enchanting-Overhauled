package johnsmith.enchantingoverhauled.client.gui.config;

import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.platform.Services;

import com.google.common.collect.ImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The main configuration screen for Enchanting Overhauled.
 * <p>
 * This screen provides a user interface for modifying the mod's configuration settings in-game.
 * It uses a scrollable list to display various categories and options, handling data persistence
 * via the platform-specific configuration helper upon closing.
 */
public class ConfigScreen extends Screen {
    private static final String PREFIX = "config.enchanting_overhauled.";
    private static final String ENCHANTING_TABLE_SETTINGS = "section.enchanting_table";
    private static final String ANVIL_SETTINGS = "section.anvil";
    private static final String ENCHANTMENT_SETTINGS = "section.enchantments";
    private static final String ENCHANTMENT_TOOLTIP_SETTINGS = "section.enchantment_tooltip";
    private static final String PROTECTION_SETTINGS = "section.protection";
    private static final String DAMAGE_SETTINGS = "section.damage";
    private static final String FORTUNE_SETTINGS = "section.fortune";
    private static final String LOOT_SETTINGS = "section.loot";
    private static final String UNBREAKING_SETTINGS = "section.unbreaking";
    private static final String XP_SETTINGS = "section.xp";
    private static final String ACCESSIBILITY_SETTINGS = "section.accessibility";

    /**
     * The parent screen to return to when this screen is closed.
     */
    private final Screen parent;

    /**
     * The scrollable list containing all configuration entries.
     */
    private ConfigList list;

    /**
     * The button used to reset all modified options to their default values.
     */
    private Button resetButton;

    /**
     * The layout manager for the screen's header and footer.
     */
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    /**
     * Constructs a new ConfigScreen.
     *
     * @param parent The screen that opened this configuration menu.
     */
    public ConfigScreen(Screen parent) {
        super(createStyledTitle());
        this.parent = parent;
    }

    /**
     * Creates a styled title component for the screen.
     * <p>
     * Resolves the translation key, splits it on the first space,
     * and applies {@link ChatFormatting#BLUE} and {@link ChatFormatting#BOLD} to the first word,
     * and {@link ChatFormatting#YELLOW} and {@link ChatFormatting#BOLD} to the rest.
     *
     * @return The styled title Component.
     */
    private static Component createStyledTitle() {
        // 1. Resolve the translation to a raw String
        String fullText = Component.translatable(PREFIX + "title").getString();

        // 2. Split on the first whitespace
        String[] parts = fullText.split("\\s+", 2);

        // 3. Reconstruct as styled components
        if (parts.length >= 2) {
            return Component.literal(parts[0])
                    .withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD)
                    .append(Component.literal(" "))
                    .append(Component.literal(parts[1])
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        }

        // Fallback: If no space exists, return the whole string in Blue
        return Component.literal(fullText).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
    }

    /**
     * Initializes the screen's components.
     * <p>
     * Sets up the header, the configuration list, and the footer buttons (Reset and Done).
     */
    @Override
    protected void init() {
        // 1. Add Title (managed by layout)
        this.layout.addTitleHeader(this.title, this.font);

        // 2. Create the List
        this.list = new ConfigList(this, this.minecraft);
        this.layout.addToContents(this.list);

        // 3. Footer
        net.minecraft.client.gui.layouts.LinearLayout footerRow = net.minecraft.client.gui.layouts.LinearLayout.horizontal().spacing(8);

        this.resetButton = footerRow.addChild(Button.builder(Component.translatable(PREFIX + "reset"), button -> this.resetAll())
                .width(150)
                .build());

        footerRow.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .width(150)
                .build());

        this.layout.addToFooter(footerRow);

        // 4. Visit widgets & Position
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();

        this.updateMasterResetButton();
    }

    /**
     * Repositions elements when the screen is resized or initialized.
     */
    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    /**
     * Resets all configuration options in the list to their default values.
     */
    private void resetAll() {
        if (this.list == null) return;
        for (ConfigList.Entry entry : this.list.children()) {
            if (entry instanceof ConfigList.OptionEntry<?, ?> option) {
                option.reset();
            }
        }
        this.updateMasterResetButton();
    }

    /**
     * Updates the active state of the master reset button.
     * <p>
     * The button is enabled if at least one option is not set to its default value.
     */
    public void updateMasterResetButton() {
        if (this.list == null) return;
        boolean canReset = false;
        for (ConfigList.Entry entry : this.list.children()) {
            if (entry instanceof ConfigList.OptionEntry<?, ?> option && !option.isDefault()) {
                canReset = true;
                break;
            }
        }
        if (this.resetButton != null) {
            this.resetButton.active = canReset;
        }
    }

    /**
     * Saves the configuration and closes the screen, returning to the parent.
     */
    @Override
    public void onClose() {
        Services.PLATFORM.saveConfig();
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * A custom selection list for displaying configuration entries.
     */
    private class ConfigList extends ContainerObjectSelectionList<ConfigList.Entry> {
        public ConfigList(ConfigScreen screen, Minecraft minecraft) {
            super(minecraft, screen.width, screen.layout.getContentHeight(), screen.layout.getHeaderHeight(), 20);

            // --- Enchanting Table Settings ---
            addEntry(new CategoryEntry(PREFIX + ENCHANTING_TABLE_SETTINGS));
            addEntry(new BooleanEntry(Config.ARCANE_RETRIBUTION_KEY,
                    () -> Config.ARCANE_RETRIBUTION, v -> Config.ARCANE_RETRIBUTION = v,
                    Config.ARCANE_RETRIBUTION_DEFAULT));
            addEntry(new BooleanEntry(Config.ACTIVATION_EFFECTS_KEY,
                    () -> Config.ACTIVATION_EFFECTS, v -> Config.ACTIVATION_EFFECTS = v,
                    Config.ACTIVATION_EFFECTS_DEFAULT));
            addEntry(new BooleanEntry(Config.MINEABLE_ENCHANTING_TABLE_KEY,
                    () -> Config.MINEABLE_ENCHANTING_TABLE, v -> Config.MINEABLE_ENCHANTING_TABLE = v,
                    Config.MINEABLE_ENCHANTING_TABLE_DEFAULT));

            // --- Anvil Settings ---
            addEntry(new CategoryEntry(PREFIX + ANVIL_SETTINGS));
            addEntry(new IntEntry(Config.ANVIL_MAX_ITEM_COST_KEY,
                    () -> Config.ANVIL_MAX_ITEM_COST, v -> Config.ANVIL_MAX_ITEM_COST = v,
                    Config.ANVIL_MAX_ITEM_COST_DEFAULT, Config.ANVIL_MAX_ITEM_COST_FLOOR, Config.ANVIL_MAX_ITEM_COST_CEILING));
            addEntry(new IntEntry(Config.ANVIL_REPAIR_BONUS_KEY,
                    () -> Config.ANVIL_REPAIR_BONUS, v -> Config.ANVIL_REPAIR_BONUS = v,
                    Config.ANVIL_REPAIR_BONUS_DEFAULT, Config.ANVIL_REPAIR_BONUS_FLOOR, Config.ANVIL_REPAIR_BONUS_CEILING));
            addEntry(new DoubleEntry(Config.ANVIL_BREAK_CHANCE_KEY,
                    () -> Config.ANVIL_BREAK_CHANCE, v -> Config.ANVIL_BREAK_CHANCE = v,
                    Config.ANVIL_BREAK_CHANCE_DEFAULT, Config.ANVIL_BREAK_CHANCE_FLOOR, Config.ANVIL_BREAK_CHANCE_CEILING));

            // --- Enchantment Settings ---
            addEntry(new CategoryEntry(PREFIX + ENCHANTMENT_SETTINGS));
            addEntry(new IntEntry(Config.ENCHANTMENT_MAX_LEVEL_KEY,
                    () -> Config.ENCHANTMENT_MAX_LEVEL, v -> Config.ENCHANTMENT_MAX_LEVEL = v,
                    Config.ENCHANTMENT_MAX_LEVEL_DEFAULT, Config.ENCHANTMENT_MAX_LEVEL_FLOOR, Config.ENCHANTMENT_MAX_LEVEL_CEILING));
            addEntry(new BooleanEntry(Config.TOMES_HAVE_GREATER_ENCHANTMENTS_KEY,
                    () -> Config.TOMES_HAVE_GREATER_ENCHANTMENTS, v -> Config.TOMES_HAVE_GREATER_ENCHANTMENTS = v,
                    Config.TOMES_HAVE_GREATER_ENCHANTMENTS_DEFAULT));

            // --- Enchantment Tooltip Settings ---
            addEntry(new CategoryEntry(PREFIX + ENCHANTMENT_TOOLTIP_SETTINGS));
            addEntry(new BooleanEntry(Config.SHOW_ENCHANTMENT_TOOLTIP_HEADER_KEY,
                    () -> Config.SHOW_ENCHANTMENT_TOOLTIP_HEADER, v -> Config.SHOW_ENCHANTMENT_TOOLTIP_HEADER = v,
                    Config.SHOW_ENCHANTMENT_TOOLTIP_HEADER_DEFAULT));
            addEntry(new BooleanEntry(Config.SHOW_ENCHANTMENT_DESCRIPTIONS_KEY,
                    () -> Config.SHOW_ENCHANTMENT_DESCRIPTIONS, v -> Config.SHOW_ENCHANTMENT_DESCRIPTIONS = v,
                    Config.SHOW_ENCHANTMENT_DESCRIPTIONS_DEFAULT));

            // --- Protection Settings ---
            addEntry(new CategoryEntry(PREFIX + PROTECTION_SETTINGS));
            addEntry(new DoubleEntry(Config.PROTECTION_CAP_KEY,
                    () -> Config.PROTECTION_CAP, v -> Config.PROTECTION_CAP = v,
                    Config.PROTECTION_CAP_DEFAULT, Config.PROTECTION_CAP_FLOOR, Config.PROTECTION_CAP_CEILING));
            addEntry(new DoubleEntry(Config.PROTECTION_DIVISOR_KEY,
                    () -> Config.PROTECTION_DIVISOR, v -> Config.PROTECTION_DIVISOR = v,
                    Config.PROTECTION_DIVISOR_DEFAULT, Config.PROTECTION_DIVISOR_FLOOR, Config.PROTECTION_DIVISOR_CEILING));
            addEntry(new IntEntry(Config.PHYSICAL_PROTECTION_STRENGTH_KEY,
                    () -> Config.PHYSICAL_PROTECTION_STRENGTH, v -> Config.PHYSICAL_PROTECTION_STRENGTH = v,
                    Config.PHYSICAL_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING));
            addEntry(new IntEntry(Config.FIRE_PROTECTION_STRENGTH_KEY,
                    () -> Config.FIRE_PROTECTION_STRENGTH, v -> Config.FIRE_PROTECTION_STRENGTH = v,
                    Config.FIRE_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING));
            addEntry(new IntEntry(Config.BLAST_PROTECTION_STRENGTH_KEY,
                    () -> Config.BLAST_PROTECTION_STRENGTH, v -> Config.BLAST_PROTECTION_STRENGTH = v,
                    Config.BLAST_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING));
            addEntry(new IntEntry(Config.PROJECTILE_PROTECTION_STRENGTH_KEY,
                    () -> Config.PROJECTILE_PROTECTION_STRENGTH, v -> Config.PROJECTILE_PROTECTION_STRENGTH = v,
                    Config.PROJECTILE_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING));
            addEntry(new IntEntry(Config.MAGIC_PROTECTION_STRENGTH_KEY,
                    () -> Config.MAGIC_PROTECTION_STRENGTH, v -> Config.MAGIC_PROTECTION_STRENGTH = v,
                    Config.MAGIC_PROTECTION_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING));
            addEntry(new IntEntry(Config.FEATHER_FALLING_STRENGTH_KEY,
                    () -> Config.FEATHER_FALLING_STRENGTH, v -> Config.FEATHER_FALLING_STRENGTH = v,
                    Config.FEATHER_FALLING_STRENGTH_DEFAULT, Config.PROTECTION_STRENGTH_FLOOR, Config.PROTECTION_STRENGTH_CEILING));

            // --- Damage Settings ---
            addEntry(new CategoryEntry(PREFIX + DAMAGE_SETTINGS));
            addEntry(new DoubleEntry(Config.SHARPNESS_INITIAL_DAMAGE_KEY,
                    () -> (double) Config.SHARPNESS_INITIAL_DAMAGE, v -> Config.SHARPNESS_INITIAL_DAMAGE = v.floatValue(),
                    Config.SHARPNESS_INITIAL_DAMAGE_DEFAULT, Config.SHARPNESS_INITIAL_DAMAGE_FLOOR, Config.SHARPNESS_INITIAL_DAMAGE_CEILING));
            addEntry(new DoubleEntry(Config.SHARPNESS_DIMINISHING_RETURNS_KEY,
                    () -> (double) Config.SHARPNESS_DIMINISHING_RETURNS, v -> Config.SHARPNESS_DIMINISHING_RETURNS = v.floatValue(),
                    Config.SHARPNESS_DIMINISHING_RETURNS_DEFAULT, Config.SHARPNESS_DIMINISHING_RETURNS_FLOOR, Config.SHARPNESS_DIMINISHING_RETURNS_CEILING));
            addEntry(new DoubleEntry(Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_KEY,
                    () -> (double) Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT, v -> Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT = v.floatValue(),
                    Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_DEFAULT, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_FLOOR, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT_CEILING));
            addEntry(new DoubleEntry(Config.SMITE_MULTIPLIER_KEY,
                    () -> (double) Config.SMITE_MULTIPLIER, v -> Config.SMITE_MULTIPLIER = v.floatValue(),
                    Config.DAMAGE_MULTIPLIER_DEFAULT, Config.DAMAGE_MULTIPLIER_FLOOR, Config.DAMAGE_MULTIPLIER_CEILING));
            addEntry(new DoubleEntry(Config.EXTERMINATION_MULTIPLIER_KEY,
                    () -> (double) Config.EXTERMINATION_MULTIPLIER, v -> Config.EXTERMINATION_MULTIPLIER = v.floatValue(),
                    Config.DAMAGE_MULTIPLIER_DEFAULT, Config.DAMAGE_MULTIPLIER_FLOOR, Config.DAMAGE_MULTIPLIER_CEILING));

            // --- Loot Enchantment ---
            addEntry(new CategoryEntry(PREFIX + FORTUNE_SETTINGS));
            addEntry(new DoubleEntry(Config.FORTUNE_INITIAL_LIMIT_KEY,
                    () -> (double) Config.FORTUNE_INITIAL_LIMIT, v -> Config.FORTUNE_INITIAL_LIMIT = v.floatValue(),
                    Config.FORTUNE_INITIAL_LIMIT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING));
            addEntry(new DoubleEntry(Config.FORTUNE_DIMINISHING_RETURNS_KEY,
                    () -> (double) Config.FORTUNE_DIMINISHING_RETURNS, v -> Config.FORTUNE_DIMINISHING_RETURNS = v.floatValue(),
                    Config.FORTUNE_DIMINISHING_RETURNS_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING));
            addEntry(new DoubleEntry(Config.FORTUNE_MINIMUM_INCREMENT_KEY,
                    () -> (double) Config.FORTUNE_MINIMUM_INCREMENT, v -> Config.FORTUNE_MINIMUM_INCREMENT = v.floatValue(),
                    Config.FORTUNE_MINIMUM_INCREMENT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING));
            addEntry(new DoubleEntry(Config.LOOTING_INITIAL_LIMIT_KEY,
                    () -> (double) Config.LOOTING_INITIAL_LIMIT, v -> Config.LOOTING_INITIAL_LIMIT = v.floatValue(),
                    Config.LOOTING_INITIAL_LIMIT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING));
            addEntry(new DoubleEntry(Config.LOOTING_DIMINISHING_RETURNS_KEY,
                    () -> (double) Config.LOOTING_DIMINISHING_RETURNS, v -> Config.LOOTING_DIMINISHING_RETURNS = v.floatValue(),
                    Config.LOOTING_DIMINISHING_RETURNS_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING));
            addEntry(new DoubleEntry(Config.LOOTING_MINIMUM_INCREMENT_KEY,
                    () -> (double) Config.LOOTING_MINIMUM_INCREMENT, v -> Config.LOOTING_MINIMUM_INCREMENT = v.floatValue(),
                    Config.LOOTING_MINIMUM_INCREMENT_DEFAULT, Config.LOOT_LIMIT_FLOOR, Config.LOOT_LIMIT_CEILING));

            // --- Unbreaking Settings ---
            addEntry(new CategoryEntry(PREFIX + UNBREAKING_SETTINGS));
            addEntry(new IntEntry(Config.UNBREAKING_STRENGTH_KEY,
                    () -> Config.UNBREAKING_STRENGTH, v -> Config.UNBREAKING_STRENGTH = v,
                    Config.UNBREAKING_STRENGTH_DEFAULT, Config.UNBREAKING_STRENGTH_FLOOR, Config.UNBREAKING_STRENGTH_CEILING));
            addEntry(new DoubleEntry(Config.UNBREAKING_ARMOR_PENALTY_FACTOR_KEY,
                    () -> Config.UNBREAKING_ARMOR_PENALTY_FACTOR, v -> Config.UNBREAKING_ARMOR_PENALTY_FACTOR = v,
                    Config.UNBREAKING_ARMOR_PENALTY_FACTOR_DEFAULT, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_FLOOR, Config.UNBREAKING_ARMOR_PENALTY_FACTOR_CEILING));

            // --- XP Settings ---
            addEntry(new CategoryEntry(PREFIX + XP_SETTINGS));
            addEntry(new IntEntry(Config.XP_GROWTH_FACTOR_KEY,
                    () -> Config.XP_GROWTH_FACTOR, v -> Config.XP_GROWTH_FACTOR = v,
                    Config.XP_GROWTH_FACTOR_DEFAULT, Config.XP_GROWTH_FACTOR_FLOOR, Config.XP_GROWTH_FACTOR_CEILING));
            addEntry(new IntEntry(Config.XP_LEVEL_BRACKET_SIZE_KEY,
                    () -> Config.XP_LEVEL_BRACKET_SIZE, v -> Config.XP_LEVEL_BRACKET_SIZE = v,
                    Config.XP_LEVEL_BRACKET_SIZE_DEFAULT, Config.XP_LEVEL_BRACKET_SIZE_FLOOR, Config.XP_LEVEL_BRACKET_SIZE_CEILING));
            addEntry(new IntEntry(Config.XP_GROWTH_Y_OFFSET_KEY,
                    () -> Config.XP_GROWTH_Y_OFFSET, v -> Config.XP_GROWTH_Y_OFFSET = v,
                    Config.XP_GROWTH_Y_OFFSET_DEFAULT, Config.XP_GROWTH_Y_OFFSET_FLOOR, Config.XP_GROWTH_Y_OFFSET_CEILING));
            addEntry(new IntEntry(Config.XP_MAX_LEVEL_KEY,
                    () -> Config.XP_MAX_LEVEL, v -> Config.XP_MAX_LEVEL = v,
                    Config.XP_MAX_LEVEL_DEFAULT, Config.XP_MAX_LEVEL_FLOOR, Config.XP_MAX_LEVEL_CEILING));

            // --- Loot Settings ---
            addEntry(new CategoryEntry(PREFIX + LOOT_SETTINGS));
            addEntry(new DoubleEntry(Config.EPIC_LOOT_CHANCE_KEY,
                    () -> (double) Config.EPIC_LOOT_CHANCE, v -> Config.EPIC_LOOT_CHANCE = v.floatValue(),
                    Config.EPIC_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING, 0.001D));
            addEntry(new DoubleEntry(Config.RARE_LOOT_CHANCE_KEY,
                    () -> (double) Config.RARE_LOOT_CHANCE, v -> Config.RARE_LOOT_CHANCE = v.floatValue(),
                    Config.RARE_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING, 0.001D));
            addEntry(new DoubleEntry(Config.UNCOMMON_LOOT_CHANCE_KEY,
                    () -> (double) Config.UNCOMMON_LOOT_CHANCE, v -> Config.UNCOMMON_LOOT_CHANCE = v.floatValue(),
                    Config.UNCOMMON_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING, 0.001D));
            addEntry(new DoubleEntry(Config.COMMON_LOOT_CHANCE_KEY,
                    () -> (double) Config.COMMON_LOOT_CHANCE, v -> Config.COMMON_LOOT_CHANCE = v.floatValue(),
                    Config.COMMON_LOOT_CHANCE_DEFAULT, Config.LOOT_CHANCE_FLOOR, Config.LOOT_CHANCE_CEILING, 0.001D));

            // --- Accessibility Settings ---
            addEntry(new CategoryEntry(PREFIX + ACCESSIBILITY_SETTINGS));
            addEntry(new BooleanEntry(Config.USE_PLAIN_BACKGROUND_KEY,
                    () -> Config.USE_PLAIN_BACKGROUND, v -> Config.USE_PLAIN_BACKGROUND = v,
                    Config.USE_PLAIN_BACKGROUND_DEFAULT));
            addEntry(new BooleanEntry(Config.OBFUSCATE_NEW_ENCHANTMENTS_KEY,
                    () -> Config.OBFUSCATE_NEW_ENCHANTMENTS, v -> Config.OBFUSCATE_NEW_ENCHANTMENTS = v,
                    Config.OBFUSCATE_NEW_ENCHANTMENTS_DEFAULT));
            addEntry(new BooleanEntry(Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_KEY,
                    () -> Config.OVERRIDE_ENCHANTMENT_NAME_COLORING, v -> Config.OVERRIDE_ENCHANTMENT_NAME_COLORING = v,
                    Config.OVERRIDE_ENCHANTMENT_NAME_COLORING_DEFAULT));
            addEntry(new IntEntry(Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_KEY,
                    () -> Config.OVERRIDE_ENCHANTMENT_NAME_COLOR, v -> Config.OVERRIDE_ENCHANTMENT_NAME_COLOR = v,
                    Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_DEFAULT, Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_FLOOR, Config.OVERRIDE_ENCHANTMENT_NAME_COLOR_CEILING));
            addEntry(new BooleanEntry(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_KEY,
                    () -> Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING, v -> Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING = v,
                    Config.OVERRIDE_ENCHANTMENT_LEVEL_COLORING_DEFAULT));
            addEntry(new IntEntry(Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_KEY,
                    () -> Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR, v -> Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR = v,
                    Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_DEFAULT, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_FLOOR, Config.OVERRIDE_ENCHANTMENT_LEVEL_COLOR_CEILING));
            addEntry(new BooleanEntry(Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLORING_KEY,
                    () -> Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLORING, v -> Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLORING = v,
                    Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLORING_DEFAULT));
            addEntry(new IntEntry(Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLOR_KEY,
                    () -> Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLOR, v -> Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLOR = v,
                    Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLOR_DEFAULT, Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLOR_FLOOR, Config.OVERRIDE_ENCHANTMENT_TOOLTIP_COLOR_CEILING));
            addEntry(new IntEntry(Config.ENCHANTMENT_DESCRIPTION_COLOR_KEY,
                    () -> Config.ENCHANTMENT_DESCRIPTION_COLOR, v -> Config.ENCHANTMENT_DESCRIPTION_COLOR = v,
                    Config.ENCHANTMENT_DESCRIPTION_COLOR_DEFAULT, Config.ENCHANTMENT_DESCRIPTION_COLOR_FLOOR, Config.ENCHANTMENT_DESCRIPTION_COLOR_CEILING));
        }

        @Override
        public int getRowWidth() {
            return 340;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 15;
        }

        /**
         * Base entry class for the configuration list.
         */
        public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {}

        /**
         * A non-interactive header entry representing a configuration category.
         */
        public class CategoryEntry extends Entry {
            private final Component label;
            private final int width;

            public CategoryEntry(String labelKey) {
                this.label = Component.translatable(labelKey).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);;
                this.width = minecraft.font.width(this.label);
            }

            @Override
            public void render(
                    GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovering,
                    float partialTick
            ) {
                int textY = top + (height - minecraft.font.lineHeight) / 2;
                assert minecraft.screen != null;
                guiGraphics.drawString(minecraft.font, this.label, minecraft.screen.width / 2 - this.width / 2, textY, 0xFFFFFF, false);
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() { return Collections.emptyList(); }
            @Override
            public @NotNull List<? extends NarratableEntry> narratables() { return ImmutableList.of(new NarratableEntry() {
                @Override
                public NarratableEntry.@NotNull NarrationPriority narrationPriority() { return NarratableEntry.NarrationPriority.HOVERED; }
                @Override
                public void updateNarration(@NotNull NarrationElementOutput output) { output.add(NarratedElementType.TITLE, label); }
            });}
        }

        /**
         * A generic interactive entry representing a single configuration option.
         *
         * @param <T> The type of the configuration value.
         * @param <W> The type of the widget used to edit the value.
         */
        public abstract class OptionEntry<T, W extends net.minecraft.client.gui.components.AbstractWidget> extends Entry {
            protected final String key;
            protected final Supplier<T> getter;
            protected final Consumer<T> setter;
            protected final T defaultValue;
            protected final Button resetButton;
            protected final W widget;
            protected final Component labelComponent;

            public OptionEntry(String key, Supplier<T> getter, Consumer<T> setter, T defaultValue) {
                this.key = key;
                this.getter = getter;
                this.setter = setter;
                this.defaultValue = defaultValue;
                this.labelComponent = Component.translatable(PREFIX + key);

                this.resetButton = Button.builder(Component.translatable("controls.reset"), b -> reset())
                        .bounds(0, 0, 50, 20)
                        .build();
                this.widget = createWidget();
                updateResetButton();
            }

            protected abstract W createWidget();
            protected abstract void updateWidgetValue();

            public void reset() {
                setter.accept(defaultValue);
                updateWidgetValue();
                updateResetButton();
                ConfigScreen.this.updateMasterResetButton();
            }

            public boolean isDefault() {
                return Objects.equals(getter.get(), defaultValue);
            }

            protected void updateResetButton() {
                this.resetButton.active = !isDefault();
            }

            @Override
            public void render(
                    @NotNull GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovering,
                    float partialTick
            ) {
                int y = top + (height - 20) / 2;

                // 1. Reset Button (Far Right)
                int resetX = ConfigList.this.getScrollbarPosition() - 50 - 10;
                this.resetButton.setPosition(resetX, y);
                this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);

                // 2. Widget (Left of Reset Button)
                int widgetWidth = 75;
                int widgetX = resetX - 5 - widgetWidth;
                this.widget.setX(widgetX);
                this.widget.setY(y);
                this.widget.setWidth(widgetWidth);
                this.widget.render(guiGraphics, mouseX, mouseY, partialTick);

                // 3. Label (Left Aligned)
                int textY = top + (height - minecraft.font.lineHeight) / 2;
                guiGraphics.drawString(minecraft.font, this.labelComponent, left, textY, 0xFFFFFF);
            }

            @Override
            public @NotNull List<? extends GuiEventListener> children() { return ImmutableList.of(widget, resetButton); }
            @Override
            public @NotNull List<? extends NarratableEntry> narratables() { return ImmutableList.of(widget, resetButton); }
        }

        /**
         * An option entry for integer values, using an {@link EditBox}.
         */
        public class IntEntry extends OptionEntry<Integer, EditBox> {
            private final int min, max;
            public IntEntry(String key, Supplier<Integer> getter, Consumer<Integer> setter, int def, int min, int max) {
                super(key, getter, setter, def);
                this.min = min; this.max = max;
                this.widget.setValue(String.valueOf(getter.get()));
            }
            @Override
            protected EditBox createWidget() {
                EditBox box = new EditBox(minecraft.font, 0, 0, 75, 20, Component.empty());
                box.setFilter(s -> s.matches("-?\\d*"));
                box.setResponder(s -> {
                    try {
                        if (s.isEmpty() || s.equals("-")) return;
                        int val = Integer.parseInt(s);
                        if (val >= min && val <= max) {
                            setter.accept(val);
                            box.setTextColor(0xFFFFFF);
                        } else {
                            box.setTextColor(0xFF0000);
                        }
                        updateResetButton();
                        ConfigScreen.this.updateMasterResetButton();
                    } catch (NumberFormatException ignored) {
                        box.setTextColor(0xFF0000);
                    }
                });
                return box;
            }
            @Override
            protected void updateWidgetValue() {
                this.widget.setValue(String.valueOf(getter.get()));
                this.widget.setTextColor(0xFFFFFF);
            }
        }

        /**
         * An option entry for double values, using an {@link EditBox}.
         */
        public class DoubleEntry extends OptionEntry<Double, EditBox> {
            private final double min, max, precision;
            public DoubleEntry(String key, Supplier<Double> getter, Consumer<Double> setter, double def, double min, double max) {
                super(key, getter, setter, def);
                this.min = min; this.max = max; this.precision = 0.0001;
                this.widget.setValue(String.format("%.2f", getter.get()));
            }
            public DoubleEntry(String key, Supplier<Double> getter, Consumer<Double> setter, double def, double min, double max, double precision) {
                super(key, getter, setter, def);
                this.min = min; this.max = max; this.precision = precision;
                this.widget.setValue(String.format("%.2f", getter.get()));
            }
            @Override
            protected EditBox createWidget() {
                EditBox box = new EditBox(minecraft.font, 0, 0, 75, 20, Component.empty());
                box.setFilter(s -> s.matches("-?\\d*\\.?\\d*"));
                box.setResponder(s -> {
                    try {
                        if (s.isEmpty() || s.equals("-") || s.equals(".")) return;
                        double val = Double.parseDouble(s);
                        if (val >= min && val <= max) {
                            setter.accept(val);
                            box.setTextColor(0xFFFFFF);
                        } else {
                            box.setTextColor(0xFF0000);
                        }
                        updateResetButton();
                        ConfigScreen.this.updateMasterResetButton();
                    } catch (NumberFormatException ignored) {
                        box.setTextColor(0xFF0000);
                    }
                });
                return box;
            }
            @Override
            protected void updateWidgetValue() {
                this.widget.setValue(String.format("%.2f", getter.get()));
                this.widget.setTextColor(0xFFFFFF);
            }
            @Override
            public boolean isDefault() {
                return Math.abs(getter.get() - defaultValue) < this.precision;
            }
        }

        /**
         * An option entry for boolean values, using a {@link CycleButton}.
         */
        public class BooleanEntry extends OptionEntry<Boolean, CycleButton<Boolean>> {
            public BooleanEntry(String key, Supplier<Boolean> getter, Consumer<Boolean> setter, boolean def) {
                super(key, getter, setter, def);
            }
            @Override
            protected CycleButton<Boolean> createWidget() {
                return CycleButton.onOffBuilder(getter.get())
                        .displayOnlyValue()
                        .create(0, 0, 75, 20, Component.empty(), (b, val) -> {
                            setter.accept(val);
                            updateResetButton();
                            ConfigScreen.this.updateMasterResetButton();
                        });
            }
            @Override
            protected void updateWidgetValue() {
                this.widget.setValue(getter.get());
            }
        }
    }
}