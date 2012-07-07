/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import static fr.prima.omiscid.user.service.ServiceFilters.*;

import fr.prima.omiscid.user.connector.ConnectorType;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import fr.prima.omiscid.user.variable.VariableAccessType;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.swing.JFrame;

public class DesktopExporterFrame {

    JFrame frame;
    ExportAsServiceMouseListener mouseListener;
    ExportDisplayAsServiceConnectorListener displayListener;
    Service mouseService;
    String eventsConnector = "events";
    Service displayService;
    String displayConnector = "display";
    String commandConnector = "command";
    String exportConnector = "export";
    String requiredId = "DisplayExporter_" + Math.random();
    ServiceRepository repository;

    public DesktopExporterFrame(ServiceFactory factory, boolean exportDisplay, boolean exportMouse) throws IOException {
        init(factory, exportDisplay, exportMouse);
        GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        defaultScreenDevice.setFullScreenWindow(frame);
        DisplayMode mode = defaultScreenDevice.getDisplayMode();
        frame.setLocation(0, 0);
        frame.setSize(mode.getWidth(), mode.getHeight());
    }

    public DesktopExporterFrame(ServiceFactory factory, int w, int h, boolean exportDisplay, boolean exportMouse) throws IOException {
        init(factory, exportDisplay, exportMouse);
        //frame.setLocation(0, 0);
        frame.getContentPane().setPreferredSize(new Dimension(w, h));
        frame.pack();
        //frame.setUndecorated(true);
    }
    private static AtomicInteger exporterIndex = new AtomicInteger(0);

    private void init(ServiceFactory factory, boolean exportDisplay, boolean exportMouse) throws IOException {
        final int index = exporterIndex.incrementAndGet();
        frame = new JFrame("Export " + index);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (exportMouse) { // mouse capture
            mouseService = factory.create("Mouse3");
            mouseService.addConnector(eventsConnector, "mouse motion and click events", ConnectorType.OUTPUT);
            mouseService.addVariable("provides", "string", "provided functionalities", VariableAccessType.CONSTANT);
            mouseService.setVariableValue("provides", "Mouse3");
            mouseService.addVariable("description", "string", "service description for adapter framework", VariableAccessType.CONSTANT);
            mouseService.setVariableValue("description", "M" + index + "@" + InetAddress.getLocalHost().getHostName());
            mouseListener = new ExportAsServiceMouseListener(mouseService, eventsConnector);
            mouseService.start();

            frame.getContentPane().addMouseListener(mouseListener);
            frame.getContentPane().addMouseMotionListener(mouseListener);
        }

        if (exportDisplay) { // display export
            displayService = factory.create("Display");
            displayService.addConnector(displayConnector, "receives display commands", ConnectorType.INPUT);
            displayService.addConnector(commandConnector, "receives display commands", ConnectorType.INPUT);
            displayService.addConnector(exportConnector, "export display", ConnectorType.OUTPUT);
            displayService.addVariable("requires", "string", "required functionalities", VariableAccessType.CONSTANT);
            String requirementSource = "DisplaySource for=" + requiredId;
            String requirementCommand = "DisplayCommand for=" + requiredId;
            displayService.setVariableValue("requires", requirementSource + "\n" + requirementCommand);
            displayService.addVariable("description", "string", "service description for adapter framework", VariableAccessType.CONSTANT);
            displayService.setVariableValue("description", "D" + index + "@" + InetAddress.getLocalHost().getHostName());
            displayListener = new ExportDisplayAsServiceConnectorListener(displayService);
            displayService.addConnectorListener(displayConnector, displayListener);
            displayService.addConnectorListener(commandConnector, displayListener);

            displayService.addVariable("background", "string", "image for background", VariableAccessType.READ);
            displayService.setVariableValue("background", "null");
            displayService.addVariable("fgcolor", "string", "color for foreground", VariableAccessType.READ);
            displayService.setVariableValue("fgcolor", "White");

            displayService.start();
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(displayListener);

            repository = factory.createServiceRepository();
            repository.addListener(new ServiceRepositoryListener() {
                public void serviceAdded(ServiceProxy serviceProxy) {
                    String var = serviceProxy.getVariableValue("provides").replaceFirst("^.* z=([^ ]+).*$", "$1");
                    float layer = var.isEmpty() ? 0 : Float.parseFloat(var);
                    displayListener.setLayerForPendingConnection(serviceProxy.getPeerId(), layer);
                    displayService.connectTo(displayConnector, serviceProxy, "display");
                }

                public void serviceRemoved(ServiceProxy serviceProxy) {
                }
            }, hasVariable("provides", Pattern.quote(requirementSource) + ".*"));
            repository.addListener(new ServiceRepositoryListener() {
                public void serviceAdded(ServiceProxy serviceProxy) {
                    String var = serviceProxy.getVariableValue("provides").replaceFirst("^.* z=([^ ]+).*$", "$1");
                    float layer = var.isEmpty() ? 0 : Float.parseFloat(var);
                    displayListener.setLayerForPendingConnection(serviceProxy.getPeerId(), layer);
                    displayService.connectTo(commandConnector, serviceProxy, "command");
                }

                public void serviceRemoved(ServiceProxy serviceProxy) {
                }
            }, hasVariable("provides", Pattern.quote(requirementCommand) + ".*"));
        }
        frame.setVisible(true);
    }

    public void stop() {
        if (mouseService != null) {
            mouseService.stop();
        }
        if (displayService != null) {
            displayService.stop();
        }
        if (repository != null) {
            repository.stop();
        }
        frame.dispose();
    }
}
