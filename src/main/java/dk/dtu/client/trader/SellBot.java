package dk.dtu.client.trader;

import dk.dtu.client.Order;
import dk.dtu.client.trader.Trader;

public class SellBot extends Trader {

    public SellBot() throws InterruptedException {
        super();
    }

        public void run() {
            while (true) {
                try {
                    String orderType = "sell";
                    Order order = new Order(super.getTraderId(), "Apple","AAPL", 1, 1);
                    int amount = 2000;
                    for (int i = 0; i < amount; i++) {
                        super.sendOrderToBroker(orderType, order);
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error in trader");
                }
            }
        }
}
