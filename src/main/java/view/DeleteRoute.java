package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteRoute extends JDialog {
    private JPanel contentPane;
    private JButton buttonDelete;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblID;
    private JComboBox comboBox1;

    public DeleteRoute() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonDelete);
        loadRouteIds();

        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRoute();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadRouteIds() {
        comboBox1.removeAllItems();
        String sql = "SELECT route_id FROM ROUTE ORDER BY route_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                comboBox1.addItem(rs.getInt("route_id"));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading Route IDs: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRoute() {
        Integer selectedId = (Integer) comboBox1.getSelectedItem();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Please select a Route ID to remove.",
                    "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmChoice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete Route ID: " + selectedId + "?\n" +
                        "This will cascade delete all corresponding trips, tickets, and payment records associated with this route.",
                "Confirm Route Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmChoice != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection con = OracleConnector.getConnection()) {
            con.setAutoCommit(false); // Begin database transactional atomic isolation
            String deletePaymentsSql = "DELETE FROM PAYMENT WHERE ticket_id IN (" +
                    "SELECT tk.ticket_id FROM TICKET tk " +
                    "JOIN TRIP t ON tk.trip_id = t.trip_id " +
                    "WHERE t.route_id = ?)";
            try (PreparedStatement psPayments = con.prepareStatement(deletePaymentsSql)) {
                psPayments.setInt(1, selectedId);
                psPayments.executeUpdate();
            }

            String deleteTicketsSql = "DELETE FROM TICKET WHERE trip_id IN (" +
                    "SELECT trip_id FROM TRIP WHERE route_id = ?)";
            try (PreparedStatement psTickets = con.prepareStatement(deleteTicketsSql)) {
                psTickets.setInt(1, selectedId);
                psTickets.executeUpdate();
            }

            String deleteTripsSql = "DELETE FROM TRIP WHERE route_id = ?";
            try (PreparedStatement psTrips = con.prepareStatement(deleteTripsSql)) {
                psTrips.setInt(1, selectedId);
                psTrips.executeUpdate();
            }

            String deleteRouteSql = "DELETE FROM ROUTE WHERE route_id = ?";
            try (PreparedStatement psRoute = con.prepareStatement(deleteRouteSql)) {
                psRoute.setInt(1, selectedId);
                int rowsDeleted = psRoute.executeUpdate();

                if (rowsDeleted > 0) {
                    con.commit();
                    JOptionPane.showMessageDialog(this, "Route records removed successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
                else {
                    con.rollback();
                    JOptionPane.showMessageDialog(this, "Target route record could not be found.",
                            "Execution Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database execution rejected deletion request:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
