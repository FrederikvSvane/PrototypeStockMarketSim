package dk.dtu;

import java.util.Scanner;

public class Client {
    int amountOfTraders = 1;

    public static void main(String[] args) {
        SellBot sellBot = new SellBot("10.209.74.151", 32989);
        new Thread(sellBot).start();
    }
}
