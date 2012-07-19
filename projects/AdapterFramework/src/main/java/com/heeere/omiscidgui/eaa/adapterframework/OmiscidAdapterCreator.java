/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework;

import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.util.Utility;
import com.heeere.omiscidgui.eaa.adapterframework.model.Adapter;
import com.heeere.omiscidgui.eaa.adapterframework.model.Parameter;
import fr.prima.omiscidgui.browser.ServiceClient;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author twilight
 *
 *

---


(18:17:52) matt:   http://www.comsoc.org/confs/globecom/2010/
(18:17:56) matt: http://www.ubicomp2010.org/node/8
(18:21:47) matt: http://perware.uni-mannheim.de/
(18:22:26) matt: http://www2.ing.unipi.it/persens/cfp.htm
(18:22:28) twilight: sauve ça sur une page du wiki sinon ça va se perdre
(18:22:50) matt: http://smarte.eecs.wsu.edu/call-papers
(18:24:34) matt has left the conversation.
 http://www.playframework.org/
 *
 */
public class OmiscidAdapterCreator {

    public static void createServiceFor(final Adapter adapter, final ServiceClient serviceClient) {
        ServiceProxy proxy = adapter.getPossibleServiceProxy();
        if (proxy == null) {
            throw new IllegalArgumentException("possible-service-proxy is not set");
        }
        adapter.setMode(Adapter.Mode.IN_CREATION);
        new Thread() {
            @Override
            public void run() {
                startServiceFor(adapter, serviceClient);
            }
        }.start();
    }

    private static void startServiceFor(Adapter adapter, ServiceClient client) {
        try {
            String message = getParametersAsString(adapter);
            System.err.println(message);
            sendMessage(client, adapter.getPossibleServiceProxy(), "create", message);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void sendMessage(ServiceClient client, ServiceProxy proxy, String remoteConnector, String message) throws IOException {
        String connector = client.getConnector(ConnectorType.OUTPUT);
        client.getConnectorService(connector).connectTo(connector, proxy, remoteConnector);
        sleep(250);
        client.getConnectorService(connector).sendToAllClients(connector, Utility.message(message));
        sleep(250);
        client.freeConnector(connector);
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static String escapeValue(Parameter p) {
        String val = p.getPossibleAffectedValue();
        val = val != null ? val : p.getDefaultValue();
        val = val != null ? val : "";
        return val.replaceAll("%", "%%").replaceAll("\\n", "%n").replaceAll(" ", "%s");
    }
    public static boolean canGetParametersAsString(Adapter adapter) {
        // todo cleaner way
        try {
            getParametersAsString(adapter);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
    public static String getParametersAsString(Adapter adapter) {
        StringBuilder res = new StringBuilder();
        if (adapter.getRequired().getFullfilledBy().getPossibleServiceProxy() != null) {
            String id = adapter.getRequired().getFullfilledBy().getPossibleServiceProxy().getPeerIdAsString();
            res.append(id).append("\n");
        } else if (adapter.getRequired().getFullfilledBy().getPossibleParentAdapter() != null) {
            if (adapter.getRequired().getFullfilledBy().getPossibleParentAdapter().getMode() == Adapter.Mode.PRESENT) {
                String id = adapter.getRequired().getFullfilledBy().getPossibleParentAdapter().getProvided().getPossibleServiceProxy().getPeerIdAsString();
                res.append(id).append("\n");
            } else {
                throw new UnsupportedOperationException("extracting parameters not supported in this case");
            }
        } else {
            throw new UnsupportedOperationException("extracting parameters not supported in this case");
        }
        for (Parameter p : adapter.getParameters()) {
            res.append(p.getName()).append(":=").append(escapeValue(p)).append("\n");
        }
        return res.toString();
    }

}
