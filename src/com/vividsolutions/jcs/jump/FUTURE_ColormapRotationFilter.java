package com.vividsolutions.jcs.jump;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

/**
 * Maps one range of hue values to another. Inspired by the GIMP's Colormap
 * Rotation plug-in.
 */
public class FUTURE_ColormapRotationFilter extends RGBImageFilter {

    private float m;

    private float b;

    private float sourceStart;

    private float sourceEnd;

    public static Image filter(Image i, float sourceStart, float sourceEnd,
            float destinationStart, float destinationEnd) {
        FUTURE_ColormapRotationFilter filter = new FUTURE_ColormapRotationFilter(
                sourceStart, sourceEnd, destinationStart, destinationEnd);
        ImageProducer prod = new FilteredImageSource(i.getSource(), filter);
        Image grayImage = Toolkit.getDefaultToolkit().createImage(prod);
        return grayImage;
    }

    public FUTURE_ColormapRotationFilter(float sourceStart, float sourceEnd,
            float destinationStart, float destinationEnd) {
        //y = mx + b [Jon Aquino 2004-02-18]
        this.sourceStart = sourceStart;
        this.sourceEnd = sourceEnd;
        m = (destinationEnd - destinationStart) / (sourceEnd - sourceStart);
        b = destinationStart - (m * sourceStart);
        canFilterIndexColorModel = true;
    }

    private float[] hsbValues = new float[3];

    public int filterRGB(int x, int y, int rgb) {
        Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF,
                (rgb >> 0) & 0xFF, hsbValues);
        return (rgb & 0xff000000) | (0x00ffffff & Color.HSBtoRGB(adjustIn(rotateHue(adjustOut(hsbValues[0]))),
                hsbValues[1], hsbValues[2]));
    }

    private float adjustIn(float hue) {
        return (float) (hue - Math.floor(hue));
    }

    private float adjustOut(float hue) {
        if (between(hue, sourceStart, sourceEnd)) { return hue; }
        if (between(hue + 1, sourceStart, sourceEnd)) { return hue + 1; }
        if (between(hue - 1, sourceStart, sourceEnd)) { return hue - 1; }
        return hue;
    }

    private float rotateHue(float hue) {
        if (!between(hue, sourceStart, sourceEnd) && !between(hue+1, sourceStart, sourceEnd) && !between(hue-1, sourceStart, sourceEnd)) { return hue; }
        return (m * hue) + b;
    }

    private boolean between(float hue, float start, float end) {
        return Math.max(start, end) > hue && hue > Math.min(start, end);
    }
}
