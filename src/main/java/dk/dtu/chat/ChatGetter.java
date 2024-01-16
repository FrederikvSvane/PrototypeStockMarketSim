package dk.dtu.chat;

import dk.dtu.host.HostUtil;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import java.util.ArrayList;
import java.util.List;

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
                RemoteSpace chatRoom = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + (HostUtil.getLobbyPort()) + "/" + roomName + "?keep");
                while (true){

                    //Gets the newest message in the space.
                    Object[] responseMessage = chatRoom.get(new FormalField(String.class), new FormalField(String.class));

                    //Gets all users connected to the space.
                    List<Object[]> users = chatRoom.queryAll(new ActualField("ConnectedToken"), new FormalField(String.class), new FormalField(String.class));

                    if(!responseMessage[1].equals("EXIT")){ //To exit the terminal to write.
                        //Loops over every user collected, to send messages.
                        for(Object[] user : users){
                            if(!user[1].equals(responseMessage[0])){ //Check user isnt the one who send the message.
                                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getChatRepoPort() + "/" + user[1] + "?keep");
                                tradersRoom.put(responseMessage[0], responseMessage[1]);
                            }
                        }
                        updateHistory(chatRoom, (String) responseMessage[0], (String) responseMessage[1]);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else { // This is a getter running on a client, listening to their mailbox.
            System.out.println("Im listening to " + traderId + "'s room"); //For debugging.
            try{
                RemoteSpace tradersRoom = new RemoteSpace("tcp://" + HostUtil.getHostIp() + ":" + HostUtil.getChatRepoPort() + "/" + traderId + "?keep");
                while(true){
                    Object[] messagesRead = tradersRoom.get(new FormalField(String.class), new FormalField(String.class));
                    System.out.println(messagesRead[0] + ": " + messagesRead[1]);
                    //updateHistory(tradersRoom, (String) messagesRead[0], (String) messagesRead[1]);
                }
            } catch (InterruptedException e){
                System.out.println("You left the chat by: " + e);
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
        System.out.println(historyMessage.get(0) + " " + historyMessage.get(1));
        history.add(historyMessage);
        chatSpace.put("History", history);
        System.out.println("I added: " + sender + ": " + message + ". To history");
    }


}
