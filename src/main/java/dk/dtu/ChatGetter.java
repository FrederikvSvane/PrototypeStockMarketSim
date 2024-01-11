package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

import static dk.dtu.Host.hostIp;
import static dk.dtu.Host.hostPort;

public class ChatGetter implements Runnable{

    private String roomName;
    private String traderId;
    private boolean isRoomGetter;

    public ChatGetter(String roomName, String traderId){
        this.roomName = roomName;
        this.traderId = traderId;
    }

    public ChatGetter(String roomName, String traderId, boolean isRoomGetter){
        this.roomName = roomName;
        this.traderId = traderId;
        this.isRoomGetter = isRoomGetter;
    }
    @Override
    public void run() {
        if(isRoomGetter){ // This is listening to a room
            System.out.println("I am listening to room: " + roomName);
            try {
                RemoteSpace chatRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + roomName + "?keep");
                while (true){
                    //Object[] response = chatRoom.query(new ActualField("AuthToken"), new FormalField(String.class), new FormalField(Integer.class), new FormalField(Integer.class));
                    Object[] responseMessage = chatRoom.get(new FormalField(String.class), new FormalField(String.class));
                    List<Object[]> users = chatRoom.queryAll(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));
                    System.out.println(users.size());
                    if(users.size() > 0){
                        for(Object[] user : users){
                            if(!user[1].equals(responseMessage[0])){
                                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + user[1] + "?keep");
                                System.out.println(responseMessage[0] + ": " + responseMessage[1] + " to " + tradersRoom.getUri());
                                tradersRoom.put(responseMessage[0], responseMessage[1]);
                                System.out.println(responseMessage[0] + ": " + responseMessage[1] + " to " + user[1]);
                            }
                        }
                    } //Forloop over alle traders
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Im listening to " + traderId + "'s room");
            try{
                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + traderId + "?keep");
                while(true){
                    Object[] messagesRead = tradersRoom.get(new FormalField(String.class), new FormalField(String.class));
                    System.out.println(messagesRead[0] + ": " + messagesRead[1]);
                }
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
