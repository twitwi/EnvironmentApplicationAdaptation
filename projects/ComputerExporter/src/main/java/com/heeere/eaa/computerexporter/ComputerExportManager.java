/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ButtonModel;
import javax.swing.JFrame;

public class ComputerExportManager {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        new ComputerExportManager(new ServiceFactoryImpl());
    }
    final String espeak = findInPath("espeak");
    final String sendxevent = findInPath("sendxevent");
    final String amixer = findInPath("amixer");
    private DesktopExportManager content;
    private ServiceFactory serviceFactory;

    public ComputerExportManager(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
        JFrame manager = new JFrame("Computer Exporter");
        manager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content = new DesktopExportManager();
        if (espeak == null) {
            content.setExportSpeechEnabled(false, "Searching for “" + espeak + "” failed.");
        }
        if (sendxevent == null) {
            content.setExportPresenterEnabled(false, "Searching for “" + sendxevent + "” failed.");
        }
        if (amixer == null) {
            content.setVolumeControllerEnabled(false, "Searching for “" + amixer + "” failed.");
        }
        content.setAddExportDisplay(addExportDisplay);
        content.getExportSpeechModel().addActionListener(speechExportListener);
        content.getExportPresenterModel().addActionListener(presenterExportListener);
        content.getVolumeControllerModel().addActionListener(volumeExportListener);
        content.getExportChatModel().addActionListener(chatExportListener);
        manager.setContentPane(content);
        manager.pack();
        manager.setVisible(true);
//        DesktopExporterFrame exporter = new DesktopExporterFrame(new ServiceFactoryImpl());
    }
    private TextSpeaker speaker = new TextSpeaker() {
        public synchronized void say(String sentence) {
            try {
                System.err.println("Saying: “" + sentence + "”");
                Process p = Runtime.getRuntime().exec(new String[]{
                            espeak,
                            "-a", "200", // louder
                            "-g", "2", // slower (between words)
                            sentence
                        });
                p.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    private TextToSpeechService ttsService;
    private ActionListener speechExportListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            final ButtonModel model = content.getExportSpeechModel();
            final boolean startIt = model.isSelected();
            content.setExportSpeechEnabled(false, (startIt ? "starting" : "stopping") + " service");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (startIt) {
                            if (ttsService != null) {
                                throw new IllegalStateException("internal problem with tts starting logic");
                            }
                            ttsService = new TextToSpeechService(serviceFactory, speaker);
                        } else {
                            ttsService.stop();
                            ttsService = null;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        content.setExportSpeechEnabled(true, "");
                    }
                }
            }).start();
        }
    };
    private SlidePresenterService presenterService;
    private ActionListener presenterExportListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            final ButtonModel model = content.getExportPresenterModel();
            final boolean startIt = model.isSelected();
            content.setExportPresenterEnabled(false, (startIt ? "starting" : "stopping") + " service");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (startIt) {
                            if (presenterService != null) {
                                throw new IllegalStateException("internal problem with presenter starting logic");
                            }
                            presenterService = new SlidePresenterService(serviceFactory, sendxevent);
                        } else {
                            presenterService.stop();
                            presenterService = null;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        content.setExportPresenterEnabled(true, "");
                    }
                }
            }).start();
        }
    };
    private VolumeControllerService volumeService;
    private ActionListener volumeExportListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            final ButtonModel model = content.getVolumeControllerModel();
            final boolean startIt = model.isSelected();
            content.setVolumeControllerEnabled(false, (startIt ? "starting" : "stopping") + " service");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (startIt) {
                            if (volumeService != null) {
                                throw new IllegalStateException("internal problem with volume starting logic");
                            }
                            volumeService = new VolumeControllerService(serviceFactory, amixer);
                        } else {
                            volumeService.stop();
                            volumeService = null;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        content.setVolumeControllerEnabled(true, "");
                    }
                }
            }).start();
        }
    };
    private ChatService chatService;
    private ActionListener chatExportListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            final ButtonModel model = content.getExportChatModel();
            final boolean startIt = model.isSelected();
            content.setExportChatEnabled(false, (startIt ? "starting" : "stopping") + " service");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (startIt) {
                            if (chatService != null) {
                                throw new IllegalStateException("internal problem with chat starting logic");
                            }
                            chatService = new ChatService(serviceFactory, content.getChatAlias());
                        } else {
                            chatService.stop();
                            chatService = null;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        content.setExportChatEnabled(true, "");
                    }
                }
            }).start();
        }
    };
//    private ChangeListener speechExportListener = new ChangeListener() {
//        public void stateChanged(ChangeEvent e) {
//            ButtonModel model = content.getExportSpeechModel();
//        }
//    };
    private Runnable addExportDisplay = new Runnable() {
        public void run() {
            try {
                int w = content.getExportDisplayWidth();
                int h = content.getExportDisplayHeight();
                boolean exportDisplay = content.getExportDisplay();
                boolean exportMouse = content.getExportMouse();
                final DesktopExporterFrame exporter = new DesktopExporterFrame(serviceFactory, w, h, exportDisplay, exportMouse);
                AbstractAction kill = new AbstractAction("stop " + w + "x" + h + " " + bo(exportDisplay) + " " + bo(exportMouse)) {
                    public void actionPerformed(ActionEvent e) {
                        exporter.stop();
                        content.removeDisplayAction(this);
                    }
                };
                content.addDisplayAction(kill);
            } catch (IOException ex) {
                Logger.getLogger(ComputerExportManager.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        private String bo(boolean booleanValue) {
            return booleanValue ? "✔" : ".";
        }
    };

    private static String findInPath(String executableName) {
        for (String dir : System.getenv("PATH").split(File.pathSeparator)) {
            File exe = new File(dir, executableName);
            if (exe.exists() && exe.canExecute()) {
                return exe.getAbsolutePath();
            }
        }
        return null;
    }
}
