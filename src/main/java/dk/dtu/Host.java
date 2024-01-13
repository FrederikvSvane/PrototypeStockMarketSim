package dk.dtu;

import dk.dtu.company.IRS;
import org.jspace.SpaceRepository;

import java.time.LocalDateTime;

public class Host {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting client");
        HostUtil.initialize("keep");
        ClientUtil.initialize();

        SpaceRepository hostRepo = HostUtil.getHostRepo();

        Exchange exchange = new Exchange(hostRepo);
        new Thread(exchange).start();
        new Thread(new Lobby(hostRepo)).start();
        GlobalCock.initialize(hostRepo, LocalDateTime.of(1700,1,1,0,0,0),2);
        new Thread(new IRS(hostRepo)).start();



    }
}
