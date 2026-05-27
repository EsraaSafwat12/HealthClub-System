package com.mycompany.healthclubsystem;

/**
 * Salary — monthly salary record for a coach.
 */
public class Salary {
    private int    salaryId;
    private int    coachId;
    private String coachName;
    private double baseSalary;
    private double bonus;
    private double deductions;
    private String month;   // YYYY-MM
    private boolean isPaid;
    private String paidDate; // YYYY-MM-DD or empty

    public Salary(int salaryId, int coachId, String coachName,
                  double baseSalary, double bonus, double deductions,
                  String month) {
        this.salaryId   = salaryId;
        this.coachId    = coachId;
        this.coachName  = coachName;
        this.baseSalary = baseSalary;
        this.bonus      = bonus;
        this.deductions = deductions;
        this.month      = month;
        this.isPaid     = false;
        this.paidDate   = "";
    }

    public double getNet()       { return baseSalary + bonus - deductions; }
    public int    getSalaryId()  { return salaryId; }
    public int    getCoachId()   { return coachId; }
    public String getCoachName() { return coachName; }
    public double getBaseSalary(){ return baseSalary; }
    public double getBonus()     { return bonus; }
    public double getDeductions(){ return deductions; }
    public String getMonth()     { return month; }
    public boolean isPaid()      { return isPaid; }
    public String getPaidDate()  { return paidDate; }

    public void setBaseSalary(double b) { this.baseSalary = b; }
    public void setBonus(double b)      { this.bonus = b; }
    public void setDeductions(double d) { this.deductions = d; }
    public void markPaid(String date)   { this.isPaid = true; this.paidDate = date; }

    public String toCSV() {
        return salaryId + "," + coachId + "," + coachName + ","
             + baseSalary + "," + bonus + "," + deductions + ","
             + month + "," + isPaid + "," + paidDate;
    }

    public static Salary fromCSV(String csv) {
        String[] p = csv.split(",", 9);
        if (p.length < 9) return null;
        try {
            Salary s = new Salary(
                Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                p[2].trim(), Double.parseDouble(p[3].trim()),
                Double.parseDouble(p[4].trim()), Double.parseDouble(p[5].trim()),
                p[6].trim()
            );
            s.isPaid   = Boolean.parseBoolean(p[7].trim());
            s.paidDate = p[8].trim();
            return s;
        } catch (Exception e) { return null; }
    }

    @Override
    public String toString() {
        return String.format("%-4d | %-15s | %s | Base: $%.0f | Bonus: $%.0f | Ded: $%.0f | Net: $%.0f | %s",
            salaryId, coachName, month, baseSalary, bonus, deductions, getNet(),
            isPaid ? "PAID (" + paidDate + ")" : "UNPAID");
    }
}
