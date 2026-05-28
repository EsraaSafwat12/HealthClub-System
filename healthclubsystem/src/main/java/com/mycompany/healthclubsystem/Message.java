package com.mycompany.healthclubsystem;

/**
 * Message class handles communication between coaches and members.
 *
 * ASSOCIATION: Message uses Coach and Member objects.
 * Message knows about Coach and Member but does NOT own them.
 * Coach and Member exist independently.
 */
public class Message {
    private int msgId;

    // ASSOCIATION: holds references to Coach and Member objects
    private Coach fromCoach;
    private Member toMember;

    private String content;
    private String date;

    // Full constructor with objects (Association)
    public Message(int msgId, Coach fromCoach, Member toMember, String content, String date) {
        this.msgId = msgId;
        this.fromCoach = fromCoach;   // Association
        this.toMember = toMember;     // Association
        this.content = content;
        this.date = date;
    }

    // Convenience constructor using names/IDs (used by GUI screens)
    public Message(int msgId, String coachName, int memberId, String content, String date) {
        this.msgId = msgId;
        this.fromCoach = null;
        this.toMember = null;
        this._tempCoachName = coachName;
        this._tempMemberId = memberId;
        this.content = content;
        this.date = date;
    }

    // Setters for linking objects after file load
    public void setFromCoach(Coach coach) { this.fromCoach = coach; }
    public void setToMember(Member member) { this.toMember = member; }

    // Getters
    public int getMsgId() { return msgId; }
    public Coach getFromCoach() { return fromCoach; }
    public Member getToMember() { return toMember; }
    public int getToMemberId() { return toMember != null ? toMember.getId() : _tempMemberId; }
    public String getFromCoachName() { return fromCoach != null ? fromCoach.getName() : _tempCoachName; }
    public String getContent() { return content; }
    public String getDate() { return date; }

    public String toCSV() {
        return msgId + "," + getFromCoachName() + "," + getToMemberId() + "," + content + "," + date;
    }

    // fromCSV kept for FileManager - objects are linked later via FileManager.linkRelations()
    public static Message fromCSV(String csv) {
        String[] parts = csv.split(",");
        // Temporarily stores IDs/names; FileManager.linkRelations() will attach real objects
        Message m = new Message(
            Integer.parseInt(parts[0]),
            null,   // Coach linked later
            null,   // Member linked later
            parts[3],
            parts[4]
        );
        m._tempCoachName = parts[1];
        m._tempMemberId = Integer.parseInt(parts[2]);
        return m;
    }

    // Temporary fields used during loading before objects are linked
    String _tempCoachName = "";
    int _tempMemberId = -1;
}
