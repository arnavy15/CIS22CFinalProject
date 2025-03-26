public class Order {
    public enum ShippingMethod {
        OVERNIGHT, RUSH, STANDARD
    }

    private int orderID;
    private String username;     // who placed the order
    private String productName;  // what product
    private ShippingMethod shippingMethod;
    private boolean shipped;

    public Order(int orderID, String username, String productName, ShippingMethod shippingMethod) {
        this.orderID = orderID;
        this.username = username;
        this.productName = productName;
        this.shippingMethod = shippingMethod;
        this.shipped = false;
    }

    public int getOrderID() { return orderID; }
    public String getUsername() { return username; }
    public String getProductName() { return productName; }
    public ShippingMethod getShippingMethod() { return shippingMethod; }
    public boolean isShipped() { return shipped; }
    public void setShipped(boolean shipped) { this.shipped = shipped; }

    // Priority: OVERNIGHT (high), RUSH (medium), STANDARD (low)
    public int getPriority() {
        switch (shippingMethod) {
            case OVERNIGHT: return 3;
            case RUSH: return 2;
            default: return 1;
        }
    }

    @Override
    public String toString() {
        return String.format("OrderID=%d, User=%s, Product=%s, Shipping=%s, Shipped=%b",
                orderID, username, productName, shippingMethod, shipped);
    }
}
