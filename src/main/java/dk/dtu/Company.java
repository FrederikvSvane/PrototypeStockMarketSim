package dk.dtu;

import java.io.IOException;
import java.util.UUID;
import dk.dtu.Broker;
import org.jspace.RemoteSpace;
import org.jspace.Space;

public class Company implements Runnable{
    private String companyId;
    private String companyName;
    private String companyTicker;
    private int amountOfStocks;
    private int amountOfNonTradedStocks;

    public Company(String companyName, String companyTicker) {
        this.companyId = UUID.randomUUID().toString();
        this.companyName = companyName;
        this.companyTicker = companyTicker;

    }

    @Override
    public void run() {
        //Lav ipo én gang
        Order order = makeOrder(100, 35);
        try {
            sendRequestToCompanyBroker("IPO", order);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void sendRequestToCompanyBroker(String orderType, Order order) throws InterruptedException {
        CompanyBroker companyBroker = new CompanyBroker();
        new Thread(companyBroker).start();
        companyBroker.getRequestSpace().put(orderType, this, order);
/*
        try {
            if (orderType.equals("IPO")) {
                sendIpoRequestToExchange(companyBroker, order);
            } else if (orderType.equals("buy")) {
                sendBuyOrder(companyId, companyBroker, order);
            } else if (orderType.equals("sell")) {
                sendSellOrder(companyId, companyBroker, order);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
 */
    }

    private void sendIpoRequestToExchange(CompanyBroker companyBroker, Order order) throws IOException, InterruptedException {
        String uri = ClientUtil.getHostUri("exchangeRequestSpace");  //TODO den skal have et rigtig room navn
        String uriConnection = ClientUtil.setConnectType(uri,"keep");
        //TODO er keep den rigtige forbindelse her? Og hvad med alle andre steder? STOR TODO
        Space exchangeRequestSpace = new RemoteSpace(uriConnection);
        exchangeRequestSpace.put(order.getOrderId());

    }

    private void sendBuyOrder(String companyId, CompanyBroker companyBroker, Order order) {
        return; //TODO samme logik som i Trader.java
    }

    private void sendSellOrder(String companyId, CompanyBroker companyBroker, Order order) {
        return; //TODO samme logik som i Trader.java
    }

    private Order makeOrder(int amount, float price) {
        return new Order(companyId, companyName,companyTicker, amount, price);
    }
    public String getCompanyName() { return companyName; }
    public String getCompanyTicker() { return companyTicker; }
    public String getCompanyId() { return companyId; }

}
