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
                        System.out.println("Someone attempted to join");
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

            System.out.println("User: " + traderUuid + " created room " + roomName + " with max capacity: " + capacity);

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
            String joinRoomTraderUUID = (String) req[0];
            String roomName = (String) req[1];
            String password = (String) req[2];
            System.out.println("Lobby received room name trying to join: " + roomName + " from user with ID: " + joinRoomTraderUUID + " and password " + password);

            Space chatRoom = hostRepo.get(roomName);


            //If a room doesn't exist we create it.
            if(chatRoom == null) {
                System.out.println("Room: " + roomName + " doesn't exist we're trying to create a new");
                lobbyToTrader.put(traderUuid,"Create room it doesn't exist");
                createRoom(traderUuid);
                return;
            }

            //Check password
            chatRoomLobby.put(roomName,"join");
            chatRoomLobby.put(traderUuid,password);


            //Check capacity
            Object[] joinRequest = chatRoomLobby.get(new ActualField("Chat room response"),new ActualField(roomName), new FormalField(String.class));
            String responseMessage = (String) joinRequest[2];
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

    public void sendChatroomResponse(String response_message) throws InterruptedException {
        chatRoomLobby.put("Chat room response",name, response_message);
    }

    public boolean recordNewUser(String userID) throws InterruptedException {

        System.out.println("Trying to add user: " + userID + "to chatroom with name: " + name);

        if(usersRegister.contains(userID))
        {
            sendChatroomResponse("User: " + userID + " already exists");
            return false;
        }

        if(this.totalCapactiy == 0)
            {
                sendChatroomResponse("capacity full");
                return false;
            }

        this.totalCapactiy -= 1;
        this.usersRegister.add(userID);
        return true;
    }

    public void run()
    {
        while (true)
        {
            try {
                //System.out.println(name);
                Object[] req = chatRoomLobby.get(new FormalField(String.class),new FormalField(String.class));
                String attemptedName = (String) req[0];
                String command = (String) req[1];
                //System.out.println("Room " + attemptedName + " received command " + command);

                switch (command)
                {
                    case "join":
                        Object[] joinAttempt = chatRoomLobby.get(new FormalField(String.class),new FormalField(String.class));
                        String userID = (String) joinAttempt[0];
                        String attemptedPassword = (String) joinAttempt[1];

                        //System.out.println("User " + userID + " tried to log in to chatroom with passwrd " + attemptedPassword);
                        if(!attemptedPassword.equals(password))
                        {
                            sendChatroomResponse("Wrong password");
                        }
                        if(recordNewUser(userID))
                        {
                            sendChatroomResponse("Succesfully joined");
                        }

                        break;

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
    }

}


