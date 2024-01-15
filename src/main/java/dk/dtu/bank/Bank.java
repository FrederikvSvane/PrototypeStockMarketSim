package dk.dtu.bank;

import dk.dtu.ClientUtil;
import dk.dtu.HostUtil;
import dk.dtu.Order;
import org.jspace.*;

import java.util.List;

public class Bank implements Runnable {

    private SpaceRepository bankRepository = new SpaceRepository();


    // Structure: (TraderId, balance, reservedBalance, List<StockHolding> {companyTicker, amount})
    private Space traderAccountSpace = new SequentialSpace();

    // Structure: (String transactionType, Transaction {buyerId, sellerId, companyTicker, orderId})
    private Space transactionSpace = new SequentialSpace();

    public Bank() {
        this.bankRepository.add("bankInformationSpace", this.traderAccountSpace);
        this.bankRepository.add("transactionSpace", this.transactionSpace);
        int port = HostUtil.getHostPort() + 2;
        String URI = ClientUtil.getHostUri("", port, "keep");
        this.bankRepository.addGate(URI);
    }

    public void run() {
        while (true) {
            try {
                // Opdater folks bankbalance (aktiebeholdning og pengebalance)
                Object[] transaction = this.transactionSpace.get(new FormalField(String.class), new FormalField(Transaction.class));
                String transactionType = (String) transaction[0];
                Transaction transactionData = (Transaction) transaction[1];

                switch (transactionType) {
                    case "reserve money":
                        String orderId = transactionData.getOrderId();
                        String companyTicker = transactionData.getCompanyTicker();
                        RemoteSpace companyOrderSpace = new RemoteSpace(ClientUtil.getHostUri(companyTicker));
                        Object[] result = companyOrderSpace.getp(new FormalField(String.class) /*companyTicker*/, new ActualField(orderId) /*orderId*/, new FormalField(String.class) /*orderType*/, new FormalField(Order.class) /*order*/);
                        Order order = (Order) result[3];
                        String orderType = (String) result[2];
                        if (order == null) {
                            throw new RuntimeException("Order not found");
                        }
                        if (orderType.equals("buy")) {
                            String buyerId = transactionData.getBuyerId();
                            float amountToReserve = order.getAmount() * order.getPrice();
                            //Acess the account of the trader
                            Object[] buyerAccount = getTraderAccount(buyerId);
                            float buyerBalance = (float) buyerAccount[1];
                            buyerBalance -= amountToReserve;

                            float reservedBalance = (float) buyerAccount[2];
                            reservedBalance += amountToReserve;

                            traderAccountSpace.put(buyerId, buyerBalance, reservedBalance, buyerAccount[3]);
                        }

                    case "reserve stocks":
                        break;
                    case "complete order":
                        break;
                    case "cancel order":
                        break;

                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * This method returns the specified trader's account (balance, reservedBalance, stockHoldings)
     *
     * @param traderId
     * @return
     * @throws InterruptedException
     */
    public Object[] queryTraderAccount(String traderId) throws InterruptedException {
        Object[] traderAccount = this.traderAccountSpace.query(new ActualField(traderId));
        return traderAccount;
    }


    public Object[] getTraderAccount(String traderId) throws InterruptedException {
        Object[] traderAccount = this.traderAccountSpace.get(new ActualField(traderId));
        return traderAccount;
    }

    public int getTraderMoneyBalance(String traderId) throws InterruptedException {
        Object[] traderAccount = this.queryTraderAccount(traderId);
        return (int) traderAccount[1];
    }

    /**
     * This method loops through the trader's stock holdings and returns the amount of stocks the trader has of the specified company
     *
     * @param traderId
     * @param companyId
     * @return amount of stocks the trader has of the specified company
     * @throws InterruptedException
     */
    public int getTraderStockBalanceOfCompany(String traderId, String companyId) throws InterruptedException {
        Object[] traderAccount = this.queryTraderAccount(traderId);
        List<StockHolding> traderStocks = (List<StockHolding>) traderAccount[3];
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
    public List<StockHolding> getTraderStockHoldings(String traderId) throws InterruptedException {
        Object[] traderAccount = this.queryTraderAccount(traderId);
        return (List<StockHolding>) traderAccount[3];
    }
}
