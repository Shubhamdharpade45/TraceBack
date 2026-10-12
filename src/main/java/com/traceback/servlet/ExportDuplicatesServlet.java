
package com.traceback.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/export-duplicates")
public class ExportDuplicatesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<FileAnalysis> files =
                    fileAnalysisDAO.getDuplicateAnalyses();

            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=\"TraceBack-Duplicates.csv\""
            );

            // UTF-8 BOM helps Excel recognize UTF-8 CSV.
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.write('\uFEFF');

            out.println(
                "File ID,File Name,SHA-256,File Size (Bytes),MIME Type,Analysis Date"
            );

            for (FileAnalysis file : files) {
                out.print(file.getId());
                out.print(",");
                out.print(csv(file.getOriginalFileName()));
                out.print(",");
                out.print(csv(file.getHashSha256()));
                out.print(",");
                out.print(file.getFileSizeBytes());
                out.print(",");
                out.print(csv(file.getMimeType()));
                out.print(",");
                out.println(csv(
                    file.getCreatedAt() == null
                        ? "" : file.getCreatedAt().toString()
                ));
            }

            out.flush();

        } catch (SQLException e) {
            log("Unable to export duplicate file records", e);

            if (!response.isCommitted()) {
                response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to export duplicate records."
                );
            }
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }

        // Prevent spreadsheet formula injection.
        String safe = value;
        if (!safe.isEmpty()
                && (safe.charAt(0) == '='
                || safe.charAt(0) == '+'
                || safe.charAt(0) == '-'
                || safe.charAt(0) == '@'
                || safe.charAt(0) == '\t'
                || safe.charAt(0) == '\r')) {
            safe = "'" + safe;
        }

        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
