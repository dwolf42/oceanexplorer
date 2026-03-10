package explorer;

// Database connection parameters

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDB {
	static final String DB_URL = "jdbc:mariadb://localhost:3306/dvd_verleih";
	static final String USER = "root";
	static final String PASS = "password";

	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql = "SELECT * FROM kunden";
			rs = stmt.executeQuery(sql);

			// Extract data from result set
//			while (rs.next()) {
//				// Retrieve by column name
//				int id = rs.getInt("id");
//				String name = rs.getString("name");
//
//				// Display values
//				System.out.print("ID: " + id);
//				System.out.println(", Name: " + name);
//			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close resources
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// do nothing
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Database resources closed.");
		}
	}
}
