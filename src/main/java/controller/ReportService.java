package controller;

import java.sql.*;
import javax.swing.table.DefaultTableModel;

import data.OracleConnector;
import oracle.jdbc.OracleTypes;

public class ReportService {

    // REPORT 1 Popular Routes
    public DefaultTableModel getPopularRoutes() throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Start", "Destination", "Trip Count"}, 0);

        try (Connection conn = OracleConnector.getConnection();
             CallableStatement statement = conn.prepareCall("{call report_popular_routes(?)}")) {

            statement.registerOutParameter(1, OracleTypes.CURSOR);
            statement.execute();

            try (ResultSet result = (ResultSet) statement.getObject(1)) {
                while (result.next()) {
                    model.addRow(new Object[]{
                            result.getString("start_location"),
                            result.getString("destination"),
                            result.getInt("trip_count")
                    });
                }
            }
        }
        return model;
    }

    // REPORT 2 Revenue Report
    public DefaultTableModel getRevenue(Date startDate, Date endDate) throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Total Revenue"}, 0);

        try (Connection conn = OracleConnector.getConnection();
             CallableStatement statement = conn.prepareCall("{call report_revenue(?, ?, ?)}")) {

            statement.setDate(1, startDate);
            statement.setDate(2, endDate);
            statement.registerOutParameter(3, OracleTypes.CURSOR);
            statement.execute();

            try (ResultSet result = (ResultSet) statement.getObject(3)) {
                while (result.next()) {
                    model.addRow(new Object[]{ result.getDouble("total_revenue") });
                }
            }
        }
        return model;
    }

    // REPORT 3 Passenger History
    public DefaultTableModel getPassengerHistory(int passengerId) throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Trip ID", "Start", "Destination", "Date", "Status"}, 0);

        try (Connection conn = OracleConnector.getConnection();
             CallableStatement statement = conn.prepareCall("{call report_passenger_history(?, ?)}")) {

            statement.setInt(1, passengerId);
            statement.registerOutParameter(2, OracleTypes.CURSOR);
            statement.execute();

            try (ResultSet result = (ResultSet) statement.getObject(2)) {
                while (result.next()) {
                    model.addRow(new Object[]{
                            result.getInt("trip_id"),
                            result.getString("start_location"),
                            result.getString("destination"),
                            result.getDate("trip_date"),
                            result.getString("status")
                    });
                }
            }
        }
        return model;
    }

    // REPORT 4 Maintenance
    public DefaultTableModel getMaintenanceDue() throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Vehicle ID", "License Number", "Next Due Date"}, 0);

        try (Connection conn = OracleConnector.getConnection();
             CallableStatement statement = conn.prepareCall("{call report_maintenance_due(?)}")) {

            statement.registerOutParameter(1, OracleTypes.CURSOR);
            statement.execute();

            try (ResultSet result = (ResultSet) statement.getObject(1)) {
                while (result.next()) {
                    model.addRow(new Object[]{
                            result.getInt("vehicle_id"),
                            result.getString("license_number"),
                            result.getDate("next_due_date")
                    });
                }
            }
        }
        return model;
    }

    // note to other team members, you might need to use this?
    // to whoever is working on driver panel, just copy and paste then uncomment (remove /* and */) it if youre displaying it in a table

    /*
    public DefaultTableModel getDriverActivity() throws SQLException {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Driver Name", "Total Trips"}, 0);

        try (Connection conn = OracleConnector.getConnection();
             CallableStatement statement = conn.prepareCall("{call report_driver_activity(?)}")) {

            statement.registerOutParameter(1, OracleTypes.CURSOR);
            statement.execute();

            try (ResultSet result = (ResultSet) statement.getObject(1)) {
                while (result.next()) {
                    model.addRow(new Object[]{
                            result.getString("driver_name"),
                            result.getInt("total_trips")
                    });
                }
            }
        }
        return model;
    }
    */
}