package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditRoute extends JDialog {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblID;
    private JLabel lblStart;
    private JLabel lblDestination;
    private JTextField txtStart;
    private JTextField txtDestination;
    private JComboBox selectRoute;

    public EditRoute() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSave);

        selectRoute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer selectedId = (Integer) selectRoute.getSelectedItem();
                if (selectedId != null) {
                    loadRouteDetails(selectedId);
                }
            }
        });
        loadRouteIds();

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

    private void loadRouteIds() {
        selectRoute.removeAllItems();
        String sql = "SELECT route_id FROM ROUTE ORDER BY route_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                selectRoute.addItem(rs.getInt("route_id"));
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading Route IDs: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRouteDetails(int routeId) {
        String sql = "SELECT start_location, destination FROM ROUTE WHERE route_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtStart.setText(rs.getString("start_location"));
                    txtDestination.setText(rs.getString("destination"));
                }
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading route details: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        Integer selectedId = (Integer) selectRoute.getSelectedItem();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Please select a Route ID.",
                    "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String startLocation = txtStart.getText().trim();
        String destination = txtDestination.getText().trim();

        if (startLocation.isEmpty() || destination.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Locations cannot be blank.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE ROUTE SET start_location = ?, destination = ? WHERE route_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, startLocation);
            ps.setString(2, destination);
            ps.setInt(3, selectedId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Route updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }
        catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
