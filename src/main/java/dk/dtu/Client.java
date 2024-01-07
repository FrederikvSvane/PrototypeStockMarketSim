package dk.dtu;

import java.util.Scanner;

public class Client {
    int amountOfTraders = 1;

    public static void main(String[] args) {
        Trader trader = new Trader("localhost", 8080);
        new Thread(trader).start();
    }

}
