package dk.dtu;

import java.util.UUID;
import org.jspace.*;
public class Broker implements Runnable {

    UUID brokerUuid;
    SequentialSpace requestSpace = new SequentialSpace();

    public Broker(UUID brokerUuid){
        this.brokerUuid = UUID.randomUUID();
        this.requestSpace = new SequentialSpace();
    }

    public Space getSpace(){
        return requestSpace;
    }

    public void run() {
        while(true) {


        }
    }
}
