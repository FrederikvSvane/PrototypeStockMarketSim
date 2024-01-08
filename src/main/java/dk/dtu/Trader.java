package dk.dtu;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.jspace.*;

public class Trader implements Runnable {
    String traderId;
    String hostIp;
    int hostPort;

    public Trader(String hostIp, int hostPort) {
        this.traderId = UUID.randomUUID().toString();
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public void run() {
        while (true) {
            try {
                consoleInputToBuyOrder();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in trader");
            }
        }
    }

    private void sendOrderToBroker(String orderType, Order order) throws IOException, InterruptedException {
        Broker broker = new Broker(hostIp, hostPort);
        new Thread(broker).start();

        if (orderType.equals("buy")) {
            sendBuyOrder(broker.brokerUuid, broker, order);
        } else if (orderType.equals("sell")) {
            sendSellOrder(broker.brokerUuid, broker, order);
        }
    }

    public void sendBuyOrder(String brokerUuid, Broker broker, Order order) throws IOException, InterruptedException {
        Space requestSpace = broker.getRequestSpace();
        requestSpace.put(brokerUuid, order.getOrderId(), "buy", order);
        //TODO get response of order completion result from broker here?
    }

    public void sendSellOrder(String brokerUuid, Broker broker, Order order) throws IOException, InterruptedException {
        Space requestSpace = broker.getRequestSpace();
        requestSpace.put(brokerUuid, order.getOrderId(), "sell", order);
        //TODO get response of order completion result from broker here?
    }

    private void consoleInputToBuyOrder() throws IOException, InterruptedException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Enter order with format {buy/sell, stock name, amount, price}: ");
        String orderString = terminalIn.nextLine();
        String[] orderParts = orderString.split(" ");
        String orderType = orderParts[0];
        String stockName = orderParts[1];
        int amount = Integer.parseInt(orderParts[2]);
        float price = Float.parseFloat(orderParts[3]);
        Order order = new Order(traderId, stockName, amount, price);
        sendOrderToBroker(orderType, order);
    }
}

class Order {
    private String orderId;
    private String traderId;
    private String stockName;
    private int amount;
    private float price;

    private HashMap<String, String> tickerMap = new HashMap<String, String>();

    public Order(String traderId, String stockName, int amount, float price) {
        this.orderId = UUID.randomUUID().toString();
        this.stockName = stockName;
        this.amount = amount;
        this.price = price;

        tickerMap.put("Apple", "AAPL");
    }

    public Order(String traderId, String orderId, String stockName, int amount, float price) {
        this.traderId = traderId;
        this.orderId = orderId;
        this.stockName = stockName;
        this.amount = amount;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Order{" +
                "stockName='" + stockName + '\'' +
                ", amount=" + amount +
                ", price=" + price +
                '}';
    }

    public float getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public String getStockName() {
        return stockName;
    }

    public String getTicker() {
        return tickerMap.get(stockName);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTraderId() { return traderId;
    }

    public void setAmount(int n) { amount = n; }
}

class CompanySellOrder extends Order {

    String brokerUUID;

    public CompanySellOrder(String traderId, String brokerUUID, String orderId, String companyName, int amount, float price) {
        super(traderId, orderId, companyName, amount, price);
        this.brokerUUID = brokerUUID;
    }

    public String getbrokerUUID() {
        return brokerUUID;
    }
}
