package com.mycompany.healthclubsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * FamilyPackage — groups multiple members under one family subscription.
 * Auto-applies a discount when 2+ members are in the package.
 * The "primary" member is the account holder / payer.
 */
public class FamilyPackage {
    public static final double DISCOUNT_2 = 10.0;  // 10% off for 2 members
    public static final double DISCOUNT_3 = 20.0;  // 20% off for 3+
    public static final double DISCOUNT_4 = 30.0;  // 30% off for 4+

    private int         packageId;
    private String      packageName;   // e.g. "Smith Family"
    private int         primaryMemberId;
    private List<Integer> memberIds;   // all member IDs including primary
    private String      startDate;     // YYYY-MM-DD
    private String      endDate;       // YYYY-MM-DD
    private double      basePrice;     // price per person before discount
    private boolean     active;

    public FamilyPackage(int packageId, String packageName, int primaryMemberId,
                         String startDate, String endDate, double basePrice) {
        this.packageId       = packageId;
        this.packageName     = packageName;
        this.primaryMemberId = primaryMemberId;
        this.memberIds       = new ArrayList<>();
        this.memberIds.add(primaryMemberId);
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.basePrice       = basePrice;
        this.active          = true;
    }

    public int          getPackageId()       { return packageId; }
    public String       getPackageName()     { return packageName; }
    public int          getPrimaryMemberId() { return primaryMemberId; }
    public List<Integer>getMemberIds()       { return memberIds; }
    public String       getStartDate()       { return startDate; }
    public String       getEndDate()         { return endDate; }
    public double       getBasePrice()       { return basePrice; }
    public boolean      isActive()           { return active; }
    public void         setActive(boolean a) { this.active = a; }

    public void addMember(int memberId) {
        if (!memberIds.contains(memberId)) memberIds.add(memberId);
    }
    public void removeMember(int memberId) {
        memberIds.remove(Integer.valueOf(memberId));
    }

    public int size() { return memberIds.size(); }

    /** Returns the discount % based on group size */
    public double discountPercent() {
        int n = memberIds.size();
        if (n >= 4) return DISCOUNT_4;
        if (n == 3) return DISCOUNT_3;
        if (n == 2) return DISCOUNT_2;
        return 0;
    }

    /** Total price for the whole family (after discount) */
    public double totalPrice() {
        double gross = basePrice * memberIds.size();
        return Math.max(0, gross - (gross * discountPercent() / 100.0));
    }

    /** Price per person after discount */
    public double pricePerPerson() {
        int n = memberIds.size();
        return n > 0 ? totalPrice() / n : basePrice;
    }

    public String toCSV() {
        String ids = String.join("|", memberIds.stream()
            .map(String::valueOf).toArray(String[]::new));
        return packageId + "," + packageName.replace(",",";") + ","
             + primaryMemberId + "," + ids + ","
             + startDate + "," + endDate + ","
             + basePrice + "," + active;
    }

    public static FamilyPackage fromCSV(String csv) {
        String[] p = csv.split(",", 8);
        if (p.length < 8) return null;
        try {
            FamilyPackage fp = new FamilyPackage(
                Integer.parseInt(p[0].trim()),
                p[1].trim().replace(";", ","),
                Integer.parseInt(p[2].trim()),
                p[4].trim(), p[5].trim(),
                Double.parseDouble(p[6].trim())
            );
            fp.memberIds.clear();
            for (String id : p[3].split("\\|")) {
                try { fp.memberIds.add(Integer.parseInt(id.trim())); }
                catch (Exception ignored) {}
            }
            fp.active = Boolean.parseBoolean(p[7].trim());
            return fp;
        } catch (Exception e) { return null; }
    }
}
