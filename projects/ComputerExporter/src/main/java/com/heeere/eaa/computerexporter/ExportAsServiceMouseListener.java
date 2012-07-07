/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.util.Utility;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class ExportAsServiceMouseListener extends MouseAdapter {

    Service mouseService;
    String eventsConnector;

    public ExportAsServiceMouseListener(Service mouseService, String eventsConnector) {
        this.mouseService = mouseService;
        this.eventsConnector = eventsConnector;
    }

    private void send(String message) {
        mouseService.sendToAllClients(eventsConnector, Utility.message(message));
    }
    Map<Integer, Integer> buttons = new HashMap<Integer, Integer>() {
        {
            put(MouseEvent.BUTTON1, 1);
            put(MouseEvent.BUTTON2, 2);
            put(MouseEvent.BUTTON3, 3);
        }
    };
    int count = 0;

    @Override
    public void mouseClicked(MouseEvent e) {
        int buttonNumber = buttons.get(e.getButton());
        send("<click x='" + e.getX() + "' y='" + e.getY() + "' button='" + buttonNumber + "'/>");
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        send("<move x='" + e.getX() + "' y='" + e.getY() + "'/>");
    }
}
