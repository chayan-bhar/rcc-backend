package com.ngo.ngoapp.services;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ngo.ngoapp.models.Donation;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptService {

    public ByteArrayInputStream generateDonationReceipt(Donation donation) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new Color(41, 128, 185));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(44, 62, 80));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            // NGO Header
            Paragraph title = new Paragraph("HOPE & CARE FOUNDATION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("123 Helping Hands Street, Bengaluru, India | info@hopecare.org\nRegistration No: NGO-12345-BLR\nSection 80G Tax Exemption Certified", subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Receipt Headline
            Paragraph headline = new Paragraph("DONATION RECEIPT", sectionHeaderFont);
            headline.setAlignment(Element.ALIGN_CENTER);
            headline.setSpacingAfter(20);
            document.add(headline);

            // Table of Details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);
            table.setWidths(new float[]{1.5f, 3.5f});

            // Helper to add row
            addTableRow(table, "Receipt Number:", "HCR-" + donation.getId() + "-" + System.currentTimeMillis() % 10000, labelFont, valueFont);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
            addTableRow(table, "Donation Date & Time:", donation.getCreatedAt().format(formatter), labelFont, valueFont);
            
            addTableRow(table, "Donor Name:", donation.getDonorName() != null ? donation.getDonorName() : "Anonymous Donor", labelFont, valueFont);
            addTableRow(table, "Donor Email:", donation.getDonorEmail() != null ? donation.getDonorEmail() : "N/A", labelFont, valueFont);
            
            String campaignName = donation.getCampaign() != null ? donation.getCampaign().getTitle() : "General NGO Fund";
            addTableRow(table, "Donated For (Campaign):", campaignName, labelFont, valueFont);
            
            addTableRow(table, "Donation Amount:", "INR " + String.format("%.2f", donation.getAmount()), labelFont, valueFont);
            addTableRow(table, "Payment status:", donation.getStatus(), labelFont, valueFont);
            addTableRow(table, "Order ID:", donation.getOrderId() != null ? donation.getOrderId() : "N/A", labelFont, valueFont);
            addTableRow(table, "Payment ID:", donation.getPaymentId() != null ? donation.getPaymentId() : "N/A", labelFont, valueFont);

            document.add(table);

            // Tax Exemption Disclaimer
            Paragraph taxInfo = new Paragraph("Note: Hope & Care Foundation is registered under section 12A of the Income Tax Act, 1961. " +
                    "This payment qualifies for a 50% tax exemption under section 80G of the Income Tax Act.", valueFont);
            taxInfo.setSpacingAfter(30);
            document.add(taxInfo);

            // Footer Signature section
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(90);
            footerTable.setWidths(new float[]{2.5f, 2.5f});

            PdfPCell cellLeft = new PdfPCell(new Phrase("Thank you for your kindness!", footerFont));
            cellLeft.setBorder(PdfPCell.NO_BORDER);
            cellLeft.setVerticalAlignment(Element.ALIGN_BOTTOM);
            footerTable.addCell(cellLeft);

            PdfPCell cellRight = new PdfPCell(new Paragraph("Authorized Signatory\n\n[Hope & Care Finance]", labelFont));
            cellRight.setBorder(PdfPCell.NO_BORDER);
            cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footerTable.addCell(cellRight);

            document.add(footerTable);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setPadding(8);
        cellLabel.setBackgroundColor(new Color(245, 247, 250));
        cellLabel.setBorderColor(new Color(220, 224, 230));
        
        PdfPCell cellValue = new PdfPCell(new Phrase(value, valueFont));
        cellValue.setPadding(8);
        cellValue.setBorderColor(new Color(220, 224, 230));

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
}
