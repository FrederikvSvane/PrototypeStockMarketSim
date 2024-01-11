package dk.dtu;

import org.jspace.*;

import java.util.ArrayList;

public class Lobby implements Runnable {

    String hostIp;
    int hostPort;
    SpaceRepository hostRepo;
    SequentialSpace toLobby;
    SequentialSpace fromLobby;
    Space chatRoomLobby;

    SpaceRepository chatRooms;
    ArrayList<String> roomRegister = new ArrayList<String>();


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


        //ChatRoomLobby is used to communicate between the chatroom and the lobby
        //hostRepo.add("chatRoomLobby", chatRoomLobby);
    }

    //Need a function that gets from a chatroom, and distributes to all connected clients.
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

                System.out.println("Server got request for: " + command + " name: " + roomName + ". From: " + traderId);
                switch(command){
                    case "create":{
                        Space roomExists = hostRepo.get(roomName);
                        if(roomExists != null){
                            fromLobby.put(traderId, "Failed");
                        } else {
                            fromLobby.put(traderId, "Fulfilled");
                            //After fulfillment we create a new space for chatting in the chatRooms space.
                            SequentialSpace newRoom = new SequentialSpace();
                            newRoom.put("AuthToken", password, 0, capacity);
                            chatRooms.add(roomName, newRoom);
                            new Thread(new ChatGetter(roomName, traderId, true)).start();
                        }
                        break;
                    }

                    case "join":{
                        Space roomExists = chatRooms.get(roomName);
                        System.out.println(roomExists);
                        //Check if room exists.
                        if(roomExists != null){
                            //Get authToken,
                            Object[] authToken = roomExists.get(new ActualField("AuthToken"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            String correctPassword = (String) authToken[1];
                            int currentlyConnected = (int) authToken[2];
                            int fullCapacity = (int) authToken[3];

                            if(correctPassword.equals(password)){
                                if(currentlyConnected < fullCapacity){
                                    roomExists.put("AuthToken", password, currentlyConnected + 1, fullCapacity);
                                    roomExists.put("ConnectedToken", traderId, "connected");
                                    /*Object[] response = roomExists.query(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));
                                    System.out.println(response[1] + " " + response[2]);*/
                                    fromLobby.put(traderId, "Fulfilled");

                                } else {
                                    roomExists.put("AuthToken", password, currentlyConnected, fullCapacity);
                                    fromLobby.put(traderId, "Room is full");

                                }

                            } else {
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
                    case "subscribe":{
                        break;
                    }
                    case "createUserSpace":{
                        Space traderChat = chatRooms.get(traderId);

                        if (traderChat == null){
                            chatRooms.add(traderId, new SequentialSpace());
                            traderChat = chatRooms.get(traderId);
                        }

                        traderChat.put("Lobby", "Test message");
                        System.out.println("Room added for user: " + traderId + "on uri: " );
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
