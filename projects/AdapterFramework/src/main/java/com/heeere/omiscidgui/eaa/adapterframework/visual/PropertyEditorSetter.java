/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework.visual;

import org.openide.nodes.Node;

/**
 *
 * @author twilight
 */
public interface PropertyEditorSetter {

    void setNodeToDisplayInProperties(Node node);
    void setBeanToDisplayInProperties(Object o);

}
