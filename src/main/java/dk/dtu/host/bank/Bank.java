package dk.dtu.host.bank;

import dk.dtu.client.ClientUtil;
import dk.dtu.host.HostUtil;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.util.ArrayList;
import java.util.List;

public class Bank implements Runnable {

    private SpaceRepository bankRepository;

    // Bank Information Space
    // Structure: (TraderId, balance, reservedBalance, List<StockHolding> {companyTicker, amount}, reserve List<StockHolding> {companyTicker, amount})
    private Space traderAccountSpace;

    // Call space from Broker
    // Structure: (String transactionType, Object transactionData)
    private Space bankRequestSpace;

    // Response space for Broker
    // Structure: (String transactionType, Object transactionData)
    private Space transactionResponseSpace;

    private List<BankWorker> bankWorkers;
    private int numberOfBankWorkers = 10;

    public Bank(SpaceRepository bankRepository) {
        this.bankRepository = bankRepository;
        this.traderAccountSpace = new SequentialSpace();
        this.bankRequestSpace = new SequentialSpace();
        this.transactionResponseSpace = new SequentialSpace();
        this.bankWorkers = new ArrayList<>();

        this.bankRepository.add("bankInformationSpace", this.traderAccountSpace);
        this.bankRepository.add("bankRequestSpace", this.bankRequestSpace);
        this.bankRepository.add("transactionResponseSpace", this.transactionResponseSpace);
        this.bankWorkers = new java.util.ArrayList<>();
        int port = HostUtil.getBankPort();
        String URI = ClientUtil.getHostUri("", port, "keep");
        this.bankRepository.addGate(URI);


    }

    public void run() {
        for (int i = 0; i < numberOfBankWorkers; i++) {
            BankWorker bankWorker = new BankWorker(bankRepository);
            bankWorkers.add(bankWorker);
        }
        for (BankWorker worker : bankWorkers) {
            new Thread(worker).start();
        }


    }


}
