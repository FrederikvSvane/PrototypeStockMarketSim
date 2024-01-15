package dk.dtu.bank;

import dk.dtu.ClientUtil;
import dk.dtu.Order;
import org.jspace.*;

import java.util.List;

public class BankWorker implements Runnable {
    private SpaceRepository bankRepository;
    private Space traderAccountSpace; // {traderId, balance, reservedBalance, stockHoldings, reserve{stockHoldings}
    private Space transactionSpace; // {BrokerID, transactionType, Object[] transactionData}
    private Space transactionResponseSpace; // {BrokerID, response}
    private Space completeOrderSpace; // {spaceName, obj[]}

    public BankWorker(SpaceRepository bankRepository) {
        this.bankRepository = bankRepository;
        traderAccountSpace = this.bankRepository.get("bankInformationSpace");
        transactionSpace = this.bankRepository.get("transactionSpace");
        transactionResponseSpace = this.bankRepository.get("transactionResponseSpace");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Opdater folks bankbalance (aktiebeholdning og pengebalance)
                // Listening for transactions in transactionSpace in bankRepository
                Object[] transaction = transactionSpace.get(new FormalField(String.class), new FormalField(Object.class));
                String brokerId = (String) transaction[0];
                String transactionType = (String) transaction[1];

                switch (transactionType) {
                    case "reserve money": // object[] = {TraderId, price, amount}
                        // get trader account
                        // check if trader has enough money (price * amount)
                        // if trader has enough money, reserve money
                        // else send error message to trader
                        Object[] transactionData = (Object[]) transaction[2];
                        String traderId = (String) transactionData[0];
                        float price = (float) transactionData[1];
                        int amount = (int) transactionData[2];

                        float moneyNow = getTraderMoneyBalance(traderId);
                        float moneyToReserve = price * amount;
                        if (moneyNow >= moneyToReserve) {
                            //Access the account of the trader
                            Object[] traderAccount = getTraderAccount(traderId);
                            float balance = (float) traderAccount[1];
                            balance -= moneyToReserve;

                            float reservedBalance = (float) traderAccount[2];
                            reservedBalance += moneyToReserve;

                            traderAccountSpace.put(traderId, balance, reservedBalance, traderAccount[3]);
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
    public Object[] queryTraderAccount(String traderId) throws InterruptedException {
        Object[] traderAccount = traderAccountSpace.query(new ActualField(traderId), new FormalField(float.class), new FormalField(float.class), new FormalField(List.class), new FormalField(List.class));
        return traderAccount;
    }


    public Object[] getTraderAccount(String traderId) throws InterruptedException {
        Object[] traderAccount = traderAccountSpace.get(new ActualField(traderId), new FormalField(float.class), new FormalField(float.class), new FormalField(List.class), new FormalField(List.class));
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


//                        Transaction transactionData = (Transaction) transaction[1];
//                        String orderId = transactionData.getOrderId();
//                        String companyTicker = transactionData.getCompanyTicker();
//                        RemoteSpace companyOrderSpace = new RemoteSpace(ClientUtil.getHostUri(companyTicker));
//                        Object[] result = companyOrderSpace.getp(new FormalField(String.class) /*companyTicker*/, new ActualField(orderId) /*orderId*/, new FormalField(String.class) /*orderType*/, new FormalField(Order.class) /*order*/);
//                        Order order = (Order) result[3];
//                        String orderType = (String) result[2];
//                        if (order == null) {
//                            throw new RuntimeException("Order not found");
//                        }
//                        if (orderType.equals("buy")) {
//                            String buyerId = transactionData.getBuyerId();
//                            float amountToReserve = order.getAmount() * order.getPrice();
//                            //Acess the account of the trader
//                            Object[] buyerAccount = getTraderAccount(buyerId);
//                            float buyerBalance = (float) buyerAccount[1];
//                            buyerBalance -= amountToReserve;
//
//                            float reservedBalance = (float) buyerAccount[2];
//                            reservedBalance += amountToReserve;
//
//                            traderAccountSpace.put(buyerId, buyerBalance, reservedBalance, buyerAccount[3]);
//                        }
