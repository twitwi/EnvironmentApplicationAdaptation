 /**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceFilters;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.variable.VariableAccessType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SlidePresenterService {

    private Service service;
    private ServiceRepository serviceRepository;
    private final Map<String, String> commands = new HashMap<String, String>() {
        {
            put("previous", "Key Left CurrentWindow");
            put("next", "Key Right CurrentWindow");
            put("previous+", "Key Up CurrentWindow");
            put("next+", "Key Down CurrentWindow");
        }
    };
    private final String id = "" + Math.random();
    private final String requires = "RemoteControl for=SlidePresenter_" + id;
    private String sendxevent = null;

    public SlidePresenterService(ServiceFactory factory, String sendxevent) throws IOException {
        this(factory, sendxevent, "SlidePresenter", "events");
    }

    public SlidePresenterService(ServiceFactory factory, String sendxevent, String serviceName, final String inputConnector) throws IOException {
        this.sendxevent = sendxevent;
        service = factory.create(serviceName);
        service.addVariable("requires", "string", "requirements", VariableAccessType.CONSTANT);
        service.setVariableValue("requires", requires);
        service.addConnector(inputConnector, "receives sentences to emit vocally", ConnectorType.INPUT);
        service.addConnectorListener(inputConnector, new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
                processCommand(message.getBufferAsStringUnchecked());
            }

            public void disconnected(Service service, String localConnectorName, int peerId) {
            }

            public void connected(Service service, String localConnectorName, int peerId) {
            }
        });
        serviceRepository = factory.createServiceRepository();
        serviceRepository.addListener(new ServiceRepositoryListener() {
            public void serviceAdded(ServiceProxy serviceProxy) {
                service.connectTo(inputConnector, serviceProxy, inputConnector);
            }

            public void serviceRemoved(ServiceProxy serviceProxy) {
            }
        }, ServiceFilters.hasVariable("provides", Pattern.quote(requires) + ".*"));
        service.start();
    }

    public void stop() {
        if (service != null) {
            service.stop();
        }
        if (serviceRepository != null) {
            serviceRepository.stop();
        }
    }

    private void processCommand(String command) {
        String xeventCommand = commands.get(command);
        if (xeventCommand != null) {
            System.err.println("Sending xevent “" + xeventCommand + "” in response to remote control command “" + command + "”");
            exec(sendxevent, xeventCommand);
        } else {
            System.err.println("Unhandled remote control command “" + command + "”");
        }
    }

    private void exec(String... command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(SlidePresenterService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
