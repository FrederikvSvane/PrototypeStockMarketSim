package dk.dtu;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {


    public static void main(String[] args) throws InterruptedException {

        HostUtil.initialize();
        ClientUtil.initialize();

        SpaceRepository repository = new SpaceRepository();

        Exchange exchange = new Exchange(repository);
        new Thread(exchange).start();


    }
}
