package dk.dtu;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.jspace.*;


public class Trader extends DistributedClient implements Runnable{
    String traderId;
    String hostIp;
    String traderToLobbyName;
    String lobbyToTraderName;
    RemoteSpace traderToLobby;
    RemoteSpace lobbyToTrader;
    int hostPort;

    public Trader(String hostIp, int hostPort, String traderToLobbyName, String lobbyToTraderName) {
        this.traderId = UUID.randomUUID().toString();
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.traderToLobbyName = traderToLobbyName;
        this.lobbyToTraderName = lobbyToTraderName;
        try {
            this.traderToLobby = new RemoteSpace(createUri(hostIp, hostPort, this.traderToLobbyName));
            this.lobbyToTrader = new RemoteSpace(createUri(hostIp, hostPort, this.lobbyToTraderName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            String mode = chooseMode();

            switch(mode){
                case "trade":{
                    try {
                        consoleInputToBuyOrder();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error in trader");
                    }
                }
                case "chat": {
                    try {
                        chatMenu();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Error in trader");
                    }
                }
            }
        }
    }



    private void sendOrderToBroker(String orderType, Order order) throws IOException, InterruptedException {
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
        System.out.println("Sell order sent to broker");
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
    public String chooseMode(){
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Choose mode: \n1. Create trade \n2. Open chat");
        String mode = terminalIn.nextLine();
        if(mode.equals("1")){
            return "trade";
        }
        else if(mode.equals("2")){
            return "chat";
        }
        else{
            System.out.println("Invalid input");
            return chooseMode();
        }
    }

    public void sendCreateChatProtocol() throws Exception {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Enter room name: ");
        String roomName = terminalIn.nextLine();
        System.out.println("Enter password: ");
        String password = terminalIn.nextLine();
        System.out.println("Enter maxCapacity): ");
        int capacity = Integer.parseInt(terminalIn.nextLine());

        if(capacity <= 0)
        {
            System.out.println("Capacity was equal to or below 0, so it is set to 1.");
            capacity = 1;
        }

        traderToLobby.put(traderId, "create chat");
        traderToLobby.put(traderId, roomName, password, capacity);
        Object[] roomCreationAnswer = lobbyToTrader.get(new ActualField(traderId), new FormalField(String.class), new FormalField(String.class));
        System.out.println("We got the response:" + roomCreationAnswer[0].toString() + roomCreationAnswer[1].toString() + roomCreationAnswer[2].toString());
    }

    public String sendJoinChatProtocol() throws Exception
    {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Enter room name: ");
        String roomName = terminalIn.nextLine();
        System.out.println("Enter password: ");
        String password = terminalIn.nextLine();

        traderToLobby.put(traderId, roomName, password);
        Object[] response = lobbyToTrader.get(new ActualField(traderId), new FormalField(String.class));
        String responseMessage = (String) response[1];
        System.out.println("We got the response: " + responseMessage);
        return responseMessage;
    }


    public Object[] chatMenu() throws Exception {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Choose mode: \n1. Create chat \n2. Get an overview\n3. Join a chat");
        String mode = terminalIn.nextLine();
        if(mode.equals("1")){
            sendCreateChatProtocol();
        }
        else if(mode.equals("2")){
            traderToLobby.put(traderId, "show rooms");
            Object[] roomOverview = lobbyToTrader.get(new ActualField(traderId), new FormalField(String[].class), new FormalField(int.class));
            System.out.println("Following rooms are open:" + roomOverview[0].toString() + roomOverview[1].toString());
            return roomOverview;
        }
        else if(mode.equals("3"))
        {
            traderToLobby.put(traderId, "join");
            String responseMessage = sendJoinChatProtocol();
            if(responseMessage.equals("Create room it doesn't exist")){
                System.out.println("Whatever");
                sendCreateChatProtocol();
            }

        }
        else{
            System.out.println("Invalid input, please press the number of the option you want to choose");
            return chatMenu();
        }
        return null;
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

        tickerMap.put("Apple", "AAPL");
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

