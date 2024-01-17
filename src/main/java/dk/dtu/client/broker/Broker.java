package dk.dtu.client.broker;

import dk.dtu.client.ClientUtil;
import dk.dtu.host.HostUtil;
import dk.dtu.client.Order;
import dk.dtu.host.bank.Transaction;
import org.jspace.*;

import java.io.IOException;
import java.util.*;

public class Broker implements Runnable {

    private String brokerId;
    private String traderId; //who broker is working for
    private SequentialSpace requestSpace;
    private String uriConnection;
    private int portBank = HostUtil.getBankPort();
    private RemoteSpace transactionsSpace;
    private RemoteSpace transactionResponseSpace;

    public Broker() throws IOException {
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
                transactionsSpace = new RemoteSpace(ClientUtil.getHostUri("transactionSpace", portBank, "keep"));
                transactionResponseSpace = new RemoteSpace(ClientUtil.getHostUri("transactionResponseSpace", portBank, "keep"));
                String companyTicker = order.getTicker();
                String uri = ClientUtil.getHostUri(companyTicker);
                uriConnection = ClientUtil.setConnectType(uri, "keep");

                traderId = request[0].toString();

                String orderType = request[2].toString();
                Transaction transaction;
                String responseString;

                switch (orderType) {
                    case "buy":
                        // Ask bank to reserve money for order
                        // {traderId, price, amount}
                        // response BrokerId, "reserved money"/"not enough money"
                        transaction = new Transaction(traderId, order.getAmount(), order.getPrice());
                        float moneyReserved = order.getPrice() * order.getAmount();
                        responseString = sendAndReceiveRequest("reserve money", transaction);
                        float moneyUsed = 0;

                        if (responseString.equals("reserved money")) {
                            //Query all sell orders of the specific company and sort the results from lowest to highest price
                            List<Object[]> query = querySellOrdersCompanySpace(); //TODO måske queryp?
                            ArrayList<Order> sortedSellOrders = sortSellOrders(query);
                            if (sortedSellOrders.isEmpty()) {
                                System.out.println("No sell orders found for company: " + companyTicker);
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
                                        // price to take out and
                                        int boughtAmount = buyEntireOrder(sellOrder);
                                        moneyUsed += boughtAmount * sellOrder.getPrice();
                                        currentAmountBought += boughtAmount;
                                    } else if (sellOrderStockAmount > amountRemaning) {
                                        int boughtAmount = buyPartialOrder(sellOrder, amountRemaning);
                                        moneyUsed += boughtAmount * sellOrder.getPrice();
                                        currentAmountBought += boughtAmount;
                                    }
                                } else {

                                    System.out.println("Couldnt not meet order at price: " + priceMaxBid);
                                }
                            }
                        } else {
                            System.out.println("not enough money");
                        }


                        // Reserved amount of money minus used amount of money = amount of money to return to trader account
                        float moneyToReturn = moneyReserved - moneyUsed;
                         // Ask bank to unreserve money for order
                        // {traderId, price}
                        transaction = new Transaction(traderId, moneyToReturn);
                        responseString = sendAndReceiveRequest("unreserve money", transaction);

                        if (responseString.equals("unreserved money")) {
                            System.out.println("Money returned to trader account");
                        } else {
                            System.out.println("Money not returned to trader account");
                        }

                        return;
                    case "sell":
                        // Ask bank to reserve money for order
                        // {traderId, companyTicker, amount}

                        transaction = new Transaction(traderId, order.getTicker(), order.getAmount());
                        responseString = sendAndReceiveRequest("reserve stocks", transaction);

                        if (responseString.equals("stocks reserved")) {
                            // Send order to company space
                            RemoteSpace companySpace = new RemoteSpace(uriConnection);
                            // TraderId, OrderId, OrderType, Order, reservedAmount
                            companySpace.put((String) request[0], (String) request[1], (String) request[2], (Order) request[3], 0);
                            System.out.println("Sell order sent to company space");
                        } else {
                            System.out.println(responseString);
                        }
                        return;
                    case "establish account":
                        // Ask bank to establish account
                        // {traderId}
                        transaction = new Transaction(traderId);
                        responseString = sendAndReceiveRequest("establish account", transaction);
                        System.out.println(responseString);
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

