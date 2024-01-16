package dk.dtu.host.bank;

public class Transaction {
    private String buyerId;
    private String sellerId;
    private String companyTicker;
    private String orderId;
    private int amountOfStocks;
    private float amountOfMoney;


    /**
     * Construct a transaction meant for "buy" and "sell" transactions
     * @param buyerId
     * @param sellerId
     * @param companyTicker
     * @param orderId
     * @param amount
     */
    public Transaction(String buyerId, String sellerId, String companyTicker, String orderId, int amount) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.companyTicker = companyTicker;
        this.orderId = orderId;
        this.amountOfStocks = amount;
    }

    /**
     * Construct a transaction meant for "reserve money" transactions
     * @param buyerId
     * @param amount
     * @param price
     */
    public Transaction(String buyerId, int amount, float price) {
        this.buyerId = buyerId;
        this.amountOfStocks = amount;
        this.amountOfMoney = price;
    }

    public Transaction(String buyerId, float price) {
        this.buyerId = buyerId;
        this.amountOfMoney = price;
    }


    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }
    public String getCompanyTicker() { return companyTicker; }
    public String getOrderId() { return orderId; }
    public int getAmountOfStocks() { return amountOfStocks; }
    public float getAmountOfMoney() { return amountOfMoney; }
}
