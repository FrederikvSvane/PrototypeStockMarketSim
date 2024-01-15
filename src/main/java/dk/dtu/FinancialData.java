package dk.dtu;

public class FinancialData {
    private long revenue;
    private long costOfRevenue;
    private long grossProfit;

    // Constructor
    public FinancialData(long revenue, long costOfRevenue, long grossProfit) {
        this.revenue = revenue;
        this.costOfRevenue = costOfRevenue;
        this.grossProfit = grossProfit;
    }
    public FinancialData(long cash,) {


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
