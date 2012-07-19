/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author emonet
 */
public class CollectionFactory {

    public static <T> List<T> list() {
        return new ArrayList<T>();
    }

}
