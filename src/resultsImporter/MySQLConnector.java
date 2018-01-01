package resultsImporter;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnector {
	private static String username = "results";
	private static String password = "test";
	private static String db = "vysledky";
	private static int mysql_port = 3306;
	private ResultSet rs;
	private Connection conn = null;
	private Statement stmt = null;

	public MySQLConnector() {

		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
			return;
		}
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:"
					+ mysql_port + "/" + db + "?" + "user=" + username
					+ "&password=" + password
					+ "&useUnicode=true&characterEncoding=UTF-8");
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}

	}

	public PreparedStatement preparedQuery(String pq) throws SQLException {
		PreparedStatement preparedStmt = conn.prepareStatement(pq);
		return preparedStmt;
	}

	public void selectQuery(String sql) {
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void executeQuery(String sql) {
		try {
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ResultSet getResultSet() {
		return this.rs;
	}

	public void closeConn() throws SQLException {
		conn.close();
	}

}
