public class Product {
    // Primary key: name
    // Secondary key: type
    private String name;
    private String type;
    private double price;
    private String brand;
    private int quantityInStock;
    private String description;

    public Product(String name, String type, double price, String brand, int quantityInStock, String description) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.brand = brand;
        this.quantityInStock = quantityInStock;
        this.description = description;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public String getBrand() { return brand; }
    public int getQuantityInStock() { return quantityInStock; }
    public String getDescription() { return description; }

    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setQuantityInStock(int qty) { this.quantityInStock = qty; }

    @Override
    public String toString() {
        return String.format("[Name=%s, Type=%s, Brand=%s, Price=%.2f, Qty=%d, Desc=%s]",
                name, type, brand, price, quantityInStock, description);
    }
}