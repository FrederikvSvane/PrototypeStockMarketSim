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
     * @param traderId
     */
    public BankAccount(String traderId) {
        this.traderId = traderId;
        this.moneyBalance = 100000;
        this.reservedMoneyBalance = 0;
        this.stockHoldings = new ArrayList<>();
        this.reservedStockHoldings = new ArrayList<>();
    }

    public void mergeReservedMoneyAndMoneyBalance() {
        this.moneyBalance -= this.reservedMoneyBalance;
        this.reservedMoneyBalance = 0;
    }

    public void reserveMoneyFromBalance(float amount) {
        this.moneyBalance -= amount;
        this.reservedMoneyBalance += amount;
    }

    public String getTraderId() { return traderId; }
    public float getMoneyBalance() { return moneyBalance; }
    public float getReservedMoneyBalance() { return reservedMoneyBalance; }
    public List<StockHolding> getStockHoldings() { return stockHoldings; }
    public List<StockHolding> getReservedStockHoldings() { return reservedStockHoldings; }
}
