package controller;

import data.OracleConnector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceController {

    public static class Maintenance {
        private final int id;
        private final int vehicleId;
        private final Date date;
        private final String type;
        private final double cost;
        private final Date nextDue;
        private final String status;

        public Maintenance(int id, int vehicleId, Date date, String type,
                           double cost, Date nextDue, String status) {
            this.id = id;
            this.vehicleId = vehicleId;
            this.date = date;
            this.type = type;
            this.cost = cost;
            this.nextDue = nextDue;
            this.status = status;
        }

        public int getId()         { return id; }
        public int getVehicleId()  { return vehicleId; }
        public Date getDate()      { return date; }
        public String getType()    { return type; }
        public double getCost()    { return cost; }
        public Date getNextDue()   { return nextDue; }
        public String getStatus()  { return status; }
    }

    // Retrieve records, optionally filtered by a search term (matches type or status)
    public List<Maintenance> getRecords(String search) throws SQLException {
        List<Maintenance> list = new ArrayList<>();

        String sql = "SELECT maintenance_id, vehicle_id, maintenance_date, maintenance_type, "
                + "cost, next_due_date, maintenance_status "
                + "FROM MAINTENANCE "
                + "WHERE LOWER(maintenance_type) LIKE ? OR LOWER(maintenance_status) LIKE ? "
                + "ORDER BY maintenance_id";

        String term = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, term);
            ps.setString(2, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Maintenance(
                            rs.getInt("maintenance_id"),
                            rs.getInt("vehicle_id"),
                            rs.getDate("maintenance_date"),
                            rs.getString("maintenance_type"),
                            rs.getDouble("cost"),
                            rs.getDate("next_due_date"),
                            rs.getString("maintenance_status")
                    ));
                }
            }
        }
        return list;
    }

    // When we Insert a new record the ID is basically MAX(id) + 1, which is the same approach as I did in Driver and Vehicle
    public void addMaintenance(int vehicleId, Date date, String type,
                               double cost, Date nextDue, String status)
            throws SQLException {

        if (date == null) {
            throw new IllegalArgumentException("Maintenance date is required.");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Maintenance type is required.");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Cost cannot be negative.");
        }
        if (nextDue != null && nextDue.before(date)) {
            throw new IllegalArgumentException("Next due date cannot be before the maintenance date.");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required.");
        }
        // vehicle_id is a foreign key, so check the vehicle exists first
        if (!OracleConnector.value_exists("VEHICLE", "vehicle_id", vehicleId)) {
            throw new IllegalArgumentException("Vehicle ID " + vehicleId + " does not exist.");
        }

        String sql = "INSERT INTO MAINTENANCE "
                + "(MAINTENANCE_ID, VEHICLE_ID, MAINTENANCE_DATE, MAINTENANCE_TYPE, "
                + "COST, NEXT_DUE_DATE, MAINTENANCE_STATUS) "
                + "SELECT NVL(MAX(MAINTENANCE_ID), 0) + 1, ?, ?, ?, ?, ?, ? FROM MAINTENANCE";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vehicleId);
            ps.setDate(2, date);
            ps.setString(3, type.trim());
            ps.setDouble(4, cost);
            if (nextDue == null) {
                ps.setNull(5, Types.DATE);   // next_due_date is nullable
            } else {
                ps.setDate(5, nextDue);
            }
            ps.setString(6, status.trim());

            ps.executeUpdate();
        }
    }
}