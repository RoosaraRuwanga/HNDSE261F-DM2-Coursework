import { useEffect, useState } from 'react';
import './App.css';

const API = 'http://localhost:3000/api';

const emptyForm = {
  passengerId: '',
  routeId: '',
  vehicleId: '',
  driverId: '',
  rating: 0,
  comment: '',
};

export default function App() {
  const [routes, setRoutes] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [msg, setMsg] = useState(null);

  useEffect(() => {
    Promise.all([
      fetch(`${API}/routes`).then(r => r.json()),
      fetch(`${API}/vehicles`).then(r => r.json()),
      fetch(`${API}/drivers`).then(r => r.json()),
    ])
      .then(([r, v, d]) => {
        setRoutes(r);
        setVehicles(v);
        setDrivers(d);
      })
      .catch(() =>
        setMsg({ type: 'error', text: 'Could not load form data.' })
      );
  }, []);

  const set = (key, value) => setForm(f => ({ ...f, [key]: value }));

  async function submit(e) {
    e.preventDefault();
    setMsg(null);

    const body = {
      passengerId: parseInt(form.passengerId, 10),
      routeId: parseInt(form.routeId, 10),
      vehicleId: parseInt(form.vehicleId, 10),
      driverId: parseInt(form.driverId, 10),
      rating: form.rating,
      comment: form.comment,
    };

    try {
      const res = await fetch(`${API}/feedback`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error);

      setMsg({ type: 'ok', text: data.message });
      setForm(emptyForm);
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <h1>SmartMove Feedback</h1>
      <p className="sub">Tell us about your trip.</p>

      <label>
        Passenger ID
        <input
          type="number"
          min="1"
          required
          value={form.passengerId}
          onChange={e => set('passengerId', e.target.value)}
        />
      </label>

      <label>
        Route
        <select
          required
          value={form.routeId}
          onChange={e => set('routeId', e.target.value)}
        >
          <option value="">Select a route</option>
          {routes.map(r => (
            <option key={r.ROUTE_ID} value={r.ROUTE_ID}>
              {r.START_LOCATION} → {r.DESTINATION}
            </option>
          ))}
        </select>
      </label>

      <label>
        Vehicle
        <select
          required
          value={form.vehicleId}
          onChange={e => set('vehicleId', e.target.value)}
        >
          <option value="">Select a vehicle</option>
          {vehicles.map(v => (
            <option key={v.VEHICLE_ID} value={v.VEHICLE_ID}>
              {v.VEHICLE_TYPE} - {v.LICENSE_NUMBER}
            </option>
          ))}
        </select>
      </label>

      <label>
        Driver
        <select
          required
          value={form.driverId}
          onChange={e => set('driverId', e.target.value)}
        >
          <option value="">Select a driver</option>
          {drivers.map(d => (
            <option key={d.DRIVER_ID} value={d.DRIVER_ID}>
              {d.DRIVER_NAME}
            </option>
          ))}
        </select>
      </label>

      <div className="label">Rating</div>
      <div className="stars">
        {[1, 2, 3, 4, 5].map(n => (
          <button
            type="button"
            key={n}
            className={n <= form.rating ? 'star on' : 'star'}
            onClick={() => set('rating', n)}
            aria-label={`${n} stars`}
          >
            ★
          </button>
        ))}
      </div>

      <label>
        Comment
        <textarea
          rows="4"
          required
          value={form.comment}
          onChange={e => set('comment', e.target.value)}
        />
      </label>

      <button className="submit" type="submit" disabled={!form.rating}>
        Submit Feedback
      </button>

      {msg && <p className={`msg ${msg.type}`}>{msg.text}</p>}
    </form>
  );
}