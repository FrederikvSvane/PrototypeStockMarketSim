package dk.dtu;

public class BotTrader extends Trader implements Runnable {
    private String botType;

    public BotTrader(String botType) {
        super();
        this.botType = botType;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (botType.equals("buy")) {
                    String orderType = "buy";
                    Order order = new Order(super.getTraderId(), "Apple", "AAPL", 2000, 550);
                    int amount = 1;
                    for (int i = 0; i < amount; i++) {
                        super.sendOrderToBroker(orderType, order);
                        Thread.sleep(10);
                    }
                } else if (botType.equals("sell")) {
                    String orderType = "sell";
                    Order order = new Order(super.getTraderId(), "Apple", "AAPL", 1000, 550);
                    int amount = 1;
                    for (int i = 0; i < amount; i++) {
                        super.sendOrderToBroker(orderType, order);
                        Thread.sleep(10);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * This method is used to evaluate the market and send an order to the broker
     * Inputtet skal være data omkring markedet
     * Outputtet skal være en ordre til broker eller do nothing
     */
    private void evalMarketAndSendOrder() {
        //Kig på data og træf et valg omkring indhold af ordre
    }
}
