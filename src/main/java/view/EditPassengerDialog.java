package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditPassengerDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblName;
    private JLabel lblContact;
    private JLabel lblTickets;
    private JLabel lblID;
    private JComboBox selectID;
    private JTextField txtName;
    private JTextField txtContact;
    private JTextField txtTickets;
    private boolean confirmed = false;

    public EditPassengerDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSave);
        txtTickets.setEditable(true);
        selectID.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer selectedId = (Integer) selectID.getSelectedItem();
                if (selectedId != null) {
                    loadPassengerDetails(selectedId);
                }
            }
        });

        loadPassengerIds();

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadPassengerIds() {
        selectID.removeAllItems();
        String sql = "SELECT passenger_id FROM PASSENGER ORDER BY passenger_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                selectID.addItem(rs.getInt("passenger_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading IDs: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPassengerDetails(int passengerId) {
        String sql = "SELECT p.passenger_name, p.contact, COUNT(t.ticket_id) AS ticket_count " +
                "FROM PASSENGER p " +
                "LEFT JOIN TICKET t ON p.passenger_id = t.passenger_id " +
                "WHERE p.passenger_id = ? " +
                "GROUP BY p.passenger_name, p.contact";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtName.setText(rs.getString("passenger_name"));
                    txtContact.setText(rs.getString("contact"));
                    txtTickets.setText(String.valueOf(rs.getInt("ticket_count")));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading details: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        Integer selectedId = (Integer) selectID.getSelectedItem();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Please select an ID.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String ticketsStr = txtTickets.getText().trim();

        if (name.isEmpty() || contact.isEmpty() || ticketsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int targetTicketCount;
        try {
            targetTicketCount = Integer.parseInt(ticketsStr);
            if (targetTicketCount < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tickets must be a valid positive integer value.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = OracleConnector.getConnection()) {
            con.setAutoCommit(false); // Enable transactional integrity handling

            String updatePassengerSql = "UPDATE PASSENGER SET passenger_name = ?, contact = ? WHERE passenger_id = ?";
            try (PreparedStatement psPassenger = con.prepareStatement(updatePassengerSql)) {
                psPassenger.setString(1, name);
                psPassenger.setString(2, contact);
                psPassenger.setInt(3, selectedId);
                psPassenger.executeUpdate();
            }

            String countSql = "SELECT COUNT(*) FROM TICKET WHERE passenger_id = ?";
            int currentTicketCount = 0;
            try (PreparedStatement psCount = con.prepareStatement(countSql)) {
                psCount.setInt(1, selectedId);
                try (ResultSet rs = psCount.executeQuery()) {
                    if (rs.next()) currentTicketCount = rs.getInt(1);
                }
            }

            if (targetTicketCount > currentTicketCount) {
                String insertTicketSql = "INSERT INTO TICKET (ticket_id, trip_id, passenger_id, seat_no, booking_date, status) " +
                        "VALUES ((SELECT NVL(MAX(ticket_id), 0) + 1 FROM TICKET), 4, ?, 'Auto', SYSDATE, 'Booked')";
                try (PreparedStatement psInsert = con.prepareStatement(insertTicketSql)) {
                    for (int i = 0; i < (targetTicketCount - currentTicketCount); i++) {
                        psInsert.setInt(1, selectedId);
                        psInsert.executeUpdate();
                    }
                }
            } else if (targetTicketCount < currentTicketCount) {
                String deleteTicketSql = "DELETE FROM TICKET WHERE ticket_id IN " +
                        "(SELECT ticket_id FROM TICKET WHERE passenger_id = ? FETCH FIRST ? ROWS ONLY)";
                try (PreparedStatement psDelete = con.prepareStatement(deleteTicketSql)) {
                    psDelete.setInt(1, selectedId);
                    psDelete.setInt(2, (currentTicketCount - targetTicketCount));
                    psDelete.executeUpdate();
                }
            }

            con.commit();
            JOptionPane.showMessageDialog(this, "Changes saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
            confirmed = true;
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database update operation rejected:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
