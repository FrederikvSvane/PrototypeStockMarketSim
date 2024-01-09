package dk.dtu;

import java.util.UUID;

public class SellBot extends Trader{

    public SellBot(String hostIp, int hostPort) {
        super(hostIp, hostPort);
    }

        public void run() {
            while (true) {
                try {
                    String orderType = "sell";
                    Order order = new Order(traderId, "Apple", 1, 1);
                    int amount = 2000;
                    for (int i = 0; i < amount; i++) {
                        sendOrderToBroker(orderType, order);
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error in trader");
                }
            }
        }
}
