/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework.interf;

import fr.prima.omiscid.user.service.ServiceProxy;

/**
 *
 * @author emonet
 */
public class KillableServiceTask {

    private ServiceProxy serviceProxy;
    private String variableName = "killService";
    private String variableValue = "not(alive)";

    public KillableServiceTask(ServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
    }

    public ServiceProxy getServiceProxy() {
        return serviceProxy;
    }

    public void setServiceProxy(ServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
    
}
