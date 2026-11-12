package com.fourtifys.tormenteddemons;

import com.tonic.ui.VitaOverlay;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TormentedDemonsOverlay extends VitaOverlay
{
    private static final int WIDTH = 190;
    private static final int SIZE = 13;
    private static final Color FOURTIFY_YELLOW = new Color(0xF5DD42);
    private static final Color DETAIL = new Color(0xB9B4A8);

    public TormentedDemonsOverlay()
    {
        super();
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setWidth(WIDTH);
    }

    public void update(String runtime, int kills, int killsPerHour, long lootValue, int uniques, String status)
    {
        setHeight(SIZE);
        clear();
        Graphics2D graphics = getImage().createGraphics();
        FontMetrics title = graphics.getFontMetrics(new Font("Serif", Font.BOLD, SIZE));
        FontMetrics metrics = graphics.getFontMetrics(new Font("Serif", Font.PLAIN, SIZE));
        graphics.dispose();
        for (String line : wrap("Fourtifys Tormented Demons", title, WIDTH - 10))
        {
            newLineEx(line, "", SIZE, FOURTIFY_YELLOW, DETAIL);
        }
        newLineEx("Runtime: ", runtime, SIZE, DETAIL, Color.WHITE);
        newLineEx("Kills: ", kills + " (" + killsPerHour + "/h)", SIZE, DETAIL, Color.WHITE);
        newLineEx("Loot: ", shortValue(lootValue) + " gp", SIZE, DETAIL, Color.WHITE);
        newLineEx("Uniques: ", String.valueOf(uniques), SIZE, DETAIL, Color.WHITE);
        for (String line : wrap(status, metrics, WIDTH - 10))
        {
            newLine(line, SIZE, Color.WHITE);
        }
    }

    static String shortValue(long value)
    {
        if (value >= 1_000_000)
        {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        return value >= 10_000 ? String.format("%.1fK", value / 1_000.0) : String.valueOf(value);
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
