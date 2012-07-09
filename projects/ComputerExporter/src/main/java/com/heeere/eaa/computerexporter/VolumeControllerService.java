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

public class VolumeControllerService {

    private Service service;
    private ServiceRepository serviceRepository;
    private final Map<String, String> commands = new HashMap<String, String>() {
        {
            put("volumeup", "set Master 5%+");
            put("volumedown", "set Master 5%-");
            put("mute", "set Master 0");
        }
    };
    private final String id = "" + Math.random();
    private final String requires = "VolumeControl for=VolumeChanger_" + id;
    private String amixer = null;

    public VolumeControllerService(ServiceFactory factory, String amixer) throws IOException {
        this(factory, amixer, "VolumeChanger", "events");
    }

    public VolumeControllerService(ServiceFactory factory, String amixer, String serviceName, final String inputConnector) throws IOException {
        this.amixer = amixer;
        service = factory.create(serviceName);
        service.addVariable("requires", "string", "requirements", VariableAccessType.CONSTANT);
        service.setVariableValue("requires", requires);
        service.addConnector(inputConnector, "receives commands for volume control (volumeup, volumedown, mute)", ConnectorType.INPUT);
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
        String amixerCommand = commands.get(command);
        if (amixerCommand != null) {
            System.err.println("Sending amixer “" + amixerCommand + "” in response to remote control command “" + command + "”");
            exec((amixer + " " + amixerCommand).split(" "));
        } else {
            System.err.println("Unhandled remote control command “" + command + "”");
        }
    }

    private void exec(String... command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            Logger.getLogger(VolumeControllerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