    public String sendAndReceiveRequest(String command, Transaction transaction) throws InterruptedException {
        transactionsSpace.put(brokerId, command, transaction);
        Object[] bankResponse = transactionResponseSpace.get(new ActualField(brokerId), new FormalField(String.class));
        return (String) bankResponse[1];
    }

    private List<Object[]> querySellOrdersCompanySpace() throws IOException, InterruptedException {
        RemoteSpace companySpace = new RemoteSpace(uriConnection);
        List<Object[]> result = companySpace.queryAll(new FormalField(String.class), new FormalField(String.class), new ActualField("sell"), new FormalField(Order.class), new FormalField(Integer.class));
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
            int reservedAmount = (int) sellOrder[4];
            int amount = order.getAmount();
            float price = order.getPrice();
            sortedSellOrders.add(new Order(traderId, orderId, companyName, companyTicker, amount, price));
        }

        //Sort sell orders by price
        sortedSellOrders.sort(Comparator.comparing(Order::getPrice));
        return sortedSellOrders;
    }

    private int buyEntireOrder(Order sellOrder) throws IOException, InterruptedException {
        // get ticket from companyspace
        RemoteSpace companySpace = new RemoteSpace(uriConnection);
        Object[] ticket = companySpace.get(new ActualField("ticket"));
        // TraderId, OrderId, OrderType, Order, reservedAmount
        Object[] result = companySpace.getp(new FormalField(String.class), new ActualField(sellOrder.getOrderId()), new FormalField(String.class), new FormalField(Order.class), new FormalField(Integer.class));
        if (result != null) {
            Order order = (Order) result[3];
            int amount = order.getAmount();
            int reservedAmount = (int) result[4];
            int possibleAmount = amount - reservedAmount;
            reservedAmount += possibleAmount;
            companySpace.put((String) result[0], (String) result[1], (String) result[2], order, reservedAmount);
            companySpace.put("ticket");
            //Give order to bank
            Transaction transaction = new Transaction(traderId, order.getTraderId(), order.getTicker(), order.getOrderId(), possibleAmount);
            String responseString = sendAndReceiveRequest("finalize transaction", transaction);
            if (responseString.equals("completed order")) {
                return possibleAmount;
            } else {
                System.out.println("order not found"); // dette burde ikke kunne ske
                return 0;
            }

        } else {
            companySpace.put("ticket");
            System.out.println("Recieved result from company space was null");
            return 0;
        }
    }

    private int buyPartialOrder(Order sellOrder, int amountWanted) throws IOException, InterruptedException {
        // get ticket from companyspace
        RemoteSpace companySpace = new RemoteSpace(uriConnection);
        Object[] ticket = companySpace.get(new ActualField("ticket"));
        // TraderId, OrderId, OrderType, Order, reservedAmount
        Object[] result = companySpace.getp(new FormalField(String.class), new ActualField(sellOrder.getOrderId()), new FormalField(String.class), new FormalField(Order.class), new FormalField(Integer.class));
        if (result != null) {
            Order order = (Order) result[3];
            int amount = order.getAmount();
            int reservedAmount = (int) result[4];
            if (amountWanted > amount - reservedAmount) {
                System.out.println("Not enough stocks to buy");
                return 0;
            }
            reservedAmount += amountWanted;
            companySpace.put((String) result[0], (String) result[1], (String) result[2], order, reservedAmount);
            companySpace.put("ticket");
            //Give order to bank
            transactionsSpace.put(brokerId, "buy", new Object[]{traderId, order.getPrice(), amount});
            // get response from bank
            Object[] bankResponse = transactionResponseSpace.get(new ActualField(brokerId), new FormalField(String.class));
            String responseString = (String) bankResponse[1];
            if (responseString.equals("reserved money")) {
                return amount;
            } else {
                System.out.println("not enough money");
                return 0;
            }

        } else {
            companySpace.put("ticket");
            System.out.println("Recieved result from company space was null");
            return 0;
        }

    }

    public String getBrokerId() {
        return brokerId;
    }
}