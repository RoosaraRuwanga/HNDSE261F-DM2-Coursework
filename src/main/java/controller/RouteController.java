package controller;

import data.OracleConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RouteController {

    public static class Route {
        private final int id;
        private final String start;
        private final String destination;

        public Route(int id, String start, String destination) {
            this.id = id;
            this.start = start;
            this.destination = destination;
        }

        public int getId() {
            return id;
        }

        public String getStart() {
            return start;
        }

        public String getDestination() {
            return destination;
        }
    }

    // Retrieve all routes from Oracle
    public List<Route> getAllRoutes() throws SQLException {
        List<Route> routes = new ArrayList<>();

        String sql =
                "SELECT route_id, start_location, destination " +
                        "FROM ROUTE ORDER BY route_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                routes.add(new Route(
                        rs.getInt("route_id"),
                        rs.getString("start_location"),
                        rs.getString("destination")
                ));
            }
        }

        return routes;
    }
}
