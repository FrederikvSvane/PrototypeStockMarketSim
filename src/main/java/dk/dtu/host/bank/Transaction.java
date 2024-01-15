package dk.dtu.host.bank;

public class Transaction {
    private String buyerId;
    private String sellerId;
    private String companyTicker;
    private String orderId;
    private int amount;

    private float price;

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
        this.amount = amount;
    }

    /**
     * Construct a transaction meant for "reserve money" transactions
     * @param buyerId
     * @param amount
     * @param price
     */
    public Transaction(String buyerId, int amount, float price) {
        this.buyerId = buyerId;
        this.amount = amount;
        this.price = price;
    }

    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }
    public String getCompanyTicker() { return companyTicker; }
    public String getOrderId() { return orderId; }
    public int getAmount() { return amount; }
    public float getPrice() { return price; }
}
