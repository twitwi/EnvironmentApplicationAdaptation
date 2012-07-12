/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.eaa.additionalmodules;

import fr.prima.gsp.framework.ModuleParameter;
import fr.prima.gsp.framework.spi.AbstractModuleEnablable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 *
 * @author twilight
 */
public class Clicklet extends AbstractModuleEnablable {

    @ModuleParameter
    public float x = -1;
    @ModuleParameter
    public float y = -1;
    @ModuleParameter
    public float halfWidth = 1;
    @ModuleParameter
    public float halfHeight = 1;
    @ModuleParameter
    public float radius = 4;
    @ModuleParameter
    public float threshold = 50;
    @ModuleParameter
    public int acceptUpTo = 2;
    @ModuleParameter
    public boolean viewEnabled = true;
    //
    //
    private static final float HALFSQRT2 = (float) (Math.sqrt(2) / 2f);
    //

    public void input(BufferedImage im) {
        if (!isEnabled()) {
            return;
        }
        float vcenter = value(im, 0f, 0f);
        if (!on(vcenter)) {
            output(false);
            debug(false, null, null);
            return;
        }
        float diagr = HALFSQRT2 * radius;
        float radiusx = radius * halfWidth;
        float radiusy = radius * halfHeight;
        // could parameterize the size of this array
        float[] varound = new float[]{
            value(im, -radiusx, 0),
            value(im, -diagr, -diagr),
            value(im, 0, -radiusy),
            value(im, diagr, -diagr),
            value(im, radiusx, 0),
            value(im, diagr, diagr),
            value(im, 0, radiusy),
            value(im, -diagr, diagr)};
        boolean[] ons = new boolean[varound.length];
        for (int i = 0; i < ons.length; i++) {
            ons[i] = on(varound[i]);
        }
        if (hasMoreThanOneOnComponent(ons)) {
            output(false);
            debug(false, varound, ons);
            return;
        }
        boolean res = countsOn(ons) <= acceptUpTo;
        output(res);
        debug(res, varound, ons);
    }

    public void forView(BufferedImage im) {
        if (!viewEnabled) {
            return;
        }
        float radiusx = radius * halfWidth;
        float radiusy = radius * halfHeight;
        Graphics2D g = im.createGraphics();
        g.setColor(Color.WHITE);
        g.draw(new Rectangle2D.Float(x - halfWidth, y - halfWidth, halfWidth * 2, halfHeight * 2));
        g.draw(new Rectangle2D.Float(x - radiusx - halfWidth, y - radiusy - halfWidth, halfWidth * 2 + radiusx * 2, halfHeight * 2 + radiusy * 2));
        view(im);
    }

    /**
     * This method is highly contextual, the parameters being offsets as if the
     * halfSizes were 1. These offsets are corrected by this method to scale the
     * pattern according to the actual halfSizes.
     *
     * @param im
     * @param dx
     * @param dy
     * @return
     */
    private float value(BufferedImage im, float dx, float dy) {
        dx *= halfHeight;
        dy *= halfWidth;
        // for now closest sample
        return im.getRaster().getSample((int) (.5 + x + dx), (int) (.5 + y + dy), 0);
    }

    private boolean on(float value) {
        return value >= threshold;
    }

    private boolean hasMoreThanOneOnComponent(boolean[] ons) {
        int end = ons.length;
        if (ons[0]) {
            while (ons[end - 1]) {
                end--;
                if (end == 0) {
                    // all are on
                    return false;
                }
            }
        }
        boolean inComp = false;
        boolean doneComp = false;
        for (int i = 0; i < end; i++) {
            boolean b = ons[i];
            if (doneComp) {
                if (b) {
                    return true;
                }
            } else {
                if (!inComp && b) {
                    inComp = true;
                } else if (inComp && !b) {
                    inComp = false;
                    doneComp = true;
                }
            }
        }
        return false;
    }

    private int countsOn(boolean[] ons) {
        int size = 0;
        for (boolean b : ons) {
            if (b) {
                size++;
            }
        }
        return size;
    }

    private void output(boolean b) {
        emitEvent(b);
    }

    private void debug(boolean b, float[] values, boolean[] ons) {
        emitEvent(b + " " + Arrays.toString(values) + " " + Arrays.toString(ons));
    }

    private void view(BufferedImage im) {
        emitEvent(im);
    }
}
