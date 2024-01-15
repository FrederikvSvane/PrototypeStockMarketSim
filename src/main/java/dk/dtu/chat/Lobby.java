package dk.dtu.chat;

import dk.dtu.host.HostUtil;
import org.jspace.*;

import java.util.ArrayList;
import java.util.List;

public class Lobby implements Runnable {
    SpaceRepository chatRepo;
    SequentialSpace toLobby;
    SequentialSpace fromLobby;
    Space chatRoomLobby;

    SpaceRepository chatRoomsRepo;

    //TODO make traders able to directly message each other.

    public Lobby(SpaceRepository chatRepo) {
        this.chatRepo = chatRepo;
        this.toLobby = new SequentialSpace();
        this.fromLobby = new SequentialSpace();
        this.chatRoomLobby = new SequentialSpace();

        this.chatRepo.add("toLobby", toLobby);
        this.chatRepo.add("fromLobby", fromLobby);

        chatRoomsRepo = new SpaceRepository();
        chatRoomsRepo.addGate("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getLobbyPort() + "?keep");
    }

    public void run() {
        while (true) {
            try {
                Object[] request = toLobby.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
                String traderId = (String) request[0];
                String command = (String) request[1];
                String roomName = (String) request[2];
                String password = (String) request[3];
                int capacity = (int) request[4];
                //System.out.println("Server got request for: " + command + " name: " + roomName + ". From: " + traderId);
                switch (command) {
                    case "create": { //When create command is present.
                        if (checkRoomExists(roomName)) { //if room already exists.
                            fromLobby.put(traderId, "Failed");
                        } else {
                            fromLobby.put(traderId, "Fulfilled");
                            //After fulfillment we create a new space for chatting in the chatRepo space.
                            addRoomAndAuthToken(roomName, capacity, password);
                            //Initializes a new thread that listens to all conversations in roomName.
                            new Thread(new ChatGetter(roomName, traderId, true)).start();
                        }
                        break;
                    }

                    case "join": { //if join command is executed.
                        if (checkRoomExists(roomName)) { //Check if room exists.
                            Space chatRoom = chatRoomsRepo.get(roomName);
                            //Get authToken,
                            Object[] authToken = chatRoom.get(new ActualField("AuthToken"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            String correctPassword = (String) authToken[1];
                            int currentlyConnected = (int) authToken[2];
                            int fullCapacity = (int) authToken[3];

                            if (correctPassword.equals(password)) {
                                if (currentlyConnected < fullCapacity) { //When a trader joins a room.
                                    if (!checkConnectedStatus(traderId, chatRoom)) { //Checks whether the user is already connected to the chat.
                                        chatRoom.put("AuthToken", password, currentlyConnected + 1, fullCapacity);
                                        chatRoom.put("ConnectedToken", traderId, "connected");

                                        List<List<String>> historyList = new ArrayList<>();
                                        chatRoom.put("History", historyList);

                                        fromLobby.put(traderId, "Fulfilled");
                                    } else {
                                        fromLobby.put(traderId, "You're already connected to this room");
                                    }

                                } else { //If the room is too full.
                                    chatRoom.put("AuthToken", password, currentlyConnected, fullCapacity);
                                    fromLobby.put(traderId, "Room is full");

                                }

                            } else { //If the trader types the wrong password.
                                System.out.println("Wrong Password");
                                chatRoom.put("AuthToken", password, currentlyConnected, fullCapacity);
                                fromLobby.put(traderId, "Wrong Password");

                            }

                        } else {
                            //If the room does not exist, we need to return not fulfilled.
                            fromLobby.put(traderId, "Failed");

                        }
                        break;

                    }
                    case "getCapacity": {
                        if (checkRoomExists(roomName)) { //Means the room exists.
                            Space chatRoom = chatRoomsRepo.get(roomName);
                            Object[] authToken = chatRoom.query(new ActualField("AuthToken"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));
                            int currentlyConnected = (int) authToken[2];
                            int fullCapacity = (int) authToken[3];

                            fromLobby.put(traderId, "Fulfilled", currentlyConnected, fullCapacity);
                        }
                        break;
                    }
                    case "createUserSpace": { //Creates the space upon Trader initialization.
                        if (checkRoomExists(roomName)) {
                            chatRoomsRepo.add(traderId, new QueueSpace());
                        } else {
                            throw new Exception("Users room already exists");
                        }
                        //traderChat.put("Lobby", "Test message");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks whether a user is connected to a room.
     *
     * @param traderId
     * @param room
     * @return true if user is connnected else false.
     */
    public boolean checkConnectedStatus(String traderId, Space room) throws InterruptedException {
        List<Object[]> users = room.queryAll(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));
        for (Object[] user : users) {
            if (user[1].equals(traderId)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkRoomExists(String roomName) throws InterruptedException {
        Space room = chatRoomsRepo.get(roomName);
        if (room != null) {
            return true;
        }
        return false;
    }

    public void addRoomAndAuthToken(String roomName, int capacity, String password) throws InterruptedException {
        SequentialSpace newRoom = new SequentialSpace();
        newRoom.put("AuthToken", password, 0, capacity);
        chatRepo.add(roomName, newRoom);
    }
}
