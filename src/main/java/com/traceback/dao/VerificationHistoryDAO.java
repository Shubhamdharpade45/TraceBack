
package com.traceback.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.traceback.model.VerificationHistory;
import com.traceback.util.DBConnection;

public class VerificationHistoryDAO {

    public long saveVerification(
            long originalAnalysisId,
            String uploadedFileName,
            String originalSha256,
            String uploadedSha256,
            boolean verificationResult) throws SQLException {

        String sql =
            "INSERT INTO verification_history " +
            "(original_analysis_id, uploaded_file_name, " +
            "original_sha256, uploaded_sha256, verification_result) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, originalAnalysisId);
            ps.setString(2, uploadedFileName);
            ps.setString(3, originalSha256);
            ps.setString(4, uploadedSha256);
            ps.setBoolean(5, verificationResult);

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Saving verification failed.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException(
                "Verification saved, but ID could not be retrieved.");
        }
    }

    public List<VerificationHistory> getAllVerifications()
            throws SQLException {

        List<VerificationHistory> records = new ArrayList<>();

        String sql =
            "SELECT id, original_analysis_id, uploaded_file_name, " +
            "original_sha256, uploaded_sha256, verification_result, " +
            "verified_at FROM verification_history ORDER BY id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                VerificationHistory record =
                        new VerificationHistory();

                record.setId(rs.getLong("id"));
                record.setOriginalAnalysisId(
                    rs.getLong("original_analysis_id"));
                record.setUploadedFileName(
                    rs.getString("uploaded_file_name"));
                record.setOriginalSha256(
                    rs.getString("original_sha256"));
                record.setUploadedSha256(
                    rs.getString("uploaded_sha256"));
                record.setVerificationResult(
                    rs.getBoolean("verification_result"));
                record.setVerifiedAt(
                    rs.getTimestamp("verified_at"));

                records.add(record);
            }
        }

        return records;
    }
}
