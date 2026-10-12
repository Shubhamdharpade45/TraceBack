
package com.traceback.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import com.traceback.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/db-test")
public class DBTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try (Connection connection = DBConnection.getConnection()) {

            out.println("<h2>TraceBack Database Connection Successful!</h2>");
            out.println("<p>Database: traceback_db</p>");
            out.println("<p>Status: Connected</p>");

        } catch (SQLException e) {

            getServletContext().log("Database connection failed", e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("<h2>Database Connection Failed</h2>");
            out.println("<p>Check the Eclipse Console for the error.</p>");
        }
    }
}
