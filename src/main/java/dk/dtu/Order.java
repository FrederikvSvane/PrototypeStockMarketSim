package dk.dtu;

import java.util.HashMap;
import java.util.UUID;

class Order {
    private String orderId;
    private String traderId;
    private String stockName;
    private int amount;
    private float price;

    private HashMap<String, String> tickerMap = new HashMap<String, String>();

    public Order(String traderId, String stockName, int amount, float price) {
        this.orderId = UUID.randomUUID().toString();
        this.stockName = stockName;
        this.amount = amount;
        this.price = price;

        tickerMap.put("Apple", "AAPL");
    }

    public Order(String traderId, String orderId, String stockName, int amount, float price) {
        this.traderId = traderId;
        this.orderId = orderId;
        this.stockName = stockName;
        this.amount = amount;
        this.price = price;

        tickerMap.put("Apple", "AAPL");
    }

    @Override
    public String toString() {
        return "Order{" +
                "stockName='" + stockName + '\'' +
                ", amount=" + amount +
                ", price=" + price +
                '}';
    }

    public float getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public String getStockName() {
        return stockName;
    }

    public String getTicker() {
        return tickerMap.get(stockName);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTraderId() { return traderId;
    }

    public void setAmount(int n) { amount = n; }
}