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

        if (orderType.equals("buy")) {
            sendBuyOrder(broker.getBrokerId(), broker, order);
        } else if (orderType.equals("sell")) {
            sendSellOrder(broker.getBrokerId(), broker, order);
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
