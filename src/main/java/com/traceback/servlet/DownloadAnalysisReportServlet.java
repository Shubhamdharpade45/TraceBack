
package com.traceback.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download-analysis-report")
public class DownloadAnalysisReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    private static final BaseColor PURPLE =
            new BaseColor(103, 58, 183);

    private static final BaseColor DARK_PURPLE =
            new BaseColor(54, 39, 91);

    private static final BaseColor LIGHT_PURPLE =
            new BaseColor(232, 224, 247);

    private static final BaseColor LIGHT_BG =
            new BaseColor(248, 247, 252);

    private static final BaseColor TEXT =
            new BaseColor(48, 48, 60);

    private static final BaseColor MUTED =
            new BaseColor(105, 108, 125);

    private static final BaseColor BORDER =
            new BaseColor(205, 205, 215);

    private static final BaseColor SUCCESS_BG =
            new BaseColor(224, 245, 232);

    private static final BaseColor SUCCESS =
            new BaseColor(20, 135, 78);

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String idValue = request.getParameter("id");

        if (idValue == null || idValue.trim().isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Analysis ID is required."
            );
            return;
        }

        long id;

        try {
            id = Long.parseLong(idValue.trim());

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid analysis ID."
                );
                return;
            }

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Analysis ID must be a valid number."
            );
            return;
        }

        FileAnalysis file;

        try {
            file = fileAnalysisDAO.getAnalysisById(id);

        } catch (SQLException e) {
            getServletContext().log(
                    "Unable to retrieve analysis record " + id, e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve the analysis record."
            );
            return;
        }

        if (file == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Analysis record not found."
            );
            return;
        }

        String reportDate = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        );

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"TraceBack-Analysis-"
                        + file.getId() + ".pdf\""
        );
        response.setHeader("Cache-Control", "no-store");

        Document document = new Document(
                PageSize.A4, 42, 42, 48, 52
        );

        try {
            PdfWriter writer = PdfWriter.getInstance(
                    document, response.getOutputStream()
            );

            writer.setPageEvent(new ReportFooter());

            document.open();

            addHeader(document, file, reportDate);

            addSectionTitle(document, "File Details");
            addFileDetails(document, file);

            addSectionTitle(document, "SHA-256 Integrity Fingerprint");
            addHashCard(document, file.getHashSha256());

            addSectionTitle(document, "Analysis Status");
            addStatusBox(document, file);

            addDisclaimer(document);

            document.close();

        } catch (DocumentException e) {
            getServletContext().log(
                    "Unable to generate analysis PDF for ID " + id, e
            );

            if (!response.isCommitted()) {
                response.reset();
                response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Unable to generate the PDF report."
                );
            }
        }
    }

    // Centered heading similar to the comparison report.
    private void addHeader(
            Document document,
            FileAnalysis file,
            String reportDate)
            throws DocumentException {

        Font brandFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                24, PURPLE
        );

        Font subtitleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                12, DARK_PURPLE
        );

        Font metadataFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                9, TEXT
        );

        Paragraph brand = new Paragraph("TraceBack", brandFont);
        brand.setAlignment(Element.ALIGN_CENTER);
        brand.setSpacingAfter(3);
        document.add(brand);

        Paragraph subtitle = new Paragraph(
                "FILE INTEGRITY & FORENSICS REPORT",
                subtitleFont
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        Paragraph reportInfo = new Paragraph(
                "Report ID: TB-AN-" + file.getId()
                        + "\nGenerated At: " + reportDate,
                metadataFont
        );
        reportInfo.setLeading(15);
        reportInfo.setSpacingAfter(20);
        document.add(reportInfo);

        addDivider(document);
    }

    private void addDivider(Document document)
            throws DocumentException {

        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        divider.setSpacingAfter(17);

        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setFixedHeight(2);
        cell.setBackgroundColor(LIGHT_PURPLE);
        cell.setBorder(Rectangle.NO_BORDER);

        divider.addCell(cell);
        document.add(divider);
    }

    private void addSectionTitle(
            Document document,
            String title)
            throws DocumentException {

        Paragraph heading = new Paragraph(
                title,
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        13, DARK_PURPLE
                )
        );

        heading.setSpacingBefore(8);
        heading.setSpacingAfter(9);

        document.add(heading);
    }

    private void addFileDetails(
            Document document,
            FileAnalysis file)
            throws DocumentException {

        PdfPTable table = new PdfPTable(new float[] { 30, 70 });
        table.setWidthPercentage(100);
        table.setSpacingAfter(19);

        addDetailRow(table, "Database Record ID",
                String.valueOf(file.getId()));

        addDetailRow(table, "Original File Name",
                safe(file.getOriginalFileName()));

        addDetailRow(table, "File Size",
                formatSize(file.getFileSizeBytes()));

        addDetailRow(table, "Size in Bytes",
                file.getFileSizeBytes() + " bytes");

        addDetailRow(table, "MIME Type",
                safe(file.getMimeType()));

        addDetailRow(table, "Analysis Status", "COMPLETED");

        addDetailRow(table, "Analyzed At",
                file.getCreatedAt() == null
                        ? "Not available"
                        : file.getCreatedAt().toString());

        document.add(table);
    }

    private void addDetailRow(
            PdfPTable table,
            String label,
            String value) {

        Font labelFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                8, MUTED
        );

        Font valueFont = FontFactory.getFont(
                FontFactory.HELVETICA,
                9, TEXT
        );

        PdfPCell labelCell = new PdfPCell(
                new Phrase(label, labelFont)
        );
        labelCell.setBackgroundColor(LIGHT_BG);
        labelCell.setPadding(9);
        labelCell.setBorderColor(BORDER);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valueCell = new PdfPCell(
                new Phrase(value, valueFont)
        );
        valueCell.setPadding(9);
        valueCell.setBorderColor(BORDER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHashCard(
            Document document,
            String hash)
            throws DocumentException {

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(19);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_BG);
        cell.setBorderColor(BORDER);
        cell.setPadding(13);

        Font labelFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                8, MUTED
        );

        Font hashFont = FontFactory.getFont(
                FontFactory.COURIER,
                8, DARK_PURPLE
        );

        Paragraph label = new Paragraph(
                "SHA-256 CHECKSUM",
                labelFont
        );
        label.setSpacingAfter(8);
        cell.addElement(label);

        Paragraph hashText = new Paragraph(
                groupHash(safe(hash)),
                hashFont
        );
        hashText.setLeading(14);
        cell.addElement(hashText);

        table.addCell(cell);
        document.add(table);
    }

    private void addStatusBox(
            Document document,
            FileAnalysis file)
            throws DocumentException {

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(SUCCESS_BG);
        cell.setBorderColor(new BaseColor(160, 218, 181));
        cell.setPadding(15);

        Paragraph status = new Paragraph(
                "ANALYSIS COMPLETED",
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        13, SUCCESS
                )
        );
        status.setAlignment(Element.ALIGN_CENTER);
        status.setSpacingAfter(7);
        cell.addElement(status);

        Paragraph description = new Paragraph(
                "The file metadata and SHA-256 fingerprint "
                        + "have been recorded in the TraceBack database.\n"
                        + "Analysis Record ID: #" + file.getId(),
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        9, TEXT
                )
        );
        description.setAlignment(Element.ALIGN_CENTER);
        description.setLeading(14);
        cell.addElement(description);

        table.addCell(cell);
        document.add(table);
    }

    private void addDisclaimer(Document document)
            throws DocumentException {

        Paragraph note = new Paragraph(
                "Note: This report represents the file metadata and "
                        + "SHA-256 fingerprint stored in TraceBack. "
                        + "A hash can help detect file-content changes, "
                        + "but does not independently prove a file's origin "
                        + "or authenticity.",
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8, MUTED
                )
        );

        note.setLeading(12);
        note.setSpacingBefore(8);
        document.add(note);
    }

    private String formatSize(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024L * 1024L) {
            return String.format("%.2f KB", bytes / 1024.0);
        }

        if (bytes < 1024L * 1024L * 1024L) {
            return String.format(
                    "%.2f MB", bytes / (1024.0 * 1024.0)
            );
        }

        return String.format(
                "%.2f GB",
                bytes / (1024.0 * 1024.0 * 1024.0)
        );
    }

    private String groupHash(String hash) {

        StringBuilder grouped = new StringBuilder();

        for (int i = 0; i < hash.length(); i++) {
            if (i > 0 && i % 8 == 0) {
                grouped.append(' ');
            }
            grouped.append(hash.charAt(i));
        }

        return grouped.toString();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }

    private static class ReportFooter
            extends PdfPageEventHelper {

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document) {

            PdfPTable footer = new PdfPTable(2);

            try {
                footer.setWidths(new float[] { 75, 25 });
                footer.setTotalWidth(
                        document.right() - document.left()
                );

                Font footerFont = FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8, MUTED
                );

                PdfPCell left = new PdfPCell(
                        new Phrase(
                                "TraceBack | File Integrity Report",
                                footerFont
                        )
                );
                left.setBorder(Rectangle.TOP);
                left.setBorderColor(BORDER);
                left.setPaddingTop(7);

                PdfPCell right = new PdfPCell(
                        new Phrase(
                                "Page " + writer.getPageNumber(),
                                footerFont
                        )
                );
                right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                right.setBorder(Rectangle.TOP);
                right.setBorderColor(BORDER);
                right.setPaddingTop(7);

                footer.addCell(left);
                footer.addCell(right);

                footer.writeSelectedRows(
                        0, -1,
                        document.left(),
                        document.bottom() - 16,
                        writer.getDirectContent()
                );

            } catch (DocumentException e) {
                throw new IllegalStateException(
                        "Unable to render PDF footer.", e
                );
            }
        }
    }
}
