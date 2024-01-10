package dk.dtu;

import org.jspace.FormalField;
import org.jspace.Space;

import java.util.ArrayList;

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

