
package controller;

import data.OracleConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TripController {

    public static class Trip {
        private final int id;
        private final String route;
        private final String vehicle;
        private final String driver;
        private final java.sql.Date date;
        private final String status;

        public Trip(int id, String route, String vehicle,
                    String driver, java.sql.Date date, String status) {
            this.id = id;
            this.route = route;
            this.vehicle = vehicle;
            this.driver = driver;
            this.date = date;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getRoute() {
            return route;
        }

        public String getVehicle() {
            return vehicle;
        }

        public String getDriver() {
            return driver;
        }

        public java.sql.Date getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }
    }

    // Retrieve trips, optionally filtering by the search text
    public List<Trip> getTrips(String searchText) throws SQLException {
        List<Trip> trips = new ArrayList<>();

        String term = "%" +
                (searchText == null ? "" : searchText.trim().toLowerCase()) +
                "%";

        String sql =
                "SELECT t.trip_id, " +
                        "r.start_location || ' - ' || r.destination AS route_name, " +
                        "v.license_number, " +
                        "d.driver_name, " +
                        "t.trip_date, " +
                        "t.status " +
                        "FROM TRIP t " +
                        "JOIN ROUTE r ON t.route_id = r.route_id " +
                        "JOIN VEHICLE v ON t.vehicle_id = v.vehicle_id " +
                        "JOIN DRIVER d ON t.driver_id = d.driver_id " +
                        "WHERE LOWER(r.start_location || ' - ' || r.destination) LIKE ? " +
                        "OR LOWER(v.license_number) LIKE ? " +
                        "OR LOWER(d.driver_name) LIKE ? " +
                        "OR LOWER(t.status) LIKE ? " +
                        "ORDER BY t.trip_date, t.trip_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, term);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(new Trip(
                            rs.getInt("trip_id"),
                            rs.getString("route_name"),
                            rs.getString("license_number"),
                            rs.getString("driver_name"),
                            rs.getDate("trip_date"),
                            rs.getString("status")
                    ));
                }
            }
        }

        return trips;
    }
}
