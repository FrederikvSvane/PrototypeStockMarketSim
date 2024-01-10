package dk.dtu;

class CompanySellOrder extends Order {

    String brokerUUID;

    public CompanySellOrder(String traderId, String brokerUUID, String orderId, String companyName, int amount, float price) {
        super(traderId, orderId, companyName, amount, price);
        this.brokerUUID = brokerUUID;
    }

    public String getbrokerUUID() {
        return brokerUUID;
    }
}
