package com.mycompany.healthclubsystem;

/**
 * WorkoutExercise — a single exercise inside a WorkoutPlan.
 */
public class WorkoutExercise {
    private String name;
    private int    sets;
    private int    reps;
    private String duration; // e.g. "30 sec" or "" if sets/reps used
    private String muscle;   // targeted muscle group
    private String notes;

    public WorkoutExercise(String name, int sets, int reps,
                           String duration, String muscle, String notes) {
        this.name     = name;
        this.sets     = sets;
        this.reps     = reps;
        this.duration = duration;
        this.muscle   = muscle;
        this.notes    = notes;
    }

    public String getName()     { return name; }
    public int    getSets()     { return sets; }
    public int    getReps()     { return reps; }
    public String getDuration() { return duration; }
    public String getMuscle()   { return muscle; }
    public String getNotes()    { return notes; }

    /** Inline format: name~sets~reps~duration~muscle~notes */
    public String toInline() {
        return name.replace("~","") + "~" + sets + "~" + reps + "~"
             + duration.replace("~","") + "~" + muscle.replace("~","") + "~"
             + notes.replace("~","").replace(",",";");
    }

    public static WorkoutExercise fromInline(String s) {
        String[] p = s.split("~", 6);
        if (p.length < 6) return null;
        try {
            return new WorkoutExercise(p[0], Integer.parseInt(p[1]),
                Integer.parseInt(p[2]), p[3], p[4], p[5].replace(";",","));
        } catch (Exception e) { return null; }
    }

    @Override
    public String toString() {
        String setsReps = sets > 0 ? sets + " sets x " + reps + " reps" : duration;
        return String.format("%-22s | %-20s | %-18s | %s",
            name, muscle, setsReps, notes.isEmpty() ? "" : "(" + notes + ")");
    }
}
