// ContractPdfGenerator.java
package com.mycompany.healthclubgui;

import com.mycompany.healthclubsystem.DigitalContract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;

/**
 * ContractPdfGenerator — generates a PDF membership contract using PDFBox.
 * PDFBox is already in the project's pom.xml.
 *
 * Output: contract_<signatureCode>.pdf in the working directory.
 */
public class ContractPdfGenerator {

    /**
     * Generate a PDF for the given contract.
     * @return path to the generated PDF file, or empty string on error
     */
    public static String generate(DigitalContract dc) {
        String fileName = "contract_" + dc.getSignatureCode() + ".pdf";
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50, y = PDRectangle.A4.getHeight() - margin;
            float lineH  = 18, titleH = 26, sectionH = 22;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ── Header band ─────────────────────────────────────────
                cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                cs.addRect(0, PDRectangle.A4.getHeight() - 80,
                           PDRectangle.A4.getWidth(), 80);
                cs.fill();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 22);
                cs.setNonStrokingColor(1f, 0.42f, 0f); // orange
                cs.newLineAtOffset(margin, y - 10);
                cs.showText("POWER GYM — MEMBERSHIP CONTRACT");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.setNonStrokingColor(0.7f, 0.7f, 0.7f);
                cs.newLineAtOffset(margin, y - 30);
                cs.showText("Transform Your Body. Transform Your Life.");
                cs.endText();

                y -= 90;
                cs.setNonStrokingColor(0f, 0f, 0f);

                // ── Contract Info ────────────────────────────────────────
                y = drawLine(cs, "Contract ID", dc.getSignatureCode(), margin, y, lineH);
                y = drawLine(cs, "Member Name", dc.getMemberName(), margin, y, lineH);
                y = drawLine(cs, "Member ID",   String.valueOf(dc.getMemberId()), margin, y, lineH);
                y = drawLine(cs, "Plan Type",   dc.getPlanType(), margin, y, lineH);
                y = drawLine(cs, "Start Date",  dc.getStartDate(), margin, y, lineH);
                y = drawLine(cs, "End Date",    dc.getEndDate(), margin, y, lineH);
                y = drawLine(cs, "Monthly Fee", String.format("%.2f EGP", dc.getMonthlyFee()), margin, y, lineH);
                y -= sectionH;

                // ── Terms ────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
                cs.setNonStrokingColor(1f, 0.42f, 0f);
                cs.newLineAtOffset(margin, y);
                cs.showText("TERMS & CONDITIONS");
                cs.endText();
                y -= sectionH;

                cs.setNonStrokingColor(0f, 0f, 0f);
                for (String term : dc.getTerms().split("\n")) {
                    if (y < 100) break; // don't overflow page
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.newLineAtOffset(margin, y);
                    cs.showText(term.length() > 90 ? term.substring(0, 87) + "..." : term);
                    cs.endText();
                    y -= lineH;
                }
                y -= sectionH;

                // ── Signatures ───────────────────────────────────────────
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
                cs.setNonStrokingColor(1f, 0.42f, 0f);
                cs.newLineAtOffset(margin, y);
                cs.showText("SIGNATURES");
                cs.endText();
                y -= sectionH;

                cs.setNonStrokingColor(0f, 0f, 0f);
                y = drawLine(cs, "Member Signature", "_________________________", margin, y, lineH);
                y = drawLine(cs, "Date",
                    dc.getStatus() == DigitalContract.Status.SIGNED ? dc.getSignedDate() : "________________",
                    margin, y, lineH);
                y = drawLine(cs, "Admin Signature",  "_________________________", margin, y, lineH);
                y = drawLine(cs, "Status",           dc.getStatus().name(), margin, y, lineH);

                // ── Footer ───────────────────────────────────────────────
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                cs.newLineAtOffset(margin, 30);
                cs.showText("Power Gym  ·  " + dc.getSignatureCode()
                          + "  ·  Generated by Power Gym Management System");
                cs.endText();
            }

            doc.save(new File(fileName));
            System.out.println("[PDF] Contract saved: " + fileName);
            return new File(fileName).getAbsolutePath();

        } catch (IOException e) {
            System.out.println("[PDF ERROR] " + e.getMessage());
            return "";
        }
    }

    private static float drawLine(PDPageContentStream cs, String label, String value,
                                   float margin, float y, float lineH) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
        cs.newLineAtOffset(margin, y);
        cs.showText(label + ":  ");
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.setNonStrokingColor(0f, 0f, 0f);
        cs.showText(value);
        cs.endText();
        return y - lineH;
    }
}
