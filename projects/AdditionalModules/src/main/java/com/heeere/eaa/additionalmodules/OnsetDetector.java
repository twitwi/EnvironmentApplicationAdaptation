/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.eaa.additionalmodules;

import fr.prima.gsp.framework.ModuleParameter;
import fr.prima.gsp.framework.spi.AbstractModuleEnablable;

/**
 *
 * @author twilight
 */
public class OnsetDetector extends AbstractModuleEnablable {

    @ModuleParameter
    public boolean negate = false;
    @ModuleParameter
    public int onDelay = 1;
    @ModuleParameter
    public int offDelay = 1;
    @ModuleParameter
    public boolean useFirst = true;
    //
    //
    private int counter = 0;
    boolean wasOn = false;
    boolean state = false;
    //

    public void input(boolean v) {
        v ^= negate; // maybe invert/negate the value
        if (!isEnabled()) {
            return;
        }
        if (useFirst) {
            useFirst = false;
            wasOn = v;
            state = v;
            return;
        }
        if (v == wasOn) {
            counter++;
        } else {
            counter = 1;
        }
        if (!state && v && counter == onDelay) {
            output("ON");
            state = true;
        }
        if (state && !v && counter == offDelay) {
            output("OFF");
            state = false;
        }
        wasOn = v;
    }

    private void output(String s) {
        emitEvent(s);
    }
}
