package com.fourtifys.gemmining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

class GemMiningOverlayTest
{
    @Test
    void aLongStatusWrapsWithinTheOverlay()
    {
        Graphics2D graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        FontMetrics metrics = graphics.getFontMetrics(new Font("Serif", Font.PLAIN, 13));
        String status = "Some gems will not deposit. Unlock their inventory slots, then disable and enable the plugin";
        List<String> lines = GemMiningOverlay.wrap(status, metrics, 180);
        assertTrue(lines.size() > 1);
        assertEquals(status, String.join(" ", lines));
        lines.forEach(line -> assertTrue(metrics.stringWidth(line) <= 180, line));
        assertEquals(List.of("Mining gem rocks"), GemMiningOverlay.wrap("Mining gem rocks", metrics, 180));
    }
}
