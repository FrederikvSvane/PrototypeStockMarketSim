package dk.dtu;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException {
        HostUtil.initialize();
        ClientUtil.initialize();

        HumanTrader humanTrader = new HumanTrader();
        new Thread(humanTrader).start();

        while (true) {
            try {
                Thread.sleep(1000);
                System.out.println(GlobalClock.getTimePassed());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

