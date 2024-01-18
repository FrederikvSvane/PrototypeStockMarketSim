package dk.dtu.client.trader;

import dk.dtu.chat.ChatGetter;
import dk.dtu.client.Order;
import dk.dtu.company.IRS;
import dk.dtu.company.api.FinancialData;
import dk.dtu.host.HostUtil;
import org.jspace.*;
import dk.dtu.company.Company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HumanTrader extends Trader implements Runnable {

    RemoteSpace toLobby;
    RemoteSpace fromLobby;
    SequentialSpace connectedChats;
    RemoteSpace myMessages;

    public HumanTrader() throws IOException, InterruptedException {
        super();
        toLobby = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getLobbyPort() + "/toLobby?keep");
        fromLobby = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getLobbyPort() + "/fromLobby?keep");
        connectedChats = new SequentialSpace();
        myMessages = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getChatRepoPort() + "/" + super.getTraderId() + "?keep");
    }

    @Override
    public void run() {
        try {
            openTraderMessages();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            String mode = chooseMode();

            switch (mode) {
                case "trade": {
                    try {
                        consoleInputToSendOrder();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "chat": {
                    try {
                        consoleInputToChat();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "Company Fundamentals": {
                    try {
                        showCompanyFundametals();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "All companies": {
                    try {
                        showAllCompanies();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
    }

    private void showAllCompanies() throws InterruptedException {
        System.out.println("før getticket");
        Object[] getticket = super.getCompanyPriceGraphs().get(new ActualField("ticket"));
        System.out.println("efter getticket");
        List<Object[]> allCompanies = super.getCompanyPriceGraphs().queryAll(new FormalField(String.class), new FormalField(String.class), new FormalField(Float.class));
        super.getCompanyPriceGraphs().put("ticket");
        System.out.println("All companies traded at the exchange: " + allCompanies.size());
        for (Object[] company : allCompanies) {
            System.out.println("name: "+company[0] + " | ticker: " + company[1] + " | price: " + company[2]);
        }
//        for (Object[] company : allCompanies) {
//            System.out.println("name: " + company[1] + " | ticker: " + company[2]);
//        }
        System.out.println("You will be directed back to the main menu in 5 seconds");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void consoleInputToSendOrder() throws IOException, InterruptedException {
        System.out.println("Enter order with format: {buy/sell} {companyName} {amount} {price} \nExample: buy apple 10 100");
        Scanner terminalIn = new Scanner(System.in);
        String orderString = terminalIn.nextLine();
        String[] orderParts = orderString.split(" ");
        String orderType = orderParts[0];
        String companyName = orderParts[1];
        String companyTicker;
        Object[] companyData = super.getMasterCompanyRegister().queryp(new FormalField(String.class), new ActualField(companyName), new FormalField(String.class));

        if (companyData == null) {
            // Så man kan skriver ticker i stedet for navn
            companyData = super.getMasterCompanyRegister().queryp(new FormalField(String.class), new FormalField(String.class), new ActualField(companyName.toUpperCase()));

            if (companyData == null) {
                throw new RuntimeException("Company does not exist");
            }
        }
        companyName = (String) companyData[1];
        companyTicker = (String) companyData[2];

        int amount = Integer.parseInt(orderParts[2]);
        float price = Float.parseFloat(orderParts[3]);
        Order order = new Order(super.getTraderId(), companyName, companyTicker, amount, price);
        super.sendOrderToBroker(orderType, order);
    }

    public String chooseMode() {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Choose mode: \n1. Create trade \n2. Open chat \n3. Look up company fundamentals \n4. Show all companies traded at exchange");
        String mode = terminalIn.nextLine();
        if (mode.equals("1")) {
            return "trade";
        } else if (mode.equals("2")) {
            return "chat";
        } else if (mode.equals("3")) {
            return "Company Fundamentals";
        } else if (mode.equals("4")) {
            return "All companies";
        } else {
            System.out.println("Invalid input");
            return chooseMode();
        }
    }


    public void consoleInputToChat() throws InterruptedException, IOException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("1. Create chat \n2. Get Overview \n3. Join Chat");
        String choiceInput = terminalIn.nextLine();

        switch (choiceInput) {
            case "1": { //Create Room
                createRoomOrder();
                break;
            }
            case "2": { //Get overview
                getOverviewOrder();
                break;
            }
            case "3": { //Join room
                joinRoomOrder();
                break;
            }
            case "4": { //Send directly to trader

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

        toLobby.put(super.getTraderId(), "create", roomName, password, capacity); //Send create order
        Object[] response = fromLobby.get(new ActualField(super.getTraderId()), new FormalField(String.class)); //Get response based on traderID

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
    public void joinRoomOrder(String roomName, String password) throws InterruptedException {
        toLobby.put(super.getTraderId(), "join", roomName, password, 0);

        Object[] response = fromLobby.get(new ActualField(super.getTraderId()), new FormalField(String.class));
        System.out.println(response[1]);
        if (response[1].equals("Fulfilled")) {
            connectedChats.put(roomName);
        }
    }

    public void getOverviewOrder() throws InterruptedException, IOException {
        //Querys all rooms the Trader is connected to, then lists them.
        List<Object[]> allChats = connectedChats.queryAll(new FormalField(String.class));
        int counter = 1;
        if (allChats.isEmpty()) { //If no rooms have been collected.
            System.out.println("You have no joined rooms...");
        } else {
            for (Object[] chat : allChats) { //Loop over all chats, and displays in a good looking manner.
                //Puts in a request inorder to get back knowledge about capacity.
                toLobby.put(super.getTraderId(), "getCapacity", chat[0], "", 0);

                Object[] response = fromLobby.get(new ActualField(super.getTraderId()), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));

                System.out.println(counter + ". " + chat[0] + " | " + response[2] + "/" + response[3]);
                counter++;
            }
            consoleInputChatting();
        }
    }

    public void writeToChatroom(String roomName) throws IOException, InterruptedException {
        //RemoteSpace initialized for roomName.
        RemoteSpace chatRoom = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getChatRepoPort() + "/" + roomName + "?keep");
        Scanner terminalIn = new Scanner(System.in);
        boolean isConnected = true;
        ChatGetter getter = new ChatGetter(roomName, super.getTraderId()); //new getter for trader mailbox.
        Thread getterThread = new Thread(getter);

        //Start getterThread, that listens to trader.
        getterThread.start();
        displayHistory(chatRoom);

        while (isConnected) {
            String currentMessage = terminalIn.nextLine();
            if (!currentMessage.equals("EXIT")) {
                chatRoom.put(super.getTraderId(), currentMessage);
            } else {
                isConnected = false;
                getterThread.interrupt(); //Thread.interrupt - causes the thread to quit, but throws InterruptedException.
                System.out.println("You left the chat...");
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
        toLobby.put(super.getTraderId(), "createUserSpace", "", "", 0);
    }

    public void displayHistory(Space chatSpace) throws InterruptedException {
        Object[] historyObject = chatSpace.query(new ActualField("History"), new FormalField(ArrayList.class));
        List<List<String>> history = new ArrayList<>();
        if (historyObject != null) {
            history = (List) historyObject[1];
            //System.out.println("I should display history " + historyObject);
            for (List<String> entry : history) {
                System.out.println(entry.get(0) + ": " + entry.get(1));
            }
        }
    }

    public void showCompanyFundametals() throws InterruptedException, IOException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Enter company or ticker name: ");
        String input = terminalIn.nextLine();
        String companyName = input.toUpperCase();
        Object[] companyData = super.getMasterCompanyRegister().queryp(new FormalField(String.class), new ActualField(companyName), new FormalField(String.class));

        if (companyData == null) {
            // Så man kan skriver ticker i stedet for navn
            companyData = super.getMasterCompanyRegister().queryp(new FormalField(String.class), new FormalField(String.class), new ActualField(companyName.toUpperCase()));

            if (companyData == null) {
                System.out.println("Company does not exist");
                return;
            }
        }
        System.out.println("Enter year of fundamentals you want to look at: ");
        int year = terminalIn.nextInt();
        FinancialData data = Company.getFundamentalDataOverview(companyName, year);

        System.out.println("The revenue of " + companyName + " is " + data.getRevenue() + " USD \n You will be directed back to the main menu in 5 seconds");
        Thread.sleep(5000);


    }
}
