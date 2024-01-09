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

    public void sendOrderToBroker(String orderType, Order order) throws IOException, InterruptedException {
        Broker broker = new Broker(hostIp, hostPort);
        new Thread(broker).start();

        //TODO det skal faktisk bare sendes til brokeren, og så skal den sende det videre til exchange
        //TODO så både sendBuyOrder og sendSellOrder skal ligge inde i Broker.java

        if (orderType.equals("buy")) {
            sendBuyOrder(traderId, broker, order);
        } else if (orderType.equals("sell")) {
            sendSellOrder(traderId, broker, order);
        }
    }

    public void sendBuyOrder(String traderId, Broker broker, Order order) throws IOException, InterruptedException { //TODO fjern traderId fordi det allerede kendes i hele scope. Samme i metoden under
        Space requestSpace = broker.getRequestSpace();
        requestSpace.put(traderId, order.getOrderId(), "buy", order);
        //TODO get response of order completion result from broker here?
    }

    public void sendSellOrder(String traderId, Broker broker, Order order) throws IOException, InterruptedException {
        Space requestSpace = broker.getRequestSpace();
        requestSpace.put(traderId, order.getOrderId(), "sell", order);
        //TODO get response of order completion result from broker here?
    }

    public void consoleInputToBuyOrder() throws IOException, InterruptedException {
        Scanner terminalIn = new Scanner(System.in);
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
