package dk.dtu;

import dk.dtu.company.IRS;
import org.jspace.SpaceRepository;

import java.time.LocalDateTime;

public class Host {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting client");
        HostUtil.initialize("keep");
        System.out.println("Port" + HostUtil.getHostPort());
        ClientUtil.initialize();

        SpaceRepository hostRepo = HostUtil.getHostRepo();
        GlobalCock.initialize(hostRepo, LocalDateTime.of(1850,1,1,0,0,0),2000);
        Thread.sleep(1000);

        Exchange exchange = new Exchange(hostRepo);
        new Thread(exchange).start();
        new Thread(new Lobby(hostRepo)).start();
        new Thread(new IRS(hostRepo)).start();
    }
}
