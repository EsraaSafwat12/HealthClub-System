// ChartsPanel.java
package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChartsPanel — pure-JavaFX Canvas charts (no external lib needed).
 * Provides:
 *   1. bodyProgressChart()  — member weight & fat % over months
 *   2. attendanceChart()    — check-ins per month (member or coach view)
 *   3. revenueChart()       — monthly or annual income from bills
 */
public class ChartsPanel {

    private static final Color BG        = Color.web("#1a1a2e");
    private static final Color GRID      = Color.web("#2a2a4a");
    private static final Color AXIS      = Color.web("#888888");
    private static final Color WEIGHT    = Color.web("#FF6B00");
    private static final Color FAT       = Color.web("#00CFFF");
    private static final Color ATTEND    = Color.web("#00E676");
    private static final Color REVENUE   = Color.web("#FFD700");
    private static final Color TEXT_CLR  = Color.web("#EEEEEE");

    // ─────────────────────────────────────────────────────────────
    //  1. BODY PROGRESS CHART
    // ─────────────────────────────────────────────────────────────

    /**
     * Creates a weight + body fat % line chart for a specific member.
     * @param progressList  all BodyProgress records for this member (sorted by date)
     * @param memberName    displayed in chart title
     */
    public static VBox bodyProgressChart(List<BodyProgress> progressList, String memberName) {
        VBox box = makeContainer("📊 Body Progress — " + memberName);

        if (progressList.isEmpty()) {
            box.getChildren().add(noDataLabel("No progress records yet.\nAsk your coach to add measurements."));
            return box;
        }

        int w = 560, h = 280;
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // background
        gc.setFill(BG); gc.fillRect(0, 0, w, h);

        int pad = 55, topPad = 20, rightPad = 20;
        int chartW = w - pad - rightPad, chartH = h - pad - topPad;

        // data
        List<String> labels = progressList.stream().map(BodyProgress::monthLabel).collect(Collectors.toList());
        double[] weights    = progressList.stream().mapToDouble(BodyProgress::getWeightKg).toArray();
        double[] fats       = progressList.stream().mapToDouble(BodyProgress::getBodyFatPct).toArray();

        double wMin = Arrays.stream(weights).min().orElse(0) * 0.95;
        double wMax = Arrays.stream(weights).max().orElse(100) * 1.05;
        double fMin = Arrays.stream(fats).min().orElse(0) * 0.95;
        double fMax = Arrays.stream(fats).max().orElse(40) * 1.05;

        drawGrid(gc, pad, topPad, chartW, chartH);

        int n = labels.size();
        double xStep = (double) chartW / Math.max(n - 1, 1);

        // draw weight line (orange)
        drawLine(gc, weights, wMin, wMax, pad, topPad, chartW, chartH, xStep, n, WEIGHT);
        // draw fat % line (cyan)
        drawLine(gc, fats, fMin, fMax, pad, topPad, chartW, chartH, xStep, n, FAT);

        // X axis labels
        gc.setFill(AXIS); gc.setFont(Font.font("Arial", 9));
        for (int i = 0; i < n; i++) {
            double x = pad + i * xStep;
            gc.fillText(labels.get(i).substring(5), x - 10, h - 8);
        }

        // Y axis labels (weight on left)
        gc.setFill(WEIGHT); gc.setFont(Font.font("Arial", 9));
        for (int i = 0; i <= 4; i++) {
            double val = wMin + (wMax - wMin) * i / 4;
            double y   = topPad + chartH - chartH * i / 4;
            gc.fillText(String.format("%.0f", val), 2, y + 4);
        }

        // Y axis labels (fat % on right)
        gc.setFill(FAT);
        for (int i = 0; i <= 4; i++) {
            double val = fMin + (fMax - fMin) * i / 4;
            double y   = topPad + chartH - chartH * i / 4;
            gc.fillText(String.format("%.1f%%", val), w - rightPad - 2, y + 4);
        }

        box.getChildren().add(canvas);
        box.getChildren().add(legend(new String[]{"⬤ Weight (kg)", "⬤ Body Fat %"},
                                     new Color[]{WEIGHT, FAT}));
        return box;
    }

    // ─────────────────────────────────────────────────────────────
    //  2. ATTENDANCE CHART (bar chart)
    // ─────────────────────────────────────────────────────────────

