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
import java.util.regex.Pattern;

public class TextToSpeechService {

    private Service service;
    private ServiceRepository serviceRepository;
    private TextSpeaker speaker;
    private final String id = "" + Math.random();
    private final String requires = "TextToSpeechSource for=TextToSpeech_" + id;
    private final String inputConnector = "say";

    public TextToSpeechService(ServiceFactory factory, TextSpeaker speaker) throws IOException {
        this.speaker = speaker;
        service = factory.create("TextToSpeech");
        service.addVariable("requires", "string", "requirements", VariableAccessType.CONSTANT);
        service.setVariableValue("requires", requires);
        service.addConnector(inputConnector, "receives sentences to emit vocally", ConnectorType.INPUT);
        service.addConnectorListener(inputConnector, new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
                speakSentence(message.getBufferAsStringUnchecked());
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

    private void speakSentence(String sentence) {
        speaker.say(sentence);
    }
}
