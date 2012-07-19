/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.eaa.magicsnake;

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
import fr.prima.omiscid.user.util.Utility;
import fr.prima.omiscid.user.variable.VariableAccessType;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 *
 * @author emonet
 */
public class MagicSnakeService {

    private final Vector<Element> commands = new Vector<Element>(); // Thread safe
    private Service service;
    private ServiceRepository serviceRepository;
    private String uniqueId = "MagicSnakeControl_" + new Random().nextInt(1000000);
    private Timer timer;

    public MagicSnakeService(ServiceFactory factory) throws IOException {
        service = factory.create("MagicSnake");
        serviceRepository = factory.createServiceRepository();
        service.addVariable("provides", "String", "provided functionalities", VariableAccessType.CONSTANT);
        service.setVariableValue("provides", "SnakeModel");
        service.addVariable("requires", "String", "requires", VariableAccessType.CONSTANT);
        //service.setVariableValue("requires", "Grid3x3ClickerWithId id="+uniqueIdBoth+"\nGrid3x3ClickerWithId id="+uniqueId1+"\nGrid3x3ClickerWithId id="+uniqueId2);
        service.setVariableValue("requires", "Relative2DControllerWithId id=" + uniqueId);

        service.addConnector("supercontrol", "String", ConnectorType.INPUT);
        service.addConnectorListener("supercontrol", new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
                if (message.getBuffer().length == 0) {
                    initGameModel(0);
                } else {
                    // can throw an exception (don't care, omiscid will catch and report it)
                    int level = Integer.parseInt(message.getBufferAsStringUnchecked());
                    initGameModel(level - 1);
                }
            }
            public void disconnected(Service service, String localConnectorName, int peerId) {
            }
            public void connected(Service service, String localConnectorName, int peerId) {
            }
        });
        service.addConnector("control", "String", ConnectorType.INPUT);
        service.addConnector("model", "String", ConnectorType.OUTPUT);
        service.addConnectorListener("control", controlListener());

        service.start();

        ServiceFilter filter = ServiceFilters.hasVariable("provides", "Relative2DControllerWithId id=" + uniqueId);
        serviceRepository.addListener(new ServiceRepositoryListener() {

            public void serviceAdded(ServiceProxy serviceProxy) {
                System.out.println("connection");
                service.connectTo("control", serviceProxy, "events");
            }

            public void serviceRemoved(ServiceProxy serviceProxy) {
            }
        }, filter);

        initGameModel(2);
        long period = 25;
        timer = new Timer();
        timer.scheduleAtFixedRate(gameLoop(), period, period);
    }

    int currentLevel = 0;
    private void reInitGameModel() {
        initGameModel(currentLevel);
    }
    private void initGameModel(int level) {
        currentLevel = level;
        model = new GameModel();
        model.obstacles = new ArrayList<Ellipse2D>();
        model.targets = new ArrayList<Ellipse2D>();
        model.x = 0;
        model.y = 0;
        model.startTime = System.currentTimeMillis();
        model.totalLength = 0;
        initGameLevel[currentLevel].run();
    }
    private Runnable[] initGameLevel = new Runnable[] {
        new Runnable() {
            public void run() {
                model.obstacles.add(new Ellipse2D.Double(-100, -100, 100, 50));
                model.obstacles.add(new Ellipse2D.Double(30, -200, 50, 200));
                model.targets.add(new Ellipse2D.Double(-100, -150, 20, 20));
            }
        },
        new Runnable() {
            public void run() {
                model.obstacles.add(new Ellipse2D.Double(-200, -200, 400, 30));
                model.obstacles.add(new Ellipse2D.Double(-200, -200, 30, 400));
                model.obstacles.add(new Ellipse2D.Double(-200, 170, 400, 30));
                model.obstacles.add(new Ellipse2D.Double(170, -200, 30, 400));
                model.obstacles.add(new Ellipse2D.Double(-100, -100, 100, 50));
                model.obstacles.add(new Ellipse2D.Double(30, -200, 50, 200));
                model.obstacles.add(new Ellipse2D.Double(30, -200, 50, 200));
                model.targets.add(new Ellipse2D.Double(100, -135, 20, 20));
                model.targets.add(new Ellipse2D.Double(115, -135, 20, 20));
                model.x = -10;
                model.y = -135;
            }
        },
        new Runnable() {
            public void run() {
                model.obstacles.add(new Ellipse2D.Double(-200, -200, 400, 30));
                model.obstacles.add(new Ellipse2D.Double(-200, -200, 30, 400));
                model.obstacles.add(new Ellipse2D.Double(-200, 150, 400, 30));
                model.obstacles.add(new Ellipse2D.Double(150, -200, 30, 400));

                model.obstacles.add(new Ellipse2D.Double(-60, -140, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(20, -140, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(100, -140, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-100, -80, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-20, -80, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(60, -80, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-140, -20, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-60, -20, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(20, -20, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(100, -20, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-100, 40, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-20, 40, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(60, 40, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-140, 100, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(-60, 100, 40, 40));
                model.obstacles.add(new Ellipse2D.Double(20, 100, 40, 40));

                model.targets.add(new Ellipse2D.Double(120, 120, 80, 80));
                model.x = -160;
                model.y = -160;
            }
        },
    };

    public enum GameStatus {RUNNING, COMPLETE, FAILED}
    public static class GameModel {
        float x;
        float y;
        float totalLength;
        List<Ellipse2D> obstacles;
        List<Ellipse2D> targets;
        long nextRestartTime;
        long lastStepTime;
        long startTime;
        GameStatus status = GameStatus.RUNNING;
    }
    private GameModel model;

    private TimerTask gameLoop() {
        return new TimerTask() {

            @Override
            public void run() {
                ArrayList<Element> events = new ArrayList<Element>();
                while (!commands.isEmpty()) {
                    events.add(commands.remove(0));
                }
                if (model.status != GameStatus.RUNNING) {
                    if (model.nextRestartTime > System.currentTimeMillis()) {
                        return;
                    } else {
                        reInitGameModel();
                        return;
                    }
                }
                float newX = model.x;
                float newY = model.y;
                for (Element e : events) {
                    try {
                        float dx = Float.parseFloat(e.getAttribute("dx"));
                        float dy = Float.parseFloat(e.getAttribute("dy"));
                        newX += dx;
                        newY -= dy;
                    } catch(Exception ex) {
                        // Ignore wrongly formatted messages (report anyway)
                        Logger.getLogger(MagicSnakeService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Line2D move = new Line2D.Double(model.x, model.y, newX, newY);
                for (Ellipse2D obstacle : model.obstacles) {
                    //if (GeneralPath.intersects(obstacle.getPathIterator(null), move.getBounds())) {
                        // we may intersect, test only the extremal points (should do better)
                        if (obstacle.contains(newX, newY) || obstacle.contains(model.x, model.y)) {
                            model.status = GameStatus.FAILED;
                        }
                    //}
                }
                // targets take precedence over obstracles
                for (Ellipse2D target : model.targets) {
                    //if (GeneralPath.intersects(target.getPathIterator(null), move.getBounds())) {
                        // we may intersect, test only the extremal points (should do better)
                        if (target.contains(newX, newY) || target.contains(model.x, model.y)) {
                            model.status = GameStatus.COMPLETE;
                        }
                    //}
                }
                model.lastStepTime = System.currentTimeMillis();
                float dx = newX - model.x;
                float dy = newY - model.y;
                model.x = newX;
                model.y = newY;
                model.totalLength += Math.sqrt(dx * dx + dy * dy);
                model.nextRestartTime = model.lastStepTime + 1500;
                if (model.status == GameStatus.COMPLETE) {
                    System.err.println("Complete level " + (currentLevel + 1) + " in " + (model.lastStepTime - model.startTime) + " a " + model.totalLength + " unit long path.");
                    currentLevel = (currentLevel + 1) % initGameLevel.length;
                }
                outputModel();
            }

        };
    }

    private void outputModel() {
        StringBuilder message = new StringBuilder();
        message.append("<snake status='").append(model.status.name())
                .append("' x='").append(model.x).append("' y='").append(model.y)
                .append("' time='").append(model.lastStepTime - model.startTime)
                .append("' length='").append(model.totalLength)
                .append("'>\n");
        for (Ellipse2D el : model.obstacles) {
            message.append("  <obstacle x='").append(el.getX()).append("' y='").append(el.getY()).append("' w='").append(el.getWidth()).append("' h='").append(el.getHeight()).append("'/>\n");
        }
        for (Ellipse2D el : model.targets) {
            message.append("  <target x='").append(el.getX()).append("' y='").append(el.getY()).append("' w='").append(el.getWidth()).append("' h='").append(el.getHeight()).append("'/>\n");
        }
        message.append("</snake>\n");
        service.sendToAllClients("model", Utility.message(message.toString()));
    }

    private ConnectorListener controlListener() {
        return new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
                try {
                    commands.add(message.getBufferAsXML());
                } catch (MessageInterpretationException ex) {
                    // Ignore wrongly formatted messages (report anyway)
                    Logger.getLogger(MagicSnakeService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            public void disconnected(Service service, String localConnectorName, int peerId) {
            }
            public void connected(Service service, String localConnectorName, int peerId) {
            }
        };
    }




}
