/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.computerexporter;

import fr.prima.omiscid.user.util.Utility;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatFrame {

    JFrame f;
    String identifier;
    JTextField input;
    JTextArea output;
    ChatSender sender;

    public ChatFrame(int peerId, ChatSender sender) {
        this.sender = sender;
        f = new JFrame();
        f.setLayout(new BorderLayout(10, 10));

        input = new JTextField();
        output = new JTextArea();
        output.setEditable(false);
        input.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && 0 != (KeyEvent.CTRL_DOWN_MASK & e.getModifiersEx())) {
                    sendMessage();
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        f.add(input, BorderLayout.AFTER_LAST_LINE);
        f.add(output, BorderLayout.CENTER);
        f.setPreferredSize(new Dimension(400, 700));
        setRemoteIdentifier("Peer " + Utility.intTo8HexString(peerId));
        f.pack();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.setVisible(true);
            }
        });
    }

    private void sendMessage() {
        sender.send(input.getText());
        input.setText("");
    }

    public synchronized void received(String message) {
        output.setText(output.getText() + "\n" + identifier + ": " + message + "\n");
        f.setVisible(true);
    }

    public synchronized void close() {
        output.setText("\n\n… Disconnected …");
        input.setEnabled(false);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public synchronized void setRemoteIdentifier(String remoteIdentifier) {
        this.identifier = remoteIdentifier;
        f.setTitle("Chatting with " + identifier);
    }
}
