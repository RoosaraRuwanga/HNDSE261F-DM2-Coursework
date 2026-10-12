package controller;

import data.OracleConnector;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PassengerController {

    // the team's report procedures live in ReportController
    private final ReportController reports = new ReportController();

    /** Table rows {id, name, contact, tickets}. */
    public List<Object[]> getPassengers(String search) throws SQLException {
        String sql = "SELECT p.passenger_id, p.passenger_name, p.contact, COUNT(tk.ticket_id) "
                + "FROM PASSENGER p LEFT JOIN TICKET tk ON tk.passenger_id = p.passenger_id "
                + "WHERE LOWER(p.passenger_name) LIKE ? "
                + "GROUP BY p.passenger_id, p.passenger_name, p.contact "
                + "ORDER BY p.passenger_id";

        List<Object[]> rows = new ArrayList<>();
        try (Connection c = OracleConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + search.trim().toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
                }
            }
        }
        return rows;
    }

    /** Travel history for one passenger (runs the report_passenger_history procedure). */
    public DefaultTableModel getPassengerHistory(int passengerId) throws SQLException {
        return reports.getPassengerHistory(passengerId);
    }

    public void addPassenger(String name, String contact) throws SQLException {
        validate(null, name, contact);
        execute("INSERT INTO PASSENGER (passenger_id, passenger_name, contact) "
                        + "SELECT NVL(MAX(passenger_id), 0) + 1, ?, ? FROM PASSENGER",
                name.trim(), contact.trim());
    }

    public void updatePassenger(int id, String name, String contact) throws SQLException {
        validate(id, name, contact);
        execute("UPDATE PASSENGER SET passenger_name = ?, contact = ? WHERE passenger_id = ?",
                name.trim(), contact.trim(), id);
    }

    public void deletePassenger(int id) throws SQLException {
        try {
            execute("DELETE FROM PASSENGER WHERE passenger_id = ?", id);
        } catch (SQLException ex) {
            // ORA-02292: child record found (TICKET rows reference this passenger)
            if (ex.getErrorCode() == 2292) {
                throw new IllegalStateException("Cannot delete a passenger who has tickets.");
            }
            throw ex;
        }
    }

    // ---- helpers ----

    // business rules, enforced here in Java: required fields, no duplicate contact number
    private void validate(Integer id, String name, String contact) throws SQLException {
        if (name == null || name.isBlank() || contact == null || contact.isBlank()) {
            throw new IllegalArgumentException("Name and contact are required.");
        }

        String sql = "SELECT COUNT(*) FROM PASSENGER WHERE contact = ? AND passenger_id <> ?";
        try (Connection c = OracleConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, contact.trim());
            ps.setInt(2, id == null ? -1 : id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Another passenger already uses this contact number.");
                }
            }
        }
    }

    private void execute(String sql, Object... params) throws SQLException {
        try (Connection c = OracleConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }
}