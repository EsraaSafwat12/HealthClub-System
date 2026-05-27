// NewFeaturesAdminTab.java
package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import com.mycompany.healthclubsystem.FileManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NewFeaturesAdminTab — contains all show*() methods for new admin features.
 *
 * HOW TO INTEGRATE:
 *   In AdminScreen.java, add these sidebar buttons and wire them up:
 *
 *   Sidebar buttons to add (after existing ones):
 *     "📊  Charts"
 *     "🎁  Coupons"
 *     "❄️  Freeze"
 *     "👨‍👩‍👧  Family"
 *     "🎯  Referrals"
 *     "💳  Split Pay"
 *     "📋  Contracts"
 *     "📱  WhatsApp"
 *
 *   Then in show(Stage stage), wire each button:
 *     btns[N].setOnAction(e -> NewFeaturesAdminTab.showCharts(contentArea, msgLabel, bills, attendanceList));
 *     btns[N+1].setOnAction(e -> NewFeaturesAdminTab.showCoupons(contentArea, msgLabel, coupons));
 *     // ... etc.
 */
public class NewFeaturesAdminTab {

    private static final DateTimeFormatter DF = DateTimeFormatter.ISO_LOCAL_DATE;

    // ─────────────────────────────────────────────────────────────
    //  CHARTS TAB
    // ─────────────────────────────────────────────────────────────

