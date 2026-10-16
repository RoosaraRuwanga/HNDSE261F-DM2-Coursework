package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RemovePassengerDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblID;
    private JComboBox selectID;

    private boolean confirmed = false;

    public RemovePassengerDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        loadPassengerIds();

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removePassenger();
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

    private void removePassenger() {
        Integer selectedId = (Integer) selectID.getSelectedItem();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Please select an ID to remove.",
                    "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmChoice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete Passenger ID: " + selectedId + "?\n" +
                        "This will delete all corresponding ticket bookings and payments.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmChoice != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = OracleConnector.getConnection()) {
            con.setAutoCommit(false);
            String deletePaymentsSql = "DELETE FROM PAYMENT WHERE ticket_id IN " +
                    "(SELECT ticket_id FROM TICKET WHERE passenger_id = ?)";
            try (PreparedStatement psPayments = con.prepareStatement(deletePaymentsSql)) {
                psPayments.setInt(1, selectedId);
                psPayments.executeUpdate();
            }
            String deleteTicketsSql = "DELETE FROM TICKET WHERE passenger_id = ?";
            try (PreparedStatement psTickets = con.prepareStatement(deleteTicketsSql)) {
                psTickets.setInt(1, selectedId);
                psTickets.executeUpdate();
            }
            String deletePassengerSql = "DELETE FROM PASSENGER WHERE passenger_id = ?";
            try (PreparedStatement psPassenger = con.prepareStatement(deletePassengerSql)) {
                psPassenger.setInt(1, selectedId);
                int rowsDeleted = psPassenger.executeUpdate();

                if (rowsDeleted > 0) {
                    con.commit();
                    JOptionPane.showMessageDialog(this, "Passenger registry entry removed successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    confirmed = true;
                    dispose();
                } else {
                    con.rollback();
                    JOptionPane.showMessageDialog(this, "Target passenger profile record could not be located.",
                            "Execution Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database execution rejected deletion request:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
