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
import fr.prima.omiscid.user.util.Utility;
import fr.prima.omiscid.user.variable.VariableAccessType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatService {

    private Service service;
    private String alias;
    private String connector;
    private String variable;
    private ServiceFactory factory;

    public ChatService(ServiceFactory factory, String alias) throws IOException {
        this(factory, alias, "Chat", "chat", "alias");
    }

    public ChatService(ServiceFactory factory, String alias, String serviceName, String connector, String variable) throws IOException {
        this.alias = alias;
        this.factory = factory;
        this.connector = connector;
        this.variable = variable;
        service = factory.create(serviceName);
        service.addVariable(variable, "string", "alias for the chat user", VariableAccessType.CONSTANT);
        service.setVariableValue(variable, alias);
        service.addConnector(connector, "send and receive messages", ConnectorType.INOUTPUT);
        service.addConnectorListener(connector, new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
                chatMessageReceived(message.getPeerId(), message.getBufferAsStringUnchecked());
            }

            public void connected(Service service, String localConnectorName, int peerId) {
                clientConnected(peerId);
            }

            public void disconnected(Service service, String localConnectorName, int peerId) {
                clientDisconnected(peerId);
            }
        });
        service.start();
    }

    public void stop() {
        if (service != null) {
            service.stop();
        }
    }
    Map<Integer, ChatFrame> chats = new HashMap<Integer, ChatFrame>();

    private void clientConnected(final int peerId) {
        chats.put(peerId, new ChatFrame(peerId, new ChatSender() {
            public void send(String message) {
                service.sendToOneClient(connector, Utility.message(message), peerId);
            }
        }));
        { // lookup for alias if the remote peer is a service
            final ServiceRepository repository = factory.createServiceRepository();
            repository.addListener(new ServiceRepositoryListener() {
                public void serviceAdded(ServiceProxy serviceProxy) {
                    ChatFrame chatFrame = chats.get(peerId);
                    if (chatFrame != null) {
                        chatFrame.setRemoteIdentifier(serviceProxy.getVariableValue("alias"));
                    }
                    repository.stop();
                }

                public void serviceRemoved(ServiceProxy serviceProxy) {
                }
            }, ServiceFilters.peerIdIs(peerId));
        }
    }

    private void chatMessageReceived(int peerId, String message) {
        ChatFrame chat = chats.get(peerId);
        if (chat != null) {
            chat.received(message);
        }
    }

    private void clientDisconnected(int peerId) {
        ChatFrame chat = chats.remove(peerId);
        if (chat != null) {
            chat.close();
        }
    }
}
