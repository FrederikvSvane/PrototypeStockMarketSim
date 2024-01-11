package dk.dtu;

import org.jspace.SpaceRepository;

public class Host {

    public static void main(String[] args) throws InterruptedException {

        //The starting time of the host
        long startTimeUnix = System.currentTimeMillis() / 1000L;

        HostUtil.initialize();
        ClientUtil.initialize();

        SpaceRepository repository = new SpaceRepository();
        GlobalCock.initialize(repository,startTimeUnix);

        Exchange exchange = new Exchange(repository);
        new Thread(exchange).start();
        new Thread(new Lobby(repository)).start();


    }
}
