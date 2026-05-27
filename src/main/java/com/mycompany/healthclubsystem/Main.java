package com.mycompany.healthclubsystem;

import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 *  HEALTH CLUB MANAGEMENT SYSTEM
 * ============================================================
 *  Course  : COM 210 – Advanced Programming
 *  Project : Project 10 — Health Club Management System
 * ============================================================
 *
 *  OOP CONCEPTS DEMONSTRATED:
 *  ┌─────────────────┬────────────────────────────────────────┐
 *  │ Inheritance     │ Admin, Coach, Member  ←  User          │
 *  │ Encapsulation   │ Private fields + Getters/Setters        │
 *  │ Abstraction     │ Abstract class User + abstract methods  │
 *  │ Polymorphism    │ toCSV(), generateReport() overridden    │
 *  │ Interface       │ Reportable implemented by all Users     │
 *  │ Aggregation     │ Member "has-a" Coach (independent life) │
 *  │ Association     │ Schedule & Message use Coach & Member   │
 *  │ Composition     │ Main owns all ArrayLists                │
 *  └─────────────────┴────────────────────────────────────────┘
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // COMPOSITION: Main owns these lists; destroyed when Main exits
        ArrayList<User>     users     = FileManager.loadUsers();
        ArrayList<Bill>     bills     = FileManager.loadBills();
        ArrayList<Message>  messages  = FileManager.loadMessages();
        ArrayList<Schedule> schedules = FileManager.loadSchedules();

        // ASSOCIATION + AGGREGATION: link objects after loading from files
        FileManager.linkRelations(users, messages, schedules);

        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

        while (true) {
            User currentUser = null;

            // ================== LOGIN / REGISTER LOOP ==================
            while (currentUser == null) {
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.println("║     HEALTH CLUB MANAGEMENT SYSTEM    ║");
                System.out.println("╚══════════════════════════════════════╝");
                System.out.println("  1. Register");
                System.out.println("  2. Login");
                System.out.print("  Choose: ");

                int option;
                try {
                    option = sc.nextInt(); sc.nextLine();
                } catch (Exception e) {
                    System.out.println("[!] Invalid input."); sc.nextLine(); continue;
                }

                // ─────────────── REGISTER ───────────────
                if (option == 1) {
                    register(sc, users, df);

                // ─────────────── LOGIN ───────────────
                } else if (option == 2) {
                    System.out.print("  Username: ");
                    String uname = sc.nextLine().trim();
                    System.out.print("  Password: ");
                    String pass  = sc.nextLine();

                    currentUser = FileManager.findUserByUsername(uname, users);
                    if (currentUser == null) {
                        System.out.println("[!] User not found.");
                        currentUser = null;
                    } else if (!currentUser.getPassword().equals(pass)) {
                        System.out.println("[!] Wrong password.");
                        currentUser = null;
                    } else {
                        System.out.println("\n[✓] Welcome, " + currentUser.getName()
                            + " (" + currentUser.getRole() + ")");

                        // AUTO NOTIFICATION after login (Requirement 1f & 3c)
                        notifyOnLogin(currentUser, users);
                    }
                } else {
                    System.out.println("[!] Invalid choice.");
                }
            }

            // ================== ROLE-BASED MENU LOOP ==================
            while (true) {
                printMenu(currentUser.getRole());
                System.out.print("\n  Choose: ");
                int choice;
                try {
                    choice = sc.nextInt(); sc.nextLine();
                } catch (Exception e) {
                    System.out.println("[!] Invalid input."); sc.nextLine(); continue;
                }

                boolean logout = false;
                switch (currentUser.getRole()) {
                    case "admin":
                        logout = adminActions(choice, sc, users, bills, messages, schedules, df, currentUser);
                        break;
                    case "coach":
                        logout = coachActions(choice, sc, users, messages, schedules, (Coach) currentUser, df);
                        break;
                    default:
                        logout = memberActions(choice, sc, (Member) currentUser, users, messages, schedules);
                }
                if (logout) break;
            }
        }
    }

    // ============================================================
    //  REGISTRATION
    // ============================================================
    private static void register(Scanner sc, ArrayList<User> users, DateTimeFormatter df) {
        System.out.println("\n── REGISTER ────────────────────────────");

        // ID
        int id;
        while (true) {
            System.out.print("  ID (number): ");
            if (sc.hasNextInt()) {
                id = sc.nextInt(); sc.nextLine();
                if (id <= 0)                                System.out.println("  [!] ID must be positive!");
                else if (FileManager.isIdExists(id, users)) System.out.println("  [!] ID already exists!");
                else break;
            } else { System.out.println("  [!] ID must be a number!"); sc.next(); }
        }

        // Name
        String name;
        while (true) {
            System.out.print("  Full Name: ");
            name = sc.nextLine().trim();
            if (name.isEmpty())                System.out.println("  [!] Name cannot be empty!");
            else if (!name.matches("[a-zA-Z ]+")) System.out.println("  [!] Name: letters only.");
            else break;
        }

        // Username
        String username;
        while (true) {
            System.out.print("  Username (lowercase): ");
            username = sc.nextLine().trim();
            if (username.isEmpty())                         System.out.println("  [!] Username cannot be empty!");
            else if (!username.matches("[a-z0-9_]+"))       System.out.println("  [!] Lowercase letters, numbers, _ only.");
            else if (FileManager.isUsernameExists(username, users)) System.out.println("  [!] Username already taken!");
            else break;
        }

        // Password
        String password;
        while (true) {
            System.out.print("  Password (min 4 chars): ");
            password = sc.nextLine();
            if (password.length() < 4) System.out.println("  [!] Password too short!");
            else break;
        }

        // Role
        String role;
        while (true) {
            System.out.print("  Role (admin / coach / member): ");
            role = sc.nextLine().trim().toLowerCase();
            if (role.equals("admin") || role.equals("coach") || role.equals("member")) break;
            System.out.println("  [!] Invalid role.");
        }

        if (role.equals("member")) {
            System.out.print("  Assigned Coach Name: ");
            String coachName = sc.nextLine().trim();

            String subDate;
            while (true) {
                System.out.print("  Subscription End Date (YYYY-MM-DD): ");
                subDate = sc.nextLine().trim();
                try { LocalDate.parse(subDate); break; }
                catch (Exception e) { System.out.println("  [!] Invalid date — use YYYY-MM-DD."); }
            }

            // AGGREGATION: find Coach object and assign it to Member
            Coach coach = FileManager.findCoachByName(coachName, users);
            Member newMember = new Member(id, name, username, password, coachName, subDate);
            if (coach != null) newMember.setAssignedCoach(coach);
            users.add(newMember);

        } else if (role.equals("coach")) {
            users.add(new Coach(id, name, username, password));
        } else {
            users.add(new Admin(id, name, username, password));
        }

        FileManager.saveUsers(users);
        System.out.println("  [✓] Registered successfully! Please login.");
    }

    // ============================================================
    //  MENUS
    // ============================================================
    private static void printMenu(String role) {
        switch (role) {
            case "admin":
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.println("║           ADMIN MENU                 ║");
                System.out.println("╠══════════════════════════════════════╣");
                System.out.println("║  1. Manage Users (Add/List/Search/   ║");
                System.out.println("║                  Edit/Delete)        ║");
                System.out.println("║  2. Assign Member to Coach           ║");
                System.out.println("║  3. Manage Billing                   ║");
                System.out.println("║  4. Generate Member Report           ║");
                System.out.println("║  5. Generate Full System Report      ║");
                System.out.println("║  6. Check Subscription Notifications ║");
                System.out.println("║  7. Update My Info                   ║");
                System.out.println("║  8. Save & Logout                    ║");
                System.out.println("╚══════════════════════════════════════╝");
                break;
            case "coach":
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.println("║           COACH MENU                 ║");
                System.out.println("╠══════════════════════════════════════╣");
                System.out.println("║  1. View My Assigned Members         ║");
                System.out.println("║  2. Add Training Schedule / Plan     ║");
                System.out.println("║  3. Send Message to All My Members   ║");
                System.out.println("║  4. Update My Info                   ║");
                System.out.println("║  5. Save & Logout                    ║");
                System.out.println("╚══════════════════════════════════════╝");
                break;
            default:
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.println("║           MEMBER MENU                ║");
                System.out.println("╠══════════════════════════════════════╣");
                System.out.println("║  1. View Subscription End Date       ║");
                System.out.println("║  2. View Coach & My Schedules        ║");
                System.out.println("║  3. View Messages from Coach         ║");
                System.out.println("║  4. Update My Info                   ║");
                System.out.println("║  5. Save & Logout                    ║");
                System.out.println("╚══════════════════════════════════════╝");
        }
    }

    // ============================================================
    //  AUTO-NOTIFICATION ON LOGIN  (Requirements 1f & 3c)
    // ============================================================
    private static void notifyOnLogin(User currentUser, ArrayList<User> users) {
        if (currentUser instanceof Member) {
            Member m    = (Member) currentUser;
            String end  = m.getSubscriptionEndDate();
            try {
                LocalDate endDate = LocalDate.parse(end);
                long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
                if (days < 0) {
                    System.out.println("\n╔══════════════════════════════════════╗");
                    System.out.println("║  [!] ALERT: Your subscription has   ║");
                    System.out.println("║      EXPIRED! Please renew now.     ║");
                    System.out.println("╚══════════════════════════════════════╝");
                } else if (days <= 7) {
                    System.out.println("\n╔══════════════════════════════════════╗");
                    System.out.printf( "║  [!] WARNING: Expires in %d day(s)  ║%n", days);
                    System.out.println("║      Please renew your subscription.║");
                    System.out.println("╚══════════════════════════════════════╝");
                }
            } catch (Exception ignored) {}

        } else if (currentUser.getRole().equals("admin")) {
            // Admin sees all expiring/expired members on login
            FileManager.checkNotifications(users);
        }
    }

    // ============================================================
    //  ADMIN ACTIONS
    // ============================================================
    private static boolean adminActions(int c, Scanner sc, ArrayList<User> users,
            ArrayList<Bill> bills, ArrayList<Message> msgs,
            ArrayList<Schedule> schs, DateTimeFormatter df, User currentUser) {

        switch (c) {

            // ── 1. MANAGE USERS ──────────────────────────────────
            case 1:
                System.out.println("\n  1.Add  2.List  3.Search  4.Delete  5.Edit  6.Back");
                System.out.print("  Choose: ");
                int sub;
                try { sub = sc.nextInt(); sc.nextLine(); }
                catch (Exception e) { sc.nextLine(); return false; }

                if (sub == 1) {
                    // ── ADD ──
                    System.out.print("  ID: ");
                    int id;
                    try { id = sc.nextInt(); sc.nextLine(); }
                    catch (Exception e) { sc.nextLine(); System.out.println("  [!] Invalid ID."); return false; }
                    if (id <= 0)                                { System.out.println("  [!] ID must be positive.");  return false; }
                    if (FileManager.isIdExists(id, users))      { System.out.println("  [!] ID already exists.");   return false; }

                    System.out.print("  Name: ");    String n = sc.nextLine().trim();
                    if (!n.matches("[a-zA-Z ]+"))              { System.out.println("  [!] Name: letters only.");   return false; }

                    System.out.print("  Username: "); String uname = sc.nextLine().trim();
                    if (uname.isEmpty() || !uname.matches("[a-z0-9_]+"))
                                                               { System.out.println("  [!] Invalid username.");     return false; }
                    if (FileManager.isUsernameExists(uname, users))
                                                               { System.out.println("  [!] Username taken.");       return false; }

                    System.out.print("  Password: "); String pass = sc.nextLine();
                    if (pass.length() < 4)                     { System.out.println("  [!] Password too short.");  return false; }

                    System.out.print("  Role (admin/coach/member): "); String r = sc.nextLine().trim().toLowerCase();

                    if (r.equals("member")) {
                        System.out.print("  Coach Name: "); String cName = sc.nextLine().trim();
                        String se;
                        while (true) {
                            System.out.print("  Sub End (YYYY-MM-DD): "); se = sc.nextLine().trim();
                            try { LocalDate.parse(se); break; }
                            catch (Exception e) { System.out.println("  [!] Invalid date."); }
                        }
                        Coach coach = FileManager.findCoachByName(cName, users);
                        Member m2 = new Member(id, n, uname, pass, cName, se);
                        if (coach != null) m2.setAssignedCoach(coach); // AGGREGATION
                        users.add(m2);
                    } else if (r.equals("coach")) {
                        users.add(new Coach(id, n, uname, pass));
                    } else {
                        users.add(new Admin(id, n, uname, pass));
                    }
                    FileManager.saveUsers(users);
                    System.out.println("  [✓] User added.");

                } else if (sub == 2) {
                    // ── LIST ──
                    if (users.isEmpty()) { System.out.println("  No users found."); return false; }
                    System.out.printf("  %-4s | %-15s | %-15s | %-8s%n", "ID", "Name", "Username", "Role");
                    System.out.println("  " + "─".repeat(50));
                    for (User u : users)
                        System.out.printf("  %-4d | %-15s | %-15s | %-8s%n",
                            u.getId(), u.getName(), u.getUsername(), u.getRole());

                } else if (sub == 3) {
                    // ── SEARCH (by ID, name, OR username) ──
                    System.out.print("  Search (ID / name / username): ");
                    String q = sc.nextLine().trim().toLowerCase();
                    boolean found = false;
                    for (User u : users) {
                        if (String.valueOf(u.getId()).equals(q)
                                || u.getName().toLowerCase().contains(q)
                                || u.getUsername().toLowerCase().contains(q)) {
                            System.out.printf("  Found → ID:%-4d | Name:%-15s | Username:%-12s | Role:%s%n",
                                u.getId(), u.getName(), u.getUsername(), u.getRole());
                            found = true;
                        }
                    }
                    if (!found) System.out.println("  No matching users.");

                } else if (sub == 4) {
                    // ── DELETE ──
                    System.out.print("  ID to Delete: ");
                    int did;
                    try { did = sc.nextInt(); sc.nextLine(); }
                    catch (Exception e) { sc.nextLine(); return false; }
                    boolean removed = users.removeIf(u -> u.getId() == did);
                    if (removed) { FileManager.saveUsers(users); System.out.println("  [✓] Deleted."); }
                    else         System.out.println("  [!] User not found.");

                } else if (sub == 5) {
                    // ── EDIT ──
                    System.out.print("  ID to Edit: ");
                    int eid;
                    try { eid = sc.nextInt(); sc.nextLine(); }
                    catch (Exception e) { sc.nextLine(); return false; }

                    User u = FileManager.findUserById(eid, users);
                    if (u == null) { System.out.println("  [!] User not found."); return false; }

                    String newName;
                    while (true) {
                        System.out.print("  New Name: "); newName = sc.nextLine().trim();
                        if (newName.isEmpty())                System.out.println("  [!] Cannot be empty.");
                        else if (!newName.matches("[a-zA-Z ]+")) System.out.println("  [!] Letters only.");
                        else break;
                    }
                    String newUname;
                    while (true) {
                        System.out.print("  New Username: "); newUname = sc.nextLine().trim();
                        if (newUname.isEmpty() || !newUname.matches("[a-z0-9_]+"))
                            System.out.println("  [!] Lowercase, numbers, _ only.");
                        else if (!u.getUsername().equals(newUname) && FileManager.isUsernameExists(newUname, users))
                            System.out.println("  [!] Username taken.");
                        else break;
                    }
                    String newPass;
                    while (true) {
                        System.out.print("  New Password: "); newPass = sc.nextLine();
                        if (newPass.length() < 4) System.out.println("  [!] Too short.");
                        else break;
                    }
                    u.setName(newName); u.setUsername(newUname); u.setPassword(newPass);

                    if (u instanceof Member) {
                        Member mem = (Member) u;
                        System.out.print("  New Coach Name: "); String cName = sc.nextLine().trim();
                        Coach coach = FileManager.findCoachByName(cName, users);
                        if (coach != null) mem.setAssignedCoach(coach); // AGGREGATION
                        else mem.setAssignedCoachName(cName);
                        String newDate;
                        while (true) {
                            System.out.print("  New Sub End (YYYY-MM-DD): "); newDate = sc.nextLine().trim();
                            try { LocalDate.parse(newDate); break; }
                            catch (Exception e) { System.out.println("  [!] Invalid date."); }
                        }
                        mem.setSubscriptionEndDate(newDate);
                    }
                    FileManager.saveUsers(users);
                    System.out.println("  [✓] User updated.");
                }
                break;

            // ── 2. ASSIGN MEMBER TO COACH ──────────────────────
            case 2:
                System.out.print("  Member ID: ");
                int mid;
                try { mid = sc.nextInt(); sc.nextLine(); }
                catch (Exception e) { sc.nextLine(); return false; }

                System.out.print("  Coach Name: ");
                String cn = sc.nextLine().trim();
                if (cn.isEmpty()) { System.out.println("  [!] Coach name cannot be empty."); break; }

                User mUser = FileManager.findUserById(mid, users);
                if (mUser instanceof Member) {
                    Coach coach = FileManager.findCoachByName(cn, users);
                    if (coach != null) ((Member) mUser).setAssignedCoach(coach); // AGGREGATION
                    else ((Member) mUser).setAssignedCoachName(cn);
                    FileManager.saveUsers(users);
                    System.out.println("  [✓] Assigned " + mUser.getName() + " → Coach " + cn);
                } else {
                    System.out.println("  [!] Member not found.");
                }
                break;

            // ── 3. MANAGE BILLING ──────────────────────────────
            case 3:
                System.out.println("\n  1.Add Bill  2.Mark Paid  3.List All Bills");
                System.out.print("  Choose: ");
                int bch;
                try { bch = sc.nextInt(); sc.nextLine(); }
                catch (Exception e) { sc.nextLine(); return false; }

                if (bch == 1) {
                    System.out.print("  Member ID: ");
                    int bmi; try { bmi = sc.nextInt(); sc.nextLine(); } catch (Exception e) { sc.nextLine(); return false; }
                    System.out.print("  Amount: ");
                    double amt; try { amt = sc.nextDouble(); sc.nextLine(); } catch (Exception e) { sc.nextLine(); return false; }
                    System.out.print("  Description: ");
                    String desc = sc.nextLine().trim();
                    if (desc.isEmpty()) desc = "Subscription Fee";
                    bills.add(new Bill(bills.size() + 1, bmi, amt, desc, LocalDate.now().format(df)));
                    FileManager.saveBills(bills);
                    System.out.println("  [✓] Bill added.");

                } else if (bch == 2) {
                    System.out.print("  Bill ID to Mark Paid: ");
                    int bi; try { bi = sc.nextInt(); sc.nextLine(); } catch (Exception e) { sc.nextLine(); return false; }
                    boolean found = false;
                    for (Bill b : bills) {
                        if (b.getBillId() == bi) { b.setPaid(true); found = true;
                            FileManager.saveBills(bills);
                            System.out.println("  [✓] Bill #" + bi + " marked PAID."); break; }
                    }
                    if (!found) System.out.println("  [!] Bill not found.");

                } else {
                    if (bills.isEmpty()) { System.out.println("  No bills found."); break; }
                    System.out.printf("  %-6s | %-8s | %-10s | %-20s | %s%n",
                        "BillID", "MemberID", "Amount", "Description", "Status");
                    System.out.println("  " + "─".repeat(65));
                    for (Bill b : bills)
                        System.out.printf("  %-6d | %-8d | %-10.2f | %-20s | %s%n",
                            b.getBillId(), b.getMemberId(), b.getAmount(),
                            b.getDescription(), b.isPaid() ? "✅ PAID" : "❌ UNPAID");
                }
                break;

            // ── 4. MEMBER REPORT ───────────────────────────────
            case 4:
                FileManager.generateMemberReport(users);
                break;

            // ── 5. FULL SYSTEM REPORT ──────────────────────────
            case 5:
                FileManager.generateFullReport(users, bills);
                break;

            // ── 6. NOTIFICATIONS ───────────────────────────────
            case 6:
                FileManager.checkNotifications(users);
                break;

            // ── 7. UPDATE MY INFO ──────────────────────────────
            case 7:
                updateInfo(sc, currentUser, users);
                break;

            // ── 8. LOGOUT ──────────────────────────────────────
            case 8:
                FileManager.saveUsers(users);
                FileManager.saveBills(bills);
                FileManager.saveMessages(msgs);
                FileManager.saveSchedules(schs);
                System.out.println("  [✓] Saved. Logged out.");
                return true;

            default:
                System.out.println("  [!] Invalid choice.");
        }
        return false;
    }

    // ============================================================
    //  COACH ACTIONS
    // ============================================================
    private static boolean coachActions(int c, Scanner sc, ArrayList<User> users,
            ArrayList<Message> msgs, ArrayList<Schedule> schs,
            Coach coach, DateTimeFormatter df) {

        switch (c) {

            // ── 1. VIEW MY MEMBERS ─────────────────────────────
            case 1:
                System.out.println("\n── My Assigned Members ────────────────");
                System.out.printf("  %-4s | %-15s | %-12s%n", "ID", "Name", "Sub End");
                System.out.println("  " + "─".repeat(38));
                boolean anyMember = false;
                for (User u : users) {
                    if (u instanceof Member) {
                        Member m = (Member) u;
                        // AGGREGATION: compare using Coach object reference or name
                        if (m.getAssignedCoachName().equalsIgnoreCase(coach.getName())) {
                            System.out.printf("  %-4d | %-15s | %-12s%n",
                                m.getId(), m.getName(), m.getSubscriptionEndDate());
                            anyMember = true;
                        }
                    }
                }
                if (!anyMember) System.out.println("  No members assigned yet.");
                break;

            // ── 2. ADD SCHEDULE ────────────────────────────────
            case 2:
                System.out.print("  Member ID: ");
                int schedMid;
                try { schedMid = sc.nextInt(); sc.nextLine(); }
                catch (Exception e) { sc.nextLine(); return false; }

                System.out.print("  Plan Details: "); String plan = sc.nextLine().trim();
                if (plan.isEmpty()) { System.out.println("  [!] Plan cannot be empty."); break; }

                String date;
                while (true) {
                    System.out.print("  Date (YYYY-MM-DD): "); date = sc.nextLine().trim();
                    try { LocalDate.parse(date); break; }
                    catch (Exception e) { System.out.println("  [!] Invalid date format."); }
                }

                // ASSOCIATION: Schedule links Coach and Member objects
                User schedTarget = FileManager.findUserById(schedMid, users);
                Member schedMember = (schedTarget instanceof Member) ? (Member) schedTarget : null;
                schs.add(new Schedule(schs.size() + 1, coach, schedMember, plan, date));
                FileManager.saveSchedules(schs);
                System.out.println("  [✓] Schedule added.");
                break;

            // ── 3. SEND MESSAGE TO ALL MY MEMBERS ─────────────
            case 3:
                System.out.print("  Message Content: "); String content = sc.nextLine().trim();
                if (content.isEmpty()) { System.out.println("  [!] Message cannot be empty."); break; }
                int sentCount = 0;
                for (User u : users) {
                    if (u instanceof Member) {
                        Member m = (Member) u;
                        if (m.getAssignedCoachName().equalsIgnoreCase(coach.getName().trim())) {
                            // ASSOCIATION: Message holds references to Coach and Member
                            msgs.add(new Message(msgs.size() + 1, coach, m, content,
                                LocalDate.now().format(df)));
                            sentCount++;
                        }
                    }
                }
                FileManager.saveMessages(msgs);
                System.out.println("  [✓] Message sent to " + sentCount + " member(s).");
                break;

            // ── 4. UPDATE MY INFO ──────────────────────────────
            case 4:
                updateInfo(sc, coach, users);
                break;

            // ── 5. LOGOUT ──────────────────────────────────────
            case 5:
                FileManager.saveUsers(users);
                FileManager.saveMessages(msgs);
                FileManager.saveSchedules(schs);
                System.out.println("  [✓] Saved. Logged out.");
                return true;

            default:
                System.out.println("  [!] Invalid choice.");
        }
        return false;
    }

    // ============================================================
    //  MEMBER ACTIONS
    // ============================================================
    private static boolean memberActions(int c, Scanner sc, Member member,
            ArrayList<User> users, ArrayList<Message> msgs, ArrayList<Schedule> schs) {

        switch (c) {

            // ── 1. VIEW SUBSCRIPTION DATE ──────────────────────
            case 1:
                System.out.println("\n── Subscription Info ────────────────────");
                System.out.println("  End Date : " + member.getSubscriptionEndDate());
                try {
                    LocalDate end  = LocalDate.parse(member.getSubscriptionEndDate());
                    long days      = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), end);
                    if (days < 0)
                        System.out.println("  Status   : ❌ EXPIRED (" + Math.abs(days) + " days ago)");
                    else if (days <= 7)
                        System.out.println("  Status   : ⚠️  Expires in " + days + " day(s)!");
                    else
                        System.out.println("  Status   : ✅ ACTIVE (" + days + " days remaining)");
                } catch (Exception e) { System.out.println("  [!] Invalid date format."); }
                break;

            // ── 2. VIEW COACH & SCHEDULE ───────────────────────
            case 2:
                System.out.println("\n── Coach & Schedule ─────────────────────");
                // AGGREGATION: access Coach details through Member's Coach reference
                System.out.println("  Assigned Coach : " + member.getAssignedCoachName());
                if (member.getAssignedCoach() != null)
                    System.out.println("  Coach Username : " + member.getAssignedCoach().getUsername());

                System.out.println("\n  Your Schedules:");
                System.out.printf("  %-12s | %s%n", "Date", "Plan");
                System.out.println("  " + "─".repeat(45));
                boolean anySchedule = false;
                for (Schedule s : schs) {
                    if (s.getMemberId() == member.getId()) {
                        System.out.printf("  %-12s | %s%n", s.getDate(), s.getPlanDetails());
                        anySchedule = true;
                    }
                }
                if (!anySchedule) System.out.println("  No schedules yet.");
                break;

            // ── 3. VIEW MESSAGES ───────────────────────────────
            case 3:
                System.out.println("\n── Messages from Your Coach ─────────────");
                boolean anyMsg = false;
                for (Message msg : msgs) {
                    if (msg.getToMemberId() == member.getId()) {
                        // ASSOCIATION: access Coach info via Message's reference
                        System.out.println("  From : " + msg.getFromCoachName());
                        System.out.println("  Date : " + msg.getDate());
                        System.out.println("  Msg  : " + msg.getContent());
                        System.out.println("  " + "─".repeat(40));
                        anyMsg = true;
                    }
                }
                if (!anyMsg) System.out.println("  No messages yet.");
                break;

            // ── 4. UPDATE MY INFO ──────────────────────────────
            case 4:
                updateInfo(sc, member, users);
                break;

            // ── 5. LOGOUT ──────────────────────────────────────
            case 5:
                FileManager.saveMessages(msgs);
                FileManager.saveSchedules(schs);
                System.out.println("  [✓] Saved. Logged out.");
                return true;

            default:
                System.out.println("  [!] Invalid choice.");
        }
        return false;
    }

    // ============================================================
    //  SHARED: UPDATE INFO  (Requirement 4b: update except ID)
    // ============================================================
    private static void updateInfo(Scanner sc, User user, ArrayList<User> users) {
        System.out.println("\n── Update My Info ───────────────────────");
        System.out.print("  Current Password: ");
        String current = sc.nextLine();
        if (!user.getPassword().equals(current)) {
            System.out.println("  [!] Wrong password!"); return;
        }

        System.out.print("  New Name: ");
        String n = sc.nextLine().trim();
        if (!n.matches("[a-zA-Z ]+")) { System.out.println("  [!] Name: letters only!"); return; }

        System.out.print("  New Username: ");
        String u = sc.nextLine().trim();
        if (u.isEmpty() || !u.matches("[a-z0-9_]+")) { System.out.println("  [!] Invalid username!"); return; }
        if (!user.getUsername().equals(u) && FileManager.isUsernameExists(u, users)) {
            System.out.println("  [!] Username already taken!"); return;
        }

        System.out.print("  New Password: ");
        String p = sc.nextLine();
        if (p.length() < 4) { System.out.println("  [!] Password too short!"); return; }

        // NOTE: ID is intentionally NOT updated (Requirement 4b)
        user.setName(n);
        user.setUsername(u);
        user.setPassword(p);
        FileManager.saveUsers(users);
        System.out.println("  [✓] Info updated successfully.");
    }
}