    /**
     * Bar chart of monthly attendance check-ins.
     * @param attendance  all Attendance records
     * @param memberId    if > 0, filter for one member; if 0, show totals (admin/coach view)
     * @param title       chart title
     */
    public static VBox attendanceChart(List<Attendance> attendance, int memberId, String title) {
        VBox box = makeContainer("📅 " + title);

        // group by YYYY-MM
        Map<String, Long> monthly = attendance.stream()
            .filter(a -> memberId <= 0 || a.getUserId() == memberId)
            .collect(Collectors.groupingBy(a -> a.getCheckIn().substring(0, Math.min(10, a.getCheckIn().length())).substring(0, 7), Collectors.counting()));

        if (monthly.isEmpty()) {
            box.getChildren().add(noDataLabel("No attendance records found."));
            return box;
        }

        List<String> months = new ArrayList<>(monthly.keySet());
        Collections.sort(months);
        // last 12 months max
        if (months.size() > 12) months = months.subList(months.size() - 12, months.size());

        int w = 560, h = 280;
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(BG); gc.fillRect(0, 0, w, h);

        int pad = 45, topPad = 20, rightPad = 20;
        int chartW = w - pad - rightPad, chartH = h - pad - topPad;

        long maxVal = months.stream().mapToLong(m -> monthly.getOrDefault(m, 0L)).max().orElse(1);

        drawGrid(gc, pad, topPad, chartW, chartH);

        double barW = (double) chartW / months.size() * 0.6;
        double gap  = (double) chartW / months.size();

        for (int i = 0; i < months.size(); i++) {
            long val   = monthly.getOrDefault(months.get(i), 0L);
            double barH = (double) val / maxVal * chartH;
            double x    = pad + i * gap + gap * 0.2;
            double y    = topPad + chartH - barH;

            // gradient bar
            gc.setFill(ATTEND.deriveColor(0, 1, 0.6, 1));
            gc.fillRect(x, y + barH * 0.5, barW, barH * 0.5);
            gc.setFill(ATTEND);
            gc.fillRect(x, y, barW, barH * 0.5);

            // label
            gc.setFill(TEXT_CLR); gc.setFont(Font.font("Arial", 9));
            gc.fillText(months.get(i).substring(5), x, h - 8);
            gc.fillText(String.valueOf(val), x + barW * 0.1, y - 3);
        }

        // Y axis
        gc.setFill(AXIS); gc.setFont(Font.font("Arial", 9));
        for (int i = 0; i <= 4; i++) {
            long val = maxVal * i / 4;
            double y = topPad + chartH - chartH * i / 4;
            gc.fillText(String.valueOf(val), 2, y + 4);
        }

        box.getChildren().add(canvas);
        return box;
    }

    // ─────────────────────────────────────────────────────────────
    //  3. REVENUE CHART
    // ─────────────────────────────────────────────────────────────

