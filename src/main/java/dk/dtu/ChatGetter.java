package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

import static dk.dtu.Host.hostIp;
import static dk.dtu.Host.hostPort;

public class ChatGetter implements Runnable{
    private String roomName;
    private String traderId;
    private boolean isRoomGetter;

    boolean running;

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
                while (true){

                    //Gets the newest message in the space.
                    Object[] responseMessage = chatRoom.get(new FormalField(String.class), new FormalField(String.class));

                    //Gets all users connected to the space.
                    List<Object[]> users = chatRoom.queryAll(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));

                    if(!responseMessage[1].equals("EXIT")){ //To exit the terminal to write.
                        //Loops over every user collected, to send messages.
                        updateHistory(chatRoom, (String) responseMessage[0], (String) responseMessage[1]);
                        for(Object[] user : users){
                            if(!user[1].equals(responseMessage[0])){ //Check user isnt the one who send the message.
                                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + user[1] + "?keep");
                                tradersRoom.put(responseMessage[0], responseMessage[1]);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else { // This is a getter running on a client, listening to their mailbox.
            //System.out.println("Im listening to " + traderId + "'s room"); For debugging.
            try{
                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + hostIp + ":" + (hostPort + 1) + "/" + traderId + "?keep");
                while(true){
                    //Gets new message.
                    Object[] messagesRead = tradersRoom.get(new FormalField(String.class), new FormalField(String.class));
                    updateHistory(tradersRoom, (String) messagesRead[0], (String) messagesRead[1]);
                    System.out.println(messagesRead[0] + ": " + messagesRead[1]);
                }
            } catch (InterruptedException e){
                System.out.println("You left the chat...");
            } catch (Exception f){
                throw new RuntimeException(f);
            }
        }
    }

    public void updateHistory(Space chatSpace, String sender, String message) throws InterruptedException {
        Object[] historyObject = chatSpace.get(new ActualField("History"), new FormalField(ArrayList.class));
        List<List<String>> history = (List) historyObject[1];
        List<String> historyMessage = new ArrayList<>();
        historyMessage.add(sender);
        historyMessage.add(message);
        history.add(historyMessage);
        chatSpace.put("History", history);
    }

    public List<List<String>> getHistory(Space chatSpace) throws InterruptedException {
        Object[] historyObject = chatSpace.query(new ActualField("History"), new FormalField(ArrayList.class));
        List<List<String>> history = (List) historyObject[1];
        return history;
    }
}
