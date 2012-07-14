/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heeere.eaa.additionalmodules;

import fr.prima.gsp.framework.ModuleParameter;
import fr.prima.gsp.framework.spi.AbstractModuleEnablable;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import fr.prima.omiscid.user.util.Utility;
import fr.prima.omiscid.user.variable.VariableAccessType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author twilight
 */
public class OmiscidService extends AbstractModuleEnablable {

    @ModuleParameter
    public String name = "ServiceInGSP";
    @ModuleParameter
    public String output = "";
    @ModuleParameter
    public String constant = "";
    //
    //
    private Service service;
    //

    @Override
    protected void initModule() {
        ServiceFactory factory = new ServiceFactoryImpl();
        service = factory.create(name);
        if (output != null && !output.isEmpty()) {
            try {
                service.addConnector(output, "GSP output", ConnectorType.OUTPUT);
            } catch (IOException ex) {
                Logger.getLogger(OmiscidService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (constant != null && !constant.isEmpty()) {
            String[] parts = constant.split(":");
            service.addVariable(parts[0], "String", "GSP variable", VariableAccessType.CONSTANT);
            service.setVariableValue(parts[0], parts[1]);
        }
        service.start();
    }

    public void input(String message) {
        service.sendToAllClients(output, Utility.message(message));
    }
}
