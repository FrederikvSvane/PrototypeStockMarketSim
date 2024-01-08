package dk.dtu;

import org.jspace.*;

import java.io.IOException;
import java.util.*;

public class Broker implements Runnable {

    String brokerUuid;
    SequentialSpace requestSpace;
    String hostIp;
    int hostPort;
    String hostUri;

    public Broker(String hostIp, int hostPort) {
        this.brokerUuid = UUID.randomUUID().toString();
        this.requestSpace = new SequentialSpace();
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    private void setHostUri(String companyName) {
        this.hostUri = "tcp://" + hostIp + ":" + hostPort + "/" + companyName + "?keep";
    }

    public Space getRequestSpace() {
        return requestSpace;
    }

    public void run() {
        while (true) {
            try {
                // UUID "buy/sell" order
                Object[] request = requestSpace.get(new ActualField(brokerUuid), new FormalField(String.class), new FormalField(Order.class));
                Order order = (Order) request[2];
                String companyTicker = order.getTicker();
                setHostUri(companyTicker);


                switch (request[1].toString()) {
                    case "buy":
                        //Query all sell orders of the specific company and sort the results from lowest to highest price
                        List<Object[]> query = querySellOrdersCompanySpace();
                        ArrayList<CompanySellOrder> sortedSellOrders = sortSellOrders(query);
                        if (sortedSellOrders.size() == 0) {
                            System.out.println("No sell orders found for company: " + companyTicker);
                            break;
                        }
                        float priceMaxBid = order.getPrice();
                        int currentAmountBought = 0;
                        int maxAmountWanted = order.getAmount();
                        for (CompanySellOrder sellOrder : sortedSellOrders) {
                            int amountRemaning = maxAmountWanted - currentAmountBought;
                            if (sellOrder.getPrice() <= priceMaxBid || !(currentAmountBought >= maxAmountWanted)) { //TODO burde være ==, ikke >=, men vi skriver det alligevel
                                //Her har vi fundet en salgsorder, som udbyder til en pris som vi gerne vil give
                                //Vi skal så handle ud fra hvor mange aktier, som ordren udbyder
                                int sellOrderStockAmount = sellOrder.getAmount();
                                if (sellOrderStockAmount <= amountRemaning) {
                                    currentAmountBought += buyEntireOrder(sellOrder);
                                } else if (sellOrderStockAmount > amountRemaning) {
                                    buyPartialOrder(sellOrder, amountRemaning);
                                }
                            } else{
                                System.out.println("Couldnt not meet order at price: " + priceMaxBid);
                            }
                        }

                        break;
                    case "sell":
                        // Get company ticker from order and set hostUri
                        RemoteSpace companySpace = new RemoteSpace(hostUri);
                        // send to host
                        companySpace.put((String) request[0], (String) request[1], (Order) request[2]);
                        return;
                    default:
                        System.out.println("Broker " + request[0].toString() + " received unknown order" + request[2].toString());
                        break;
                }

                // This will terminate the broker
                return;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in broker");
            }
        }

    }

    private List<Object[]> querySellOrdersCompanySpace() {
        return requestSpace.queryAll(new FormalField(String.class), new FormalField(String.class), new ActualField("sell"), new FormalField(Order.class));
    }

    private ArrayList<CompanySellOrder> sortSellOrders(List<Object[]> sellOrders) {
        //Add all sell orders to sortedSellOrders
        ArrayList<CompanySellOrder> sortedSellOrders = new ArrayList<>();
        for (Object[] sellOrder : sellOrders) {
            Order order = (Order) sellOrder[3];
            String traderId = order.getTraderId();
            String brokerId = (String) sellOrder[0];
            String orderId = order.getOrderId();
            String companyName = order.getStockName();
            int amount = order.getAmount();
            float price = order.getPrice();
            sortedSellOrders.add(new CompanySellOrder(traderId, brokerId, orderId, companyName, amount, price));
        }

        //Sort sell orders by price
        Collections.sort(sortedSellOrders, Comparator.comparing(CompanySellOrder::getPrice));
        return sortedSellOrders;
    }

    private int buyEntireOrder(CompanySellOrder companySellOrder) throws IOException, InterruptedException {
        RemoteSpace companySpace = new RemoteSpace(hostUri);
        //TODO her bruges getp. Måske skal det laves om til en ticket mechanic?
        Object[] result = companySpace.getp(new FormalField(String.class), new ActualField(companySellOrder.getOrderId()), new FormalField(String.class), new FormalField(Order.class));
        if (result != null) {
            Order order = (Order) result[3];
            return order.getAmount();
        } else {
            System.out.println("Recieved result from company space was null");
            return 0;
        }
    }

    private void buyPartialOrder(CompanySellOrder companySellOrder, int amountWanted) throws IOException, InterruptedException {
        //get order, update amount of order, put it back
        RemoteSpace companySpace = new RemoteSpace(hostUri);
        Object[] result = companySpace.getp(new FormalField(String.class), new ActualField(companySellOrder.getOrderId()), new FormalField(String.class), new FormalField(Order.class));
        if (result != null) {
            Order order = (Order) result[3];
            order.setAmount(order.getAmount() - amountWanted);
            companySpace.put((String) result[0], (String) result[1], (String) result[2], order);
        } else {
            System.out.println("Recieved result from company space was null");
        }
    }
}