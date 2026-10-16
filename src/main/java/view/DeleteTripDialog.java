package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteTripDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonRemove;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblID;
    private JComboBox SelectID;

    public DeleteTripDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonRemove);
        loadTripIds();

        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTripRecord();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadTripIds() {
        SelectID.removeAllItems();
        String sql = "SELECT trip_id FROM TRIP ORDER BY trip_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SelectID.addItem(rs.getInt("trip_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading Trip IDs:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTripRecord() {
        Integer selectedTripId = (Integer) SelectID.getSelectedItem();
        if (selectedTripId == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid Trip ID to remove.",
                    "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmChoice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete Trip ID: " + selectedTripId + "?\n" +
                        "This will delete all linked seat allocations, ticket records, and customer payments.",
                "Confirm Trip Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmChoice != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = OracleConnector.getConnection()) {
            con.setAutoCommit(false);
            String deletePaymentsSql = "DELETE FROM PAYMENT WHERE ticket_id IN (" +
                    "SELECT ticket_id FROM TICKET WHERE trip_id = ?)";
            try (PreparedStatement psPayments = con.prepareStatement(deletePaymentsSql)) {
                psPayments.setInt(1, selectedTripId);
                psPayments.executeUpdate();
            }

            String deleteTicketsSql = "DELETE FROM TICKET WHERE trip_id = ?";
            try (PreparedStatement psTickets = con.prepareStatement(deleteTicketsSql)) {
                psTickets.setInt(1, selectedTripId);
                psTickets.executeUpdate();
            }

            String deleteTripSql = "DELETE FROM TRIP WHERE trip_id = ?";
            try (PreparedStatement psTrip = con.prepareStatement(deleteTripSql)) {
                psTrip.setInt(1, selectedTripId);
                int rowsDeleted = psTrip.executeUpdate();

                if (rowsDeleted > 0) {
                    con.commit();
                    JOptionPane.showMessageDialog(this, "Trip layout and structural logs dropped successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    con.rollback();
                    JOptionPane.showMessageDialog(this, "Target trip schedule could not be located.",
                            "Execution Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database execution rejected deletion request:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
