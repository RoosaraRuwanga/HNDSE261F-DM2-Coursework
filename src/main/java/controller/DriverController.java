package controller;

import data.OracleConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DriverController {

    public static class Driver {
        private final int id;
        private final String name, license, contact, status;

        public Driver(int id, String name, String license,
                      String contact, String status) {
            this.id = id;
            this.name = name;
            this.license = license;
            this.contact = contact;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLicense() {
            return license;
        }

        public String getContact() {
            return contact;
        }

        public String getStatus() {
            return status;
        }
    }

    public List<Driver> getDrivers(String search) throws SQLException {
        String sql = "SELECT driver_id, driver_name, license_no, contact, status "
                + "FROM DRIVER "
                + "WHERE LOWER(driver_name) LIKE ? "
                + "OR LOWER(license_no) LIKE ? "
                + "ORDER BY driver_id";

        String term = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";

        List<Driver> list = new ArrayList<>();

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, term);
            ps.setString(2, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Driver(
                            rs.getInt("driver_id"),
                            rs.getString("driver_name"),
                            rs.getString("license_no"),
                            rs.getString("contact"),
                            rs.getString("status")
                    ));
                }
            }
        }

        return list;
    }

    public void addDriver(String name, String license,
                          String contact, String status)
            throws SQLException {

        if (name == null || name.isBlank()
                || license == null || license.isBlank()
                || contact == null || contact.isBlank()) {
            throw new IllegalArgumentException(
                    "Name, licence and contact are required."
            );
        }

        // next ID = highest existing ID + 1, same approach as AddRouteDialog
        String sql = "INSERT INTO DRIVER "
                + "(driver_id, driver_name, license_no, contact, status) "
                + "SELECT NVL(MAX(driver_id), 0) + 1, ?, ?, ?, ? "
                + "FROM DRIVER";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, license.trim());
            ps.setString(3, contact.trim());
            ps.setString(4, status);

            ps.executeUpdate();
        }
    }


    //Newly Added
    public void updateDriver(int id, String name, String license,
                             String contact, String status)
            throws SQLException {

        if (name == null || name.isBlank()
                || license == null || license.isBlank()
                || contact == null || contact.isBlank()) {
            throw new IllegalArgumentException(
                    "Name, licence and contact are required."
            );
        }

        String sql = "UPDATE DRIVER "
                + "SET driver_name = ?, license_no = ?, contact = ?, status = ? "
                + "WHERE driver_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, license.trim());
            ps.setString(3, contact.trim());
            ps.setString(4, status);
            ps.setInt(5, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("This driver no longer exists.");
            }
        }
    }

    public void deleteDriver(int id) throws SQLException {
        String sql = "DELETE FROM DRIVER WHERE driver_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("This driver no longer exists.");
            }

        } catch (SQLException ex) {
            // ORA-02292: child record found (the driver is used by a TRIP)
            if (ex.getErrorCode() == 2292) {
                throw new IllegalArgumentException(
                        "This driver is assigned to one or more trips and cannot be deleted.\n"
                                + "Set the status to Inactive instead.");
            }
            throw ex;
        }
    }
}