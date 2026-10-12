
package com.traceback.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/upload-file")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 10 * 1024 * 1024,
    maxRequestSize = 12 * 1024 * 1024
)
public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");

        String ctx = request.getContextPath();

        try {
            Part filePart = request.getPart("file");

            if (filePart == null || filePart.getSize() == 0) {
                showError(response, ctx,
                        "Please select a file before uploading.");
                return;
            }

            if (filePart.getSize() > 10L * 1024 * 1024) {
                showError(response, ctx,
                        "File size must not exceed 10 MB.");
                return;
            }

            String originalFileName =
                    filePart.getSubmittedFileName();

            if (originalFileName == null ||
                    originalFileName.trim().isEmpty()) {
                originalFileName = "unnamed-file";
            }

            // Get the file's SHA-256 fingerprint.
            String hashSha256;

            try (InputStream inputStream = filePart.getInputStream()) {
                hashSha256 = calculateSHA256(inputStream);
            }

            String mimeType = filePart.getContentType();

            if (mimeType == null || mimeType.trim().isEmpty()) {
                mimeType = "application/octet-stream";
            }

            FileAnalysis analysis = new FileAnalysis();

            analysis.setOriginalFileName(originalFileName);
            analysis.setHashSha256(hashSha256);
            analysis.setFileSizeBytes(filePart.getSize());
            analysis.setMimeType(mimeType);

            // Save metadata through the DAO.

            // Save metadata and get the generated database ID.
           long analysisId = fileAnalysisDAO.saveAnalysis(
               originalFileName,
               hashSha256,
               filePart.getSize(),
               mimeType
           );


            showSuccess(
                response,
                ctx,
                originalFileName,
                hashSha256,
                filePart.getSize(),
                mimeType
            );

        } catch (IllegalStateException e) {
            showError(response, ctx,
                    "The uploaded file exceeds the allowed size.");
        } catch (NoSuchAlgorithmException e) {
            throw new ServletException(
                    "SHA-256 algorithm is not available.", e);
        } catch (Exception e) {
            log("File upload or database save failed.", e);

            showError(response, ctx,
                    "Upload failed. Please check the server logs "
                    + "and database connection.");
        }
    }

    private String calculateSHA256(InputStream inputStream)
            throws IOException, NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hashBytes = digest.digest();

        StringBuilder result = new StringBuilder();

        for (byte b : hashBytes) {
            result.append(String.format("%02x", b & 0xff));
        }

        return result.toString();
    }

    private void showSuccess(HttpServletResponse response,
                             String ctx,
                             String fileName,
                             String hash,
                             long fileSize,
                             String mimeType)
            throws IOException {

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en' data-theme='dark'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' "
                + "content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Analysis Complete | TraceBack</title>");
        out.println("<link rel='stylesheet' href='"
                + ctx + "/style.css?v=4'>");
        out.println("<script src='" + ctx
                + "/app.js' defer></script>");
        out.println("</head><body>");

        printNavigation(out, ctx);

        out.println("<main class='result-page'>");
        out.println("<section class='result-card'>");

        out.println("<div class='result-icon'>✓</div>");
        out.println("<div class='eyebrow'>ANALYSIS COMPLETE</div>");
        out.println("<h1>File <span class='gradient-text'>Analyzed</span></h1>");
        out.println("<p class='result-description'>"
                + "SHA-256 fingerprint generated and metadata "
                + "saved successfully.</p>");

        out.println("<div class='result-details'>");

        out.println("<div class='detail-row'>");
        out.println("<span>File name</span><strong>"
                + escapeHtml(fileName) + "</strong></div>");

        out.println("<div class='detail-row'>");
        out.println("<span>File size</span><strong>"
                + formatSize(fileSize) + "</strong></div>");

        out.println("<div class='detail-row'>");
        out.println("<span>MIME type</span><strong>"
                + escapeHtml(mimeType) + "</strong></div>");

        out.println("</div>");

        out.println("<div class='hash-result'>");
        out.println("<span class='section-kicker'>SHA-256 FINGERPRINT</span>");
        out.println("<code class='result-hash'>"
                + escapeHtml(hash) + "</code>");
        out.println("<button type='button' class='copy-btn' "
                + "data-hash='" + escapeHtml(hash) + "'>COPY HASH</button>");
        out.println("</div>");

        out.println("<div class='result-actions'>");
        out.println("<a class='primary-btn' href='"
                + ctx + "/index.html'>Analyze Another File</a>");
        out.println("<a class='secondary-btn' href='"
                + ctx + "/history'>View Analysis History</a>");
        out.println("</div>");

        out.println("</section></main>");
        out.println("</body></html>");
    }

    private void showError(HttpServletResponse response,
                           String ctx,
                           String message) throws IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en' data-theme='dark'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' "
                + "content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Upload Error | TraceBack</title>");
        out.println("<link rel='stylesheet' href='"
                + ctx + "/style.css?v=4'>");
        out.println("<script src='" + ctx
                + "/app.js' defer></script>");
        out.println("</head><body>");

        printNavigation(out, ctx);

        out.println("<main class='result-page'>");
        out.println("<section class='result-card error-card'>");
        out.println("<div class='result-icon'>!</div>");
        out.println("<div class='eyebrow'>UPLOAD ERROR</div>");
        out.println("<h1>Analysis <span class='gradient-text'>Failed</span></h1>");
        out.println("<p class='result-description'>"
                + escapeHtml(message) + "</p>");
        out.println("<div class='result-actions'>");
        out.println("<a class='primary-btn' href='"
                + ctx + "/index.html'>Try Again</a>");
        out.println("<a class='secondary-btn' href='"
                + ctx + "/history'>Analysis History</a>");
        out.println("</div>");
        out.println("</section></main>");
        out.println("</body></html>");
    }

    private void printNavigation(PrintWriter out, String ctx) {

        out.println("<header class='topbar'>");
        out.println("<a href='" + ctx
                + "/index.html' class='brand'>");
        out.println("<span class='brand-icon'>⌁</span>");
        out.println("<span>TraceBack</span></a>");

        out.println("<nav class='nav-links'>");
        out.println("<a href='" + ctx + "/index.html'>Home</a>");
        out.println("<a href='" + ctx + "/history'>Analysis History</a>");
        out.println("<button type='button' class='theme-toggle' "
                + "id='themeToggle'>☀ Light mode</button>");
        out.println("</nav></header>");
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private String formatSize(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024L * 1024L) {
            return String.format("%.2f KB", bytes / 1024.0);
        }

        return String.format("%.2f MB",
                bytes / (1024.0 * 1024.0));
    }
}
