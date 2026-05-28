package com.mycompany.healthclubsystem;

/**
 * BodyProgress — stores a monthly body measurement entry for a member.
 * Used to draw the weight/fat progress chart in MemberScreen and CoachScreen.
 */
public class BodyProgress {
    private int    progressId;
    private int    memberId;
    private String memberName;
    private String recordDate;   // YYYY-MM-DD (first of the month recommended)
    private double weightKg;
    private double bodyFatPct;
    private String notes;

    public BodyProgress(int progressId, int memberId, String memberName,
                        String recordDate, double weightKg, double bodyFatPct, String notes) {
        this.progressId  = progressId;
        this.memberId    = memberId;
        this.memberName  = memberName;
        this.recordDate  = recordDate;
        this.weightKg    = weightKg;
        this.bodyFatPct  = bodyFatPct;
        this.notes       = notes;
    }

    public int    getProgressId()  { return progressId; }
    public int    getMemberId()    { return memberId; }
    public String getMemberName()  { return memberName; }
    public String getRecordDate()  { return recordDate; }
    public double getWeightKg()    { return weightKg; }
    public double getBodyFatPct()  { return bodyFatPct; }
    public String getNotes()       { return notes; }

    public void setWeightKg(double w)    { this.weightKg = w; }
    public void setBodyFatPct(double f)  { this.bodyFatPct = f; }
    public void setNotes(String n)       { this.notes = n; }

    /** Month label for chart X-axis, e.g. "2026-05" */
    public String monthLabel() {
        return recordDate.length() >= 7 ? recordDate.substring(0, 7) : recordDate;
    }

    public String toCSV() {
        return progressId + "," + memberId + "," + memberName.replace(",",";")
             + "," + recordDate + "," + weightKg + "," + bodyFatPct
             + "," + notes.replace(",",";");
    }

    public static BodyProgress fromCSV(String csv) {
        String[] p = csv.split(",", 7);
        if (p.length < 7) return null;
        try {
            return new BodyProgress(
                Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                p[2].trim().replace(";",","), p[3].trim(),
                Double.parseDouble(p[4].trim()), Double.parseDouble(p[5].trim()),
                p[6].trim().replace(";",",")
            );
        } catch (Exception e) { return null; }
    }
}
