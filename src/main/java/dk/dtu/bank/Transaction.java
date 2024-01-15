package dk.dtu.bank;

public class Transaction {
    private String buyerId;
    private String sellerId;
    private String companyTicker;
    private String orderId;
    private int amount;

    public Transaction(String buyerId, String sellerId, String companyTicker, String orderId, int amount) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.companyTicker = companyTicker;
        this.orderId = orderId;
        this.amount = amount;
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

    public static class Splitter implements Runnable {
        private int version;

        public Splitter(int version) {
            this.version = version;
        }

        @Override
        public void run() {
            switch (version) {
                case 1:
                    //TODO implement version 1
                    break;
                case 2:
                    //TODO implement version 2
                    break;
                case 3:
                    //TODO implement version 3
                    break;
                default:
                    throw new RuntimeException("Invalid version number");
            }
        }
    }
}
