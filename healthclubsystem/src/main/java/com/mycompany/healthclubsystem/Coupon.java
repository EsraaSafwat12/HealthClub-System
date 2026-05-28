package com.mycompany.healthclubsystem;

/**
 * Coupon — discount system.
 * Admin creates a coupon with a code, expiry date, and value (% or fixed amount).
 */
public class Coupon {
    public enum Type { PERCENTAGE, FIXED }

    private String code;
    private Type   type;
    private double value;       // percent (0-100) OR fixed amount
    private String expiryDate;  // YYYY-MM-DD
    private boolean active;

    public Coupon(String code, Type type, double value, String expiryDate) {
        this.code       = code;
        this.type       = type;
        this.value      = value;
        this.expiryDate = expiryDate;
        this.active     = true;
    }

    public String  getCode()       { return code; }
    public Type    getType()       { return type; }
    public double  getValue()      { return value; }
    public String  getExpiryDate() { return expiryDate; }
    public boolean isActive()      { return active; }
    public void    setActive(boolean a) { this.active = a; }

    /** Apply coupon to an amount and return the discounted amount. */
    public double apply(double amount) {
        if (!active) return amount;
        if (type == Type.PERCENTAGE)
            return Math.max(0, amount - (amount * value / 100.0));
        return Math.max(0, amount - value);
    }

    /** Human-readable discount label, e.g. "20% OFF" or "50 EGP OFF" */
    public String label() {
        return type == Type.PERCENTAGE
            ? (int) value + "% OFF"
            : (int) value + " EGP OFF";
    }

    public String toCSV() {
        return code + "," + type.name() + "," + value + "," + expiryDate + "," + active;
    }

    public static Coupon fromCSV(String csv) {
        String[] p = csv.split(",");
        if (p.length < 5) return null;
        try {
            Coupon c = new Coupon(p[0].trim(), Type.valueOf(p[1].trim()),
                                  Double.parseDouble(p[2].trim()), p[3].trim());
            c.active = Boolean.parseBoolean(p[4].trim());
            return c;
        } catch (Exception e) { return null; }
    }
}
