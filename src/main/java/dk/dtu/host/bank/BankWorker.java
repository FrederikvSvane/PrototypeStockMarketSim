package dk.dtu.host.bank;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.util.List;

public class BankWorker implements Runnable {
    private SpaceRepository bankRepository;
    private Space traderAccountSpace; // {traderId, BankAccount}
    private Space bankRequestSpace; // {BrokerID, transactionType, Object[] transactionData}
    private Space transactionResponseSpace; // {BrokerID, response}
    private Space completeOrderSpace; // {spaceName, obj[]}

    public BankWorker(SpaceRepository bankRepository) {
        this.bankRepository = bankRepository;
        traderAccountSpace = this.bankRepository.get("bankInformationSpace");
        bankRequestSpace = this.bankRepository.get("bankRequestSpace");
        transactionResponseSpace = this.bankRepository.get("transactionResponseSpace");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Opdater folks bankbalance (aktiebeholdning og pengebalance)
                // Listening for transactions in transactionSpace in bankRepository
                Object[] result = bankRequestSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Object.class));
                String brokerId = (String) result[0];
                String transactionType = (String) result[1];
                Transaction transaction = (Transaction) result[2];
                int amount;
                String traderId;
<<<<<<< HEAD
                String buyerId;
                String companyTicker;
                BankAccount traderAccount;
=======
>>>>>>> parent of 895e6f6 (BankWorker Update with transaction)

                switch (transactionType) {
                    case "reserve money":
                        String buyerId = transaction.getBuyerId();
                        float price = transaction.getAmountOfMoney();
                        amount = transaction.getAmountOfStocks();

                        traderAccount = getTraderAccount(buyerId);

                        float currentMoneyBalance = traderAccount.getMoneyBalance();
                        float moneyToReserve = price * amount;
                        if (currentMoneyBalance >= moneyToReserve) {
                            traderAccount.reserveMoneyFromBalance(moneyToReserve);

                            traderAccountSpace.put(buyerId, traderAccount);
                            transactionResponseSpace.put(brokerId, "reserved money");
                        } else {
                            transactionResponseSpace.put(brokerId, "not enough money");
                        }

                        break;
                    case "complete order":
                        // in object[] = {Transaction transactionData}
                        // out {BrokerID, response} // response = "completed order" or "order not found"
                        // Skal følge petri net



                        break;
                    case "reserve stocks":
                        // in {tradeId, companyTicker, amount}
                        // out {BrokerID, response} // response = "enough stocks" or "not enough stocks"
                        traderId = transaction.getBuyerId();
                        String companyTicker = transaction.getCompanyTicker();
                        amount = transaction.getAmountOfStocks();
                        BankAccount account = getTraderAccount(traderId); // Get trader account from traderAccountSpace
                        String response = account.reserveStocksFromBalance(companyTicker, amount);
                        putTraderAccount(account);
                        transactionResponseSpace.put(brokerId, response);
                        break;
                    case "unreserve money":
                        // in {tradeId, money}
                        // out {BrokerID, response} // response = "unreserved money" or "not enough money"
                        float unusedMoney = transaction.getAmountOfMoney();
                        traderId = transaction.getBuyerId();
                        BankAccount bankAccount = getTraderAccount(traderId);
                        bankAccount.changeReservedMoneyBalance(unusedMoney);
                        putTraderAccount(bankAccount);
                        transactionResponseSpace.put(brokerId, "unreserved money");
                        break;
<<<<<<< HEAD
                    case "establish account":
                        if (transaction.getBuyerId() != null) {
                            traderId = transaction.getBuyerId();
                            Object[] traderAcc = traderAccountSpace.queryp(new ActualField(traderId), new FormalField(BankAccount.class));
                            if (traderAcc != null) {
                                transactionResponseSpace.put(brokerId, "trader already has account");
                                break;
                            }
                            traderAccount = new BankAccount(traderId);
                            putTraderAccount(traderAccount);
                            transactionResponseSpace.put(brokerId, "account established");
                        }
=======
                    case "join bank":

>>>>>>> parent of 895e6f6 (BankWorker Update with transaction)
                        break;
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method returns the specified trader's account (traderId, balance, reservedBalance, stockHoldings, reserve{stockHoldings})
     *
     * @param traderId
     * @return
     * @throws InterruptedException
     */
    public BankAccount queryTraderAccount(String traderId) throws InterruptedException {
        Object[] result = traderAccountSpace.query(new ActualField(traderId), new FormalField(BankAccount.class));
        if (result.length == 0) {
            throw new RuntimeException("Trader account not found");
        }
        return (BankAccount) result[1];
    }

    public BankAccount getTraderAccount(String traderId) throws InterruptedException {
        Object[] traderAccount = traderAccountSpace.get(new ActualField(traderId), new FormalField(BankAccount.class));
        return (BankAccount) traderAccount[1];
    }

    public void putTraderAccount(BankAccount bankAccount) throws InterruptedException {
        String traderId = bankAccount.getTraderId();
        traderAccountSpace.put(traderId, bankAccount);
    }

    public float queryTraderMoneyBalance(String traderId) throws InterruptedException {
        BankAccount traderAccount = this.queryTraderAccount(traderId);
        return traderAccount.getMoneyBalance();
    }

    /**
     * This method loops through the trader's stock holdings and returns the amount of stocks the trader has of the specified company
     *
     * @param traderId
     * @param companyId
     * @return amount of stocks the trader has of the specified company
     * @throws InterruptedException
     */
    public int queryTraderStockBalanceOfCompany(String traderId, String companyId) throws InterruptedException {
        BankAccount traderAccount = this.queryTraderAccount(traderId);
        List<StockHolding> traderStocks = traderAccount.getStockHoldings();
        for (StockHolding stock : traderStocks) {
            if (stock.getCompanyTicker().equals(companyId)) {
                return stock.getAmount();
            }
        }
        return 0;
    }

    /**
     * This method returns the specified trader's stock holdings as a List<StockHolding> {companyTicker, amount}
     *
     * @param traderId
     * @return
     * @throws InterruptedException
     */
    public List<StockHolding> queryTraderStockHoldings(String traderId) throws InterruptedException {
        BankAccount traderAccount = this.queryTraderAccount(traderId);
        return traderAccount.getStockHoldings();
    }
}