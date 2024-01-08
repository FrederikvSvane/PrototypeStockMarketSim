package dk.dtu;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;
import org.jspace.*;
public class Broker implements Runnable {

    String brokerUuid;
    SequentialSpace requestSpace;
    String hostIp;
    int hostPort;
    String hostUri;

    public Broker(String brokerUuid, String hostIp, int hostPort){
        this.brokerUuid = brokerUuid;
        this.requestSpace = new SequentialSpace();
        this.hostIp = hostIp;
        this.hostPort = hostPort;
        this.hostUri = getHostUri("host");
    }

    private String getHostUri(String roomName){
        return "tcp://" + hostIp + ":" + hostPort + "/" + roomName + "?keep";
    }
    public Space getSpace(){
        return requestSpace;
    }

    public void run() {
        while(true) {
            try {
                Object[] request = requestSpace.get(new ActualField(brokerUuid), new FormalField(String.class), new FormalField(Order.class));
                System.out.println("Broker " + request[0].toString() + " received order" + request[2].toString());
                RemoteSpace hostSpace = new RemoteSpace(hostUri);
                hostSpace.put((String) request[0], (String) request[1], (Order) request[2]);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error in broker");
            }
        }
    }
}
