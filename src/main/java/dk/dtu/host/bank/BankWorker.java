package dk.dtu.host.bank;

import dk.dtu.client.ClientUtil;
import dk.dtu.host.HostUtil;
import dk.dtu.client.Order;
import org.jspace.*;

import java.io.IOException;
import java.util.List;

public class BankWorker implements Runnable {
    private SpaceRepository bankRepository;
    private Space traderAccountSpace; // {traderId, BankAccount} + token
    private Space bankRequestSpace; // {BrokerID, transactionType, Object[] transactionData}
    private Space transactionResponseSpace; // {BrokerID, response}
    private Space completeOrderSpace = new SequentialSpace(); // {spaceName, obj[]}

    public BankWorker(SpaceRepository bankRepository) throws InterruptedException {
        this.bankRepository = bankRepository;
        traderAccountSpace = this.bankRepository.get("bankInformationSpace");
        bankRequestSpace = this.bankRepository.get("bankRequestSpace");
        transactionResponseSpace = this.bankRepository.get("transactionResponseSpace");
        completeOrderSpace.put("readyToken");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Opdater folks bankbalance (aktiebeholdning og pengebalance)
                // Listening for transactions in transactionSpace in bankRepository
                Object[] result = bankRequestSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Transaction.class));
                String brokerId = (String) result[0];
                String transactionType = (String) result[1];
                Transaction transaction = (Transaction) result[2];
                int amount;
                String traderId;
                String buyerId;
                String companyTicker;
                BankAccount traderAccount;

                switch (transactionType) {
                    case "reserve money":
                        buyerId = transaction.getBuyerId();
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
                    case "finalize transaction":
                        // in object[] = {Transaction transactionData}
                        // out {BrokerID, response} // response = "completed order" or "order not found"
                        // Skal følge petri net


                        // "inGetBuyer", {tradeId, companyTicker, amount}
                        // "inGetSeller", {tradeId, companyTicker, amount}
                        // "inQueryInfo", {tradeId, companyTicker, amount}
                        // "outGetBuyer",

                        // initialize threads
                        Thread getBuyerThread = new Thread(new InformationCollector(completeOrderSpace, "GetBuyer"));
                        Thread getSellerThread = new Thread(new InformationCollector(completeOrderSpace, "GetSeller"));
                        Thread queryInfoThread = new Thread(new InformationCollector(completeOrderSpace, "QueryInfo"));
                        getBuyerThread.start();
                        getSellerThread.start();
                        queryInfoThread.start();

                        buyerId = transaction.getBuyerId();
                        completeOrderSpace.put("inGetBuyer", buyerId);
                        String sellerId = transaction.getSellerId();
                        completeOrderSpace.put("inGetSeller", sellerId);
                        String orderId = transaction.getOrderId();
                        companyTicker = transaction.getCompanyTicker();
                        completeOrderSpace.put("inQueryInfo", orderId, companyTicker);



                        get_token();

                        // All 3 threads are done and dead
                        Object[] getSellerInfo = completeOrderSpace.get(new ActualField("outGetSeller"), new FormalField(String.class), new FormalField(BankAccount.class));
                        completeOrderSpace.get(new ActualField("outQueryInfo"));
                        Object[] getBuyerInfo = completeOrderSpace.get(new ActualField("outGetBuyer"), new FormalField(String.class), new FormalField(BankAccount.class));
                        System.out.println(getBuyerInfo[1] + " " + getSellerInfo[1]);

                        BankAccount buyerAccount = (BankAccount) getBuyerInfo[2];
                        BankAccount sellerAccount = (BankAccount) getSellerInfo[2];
                        System.out.println(buyerAccount + " " + sellerAccount);

                        completeOrderSpace.put("readyToFinalize",buyerAccount,sellerAccount, transaction);

                        finalizeTransaction();
                        transactionResponseSpace.put(brokerId, "order: " + orderId + " fulfilled");
                        break;
                    case "reserve stocks":
                        // in {tradeId, companyTicker, amount}
                        // out {BrokerID, response} // response = "enough stocks" or "not enough stocks"
                        traderId = transaction.getBuyerId();
                        companyTicker = transaction.getCompanyTicker();
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
                        break;
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void finalizeTransaction() throws InterruptedException, IOException {
        Object[] info = completeOrderSpace.get(new ActualField("readyToFinalize"), new FormalField(BankAccount.class), new FormalField(BankAccount.class), new FormalField(Transaction.class));
        BankAccount buyerAccount = (BankAccount) info[1];
        BankAccount sellerAccount = (BankAccount) info[2];
        Transaction transaction = (Transaction) info[3];
        int amount = transaction.getAmountOfStocks();
        String orderId = transaction.getOrderId();
        String companyTicker = transaction.getCompanyTicker();
        RemoteSpace companyStockSpace = new RemoteSpace(ClientUtil.getHostUri(companyTicker, HostUtil.getExchangePort(), "keep"));
        // traderId, orderId, orderType, order, reservedAmount
        Object[] orderInfo = companyStockSpace.getp(new FormalField(String.class), new ActualField(orderId), new ActualField("sell"), new FormalField(Order.class), new FormalField(Integer.class));
        Order order = (Order) orderInfo[3];
        int reservedAmount = (int) orderInfo[4];
        float price = order.getPrice();
        float moneyToTransfer = price * amount;
        sellerAccount.changeStockHoldings(companyTicker, -amount);
        buyerAccount.changeReservedMoneyBalance(moneyToTransfer);
        sellerAccount.changeMoney(moneyToTransfer);
        buyerAccount.changeStockHoldings(companyTicker, amount);

        putTraderAccount(buyerAccount);
        putTraderAccount(sellerAccount);
        if (!(order.getAmount() ==0 && reservedAmount == amount)) {
            // we need to update the order and put it back in the companyStockSpace
            order.setAmount(order.getAmount() - amount);
            reservedAmount -= amount;
            companyStockSpace.put(order.getTraderId(), order.getOrderId(), "sell", order, reservedAmount);
        } // else the order is deleted

        traderAccountSpace.put("token");
        completeOrderSpace.put("readyToken");
    }

    private void get_token() {
        System.out.println("Im in get:token");
        try {
            traderAccountSpace.get(new ActualField("token"));
            completeOrderSpace.put("token");
            completeOrderSpace.put("token");
        } catch (InterruptedException e) {
            e.printStackTrace();
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