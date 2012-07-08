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
public class Main {

    public static void main(String[] args) {
        TicTacToeService service = new TicTacToeService();
        TicTacToe ttt = new TicTacToe(service);
        service.setGame(ttt);
    }
}
