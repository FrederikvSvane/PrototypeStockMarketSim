package dk.dtu;

import org.jspace.SpaceRepository;

public class Host {

    public static void main(String[] args) throws InterruptedException {

        HostUtil.initialize();
        ClientUtil.initialize();

        SpaceRepository repository = new SpaceRepository();

        Exchange exchange = new Exchange(repository);
        new Thread(exchange).start();
        new Thread(new Lobby(repository)).start();


    }
}
