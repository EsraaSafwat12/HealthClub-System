package com.mycompany.healthclubsystem;

/**
 * Referral — tracks member referrals.
 * When a referred member registers and subscribes,
 * the referrer automatically gets 1 free month added to their subscription.
 */
public class Referral {
    public enum Status { PENDING, COMPLETED, REWARDED }

    private int    referralId;
    private int    referrerId;      // member who referred
    private String referrerName;
    private int    referredId;      // new member who was referred
    private String referredName;
    private String referralDate;    // YYYY-MM-DD
    private Status status;
    private String rewardGiven;     // description, e.g. "1 free month added"

    public Referral(int referralId, int referrerId, String referrerName,
                    int referredId, String referredName, String referralDate) {
        this.referralId   = referralId;
        this.referrerId   = referrerId;
        this.referrerName = referrerName;
        this.referredId   = referredId;
        this.referredName = referredName;
        this.referralDate = referralDate;
        this.status       = Status.PENDING;
        this.rewardGiven  = "";
    }

    public int    getReferralId()   { return referralId; }
    public int    getReferrerId()   { return referrerId; }
    public String getReferrerName() { return referrerName; }
    public int    getReferredId()   { return referredId; }
    public String getReferredName() { return referredName; }
    public String getReferralDate() { return referralDate; }
    public Status getStatus()       { return status; }
    public String getRewardGiven()  { return rewardGiven; }

    public void complete()  { this.status = Status.COMPLETED; }
    public void reward(String desc) {
        this.status = Status.REWARDED;
        this.rewardGiven = desc;
    }

    public String toCSV() {
        return referralId + "," + referrerId + "," + referrerName.replace(",",";")
             + "," + referredId + "," + referredName.replace(",",";")
             + "," + referralDate + "," + status.name()
             + "," + rewardGiven.replace(",",";");
    }

    public static Referral fromCSV(String csv) {
        String[] p = csv.split(",", 8);
        if (p.length < 8) return null;
        try {
            Referral r = new Referral(
                Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()),
                p[2].trim().replace(";",","),
                Integer.parseInt(p[3].trim()),
                p[4].trim().replace(";",","),
                p[5].trim()
            );
            r.status = Status.valueOf(p[6].trim());
            r.rewardGiven = p[7].trim().replace(";",",");
            return r;
        } catch (Exception e) { return null; }
    }
}
