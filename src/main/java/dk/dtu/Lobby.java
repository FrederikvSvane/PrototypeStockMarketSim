package dk.dtu;

import org.jspace.*;

import java.util.HashMap;

public class Lobby implements Runnable {

    String hostIp;
    int hostPort;
    SpaceRepository chatRepo;
    SequentialSpace traderToLobby;
    SequentialSpace lobbyToTrader;

    public Lobby(String hostIp, int hostPort, SpaceRepository chatRepo)
    {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.chatRepo = chatRepo;
        this.traderToLobby = new SequentialSpace();
        this.lobbyToTrader = new SequentialSpace();
        chatRepo.add("traderToLobby",traderToLobby);
        chatRepo.add("lobbyToTrader",lobbyToTrader);

    }

    public void run()
    {
        while(true) {
            try {
                Object[] req = traderToLobby.get(new FormalField(String.class), new FormalField(String.class));
                String requester = (String) req[0];
                String request = (String) req[1];

                switch(request) {
                    case "create chat":
                        createRoom(requester);
                        break;
                    case "join":
                        joinRoom(requester);
                        break;

                    default:
                        throw new RuntimeException("Invalid request");
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in lobby");
            }
        }
    }

    public void createRoom(String traderUuid)
    {
        try {
            Object[] req = traderToLobby.get(new ActualField(traderUuid), new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
            String roomName = (String) req[1];
            String password = (String) req[2];
            int capacity = (int) req[3];
            Space roomExists =  chatRepo.get(roomName);

            if(roomExists != null) {
                lobbyToTrader.put(traderUuid, "room with name" + roomName + "already exists");
                return;
            }

            Space chatRoom = new SequentialSpace();
            chatRepo.add(roomName,chatRoom);
            new Thread(new ChatRoom(chatRoom, roomName, password, capacity)).start();
            lobbyToTrader.put(traderUuid, "room with name" + roomName + "created");





        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in lobby");
        }

    }

    public void joinRoom(String traderUuid)
    {
        try {
            Object[] req = traderToLobby.get(new ActualField(traderUuid), new FormalField(String.class), new FormalField(String.class));
            String roomName = (String) req[1];
            String password = (String) req[2];
            Space chatRoom = chatRepo.get(roomName);

            if(chatRoom == null) {
                createRoom(traderUuid);
            }

            //TODO: check if password is correct
            lobbyToTrader.put("We have not finished this yet");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in lobby");
        }

    }

}

class ChatRoom implements Runnable
{
    Space chatRoom;
    String name;
    String password;
    int totalCapactiy;
    int currentCapacity;
    String[] users;


    public ChatRoom(Space chatRoom, String name, String password, int totalCapactiy)
    {
        this.chatRoom = chatRoom;
        this.name = name;
        this.password = password;
        this.totalCapactiy = totalCapactiy;
    }



    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }

    public int getTotalCapacity()
    {
        return totalCapactiy;
    }

    public void run()
    {
        return;
    }

}