    public static void showCharts(VBox content, Label msg,
                                  ArrayList<Bill> bills,
                                  ArrayList<Attendance> attendance,
                                  ArrayList<BodyProgress> progress,
                                  ArrayList<User> users) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("📊 Charts & Analytics"));

        // Revenue chart toggle
        HBox toggleRow = new HBox(12);
        toggleRow.setAlignment(Pos.CENTER_LEFT);
        Button btnMonthly = styledBtn("Monthly Revenue", "#FF6B00");
        Button btnAnnual  = styledBtn("Annual Revenue",  "#555");
        VBox[] revenueHolder = {new VBox(ChartsPanel.revenueChart(bills, true))};

        btnMonthly.setOnAction(e -> {
            content.getChildren().remove(revenueHolder[0]);
            revenueHolder[0] = new VBox(ChartsPanel.revenueChart(bills, true));
            content.getChildren().add(revenueHolder[0]);
        });
        btnAnnual.setOnAction(e -> {
            content.getChildren().remove(revenueHolder[0]);
            revenueHolder[0] = new VBox(ChartsPanel.revenueChart(bills, false));
            content.getChildren().add(revenueHolder[0]);
        });
        toggleRow.getChildren().addAll(new Label("Revenue: "), btnMonthly, btnAnnual);
        content.getChildren().add(toggleRow);
        content.getChildren().add(revenueHolder[0]);

        // Attendance chart (all members)
        content.getChildren().add(ChartsPanel.attendanceChart(attendance, 0, "Gym Attendance (All Members)"));

        // Body progress — member selector
        Label bpTitle = sectionTitle("Body Progress Chart");
        content.getChildren().add(bpTitle);
        ComboBox<String> memberPicker = new ComboBox<>();
        memberPicker.getItems().add("-- Select Member --");
        for (User u : users)
            if (u instanceof Member) memberPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
        memberPicker.setValue("-- Select Member --");
        VBox[] bpHolder = {new VBox(ChartsPanel.bodyProgressChart(new ArrayList<>(), ""))};

        memberPicker.setOnAction(e -> {
            String sel = memberPicker.getValue();
            if (sel == null || sel.startsWith("--")) return;
            try {
                int id = Integer.parseInt(sel.replaceAll(".*\\[(\\d+)\\]", "$1"));
                List<BodyProgress> memberProg = FileManager.getMemberProgress(id, progress);
                content.getChildren().remove(bpHolder[0]);
                bpHolder[0] = new VBox(ChartsPanel.bodyProgressChart(memberProg, sel.split(" \\[")[0]));
                content.getChildren().add(bpHolder[0]);
            } catch (Exception ignored) {}
        });
        content.getChildren().addAll(memberPicker, bpHolder[0]);
    }

    // ─────────────────────────────────────────────────────────────
    //  COUPONS TAB
    // ─────────────────────────────────────────────────────────────

    public static void showCoupons(VBox content, Label msg, ArrayList<Coupon> coupons) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("🎁 Discount Coupons"));

        // Add coupon form
        GridPane form = twoColGrid();
        TextField codeF   = field("e.g. GYM20");
        ComboBox<String> typeF = new ComboBox<>();
        typeF.getItems().addAll("PERCENTAGE", "FIXED");
        typeF.setValue("PERCENTAGE");
        TextField valueF  = field("e.g. 20");
        TextField expiryF = field("YYYY-MM-DD");
        expiryF.setText(LocalDate.now().plusMonths(1).format(DF));

        addRow(form, 0, "Coupon Code:", codeF);
        addRow(form, 1, "Type:", typeF);
        addRow(form, 2, "Value:", valueF);
        addRow(form, 3, "Expiry Date:", expiryF);

        Button addBtn = styledBtn("➕ Add Coupon", "#FF6B00");
        addBtn.setOnAction(e -> {
            String code = codeF.getText().trim().toUpperCase();
            if (code.isEmpty()) { msg.setText("❌ Enter coupon code."); return; }
            for (Coupon c : coupons)
                if (c.getCode().equalsIgnoreCase(code)) { msg.setText("❌ Code already exists."); return; }
            try {
                Coupon.Type t = Coupon.Type.valueOf(typeF.getValue());
                double val    = Double.parseDouble(valueF.getText().trim());
                String expiry = expiryF.getText().trim();
                coupons.add(new Coupon(code, t, val, expiry));
                FileManager.saveCoupons(coupons);
                msg.setText("✅ Coupon " + code + " added (" + val
                    + (t == Coupon.Type.PERCENTAGE ? "% OFF" : " EGP OFF") + ")");
                showCoupons(content, msg, coupons);
            } catch (Exception ex) { msg.setText("❌ Invalid value."); }
        });
        form.add(addBtn, 1, 4);
        content.getChildren().add(form);
        content.getChildren().add(msg);

        // List existing coupons
        if (!coupons.isEmpty()) {
            content.getChildren().add(sectionTitle("Active Coupons"));
            for (Coupon c : coupons) {
                HBox row = cardRow(
                    "🏷️  " + c.getCode() + "  ·  " + c.label()
                    + "  ·  Expires: " + c.getExpiryDate()
                    + "  [" + (c.isActive() ? "ACTIVE" : "DISABLED") + "]"
                );
                Button toggleBtn = smallBtn(c.isActive() ? "Disable" : "Enable");
                toggleBtn.setOnAction(e -> {
                    c.setActive(!c.isActive());
                    FileManager.saveCoupons(coupons);
                    showCoupons(content, msg, coupons);
                });
                row.getChildren().add(toggleBtn);
                content.getChildren().add(row);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  FREEZE MEMBERSHIP TAB
    // ─────────────────────────────────────────────────────────────

    public static void showFreeze(VBox content, Label msg,
                                  ArrayList<FrozenMembership> freezes,
                                  ArrayList<User> users) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("❄️ Freeze Membership"));

        GridPane form = twoColGrid();
        ComboBox<String> memberPicker = new ComboBox<>();
        for (User u : users)
            if (u instanceof Member) memberPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
        TextField reasonF = field("Reason (injury, travel, etc.)");
        addRow(form, 0, "Member:", memberPicker);
        addRow(form, 1, "Reason:", reasonF);

        Button freezeBtn   = styledBtn("❄️ Freeze Now", "#00CFFF");
        Button unfreezeBtn = styledBtn("🔥 Unfreeze", "#FF6B00");

        freezeBtn.setOnAction(e -> {
            String sel = memberPicker.getValue();
            if (sel == null) { msg.setText("❌ Select a member."); return; }
            int id = extractId(sel);
            if (FileManager.isMemberFrozen(id, freezes)) {
                msg.setText("⚠️ Member is already frozen."); return;
            }
            FrozenMembership fm = new FrozenMembership(id, LocalDate.now().format(DF),
                reasonF.getText().trim().isEmpty() ? "No reason given" : reasonF.getText().trim());
            freezes.add(fm);
            FileManager.saveFrozen(freezes);
            // send WhatsApp
            for (User u : users)
                if (u.getId() == id) {
                    String phone = ((Member)u).getPhone() != null ? ((Member)u).getPhone() : "";
                    if (!phone.isEmpty())
                        WhatsAppService.send(phone,
                            WhatsAppService.msgFreezeConfirm(u.getName(), LocalDate.now().format(DF)));
                    break;
                }
            msg.setText("✅ Membership frozen from " + LocalDate.now().format(DF));
            showFreeze(content, msg, freezes, users);
        });

        unfreezeBtn.setOnAction(e -> {
            String sel = memberPicker.getValue();
            if (sel == null) { msg.setText("❌ Select a member."); return; }
            int id = extractId(sel);
            FrozenMembership fm = FileManager.getActiveFreeze(id, freezes);
            if (fm == null) { msg.setText("⚠️ Member is not frozen."); return; }
            long days = fm.unfreeze();
            FileManager.saveFrozen(freezes);
            // extend subscription
            for (User u : users) {
                if (u instanceof Member && u.getId() == id) {
                    Member m = (Member) u;
                    try {
                        LocalDate end = LocalDate.parse(m.getSubscriptionEndDate(), DF);
                        end = end.plusDays(days);
                        m.setSubscriptionEndDate(end.format(DF));
                        FileManager.saveUsers(users);
                    } catch (Exception ignored) {}
                    break;
                }
            }
            msg.setText("✅ Unfrozen. " + days + " days added to subscription.");
            showFreeze(content, msg, freezes, users);
        });

        HBox btnRow = new HBox(10, freezeBtn, unfreezeBtn);
        form.add(btnRow, 1, 2);
        content.getChildren().addAll(form, msg);

        // List current freezes
        List<FrozenMembership> active = freezes.stream().filter(FrozenMembership::isActive).collect(Collectors.toList());
        if (!active.isEmpty()) {
            content.getChildren().add(sectionTitle("Currently Frozen Members"));
            for (FrozenMembership fm : active) {
                String name = getUserName(fm.getMemberId(), users);
                content.getChildren().add(cardRow(
                    "❄️  " + name + "  ·  Frozen since: " + fm.getFrozenFrom()
                    + "  (" + fm.frozenDays() + " days)  ·  Reason: " + fm.getReason()
                ));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  FAMILY PACKAGE TAB
    // ─────────────────────────────────────────────────────────────

    public static void showFamilyPackages(VBox content, Label msg,
                                           ArrayList<FamilyPackage> packages,
                                           ArrayList<User> users) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("👨‍👩‍👧 Family Packages"));

        GridPane form = twoColGrid();
        TextField nameF    = field("e.g. Smith Family");
        ComboBox<String> primaryPicker = new ComboBox<>();
        TextField endF     = field("YYYY-MM-DD");
        endF.setText(LocalDate.now().plusYears(1).format(DF));
        TextField priceF   = field("Base price per person (EGP)");

        for (User u : users)
            if (u instanceof Member) primaryPicker.getItems().add(u.getName() + " [" + u.getId() + "]");

        addRow(form, 0, "Package Name:", nameF);
        addRow(form, 1, "Primary Member:", primaryPicker);
        addRow(form, 2, "End Date:", endF);
        addRow(form, 3, "Price/Person:", priceF);

        Button createBtn = styledBtn("➕ Create Package", "#FF6B00");
        createBtn.setOnAction(e -> {
            String sel = primaryPicker.getValue();
            if (sel == null) { msg.setText("❌ Select primary member."); return; }
            try {
                int nextId = packages.stream().mapToInt(FamilyPackage::getPackageId).max().orElse(0) + 1;
                FamilyPackage fp = new FamilyPackage(nextId, nameF.getText().trim(),
                    extractId(sel), LocalDate.now().format(DF), endF.getText().trim(),
                    Double.parseDouble(priceF.getText().trim()));
                packages.add(fp);
                FileManager.saveFamilyPackages(packages);
                msg.setText("✅ Family package created: " + nameF.getText().trim());
                showFamilyPackages(content, msg, packages, users);
            } catch (Exception ex) { msg.setText("❌ Invalid input."); }
        });
        form.add(createBtn, 1, 4);
        content.getChildren().addAll(form, msg);

        // List packages
        for (FamilyPackage fp : packages) {
            if (!fp.isActive()) continue;
            VBox card = new VBox(6);
            card.setPadding(new Insets(12));
            card.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8; "
                        + "-fx-border-color: #FF6B00; -fx-border-radius: 8;");
            Label title = new Label("👨‍👩‍👧  " + fp.getPackageName()
                + "  ·  " + fp.size() + " members  ·  "
                + String.format("Discount: %.0f%%  ·  Total: %.2f EGP", fp.discountPercent(), fp.totalPrice()));
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            // add member
            ComboBox<String> addPicker = new ComboBox<>();
            for (User u : users)
                if (u instanceof Member && !fp.getMemberIds().contains(u.getId()))
                    addPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
            Button addMbr = smallBtn("Add Member");
            addMbr.setOnAction(e2 -> {
                String s = addPicker.getValue();
                if (s == null) return;
                fp.addMember(extractId(s));
                FileManager.saveFamilyPackages(packages);
                showFamilyPackages(content, msg, packages, users);
            });
            HBox members = new HBox(8);
            for (int id : fp.getMemberIds()) {
                Label ml = new Label("• " + getUserName(id, users));
                ml.setStyle("-fx-text-fill: #aaa;");
                members.getChildren().add(ml);
            }
            card.getChildren().addAll(title, members, new HBox(6, addPicker, addMbr));
            content.getChildren().add(card);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  REFERRAL TAB
    // ─────────────────────────────────────────────────────────────

    public static void showReferrals(VBox content, Label msg,
                                     ArrayList<Referral> referrals,
                                     ArrayList<User> users) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("🎯 Referral System"));

        GridPane form = twoColGrid();
        ComboBox<String> referrerPicker = new ComboBox<>();
        ComboBox<String> referredPicker = new ComboBox<>();
        for (User u : users)
            if (u instanceof Member) {
                referrerPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
                referredPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
            }
        addRow(form, 0, "Referrer (existing member):", referrerPicker);
        addRow(form, 1, "Referred (new member):", referredPicker);

        Button addBtn = styledBtn("➕ Record Referral", "#FF6B00");
        addBtn.setOnAction(e -> {
            String s1 = referrerPicker.getValue(), s2 = referredPicker.getValue();
            if (s1 == null || s2 == null || s1.equals(s2)) {
                msg.setText("❌ Select two different members."); return;
            }
            int referId = extractId(s1), newId = extractId(s2);
            String referrerName = s1.split(" \\[")[0], referredName = s2.split(" \\[")[0];
            int nextId = referrals.stream().mapToInt(Referral::getReferralId).max().orElse(0) + 1;
            Referral ref = new Referral(nextId, referId, referrerName,
                newId, referredName, LocalDate.now().format(DF));
            ref.complete(); // mark completed right away
            // apply reward
            String newEnd = FileManager.applyReferralReward(ref, users);
            FileManager.saveUsers(users);
            referrals.add(ref);
            FileManager.saveReferrals(referrals);
            // WhatsApp notification
            for (User u : users)
                if (u instanceof Member && u.getId() == referId) {
                    String phone = ((Member)u).getPhone() != null ? ((Member)u).getPhone() : "";
                    if (!phone.isEmpty())
                        WhatsAppService.send(phone,
                            WhatsAppService.msgReferralReward(referrerName, newEnd));
                    break;
                }
            msg.setText("✅ Referral recorded. 1 free month added to " + referrerName
                      + ". New end: " + newEnd);
            showReferrals(content, msg, referrals, users);
        });
        form.add(addBtn, 1, 2);
        content.getChildren().addAll(form, msg);

        // List
        if (!referrals.isEmpty()) {
            content.getChildren().add(sectionTitle("Referral History"));
            for (Referral r : referrals) {
                content.getChildren().add(cardRow(
                    "🎯  " + r.getReferrerName() + " → " + r.getReferredName()
                    + "  ·  " + r.getReferralDate()
                    + "  [" + r.getStatus().name() + "]"
                    + (r.getRewardGiven().isEmpty() ? "" : "  ·  " + r.getRewardGiven())
                ));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SPLIT PAYMENT TAB
    // ─────────────────────────────────────────────────────────────

    public static void showSplitPayments(VBox content, Label msg,
                                          ArrayList<SplitPayment> splits,
                                          ArrayList<Bill> bills,
                                          ArrayList<User> users) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("💳 Split Payments"));

        GridPane form = twoColGrid();
        ComboBox<String> billPicker = new ComboBox<>();
        for (Bill b : bills)
            if (!b.isPaid()) billPicker.getItems().add(
                "Bill #" + b.getBillId() + " — " + b.getDescription()
                + " — " + b.getAmount() + " EGP [Member " + b.getMemberId() + "]");

        ComboBox<String> installF = new ComboBox<>();
        installF.getItems().addAll("2", "3", "4", "6", "12");
        installF.setValue("3");
        TextField startF = field("First due date YYYY-MM-DD");
        startF.setText(LocalDate.now().format(DF));

        addRow(form, 0, "Unpaid Bill:", billPicker);
        addRow(form, 1, "Installments:", installF);
        addRow(form, 2, "First Due Date:", startF);

        Button splitBtn = styledBtn("💳 Create Split", "#FF6B00");
        splitBtn.setOnAction(e -> {
            String sel = billPicker.getValue();
            if (sel == null) { msg.setText("❌ Select a bill."); return; }
            try {
                int billId = Integer.parseInt(sel.split("#")[1].split(" ")[0]);
                Bill b = bills.stream().filter(x -> x.getBillId() == billId).findFirst().orElse(null);
                if (b == null) { msg.setText("❌ Bill not found."); return; }
                int n = Integer.parseInt(installF.getValue());
                int nextId = splits.stream().mapToInt(SplitPayment::getSplitId).max().orElse(0) + 1;
                SplitPayment sp = new SplitPayment(nextId, billId, b.getMemberId(),
                    b.getAmount(), n, startF.getText().trim());
                splits.add(sp);
                FileManager.saveSplitPayments(splits);
                msg.setText("✅ Split payment created: " + n + " installments of "
                    + String.format("%.2f", b.getAmount() / n) + " EGP");
                showSplitPayments(content, msg, splits, bills, users);
            } catch (Exception ex) { msg.setText("❌ Error: " + ex.getMessage()); }
        });
        form.add(splitBtn, 1, 3);
        content.getChildren().addAll(form, msg);

        // List splits
        if (!splits.isEmpty()) {
            content.getChildren().add(sectionTitle("Active Split Plans"));
            for (SplitPayment sp : splits) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8; "
                            + "-fx-border-color: #555; -fx-border-radius: 8;");
                String name = getUserName(sp.getMemberId(), users);
                Label hdr = new Label("💳  " + name + "  ·  Bill #" + sp.getBillId()
                    + "  ·  Total: " + sp.getTotalAmount() + " EGP"
                    + "  ·  Paid: " + String.format("%.2f", sp.paidSoFar())
                    + "  ·  Remaining: " + String.format("%.2f", sp.remaining()));
                hdr.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                card.getChildren().add(hdr);
                for (SplitPayment.Installment inst : sp.getInstallments()) {
                    HBox row = new HBox(10);
                    Label l = new Label((inst.isPaid() ? "✅" : "⏳") + "  #" + inst.getNumber()
                        + "  " + String.format("%.2f EGP", inst.getAmount())
                        + "  Due: " + inst.getDueDate()
                        + (inst.isPaid() ? "  (Paid " + inst.getPaidDate() + ")" : ""));
                    l.setStyle("-fx-text-fill: " + (inst.isPaid() ? "#00E676" : "#aaa") + ";");
                    row.getChildren().add(l);
                    if (!inst.isPaid()) {
                        Button payBtn = smallBtn("Mark Paid");
                        payBtn.setOnAction(e2 -> {
                            inst.markPaid(LocalDate.now().format(DF));
                            if (sp.isFullyPaid()) {
                                // mark original bill paid
                                bills.stream().filter(b -> b.getBillId() == sp.getBillId())
                                     .findFirst().ifPresent(b -> b.setPaid(true));
                                FileManager.saveBills(bills);
                                msg.setText("✅ All installments paid! Bill marked as paid.");
                            } else {
                                msg.setText("✅ Installment #" + inst.getNumber() + " marked as paid.");
                            }
                            FileManager.saveSplitPayments(splits);
                            showSplitPayments(content, msg, splits, bills, users);
                        });
                        row.getChildren().add(payBtn);
                    }
                    card.getChildren().add(row);
                }
                content.getChildren().add(card);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DIGITAL CONTRACT TAB
    // ─────────────────────────────────────────────────────────────

    public static void showContracts(VBox content, Label msg,
                                     ArrayList<DigitalContract> contracts,
                                     ArrayList<User> users,
                                     ArrayList<Bill> bills) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("📋 Digital Contracts"));

        GridPane form = twoColGrid();
        ComboBox<String> memberPicker = new ComboBox<>();
        for (User u : users)
            if (u instanceof Member) memberPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
        ComboBox<String> planF = new ComboBox<>();
        planF.getItems().addAll("Monthly", "Quarterly (3 months)", "Semi-Annual (6 months)", "Annual");
        planF.setValue("Monthly");
        TextField feeF = field("Monthly fee (EGP)");
        TextField endF = field("End date YYYY-MM-DD");
        endF.setText(LocalDate.now().plusMonths(1).format(DF));
        TextArea termsArea = new TextArea(defaultTerms());
        termsArea.setPrefRowCount(4);
        termsArea.setWrapText(true);

        addRow(form, 0, "Member:", memberPicker);
        addRow(form, 1, "Plan:", planF);
        addRow(form, 2, "Monthly Fee:", feeF);
        addRow(form, 3, "End Date:", endF);
        form.add(new Label("Terms:"), 0, 4);
        form.add(termsArea, 1, 4);

        Button generateBtn = styledBtn("📄 Generate Contract", "#FF6B00");
        generateBtn.setOnAction(e -> {
            String sel = memberPicker.getValue();
            if (sel == null) { msg.setText("❌ Select a member."); return; }
            try {
                int id = extractId(sel), nextId =
                    contracts.stream().mapToInt(DigitalContract::getContractId).max().orElse(0) + 1;
                String memberName = sel.split(" \\[")[0];
                DigitalContract dc = new DigitalContract(nextId, id, memberName,
                    LocalDate.now().format(DF), endF.getText().trim(),
                    Double.parseDouble(feeF.getText().trim()),
                    planF.getValue(), termsArea.getText().trim());
                // generate PDF
                String pdfPath = ContractPdfGenerator.generate(dc);
                dc.setPdfPath(pdfPath);
                contracts.add(dc);
                FileManager.saveContracts(contracts);
                msg.setText("✅ Contract generated: " + dc.getSignatureCode()
                          + "\nPDF: " + pdfPath);
                showContracts(content, msg, contracts, users, bills);
            } catch (Exception ex) { msg.setText("❌ Error: " + ex.getMessage()); }
        });
        form.add(generateBtn, 1, 5);
        content.getChildren().addAll(form, msg);

        // List contracts
        if (!contracts.isEmpty()) {
            content.getChildren().add(sectionTitle("All Contracts"));
            for (DigitalContract dc : contracts) {
                HBox row = cardRow("📋  " + dc.getMemberName()
                    + "  ·  " + dc.getSignatureCode()
                    + "  ·  " + dc.getPlanType()
                    + "  ·  " + dc.getStatus().name()
                    + (dc.getStatus() == DigitalContract.Status.SIGNED ? "  ✅ " + dc.getSignedDate() : ""));
                if (dc.getStatus() == DigitalContract.Status.PENDING) {
                    Button signBtn = smallBtn("Mark Signed");
                    signBtn.setOnAction(e2 -> {
                        dc.sign(LocalDate.now().format(DF));
                        FileManager.saveContracts(contracts);
                        // WhatsApp
                        for (User u : users)
                            if (u instanceof Member && u.getId() == dc.getMemberId()) {
                                String phone = ((Member)u).getPhone() != null ? ((Member)u).getPhone() : "";
                                if (!phone.isEmpty())
                                    WhatsAppService.send(phone,
                                        WhatsAppService.msgContractSigned(dc.getMemberName(), dc.getSignatureCode()));
                                break;
                            }
                        msg.setText("✅ Contract signed: " + dc.getSignatureCode());
                        showContracts(content, msg, contracts, users, bills);
                    });
                    row.getChildren().add(signBtn);
                }
                content.getChildren().add(row);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  WHATSAPP SETTINGS TAB
    // ─────────────────────────────────────────────────────────────

    public static void showWhatsAppSettings(VBox content, Label msg) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("📱 WhatsApp Integration (Twilio)"));

        Label info = new Label(
            "To enable WhatsApp notifications:\n"
          + "1. Create a Twilio account at twilio.com\n"
          + "2. Activate the WhatsApp Sandbox\n"
          + "3. Enter your credentials below\n"
          + "4. Members must save the Twilio number in their contacts"
        );
        info.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13;");
        info.setWrapText(true);
        content.getChildren().add(info);

        GridPane form = twoColGrid();
        TextField sidF   = field("Twilio Account SID");
        TextField tokenF = field("Twilio Auth Token");
        TextField fromF  = field("+14155238886");
        addRow(form, 0, "Account SID:", sidF);
        addRow(form, 1, "Auth Token:", tokenF);
        addRow(form, 2, "From Number:", fromF);

        Button saveBtn = styledBtn("💾 Save Credentials", "#FF6B00");
        Button testBtn = styledBtn("📤 Send Test Message", "#555");
        TextField testPhone = field("Test phone +20XXXXXXXXX");

        saveBtn.setOnAction(e -> {
            WhatsAppService.setCredentials(
                sidF.getText().trim(), tokenF.getText().trim(), fromF.getText().trim());
            msg.setText("✅ Credentials saved for this session.");
        });
        testBtn.setOnAction(e -> {
            boolean ok = WhatsAppService.send(testPhone.getText().trim(),
                "🏋️ POWER GYM — Test message. WhatsApp integration is working! 💪");
            msg.setText(ok ? "✅ Test message sent!" : "❌ Failed to send. Check credentials.");
        });

        form.add(new HBox(8, saveBtn, testBtn), 1, 3);
        form.add(new Label("Test Phone:"), 0, 4);
        form.add(testPhone, 1, 4);
        content.getChildren().addAll(form, msg);

        // notification toggles info
        Label notifInfo = sectionTitle("Automatic Notifications");
        Label notifDesc = new Label(
            "The following are sent automatically:\n"
          + "• ⚠️  Expiry warning — 3 days before subscription ends\n"
          + "• ✅  Check-in confirmation — on attendance scan\n"
          + "• 🎉  Workout complete — when all exercises checked\n"
          + "• 💳  Bill reminder — on bill creation\n"
          + "• 🎁  Referral reward — when referral is processed\n"
          + "• ❄️  Freeze confirmation — on membership freeze\n"
          + "• 📋  Contract signed — on digital signature"
        );
        notifDesc.setStyle("-fx-text-fill: #ccc; -fx-font-size: 12;");
        notifDesc.setWrapText(true);
        content.getChildren().addAll(notifInfo, notifDesc);
    }

    // ─────────────────────────────────────────────────────────────
    //  BODY PROGRESS — Admin adds measurements
    // ─────────────────────────────────────────────────────────────

    public static void showBodyProgressAdmin(VBox content, Label msg,
                                              ArrayList<BodyProgress> progress,
                                              ArrayList<User> users) {
        content.getChildren().clear();
        content.getChildren().add(sectionTitle("⚖️ Body Progress Tracking"));

        GridPane form = twoColGrid();
        ComboBox<String> memberPicker = new ComboBox<>();
        for (User u : users)
            if (u instanceof Member) memberPicker.getItems().add(u.getName() + " [" + u.getId() + "]");
        TextField dateF   = field("YYYY-MM-DD");
        dateF.setText(LocalDate.now().format(DF));
        TextField weightF = field("Weight in kg, e.g. 85.5");
        TextField fatF    = field("Body fat %, e.g. 22.3");
        TextField notesF  = field("Optional notes");

        addRow(form, 0, "Member:", memberPicker);
        addRow(form, 1, "Date:", dateF);
        addRow(form, 2, "Weight (kg):", weightF);
        addRow(form, 3, "Body Fat %:", fatF);
        addRow(form, 4, "Notes:", notesF);

        Button saveBtn = styledBtn("💾 Save Measurement", "#FF6B00");
        saveBtn.setOnAction(e -> {
            String sel = memberPicker.getValue();
            if (sel == null) { msg.setText("❌ Select a member."); return; }
            try {
                int id = extractId(sel);
                String memberName = sel.split(" \\[")[0];
                int nextId = progress.stream().mapToInt(BodyProgress::getProgressId).max().orElse(0) + 1;
                BodyProgress bp = new BodyProgress(nextId, id, memberName,
                    dateF.getText().trim(),
                    Double.parseDouble(weightF.getText().trim()),
                    Double.parseDouble(fatF.getText().trim()),
                    notesF.getText().trim());
                progress.add(bp);
                FileManager.saveBodyProgress(progress);
                msg.setText("✅ Measurement saved for " + memberName);
                showBodyProgressAdmin(content, msg, progress, users);
            } catch (Exception ex) { msg.setText("❌ Invalid input."); }
        });
        form.add(saveBtn, 1, 5);
        content.getChildren().addAll(form, msg);
    }

    // ─────────────────────────────────────────────────────────────
    //  SHARED HELPERS
    // ─────────────────────────────────────────────────────────────

    private static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        l.setStyle("-fx-text-fill: #FF6B00;");
        return l;
    }

    private static GridPane twoColGrid() {
        GridPane g = new GridPane();
        g.setHgap(12); g.setVgap(10);
        g.setPadding(new Insets(12));
        ColumnConstraints c1 = new ColumnConstraints(160);
        ColumnConstraints c2 = new ColumnConstraints(300);
        g.getColumnConstraints().addAll(c1, c2);
        return g;
    }

    private static void addRow(GridPane g, int row, String label, javafx.scene.Node field) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #ccc;");
        g.add(l, 0, row);
        g.add(field, 1, row);
    }

    private static TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: white; "
                  + "-fx-border-color: #444; -fx-border-radius: 4; -fx-background-radius: 4;");
        return tf;
    }

    private static Button styledBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                 + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        return b;
    }

    private static Button smallBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #333; -fx-text-fill: #ccc; "
                 + "-fx-font-size: 11; -fx-background-radius: 4; -fx-cursor: hand;");
        return b;
    }

    private static HBox cardRow(String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 6;");
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #ddd; -fx-font-size: 12;");
        l.setWrapText(true);
        row.getChildren().add(l);
        HBox.setHgrow(l, Priority.ALWAYS);
        return row;
    }

    private static int extractId(String s) {
        return Integer.parseInt(s.replaceAll(".*\\[(\\d+)\\]", "$1"));
    }

    private static String getUserName(int id, ArrayList<User> users) {
        for (User u : users) if (u.getId() == id) return u.getName();
        return "Unknown [" + id + "]";
    }

    private static String defaultTerms() {
        return "1. Member agrees to follow all gym rules and regulations.\n"
             + "2. Monthly fee is due on the 1st of each month.\n"
             + "3. Membership can be frozen once per year for up to 30 days.\n"
             + "4. Power Gym is not liable for personal injury or loss of property.\n"
             + "5. Contract is valid for the duration stated above.";
    }
}
