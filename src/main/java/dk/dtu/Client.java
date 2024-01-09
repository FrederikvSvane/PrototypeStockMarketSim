package dk.dtu;

import java.util.Scanner;

public class Client {
    int amountOfTraders = 1;
     static String lobbyToTraderName = "lobbyToTrader";
     static String traderToLobbyName = "traderToLobby";

    public static void main(String[] args) {
        Trader trader = new Trader("10.209.74.151", 32989, traderToLobbyName, lobbyToTraderName);
        new Thread(trader).start();
    }
}
