package dk.dtu.client;

import dk.dtu.host.HostUtil;
import dk.dtu.client.trader.HumanTrader;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        HostUtil.initialize();
        ClientUtil.initialize();

        HumanTrader humanTrader = new HumanTrader();
        new Thread(humanTrader).start();
    }
}

