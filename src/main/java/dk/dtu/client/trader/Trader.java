package dk.dtu.client.trader;

import java.net.URI;
import java.rmi.Remote;
import java.time.LocalDateTime;
import java.util.*;
import java.io.IOException;
import java.util.UUID;

import dk.dtu.client.broker.Broker;
import dk.dtu.client.datafetcher.FinancialTimes;
import dk.dtu.client.Order;
import dk.dtu.client.datafetcher.NameDataFetcher;
import dk.dtu.client.datafetcher.PriceGraphDataFetcher;
import org.jspace.*;

public class Trader {
    private String traderId;
    private SequentialSpace masterCompanyRegister;
    private SequentialSpace companyPriceGraphs;
    private SequentialSpace companyFundamentals;

    public Trader() throws InterruptedException, IOException { //TODO lav en overklasse, som ikke har nogen argumenter, som kan nedarves til HumanTrader og BotTrader. Det er kun HumanTrader, som kan chatte
        this.traderId = UUID.randomUUID().toString();

        // List of all companies traded at exchange. Updated by datafetcher
        this.masterCompanyRegister = new SequentialSpace(/*companyId, companyName, companyTicker*/);

        // A space for the coordinates of the price graph of each company. Updated by datafetcher
        this.companyPriceGraphs = new SequentialSpace(/*companyName, companyTicker, QueueList<time, price>*/);

        //A space for the fundamental data of companies
        this.companyFundamentals = new SequentialSpace(/*(String companyTicker, LocalDateTime simulatedGameTime , String financialStatement, String financialPost, float financialValue)*/);
        makeDataFetchers();
        sendOrderToBroker("establish account", new Order());
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


    public Object[] getFundamentalData(String companyTicker, String financialPost) throws InterruptedException {
        new Thread(new FinancialTimes(companyFundamentals,0,companyTicker,financialPost)).start();
        companyFundamentals.get(new ActualField("mail"));
        return companyFundamentals.getp(new ActualField(companyTicker),new FormalField(LocalDateTime.class), new FormalField(String.class), new ActualField(financialPost), new FormalField(Float.class));
    }

    public void makeDataFetchers() throws InterruptedException {
        NameDataFetcher nameDataFetcher = new NameDataFetcher(masterCompanyRegister);
        PriceGraphDataFetcher priceGraphDataFetcher = new PriceGraphDataFetcher(companyPriceGraphs);
        new Thread(nameDataFetcher).start();
        new Thread(priceGraphDataFetcher).start();
    }

    public String getTraderId() { return traderId; }
    public SequentialSpace getMasterCompanyRegister() { return masterCompanyRegister; }
    public SequentialSpace getCompanyPriceGraphs() { return companyPriceGraphs; }
}

