// ============================================================
//  PATCH: Add phone number to Member class
//  Apply this manually to Member.java in healthclubsystem
// ============================================================

// 1. Add this field after `private String subscriptionEndDate;`
//    private String phone = "";

// 2. Add getter/setter:
//    public String getPhone() { return phone == null ? "" : phone; }
//    public void   setPhone(String phone) { this.phone = phone; }

// 3. Update toCSV() to append phone:
//    return getId() + "," + getName() + "," + getUsername() + "," + getPassword() + "," + getRole()
//           + "," + getAssignedCoachName() + "," + subscriptionEndDate + "," + getPhone();

// 4. In FileManager.parseUser() for "member" case, add:
//    String phone = p.length > 7 ? p[7].trim() : "";
//    Member m = new Member(id, name, user, pass, coach, sub);
//    m.setPhone(phone);
//    return m;

// ── FULL UPDATED Member.java (replace your existing file) ──────

package com.mycompany.healthclubsystem;

public class Member extends User {

    private Coach  assignedCoach;
    private String subscriptionEndDate;
    private String phone;            // ← NEW: WhatsApp / contact number

    public Member(int id, String name, String username, String password,
                  Coach assignedCoach, String subscriptionEndDate) {
        super(id, name, username, password, "member");
        this.assignedCoach       = assignedCoach;
        this.subscriptionEndDate = subscriptionEndDate;
        this.phone               = "";
    }

    public Member(int id, String name, String username, String password,
                  String coachName, String subscriptionEndDate) {
        super(id, name, username, password, "member");
        this.assignedCoach    = null;
        this.subscriptionEndDate = subscriptionEndDate;
        this._tempCoachName   = coachName;
        this.phone            = "";
    }

    private String _tempCoachName = "";

    public String getTempCoachName()  { return _tempCoachName; }
    public Coach  getAssignedCoach()  { return assignedCoach; }

    public void setAssignedCoach(Coach coach) {
        this.assignedCoach = coach;
        this._tempCoachName = coach != null ? coach.getName() : "";
    }

    public String getAssignedCoachName() {
        return assignedCoach != null ? assignedCoach.getName() : _tempCoachName;
    }

    public void setAssignedCoachName(String coachName) { this._tempCoachName = coachName; }

    public String getSubscriptionEndDate()          { return subscriptionEndDate; }
    public void   setSubscriptionEndDate(String d)  { this.subscriptionEndDate = d; }

    // ── Phone ──────────────────────────────────────────────────
    public String getPhone()          { return phone == null ? "" : phone; }
    public void   setPhone(String p)  { this.phone = p == null ? "" : p; }

    // ── Password convenience (for auto-lock) ──────────────────
    // setPassword is already in User via getPassword()/setPassword()
    // If User doesn't expose setPassword, add it here:
    public void setPassword(String pwd) { super.setPasswordDirect(pwd); }

    @Override
    public String toCSV() {
        return getId() + "," + getName() + "," + getUsername() + "," + getPassword()
             + "," + getRole() + "," + getAssignedCoachName() + ","
             + subscriptionEndDate + "," + getPhone();
    }

    @Override
    public String generateReport() {
        return "Member | ID: " + getId()
             + " | Name: " + getName()
             + " | Coach: " + getAssignedCoachName()
             + " | Sub End: " + subscriptionEndDate
             + " | Phone: " + getPhone();
    }
}
