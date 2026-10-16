require('dotenv').config();
const express = require('express');
const cors = require('cors');
const oracledb = require('oracledb');
const { MongoClient } = require('mongodb');

oracledb.outFormat = oracledb.OUT_FORMAT_OBJECT;

const app = express();
app.use(cors());
app.use(express.json());

// ---------- MongoDB (feedback is saved here) ----------
const mongo = new MongoClient(process.env.MONGO_URI || 'mongodb://localhost:27017');
const feedbackCol = () => mongo.db('smartmove').collection('feedback');

// ---------- Oracle (dropdown data) ----------
async function run(sql, binds = {}, options = {}) {
  let conn;
  try {
    conn = await oracledb.getConnection({
      user: process.env.ORACLE_USER,
      password: process.env.ORACLE_PASSWORD,
      connectString: process.env.ORACLE_CONNECT,
    });
    return await conn.execute(sql, binds, options);
  } finally {
    if (conn) await conn.close();
  }
}

app.get('/api/routes', async (req, res) => {
  try {
    const r = await run(
      `SELECT route_id, start_location, destination FROM ROUTE ORDER BY route_id`
    );
    res.json(r.rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/vehicles', async (req, res) => {
  try {
    const r = await run(
      `SELECT vehicle_id, vehicle_type, license_number FROM VEHICLE ORDER BY vehicle_id`
    );
    res.json(r.rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

app.get('/api/drivers', async (req, res) => {
  try {
    const r = await run(
      `SELECT driver_id, driver_name FROM DRIVER ORDER BY driver_id`
    );
    res.json(r.rows);
  } catch (e) { res.status(500).json({ error: e.message }); }
});

// ---------- Submit feedback (saved to MongoDB) ----------
app.post('/api/feedback', async (req, res) => {
  const { passengerId, routeId, vehicleId, driverId, rating, comment } = req.body;

  if (![passengerId, routeId, vehicleId, driverId].every(Number.isInteger)) {
    return res.status(400).json({ error: 'Passenger, route, vehicle and driver are required.' });
  }
  if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
    return res.status(400).json({ error: 'Rating must be between 1 and 5.' });
  }
  if (!comment || !comment.trim()) {
    return res.status(400).json({ error: 'Please write a comment.' });
  }

  try {
    // same safety check as the Java app: every ID must exist in Oracle
    const checks = [
      ['PASSENGER', 'passenger_id', passengerId, 'Passenger'],
      ['ROUTE', 'route_id', routeId, 'Route'],
      ['VEHICLE', 'vehicle_id', vehicleId, 'Vehicle'],
      ['DRIVER', 'driver_id', driverId, 'Driver'],
    ];
    for (const [table, col, id, label] of checks) {
      const r = await run(`SELECT 1 FROM ${table} WHERE ${col} = :id`, { id });
      if (!r.rows.length) {
        return res.status(400).json({ error: `${label} ID ${id} does not exist.` });
      }
    }

    // field names must match what the Java app reads
    await feedbackCol().insertOne({
      passenger_id: passengerId,
      route_id: routeId,
      vehicle_id: vehicleId,
      driver_id: driverId,
      rating,
      comment: comment.trim(),
    });

    res.status(201).json({ message: 'Thank you for your feedback!' });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: e.message });
  }
});

// ---------- Start ----------
const PORT = process.env.PORT || 3000;
mongo.connect()
  .then(() => app.listen(PORT, () => console.log(`API running on http://localhost:${PORT}`)))
  .catch(err => console.error('MongoDB connection failed:', err.message));