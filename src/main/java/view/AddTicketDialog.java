package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import data.OracleConnector;

public class AddTicketDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblPassenger;
    private JLabel lblTrip;
    private JLabel lblSeat;
    private JComboBox SelectSeat;
    private JLabel lblStatus;
    private JComboBox SelectStatus;
    private JComboBox selectPassenger;
    private JComboBox selectTrip;


    public AddTicketDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        SelectStatus.addItem("Booked");
        SelectStatus.addItem("Pending");
        loadPassengers();
        loadTrips();

        selectTrip.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAvailableSeats();
            }
        });

        updateAvailableSeats();

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTicket();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadPassengers() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT passenger_name FROM PASSENGER ORDER BY passenger_name");

            selectPassenger.removeAllItems();
            while (rs.next()) {
                selectPassenger.addItem(rs.getString("passenger_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading passengers: " + e.getMessage());
        } finally {
            closeConnections(conn, stmt, rs);
        }
    }

    private void loadTrips() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT trip_id FROM TRIP ORDER BY trip_id");

            selectTrip.removeAllItems();
            while (rs.next()) {
                selectTrip.addItem(rs.getString("trip_id"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading trips: " + e.getMessage());
        } finally {
            closeConnections(conn, stmt, rs);
        }
    }

    private void updateAvailableSeats() {
        SelectSeat.removeAllItems();
        if (selectTrip.getSelectedItem() == null) return;

        String selectedTripId = selectTrip.getSelectedItem().toString();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<String> takenSeats = new ArrayList<>();

        try {
            conn = OracleConnector.getConnection();
            String sql = "SELECT seat_no FROM TICKET WHERE trip_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(selectedTripId));
            rs = pstmt.executeQuery();

            while (rs.next()) {
                takenSeats.add(rs.getString("seat_no"));
            }

            for (int i = 1; i <= 26; i++) {
                String seatName = "A" + i;
                if (!takenSeats.contains(seatName)) {
                    SelectSeat.addItem(seatName);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading seats: " + e.getMessage());
        } finally {
            closeConnections(conn, pstmt, rs);
        }
    }

    private void saveTicket() {
        if (selectPassenger.getSelectedItem() == null || selectTrip.getSelectedItem() == null || SelectSeat.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please ensure all selections are filled.");
            return;
        }

        String passengerName = selectPassenger.getSelectedItem().toString();
        String tripIdStr = selectTrip.getSelectedItem().toString();
        String seatNo = SelectSeat.getSelectedItem().toString();
        String status = SelectStatus.getSelectedItem().toString();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = OracleConnector.getConnection();
            String getPassengerIdSql = "SELECT passenger_id FROM PASSENGER WHERE passenger_name = ?";
            pstmt = conn.prepareStatement(getPassengerIdSql);
            pstmt.setString(1, passengerName);
            rs = pstmt.executeQuery();

            int passengerId = -1;
            if (rs.next()) {
                passengerId = rs.getInt("passenger_id");
            }
            pstmt.close();
            String insertSql = "INSERT INTO TICKET (ticket_id, trip_id, passenger_id, seat_no, booking_date, status) " +
                    "VALUES ((SELECT NVL(MAX(ticket_id), 0) + 1 FROM TICKET), ?, ?, ?, SYSDATE, ?)";

            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, Integer.parseInt(tripIdStr));
            pstmt.setInt(2, passengerId);
            pstmt.setString(3, seatNo);
            pstmt.setString(4, status);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Ticket Successfully Booked!");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            closeConnections(conn, pstmt, rs);
        }
    }

    private void closeConnections(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            if (c != null) c.close();
        } catch (Exception e) {
        }
    }

}
