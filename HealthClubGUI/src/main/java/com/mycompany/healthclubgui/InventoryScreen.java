package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.time.LocalDate;
import java.util.ArrayList;

/** Inventory management panel — used inside AdminScreen */
public class InventoryScreen {

    private final ArrayList<InventoryItem> items;

    public InventoryScreen() {
        this.items = FileManager.loadInventory();
    }

    public VBox buildPanel(Label msgLabel) {
        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle(LoginScreen.contentBg());

        // ── Add Item ──
        VBox addSection = LoginScreen.makeSectionBox("  Add / Update Item");
        TextField idF   = LoginScreen.makeTextField("Item ID");
        TextField nameF = LoginScreen.makeTextField("Item Name");
        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Equipment","Supplement","Accessory","Other");
        catBox.setValue("Equipment"); catBox.setPrefHeight(40); catBox.setPrefWidth(250);
        TextField qtyF  = LoginScreen.makeTextField("Quantity");
        TextField minF  = LoginScreen.makeTextField("Min Quantity (alert threshold)");
        TextField priceF= LoginScreen.makeTextField("Price per unit ($)");

        Button addBtn = makeBtn("  ADD / UPDATE", "#27AE60");
        addBtn.setOnAction(e -> {
            try {
                int    id    = Integer.parseInt(idF.getText().trim());
                String name  = nameF.getText().trim();
                String cat   = catBox.getValue();
                int    qty   = Integer.parseInt(qtyF.getText().trim());
                int    min   = Integer.parseInt(minF.getText().trim());
                double price = Double.parseDouble(priceF.getText().trim());
                if (name.isEmpty()) { showMsg(msgLabel,"Name required!",true); return; }

                // update if exists
                InventoryItem existing = findById(id);
                if (existing != null) {
                    existing.setName(name); existing.setCategory(cat);
                    existing.setQuantity(qty); existing.setMinQuantity(min);
                    existing.setPrice(price);
                    existing.setLastUpdated(LocalDate.now().toString());
                    showMsg(msgLabel,"Item updated!",false);
                } else {
                    items.add(new InventoryItem(id,name,cat,qty,min,price,LocalDate.now().toString()));
                    showMsg(msgLabel,"Item added!",false);
                }
                FileManager.saveInventory(items);
                idF.clear(); nameF.clear(); qtyF.clear(); minF.clear(); priceF.clear();
            } catch (Exception ex) { showMsg(msgLabel,"Invalid input!",true); }
        });

        addSection.getChildren().addAll(
            LoginScreen.makeLabel("ITEM ID"), idF,
            LoginScreen.makeLabel("NAME"), nameF,
            LoginScreen.makeLabel("CATEGORY"), catBox,
            LoginScreen.makeLabel("QUANTITY"), qtyF,
            LoginScreen.makeLabel("MIN QUANTITY"), minF,
            LoginScreen.makeLabel("PRICE ($)"), priceF,
            addBtn
        );

        // ── Delete ──
        VBox delSection = LoginScreen.makeSectionBox("  Remove Item");
        TextField delIdF = LoginScreen.makeTextField("Item ID to remove");
        Button delBtn = makeBtn("  REMOVE", "#E74C3C");
        delBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(delIdF.getText().trim());
                InventoryItem it = findById(id);
                if (it == null) { showMsg(msgLabel,"Item not found!",true); return; }
                items.remove(it);
                FileManager.saveInventory(items);
                showMsg(msgLabel,"Item removed!",false);
                delIdF.clear();
            } catch (Exception ex) { showMsg(msgLabel,"Invalid ID!",true); }
        });
        delSection.getChildren().addAll(LoginScreen.makeLabel("ITEM ID"), delIdF, delBtn);

        // ── List ──
        VBox listSection = LoginScreen.makeSectionBox("  Inventory List");
        TextArea listArea = makeTextArea(240);
        Button listBtn  = makeBtn("  LIST ALL",  "#3498DB");
        Button alertBtn = makeBtn("  LOW STOCK ALERTS", "#E67E22");

        listBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder(
                String.format("%-4s | %-20s | %-12s | %-6s | %-5s | %-8s | %s%n",
                    "ID","Name","Category","Qty","Min","Price","Updated"));
            sb.append("─".repeat(80)).append("\n");
            for (InventoryItem it : items) sb.append(it).append("\n");
            listArea.setText(sb.toString());
        });

        alertBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder("⚠️  LOW STOCK ITEMS:\n" + "─".repeat(50) + "\n");
            boolean found = false;
            for (InventoryItem it : items) {
                if (it.isLowStock()) {
                    sb.append(String.format("%-20s | Qty: %d | Min: %d%n",
                        it.getName(), it.getQuantity(), it.getMinQuantity()));
                    found = true;
                }
            }
            if (!found) sb.append("All items are sufficiently stocked.");
            listArea.setText(sb.toString());
        });

        listSection.getChildren().addAll(new HBox(10,listBtn,alertBtn), listArea);

        root.getChildren().addAll(addSection, delSection, listSection, msgLabel);
        return root;
    }

    private InventoryItem findById(int id) {
        return items.stream().filter(i -> i.getItemId()==id).findFirst().orElse(null);
    }

    private void showMsg(Label l, String t, boolean err) {
        l.setText(t); l.setTextFill(err ? Color.RED : Color.LIMEGREEN);
    }

    private Button makeBtn(String t, String c) {
        Button b = new Button(t);
        b.setPrefHeight(38); b.setPrefWidth(180);
        b.setStyle("-fx-background-color:"+c+";-fx-text-fill:white;"
                 + "-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");
        return b;
    }

    private TextArea makeTextArea(int h) {
        TextArea ta = new TextArea(); ta.setEditable(false); ta.setPrefHeight(h);
        ta.setFont(Font.font("Monospaced",11));
        ta.setStyle("-fx-control-inner-background:#1a1a1a;-fx-text-fill:#e0e0e0;");
        return ta;
    }
}
