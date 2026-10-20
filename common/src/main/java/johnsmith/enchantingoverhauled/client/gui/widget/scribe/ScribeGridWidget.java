package johnsmith.enchantingoverhauled.client.gui.widget.scribe;

import com.mojang.blaze3d.systems.RenderSystem;
import johnsmith.enchantingoverhauled.Config;
import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.menu.scribe.model.ScribeGridData;
import johnsmith.enchantingoverhauled.menu.scribe.controller.ScribeInput;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the dynamic Minesweeper grid and handles mouse interaction mapping.
 */
public class ScribeGridWidget {

    private static final ResourceLocation[] RUNE_TEXTURES = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_1.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_2.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_3.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_4.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_5.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_6.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_7.png"),
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_8.png")
    };

    private static final ResourceLocation RUNE_DEATH_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_death.png");
    private static final ResourceLocation RUNE_SEAL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/game/rune_seal.png");

    private static final ResourceLocation BOOK_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/book_background.png");
    private static final ResourceLocation TOME_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/scribe/tome_background.png");

    private static final int GRID_START_X = 11;
    private static final int GRID_START_Y = 21;
    private static final int CELL_SIZE = 7;

    private final ScribeGridData gridData;
    private int leftPos, topPos;

    public ScribeGridWidget(ScribeGridData gridData) {
        this.gridData = gridData;
    }

    public void updatePosition(int leftPos, int topPos) {
        this.leftPos = leftPos;
        this.topPos = topPos;
    }

    private void drawCellBorder(GuiGraphics guiGraphics, int screenX, int screenY, int color) {
        // Skip drawing if the color is fully transparent
        if ((color & 0xFF000000) == 0) return;

        // Draw 1px ink border strictly on the inside edges of the cell
        guiGraphics.fill(screenX, screenY, screenX + CELL_SIZE, screenY + 1, color); // Top
        guiGraphics.fill(screenX, screenY, screenX + 1, screenY + CELL_SIZE, color); // Left
        guiGraphics.fill(screenX, screenY + CELL_SIZE - 1, screenX + CELL_SIZE, screenY + CELL_SIZE, color); // Bottom
        guiGraphics.fill(screenX + CELL_SIZE - 1, screenY, screenX + CELL_SIZE, screenY + CELL_SIZE, color); // Right
    }

    /**
     * Translates a relative mouse click into a ScribeInput payload.
     * Validates grid logic locally to prevent dispatching useless interactions.
     * Returns -1 if the click was out of bounds or invalid.
     */
    public int mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) (mouseX - this.leftPos);
        int relY = (int) (mouseY - this.topPos);

        int gridX = GRID_START_X;
        int gridY = GRID_START_Y;
        int gridWidth = ScribeGridData.MAX_COLS * CELL_SIZE;
        int gridHeight = ScribeGridData.MAX_ROWS * CELL_SIZE;

        if (relX >= gridX && relX < gridX + gridWidth && relY >= gridY && relY < gridY + gridHeight) {
            int cellX = (relX - gridX) / CELL_SIZE;
            int cellY = (relY - gridY) / CELL_SIZE;

            boolean isRevealed = this.gridData.isRevealed(cellX, cellY);
            boolean isFlagged = this.gridData.isFlagged(cellX, cellY);

            // Right Click / Flag Interaction
            if (button == 1) {
                if (isRevealed) return -1; // Cannot flag revealed cells
                return new ScribeInput(cellX, cellY, ScribeInput.ACTION_FLAG).encode();
            }
            // Left Click / Reveal & Chord Interaction
            else if (button == 0) {
                if (isFlagged) return -1; // Cannot directly reveal flagged cells

                // If it's already revealed, we evaluate it as a Chord attempt
                if (isRevealed) {
                    int adj = this.gridData.getAdjacentMines(cellX, cellY);
                    if (adj == 0) return -1; // Blank cells automatically cleared their neighbors

                    int flagCount = 0;
                    boolean hasHidden = false;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = cellX + dx;
                            int ny = cellY + dy;

                            if (nx >= 0 && nx < ScribeGridData.MAX_COLS && ny >= 0 && ny < ScribeGridData.MAX_ROWS) {
                                if (this.gridData.isFlagged(nx, ny)) {
                                    flagCount++;
                                } else if (!this.gridData.isRevealed(nx, ny)) {
                                    hasHidden = true;
                                }
                            }
                        }
                    }

                    // Prevent firing payload if chord conditions aren't met or if it's already fully solved
                    if (flagCount < adj || !hasHidden) {
                        return -1;
                    }
                }

                return new ScribeInput(cellX, cellY, ScribeInput.ACTION_REVEAL).encode();
            }
        }

        return -1; // Out of bounds
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, Font font) {
        RenderSystem.enableBlend();
        int renderStartX = this.leftPos + GRID_START_X;
        int renderStartY = this.topPos + GRID_START_Y;
        ResourceLocation innerBg = this.gridData.isTome() ? TOME_BACKGROUND : BOOK_BACKGROUND;

        int unrevealedColor = Config.UNREVEALED_GRID_COLOR.get();
        int revealedColor = Config.REVEALED_GRID_COLOR.get();

        for (int y = 0; y < ScribeGridData.MAX_ROWS; y++) {
            for (int x = 0; x < ScribeGridData.MAX_COLS; x++) {
                boolean hasMine = this.gridData.hasMine(x, y);
                boolean isRevealed = this.gridData.isRevealed(x, y);
                boolean isFlagged = this.gridData.isFlagged(x, y);
                int adj = this.gridData.getAdjacentMines(x, y);

                int screenX = renderStartX + (x * CELL_SIZE);
                int screenY = renderStartY + (y * CELL_SIZE);

                if (!isRevealed) {
                    // Mask the text underneath by cleanly redrawing the pristine background snippet
                    int u = 7 + (x * CELL_SIZE);
                    int v = 4 + (y * CELL_SIZE);
                    guiGraphics.blit(innerBg, screenX, screenY, u, v, CELL_SIZE, CELL_SIZE, 165, 85);

                    this.drawCellBorder(guiGraphics, screenX, screenY, unrevealedColor);

                    if (isFlagged) {
                        guiGraphics.blit(RUNE_SEAL_TEXTURE, screenX, screenY, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                } else {
                    this.drawCellBorder(guiGraphics, screenX, screenY, revealedColor);

                    if (hasMine) {
                        guiGraphics.blit(RUNE_DEATH_TEXTURE, screenX, screenY, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    } else if (adj > 0 && adj <= 8) {
                        guiGraphics.blit(RUNE_TEXTURES[adj - 1], screenX, screenY, 0, 0, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }

                // Hover Highlight for clickable cells
                if (!isRevealed && mouseX >= screenX && mouseX < screenX + CELL_SIZE && mouseY >= screenY && mouseY < screenY + CELL_SIZE) {
                    guiGraphics.fill(screenX, screenY, screenX + CELL_SIZE, screenY + CELL_SIZE, 0x40FFFFFF);
                }
            }
        }
        RenderSystem.disableBlend();
    }
}