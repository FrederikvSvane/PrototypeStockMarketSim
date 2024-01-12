package dk.dtu;

import java.util.HashMap;
import java.util.UUID;

public class Order {
    private String orderId;
    private String traderId;
    private String stockName;
    private int amount;
    private float price;

    /**
     * Construct an order with a randomly generated orderId
     * @param traderId
     * @param companyName
     * @param amount
     * @param price
     */
    public Order(String traderId, String companyName, int amount, float price) {
        this.traderId = traderId;
        this.orderId = UUID.randomUUID().toString();
        this.stockName = companyName;
        this.amount = amount;
        this.price = price;
    }

    /**
     * Construct an order with a given/known orderId
     * @param traderId
     * @param orderId
     * @param companyName
     * @param amount
     * @param price
     */
    public Order(String traderId, String orderId, String companyName, int amount, float price) {
        this.traderId = traderId;
        this.orderId = orderId;
        this.stockName = companyName;
        this.amount = amount;
        this.price = price;
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

    public String getOrderId() {
        return orderId;
    }

    public String getTraderId() { return traderId;
    }

    public void setAmount(int n) { amount = n; }
}