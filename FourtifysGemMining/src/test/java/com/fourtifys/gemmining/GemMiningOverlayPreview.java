package com.fourtifys.gemmining;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GemMiningOverlayPreview
{
    public static void main(String[] args) throws Exception
    {
        BufferedImage image = new BufferedImage(420, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(74, 66, 52));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        GemMiningOverlay ordinary = new GemMiningOverlay();
        ordinary.update("00:42:17", "Underground", 63, "Mining gem rocks");
        GemMiningOverlay waiting = new GemMiningOverlay();
        waiting.update("01:05:09", "Surface", 128, "Some gems will not deposit. Unlock their inventory slots, then disable and enable the plugin");
        graphics.translate(10, 10);
        ordinary.render(graphics);
        graphics.translate(210, 0);
        waiting.render(graphics);
        graphics.dispose();
        File output = new File(args[0]);
        output.getAbsoluteFile().getParentFile().mkdirs();
        ImageIO.write(image, "png", output);
    }
}
