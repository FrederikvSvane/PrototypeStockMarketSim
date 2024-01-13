package dk.dtu.bank;

public class Transaction {
    private String buyerId;
    private String sellerId;
    private String companyTicker;
    private String orderId;

    public Transaction(String buyerId, String sellerId, String companyTicker, String orderId) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.companyTicker = companyTicker;
        this.orderId = orderId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getCompanyTicker() {
        return companyTicker;
    }

    public String getOrderId() {
        return orderId;
    }
}