    /**
     * Line chart of monthly revenue from paid bills.
     * @param bills     all bills
     * @param monthly   true = group by month; false = group by year
     */
    public static VBox revenueChart(List<Bill> bills, boolean monthly) {
        VBox box = makeContainer(monthly ? "💰 Monthly Revenue" : "💰 Annual Revenue");

        Map<String, Double> grouped = bills.stream()
            .filter(Bill::isPaid)
            .collect(Collectors.groupingBy(
                b -> monthly ? b.getDate().substring(0, 7) : b.getDate().substring(0, 4),
                Collectors.summingDouble(Bill::getAmount)
            ));

        if (grouped.isEmpty()) {
            box.getChildren().add(noDataLabel("No paid bills yet."));
            return box;
        }

        List<String> periods = new ArrayList<>(grouped.keySet());
        Collections.sort(periods);
        if (monthly && periods.size() > 12)
            periods = periods.subList(periods.size() - 12, periods.size());

        int w = 560, h = 280;
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(BG); gc.fillRect(0, 0, w, h);

        int pad = 65, topPad = 20, rightPad = 20;
        int chartW = w - pad - rightPad, chartH = h - pad - topPad;

        double[] vals = periods.stream().mapToDouble(p -> grouped.getOrDefault(p, 0.0)).toArray();
        double vMin = 0, vMax = Arrays.stream(vals).max().orElse(1) * 1.1;

        drawGrid(gc, pad, topPad, chartW, chartH);

        int n = periods.size();
        double xStep = (double) chartW / Math.max(n - 1, 1);

        // filled area
        gc.setFill(REVENUE.deriveColor(0, 1, 1, 0.2));
        gc.beginPath();
        gc.moveTo(pad, topPad + chartH);
        for (int i = 0; i < n; i++) {
            double x = pad + i * xStep;
            double y = topPad + chartH - (vals[i] - vMin) / (vMax - vMin) * chartH;
            gc.lineTo(x, y);
        }
        gc.lineTo(pad + (n - 1) * xStep, topPad + chartH);
        gc.closePath();
        gc.fill();

        // line
        gc.setStroke(REVENUE); gc.setLineWidth(2.5);
        gc.beginPath();
        for (int i = 0; i < n; i++) {
            double x = pad + i * xStep;
            double y = topPad + chartH - (vals[i] - vMin) / (vMax - vMin) * chartH;
            if (i == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.stroke();

        // dots + values
        for (int i = 0; i < n; i++) {
            double x = pad + i * xStep;
            double y = topPad + chartH - (vals[i] - vMin) / (vMax - vMin) * chartH;
            gc.setFill(REVENUE); gc.fillOval(x - 4, y - 4, 8, 8);
            gc.setFill(TEXT_CLR); gc.setFont(Font.font("Arial", 8));
            gc.fillText(String.format("%.0f", vals[i]), x - 15, y - 8);
            gc.setFill(AXIS);
            String lbl = monthly ? periods.get(i).substring(5) : periods.get(i);
            gc.fillText(lbl, x - 10, h - 8);
        }

        // Y axis
        gc.setFill(AXIS); gc.setFont(Font.font("Arial", 9));
        for (int i = 0; i <= 4; i++) {
            double val = vMax * i / 4;
            double y   = topPad + chartH - chartH * i / 4;
            gc.fillText(String.format("%.0f", val), 2, y + 4);
        }

        box.getChildren().add(canvas);
        box.getChildren().add(legend(new String[]{"⬤ Revenue (EGP)"}, new Color[]{REVENUE}));
        return box;
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────

    private static void drawGrid(GraphicsContext gc, int pad, int topPad, int chartW, int chartH) {
        gc.setStroke(GRID); gc.setLineWidth(0.5);
        for (int i = 0; i <= 4; i++) {
            double y = topPad + chartH * i / 4;
            gc.strokeLine(pad, y, pad + chartW, y);
        }
        gc.setStroke(AXIS); gc.setLineWidth(1);
        gc.strokeLine(pad, topPad, pad, topPad + chartH);
        gc.strokeLine(pad, topPad + chartH, pad + chartW, topPad + chartH);
    }

    private static void drawLine(GraphicsContext gc, double[] vals,
                                  double min, double max,
                                  int pad, int topPad, int chartW, int chartH,
                                  double xStep, int n, Color color) {
        gc.setStroke(color); gc.setLineWidth(2);
        gc.beginPath();
        for (int i = 0; i < n; i++) {
            double x = pad + i * xStep;
            double y = topPad + chartH - (vals[i] - min) / (max - min) * chartH;
            if (i == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.stroke();
        gc.setFill(color);
        for (int i = 0; i < n; i++) {
            double x = pad + i * xStep;
            double y = topPad + chartH - (vals[i] - min) / (max - min) * chartH;
            gc.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    private static VBox makeContainer(String title) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: #12122a; -fx-background-radius: 12; "
                   + "-fx-border-color: #2a2a5a; -fx-border-radius: 12;");
        Label ttl = new Label(title);
        ttl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        ttl.setStyle("-fx-text-fill: #FF6B00;");
        box.getChildren().add(ttl);
        return box;
    }

    private static Label noDataLabel(String msg) {
        Label lbl = new Label(msg);
        lbl.setStyle("-fx-text-fill: #888; -fx-font-size: 13;");
        lbl.setTextAlignment(TextAlignment.CENTER);
        return lbl;
    }

    private static HBox legend(String[] labels, Color[] colors) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < labels.length; i++) {
            Label l = new Label(labels[i]);
            final Color c = colors[i];
            l.setStyle(String.format("-fx-text-fill: #%02x%02x%02x; -fx-font-size: 12;",
                (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)));
            row.getChildren().add(l);
        }
        return row;
    }
}
