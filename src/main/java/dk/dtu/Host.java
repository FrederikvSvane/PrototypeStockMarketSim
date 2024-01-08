package dk.dtu;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    static String hostIp = "10.209.74.151";
    static int hostPort = 32989;

    public static void main(String[] args) throws UnknownHostException {

        SpaceRepository repository = new SpaceRepository();

        Exchange exchange = new Exchange(hostIp, hostPort, repository);
        new Thread(exchange).start();

            // Object[] request = hostSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Order.class));
            // System.out.println("Host received request: " + request[0].toString() + " " + request[1].toString()+ " " + request[2].toString());

    }
}
