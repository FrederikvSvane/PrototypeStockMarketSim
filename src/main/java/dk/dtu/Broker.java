package dk.dtu;

import java.util.UUID;
import org.jspace.*;
public class Broker implements Runnable {

    String brokerUuid;
    SequentialSpace requestSpace = new SequentialSpace();

    public Broker(String brokerUuid){
        this.brokerUuid = brokerUuid;
        this.requestSpace = new SequentialSpace();
    }

    public Space getSpace(){
        return requestSpace;
    }

    public void run() {
        while(true) {
            try {
                System.out.println("Broker " + brokerUuid + " waiting for order");
                Object[] request = requestSpace.get(new ActualField(brokerUuid), new FormalField(String.class), new FormalField(Order.class));
                System.out.println("Broker " + request[0].toString() + " received order" + request[2].toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
