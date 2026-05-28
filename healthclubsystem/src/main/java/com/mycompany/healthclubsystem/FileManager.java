package com.mycompany.healthclubsystem;

import java.io.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * FileManager handles all file I/O operations.
 * Demonstrates FILE HANDLING: reading and writing to persistent storage.
 * Also manages OOP RELATIONSHIPS by linking objects after loading.
 */
public class FileManager {
    private static final String USERS_FILE    = "users.txt";
    private static final String BILLS_FILE    = "bills.txt";
    private static final String MSG_FILE      = "messages.txt";
    private static final String SCH_FILE      = "schedules.txt";
    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    // ==================== USER HELPERS ====================

    public static boolean isUsernameExists(String username, ArrayList<User> users) {
        for (User u : users)
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        return false;
    }

    public static boolean isIdExists(int id, ArrayList<User> users) {
        return findUserById(id, users) != null;
    }

    public static User findUserById(int id, ArrayList<User> users) {
        for (User u : users) if (u.getId() == id) return u;
        return null;
    }

    public static User findUserByUsername(String username, ArrayList<User> users) {
        for (User u : users)
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        return null;
    }

    public static Coach findCoachByName(String name, ArrayList<User> users) {
        for (User u : users)
            if (u instanceof Coach && u.getName().equalsIgnoreCase(name))
                return (Coach) u;
        return null;
    }

    // ==================== USERS FILE ====================

