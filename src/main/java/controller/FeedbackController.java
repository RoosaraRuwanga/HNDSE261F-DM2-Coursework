package controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import data.MongoConnector;
import data.OracleConnector;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FeedbackController {

    public static final String[] COLUMNS =
            {"Date", "Passenger", "Route", "Vehicle", "Driver", "Rating", "Comment"};

    // Item for the route dropdown
    public static class RouteOption {
        public static final RouteOption ALL = new RouteOption(0, "All routes");

        public final int id;
        private final String label;

        public RouteOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Routes for the dropdown
    public List<RouteOption> getRouteOptions() throws SQLException {
        List<RouteOption> list = new ArrayList<>();
        Map<Integer, String> routes = lookup(
                "SELECT route_id, start_location || ' - ' || destination FROM ROUTE ORDER BY route_id");
        for (Map.Entry<Integer, String> e : routes.entrySet()) {
            list.add(new RouteOption(e.getKey(), e.getValue()));
        }
        list.sort((a, b) -> Integer.compare(a.id, b.id));
        return list;
    }

    // Feedback table. routeId 0 = all routes, rating 0 = all ratings.
    public DefaultTableModel getFeedback(String keyword, int routeId, int rating)
            throws SQLException {

        DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // admins cannot change a passenger's response
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 5 ? Integer.class : String.class;
            }
        };

        // names come from Oracle, feedback comes from MongoDB
        Map<Integer, String> passengers = lookup(
                "SELECT passenger_id, passenger_name FROM PASSENGER");
        Map<Integer, String> routes = lookup(
                "SELECT route_id, start_location || ' - ' || destination FROM ROUTE");
        Map<Integer, String> vehicles = lookup(
                "SELECT vehicle_id, vehicle_type || ' ' || license_number FROM VEHICLE");
        Map<Integer, String> drivers = lookup(
                "SELECT driver_id, driver_name FROM DRIVER");

        List<Bson> filters = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            filters.add(Filters.regex("comment", Pattern.quote(keyword.trim()), "i"));
        }
        if (routeId > 0) {
            // also matches older documents saved with camelCase names
            filters.add(Filters.or(
                    Filters.eq("route_id", routeId),
                    Filters.eq("routeId", routeId)));
        }
        if (rating > 0) {
            filters.add(Filters.eq("rating", rating));
        }
        Bson query = filters.isEmpty() ? new Document() : Filters.and(filters);

        MongoCollection<Document> collection =
                MongoConnector.getDatabase().getCollection("feedback");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // newest first (an ObjectId starts with its creation time)
        for (Document doc : collection.find(query).sort(Sorts.descending("_id"))) {
            Object id = doc.get("_id");
            String date = id instanceof ObjectId ? fmt.format(((ObjectId) id).getDate()) : "";

            model.addRow(new Object[]{
                    date,
                    label(passengers, num(doc, "passenger_id", "passengerId")),
                    label(routes, num(doc, "route_id", "routeId")),
                    label(vehicles, num(doc, "vehicle_id", "vehicleId")),
                    label(drivers, num(doc, "driver_id", "driverId")),
                    num(doc, "rating"),
                    doc.getString("comment") == null ? "" : doc.getString("comment")
            });
        }
        return model;
    }

    // ---- helpers ----

    // reads the first key that exists (supports snake_case and camelCase documents)
    private static int num(Document doc, String... keys) {
        for (String key : keys) {
            Object value = doc.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 0;
    }

    private static String label(Map<Integer, String> map, int id) {
        return map.getOrDefault(id, "#" + id);
    }

    private Map<Integer, String> lookup(String sql) throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt(1), rs.getString(2));
            }
        }
        return map;
    }
}