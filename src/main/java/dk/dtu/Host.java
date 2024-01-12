package dk.dtu;

import org.jspace.SpaceRepository;

import java.time.LocalDateTime;

public class Host {

    public static void main(String[] args) throws InterruptedException {


        HostUtil.initialize("keep");
        ClientUtil.initialize();

        SpaceRepository hostRepo = HostUtil.getHostRepo();


        Exchange exchange = new Exchange(hostRepo);
        new Thread(exchange).start();
        GlobalCock.initialize(hostRepo, LocalDateTime.of(1980,1,1,0,0,0),2);
        new Thread(new Lobby(hostRepo)).start();


    }
}
