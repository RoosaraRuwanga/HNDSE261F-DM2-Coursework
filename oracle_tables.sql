-- !!! USE ONLY IN CASE OF EMERGENCY
DROP TABLE VEHICLE CASCADE CONSTRAINTS PURGE;
DROP TABLE DRIVER CASCADE CONSTRAINTS PURGE;
DROP TABLE ROUTE CASCADE CONSTRAINTS PURGE;
DROP TABLE PASSENGERS CASCADE CONSTRAINTS PURGE;
DROP TABLE TRIP CASCADE CONSTRAINTS PURGE;
DROP TABLE TICKET CASCADE CONSTRAINTS PURGE;
DROP TABLE PAYMENT CASCADE CONSTRAINTS PURGE;
DROP TABLE MAINTENANCE CASCADE CONSTRAINTS PURGE;

CREATE TABLE VEHICLE (
    vehicle_id NUMBER PRIMARY KEY,
    vehicle_type VARCHAR2(30),
    license_number VARCHAR2(20),
    available_seats NUMBER,
    status VARCHAR2(20)
);
CREATE TABLE DRIVER (
    driver_id NUMBER PRIMARY KEY,
    driver_name VARCHAR2(100),
    contact VARCHAR2(20),
    status VARCHAR2(20)
);
CREATE TABLE ROUTE (
    route_id NUMBER PRIMARY KEY,
    start_location VARCHAR2(100),
    destination VARCHAR2(100)
);
CREATE TABLE PASSENGER (
    passenger_id NUMBER PRIMARY KEY,
    passenger_name VARCHAR2(100),
    contact VARCHAR2(20)
);
CREATE TABLE TRIP (
    trip_id NUMBER PRIMARY KEY,
    route_id NUMBER REFERENCES ROUTE(route_id),
    vehicle_id NUMBER REFERENCES VEHICLE(vehicle_id),
    driver_id NUMBER REFERENCES DRIVER(driver_id),
    trip_date DATE,
    status VARCHAR2(20)
);
CREATE TABLE TICKET (
    ticket_id NUMBER PRIMARY KEY,
    trip_id NUMBER REFERENCES TRIP(trip_id),
    passenger_id NUMBER REFERENCES PASSENGER(passenger_id),
    seat_no VARCHAR2(10),
    booking_date DATE,
    status VARCHAR2(20)
);
CREATE TABLE PAYMENT (
    payment_id NUMBER PRIMARY KEY,
    ticket_id NUMBER REFERENCES TICKET(ticket_id),
    amount NUMBER(10,2),
    payment_date DATE
);
CREATE TABLE MAINTENANCE (
    maintenance_id NUMBER PRIMARY KEY,
    vehicle_id NUMBER REFERENCES VEHICLE(vehicle_id),
    maintenance_date DATE,
    maintenance_type VARCHAR2(50),
    cost NUMBER(10,2),
    next_due_date DATE
);
-- insertion
INSERT INTO VEHICLE VALUES (1, 'Bus', 'V001', 40, 'Active');
INSERT INTO VEHICLE VALUES (2, 'Van', 'V002', 15, 'Active');
INSERT INTO VEHICLE VALUES (3, 'Bus', 'V003', 40, 'Under Maintenance');

INSERT INTO DRIVER VALUES (1, 'John', '0771234567', 'Active');
INSERT INTO DRIVER VALUES (2, 'Amara', '0779876543', 'Active');
INSERT INTO DRIVER VALUES (3, 'Ravi', '0775551234', 'Active');

INSERT INTO ROUTE VALUES (1, 'Colombo', 'Kandy');
INSERT INTO ROUTE VALUES (2, 'Colombo', 'Galle');
INSERT INTO ROUTE VALUES (3, 'Kandy', 'Jaffna');

INSERT INTO PASSENGER VALUES (1, 'Thevan', '0711112222');
INSERT INTO PASSENGER VALUES (2, 'Roosara', '0701163365');
INSERT INTO PASSENGER VALUES (3, 'Gaveen', '0715556666');
INSERT INTO PASSENGER VALUES (4, 'Mona', '0717778888');

INSERT INTO TRIP VALUES (1, 1, 1, 1, DATE '2026-09-10', 'Completed');
INSERT INTO TRIP VALUES (2, 2, 2, 2, DATE '2026-09-12', 'Completed');
INSERT INTO TRIP VALUES (3, 3, 1, 3, DATE '2026-09-14', 'Completed');
INSERT INTO TRIP VALUES (4, 1, 2, 2, DATE '2026-09-20', 'Scheduled');

