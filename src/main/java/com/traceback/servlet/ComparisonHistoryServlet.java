
package com.traceback.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.traceback.dao.ComparisonHistoryDAO;
import com.traceback.dao.ComparisonHistoryDAO.ComparisonRecord;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/comparison-history")
public class ComparisonHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ComparisonHistoryDAO comparisonHistoryDAO =
            new ComparisonHistoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<ComparisonRecord> comparisons =
                    comparisonHistoryDAO.getAllComparisons();

            request.setAttribute("comparisons", comparisons);

            request.getRequestDispatcher("/comparison-history.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            log("Unable to retrieve comparison history.", e);

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
                + "<title>Comparison History Error | TraceBack</title>"
                + "<link rel='stylesheet' href='"
                + request.getContextPath()
                + "/style.css?v=4'>"
                + "</head><body>"
                + "<main class='result-page'>"
                + "<section class='result-card error-card'>"
                + "<h1>Unable to load comparison history</h1>"
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
