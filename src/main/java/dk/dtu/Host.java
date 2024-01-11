package dk.dtu;

import org.jspace.SpaceRepository;

public class Host {

    public static void main(String[] args) throws InterruptedException {

        //The starting time of the host
        long startTimeUnix = System.currentTimeMillis();

        HostUtil.initialize();
        ClientUtil.initialize();

        SpaceRepository repository = new SpaceRepository();


        Exchange exchange = new Exchange(repository);
        new Thread(exchange).start();
        GlobalCock.initialize(repository,startTimeUnix);
        new Thread(new Lobby(repository)).start();


    }
}