INSERT INTO TICKET VALUES (1, 1, 1, 'A1', DATE '2026-09-09', 'Booked');
INSERT INTO TICKET VALUES (2, 2, 2, 'A2', DATE '2026-09-11', 'Booked');
INSERT INTO TICKET VALUES (3, 3, 3, 'A3', DATE '2026-09-13', 'Booked');
INSERT INTO TICKET VALUES (4, 4, 4, 'A4', DATE '2026-09-19', 'Booked');

INSERT INTO PAYMENT VALUES (1, 1, 1200.00, DATE '2026-09-10');
INSERT INTO PAYMENT VALUES (2, 2, 1500.00, DATE '2026-09-12');
INSERT INTO PAYMENT VALUES (3, 3, 2000.00, DATE '2026-09-14');

INSERT INTO MAINTENANCE VALUES (1, 3, DATE '2026-09-05', 'Engine Service', 5000.00, DATE '2026-12-05');
INSERT INTO MAINTENANCE VALUES (2, 1, DATE '2026-08-20', 'Tire Change', 1500.00, DATE '2027-02-20');

-- reports using PSQL and SYS_REFCURSOR, 
-- jasper reports too complicated to setup rn, though i think this is what we want either way??
CREATE OR REPLACE PROCEDURE report_popular_routes(p_cursor OUT SYS_REFCURSOR) AS
BEGIN
    OPEN p_cursor FOR
        SELECT r.start_location, r.destination, COUNT(t.trip_id) AS trip_count
        FROM ROUTE r
        JOIN TRIP t ON r.route_id = t.route_id
        JOIN TICKET tk ON t.trip_id = tk.trip_id
        GROUP BY r.start_location, r.destination
        ORDER BY trip_count DESC;
END;

CREATE OR REPLACE PROCEDURE report_revenue(p_start_date IN DATE, p_end_date IN DATE, p_cursor OUT SYS_REFCURSOR) AS
BEGIN
    OPEN p_cursor FOR
        SELECT NVL(SUM(amount), 0) AS total_revenue
        FROM PAYMENT
        WHERE payment_date BETWEEN p_start_date AND p_end_date;
END;

CREATE OR REPLACE PROCEDURE report_passenger_history(p_passenger_id IN NUMBER, p_cursor OUT SYS_REFCURSOR) AS
BEGIN
    OPEN p_cursor FOR
        SELECT t.trip_id, r.start_location, r.destination, t.trip_date, tk.status
        FROM TICKET tk
        JOIN TRIP t ON tk.trip_id = t.trip_id
        JOIN ROUTE r ON t.route_id = r.route_id
        WHERE tk.passenger_id = p_passenger_id
        ORDER BY t.trip_date;
END;

CREATE OR REPLACE PROCEDURE report_maintenance_due(p_cursor OUT SYS_REFCURSOR) AS
BEGIN
    OPEN p_cursor FOR
        SELECT v.vehicle_id, v.license_number, m.next_due_date
        FROM VEHICLE v
        JOIN MAINTENANCE m ON v.vehicle_id = m.vehicle_id
        WHERE m.next_due_date <= SYSDATE + 30
        ORDER BY m.next_due_date;
END;

CREATE OR REPLACE PROCEDURE report_driver_activity(p_cursor OUT SYS_REFCURSOR) AS
BEGIN
    OPEN p_cursor FOR
        SELECT d.driver_name, COUNT(t.trip_id) AS total_trips
        FROM DRIVER d
        JOIN TRIP t ON d.driver_id = t.driver_id
        WHERE t.status = 'Completed'
        GROUP BY d.driver_name
        ORDER BY total_trips DESC;
END;

-- testing code
VAR c REFCURSOR;
EXEC report_popular_routes(:c);
PRINT c;

-- trigger to prevent overbooking (vehicle does not have enough seats for trip)
-- Minor error on the last trigger was that:
-- seats_available >= seats_booked shows that there's still room
      -- where as we want to prevent overbooking
      -- So I replaced that with seats_booked >= seats_available
CREATE OR REPLACE TRIGGER trg_prevent_overbooking
BEFORE INSERT ON TICKET
FOR EACH ROW
DECLARE
seats_available NUMBER;
    seats_booked NUMBER;
BEGIN
    -- seat capacity for the vehicle assigned to this trip
SELECT v.available_seats INTO seats_available
FROM VEHICLE v
         JOIN TRIP t ON v.vehicle_id = t.vehicle_id
WHERE t.trip_id = :NEW.trip_id;

-- tickets already booked on this trip
SELECT COUNT(*) INTO seats_booked
FROM TICKET
WHERE trip_id = :NEW.trip_id;

IF seats_booked >= seats_available THEN
        RAISE_APPLICATION_ERROR(-20001, 'Booking failed: trip is fully booked.');
END IF;
-- incorrect trip id given
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20002, 'Booking failed: invalid trip ID.');
END;