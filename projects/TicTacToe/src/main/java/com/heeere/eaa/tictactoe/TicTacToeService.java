/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.tictactoe;

/**
 *
 * @author langet, emonet
 */
import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.exception.MessageInterpretationException;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceFilter;
import fr.prima.omiscid.user.service.ServiceFilters;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import fr.prima.omiscid.user.variable.VariableAccessType;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeService implements ConnectorListener {

    private final Service service;
    private ServiceFactory factory;
    private TicTacToe tictactoe;
    private final ServiceRepository repo;
    private String uniqueId1;
    private String uniqueId2;
    private String uniqueIdBoth;

    public Service getService() {
        return service;
    }

    public void setGame(TicTacToe game) {
        tictactoe = game;
    }

    public TicTacToeService() {
        factory = new ServiceFactoryImpl();
        repo = factory.createServiceRepository();

        //uniqueIdBoth = "MorpionPlayer_Both_"+Math.random()+"_3";
        uniqueId1 = "TicTacToePlayer_1_" + Math.random() + "_1";
        uniqueId2 = "TicTacToePlayer_2_" + Math.random() + "_2";

        service = factory.create("TicTacToe");
        service.addVariable("provides", "String", "provided functionalities", VariableAccessType.CONSTANT);
        service.setVariableValue("provides", "TicTacToeModel");
        service.addVariable("requires", "String", "requires", VariableAccessType.CONSTANT);
        //service.setVariableValue("requires", "Grid3x3ClickerWithId id="+uniqueIdBoth+"\nGrid3x3ClickerWithId id="+uniqueId1+"\nGrid3x3ClickerWithId id="+uniqueId2);
        service.setVariableValue("requires", "Grid3x3ClickerWithId id=" + uniqueId1 + "\nGrid3x3ClickerWithId id=" + uniqueId2);
        try {
            service.addConnector("input", "String", ConnectorType.INPUT);
            service.addConnector("output", "String", ConnectorType.OUTPUT);
            service.addConnector("model", "String", ConnectorType.OUTPUT);
        } catch (IOException ex) {
            Logger.getLogger(TicTacToeService.class.getName()).log(Level.SEVERE, null, ex);
        }
        service.addConnectorListener("input", this);

        service.start();
        //ServiceFilter filter = ServiceFilters.or(ServiceFilters.hasVariable("provides", "Grid3x3ClickerWithId id="+uniqueId1), ServiceFilters.hasVariable("provides", "Grid3x3ClickerWithId id="+uniqueId2), ServiceFilters.hasVariable("provides", "Grid3x3ClickerWithId id="+uniqueIdBoth));
        ServiceFilter filter = ServiceFilters.or(ServiceFilters.hasVariable("provides", "Grid3x3ClickerWithId id=" + uniqueId1), ServiceFilters.hasVariable("provides", "Grid3x3ClickerWithId id=" + uniqueId2));
        repo.addListener(new ServiceRepositoryListener() {
            public void serviceAdded(ServiceProxy serviceProxy) {
                System.out.println("connection");
                service.connectTo("input", serviceProxy, "events");
            }

            public void serviceRemoved(ServiceProxy serviceProxy) {
            }
        }, filter);
        /*
         ServiceFilter filter2 = ServiceFilters.nameIs("MorpionRender");
         repo.addListener(new ServiceRepositoryListener() {

         public void serviceAdded(ServiceProxy serviceProxy) {
         service.connectTo("output", serviceProxy, "input");
         }

         public void serviceRemoved(ServiceProxy serviceProxy) {
         }
         }, filter2 );
         */
    }

    public void messageReceived(Service service, String localConnectorName, Message message) {
        String msg = "";
        try {
            msg = message.getBufferAsString();
        } catch (MessageInterpretationException ex) {
            Logger.getLogger(TicTacToeService.class.getName()).log(Level.SEVERE, null, ex);
        }
        int code = Integer.valueOf(msg);
        int player = code / 10;
        int cell = code % 10;

        tictactoe.action(player, cell);
    }

    public void disconnected(Service service, String localConnectorName, int peerId) {
    }

    public void connected(Service service, String localConnectorName, int peerId) {
    }
}
