package dk.dtu;

import org.jspace.*;

import java.util.ArrayList;
import java.util.List;

public class Lobby implements Runnable {

    String hostIp;
    int hostPort;
    SpaceRepository hostRepo;
    SequentialSpace toLobby;
    SequentialSpace fromLobby;
    Space chatRoomLobby;

    SpaceRepository chatRooms;
    //TODO make traders able to directly message each other.
    //TODO fetch chat history when sending messages to a chat.
    public Lobby(String hostIp, int hostPort, SpaceRepository chatRepo)
    {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.hostRepo = chatRepo;
        this.toLobby = new SequentialSpace();
        this.fromLobby = new SequentialSpace();
        this.chatRoomLobby = new SequentialSpace();

        hostRepo.add("toLobby",toLobby);
        hostRepo.add("fromLobby",fromLobby);

        chatRooms = new SpaceRepository();
        chatRooms.addGate("tcp://" + hostIp + ":" + (hostPort + 1) + "?keep");
    }
    public void run()
    {
        while(true){
            try {
                Object[] request = toLobby.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
                String traderId = (String) request[0];
                String command = (String) request[1];
                String roomName = (String) request[2];
                String password = (String) request[3];
                int capacity = (int) request[4];

                //System.out.println("Server got request for: " + command + " name: " + roomName + ". From: " + traderId);
                switch(command){
                    case "create":{ //When create command is present.
                        Space roomExists = hostRepo.get(roomName);
                        if(roomExists != null){ //if room already exists.
                            fromLobby.put(traderId, "Failed");
                        } else {
                            fromLobby.put(traderId, "Fulfilled");
                            //After fulfillment, we create a new space for chatting in the chatRooms space.
                            SequentialSpace newRoom = new SequentialSpace();
                            newRoom.put("AuthToken", password, 0, capacity);
                            chatRooms.add(roomName, newRoom);
                            //Initializes a new thread that listens to all conversations in roomName.
                            new Thread(new ChatGetter(roomName, traderId, true)).start();
                        }
                        break;
                    }

                    case "join":{ //if join command is executed.
                        Space roomExists = chatRooms.get(roomName);

                        if(roomExists != null){ //Check if room exists.
                            //Get authToken,
                            Object[] authToken = roomExists.get(new ActualField("AuthToken"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            String correctPassword = (String) authToken[1];
                            int currentlyConnected = (int) authToken[2];
                            int fullCapacity = (int) authToken[3];

                            if(correctPassword.equals(password)){
                                if(currentlyConnected < fullCapacity){ //When a trader joins a room.
                                    if(!checkConnectedStatus(traderId, roomExists)){ //Checks whether the user is already connected to the chat.
                                        roomExists.put("AuthToken", password, currentlyConnected + 1, fullCapacity);
                                        roomExists.put("ConnectedToken", traderId, "connected");

                                        List<List<String>> historyList = new ArrayList<>();
                                        roomExists.put("History", historyList);
                                        fromLobby.put(traderId, "Fulfilled");
                                    } else{
                                        fromLobby.put(traderId, "You're already connected to this room");
                                    }

                                } else { //If the room is too full.
                                    roomExists.put("AuthToken", password, currentlyConnected, fullCapacity);
                                    fromLobby.put(traderId, "Room is full");

                                }

                            } else { //If the trader types the wrong password.
                                System.out.println("Wrong Password");
                                roomExists.put("AuthToken", password, currentlyConnected, fullCapacity);
                                fromLobby.put(traderId, "Wrong Password");

                            }

                        } else {
                            //If the room does not exist, we need to return not fulfilled.
                            fromLobby.put(traderId, "Failed");

                        }
                        break;

                    }
                    case "getCapacity":{
                        Space roomExists = chatRooms.get(roomName);

                        if(roomExists != null){
                            Object[] authToken = roomExists.query(new ActualField("AuthToken"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            int currentlyConnected = (int) authToken[2];
                            int fullCapacity = (int) authToken[3];

                            fromLobby.put(traderId, "Fulfilled", currentlyConnected, fullCapacity);
                        }
                        break;
                    }
                    case "subscribe":{ //Not sure this will be used.
                        break;
                    }
                    case "createUserSpace":{ //Creates the space upon Trader initialization.
                        Space traderChat = chatRooms.get(traderId);

                        if (traderChat == null){
                            chatRooms.add(traderId, new SequentialSpace());
                            traderChat = chatRooms.get(traderId);
                        }
                        List<Object[]> historyList = new ArrayList<>();
                        traderChat.put("History", historyList);

                        //traderChat.put("Lobby", "Test message");
                        System.out.println("Room added for user: " + traderId + "on uri: " );
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks whether a user is connected to a room.
     * @param traderId
     * @param room
     * @return true if user is connnected else false.
     */
    public boolean checkConnectedStatus(String traderId, Space room) throws InterruptedException {
        List<Object[]> users = room.queryAll(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));
        for(Object[] user : users){
            if(user[1].equals(traderId)){
                return true;
            }
        }
        return false;
    }
}
