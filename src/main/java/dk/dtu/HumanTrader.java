package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Scanner;

public class HumanTrader extends Trader implements Runnable{

    String traderToLobbyName;
    String lobbyToTraderName;
    RemoteSpace traderToLobby;
    RemoteSpace lobbyToTrader;

    public HumanTrader(String traderToLobbyName, String lobbyToTraderName) {
        super();
        this.traderToLobbyName = traderToLobbyName;
        this.lobbyToTraderName = lobbyToTraderName;
        String hostUri = ClientUtil.getHostUri(traderToLobbyName);
        try {
            this.traderToLobby = new RemoteSpace(ClientUtil.setConnectType(hostUri,"keep"));
            this.lobbyToTrader = new RemoteSpace(ClientUtil.setConnectType(hostUri,"keep"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        chatMenu();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in HumanTrader");
                    }
                }
            }
        }
    }

    public String chooseMode() {
        Scanner terminalIn = new Scanner(System.in);
        System.out.println("Choose mode: \n1. Create trade \n2. Open chat");
        String mode = terminalIn.nextLine();
        if (mode.equals("1")) {
            return "trade";
        } else if (mode.equals("2")) {
            return "chat";
        } else {
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

        if (capacity <= 0) {
            System.out.println("Capacity was equal to or below 0, so it is set to 1.");
            capacity = 1;
        }

        traderToLobby.put(traderId, "create chat");
        traderToLobby.put(traderId, roomName, password, capacity);
        Object[] roomCreationAnswer = lobbyToTrader.get(new ActualField(traderId), new FormalField(String.class), new FormalField(String.class));
        System.out.println("We got the response:" + roomCreationAnswer[0].toString() + roomCreationAnswer[1].toString() + roomCreationAnswer[2].toString());
    }

    public String sendJoinChatProtocol() throws Exception {
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
        if (mode.equals("1")) {
            sendCreateChatProtocol();
        } else if (mode.equals("2")) {
            traderToLobby.put(traderId, "show rooms");
            Object[] roomOverview = lobbyToTrader.get(new ActualField(traderId), new FormalField(String[].class), new FormalField(int.class));
            System.out.println("Following rooms are open:" + roomOverview[0].toString() + roomOverview[1].toString());
            return roomOverview;
        } else if (mode.equals("3")) {
            traderToLobby.put(traderId, "join");
            String responseMessage = sendJoinChatProtocol();
            if (responseMessage.equals("Create room it doesn't exist")) {
                System.out.println("Whatever");
                sendCreateChatProtocol();
            }

        } else {
            System.out.println("Invalid input, please press the number of the option you want to choose");
            return chatMenu();
        }
        return null;
    }

}
