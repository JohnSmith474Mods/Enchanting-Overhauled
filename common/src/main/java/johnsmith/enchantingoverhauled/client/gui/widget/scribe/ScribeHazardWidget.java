package johnsmith.enchantingoverhauled.client.gui.widget.scribe;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Encapsulates the generation, caching, and rendering of the justified hazard
 * standard galactic alphabet text shown during active gameplay.
 */
public class ScribeHazardWidget {

    private static final String[] HAZARDS = {
            "nothing cannot exist thus something must be",
            "the meaning of meaning is recursive in nature",
            "this sentence is false therefore it is true",
            "you just lost the game",
            "do not think about chickens",
            "nothing is not the absence of something as a shadow is the absence of light",
            "why is there something rather than nothing",
            "contradictions cannot exist for truth already does",
            "infinity is that which has no end",
            "that which has no beginning is eternal",
            "to suppress is to remember",
            "to forget is to neglect",
            "logic requires the logic it seeks to prove",
            "reality is in the eye of the beholder",
            "perception shapes reality and thus truth"
    };

    private List<List<String>> cachedHazardLines = null;
    private int currentSeed = -1;

    public void render(GuiGraphics guiGraphics, Font font, int leftPos, int topPos, int seed) {
        if (cachedHazardLines == null || currentSeed != seed) {
            generateHazardText(font, seed);
        }

        int textColor = 0x303E2613; // Faded brownish ink
        int lineHeight = 10;
        int maxLines = 7;
        int maxWidth = 71;

        Style sgaStyle = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));

        // Draw Left Page
        int leftX = leftPos + 14;
        int startY = topPos + 24;
        for (int i = 0; i < maxLines && i < this.cachedHazardLines.size(); i++) {
            drawJustifiedLine(guiGraphics, font, this.cachedHazardLines.get(i), leftX, startY + (i * lineHeight + 2), maxWidth, textColor, sgaStyle);
        }

        // Draw Right Page (continuing)
        int rightX = leftPos + 91;
        for (int i = maxLines; i < maxLines * 2 && i < this.cachedHazardLines.size(); i++) {
            drawJustifiedLine(guiGraphics, font, this.cachedHazardLines.get(i), rightX, startY + ((i - maxLines) * lineHeight + 2), maxWidth, textColor, sgaStyle);
        }
    }

    private void generateHazardText(Font font, int seed) {
        this.currentSeed = seed;
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 30; i++) {
            sb.append(HAZARDS[random.nextInt(HAZARDS.length)]).append(" ");
        }

        String fullText = sb.toString();
        int startIndex = random.nextInt(60);
        while (startIndex < fullText.length() && fullText.charAt(startIndex) == ' ') {
            startIndex++;
        }
        fullText = fullText.substring(startIndex);

        Style sgaStyle = Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"));
        int maxWidth = 71;
        this.cachedHazardLines = new ArrayList<>();
        List<String> currentLine = new ArrayList<>();
        int currentLineWidth = 0;
        int spaceWidth = font.width(Component.literal(" ").withStyle(sgaStyle));

        String[] words = fullText.split(" ");
        for (String word : words) {
            if (word.isEmpty()) continue;
            int wordWidth = font.width(Component.literal(word).withStyle(sgaStyle));

            if (currentLine.isEmpty()) {
                currentLine.add(word);
                currentLineWidth = wordWidth;
            } else {
                if (currentLineWidth + spaceWidth + wordWidth <= maxWidth) {
                    currentLine.add(word);
                    currentLineWidth += spaceWidth + wordWidth;
                } else {
                    this.cachedHazardLines.add(new ArrayList<>(currentLine));
                    currentLine.clear();
                    currentLine.add(word);
                    currentLineWidth = wordWidth;
                }
            }
        }
        if (!currentLine.isEmpty()) {
            this.cachedHazardLines.add(currentLine);
        }
    }

    private void drawJustifiedLine(GuiGraphics guiGraphics, Font font, List<String> words, int x, int y, int maxWidth, int color, Style style) {
        if (words.isEmpty()) return;

        if (words.size() == 1) {
            guiGraphics.drawString(font, Component.literal(words.get(0)).withStyle(style), x, y, color, false);
            return;
        }

        int totalWordWidth = 0;
        for (String word : words) {
            totalWordWidth += font.width(Component.literal(word).withStyle(style));
        }

        int totalSpace = maxWidth - totalWordWidth;
        float spaceWidth = (float) totalSpace / (words.size() - 1);
        float currentX = x;

        for (int i = 0; i < words.size(); i++) {
            Component wordComp = Component.literal(words.get(i)).withStyle(style);
            guiGraphics.drawString(font, wordComp, Math.round(currentX), y, color, false);
            currentX += font.width(wordComp) + spaceWidth;
        }
    }
}