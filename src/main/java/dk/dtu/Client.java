package dk.dtu;

import java.util.Scanner;

public class Client {
    private int amountOfTraders = 1;


    public static void main(String[] args) {
        HostUtil.initialize();
        ClientUtil.initialize();
        SellBot sellBot = new SellBot();
        new Thread(sellBot).start();
    }
}
