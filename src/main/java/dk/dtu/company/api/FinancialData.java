package dk.dtu.company.api;

public class FinancialData {
    private long revenue;
    private long costOfRevenue;
    private long grossProfit;
    private long cash;
    private long netReceiveable;
    private long totalAssets;
    private long totalDebt;

    // Constructor
    public FinancialData(long revenue, long costOfRevenue, long grossProfit) {
        this.revenue = revenue;
        this.costOfRevenue = costOfRevenue;
        this.grossProfit = grossProfit;
        this.cash = 0;
        this.netReceiveable = 0;
        this.totalAssets = 0;
        this.totalDebt = 0;
    }
    public FinancialData(long revenue, long costOfRevenue, long grossProfit , long cash, long netReceiveable, long totalAssets, long totalDebt) {
        this.revenue = revenue;
        this.costOfRevenue = costOfRevenue;
        this.grossProfit = grossProfit;
        this.cash = cash;
        this.netReceiveable = netReceiveable;
        this.totalAssets = totalAssets;
        this.totalDebt = totalDebt;
    }


    // Getters
    public long getRevenue() {
        return revenue;
    }

    public long getCostOfRevenue() {
        return costOfRevenue;
    }

    public long getGrossProfit() {
        return grossProfit;
    }

    // toString method for easy printing
    @Override
    public String toString() {
        return "Revenue: " + revenue + ", Cost of Revenue: " + costOfRevenue + ", Gross Profit: " + grossProfit;
    }
}
