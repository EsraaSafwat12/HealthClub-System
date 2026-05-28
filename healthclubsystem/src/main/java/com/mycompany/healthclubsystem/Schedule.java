package com.mycompany.healthclubsystem;

/**
 * Schedule class stores training plans assigned by a coach to a member.
 *
 * ASSOCIATION: Schedule uses Coach and Member objects.
 * Schedule knows about them but does NOT own them.
 */
public class Schedule {
    private int scheduleId;

    // ASSOCIATION: references to Coach and Member (not owned by Schedule)
    private Coach coach;
    private Member member;

    private String planDetails;
    private String date;

    // Full constructor with objects (Association)
    public Schedule(int scheduleId, Coach coach, Member member, String planDetails, String date) {
        this.scheduleId = scheduleId;
        this.coach = coach;       // Association
        this.member = member;     // Association
        this.planDetails = planDetails;
        this.date = date;
    }

    // Convenience constructor using IDs/names (used by GUI screens)
    public Schedule(int scheduleId, int memberId, String coachName, String planDetails, String date) {
        this.scheduleId = scheduleId;
        this.coach = null;
        this.member = null;
        this._tempMemberId = memberId;
        this._tempCoachName = coachName;
        this.planDetails = planDetails;
        this.date = date;
    }

    // Setters for linking after file load
    public void setCoach(Coach coach) { this.coach = coach; }
    public void setMember(Member member) { this.member = member; }

    // Getters
    public int getScheduleId() { return scheduleId; }
    public Coach getCoach() { return coach; }
    public Member getMember() { return member; }
    public int getMemberId() { return member != null ? member.getId() : _tempMemberId; }
    public String getCoachName() { return coach != null ? coach.getName() : _tempCoachName; }
    public String getPlanDetails() { return planDetails; }
    public String getDate() { return date; }

    public String toCSV() {
        return scheduleId + "," + getMemberId() + "," + getCoachName() + "," + planDetails + "," + date;
    }

    // fromCSV kept for FileManager - objects are linked later
    public static Schedule fromCSV(String csv) {
        String[] parts = csv.split(",");
        Schedule s = new Schedule(
            Integer.parseInt(parts[0]),
            null,   // Coach linked later
            null,   // Member linked later
            parts[3],
            parts[4]
        );
        s._tempMemberId = Integer.parseInt(parts[1]);
        s._tempCoachName = parts[2];
        return s;
    }

    // Temporary fields used during loading before objects are linked
    int _tempMemberId = -1;
    String _tempCoachName = "";
}
