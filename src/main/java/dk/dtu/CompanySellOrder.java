package dk.dtu;

class CompanySellOrder extends Order {

    String brokerUUID;

    //TODO: hvorfor er denne ikke brugt
    public CompanySellOrder(String traderId, String brokerUUID, String orderId, String companyName, String companyTicker, int amount, float price) {
        super(traderId, orderId, companyName, companyTicker, amount, price);
        this.brokerUUID = brokerUUID;
    }

    public String getbrokerUUID() {
        return brokerUUID;
    }
}
