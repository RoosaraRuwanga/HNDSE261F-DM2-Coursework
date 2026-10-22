package controller;

import data.OracleConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleController {

        public static class Vehicle {
        private final int id;
        private final String type;
        private final String license;
        private final int seats;
        private final String status;

        public Vehicle(int id, String type, String license, int seats, String status) {
            this.id = id;
            this.type = type;
            this.license = license;
            this.seats = seats;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getLicense() {
            return license;
        }

        public int getSeats() {
            return seats;
        }

        public String getStatus() {
            return status;
        }
    }

    // Retrieve vehicles, optionally filtered by a search term (matches type or license plate)
    public List<Vehicle> getVehicles(String search) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();

        String sql =
                "SELECT vehicle_id, vehicle_type, license_number, available_seats, status " +
                        "FROM VEHICLE " +
                        "WHERE UPPER(vehicle_type) LIKE UPPER(?) OR UPPER(license_number) LIKE UPPER(?) " +
                        "ORDER BY vehicle_id";

        String term = "%" + (search == null ? "" : search.trim()) + "%";

        //This will create the database connection and ps is used to prevent any SQL injection
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, term);
            ps.setString(2, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(new Vehicle(
                            rs.getInt("vehicle_id"),
                            rs.getString("vehicle_type"),
                            rs.getString("license_number"),
                            rs.getInt("available_seats"),
                            rs.getString("status")
                    ));
                }
            }
        }

        return vehicles;
    }

    // Insert a new vehicle; the ID is auto-generated (MAX(id) + 1, no sequence needed)
    public void addVehicle(String type, String license, int seats, String status) throws SQLException {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type is required.");
        }
        if (license == null || license.trim().isEmpty()) {
            throw new IllegalArgumentException("License number is required.");
        }
        if (seats < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative.");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required.");
        }

        String sql =
                "INSERT INTO VEHICLE (vehicle_id, vehicle_type, license_number, available_seats, status) " +
                        "SELECT NVL(MAX(vehicle_id), 0) + 1, ?, ?, ?, ? FROM VEHICLE";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type.trim());
            ps.setString(2, license.trim());
            ps.setInt(3, seats);
            ps.setString(4, status.trim());
            ps.executeUpdate();
        }
    }

    public void updateVehicle(int id, String type, String license, int seats, String status)
            throws SQLException {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle type is required.");
        }
        if (license == null || license.trim().isEmpty()) {
            throw new IllegalArgumentException("License number is required.");
        }
        if (seats < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative.");
        }

        String sql = "UPDATE VEHICLE SET vehicle_type = ?, license_number = ?, "
                + "available_seats = ?, status = ? WHERE vehicle_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, type.trim());
            ps.setString(2, license.trim());
            ps.setInt(3, seats);
            ps.setString(4, status);
            ps.setInt(5, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("This vehicle no longer exists.");
            }
        }
    }

    public void deleteVehicle(int id) throws SQLException {
        String sql = "DELETE FROM VEHICLE WHERE vehicle_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("This vehicle no longer exists.");
            }

        } catch (SQLException ex) {
            // ORA-02292: the vehicle is used by a TRIP or a MAINTENANCE record
            if (ex.getErrorCode() == 2292) {
                throw new IllegalArgumentException(
                        "This vehicle is used by trips or maintenance records and cannot be deleted.\n"
                                + "Set its status to Inactive instead.");
            }
            throw ex;
        }
    }
}
