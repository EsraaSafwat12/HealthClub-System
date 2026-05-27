package com.mycompany.healthclubsystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * FrozenMembership — tracks a freeze period for a member.
 * When frozen, the subscription end date is extended automatically
 * by the number of frozen days when the member unfreezes.
 */
public class FrozenMembership {
    private int    memberId;
    private String frozenFrom;   // YYYY-MM-DD
    private String frozenUntil;  // YYYY-MM-DD (null if still frozen)
    private String reason;
    private boolean active;      // true = currently frozen

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    public FrozenMembership(int memberId, String frozenFrom, String reason) {
        this.memberId   = memberId;
        this.frozenFrom = frozenFrom;
        this.frozenUntil = null;
        this.reason     = reason;
        this.active     = true;
    }

    public int     getMemberId()   { return memberId; }
    public String  getFrozenFrom() { return frozenFrom; }
    public String  getFrozenUntil(){ return frozenUntil == null ? "" : frozenUntil; }
    public String  getReason()     { return reason; }
    public boolean isActive()      { return active; }

    /**
     * Unfreeze now — sets frozenUntil to today and marks inactive.
     * Returns how many days were frozen so the caller can extend the subscription.
     */
    public long unfreeze() {
        if (!active) return 0;
        String today = LocalDate.now().format(DF);
        this.frozenUntil = today;
        this.active = false;
        try {
            long days = ChronoUnit.DAYS.between(
                LocalDate.parse(frozenFrom, DF),
                LocalDate.parse(today, DF)
            );
            return Math.max(0, days);
        } catch (Exception e) { return 0; }
    }

    /**
     * How many days has the member been frozen so far (if still active).
     */
    public long frozenDays() {
        try {
            String end = (frozenUntil == null || frozenUntil.isEmpty())
                ? LocalDate.now().format(DF)
                : frozenUntil;
            return ChronoUnit.DAYS.between(
                LocalDate.parse(frozenFrom, DF),
                LocalDate.parse(end, DF)
            );
        } catch (Exception e) { return 0; }
    }

    public String toCSV() {
        return memberId + "," + frozenFrom + "," + (frozenUntil == null ? "" : frozenUntil)
             + "," + reason.replace(",", ";") + "," + active;
    }

    public static FrozenMembership fromCSV(String csv) {
        String[] p = csv.split(",", 5);
        if (p.length < 5) return null;
        try {
            FrozenMembership f = new FrozenMembership(
                Integer.parseInt(p[0].trim()), p[1].trim(), p[3].trim().replace(";", ","));
            String until = p[2].trim();
            if (!until.isEmpty()) f.frozenUntil = until;
            f.active = Boolean.parseBoolean(p[4].trim());
            return f;
        } catch (Exception e) { return null; }
    }
}
