
package com.traceback.servlet;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.traceback.dao.ComparisonHistoryDAO;
import com.traceback.dao.ComparisonHistoryDAO.ComparisonRecord;
import com.traceback.dao.FileAnalysisDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download-report")
public class DownloadReportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final ComparisonHistoryDAO comparisonDAO = new ComparisonHistoryDAO();

	private final FileAnalysisDAO fileAnalysisDAO = new FileAnalysisDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String idParameter = request.getParameter("comparisonId");

		if (idParameter == null || idParameter.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Comparison ID is required.");
			return;
		}

		long comparisonId;

		try {
			comparisonId = Long.parseLong(idParameter);

			if (comparisonId <= 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid comparison ID.");
			return;
		}

		try {
			// Fetch the actual comparison from the database.
			ComparisonRecord comparison = comparisonDAO.getComparisonById(comparisonId);

			if (comparison == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comparison record not found.");
				return;
			}

			// Fetch both files using their stored database IDs.
			FileAnalysis file1 = fileAnalysisDAO.getAnalysisById(comparison.getFile1Id());

			FileAnalysis file2 = fileAnalysisDAO.getAnalysisById(comparison.getFile2Id());

			if (file1 == null || file2 == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "File analysis record not found.");
				return;
			}

			String timestamp = comparison.getComparedAt() == null ? "Not available"
					: comparison.getComparedAt().toLocalDateTime()
							.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

			createPdf(response, comparison, file1, file2, timestamp);

		} catch (Exception e) {
			log("Unable to generate comparison PDF.", e);

			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to generate PDF report.");
			}
		}
	}

	private void createPdf(HttpServletResponse response, ComparisonRecord comparison, FileAnalysis file1,
			FileAnalysis file2, String timestamp) throws Exception {

		boolean identical = comparison.isContentsMatch();

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition",
				"attachment; filename=\"TraceBack-Report-" + comparison.getId() + ".pdf\"");

		Document document = new Document(PageSize.A4, 45, 45, 45, 45);

		PdfWriter.getInstance(document, response.getOutputStream());
		document.open();

		Font titleFont = new Font(Font.FontFamily.HELVETICA, 23, Font.BOLD, new BaseColor(95, 55, 160));

		Font headingFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(65, 45, 105));

		Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);

		Font hashFont = new Font(Font.FontFamily.COURIER, 8, Font.NORMAL, BaseColor.BLACK);

		try {
			Paragraph title = new Paragraph("TraceBack", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph subtitle = new Paragraph("FILE INTEGRITY & FORENSICS REPORT", headingFont);
			subtitle.setAlignment(Element.ALIGN_CENTER);
			subtitle.setSpacingAfter(15);
			document.add(subtitle);

			document.add(new Paragraph("Comparison ID: #" + comparison.getId(), normalFont));

			document.add(new Paragraph("Compared At: " + timestamp, normalFont));

			document.add(new Paragraph(" "));

			document.add(new Paragraph("Comparison Result", headingFont));

			PdfPTable resultTable = new PdfPTable(1);
			resultTable.setWidthPercentage(100);

			Font resultFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD,
					identical ? new BaseColor(20, 130, 75) : new BaseColor(190, 45, 45));

			PdfPCell resultCell = new PdfPCell(new Phrase(identical ? "IDENTICAL" : "DIFFERENT", resultFont));

			resultCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			resultCell.setPadding(14);
			resultCell.setBackgroundColor(identical ? new BaseColor(225, 245, 232) : new BaseColor(255, 232, 232));

			resultTable.addCell(resultCell);
			document.add(resultTable);

			document.add(new Paragraph(" "));
			document.add(new Paragraph("File Details", headingFont));
			document.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(new float[] { 1.3f, 3.7f });

			table.setWidthPercentage(100);

			addCell(table, "Property", true, normalFont);
			addCell(table, "Details", true, normalFont);

			addCell(table, "File 1 Name", false, normalFont);
			addCell(table, file1.getOriginalFileName(), false, normalFont);

			addCell(table, "File 1 Size", false, normalFont);
			addCell(table, file1.getFileSizeBytes() + " bytes", false, normalFont);

			addCell(table, "File 1 SHA-256", false, normalFont);
			addCell(table, file1.getHashSha256(), false, hashFont);

			addCell(table, "File 2 Name", false, normalFont);
			addCell(table, file2.getOriginalFileName(), false, normalFont);

			addCell(table, "File 2 Size", false, normalFont);
			addCell(table, file2.getFileSizeBytes() + " bytes", false, normalFont);

			addCell(table, "File 2 SHA-256", false, normalFont);
			addCell(table, file2.getHashSha256(), false, hashFont);

			addCell(table, "Hashes Match", false, normalFont);
			addCell(table, String.valueOf(identical), false, normalFont);

			document.add(table);

			Paragraph note = new Paragraph("\nThis report uses the comparison result and file "
					+ "metadata stored in the TraceBack database. " + "Matching SHA-256 hashes indicate matching file "
					+ "contents with extremely high confidence.", normalFont);

			note.setSpacingBefore(18);
			document.add(note);

			Paragraph footer = new Paragraph("\nTraceBack - File Integrity & Forensics Workbench", normalFont);

			footer.setAlignment(Element.ALIGN_CENTER);
			footer.setSpacingBefore(20);
			document.add(footer);

		} finally {
			document.close();
		}
	}

	private void addCell(PdfPTable table, String text, boolean header, Font font) {

		PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));

		cell.setPadding(8);

		if (header) {
			cell.setBackgroundColor(new BaseColor(230, 222, 244));
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		} else {
			cell.setBackgroundColor(BaseColor.WHITE);
		}

		table.addCell(cell);
	}
}
