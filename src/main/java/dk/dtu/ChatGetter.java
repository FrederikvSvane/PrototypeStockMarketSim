package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import java.util.List;

import static dk.dtu.Host.hostIp;
import static dk.dtu.Host.hostPort;

public class ChatGetter implements Runnable{
    private String roomName;
    private String traderId;
    private boolean isRoomGetter;

    boolean running;

    public void terminate(){
        this.running = false;
    }

    public ChatGetter(String roomName, String traderId){
        this.roomName = roomName;
        this.traderId = traderId;
        this.running = true;
    }

    public ChatGetter(String roomName, String traderId, boolean isRoomGetter){
        this.roomName = roomName;
        this.traderId = traderId;
        this.isRoomGetter = isRoomGetter;
        this.running = true;
    }

    @Override
    public void run() {
        if(isRoomGetter){ // This is listening to a room
            System.out.println("I am listening to room: " + roomName);
            try {
                RemoteSpace chatRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + roomName + "?keep");
                while (this.running){

                    Object[] responseMessage = chatRoom.get(new FormalField(String.class), new FormalField(String.class));
                    List<Object[]> users = chatRoom.queryAll(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));
                    System.out.println(users.size());
                    if(!responseMessage[1].equals("EXIT")){ //To exit the terminal to write.
                        for(Object[] user : users){
                            if(!user[1].equals(responseMessage[0])){
                                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + user[1] + "?keep");
                                System.out.println(responseMessage[0] + ": " + responseMessage[1] + " to " + tradersRoom.getUri());
                                tradersRoom.put(responseMessage[0], responseMessage[1]);
                                System.out.println(responseMessage[0] + ": " + responseMessage[1] + " to " + user[1]);
                            }
                        }
                    }//Forloop over alle traders
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Im listening to " + traderId + "'s room");
            try{
                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + traderId + "?keep");
                while(this.running){
                    Object[] messagesRead = tradersRoom.get(new FormalField(String.class), new FormalField(String.class));
                    System.out.println(messagesRead[0] + ": " + messagesRead[1]);
                }
            } catch (InterruptedException e){
                System.out.println("You left the chat...");
            } catch (Exception f){
                throw new RuntimeException(f);
            }
        }
    }
}
