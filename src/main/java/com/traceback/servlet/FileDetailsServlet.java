
package com.traceback.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/file-details")
public class FileDetailsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String context = request.getContextPath();
        String idValue = request.getParameter("id");

        // Validate file ID
        if (idValue == null || idValue.trim().isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "File ID is required."
            );
            return;
        }

        long id;

        try {
            id = Long.parseLong(idValue);

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid file ID."
                );
                return;
            }

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "File ID must be a valid number."
            );
            return;
        }

        // Fetch file details from database
        FileAnalysis file;

        try {
            file = fileAnalysisDAO.getAnalysisById(id);

        } catch (SQLException e) {
            getServletContext().log(
                    "Error retrieving file analysis ID: " + id, e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve file analysis details."
            );
            return;
        }

        // Check whether the record exists
        if (file == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "File analysis record not found."
            );
            return;
        }

        // Display file details
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");

        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");

        out.println("<title>File Details | TraceBack</title>");

        out.println("<link rel='stylesheet' href='"
                + context + "/style.css'>");

        out.println("<style>");

        out.println("body {"
                + "font-family: Arial, sans-serif;"
                + "padding: 30px;"
                + "background: #100f1d;"
                + "color: #ffffff;"
                + "}");

        out.println(".details-card {"
                + "max-width: 850px;"
                + "margin: 30px auto;"
                + "padding: 28px;"
                + "border-radius: 16px;"
                + "background: #19172b;"
                + "border: 1px solid #302c49;"
                + "}");

        out.println(".detail-row {"
                + "padding: 16px 0;"
                + "border-bottom: 1px solid #39364f;"
                + "overflow-wrap: anywhere;"
                + "}");

        out.println(".detail-label {"
                + "color: #aaa5c5;"
                + "display: block;"
                + "margin-bottom: 8px;"
                + "font-size: 13px;"
                + "}");

        out.println(".detail-value {"
                + "font-size: 15px;"
                + "line-height: 1.6;"
                + "}");

        out.println(".back-btn {"
                + "display: inline-block;"
                + "margin-top: 24px;"
                + "padding: 12px 18px;"
                + "background: #7657f6;"
                + "color: white;"
                + "text-decoration: none;"
                + "border-radius: 8px;"
                + "}");

        out.println(".back-btn:hover {"
                + "background: #6242e8;"
                + "}");

        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        out.println("<main class='details-card'>");

        out.println("<h1>File Analysis Details</h1>");

        out.println("<p>TraceBack — File Integrity &amp; "
                + "Forensics Workbench</p>");

        out.println("<div class='detail-row'>");
        out.println("<span class='detail-label'>Analysis ID</span>");
        out.println("<div class='detail-value'>"
                + file.getId() + "</div>");
        out.println("</div>");

        out.println("<div class='detail-row'>");
        out.println("<span class='detail-label'>File Name</span>");
        out.println("<div class='detail-value'>"
                + escapeHtml(file.getOriginalFileName())
                + "</div>");
        out.println("</div>");

        out.println("<div class='detail-row'>");
        out.println("<span class='detail-label'>SHA-256 Hash</span>");
        out.println("<div class='detail-value'>"
                + escapeHtml(file.getHashSha256())
                + "</div>");
        out.println("</div>");

        out.println("<div class='detail-row'>");
        out.println("<span class='detail-label'>File Size</span>");
        out.println("<div class='detail-value'>"
                + file.getFileSizeBytes() + " bytes</div>");
        out.println("</div>");

        out.println("<div class='detail-row'>");
        out.println("<span class='detail-label'>MIME Type</span>");
        out.println("<div class='detail-value'>"
                + escapeHtml(file.getMimeType())
                + "</div>");
        out.println("</div>");

        out.println("<div class='detail-row'>");
        out.println("<span class='detail-label'>Analysis Date</span>");
        out.println("<div class='detail-value'>"
                + escapeHtml(String.valueOf(file.getCreatedAt()))
                + "</div>");
        out.println("</div>");

        out.println("<a class='back-btn' href='"
                + context + "/history'>"
                + "Back to Analysis History</a>");

        out.println("</main>");
        out.println("</body>");
        out.println("</html>");
    }

    // Escape HTML characters before displaying database values
    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
