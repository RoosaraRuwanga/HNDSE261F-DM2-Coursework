package controller;
import com.mongodb.client.*;
import data.MongoConnector;
import data.OracleConnector;
import org.bson.Document;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class FeedbackController {

    public void submitFeedback(int passengerId, int routeId, int vehicleId, int driverId, int rating, String comment) throws SQLException {
        MongoCollection<Document> collection = MongoConnector.getDatabase().getCollection("feedback");

        // safety check
        if (!OracleConnector.value_exists("Passenger", "passenger_id", passengerId)) {
            throw new IllegalArgumentException("Passenger ID " + passengerId + " does not exist.");
        }
        if (!OracleConnector.value_exists("Route", "route_id", routeId)) {
            throw new IllegalArgumentException("Route ID " + routeId + " does not exist.");
        }
        if (!OracleConnector.value_exists("Vehicle", "vehicle_id", vehicleId)) {
            throw new IllegalArgumentException("Vehicle ID " + vehicleId + " does not exist.");
        }
        if (!OracleConnector.value_exists("Driver", "driver_id", driverId)) {
            throw new IllegalArgumentException("Driver ID " + driverId + " does not exist.");
        }


        // probably should space this out more later like i do for my hobby stuff, i just want this to work first of all
        Document feedback =
                new Document("passenger_id", passengerId).append("route_id", routeId).append("vehicle_id", vehicleId)
                .append("driver_id", driverId).append("rating", rating).append("comment", comment);

        collection.insertOne(feedback);
    }

    public DefaultTableModel getFeedbackByRoute(int routeId) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Passenger ID", "Rating", "Comment"}, 0);

        MongoCollection<Document> collection = MongoConnector.getDatabase().getCollection("feedback");
        FindIterable<Document> results = collection.find(new Document("route_id", routeId));

        for (Document doc : results) {
            model.addRow(new Object[]{doc.getInteger("passenger_id"), doc.getInteger("rating"),
                    doc.getString("comment")});
        }
        return model;
    }

    public DefaultTableModel searchFeedbackByKeyword(String keyword) {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Passenger ID", "Route ID", "Rating", "Comment"}, 0);

        // using regex again because no clue how else to search strings like outlined in the doc
        MongoCollection<Document> collection = MongoConnector.getDatabase().getCollection("feedback");
        Document regexQuery = new Document("comment", new Document("$regex", keyword).append("$options", "i"));

        FindIterable<Document> results = collection.find(regexQuery);

        for (Document doc : results) {
            model.addRow(new Object[]{doc.getInteger("passenger_id"), doc.getInteger("route_id"),
                    doc.getInteger("rating"), doc.getString("comment")});
        }
        return model;
    }

    public DefaultTableModel getHighestRated() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Vehicle ID", "Average Rating"}, 0);

        MongoCollection<Document> collection = MongoConnector.getDatabase().getCollection("feedback");

        List<Document> results = List.of(
                new Document("$group", new Document("_id", "$vehicle_id").append("avgRating", new Document("$avg", "$rating"))),
                new Document("$sort", new Document("avgRating", -1))
        );

        for (Document doc : collection.aggregate(results)) {
            model.addRow(new Object[]{doc.getInteger("_id"), doc.getDouble("avgRating")});
        }
        return model;
    }
}