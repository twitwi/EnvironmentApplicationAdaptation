/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.eaa.additionalmodules.cl;

import com.heeere.eaa.additionalmodules.ImageDifferenceKernels;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLImageFormat;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import fr.prima.gsp.framework.ModuleParameter;
import fr.prima.gsp.framework.spi.AbstractModuleEnablable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author twilight
 */
public class ImageDifference extends AbstractModuleEnablable {

    @ModuleParameter(initOnly = true)
    public String background = null;
    @ModuleParameter(initOnly = true)
    public int useNthFrame = 0;
    @ModuleParameter
    public boolean usePrevious = false;
    @ModuleParameter
    public boolean refreshOnResolutionChange = true;
    //
    //
    private CLContext context;
    private CLQueue queue;
    private ImageDifferenceKernels imageDifferenceKernels;
    //
    private CLImage2D baseImage = null;

    @Override
    protected void initModule() {
        super.initModule();
        context = JavaCL.createBestContext();
        queue = context.createDefaultQueue();
        try {
            imageDifferenceKernels = new ImageDifferenceKernels(context);
            if (background != null && !background.isEmpty()) {
                if (new File(background).exists()) {
                    baseImage = context.createImage2D(CLMem.Usage.InputOutput, ImageIO.read(new File(background)), false);
                } else {
                    baseImage = context.createImage2D(CLMem.Usage.InputOutput, ImageIO.read(new URL(background)), false);
                }
                useNthFrame = -1;
            }
        } catch (IOException ex) {
            setEnabled(false);
            Logger.getLogger(ImageDifference.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // todo: can we optimize the CLMem.Usage.InputOutput? does it matter?
    public void input(BufferedImage im) {
        if (!isEnabled()) {
            return;
        }
        if (useNthFrame > 0) {
            useNthFrame--;
            return;
        }
        boolean notSameSize = baseImage != null && (baseImage.getWidth() != im.getWidth() || baseImage.getHeight() != im.getHeight());
        if (useNthFrame == 0 || (refreshOnResolutionChange && notSameSize)) {
            useNthFrame = -1;
            baseImage = context.createImage2D(CLMem.Usage.InputOutput, im, false);
            return;
        }
        if (baseImage == null) {
            System.err.println("Error in ImageDifference: baseImage is not defined (via 'background' variable or 'base' input)");
            return;
        }
        int w = im.getWidth();
        int h = im.getHeight();
        if (notSameSize) {
            System.err.println("Error in ImageDifference: dimensions differ, base: " + baseImage.getWidth() + "x" + baseImage.getHeight() + "  new:" + w + "x" + h);
            return;
        }
        CLImage2D newImage = context.createImage2D(CLMem.Usage.InputOutput, im, false);
        CLImageFormat imageFormat = new CLImageFormat(CLImageFormat.ChannelOrder.INTENSITY, CLImageFormat.ChannelDataType.UNormInt8);
        CLImage2D output = context.createImage2D(CLMem.Usage.InputOutput, imageFormat, w, h);
        int[] globalWorkSize = new int[]{w, h};
        imageDifferenceKernels.image_difference_absolute(queue, output, baseImage, newImage, globalWorkSize, null);
        BufferedImage out = output.read(queue);
        output.release();
        if (usePrevious) {
            baseImage.release();
            baseImage = newImage;
        } else {
            newImage.release();
        }
        output(out);
    }

    private void output(BufferedImage out) {
        emitEvent(out);
    }
}
