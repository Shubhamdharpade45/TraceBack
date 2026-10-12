
package com.traceback.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.traceback.model.FileAnalysis;
import com.traceback.util.DBConnection;

public class FileAnalysisDAO {

	// Save file analysis and return the generated database ID
	public long saveAnalysis(String fileName, String sha256, long fileSize, String mimeType) throws SQLException {

		String sql = """
				INSERT INTO file_analysis
				(original_file_name, hash_sha256, file_size_bytes,
				 mime_type, analysis_status)
				VALUES (?, ?, ?, ?, 'COMPLETED')
				""";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql,
						PreparedStatement.RETURN_GENERATED_KEYS)) {

			statement.setString(1, fileName);
			statement.setString(2, sha256);
			statement.setLong(3, fileSize);
			statement.setString(4, mimeType);

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted == 0) {
				throw new SQLException("Saving file analysis failed.");
			}

			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getLong(1);
				}
			}

			throw new SQLException("Database did not return the generated file ID.");
		}
	}

	// Fetch all file analysis records
	public List<FileAnalysis> getAllAnalyses() throws SQLException {

		List<FileAnalysis> analyses = new ArrayList<>();

		String sql = """
				SELECT id, original_file_name, hash_sha256,
				       file_size_bytes, mime_type, created_at
				FROM file_analysis
				ORDER BY created_at DESC, id DESC
				""";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

				FileAnalysis analysis = new FileAnalysis();

				analysis.setId(resultSet.getLong("id"));
				analysis.setOriginalFileName(resultSet.getString("original_file_name"));
				analysis.setHashSha256(resultSet.getString("hash_sha256"));
				analysis.setFileSizeBytes(resultSet.getLong("file_size_bytes"));
				analysis.setMimeType(resultSet.getString("mime_type"));
				analysis.setCreatedAt(resultSet.getTimestamp("created_at"));

				analyses.add(analysis);
			}
		}

		return analyses;
	}

	public FileAnalysis getAnalysisById(long id) throws SQLException {

		String sql = """
				SELECT id, original_file_name, hash_sha256,
				       file_size_bytes, mime_type, created_at
				FROM file_analysis
				WHERE id = ?
				""";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setLong(1, id);

			try (ResultSet rs = statement.executeQuery()) {

				if (rs.next()) {
					FileAnalysis analysis = new FileAnalysis();

					analysis.setId(rs.getLong("id"));
					analysis.setOriginalFileName(rs.getString("original_file_name"));
					analysis.setHashSha256(rs.getString("hash_sha256"));
					analysis.setFileSizeBytes(rs.getLong("file_size_bytes"));
					analysis.setMimeType(rs.getString("mime_type"));
					analysis.setCreatedAt(rs.getTimestamp("created_at"));

					return analysis;
				}
			}
		}

		return null;
	}

	public int getTotalAnalyses() throws SQLException {

		String sql = "SELECT COUNT(*) FROM file_analysis";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}

		return 0;
	}

	public int getUniqueHashCount() throws SQLException {

		String sql = "SELECT COUNT(DISTINCT hash_sha256) FROM file_analysis";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}

		return 0;
	}

	public int getDuplicateCount() throws SQLException {

		String sql = "SELECT COUNT(*) - COUNT(DISTINCT hash_sha256) " + "FROM file_analysis";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}

		return 0;
	}

	public List<FileAnalysis> getDuplicateAnalyses() throws SQLException {

		List<FileAnalysis> duplicateFiles = new ArrayList<>();

		String sql = "SELECT id, original_file_name, hash_sha256, " + "file_size_bytes, mime_type, created_at "
				+ "FROM file_analysis " + "WHERE hash_sha256 IN ( " + "    SELECT hash_sha256 "
				+ "    FROM file_analysis " + "    WHERE hash_sha256 IS NOT NULL " + "      AND hash_sha256 <> '' "
				+ "    GROUP BY hash_sha256 " + "    HAVING COUNT(*) > 1 " + ") " + "ORDER BY hash_sha256, id";

		try (Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				FileAnalysis file = new FileAnalysis();

				file.setId(rs.getLong("id"));
				file.setOriginalFileName(rs.getString("original_file_name"));
				file.setHashSha256(rs.getString("hash_sha256"));
				file.setFileSizeBytes(rs.getLong("file_size_bytes"));
				file.setMimeType(rs.getString("mime_type"));
				file.setCreatedAt(rs.getTimestamp("created_at"));

				duplicateFiles.add(file);
			}
		}

		return duplicateFiles;
	}

}