    public static ArrayList<User> loadUsers() {
        ArrayList<User> list = new ArrayList<>();
        File f = new File(USERS_FILE);
        System.out.println("[DEBUG] Looking for users.txt at: " + f.getAbsolutePath());
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                User u = parseUser(line);
                if (u != null) list.add(u);
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not load users: " + e.getMessage());
        }
        // AGGREGATION: link Member objects to their Coach objects after all users are loaded
        linkMembersToCoaches(list);
        return list;
    }

    /**
     * AGGREGATION support:
     * After all users are loaded from file, find each Member's Coach object and assign it.
     * This establishes the real object reference instead of just a name string.
     * Demonstrates AGGREGATION: Member "has-a" Coach — they exist independently.
     */
    private static void linkMembersToCoaches(ArrayList<User> users) {
        for (User u : users) {
            if (u instanceof Member) {
                Member member = (Member) u;
                String coachName = member.getTempCoachName();
                if (coachName != null && !coachName.isEmpty()) {
                    Coach coach = findCoachByName(coachName, users);
                    if (coach != null) {
                        member.setAssignedCoach(coach); // AGGREGATION link established
                    }
                }
            }
        }
    }

    /**
     * ASSOCIATION support:
     * After loading, link Message and Schedule objects to their Coach and Member objects.
     * Demonstrates ASSOCIATION: Message and Schedule "know about" Coach and Member
     * but do NOT own them — they exist independently.
     */
    public static void linkRelations(ArrayList<User> users,
                                     ArrayList<Message> messages,
                                     ArrayList<Schedule> schedules) {
        for (Message msg : messages) {
            if (msg._tempMemberId > 0) {
                User u = findUserById(msg._tempMemberId, users);
                if (u instanceof Member) msg.setToMember((Member) u);
            }
            if (msg._tempCoachName != null && !msg._tempCoachName.isEmpty()) {
                Coach c = findCoachByName(msg._tempCoachName, users);
                if (c != null) msg.setFromCoach(c);
            }
        }
        for (Schedule sch : schedules) {
            if (sch._tempMemberId > 0) {
                User u = findUserById(sch._tempMemberId, users);
                if (u instanceof Member) sch.setMember((Member) u);
            }
            if (sch._tempCoachName != null && !sch._tempCoachName.isEmpty()) {
                Coach c = findCoachByName(sch._tempCoachName, users);
                if (c != null) sch.setCoach(c);
            }
        }
    }

    public static void saveUsers(ArrayList<User> users) {
        if (users == null) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) bw.write(u.toCSV() + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save users: " + e.getMessage());
        }
    }

    private static User parseUser(String csv) {
        String[] p = csv.split(",");
        if (p.length < 5) return null;
        try {
            int    id   = Integer.parseInt(p[0].trim());
            String name = p[1].trim();
            String user = p[2].trim();
            String pass = p[3].trim();
            String role = p[4].trim();
            switch (role) {
                case "admin":  return new Admin(id, name, user, pass);
                case "coach":  return new Coach(id, name, user, pass);
                case "member":
                    String coach = p.length > 5 ? p[5].trim() : "";
                    String sub   = p.length > 6 ? p[6].trim() : "";
                    // Coach object will be linked later in linkMembersToCoaches()
                    return new Member(id, name, user, pass, coach, sub);
                default: return null;
            }
        } catch (Exception e) { return null; }
    }

    // ==================== BILLS ====================

    public static ArrayList<Bill> loadBills() {
        ArrayList<Bill> list = new ArrayList<>();
        File f = new File(BILLS_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try { list.add(Bill.fromCSV(line)); } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not load bills: " + e.getMessage());
        }
        return list;
    }

    public static void saveBills(ArrayList<Bill> bills) {
        if (bills == null || bills.isEmpty()) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BILLS_FILE))) {
            for (Bill b : bills) bw.write(b.toCSV() + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save bills: " + e.getMessage());
        }
    }

    // ==================== MESSAGES ====================

    public static ArrayList<Message> loadMessages() {
        ArrayList<Message> list = new ArrayList<>();
        File f = new File(MSG_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try { list.add(Message.fromCSV(line)); } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not load messages: " + e.getMessage());
        }
        return list;
    }

    public static void saveMessages(ArrayList<Message> msgs) {
        if (msgs == null || msgs.isEmpty()) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(MSG_FILE))) {
            for (Message m : msgs) bw.write(m.toCSV() + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save messages: " + e.getMessage());
        }
    }

    // ==================== SCHEDULES ====================

    public static ArrayList<Schedule> loadSchedules() {
        ArrayList<Schedule> list = new ArrayList<>();
        File f = new File(SCH_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try { list.add(Schedule.fromCSV(line)); } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Could not load schedules: " + e.getMessage());
        }
        return list;
    }

    public static void saveSchedules(ArrayList<Schedule> schs) {
        if (schs == null || schs.isEmpty()) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCH_FILE))) {
            for (Schedule s : schs) bw.write(s.toCSV() + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save schedules: " + e.getMessage());
        }
    }

    // ==================== REPORTS ====================

    /**
     * REPORT GENERATION: Generates a detailed member report.
     *
     * Uses POLYMORPHISM via the Reportable interface:
     * Calls generateReport() on each User object without instanceof checks.
     * Each subclass (Member, Coach, Admin) provides its own implementation.
     *
     * Saves to both a human-readable TXT file and a structured report.
     */
    public static void generateMemberReport(ArrayList<User> users) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║              HEALTH CLUB — MEMBER REPORT             ║");
        System.out.println("║  Generated on: " + LocalDate.now() + "                          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.printf("%-4s | %-15s | %-14s | %-12s | %-8s%n",
            "ID", "Name", "Coach", "Sub End", "Status");
        System.out.println("─".repeat(65));

        int total = 0, active = 0, expired = 0;
        for (User u : users) {
            if (u instanceof Member) {
                Member m  = (Member) u;
                String st = isExpired(m.getSubscriptionEndDate()) ? "EXPIRED" : "ACTIVE";
                if (st.equals("ACTIVE")) active++; else expired++;
                total++;
                System.out.printf("%-4d | %-15s | %-14s | %-12s | %-8s%n",
                    m.getId(), m.getName(), m.getAssignedCoachName(),
                    m.getSubscriptionEndDate(), st);
            }
        }
        System.out.println("─".repeat(65));
        System.out.printf("Total: %d  |  Active: %d  |  Expired: %d%n", total, active, expired);

        // Save to TXT file using Polymorphism (Reportable interface)
        String txtPath = "members_report.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(txtPath))) {
            pw.println("╔══════════════════════════════════════════╗");
            pw.println("║       HEALTH CLUB — MEMBER REPORT        ║");
            pw.println("║  Generated: " + LocalDate.now() + "                    ║");
            pw.println("╚══════════════════════════════════════════╝");
            pw.println();
            int count = 1;
            for (User u : users) {
                if (u instanceof Member) {
                    pw.println("Member #" + count++);
                    // POLYMORPHISM: generateReport() from Reportable interface
                    pw.println("  " + u.generateReport());
                    String st = isExpired(((Member) u).getSubscriptionEndDate()) ? "EXPIRED" : "ACTIVE";
                    pw.println("  Status: " + st);
                    pw.println("─".repeat(45));
                }
            }
            pw.printf("%nSummary — Total: %d | Active: %d | Expired: %d%n", total, active, expired);
            System.out.println("\n[OK] Report saved to: " + txtPath);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save report: " + e.getMessage());
        }
    }

    /**
     * REPORT GENERATION: Full system report — all users by role.
     * Uses POLYMORPHISM: calls generateReport() on every User without instanceof.
     */
    public static void generateFullReport(ArrayList<User> users, ArrayList<Bill> bills) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║            HEALTH CLUB — FULL SYSTEM REPORT          ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        System.out.println("\n── ADMINS ──────────────────────────────────────────────");
        for (User u : users) if (u instanceof Admin)
            // POLYMORPHISM: each subclass has its own generateReport()
            System.out.println("  " + u.generateReport());

        System.out.println("\n── COACHES ─────────────────────────────────────────────");
        for (User u : users) if (u instanceof Coach)
            System.out.println("  " + u.generateReport());

        System.out.println("\n── MEMBERS ─────────────────────────────────────────────");
        for (User u : users) if (u instanceof Member) {
            System.out.println("  " + u.generateReport() +
                " | Status: " + (isExpired(((Member) u).getSubscriptionEndDate()) ? "EXPIRED" : "ACTIVE"));
        }

        System.out.println("\n── BILLING SUMMARY ─────────────────────────────────────");
        long unpaid = bills.stream().filter(b -> !b.isPaid()).count();
        System.out.println("  Total Bills: " + bills.size() + " | Unpaid: " + unpaid);

        // Save full report to file
        String path = "full_system_report.txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("HEALTH CLUB — FULL SYSTEM REPORT");
            pw.println("Generated: " + LocalDate.now());
            pw.println("=".repeat(50));
            pw.println("\n[ADMINS]");
            for (User u : users) if (u instanceof Admin) pw.println("  " + u.generateReport());
            pw.println("\n[COACHES]");
            for (User u : users) if (u instanceof Coach) pw.println("  " + u.generateReport());
            pw.println("\n[MEMBERS]");
            for (User u : users) if (u instanceof Member)
                pw.println("  " + u.generateReport() +
                    " | Status: " + (isExpired(((Member) u).getSubscriptionEndDate()) ? "EXPIRED" : "ACTIVE"));
            pw.println("\n[BILLING]");
            pw.println("  Total: " + bills.size() + " | Unpaid: " + unpaid);
            System.out.println("[OK] Full report saved to: " + path);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not save full report: " + e.getMessage());
        }
    }

    // ==================== NOTIFICATIONS ====================

    /**
     * Checks all members' subscription dates and prints alerts.
     * Called automatically after Admin login and manually from Admin menu.
     */
    public static void checkNotifications(ArrayList<User> users) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       SUBSCRIPTION NOTIFICATIONS       ║");
        System.out.println("╚════════════════════════════════════════╝");
        boolean found = false;
        for (User u : users) {
            if (u instanceof Member) {
                Member m   = (Member) u;
                String end = m.getSubscriptionEndDate();
                if (end == null || end.isEmpty()) continue;
                if (isExpired(end)) {
                    System.out.println("  [EXPIRED ❌] " + m.getName() +
                        " — expired on " + end);
                    found = true;
                } else {
                    long days = daysUntil(end);
                    if (days <= 7) {
                        System.out.println("  [WARNING ⚠️ ] " + m.getName() +
                            " — expires in " + days + " day(s) (" + end + ")");
                        found = true;
                    }
                }
            }
        }
        if (!found)
            System.out.println("  [OK ✅] All subscriptions are up to date.");
        System.out.println("═".repeat(42));
    }


    // ==================== ATTENDANCE ====================
    private static final String ATT_FILE = "attendance.txt";

    public static java.util.ArrayList<Attendance> loadAttendance() {
        java.util.ArrayList<Attendance> list = new java.util.ArrayList<>();
        java.io.File f = new java.io.File(ATT_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Attendance a = Attendance.fromCSV(line);
                if (a != null) list.add(a);
            }
        } catch (IOException e) { System.out.println("[ERROR] attendance: " + e.getMessage()); }
        return list;
    }

    public static void saveAttendance(java.util.ArrayList<Attendance> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ATT_FILE))) {
            for (Attendance a : list) bw.write(a.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save attendance: " + e.getMessage()); }
    }

    // ==================== INVENTORY ====================
    private static final String INV_FILE = "inventory.txt";

    public static java.util.ArrayList<InventoryItem> loadInventory() {
        java.util.ArrayList<InventoryItem> list = new java.util.ArrayList<>();
        java.io.File f = new java.io.File(INV_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                InventoryItem it = InventoryItem.fromCSV(line);
                if (it != null) list.add(it);
            }
        } catch (IOException e) { System.out.println("[ERROR] inventory: " + e.getMessage()); }
        return list;
    }

    public static void saveInventory(java.util.ArrayList<InventoryItem> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(INV_FILE))) {
            for (InventoryItem it : list) bw.write(it.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save inventory: " + e.getMessage()); }
    }

    // ==================== SALARIES ====================
    private static final String SAL_FILE = "salaries.txt";

    public static java.util.ArrayList<Salary> loadSalaries() {
        java.util.ArrayList<Salary> list = new java.util.ArrayList<>();
        java.io.File f = new java.io.File(SAL_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Salary s = Salary.fromCSV(line);
                if (s != null) list.add(s);
            }
        } catch (IOException e) { System.out.println("[ERROR] salaries: " + e.getMessage()); }
        return list;
    }

    public static void saveSalaries(java.util.ArrayList<Salary> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SAL_FILE))) {
            for (Salary s : list) bw.write(s.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save salaries: " + e.getMessage()); }
    }

    // ==================== WORKOUT PLANS ====================
    private static final String WP_FILE = "workout_plans.txt";

    public static java.util.ArrayList<WorkoutPlan> loadWorkoutPlans() {
        java.util.ArrayList<WorkoutPlan> list = new java.util.ArrayList<>();
        java.io.File f = new java.io.File(WP_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                WorkoutPlan wp = WorkoutPlan.fromCSV(line);
                if (wp != null) list.add(wp);
            }
        } catch (IOException e) { System.out.println("[ERROR] workout plans: " + e.getMessage()); }
        return list;
    }

    public static void saveWorkoutPlans(java.util.ArrayList<WorkoutPlan> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(WP_FILE))) {
            for (WorkoutPlan wp : list) bw.write(wp.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save workout plans: " + e.getMessage()); }
    }

    // ==================== PIN MANAGEMENT ====================
    private static final String PIN_FILE = "pins.txt";

    private static final String COUPON_FILE   = "coupons.txt";
    private static final String FREEZE_FILE   = "frozen.txt";
    private static final String FAMILY_FILE   = "family_packages.txt";
    private static final String REFERRAL_FILE = "referrals.txt";
    private static final String SPLIT_FILE    = "split_payments.txt";
    private static final String CONTRACT_FILE = "contracts.txt";
    private static final String PROGRESS_FILE = "body_progress.txt";


    public static java.util.Map<Integer,String> loadPins() {
        java.util.Map<Integer,String> map = new java.util.HashMap<>();
        java.io.File f = new java.io.File(PIN_FILE);
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length == 2) map.put(Integer.parseInt(p[0].trim()), p[1].trim());
            }
        } catch (IOException e) { System.out.println("[ERROR] pins: " + e.getMessage()); }
        return map;
    }

    public static void savePins(java.util.Map<Integer,String> pins) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PIN_FILE))) {
            for (java.util.Map.Entry<Integer,String> e : pins.entrySet())
                bw.write(e.getKey() + "," + e.getValue() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save pins: " + e.getMessage()); }
    }

    // ==================== BACKUP ====================
    public static void createBackup() {
        String backupDir = "backup_" + LocalDate.now().toString();
        new java.io.File(backupDir).mkdirs();
        String[] files = {USERS_FILE, BILLS_FILE, MSG_FILE, SCH_FILE,
                          ATT_FILE, INV_FILE, SAL_FILE, WP_FILE, PIN_FILE};
        for (String fname : files) {
            java.io.File src = new java.io.File(fname);
            if (!src.exists()) continue;
            try {
                java.nio.file.Files.copy(src.toPath(),
                    new java.io.File(backupDir + "/" + fname).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) { System.out.println("[WARN] backup failed for " + fname); }
        }
        System.out.println("[OK] Backup created in: " + backupDir);
    }

    // ==================== SUBSCRIPTION NOTIFICATIONS ====================
    public static java.util.List<String> getExpiryAlerts(java.util.ArrayList<User> users) {
        java.util.List<String> alerts = new java.util.ArrayList<>();
        for (User u : users) {
            if (!(u instanceof Member)) continue;
            Member m = (Member) u;
            String end = m.getSubscriptionEndDate();
            if (end == null || end.isEmpty()) continue;
            if (isExpired(end))
                alerts.add("EXPIRED|" + m.getId() + "|" + m.getName() + "|" + end);
            else if (daysUntil(end) <= 7)
                alerts.add("EXPIRING|" + m.getId() + "|" + m.getName() + "|" + end + "|" + daysUntil(end));
        }
        return alerts;
    }

    // ==================== PRIVATE HELPERS ====================

    public static boolean isExpired(String dateStr) {
        try {
            return LocalDate.now().isAfter(LocalDate.parse(dateStr));
        } catch (Exception e) { return false; }
    }

    public static long daysUntil(String dateStr) {
        try {
            return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(dateStr));
        } catch (Exception e) { return 999; }
    }

    // ==================== COUPONS ====================


    public static ArrayList<Coupon> loadCoupons() {
        ArrayList<Coupon> list = new ArrayList<>();
        File f = new File(COUPON_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Coupon c = Coupon.fromCSV(line.trim());
                if (c != null) list.add(c);
            }
        } catch (IOException e) { System.out.println("[ERROR] coupons: " + e.getMessage()); }
        return list;
    }

    public static void saveCoupons(ArrayList<Coupon> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(COUPON_FILE))) {
            for (Coupon c : list) bw.write(c.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save coupons: " + e.getMessage()); }
    }

    /** Find a valid (active + not expired) coupon by code. */
    public static Coupon findValidCoupon(String code, ArrayList<Coupon> coupons) {
        for (Coupon c : coupons) {
            if (c.isActive() && c.getCode().equalsIgnoreCase(code.trim())) {
                // check not expired
                try {
                    java.time.LocalDate expiry = java.time.LocalDate.parse(c.getExpiryDate(),
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                    if (!expiry.isBefore(java.time.LocalDate.now())) return c;
                } catch (Exception ignored) { return c; }
            }
        }
        return null;
    }

    // ==================== FROZEN MEMBERSHIPS ====================


    public static ArrayList<FrozenMembership> loadFrozen() {
        ArrayList<FrozenMembership> list = new ArrayList<>();
        File f = new File(FREEZE_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                FrozenMembership fm = FrozenMembership.fromCSV(line.trim());
                if (fm != null) list.add(fm);
            }
        } catch (IOException e) { System.out.println("[ERROR] frozen: " + e.getMessage()); }
        return list;
    }

    public static void saveFrozen(ArrayList<FrozenMembership> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FREEZE_FILE))) {
            for (FrozenMembership f : list) bw.write(f.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save frozen: " + e.getMessage()); }
    }

    /** Returns true if the given member is currently frozen. */
    public static boolean isMemberFrozen(int memberId, ArrayList<FrozenMembership> freezes) {
        for (FrozenMembership f : freezes)
            if (f.getMemberId() == memberId && f.isActive()) return true;
        return false;
    }

    /** Returns the active freeze record for a member, or null. */
    public static FrozenMembership getActiveFreeze(int memberId, ArrayList<FrozenMembership> freezes) {
        for (FrozenMembership f : freezes)
            if (f.getMemberId() == memberId && f.isActive()) return f;
        return null;
    }

    // ==================== FAMILY PACKAGES ====================


    public static ArrayList<FamilyPackage> loadFamilyPackages() {
        ArrayList<FamilyPackage> list = new ArrayList<>();
        File f = new File(FAMILY_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                FamilyPackage fp = FamilyPackage.fromCSV(line.trim());
                if (fp != null) list.add(fp);
            }
        } catch (IOException e) { System.out.println("[ERROR] family: " + e.getMessage()); }
        return list;
    }

    public static void saveFamilyPackages(ArrayList<FamilyPackage> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FAMILY_FILE))) {
            for (FamilyPackage fp : list) bw.write(fp.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save family: " + e.getMessage()); }
    }

    /** Returns the FamilyPackage a member belongs to, or null. */
    public static FamilyPackage getMemberFamily(int memberId, ArrayList<FamilyPackage> packages) {
        for (FamilyPackage fp : packages)
            if (fp.isActive() && fp.getMemberIds().contains(memberId)) return fp;
        return null;
    }

    // ==================== REFERRALS ====================


    public static ArrayList<Referral> loadReferrals() {
        ArrayList<Referral> list = new ArrayList<>();
        File f = new File(REFERRAL_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Referral r = Referral.fromCSV(line.trim());
                if (r != null) list.add(r);
            }
        } catch (IOException e) { System.out.println("[ERROR] referrals: " + e.getMessage()); }
        return list;
    }

    public static void saveReferrals(ArrayList<Referral> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REFERRAL_FILE))) {
            for (Referral r : list) bw.write(r.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save referrals: " + e.getMessage()); }
    }

    /**
     * Award 1 free month to the referrer.
     * Adds 30 days to their subscriptionEndDate and marks the referral as REWARDED.
     */
    public static String applyReferralReward(Referral ref, ArrayList<User> users) {
        for (User u : users) {
            if (u instanceof Member && u.getId() == ref.getReferrerId()) {
                Member m = (Member) u;
                try {
                    java.time.LocalDate end = java.time.LocalDate.parse(
                        m.getSubscriptionEndDate(),
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                    end = end.plusDays(30);
                    String newEnd = end.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                    m.setSubscriptionEndDate(newEnd);
                    ref.reward("1 free month added, new end: " + newEnd);
                    return newEnd;
                } catch (Exception e) { return ""; }
            }
        }
        return "";
    }

    // ==================== SPLIT PAYMENTS ====================


    public static ArrayList<SplitPayment> loadSplitPayments() {
        ArrayList<SplitPayment> list = new ArrayList<>();
        File f = new File(SPLIT_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                SplitPayment sp = SplitPayment.fromCSV(line.trim());
                if (sp != null) list.add(sp);
            }
        } catch (IOException e) { System.out.println("[ERROR] split: " + e.getMessage()); }
        return list;
    }

    public static void saveSplitPayments(ArrayList<SplitPayment> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SPLIT_FILE))) {
            for (SplitPayment sp : list) bw.write(sp.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save split: " + e.getMessage()); }
    }

    // ==================== DIGITAL CONTRACTS ====================


    public static ArrayList<DigitalContract> loadContracts() {
        ArrayList<DigitalContract> list = new ArrayList<>();
        File f = new File(CONTRACT_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                DigitalContract c = DigitalContract.fromCSV(line.trim());
                if (c != null) list.add(c);
            }
        } catch (IOException e) { System.out.println("[ERROR] contracts: " + e.getMessage()); }
        return list;
    }

    public static void saveContracts(ArrayList<DigitalContract> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CONTRACT_FILE))) {
            for (DigitalContract c : list) bw.write(c.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save contracts: " + e.getMessage()); }
    }

    // ==================== BODY PROGRESS ====================


    public static ArrayList<BodyProgress> loadBodyProgress() {
        ArrayList<BodyProgress> list = new ArrayList<>();
        File f = new File(PROGRESS_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                BodyProgress bp = BodyProgress.fromCSV(line.trim());
                if (bp != null) list.add(bp);
            }
        } catch (IOException e) { System.out.println("[ERROR] body progress: " + e.getMessage()); }
        return list;
    }

    public static void saveBodyProgress(ArrayList<BodyProgress> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PROGRESS_FILE))) {
            for (BodyProgress bp : list) bw.write(bp.toCSV() + "\n");
        } catch (IOException e) { System.out.println("[ERROR] save body progress: " + e.getMessage()); }
    }

    /** Returns all progress entries for a specific member, sorted by date. */
    public static ArrayList<BodyProgress> getMemberProgress(int memberId,
                                                             ArrayList<BodyProgress> all) {
        ArrayList<BodyProgress> result = new ArrayList<>();
        for (BodyProgress bp : all)
            if (bp.getMemberId() == memberId) result.add(bp);
        result.sort((a, b) -> a.getRecordDate().compareTo(b.getRecordDate()));
        return result;
    }

    // ==================== AUTO LOCK ====================

    /**
     * Auto-lock: checks all members and locks accounts whose subscription has expired.
     * "Locked" is represented by appending "-LOCKED" to the password in memory.
     * Call this on login and on app startup.
     * Returns a list of member names that were locked.
     */
    public static ArrayList<String> autoLockExpired(ArrayList<User> users,
                                                     ArrayList<FrozenMembership> freezes) {
        ArrayList<String> locked = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

        for (User u : users) {
            if (!(u instanceof Member)) continue;
            Member m = (Member) u;
            if (isMemberFrozen(m.getId(), freezes)) continue; // skip frozen members
            try {
                java.time.LocalDate end = java.time.LocalDate.parse(m.getSubscriptionEndDate(), df);
                if (end.isBefore(today)) {
                    // mark locked if not already
                    if (!m.getPassword().endsWith("-LOCKED")) {
                        m.setPassword(m.getPassword() + "-LOCKED");
                        locked.add(m.getName());
                    }
                }
            } catch (Exception ignored) {}
        }
        return locked;
    }

    /**
     * Unlock a member (admin renews subscription).
     * Removes -LOCKED suffix from password.
     */
    public static void unlockMember(Member m) {
        String pwd = m.getPassword();
        if (pwd.endsWith("-LOCKED"))
            m.setPassword(pwd.substring(0, pwd.length() - 7));
    }

    public static boolean isLocked(Member m) {
        return m.getPassword().endsWith("-LOCKED");
    }

}
