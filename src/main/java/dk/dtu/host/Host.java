package dk.dtu.host;

import dk.dtu.chat.Lobby;
import dk.dtu.client.ClientUtil;
import dk.dtu.company.IRS;
import dk.dtu.host.bank.Bank;
import dk.dtu.host.exchange.Exchange;
import org.jspace.SpaceRepository;


import java.io.IOException;
import java.time.LocalDateTime;

public class Host {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Starting client");
        HostUtil.initialize();
        ClientUtil.initialize();

        SpaceRepository exchangeRepo = new SpaceRepository();
        new Thread(new Exchange(exchangeRepo)).start();

        SpaceRepository chatRepo = new SpaceRepository();
        new Thread(new Lobby(chatRepo)).start();

        SpaceRepository bankRepo = new SpaceRepository();
        new Thread(new Bank(bankRepo)).start();

        SpaceRepository clockRepo = new SpaceRepository();
        LocalDateTime startDateTime = LocalDateTime.of(2019,1,1,0,2,0);
        GlobalClock.initialize(clockRepo, startDateTime,1);

        SpaceRepository IrsRepo = new SpaceRepository();
        new Thread(new IRS(IrsRepo, "realistic"/*"realistic"*/)).start();
    }
}
