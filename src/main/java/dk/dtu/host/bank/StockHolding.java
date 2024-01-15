package dk.dtu.host.bank;

public class StockHolding {
    private String companyTicker;
    private int amount;

    public StockHolding(String companyTicker, int amount) {
        this.companyTicker = companyTicker;
        this.amount = amount;
    }

    public String getCompanyTicker() {
        return companyTicker;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int i) { this.amount = i;
    }
}
