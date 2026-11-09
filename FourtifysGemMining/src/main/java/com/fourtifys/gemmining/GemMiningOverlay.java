package com.fourtifys.gemmining;

import com.tonic.ui.VitaOverlay;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.overlay.OverlayPosition;

public class GemMiningOverlay extends VitaOverlay
{
    private static final int WIDTH = 190;
    private static final int SIZE = 13;
    private static final Color FOURTIFY_YELLOW = new Color(0xF5DD42);
    private static final Color DETAIL = new Color(0xB9B4A8);

    public GemMiningOverlay()
    {
        super();
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setWidth(WIDTH);
    }

    public void update(String runtime, String mine, int gems, String status)
    {
        setHeight(SIZE);
        clear();
        newLineEx("Fourtifys Gem Mining", "", SIZE, FOURTIFY_YELLOW, DETAIL);
        newLineEx("Runtime: ", runtime, SIZE, DETAIL, Color.WHITE);
        newLineEx("Mine: ", mine, SIZE, DETAIL, Color.WHITE);
        newLineEx("Gems: ", String.valueOf(gems), SIZE, DETAIL, Color.WHITE);
        Graphics2D graphics = getImage().createGraphics();
        FontMetrics metrics = graphics.getFontMetrics(new Font("Serif", Font.PLAIN, SIZE));
        graphics.dispose();
        for (String line : wrap(status, metrics, WIDTH - 10))
        {
            newLine(line, SIZE, Color.WHITE);
        }
    }

    static List<String> wrap(String text, FontMetrics metrics, int width)
    {
        List<String> lines = new ArrayList<>();
        String line = "";
        for (String word : text.trim().split("\\s+"))
        {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && metrics.stringWidth(next) > width)
            {
                lines.add(line);
                next = word;
            }
            line = next;
        }
        lines.add(line);
        return lines;
    }
}
