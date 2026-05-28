package com.mycompany.healthclubsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * SplitPayment — allows a member to pay a bill in installments.
 * Each installment is tracked with its date and paid/pending status.
 */
public class SplitPayment {

    public static class Installment {
        private int    number;
        private double amount;
        private String dueDate;  // YYYY-MM-DD
        private boolean paid;
        private String paidDate; // YYYY-MM-DD or ""

        public Installment(int number, double amount, String dueDate) {
            this.number   = number;
            this.amount   = amount;
            this.dueDate  = dueDate;
            this.paid     = false;
            this.paidDate = "";
        }

        public int    getNumber()   { return number; }
        public double getAmount()   { return amount; }
        public String getDueDate()  { return dueDate; }
        public boolean isPaid()     { return paid; }
        public String getPaidDate() { return paidDate; }

        public void markPaid(String date) { this.paid = true; this.paidDate = date; }

        public String toInline() {
            return number + "~" + amount + "~" + dueDate + "~" + paid + "~" + paidDate;
        }

        public static Installment fromInline(String s) {
            String[] p = s.split("~", 5);
            if (p.length < 5) return null;
            try {
                Installment i = new Installment(Integer.parseInt(p[0]), Double.parseDouble(p[1]), p[2]);
                i.paid = Boolean.parseBoolean(p[3]);
                i.paidDate = p[4];
                return i;
            } catch (Exception e) { return null; }
        }
    }

    private int    splitId;
    private int    billId;
    private int    memberId;
    private double totalAmount;
    private int    numInstallments;
    private List<Installment> installments;

    public SplitPayment(int splitId, int billId, int memberId,
                        double totalAmount, int numInstallments,
                        String firstDueDate) {
        this.splitId         = splitId;
        this.billId          = billId;
        this.memberId        = memberId;
        this.totalAmount     = totalAmount;
        this.numInstallments = numInstallments;
        this.installments    = new ArrayList<>();

        double perInstallment = Math.ceil(totalAmount / numInstallments * 100) / 100.0;
        // Generate installment dates (monthly)
        try {
            java.time.LocalDate base = java.time.LocalDate.parse(firstDueDate,
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            for (int i = 0; i < numInstallments; i++) {
                double amt = (i == numInstallments - 1)
                    ? totalAmount - perInstallment * (numInstallments - 1)
                    : perInstallment;
                installments.add(new Installment(i + 1, Math.round(amt * 100.0) / 100.0,
                    base.plusMonths(i).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)));
            }
        } catch (Exception ignored) {}
    }

    public int    getSplitId()          { return splitId; }
    public int    getBillId()           { return billId; }
    public int    getMemberId()         { return memberId; }
    public double getTotalAmount()      { return totalAmount; }
    public int    getNumInstallments()  { return numInstallments; }
    public List<Installment> getInstallments() { return installments; }

    public double paidSoFar() {
        return installments.stream().filter(Installment::isPaid).mapToDouble(Installment::getAmount).sum();
    }

    public double remaining() { return totalAmount - paidSoFar(); }

    public Installment nextUnpaid() {
        return installments.stream().filter(i -> !i.isPaid()).findFirst().orElse(null);
    }

    public boolean isFullyPaid() {
        return installments.stream().allMatch(Installment::isPaid);
    }

    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(splitId).append(",").append(billId).append(",").append(memberId)
          .append(",").append(totalAmount).append(",").append(numInstallments).append(",");
        for (int i = 0; i < installments.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(installments.get(i).toInline());
        }
        return sb.toString();
    }

    public static SplitPayment fromCSV(String csv) {
        String[] p = csv.split(",", 6);
        if (p.length < 6) return null;
        try {
            SplitPayment sp = new SplitPayment(
                Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()),
                Integer.parseInt(p[2].trim()),
                Double.parseDouble(p[3].trim()),
                Integer.parseInt(p[4].trim()),
                "2000-01-01" // placeholder — installments loaded from CSV directly
            );
            sp.installments.clear();
            if (!p[5].trim().isEmpty()) {
                for (String seg : p[5].split("\\|")) {
                    Installment inst = Installment.fromInline(seg.trim());
                    if (inst != null) sp.installments.add(inst);
                }
            }
            return sp;
        } catch (Exception e) { return null; }
    }
}
