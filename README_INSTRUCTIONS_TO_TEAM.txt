[- Oracle -]
Open oracle_tables.sql and just execute the creation and insertion statements one by one

[- MongoDB -]
1) Setup a database called smartmove in MongoDB Compass
2) First collection should be feedback
3) insert this:
{
  "passenger_id": 1,
  "route_id": 1,
  "vehicle_id": 1,
  "driver_id": 1,
  "rating": 4,
  "comment": "Comfortable ride, driver was on time"
}

4) 2nd collection is vehicle_documents
{
  "vehicle_id": 1,
  "document_type": "Image",
  "file_name": "bus_v001_front.jpg",
  "path": "/vehicle_docs/bus_v001_front.jpg",
}

5) These are the queries:

-find feedback on a route
db.feedback.find({ route_id: 1 })

-highest-rated vehicles/drivers
db.feedback.aggregate([
  { $group: { _id: "$vehicle_id", avgRating: { $avg: "$rating" } } },
  { $sort: { avgRating: -1 } }
])

-find any complaints (feedback that contains the word late, can be replaced)
- (regex is Regular Expressions, can be used to find things in strings as far as I researched)
db.feedback.find({ comment: { $regex: "late", $options: "i" } })

-get documents for a vehicle
db.vehicle_documents.find({ vehicle_id: 1 })