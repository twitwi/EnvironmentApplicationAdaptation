/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.tictactoe;

import fr.prima.omiscid.user.connector.ConnectorListener;
import fr.prima.omiscid.user.connector.Message;
import fr.prima.omiscid.user.service.Service;
import fr.prima.omiscid.user.util.Utility;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author langet, emonet
 */
public class TicTacToe {

    private String player1Name = "tutu";
    private String player2Name = "toto";
    private int player1Score = 0;
    private int player2Score = 0;
    private int[] hits;
    private int currentWin = -1; // -1, 0 (draw), 1 (p1), 2 (p2)
    private List<Integer> currentWinList = null;
    private int waitAction = 1;
    private TicTacToeService service;

    public TicTacToe(TicTacToeService ser) {
        hits = new int[9];
        service = ser;
        service.getService().addConnectorListener("model", new ConnectorListener() {
            public void messageReceived(Service service, String localConnectorName, Message message) {
            }

            public void disconnected(Service service, String localConnectorName, int peerId) {
            }

            public void connected(Service service, String localConnectorName, int peerId) {
                outputModel();
                // unfortunately, the adapter connects before having its client ...
                // should do some statefull adapters that transform what they receive in another form that is kept and send on new connections (transformation could be lazy until a client is here)
            }
        });
    }

    public void init() {
        for (int i = 0; i < 9; i++) {
            hits[i] = 0;
        }
    }

    public int check(int player, int deb, int fin, int pas) {
        int win = 1;
        for (int i = deb; i < fin; i += pas) {
            if (hits[i] != player) {
                win = 0;
            }
        }
        return win;
    }

    public synchronized void action(int player, int cell) {
        if (player == 3) { // joker player, plays for both
            player = waitAction;
        }
        if (hits[cell] != 0 || waitAction != player) {
        } else {
            hits[cell] = player;
            waitAction = (player % 2) + 1;
            int deb = (cell / 3) * 3;
            int fin = deb + 3;
            int pas = 1;
            // horizontal check
            int win = check(player, deb, fin, pas);
            if (win == 0) {
                win = 1;
                deb = cell % 3;
                fin = 9;
                pas = 3;
                // vertical check
                win = check(player, deb, fin, pas);
            }
            if (win == 0) {
                if ((cell % 2) == 0) {
                    win = 1;
                    deb = cell % 4; // 0 or 2
                    fin = 7;
                    pas = 2;
                    if (deb == 2) // single (tr-bl) diagonal to check
                    {
                        win = check(player, deb, fin, pas);
                    } else {
                        fin = 9;
                        pas = 4;
                        // check tl-br diagonal
                        win = check(player, deb, fin, pas);
                        if (win == 0 && cell == 4) {
                            win = 1;
                            deb = 2;
                            fin = 7;
                            pas = 2;
                            // check tr-bl diagonal
                            win = check(player, deb, fin, pas);
                        }
                    }
                }
            }

            String msg = "<action player=\"" + player + "\" cell=\"" + cell + "\" />";
            service.getService().sendToAllClients("output", Utility.message(msg));
            if (win == 1) {
                ArrayList<Integer> listhits = new ArrayList<Integer>();
                for (int i = deb; i < fin; i += pas) {
                    listhits.add(i);
                }
                gameWin(player, listhits);
            } else {
                int nbcoup = 0;
                for (int i = 0; i < 9; i++) {
                    if (hits[i] != 0) {
                        nbcoup++;
                    }
                }
                if (nbcoup == 9) {
                    gameDraw();
                } else {
                    outputModel();
                }
            }
        }
    }

    public void gameWin(int player, ArrayList<Integer> listhits) {
        currentWin = player;
        currentWinList = listhits;
        outputModel();
        String msg = "<end win=\"" + player + "\" hits=\"" + listhits.toString() + "\" />";
        service.getService().sendToAllClients("output", Utility.message(msg));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TicTacToe.class.getName()).log(Level.SEVERE, null, ex);
        }
        init();
        currentWin = -1;
        currentWinList = null;
        outputModel();
    }

    public void gameDraw() {
        currentWin = 0;
        outputModel();
        String msg = "<end win=\"-1\" hits=\"0\" />";
        service.getService().sendToAllClients("output", Utility.message(msg));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TicTacToe.class.getName()).log(Level.SEVERE, null, ex);
        }
        init();
        currentWin = -1;
        currentWinList = null;
        outputModel();
    }

    public void outputModel() {
        String modelMessage = "";
        modelMessage += "<tictactoe ";
        modelMessage += " win='" + currentWin + "'";
        if (currentWinList != null) {
            modelMessage += " winList='" + join(currentWinList, " ") + "'";
        }
        for (int n = 0; n < hits.length; n++) {
            modelMessage += " c" + n + "='" + hits[n] + "'";
        }
        modelMessage += "/>";
        service.getService().sendToAllClients("model", Utility.message(modelMessage));
    }

    private String join(List<? extends Object> list, String separator) {
        StringBuilder res = null;
        for (Object o : list) {
            if (res == null) {
                res = new StringBuilder(o.toString());
            } else {
                res.append(separator).append(o.toString());
            }
        }
        return res.toString();
    }
}
