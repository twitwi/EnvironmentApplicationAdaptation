/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.adaptertools;

import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author emonet
 */
public class Adapters {

    public static boolean isAnAdapterFactory(Service service) {
        return service.getVariableValue("name").equals("AdapterFactory");
        /*
         try {
         service.getVariableValue("code");
         return true;
         } catch (UnknownVariable ex) {
         return false;
         }*/
    }

    public static AdapterInstance createAdapterFrom(ServiceFactory serviceFactory, Service factoryService, String parameterMessage, String startCode, String adapterCode) {
        String[] params = parameterMessage.split("\\n");
        String to = factoryService.getVariableValue("to");
        Map<String, String> parameters = new HashMap<String, String>();
        readParametersInto(params, parameters);

        Service instanceService = serviceFactory.create(to.split(" ")[0]);
        AdapterInstance result = new AdapterInstance(factoryService, instanceService, to, parameterMessage, parameters, true, startCode, adapterCode);

        return result;
    }

    private static void readParametersInto(String[] params, Map<String, String> parameters) {
        // skip first element intentionally
        for (int i = 1; i < params.length; i++) {
            String[] parts = params[i].split(":=", 2);
            parameters.put(parts[0], parts[1]);
        }
    }
}
