/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.exception.MessageInterpretationException;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.util.Utility;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ExportDisplayAsServiceConnectorListener extends JPanel implements ConnectorListener {

    private final Map<Integer, String> paintCode = new Hashtable<Integer, String>();
    private final Map<Integer, Float> layerFromPeerId = new Hashtable<Integer, Float>();
    private ScriptEngine engine;
    String displayConnector = "display";
    String commandConnector = "command";
    private XPathFactory xpf = XPathFactory.newInstance();
    private XPath xpath = xpf.newXPath();
    private DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder;
    String exportConnector = "export";
    Service displayService;

    {
        engine = new ScriptEngineManager().getEngineByExtension("js");
        setOpaque(true);
        setBackground(Color.BLACK);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setBorder(new LineBorder(Color.WHITE));

        domFactory.setNamespaceAware(true);
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExportDisplayAsServiceConnectorListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ExportDisplayAsServiceConnectorListener(Service service) {
        displayService = service;
    }

    @Override
    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (paintCode.isEmpty()) {
            return;
        }
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = this.getWidth();
        int height = this.getHeight();
        double aspectRatio = ((double) width) / ((double) height);
        // TODO must sandbox this (currently, e.g. java.lang.System.exit(111) is accepted)
        engine.put("width", width);
        engine.put("height", height);
        engine.put("aspectRatio", aspectRatio);
        engine.put("g", g);
        engine.put("s", displayService);

        SortedMap<Integer, String> layers = new TreeMap<Integer, String>(new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                int res = (int) Math.signum(layerFromPeerId.get(o1) - layerFromPeerId.get(o2));
                return res != 0 ? res : o1.compareTo(o2);
            }
        });
        layers.putAll(paintCode);
        for (String code : layers.values()) {
            try {
                engine.eval(code);
            } catch (ScriptException ex) {
                Logger.getLogger(ExportDisplayAsServiceConnectorListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void messageReceived(final Service service, String localConnectorName, Message message) {
        if (localConnectorName.equals(displayConnector)) {
            if (paintCode.containsKey(message.getPeerId())) {
                paintCode.put(message.getPeerId(), message.getBufferAsStringUnchecked());

                if (service.getConnectorClientCount(exportConnector) > 0) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                            Graphics myg = image.getGraphics();
                            paint(myg);
                            myg.dispose();
                            WritableRaster raster = image.getRaster();
                            DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
                            service.sendToAllClients(exportConnector, buffer.getData());

                        }
                    });
                }
                postRepaint();
            } else {
                System.err.println("Dropped message (should probably not happen).");
            }
        }
        if (localConnectorName.equals(commandConnector)) {
            String msg = "";
            try {
                msg = message.getBufferAsString();
                Document doc1 = builder.parse(new InputSource(new StringReader(msg)));
                NodeList nodeList = (NodeList) xpath.evaluate("//painttool", doc1, XPathConstants.NODESET);
                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node nodexml = nodeList.item(i);
                        String command = xpath.evaluate("./@command", (Element) nodexml);
                        if (command.equals("Capture")) {
                            String filename = xpath.evaluate("./@file", (Element) nodexml);
                            ImageProcessing.CaptureFrame(this, filename);
                            displayService.setVariableValue("background", filename);
                        }
                        if (command.equals("Clear")) {
                            displayService.setVariableValue("background", "null");
                        }
                        if (command.equals("Red") || command.equals("Green") || command.equals("Blue")) {
                            displayService.setVariableValue("fgcolor", command);
                        }
                    }
                }
            } catch (SAXException ex) {
                Logger.getLogger(ExportDisplayAsServiceConnectorListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ExportDisplayAsServiceConnectorListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(ExportDisplayAsServiceConnectorListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessageInterpretationException ex) {
                Logger.getLogger(ExportDisplayAsServiceConnectorListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private long fastestRepaintPeriod = 20; // 10 => 100fps, 20 => 50fps, 100 => 10fps
    private long lastRepaintCall = System.currentTimeMillis() - fastestRepaintPeriod - 1;
    private boolean posted = false;
    private Timer timer = new Timer();

    private TimerTask postRepaintTask() {
        return new TimerTask() {
            @Override
            public void run() {
                postRepaint();
            }
        };
    }

    ;

	private synchronized void postRepaint() {
        long now = System.currentTimeMillis();
        if (now - lastRepaintCall > fastestRepaintPeriod) {
            lastRepaintCall = now;
            repaint();
            posted = false;
        } else if (!posted) {
            timer.schedule(postRepaintTask(), lastRepaintCall + fastestRepaintPeriod - now);
            posted = true;
        }
    }

    public synchronized void disconnected(Service service, String localConnectorName, int peerId) {
        paintCode.remove(peerId);
        postRepaint();
    }

    public synchronized void connected(Service service, String localConnectorName, int peerId) {
        paintCode.put(peerId, "");
        int ppid = Utility.PeerId.rootPeerIdFromConnectorPeerId(peerId);
        Float layer = layerFromPeerId.remove(ppid);
        layer = layer != null ? layer : 0;
        layerFromPeerId.put(peerId, layer);
    }

    public void setLayerForPendingConnection(int parentPeerId, float layer) {
        layerFromPeerId.put(parentPeerId, layer);
    }
}
