package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import dk.dtu.*;

public class HumanTrader extends Trader implements Runnable {

    RemoteSpace toLobby;
    RemoteSpace fromLobby;
    SequentialSpace connectedChats;
    RemoteSpace myMessages;

    public HumanTrader() throws IOException {
        super();
        toLobby = new RemoteSpace("tcp://" + super.getHostIp() + ":" + super.getHostPort() + "/toLobby?keep");
        fromLobby = new RemoteSpace("tcp://" + super.getHostIp() + ":" + super.getHostPort() + "/fromLobby?keep");
        connectedChats = new SequentialSpace();
        myMessages = new RemoteSpace("tcp://" + super.getHostIp() + ":" + (super.getHostPort() + 1) + "/" + super.getTraderId() + "?keep");
    }

    @Override
    public void run() {
        while (true) {
            String mode = chooseMode();

            switch (mode) {
                case "trade": {
                    try {
                        consoleInputToSendOrder();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in HumanTrader");
                    }
                }
                case "chat": {
                    try {
                        consoleInputToChat();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in HumanTrader");
                    }
                }
            }
        }
    }
    public void consoleInputToSendOrder() throws IOException, InterruptedException {
        Scanner terminalIn = new Scanner(System.in);
        String orderString = terminalIn.nextLine();
        String[] orderParts = orderString.split(" ");
        String orderType = orderParts[0];
        String companyName = orderParts[1];
        String companyTicker;
        Object[] companyData = super.getMasterCompanyRegister().queryp(new FormalField(String.class), new ActualField(companyName.toLowerCase()), new FormalField(String.class));

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
    public void joinRoomOrder(String roomName, String password) throws InterruptedException{
        toLobby.put(super.getTraderId(), "join", roomName, password, 0);

        Object[] response = fromLobby.get(new ActualField(super.getTraderId()), new FormalField(String.class));
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
            for(Object[] chat : allChats){ //Loop over all chats, and displays in a good looking manner.
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
        RemoteSpace chatRoom = new RemoteSpace("tcp://" + super.getHostIp() + ":" + (super.getHostPort() + 1) + "/" + roomName + "?keep");
        //Need to get space to talk to.
        Scanner terminalIn = new Scanner(System.in);

        boolean isConnected = true;
        ChatGetter getter = new ChatGetter(roomName, super.getTraderId());
        Thread thread = new Thread(getter);
        thread.start();
        while(isConnected){
            String currentMessage = terminalIn.nextLine();
            if(!currentMessage.equals("EXIT")){
                chatRoom.put(super.getTraderId(), currentMessage);
            } else{
                isConnected = false;
                thread.interrupt();
            }
        }
    }

    //Maybe a back option.
    public void consoleInputChatting() throws IOException, InterruptedException {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Choose a group to text");
        String response = terminalIn.nextLine();
        writeToChatroom(response);
    }

    public void openTraderMessages() throws InterruptedException {
        toLobby.put(super.getTraderId(), "createUserSpace", "", "", 0);
    }

}
