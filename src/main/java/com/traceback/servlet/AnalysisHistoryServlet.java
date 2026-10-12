
package com.traceback.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.dao.ComparisonHistoryDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/history")
public class AnalysisHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    private final ComparisonHistoryDAO comparisonHistoryDAO =
            new ComparisonHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Fetch file analysis records
            List<FileAnalysis> analyses =
                    fileAnalysisDAO.getAllAnalyses();

            // File statistics
            int totalAnalyses =
                    fileAnalysisDAO.getTotalAnalyses();

            int uniqueHashes =
                    fileAnalysisDAO.getUniqueHashCount();

            int duplicateCount =
                    fileAnalysisDAO.getDuplicateCount();

            // Comparison statistics
            int totalComparisons =
                    comparisonHistoryDAO.getTotalComparisons();

            int matchingComparisons =
                    comparisonHistoryDAO.getMatchingComparisons();

            int mismatchingComparisons =
                    comparisonHistoryDAO.getMismatchingComparisons();

            // Pass all data to dashboard.jsp
            request.setAttribute("analyses", analyses);

            request.setAttribute("totalAnalyses", totalAnalyses);
            request.setAttribute("uniqueHashes", uniqueHashes);
            request.setAttribute("duplicateCount", duplicateCount);

            request.setAttribute("totalComparisons", totalComparisons);
            request.setAttribute("matchingComparisons",
                    matchingComparisons);
            request.setAttribute("mismatchingComparisons",
                    mismatchingComparisons);

            // Display dashboard
            request.getRequestDispatcher("/dashboard.jsp")
                   .forward(request, response);

        } catch (SQLException e) {

            log("Unable to retrieve history or dashboard statistics.", e);

            response.setContentType("text/html; charset=UTF-8");
            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().println(
                "<!DOCTYPE html>"
                + "<html lang='en' data-theme='dark'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' "
                + "content='width=device-width, initial-scale=1.0'>"
                + "<title>History Error | TraceBack</title>"
                + "<link rel='stylesheet' href='"
                + request.getContextPath()
                + "/style.css?v=4'>"
                + "</head><body>"
                + "<main class='result-page'>"
                + "<section class='result-card error-card'>"
                + "<h1>Unable to load history</h1>"
                + "<p class='result-description'>"
                + "Please check the database connection and try again."
                + "</p>"
                + "<a class='primary-btn' href='"
                + request.getContextPath()
                + "/index.html'>Back to Home</a>"
                + "</section></main></body></html>"
            );
        }
    }
}
