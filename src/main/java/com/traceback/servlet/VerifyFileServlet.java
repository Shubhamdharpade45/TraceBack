
package com.traceback.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;

import com.traceback.dao.FileAnalysisDAO;
import com.traceback.dao.VerificationHistoryDAO;
import com.traceback.model.FileAnalysis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/verify-file")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 10 * 1024 * 1024,
    maxRequestSize = 11 * 1024 * 1024
)
public class VerifyFileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FileAnalysisDAO fileAnalysisDAO =
            new FileAnalysisDAO();

    private final VerificationHistoryDAO verificationHistoryDAO =
            new VerificationHistoryDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/verify-file.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String recordIdText = request.getParameter("recordId");

        try {
            if (recordIdText == null
                    || !recordIdText.trim().matches("\\d+")) {

                request.setAttribute(
                    "errorMessage",
                    "Please enter a valid original file record ID."
                );
                showPage(request, response);
                return;
            }

            long recordId = Long.parseLong(recordIdText.trim());

            if (recordId <= 0) {
                request.setAttribute(
                    "errorMessage",
                    "Record ID must be greater than zero."
                );
                showPage(request, response);
                return;
            }

            FileAnalysis original =
                    fileAnalysisDAO.getAnalysisById(recordId);

            if (original == null) {
                request.setAttribute(
                    "errorMessage",
                    "No saved file analysis exists for ID " + recordId + "."
                );
                showPage(request, response);
                return;
            }

            Part uploadedPart = request.getPart("verificationFile");

            if (uploadedPart == null
                    || uploadedPart.getSize() == 0) {

                request.setAttribute(
                    "errorMessage",
                    "Please select a file to verify."
                );
                showPage(request, response);
                return;
            }

            String submittedName = uploadedPart.getSubmittedFileName();

            if (submittedName == null
                    || submittedName.trim().isEmpty()) {

                request.setAttribute(
                    "errorMessage",
                    "The selected file has no valid filename."
                );
                showPage(request, response);
                return;
            }

            String uploadedHash;

            try (InputStream input = uploadedPart.getInputStream()) {
                uploadedHash = calculateSHA256(input);
            }

            String originalHash = original.getHashSha256();

            if (originalHash == null
                    || originalHash.trim().isEmpty()) {

                request.setAttribute(
                    "errorMessage",
                    "The saved record does not contain a SHA-256 hash."
                );
                showPage(request, response);
                return;
            }

            boolean matches =
                    originalHash.trim().equalsIgnoreCase(uploadedHash);

            // Extract a safe filename without any client-supplied path.
            String safeFileName = submittedName.replace('\\', '/');

            safeFileName = safeFileName.substring(
                    safeFileName.lastIndexOf('/') + 1
            );

            // STEP 3: Save verification result in MySQL.
            verificationHistoryDAO.saveVerification(
                    original.getId(),
                    safeFileName,
                    originalHash.trim(),
                    uploadedHash,
                    matches
            );

            // Send verification result to the JSP page.
            request.setAttribute("originalRecord", original);
            request.setAttribute("uploadedFileName", safeFileName);
            request.setAttribute("uploadedHash", uploadedHash);
            request.setAttribute("hashMatches", matches);
            request.setAttribute("verificationCompleted", true);

            showPage(request, response);

        } catch (NumberFormatException e) {
            request.setAttribute(
                "errorMessage",
                "The record ID is outside the supported numeric range."
            );
            showPage(request, response);

        } catch (IllegalStateException e) {
            request.setAttribute(
                "errorMessage",
                "The uploaded file exceeds the 10 MB limit."
            );
            showPage(request, response);

        } catch (SQLException e) {
            log("Database error during file integrity verification", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Unable to retrieve the original record or save verification history."
            );

        } catch (ServletException e) {
            log("Upload error during file integrity verification", e);
            request.setAttribute(
                "errorMessage",
                "Unable to process the uploaded file. Check that it is within the 10 MB limit."
            );
            showPage(request, response);

        } catch (NoSuchAlgorithmException e) {
            log("SHA-256 algorithm unavailable", e);
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "SHA-256 verification is unavailable."
            );
        }
    }

    private void showPage(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/verify-file.jsp")
               .forward(request, response);
    }

    private String calculateSHA256(InputStream input)
            throws IOException, NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        return HexFormat.of().formatHex(digest.digest());
    }
}
