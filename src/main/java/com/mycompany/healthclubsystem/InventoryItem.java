package com.mycompany.healthclubsystem;

/**
 * InventoryItem — represents one equipment/supply item in the gym.
 */
public class InventoryItem {
    private int    itemId;
    private String name;
    private String category;   // Equipment | Supplement | Accessory | Other
    private int    quantity;
    private int    minQuantity; // alert threshold
    private double price;
    private String lastUpdated; // YYYY-MM-DD

    public InventoryItem(int itemId, String name, String category,
                         int quantity, int minQuantity, double price, String lastUpdated) {
        this.itemId      = itemId;
        this.name        = name;
        this.category    = category;
        this.quantity    = quantity;
        this.minQuantity = minQuantity;
        this.price       = price;
        this.lastUpdated = lastUpdated;
    }

    public int    getItemId()      { return itemId; }
    public String getName()        { return name; }
    public String getCategory()    { return category; }
    public int    getQuantity()    { return quantity; }
    public int    getMinQuantity() { return minQuantity; }
    public double getPrice()       { return price; }
    public String getLastUpdated() { return lastUpdated; }

    public void setQuantity(int q)      { this.quantity = q; }
    public void setName(String n)       { this.name = n; }
    public void setCategory(String c)   { this.category = c; }
    public void setMinQuantity(int m)   { this.minQuantity = m; }
    public void setPrice(double p)      { this.price = p; }
    public void setLastUpdated(String d){ this.lastUpdated = d; }

    public boolean isLowStock() { return quantity <= minQuantity; }

    public String toCSV() {
        return itemId + "," + name + "," + category + ","
             + quantity + "," + minQuantity + "," + price + "," + lastUpdated;
    }

    public static InventoryItem fromCSV(String csv) {
        String[] p = csv.split(",", 7);
        if (p.length < 7) return null;
        try {
            return new InventoryItem(
                Integer.parseInt(p[0].trim()), p[1].trim(), p[2].trim(),
                Integer.parseInt(p[3].trim()), Integer.parseInt(p[4].trim()),
                Double.parseDouble(p[5].trim()), p[6].trim()
            );
        } catch (Exception e) { return null; }
    }

    @Override
    public String toString() {
        return String.format("%-4d | %-20s | %-12s | Qty: %-4d | Min: %-3d | $%.2f",
            itemId, name, category, quantity, minQuantity, price);
    }
}
