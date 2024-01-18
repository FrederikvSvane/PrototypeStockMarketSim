package dk.dtu.host.bank;

import java.util.ArrayList;
import java.util.List;

public class BankAccount {

    private String traderId;
    private float moneyBalance;
    private float reservedMoneyBalance;
    private List<StockHolding> stockHoldings;
    private List<StockHolding> reservedStockHoldings;

    /**
     * Create a fresh bank account with 100000 USD, no reserved money and no stock holdings
     *
     * @param traderId
     */
    public BankAccount(String traderId) {
        this.traderId = traderId;
        this.moneyBalance = 100000;
        this.reservedMoneyBalance = 0;
        this.stockHoldings = new ArrayList<>();
        this.reservedStockHoldings = new ArrayList<>();
        stockHoldings.add(new StockHolding("IBM", 100));
    }

    public void mergeReservedMoneyAndMoneyBalance() {
        this.moneyBalance -= this.reservedMoneyBalance;
        this.reservedMoneyBalance = 0;
    }

    public void changeReservedMoneyBalance(float amount) {
        if (reservedMoneyBalance - amount >= 0) {
            this.reservedMoneyBalance -= amount;
        } else {
            throw new RuntimeException("Not enough reserved money");
        }
    }

    public void changeMoney(float amount) {
        this.moneyBalance += amount;
    }

    public void reserveMoneyFromBalance(float amount) {
        this.moneyBalance -= amount;
        this.reservedMoneyBalance += amount;
    }

    public String reserveStocksFromBalance(String companyTicker, int amountToReserve) {
        // check if trader has enough stocks by looping through stockHoldings and checking if the trader has the companyTicker and if the amountToReserve is >= amountToReserve
        for (StockHolding stock : stockHoldings) {
            if (stock.getCompanyTicker().equals(companyTicker)) {
                if (stock.getAmount() - amountToReserve >= 0) {
                    // the trader has enough stocks
                    // the amountToReserve of stocks the trader has in his reservedStockHoldings is increased by amountToReserve
                    boolean foundReservedStock = false;
                    for (StockHolding reservedStock : reservedStockHoldings) {
                        if (reservedStock.getCompanyTicker().equals(companyTicker)) {
                            foundReservedStock = true;
                            stock.setAmount(stock.getAmount() - amountToReserve);
                            reservedStock.setAmount(reservedStock.getAmount() + amountToReserve);
                        }
                    }
                    if (!foundReservedStock) {
                        stock.setAmount(stock.getAmount() - amountToReserve);
                        reservedStockHoldings.add(new StockHolding(companyTicker, amountToReserve));
                    }
                    return "stocks reserved";
                } else {
                    return "not enough stocks";
                }
            }
        }
        return "'t own this stock";
    }


    public String getTraderId() {
        return traderId;
    }

    public float getMoneyBalance() {
        return moneyBalance;
    }

    public float getReservedMoneyBalance() {
        return reservedMoneyBalance;
    }

    public List<StockHolding> getStockHoldings() {
        return stockHoldings;
    }

    public List<StockHolding> getReservedStockHoldings() {
        return reservedStockHoldings;
    }


    public void changeStockHoldings(String companyTicker, int amount) {
        boolean foundStock = false;
        for (StockHolding stock : stockHoldings) {
            if (stock.getCompanyTicker().equals(companyTicker)) {
                foundStock = true;
                stock.setAmount(stock.getAmount() + amount);
            }
        }
        if (!foundStock) {
            stockHoldings.add(new StockHolding(companyTicker, amount));
        }
    }

    public String showStockHoldings(){
        String data = "";
        for (StockHolding stock : stockHoldings) {
            data = (stock.getCompanyTicker() + ": " + stock.getAmount())+"\n";
        }
        return data;
    }

    public String showReservedStockHoldings(){
        String data = "";
        for (StockHolding stock : reservedStockHoldings) {
            data = (stock.getCompanyTicker() + ": " + stock.getAmount())+"\n";
        }
        return data;
    }

    public String showAccount(){
        return "TraderId: " + traderId + "\n" +
                "MoneyBalance: " + moneyBalance + "\n" +
                "ReservedMoneyBalance: " + reservedMoneyBalance + "\n" +
                "StockHoldings: \n" + showStockHoldings() + "\n" +
                "ReservedStockHoldings: \n" + showReservedStockHoldings() + "\n";
    }
}
