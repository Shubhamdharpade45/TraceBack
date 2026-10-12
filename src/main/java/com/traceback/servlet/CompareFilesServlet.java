
package com.traceback.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.SQLException;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.dao.ComparisonHistoryDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/compare-files")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 22 * 1024 * 1024)
public class CompareFilesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final FileAnalysisDAO fileAnalysisDAO = new FileAnalysisDAO();

	private final ComparisonHistoryDAO comparisonHistoryDAO = new ComparisonHistoryDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html; charset=UTF-8");

		String ctx = request.getContextPath();

		try {
			Part file1 = request.getPart("file1");
			Part file2 = request.getPart("file2");

			if (file1 == null || file2 == null || file1.getSize() == 0 || file2.getSize() == 0) {

				showError(response, ctx, "Please select both files before comparing.");
				return;
			}

			if (file1.getSize() > 10L * 1024 * 1024 || file2.getSize() > 10L * 1024 * 1024) {

				showError(response, ctx, "Each file must be 10 MB or smaller.");
				return;
			}

			String fileName1 = getSafeFileName(file1);
			String fileName2 = getSafeFileName(file2);

			String hash1;
			String hash2;

			try (InputStream input1 = file1.getInputStream(); InputStream input2 = file2.getInputStream()) {

				hash1 = calculateSHA256(input1);
				hash2 = calculateSHA256(input2);
			}

			boolean identical = hash1.equals(hash2);

			// Save both compared files in the database and get their IDs.
			long file1Id = fileAnalysisDAO.saveAnalysis(fileName1, hash1, file1.getSize(),
					file1.getContentType() == null ? "application/octet-stream" : file1.getContentType());

			long file2Id = fileAnalysisDAO.saveAnalysis(fileName2, hash2, file2.getSize(),
					file2.getContentType() == null ? "application/octet-stream" : file2.getContentType());

			// Save the comparison result.

			// Save the comparison result and capture its database ID.
			long comparisonId = comparisonHistoryDAO.saveComparison(file1Id, file2Id, identical);

			showResult(response, ctx, fileName1, fileName2, hash1, hash2, file1.getSize(), file2.getSize(), identical,
					comparisonId);

		} catch (IllegalStateException e) {
			showError(response, ctx, "The uploaded files exceed the allowed size.");
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException("SHA-256 algorithm is not available.", e);
		} catch (Exception e) {
			log("File comparison failed.", e);

			showError(response, ctx, "Comparison failed. Please check the server logs.");
		}
	}

	private String calculateSHA256(InputStream input) throws IOException, NoSuchAlgorithmException {

		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		byte[] buffer = new byte[8192];
		int bytesRead;

		while ((bytesRead = input.read(buffer)) != -1) {
			digest.update(buffer, 0, bytesRead);
		}

		byte[] hashBytes = digest.digest();
		StringBuilder result = new StringBuilder();

		for (byte b : hashBytes) {
			result.append(String.format("%02x", b & 0xff));
		}

		return result.toString();
	}

	private String getSafeFileName(Part part) {

		String name = part.getSubmittedFileName();

		if (name == null || name.trim().isEmpty()) {
			return "unnamed-file";
		}

		// Keep only the filename, not any client-side path.
		name = name.replace('\\', '/');

		int lastSlash = name.lastIndexOf('/');

		if (lastSlash >= 0) {
			name = name.substring(lastSlash + 1);
		}

		return name;
	}

	private void showResult(HttpServletResponse response, String ctx, String fileName1, String fileName2, String hash1,
			String hash2, long size1, long size2, boolean identical, long comparisonId) throws IOException {

		PrintWriter out = response.getWriter();

		out.println("<!DOCTYPE html>");
		out.println("<html lang='en' data-theme='dark'>");
		out.println("<head>");
		out.println("<meta charset='UTF-8'>");
		out.println("<meta name='viewport' " + "content='width=device-width, initial-scale=1.0'>");
		out.println("<title>File Comparison | TraceBack</title>");
		out.println("<link rel='stylesheet' href='" + ctx + "/style.css?v=4'>");
		out.println("<script src='" + ctx + "/app.js' defer></script>");
		out.println("</head><body>");

		printNavigation(out, ctx);

		out.println("<main class='result-page'>");
		out.println("<section class='result-card'>");
		out.println("<p class='result-description'>Comparison ID: #" + comparisonId + "</p>");

		if (identical) {
			out.println("<div class='result-icon'>✓</div>");
			out.println("<div class='eyebrow'>MATCH FOUND</div>");
			out.println("<h1>Files are <span class='gradient-text'>Identical</span></h1>");
			out.println("<p class='result-description'>" + "Both files produced the same SHA-256 fingerprint. "
					+ "Their contents match.</p>");
		} else {
			out.println("<div class='result-icon'>≠</div>");
			out.println("<div class='eyebrow'>DIFFERENCE DETECTED</div>");
			out.println("<h1>Files are <span class='gradient-text'>Different</span></h1>");
			out.println("<p class='result-description'>" + "The SHA-256 fingerprints differ. "
					+ "The file contents are not identical.</p>");
		}

		out.println("<div class='compare-grid'>");

		printFileDetails(out, "FILE 01", fileName1, hash1, size1);

		printFileDetails(out, "FILE 02", fileName2, hash2, size2);

		out.println("</div>");

		out.println("<div class='result-actions'>");

		out.println("<a class='primary-btn' href='" + ctx + "/index.html'>Compare Again</a>");

		out.println("<a class='secondary-btn' href='" + ctx + "/history'>Analysis History</a>");

		out.println("<a class='secondary-btn' href='" + ctx + "/comparison-history'>Comparison History</a>");

		out.println("<a class='primary-btn' href='" + ctx + "/download-report?comparisonId=" + comparisonId
				+ "'>Download PDF Report</a>");

		out.println("</div>");

		out.println("</section></main>");
		out.println("</body></html>");
	}

	private void printFileDetails(PrintWriter out, String label, String fileName, String hash, long size) {

		out.println("<article class='compare-file'>");

		out.println("<span class='section-kicker'>" + label + "</span>");

		out.println("<h3>" + escapeHtml(fileName) + "</h3>");

		out.println("<p class='file-size'>Size: " + formatSize(size) + "</p>");

		out.println("<div class='hash-result'>");
		out.println("<span class='section-kicker'>SHA-256</span>");
		out.println("<code class='result-hash'>" + escapeHtml(hash) + "</code>");
		out.println(
				"<button type='button' class='copy-btn' " + "data-hash='" + escapeHtml(hash) + "'>COPY HASH</button>");
		out.println("</div>");

		out.println("</article>");
	}

	private void showError(HttpServletResponse response, String ctx, String message) throws IOException {

		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		PrintWriter out = response.getWriter();

		out.println("<!DOCTYPE html>");
		out.println("<html lang='en' data-theme='dark'>");
		out.println("<head>");
		out.println("<meta charset='UTF-8'>");
		out.println("<meta name='viewport' " + "content='width=device-width, initial-scale=1.0'>");
		out.println("<title>Comparison Error | TraceBack</title>");
		out.println("<link rel='stylesheet' href='" + ctx + "/style.css?v=4'>");
		out.println("<script src='" + ctx + "/app.js' defer></script>");
		out.println("</head><body>");

		printNavigation(out, ctx);

		out.println("<main class='result-page'>");
		out.println("<section class='result-card error-card'>");
		out.println("<div class='result-icon'>!</div>");
		out.println("<div class='eyebrow'>COMPARISON ERROR</div>");
		out.println("<h1>Unable to <span class='gradient-text'>Compare</span></h1>");
		out.println("<p class='result-description'>" + escapeHtml(message) + "</p>");
		out.println("<div class='result-actions'>");

		out.println("<a class='primary-btn' href='" + ctx + "/index.html'>Try Again</a>");
		out.println("<a class='secondary-btn' href='" + ctx + "/history'>Analysis History</a>");
		out.println("</div>");
		out.println("</section></main>");
		out.println("</body></html>");
	}

	private void printNavigation(PrintWriter out, String ctx) {

		out.println("<header class='topbar'>");
		out.println("<a href='" + ctx + "/index.html' class='brand'>");
		out.println("<span class='brand-icon'>T</span>");
		out.println("<span>TraceBack</span></a>");

		out.println("<nav class='nav-links'>");
		out.println("<a href='" + ctx + "/index.html'>Home</a>");
		out.println("<a href='" + ctx + "/history'>Analysis History</a>");
		out.println("</nav></header>");
	}

	private String escapeHtml(String value) {

		if (value == null) {
			return "";
		}

		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private String formatSize(long bytes) {

		if (bytes < 1024) {
			return bytes + " B";
		}

		if (bytes < 1024L * 1024L) {
			return String.format("%.2f KB", bytes / 1024.0);
		}

		return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
	}
}
