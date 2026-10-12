
package com.traceback.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/duplicates")
public class DuplicateFilesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<FileAnalysis> duplicateFiles =
                    fileAnalysisDAO.getDuplicateAnalyses();

            request.setAttribute(
                    "duplicateFiles", duplicateFiles);

            request.getRequestDispatcher(
                    "/duplicates.jsp")
                    .forward(request, response);

        } catch (SQLException e) {

            log("Error fetching duplicate file records", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load duplicate file records. Please check the server logs.");
        }
    }
}
