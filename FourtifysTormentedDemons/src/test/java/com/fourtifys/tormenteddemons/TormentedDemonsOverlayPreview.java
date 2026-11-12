package com.fourtifys.tormenteddemons;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TormentedDemonsOverlayPreview
{
    public static void main(String[] args) throws Exception
    {
        BufferedImage image = new BufferedImage(420, 190, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(74, 66, 52));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        TormentedDemonsOverlay ordinary = new TormentedDemonsOverlay();
        ordinary.update("00:42:17", 18, 25, 3_412_600, 1, "Fighting with Ranged. The demon protects from Melee");
        TormentedDemonsOverlay waiting = new TormentedDemonsOverlay();
        waiting.update("01:05:09", 31, 28, 6_950_300, 2, "No Guthixian temple teleports in the bank. Restock the bank, then disable and enable the plugin");
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
