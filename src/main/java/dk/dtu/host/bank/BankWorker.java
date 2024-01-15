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

                switch (transactionType) {
                    case "reserve money":
                        String buyerId = transaction.getBuyerId();
                        float price = transaction.getPrice();
                        int amount = transaction.getAmount();

                        BankAccount traderAccount = getTraderAccount(buyerId);

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
                    case "enough stocks":
                        // in {tradeId, companyTicker, amount}
                        // out {BrokerID, response} // response = "enough stocks" or "not enough stocks"
                        Object[] transactionDataES = (Object[]) transaction[2];
                        String traderIdES = (String) transactionDataES[0];
                        String companyTickerES = (String) transactionDataES[1];
                        int amountES = (int) transactionDataES[2];
                        Object[] accountES = getTraderAccount(traderIdES);
                        List<StockHolding> stockHoldingsES = (List<StockHolding>) accountES[3];
                        boolean foundStock = false;
                        // check if trader has enough stocks by looping through stockHoldings and checking if the trader has the companyTicker and if the amount is >= amount
                        for (StockHolding stock : stockHoldingsES) {
                            if (stock.getCompanyTicker().equals(companyTickerES)) {
                                foundStock = true;
                                if (stock.getAmount() >= amountES) {
                                    // the trader has enough stocks
                                    // the amount of stocks the trader has in his reservedStockHoldings is increased by amount
                                    List<StockHolding> reservedStockHoldingsES = (List<StockHolding>) accountES[4];
                                    boolean foundReservedStock = false;
                                    boolean notEnoughStocks = false;
                                    for (StockHolding reservedStock : reservedStockHoldingsES) {
                                        if (reservedStock.getCompanyTicker().equals(companyTickerES)) {
                                            if(stock.getAmount()-reservedStock.getAmount() >= amountES) {
                                                reservedStock.setAmount(reservedStock.getAmount() + amountES);
                                                foundReservedStock = true;
                                            } else {
                                                notEnoughStocks = true;
                                            }
                                        }
                                    }
                                    if (!foundReservedStock) {
                                        reservedStockHoldingsES.add(new StockHolding(companyTickerES, amountES));
                                    }
                                    traderAccountSpace.put(traderIdES, accountES[1], accountES[2], stockHoldingsES, reservedStockHoldingsES);
                                    if (notEnoughStocks){
                                        transactionResponseSpace.put(brokerId, "not enough stocks");
                                    } else {
                                        transactionResponseSpace.put(brokerId, "enough stocks");
                                    }

                                } else {
                                    transactionResponseSpace.put(brokerId, "not enough stocks");
                                }
                                break;
                            }
                        }
                        if (!foundStock) {
                            transactionResponseSpace.put(brokerId, "you don't own this stock");
                        }
                        break;
                    case "unreserve money":
                        // in {tradeId, money}
                        // out {BrokerID, response} // response = "unreserved money" or "not enough money"
                        Object[] transactionDataUnR = (Object[]) transaction[2];
                        String traderIdUnR = (String) transactionDataUnR[0];
                        float priceUnR = (float) transactionDataUnR[1];
                        Object[] person = getTraderAccount(traderIdUnR);
                        float reservedBalance = (float) person[2];
                        reservedBalance -= priceUnR;
                        traderAccountSpace.put(traderIdUnR, person[1], reservedBalance, person[3], person[4]);
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