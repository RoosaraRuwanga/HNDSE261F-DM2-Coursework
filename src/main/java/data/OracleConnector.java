package data;

import java.sql.*;

public class OracleConnector {
    private static final String URL = "jdbc:oracle:thin:@10.247.66.112:1521:XE";
    private static final String USER = "SYSTEM";
    private static final String PASS = "274123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    // used to make sure MongoDB insertions are consistent with oracle's values
    public static boolean value_exists(String table, String idColumn, int id) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE " + idColumn + " = ?";

        try (Connection conn = OracleConnector.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}