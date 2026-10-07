package controller;
import com.mongodb.client.*;
import data.MongoConnector;
import org.bson.Document;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;


// TODO: check if theres a way to make sure the ids in the feedback section are consistent with oracle
public class FeedbackController {

    public void submitFeedback(int passengerId, int routeId, int vehicleId, int driverId, int rating, String comment) {
        MongoCollection<Document> collection = MongoConnector.getDatabase().getCollection("feedback");

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