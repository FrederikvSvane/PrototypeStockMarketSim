package dk.dtu;

import org.jspace.*;

import java.util.ArrayList;

public class Lobby implements Runnable {

    String hostIp;
    int hostPort;
    SpaceRepository hostRepo;
    SequentialSpace traderToLobby;
    SequentialSpace lobbyToTrader;
    Space chatRoomLobby;
    ArrayList<String> roomRegister = new ArrayList<String>();


    public Lobby(String hostIp, int hostPort, SpaceRepository chatRepo)
    {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.hostRepo = chatRepo;
        this.traderToLobby = new SequentialSpace();
        this.lobbyToTrader = new SequentialSpace();
        this.chatRoomLobby = new SequentialSpace();
        hostRepo.add("traderToLobby",traderToLobby);
        hostRepo.add("lobbyToTrader",lobbyToTrader);
        //ChatRoomLobby is used to communicate between the chatroom and the lobby
        hostRepo.add("chatRoomLobby", chatRoomLobby);
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
                    case "show overview":
                        break;
                    case "join":
                        joinRoom(requester);
                        break;
                    case "delete room":
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
            Space roomExists =  hostRepo.get(roomName);

            //If the room exists
            if(roomExists != null) {
                lobbyToTrader.put(traderUuid, "room with name" + roomName + "already exists");
                return;
            }

            //ChatRoomTrader is used to send messages between traders
            Space chatRoomTrader = new SequentialSpace();

            hostRepo.add(roomName,chatRoomTrader);
            new Thread(new ChatRoom(chatRoomLobby,chatRoomTrader, roomName, password, capacity)).start();
            roomRegister.add(roomName);
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
            Space chatRoom = hostRepo.get(roomName);

            //If a room doesn't exist we create it.
            if(chatRoom == null) {
                createRoom(traderUuid);
            }


            //Check password
            chatRoomLobby.put(roomName,"join");
            chatRoomLobby.put(traderUuid,password);


            //Check capacity
            Object[] joinRequest = chatRoomLobby.get(new ActualField(roomName), new FormalField(String.class));
            String responseMessage = (String) joinRequest[1];
            lobbyToTrader.put(traderUuid,responseMessage);


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in lobby");
        }

    }

    public void getOverview(String traderUuid)
    {


    }
}

class ChatRoom implements Runnable
{
    Space chatRoomTrader;
    Space chatRoomLobby;
    String name;
    String password;
    int totalCapactiy;
    int currentCapacity;
    ArrayList<String> usersRegister = new ArrayList<String>();


    public ChatRoom(Space chatRoomLobby, Space chatRoomTrader, String name, String password, int totalCapactiy)
    {
        this.chatRoomTrader = chatRoomTrader;
        this.name = name;
        this.password = password;
        this.totalCapactiy = totalCapactiy;
        this.chatRoomLobby = chatRoomLobby;
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

    public void recordNewUser(String userID) throws InterruptedException {

        if(usersRegister.contains(userID))
        {
            chatRoomLobby.put(name,"User already exists");
            return;
        }

        if(this.totalCapactiy == 0)
            {
                chatRoomLobby.put(name,"capacity full");
                return;
            }

        this.totalCapactiy -= 1;
        this.usersRegister.add(userID);
    }

    public void run()
    {
        while (true)
        {
            try {
                Object[] req = chatRoomLobby.get(new ActualField(name),new FormalField(String.class));
                String command = (String) req[1];

                switch (command)
                {
                    case "join":
                        Object[] joinAttempt = chatRoomLobby.get(new FormalField(String.class),new FormalField(String.class));
                        String userID = (String) joinAttempt[0];
                        String attemptedPassword = (String) joinAttempt[1];

                        if(attemptedPassword.equals(password))
                        {
                            chatRoomLobby.put(name,"Wrong password");
                        }

                        recordNewUser(userID);
                        chatRoomLobby.put(name,"Succesfully joined");

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
    }

}


