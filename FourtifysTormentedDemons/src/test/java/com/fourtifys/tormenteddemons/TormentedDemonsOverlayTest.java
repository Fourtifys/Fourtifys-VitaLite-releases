package com.fourtifys.tormenteddemons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

class TormentedDemonsOverlayTest
{
    @Test
    void aLongStatusWrapsWithinTheOverlay()
    {
        Graphics2D graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        FontMetrics metrics = graphics.getFontMetrics(new Font("Serif", Font.PLAIN, 13));
        String status = "No Guthixian temple teleports in the bank. Restock the bank, then disable and enable the plugin";
        List<String> lines = TormentedDemonsOverlay.wrap(status, metrics, 180);
        assertTrue(lines.size() > 1);
        assertEquals(status, String.join(" ", lines));
        lines.forEach(line -> assertTrue(metrics.stringWidth(line) <= 180, line));
        assertEquals(List.of("Opening the bank"), TormentedDemonsOverlay.wrap("Opening the bank", metrics, 180));
    }

    @Test
    void lootValueIsShortened()
    {
        assertEquals("9999", TormentedDemonsOverlay.shortValue(9_999));
        assertEquals("12.5K", TormentedDemonsOverlay.shortValue(12_500));
        assertEquals("3.2M", TormentedDemonsOverlay.shortValue(3_249_000));
    }
}
