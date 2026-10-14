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

<<<<<<< HEAD
        public Driver(int id, String name, String license,
                      String contact, String status) {
=======
        public Driver(int id, String name, String license, String contact, String status) {
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71
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
<<<<<<< HEAD
        String sql = "SELECT driver_id, driver_name, license_no, contact, status "
                + "FROM DRIVER "
                + "WHERE LOWER(driver_name) LIKE ? "
                + "OR LOWER(license_no) LIKE ? "
                + "ORDER BY driver_id";

        String term = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
=======
        String sql = "SELECT DRIVER_ID, DRIVER_NAME, LICENSE_NUMBER, CONTACT, STATUS FROM DRIVER " +
                "WHERE LOWER(DRIVER_NAME) LIKE ? OR LOWER(LICENSE_NUMBER) LIKE ? " +
                "ORDER BY DRIVER_ID";
        String term = "%" + search.trim().toLowerCase() + "%";
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71
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

<<<<<<< HEAD
        String sql = "INSERT INTO DRIVER "
                + "(driver_id, driver_name, license_no, contact, status) "
                + "SELECT NVL(MAX(driver_id), 0) + 1, ?, ?, ?, ? "
                + "FROM DRIVER";
=======
        // next ID = highest existing ID + 1, same trick as AddRouteDialog, where it jsut finds the finds all the ID's and add it in nicey, I want sleep
        String sql = "INSERT INTO DRIVER (DRIVER_ID, DRIVER_NAME, LICENSE_NUMBER, CONTACT, STATUS) " +
                "SELECT NVL(MAX(DRIVER_ID), 0) + 1, ?, ?, ?, ? FROM DRIVER";
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, license.trim());
            ps.setString(3, contact.trim());
            ps.setString(4, status);

            ps.executeUpdate();
        }
    }
}
