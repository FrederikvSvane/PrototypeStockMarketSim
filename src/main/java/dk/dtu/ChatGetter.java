package dk.dtu;

import org.jspace.FormalField;
import org.jspace.Space;

public class ChatGetter implements Runnable{

    Space chatRoom;
    String traderId;

    public ChatGetter(Space chatRoom, String traderId){
        this.chatRoom = chatRoom;
        this.traderId = traderId;
    }
    @Override
    public void run() {
        while(true){
            try {
                Object[] response = chatRoom.get(new FormalField(String.class), new FormalField(String.class));
                System.out.println();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
