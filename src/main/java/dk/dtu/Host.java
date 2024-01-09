package dk.dtu;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    static String hostIp = "10.209.69.169";
    static int hostPort = 32989;

    public static void main(String[] args) throws UnknownHostException {

        SpaceRepository repository = new SpaceRepository();

        Exchange exchange = new Exchange(hostIp, hostPort, repository);
        new Thread(exchange).start();
        new Thread(new Lobby(hostIp, hostPort, repository)).start();


    }
}
