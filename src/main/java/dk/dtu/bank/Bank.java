package dk.dtu.bank;

import dk.dtu.ClientUtil;
import dk.dtu.HostUtil;
import dk.dtu.Order;
import org.jspace.*;

import java.util.List;

public class Bank implements Runnable {

    private SpaceRepository bankRepository = new SpaceRepository();

    // Bank Information Space
    // Structure: (TraderId, balance, reservedBalance, List<StockHolding> {companyTicker, amount}, reserve List<StockHolding> {companyTicker, amount})
    private Space traderAccountSpace = new SequentialSpace();


    // Call space from Broker
    // Structure: (String transactionType, Object transactionData)
    private Space transactionSpace = new SequentialSpace();

    // Response space for Broker
    // Structure: (String transactionType, Object transactionData)
    private Space transactionResponseSpace = new SequentialSpace();

    private List<BankWorker> bankWorkers;
    private int numberOfBankWorkers = 10;
    public Bank() {
        this.bankRepository.add("bankInformationSpace", this.traderAccountSpace);
        this.bankRepository.add("transactionSpace", this.transactionSpace);
        this.bankRepository.add("transactionResponseSpace", this.transactionResponseSpace);
        int port = HostUtil.getHostPort() + 2;
        String URI = ClientUtil.getHostUri("", port, "keep");
        this.bankRepository.addGate(URI);


    }

    public void run() {
        for (int i = 0; i < numberOfBankWorkers; i++) {
            bankWorkers.add(new BankWorker(bankRepository));
        }
        for (BankWorker worker : bankWorkers) {
            new Thread(worker).start();
        }


    }


}
