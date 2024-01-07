package dk.dtu;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import org.jspace.*;

import static java.util.UUID.fromString;

public class Trader implements Runnable {
    UUID traderUuidUri;
    String hostIp;
    int hostPort;

    public Trader(String hostIp, int hostPort){
        this.traderUuidUri = UUID.randomUUID();
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
        UUID brokerUuid = UUID.randomUUID();
        Broker broker = new Broker(brokerUuid);
        new Thread(broker).start();

        if (orderType.equals("buy")){
            sendBuyOrder(traderUuidUri, broker, order);
        }
        else if (orderType.equals("sell")){
            sendSellOrder(traderUuidUri, broker, order);
        }
    }

    public void sendBuyOrder(UUID traderUuidUri, Broker broker, Order order) throws IOException, InterruptedException {
        Space brokerSpace = broker.getSpace();
        brokerSpace.put(traderUuidUri, "buy", order);
        //TODO get response of order completion result from broker here?
    }

    public void sendSellOrder(UUID traderUuidUri, Broker broker, Order order) throws IOException, InterruptedException {
        Space brokerSpace = broker.getSpace();
        brokerSpace.put(traderUuidUri, "sell", order);
        //TODO get response of order completion result from broker here?
    }

    private void consoleInputToBuyOrder() throws IOException, InterruptedException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Enter order with format {buy/sell, stock name, amount, price}: ");
        String orderString = terminalIn.nextLine();
        String[] orderParts = orderString.split(",");
        String orderType = orderParts[0];
        String stockName = orderParts[1];
        int amount = Integer.parseInt(orderParts[2]);
        float price = Float.parseFloat(orderParts[3]);
        Order order = new Order(stockName, amount, price);
        sendOrderToBroker(orderType, order);
    }









}

class Order {
    String stockName;
    int amount;
    float price;

    public Order(String stockName, int amount, float price) {
        this.stockName = stockName;
        this.amount = amount;
        this.price = price;
    }
}
