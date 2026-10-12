
package com.traceback.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.traceback.util.DBConnection;

public class ComparisonHistoryDAO {

	public long saveComparison(long file1Id, long file2Id, boolean contentsMatch) throws SQLException {

		String sql = """
				INSERT INTO comparison_history
				(file1_id, file2_id, contents_match)
				VALUES (?, ?, ?)
				""";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			statement.setLong(1, file1Id);
			statement.setLong(2, file2Id);
			statement.setBoolean(3, contentsMatch);

			int affectedRows = statement.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Saving comparison failed; no row was inserted.");
			}

			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getLong(1);
				}
			}

			throw new SQLException("Saving comparison failed; no generated ID was returned.");
		}
	}

	public List<ComparisonRecord> getAllComparisons() throws SQLException {

		List<ComparisonRecord> records = new ArrayList<>();

		String sql = """
				SELECT ch.id,
				       ch.file1_id,
				       ch.file2_id,
				       ch.contents_match,
				       ch.compared_at,
				       f1.original_file_name AS file1_name,
				       f2.original_file_name AS file2_name
				FROM comparison_history ch
				LEFT JOIN file_analysis f1 ON ch.file1_id = f1.id
				LEFT JOIN file_analysis f2 ON ch.file2_id = f2.id
				ORDER BY ch.compared_at DESC, ch.id DESC
				""";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet rs = statement.executeQuery()) {

			while (rs.next()) {
				ComparisonRecord record = new ComparisonRecord();

				record.setId(rs.getLong("id"));
				record.setFile1Id(rs.getLong("file1_id"));
				record.setFile2Id(rs.getLong("file2_id"));
				record.setContentsMatch(rs.getBoolean("contents_match"));
				record.setComparedAt(rs.getTimestamp("compared_at"));
				record.setFile1Name(rs.getString("file1_name"));
				record.setFile2Name(rs.getString("file2_name"));

				records.add(record);
			}
		}

		return records;
	}

	public ComparisonRecord getComparisonById(long comparisonId) throws SQLException {

		String sql = """
				SELECT ch.id,
				       ch.file1_id,
				       ch.file2_id,
				       ch.contents_match,
				       ch.compared_at,
				       f1.original_file_name AS file1_name,
				       f2.original_file_name AS file2_name
				FROM comparison_history ch
				LEFT JOIN file_analysis f1 ON ch.file1_id = f1.id
				LEFT JOIN file_analysis f2 ON ch.file2_id = f2.id
				WHERE ch.id = ?
				""";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setLong(1, comparisonId);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					ComparisonRecord record = new ComparisonRecord();

					record.setId(rs.getLong("id"));
					record.setFile1Id(rs.getLong("file1_id"));
					record.setFile2Id(rs.getLong("file2_id"));
					record.setContentsMatch(rs.getBoolean("contents_match"));
					record.setComparedAt(rs.getTimestamp("compared_at"));
					record.setFile1Name(rs.getString("file1_name"));
					record.setFile2Name(rs.getString("file2_name"));

					return record;
				}
			}
		}

		return null;
	}

	public static class ComparisonRecord {

		private long id;
		private long file1Id;
		private long file2Id;
		private boolean contentsMatch;
		private Timestamp comparedAt;
		private String file1Name;
		private String file2Name;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public long getFile1Id() {
			return file1Id;
		}

		public void setFile1Id(long file1Id) {
			this.file1Id = file1Id;
		}

		public long getFile2Id() {
			return file2Id;
		}

		public void setFile2Id(long file2Id) {
			this.file2Id = file2Id;
		}

		public boolean isContentsMatch() {
			return contentsMatch;
		}

		public void setContentsMatch(boolean contentsMatch) {
			this.contentsMatch = contentsMatch;
		}

		public Timestamp getComparedAt() {
			return comparedAt;
		}

		public void setComparedAt(Timestamp comparedAt) {
			this.comparedAt = comparedAt;
		}

		public String getFile1Name() {
			return file1Name;
		}

		public void setFile1Name(String file1Name) {
			this.file1Name = file1Name;
		}

		public String getFile2Name() {
			return file2Name;
		}

		public void setFile2Name(String file2Name) {
			this.file2Name = file2Name;
		}
	}

	public int getTotalComparisons() throws SQLException {

		String sql = "SELECT COUNT(*) FROM comparison_history";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}

		return 0;
	}

	public int getMatchingComparisons() throws SQLException {

		String sql = "SELECT COUNT(*) FROM comparison_history " + "WHERE contents_match = TRUE";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}

		return 0;
	}

	public int getMismatchingComparisons() throws SQLException {

		String sql = "SELECT COUNT(*) FROM comparison_history " + "WHERE contents_match = FALSE";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		}

		return 0;
	}

}
