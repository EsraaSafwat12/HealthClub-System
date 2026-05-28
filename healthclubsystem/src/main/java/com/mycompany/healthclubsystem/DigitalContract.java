package com.mycompany.healthclubsystem;

/**
 * DigitalContract — represents a membership contract for a member.
 * A PDF is generated and sent; the member acknowledges (digital signature).
 */
public class DigitalContract {
    public enum Status { PENDING, SIGNED, EXPIRED }

    private int    contractId;
    private int    memberId;
    private String memberName;
    private String startDate;      // YYYY-MM-DD
    private String endDate;        // YYYY-MM-DD
    private double monthlyFee;
    private String planType;       // e.g. "Monthly", "Quarterly", "Annual"
    private String terms;          // short summary of terms
    private Status status;
    private String signedDate;     // YYYY-MM-DD or ""
    private String signatureCode;  // e.g. "MBR-001-2026" unique sign token
    private String pdfPath;        // path to generated PDF file

    public DigitalContract(int contractId, int memberId, String memberName,
                           String startDate, String endDate,
                           double monthlyFee, String planType, String terms) {
        this.contractId    = contractId;
        this.memberId      = memberId;
        this.memberName    = memberName;
        this.startDate     = startDate;
        this.endDate       = endDate;
        this.monthlyFee    = monthlyFee;
        this.planType      = planType;
        this.terms         = terms;
        this.status        = Status.PENDING;
        this.signedDate    = "";
        this.signatureCode = "CNT-" + memberId + "-" + contractId + "-" + startDate.replace("-","");
        this.pdfPath       = "";
    }

    public int    getContractId()    { return contractId; }
    public int    getMemberId()      { return memberId; }
    public String getMemberName()    { return memberName; }
    public String getStartDate()     { return startDate; }
    public String getEndDate()       { return endDate; }
    public double getMonthlyFee()    { return monthlyFee; }
    public String getPlanType()      { return planType; }
    public String getTerms()         { return terms; }
    public Status getStatus()        { return status; }
    public String getSignedDate()    { return signedDate; }
    public String getSignatureCode() { return signatureCode; }
    public String getPdfPath()       { return pdfPath; }

    public void setPdfPath(String path) { this.pdfPath = path; }

    public void sign(String date) {
        this.status     = Status.SIGNED;
        this.signedDate = date;
    }

    public void expire() { this.status = Status.EXPIRED; }

    /**
     * Full contract text used to generate the PDF.
     */
    public String toContractText() {
        return "╔══════════════════════════════════════════╗\n"
             + "         POWER GYM — MEMBERSHIP CONTRACT\n"
             + "╚══════════════════════════════════════════╝\n\n"
             + "Contract ID  : " + signatureCode + "\n"
             + "Member Name  : " + memberName + "\n"
             + "Member ID    : " + memberId + "\n"
             + "Plan Type    : " + planType + "\n"
             + "Start Date   : " + startDate + "\n"
             + "End Date     : " + endDate + "\n"
             + "Monthly Fee  : " + String.format("%.2f", monthlyFee) + " EGP\n\n"
             + "── TERMS & CONDITIONS ──────────────────────\n"
             + terms + "\n\n"
             + "By signing below, the member agrees to abide by all gym rules,\n"
             + "payment schedules, and code of conduct.\n\n"
             + "Member Signature: ________________________\n"
             + "Date            : " + (signedDate.isEmpty() ? "________________" : signedDate) + "\n\n"
             + "Admin Signature : ________________________\n"
             + "Status          : " + status.name() + "\n";
    }

    public String toCSV() {
        return contractId + "," + memberId + "," + memberName.replace(",",";")
             + "," + startDate + "," + endDate + "," + monthlyFee
             + "," + planType.replace(",",";") + "," + terms.replace(",",";")
             + "," + status.name() + "," + signedDate
             + "," + signatureCode + "," + pdfPath.replace(",",";");
    }

    public static DigitalContract fromCSV(String csv) {
        String[] p = csv.split(",", 12);
        if (p.length < 12) return null;
        try {
            DigitalContract c = new DigitalContract(
                Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                p[2].trim().replace(";",","), p[3].trim(), p[4].trim(),
                Double.parseDouble(p[5].trim()), p[6].trim().replace(";",","),
                p[7].trim().replace(";",",")
            );
            c.status        = Status.valueOf(p[8].trim());
            c.signedDate    = p[9].trim();
            c.signatureCode = p[10].trim();
            c.pdfPath       = p[11].trim().replace(";",",");
            return c;
        } catch (Exception e) { return null; }
    }
}
