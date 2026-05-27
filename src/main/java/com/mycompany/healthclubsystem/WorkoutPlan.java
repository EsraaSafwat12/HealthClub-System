package com.mycompany.healthclubsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * WorkoutPlan — a training plan assigned by a coach to a member.
 * Contains a list of exercises the member should follow.
 */
public class WorkoutPlan {
    private int    planId;
    private int    memberId;
    private String memberName;
    private String coachName;
    private String goal;      // e.g. Weight Loss | Muscle Gain | Endurance | Flexibility
    private String createdDate;
    private String notes;
    private List<WorkoutExercise> exercises;

    public WorkoutPlan(int planId, int memberId, String memberName,
                       String coachName, String goal, String createdDate, String notes) {
        this.planId      = planId;
        this.memberId    = memberId;
        this.memberName  = memberName;
        this.coachName   = coachName;
        this.goal        = goal;
        this.createdDate = createdDate;
        this.notes       = notes;
        this.exercises   = new ArrayList<>();
    }

    public void addExercise(WorkoutExercise ex) { exercises.add(ex); }
    public List<WorkoutExercise> getExercises()  { return exercises; }

    public int    getPlanId()      { return planId; }
    public int    getMemberId()    { return memberId; }
    public String getMemberName()  { return memberName; }
    public String getCoachName()   { return coachName; }
    public String getGoal()        { return goal; }
    public String getCreatedDate() { return createdDate; }
    public String getNotes()       { return notes; }
    public void   setNotes(String n){ this.notes = n; }

    /** Serialize exercises inline separated by | */
    public String toCSV() {
        StringBuilder ex = new StringBuilder();
        for (WorkoutExercise e : exercises) {
            if (ex.length() > 0) ex.append("|");
            ex.append(e.toInline());
        }
        return planId + "," + memberId + "," + memberName + ","
             + coachName + "," + goal + "," + createdDate + ","
             + notes.replace(",", ";") + "," + ex;
    }

    public static WorkoutPlan fromCSV(String csv) {
        String[] p = csv.split(",", 8);
        if (p.length < 7) return null;
        try {
            WorkoutPlan wp = new WorkoutPlan(
                Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                p[2].trim(), p[3].trim(), p[4].trim(), p[5].trim(),
                p.length > 6 ? p[6].trim().replace(";", ",") : ""
            );
            if (p.length > 7 && !p[7].trim().isEmpty()) {
                for (String ex : p[7].split("\\|")) {
                    WorkoutExercise we = WorkoutExercise.fromInline(ex);
                    if (we != null) wp.addExercise(we);
                }
            }
            return wp;
        } catch (Exception e) { return null; }
    }

    /** Generate a human-readable plan string for WhatsApp / PDF */
    public String toReadableText() {
        StringBuilder sb = new StringBuilder();
        sb.append("💪 POWER GYM — Training Plan\n");
        sb.append("Member : ").append(memberName).append("\n");
        sb.append("Coach  : ").append(coachName).append("\n");
        sb.append("Goal   : ").append(goal).append("\n");
        sb.append("Date   : ").append(createdDate).append("\n");
        if (!notes.isEmpty()) sb.append("Notes  : ").append(notes).append("\n");
        sb.append("─────────────────────────────\n");
        int i = 1;
        for (WorkoutExercise ex : exercises) {
            sb.append(i++).append(". ").append(ex.toString()).append("\n");
        }
        return sb.toString();
    }
}
