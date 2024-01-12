package dk.dtu;

import java.io.IOException;
import java.net.URI;
import java.rmi.Remote;
import java.util.*;

import org.jspace.*;

import static dk.dtu.Host.hostIp;
import static dk.dtu.Host.hostPort;


public class Trader extends DistributedClient implements Runnable{
    String traderId;
    String hostIp;
    int hostPort;
    RemoteSpace toLobby;
    RemoteSpace fromLobby;
    SequentialSpace connectedChats;
    RemoteSpace myMessages;

    public Trader(String hostIp, int hostPort) throws IOException {
        this.traderId = UUID.randomUUID().toString();
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        toLobby = new RemoteSpace("tcp://" + hostIp + ":" + hostPort + "/toLobby?keep");
        fromLobby = new RemoteSpace("tcp://" + hostIp + ":" + hostPort + "/fromLobby?keep");
        connectedChats = new SequentialSpace();
        myMessages = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + traderId + "?keep");
    }

    public void run() {
        try {
            openTraderMessages();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
                        // Chat menu tingen.
                        consoleInputToChat();
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

    public void consoleInputToChat() throws InterruptedException, IOException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("1. Create chat \n2. Get Overview \n3. Join Chat");
        String choiceInput = terminalIn.nextLine();

        switch(choiceInput){
            case "1":{ //Create Room
                createRoomOrder();
                break;
            }
            case "2":{ //Get overview
                getOverviewOrder();
                break;
            }
            case "3" :{ //Join room
                joinRoomOrder();
                break;
            }
            case "4" :{ //Send directly to trader

            }
        }
    }

    public void createRoomOrder() throws InterruptedException {
        Scanner terminalIn = new Scanner(System.in);

        System.out.println("Room name: ");
        String roomName = terminalIn.nextLine();
        System.out.println("Password: ");
        String password = terminalIn.nextLine();
        System.out.println("Max Capacity: ");
        int capacity = terminalIn.nextInt();

        toLobby.put(traderId, "create", roomName, password, capacity); //Send create order
        Object[] response = fromLobby.get(new ActualField(traderId), new FormalField(String.class)); //Get response based on traderID

        String result = (String) response[1]; //Answer ei. Fulfilled or Failed
        System.out.println("Server came back with response: " + result);

        //Send join room request so trader automatically joins its newly created room.
        joinRoomOrder(roomName, password);
    }
    public void joinRoomOrder() throws InterruptedException {
        Scanner terminalIn = new Scanner(System.in);

        System.out.println("Room name: ");
        String roomName = terminalIn.nextLine();
        System.out.println("Password: ");
        String password = terminalIn.nextLine();

        joinRoomOrder(roomName, password);
    }

    //Overloaded function for use in automatically joining a room after creating it.
    public void joinRoomOrder(String roomName, String password) throws InterruptedException{
        toLobby.put(traderId, "join", roomName, password, 0);

        Object[] response = fromLobby.get(new ActualField(traderId), new FormalField(String.class));
        System.out.println(response[1]);
        if(response[1].equals("Fulfilled")){
            connectedChats.put(roomName);
        }
    }

    public void getOverviewOrder() throws InterruptedException, IOException {
        //Querys all rooms the Trader is connected to, then lists them.
        List<Object[]> allChats = connectedChats.queryAll(new FormalField(String.class));
        int counter = 1;
        if (allChats.isEmpty()){ //If no rooms have been collected.
            System.out.println("You have no joined rooms...");
        } else {
            for(Object[] chat : allChats){ //Loop over all chats, and displays in a good-looking manner.
                //Puts in a request inorder to get back knowledge about capacity.
                toLobby.put(traderId, "getCapacity", chat[0], "", 0);

                Object[] response = fromLobby.get(new ActualField(traderId), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));

                System.out.println(counter + ". " + chat[0] + " | " + response[2] + "/" + response[3]);
                counter++;
            }
            consoleInputChatting();
        }
    }

    public void writeToChatroom(String roomName) throws IOException, InterruptedException {
        //RemoteSpace initialized for roomName.
        RemoteSpace chatRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + roomName + "?keep");
        Scanner terminalIn = new Scanner(System.in);
        boolean isConnected = true;
        ChatGetter getter = new ChatGetter(roomName, traderId); //new getter for trader mailbox.
        Thread getterThread = new Thread(getter);

        //Start getterThread, that listens to trader.
        getterThread.start();

        while(isConnected){
            String currentMessage = terminalIn.nextLine();
            if(!currentMessage.equals("EXIT")){
                chatRoom.put(traderId, currentMessage);
            } else{
                isConnected = false;
                getterThread.interrupt(); //Thread.interrupt - causes the thread to quit, but throws InterruptedException.
            }
        }
    }

    //Maybe a back option.
    //Console input for controlling after entering overview.
    public void consoleInputChatting() throws IOException, InterruptedException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Choose a group to text");
        String response = terminalIn.nextLine();
        writeToChatroom(response);
    }
    //Sends createUserSpace command.
    public void openTraderMessages() throws InterruptedException {
        toLobby.put(traderId, "createUserSpace", "", "", 0);
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

