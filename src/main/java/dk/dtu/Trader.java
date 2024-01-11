package dk.dtu;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

import org.jspace.*;

public class Trader {
    protected String traderId;
    protected String hostIp;
    protected SequentialSpace masterCompanyRegister;
    protected SequentialSpace companyPriceGraphs;


    protected int hostPort;

    public Trader() { //TODO lav en overklasse, som ikke har nogen argumenter, som kan nedarves til HumanTrader og BotTrader. Det er kun HumanTrader, som kan chatte
        this.traderId = UUID.randomUUID().toString();
        this.hostIp = HostUtil.getHostIp();
        this.hostPort = HostUtil.getHostPort();

        // List of all companies traded at exchange. Updated by datafetcher
        this.masterCompanyRegister = new SequentialSpace(/*companyId, companyName, companyTicker*/);

        // A space for the coordinates of the price graph of each company. Updated by datafetcher
        this.companyPriceGraphs = new SequentialSpace(/*companyName, companyTicker, QueueList<time, price>*/);
    }

    public void sendOrderToBroker(String orderType, Order order) throws IOException, InterruptedException {
        Broker broker = new Broker();
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

    public void makeDataFetchers() throws InterruptedException {
        NameDataFetcher nameDataFetcher = new NameDataFetcher(masterCompanyRegister);
        PriceGraphDataFetcher priceGraphDataFetcher = new PriceGraphDataFetcher(companyPriceGraphs);
        new Thread(nameDataFetcher).start();
        new Thread(priceGraphDataFetcher).start();
    }

    public void consoleInputToSendOrder() throws IOException, InterruptedException {
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

    public String getTraderId() {
        return traderId;
    }
}

