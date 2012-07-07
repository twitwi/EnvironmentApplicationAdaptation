/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import javax.swing.JPanel;

public class ImageProcessing {

    public static Image TransformColorToTransparency(BufferedImage image, final Color color) {
        // Primitive test, just an example
        ImageFilter filter = new RGBImageFilter() {
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }

            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    public static BufferedImage ImageToBufferedImage(Image image, int width, int height) {
        BufferedImage dest = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return dest;
    }

    public static BufferedImage ScaleColorBufferedImage(BufferedImage image, float[] scaleFactor, float[] offset) {
        RescaleOp rescaleOp = new RescaleOp(scaleFactor, offset, null);
        BufferedImage imgret = rescaleOp.createCompatibleDestImage(image, image.getColorModel());
        imgret = rescaleOp.filter(image, null);
        return imgret;
    }

    public static void CaptureFrame(JPanel panel, String filename) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics myg = image.getGraphics();
        panel.paint(myg);
        myg.dispose();
        String format = "jpg";
        try {
            ImageIO.write(image, format, new File(filename));
        } catch (IOException ex) {
        }


    }
}
