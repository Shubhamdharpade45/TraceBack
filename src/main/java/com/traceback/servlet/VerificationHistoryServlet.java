
package com.traceback.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.traceback.dao.VerificationHistoryDAO;
import com.traceback.model.VerificationHistory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/verification-history")
public class VerificationHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final VerificationHistoryDAO verificationHistoryDAO =
            new VerificationHistoryDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<VerificationHistory> verifications =
                    verificationHistoryDAO.getAllVerifications();

            request.setAttribute("verifications", verifications);

            request.getRequestDispatcher("/verification-history.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            log("Unable to fetch verification history", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load verification history. Please try again."
            );
        }
    }
}
