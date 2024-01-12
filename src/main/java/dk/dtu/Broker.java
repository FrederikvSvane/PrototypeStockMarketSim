package dk.dtu;

import org.jspace.*;

import java.io.IOException;
import java.util.*;

public class Broker implements Runnable {

    private String brokerId;
    private SequentialSpace requestSpace;
    private String uriConnection;

    public Broker() {
        this.brokerId = UUID.randomUUID().toString();
        this.requestSpace = new SequentialSpace();
    }


    public Space getRequestSpace() {
        return requestSpace;
    }

    public void run() {
        while (true) { //TODO den skal måske ikke have et while overhovedet da det ikke bliver brugt
            try {
                // UUID "buy/sell" order
                Object[] request = requestSpace.get(new FormalField(String.class) /*traderId*/, new FormalField(String.class) /*orderId*/, new FormalField(String.class)/*orderType*/, new FormalField(Order.class)/*Order*/);
                Order order = (Order) request[3];

                String companyTicker = order.getTicker();
                String uri = ClientUtil.getHostUri(companyTicker);
                uriConnection = ClientUtil.setConnectType(uri,"keep");

                String orderType = request[2].toString();
                switch (orderType) {
                    case "buy":
                        //Query all sell orders of the specific company and sort the results from lowest to highest price
                        List<Object[]> query = querySellOrdersCompanySpace(); //TODO måske queryp?
                        ArrayList<Order> sortedSellOrders = sortSellOrders(query);
                        if (sortedSellOrders.isEmpty()) {
                            System.out.println("No sell orders found for company: " /* + companyTicker*/);
                            break;
                        }
                        float priceMaxBid = order.getPrice();
                        int currentAmountBought = 0;
                        int maxAmountWanted = order.getAmount();
                        for (Order sellOrder : sortedSellOrders) {
                            int amountRemaning = maxAmountWanted - currentAmountBought;
                            if (sellOrder.getPrice() <= priceMaxBid || !(currentAmountBought >= maxAmountWanted)) { //TODO burde være ==, ikke >=, men vi skriver det alligevel. Kan throw error her
                                //Her har vi fundet en salgsorder, som udbyder til en pris som vi gerne vil give
                                //Vi skal så handle ud fra hvor mange aktier, som ordren udbyder
                                int sellOrderStockAmount = sellOrder.getAmount();
                                if (sellOrderStockAmount <= amountRemaning) {
                                    currentAmountBought += buyEntireOrder(sellOrder);
                                    //TODO overfør så her penge og aktier fra køber til sælger
                                } else if (sellOrderStockAmount > amountRemaning) {
                                    //TODO overfør så her penge og aktier fra køber til sælger
                                    buyPartialOrder(sellOrder, amountRemaning);
                                }
                            } else{
                                System.out.println("Couldnt not meet order at price: " + priceMaxBid);
                            }
                        }
                        return;
                    case "sell":
                        // Get company ticker from order and set hostUri
                        RemoteSpace companySpace = new RemoteSpace(uriConnection);
                        // send to host
                        companySpace.put((String) request[0], (String) request[1], (String) request[2], (Order) request[3]);
                        System.out.println("Sell order sent to company space");
                        return;
                    default:
                        System.out.println("Broker " + request[0].toString() + " received unknown order" + request[2].toString());
                        break;
                }

                // This will terminate the broker
                return; //TODO potential error i de her return/break statements
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in broker");
            }
        }

    }

    private List<Object[]> querySellOrdersCompanySpace() throws IOException, InterruptedException {
        RemoteSpace companySpace = new RemoteSpace(uriConnection);
        List<Object[]> result = companySpace.queryAll(new FormalField(String.class), new FormalField(String.class), new ActualField("sell"), new FormalField(Order.class));
        return result;
    }

    private ArrayList<Order> sortSellOrders(List<Object[]> sellOrders) {
        //Add all sell orders to sortedSellOrders
        ArrayList<Order> sortedSellOrders = new ArrayList<>();
        for (Object[] sellOrder : sellOrders) {
            Order order = (Order) sellOrder[3];
            String traderId = order.getTraderId();
            String orderId = order.getOrderId();
            String companyName = order.getStockName();
            String companyTicker = order.getTicker();
            int amount = order.getAmount();
            float price = order.getPrice();
            sortedSellOrders.add(new Order(traderId, orderId, companyName, companyTicker, amount, price));
        }

        //Sort sell orders by price
        sortedSellOrders.sort(Comparator.comparing(Order::getPrice));
        return sortedSellOrders;
    }

    private int buyEntireOrder(Order sellOrder) throws IOException, InterruptedException {
        RemoteSpace companySpace = new RemoteSpace(uriConnection);
        //TODO her bruges getp. Måske skal det laves om til en ticket/lock mechanic?
        Object[] result = companySpace.getp(new FormalField(String.class), new ActualField(sellOrder.getOrderId()), new FormalField(String.class), new FormalField(Order.class));
        if (result != null) {
            Order order = (Order) result[3];
            return order.getAmount();
        } else {
            System.out.println("Recieved result from company space was null");
            return 0;
        }
    }

    private void buyPartialOrder(Order sellOrder, int amountWanted) throws IOException, InterruptedException {
        //get order, update amount of order, put it back
        RemoteSpace companySpace = new RemoteSpace(uriConnection);
        Object[] result = companySpace.getp(new FormalField(String.class), new ActualField(sellOrder.getOrderId()), new FormalField(String.class), new FormalField(Order.class));
        if (result != null) {
            Order order = (Order) result[3];
            order.setAmount(order.getAmount() - amountWanted);
            companySpace.put((String) result[0], (String) result[1], (String) result[2], order);
        } else {
            //TODO throw exception istedet
            System.out.println("Recieved result from company space was null");
        }
    }

    public String getBrokerId() { return brokerId; }
}