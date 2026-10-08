package view;

import data.OracleConnector;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddTripDialog extends JDialog {

    private JPanel RootPanel; // Bound to AddTripDialog.form
    private JComboBox<Item> cmbRoute;
    private JComboBox<Item> cmbVehicle;
    private JComboBox<Item> cmbDriver; // Corrected variable name from cmbDate
    private JTextField txtTripDate;
    private JComboBox<String> cmbStatus;
    private JButton saveTripButton;
    private JButton cancelButton;

    private Integer editId;
    private boolean saved = false;

    // Custom Dark Button Styling Colors
    private static final Color DARK_BUTTON_BG = Color.decode("#1E1E1E");
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;


    // =========================================================
    // COMBO BOX ITEM CLASS
    // =========================================================

    public static class Item {
        int id;
        String label;

        public Item(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label; // Displayed text inside the JComboBox
        }
    }


    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public AddTripDialog(Window owner) {
        this(owner, null);
    }

    public AddTripDialog(Window owner, Integer tripId) {
        super(
                owner,
                tripId == null ? "Add Trip" : "Edit Trip",
                ModalityType.APPLICATION_MODAL
        );

        this.editId = tripId;

        // Set RootPanel content pane
        setContentPane(RootPanel);

        // Style buttons
        styleButton(saveTripButton);
        styleButton(cancelButton);

        // Load dropdown options from database
        loadRoutes();
        loadVehicles();
        loadDrivers();

        // Populate Status dropdown
        cmbStatus.setModel(
                new DefaultComboBoxModel<>(
                        new String[]{
                                "Scheduled",
                                "Completed",
                                "Cancelled"
                        }
                )
        );

        // If editing an existing record
        if (editId != null) {
            saveTripButton.setText("Save Changes");
            loadTrip();
        }

        // SAVE BUTTON ACTION
        saveTripButton.addActionListener(e -> {
            System.out.println("SAVE TRIP BUTTON CLICKED");
            saveTrip();
        });

        // CANCEL BUTTON ACTION
        cancelButton.addActionListener(e -> {
            System.out.println("CANCEL BUTTON CLICKED");
            dispose();
        });

        getRootPane().setDefaultButton(saveTripButton);
        pack();
        setLocationRelativeTo(owner);
    }


    // Helper method to format buttons
    private void styleButton(JButton button) {
        if (button != null) {
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBackground(DARK_BUTTON_BG);
            button.setForeground(BUTTON_TEXT_COLOR);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        }
    }


    public boolean isSaved() {
        return saved;
    }


    // =========================================================
    // LOAD DROPDOWNS FROM DATABASE
    // =========================================================

    private void loadRoutes() {
        String sql = "SELECT route_id, start_location || ' - ' || destination FROM ROUTE ORDER BY route_id";
        fillCombo(cmbRoute, sql);
    }

    private void loadVehicles() {
        String sql = "SELECT vehicle_id, license_number || ' (' || vehicle_type || ')' FROM VEHICLE ORDER BY vehicle_id";
        fillCombo(cmbVehicle, sql);
    }

    private void loadDrivers() {
        String sql = "SELECT driver_id, driver_name FROM DRIVER ORDER BY driver_id";
        fillCombo(cmbDriver, sql);
    }

    private void fillCombo(JComboBox<Item> combo, String sql) {
        if (combo == null) return;

        combo.removeAllItems();

        try (
                Connection con = OracleConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                combo.addItem(
                        new Item(
                                rs.getInt(1),
                                rs.getString(2)
                        )
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load dropdown data:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // =========================================================
    // LOAD EXISTING TRIP (FOR EDITING)
    // =========================================================

    private void loadTrip() {
        String sql = "SELECT route_id, vehicle_id, driver_id, trip_date, status FROM TRIP WHERE trip_id = ?";

        try (
                Connection con = OracleConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, editId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selectItem(cmbRoute, rs.getInt("route_id"));
                    selectItem(cmbVehicle, rs.getInt("vehicle_id"));
                    selectItem(cmbDriver, rs.getInt("driver_id"));

                    Date tripDate = rs.getDate("trip_date");
                    if (tripDate != null) {
                        txtTripDate.setText(tripDate.toString());
                    }

                    cmbStatus.setSelectedItem(rs.getString("status"));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load trip:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void selectItem(JComboBox<Item> combo, int id) {
        if (combo == null) return;
        for (int i = 0; i < combo.getItemCount(); i++) {
            Item item = combo.getItemAt(i);
            if (item.id == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }


    // =========================================================
    // SAVE / UPDATE TRIP
    // =========================================================

    private void saveTrip() {
        Item route = (Item) cmbRoute.getSelectedItem();
        Item vehicle = (Item) cmbVehicle.getSelectedItem();
        Item driver = (Item) cmbDriver.getSelectedItem();
        String dateText = txtTripDate.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();

        // Validation
        if (route == null) { showError("Please select a route."); return; }
        if (vehicle == null) { showError("Please select a vehicle."); return; }
        if (driver == null) { showError("Please select a driver."); return; }
        if (dateText.isEmpty()) { showError("Please enter a trip date."); return; }
        if (status == null || status.isEmpty()) { showError("Please select a status."); return; }

        Date tripDate;
        try {
            tripDate = Date.valueOf(dateText);
        } catch (IllegalArgumentException ex) {
            showError("Invalid date.\n\nUse format:\nYYYY-MM-DD\n\nExample: 2026-10-08");
            return;
        }

        // UPDATE EXISTING TRIP
        if (editId != null) {
            String sql = "UPDATE TRIP SET route_id = ?, vehicle_id = ?, driver_id = ?, trip_date = ?, status = ? WHERE trip_id = ?";

            try (
                    Connection con = OracleConnector.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)
            ) {
                ps.setInt(1, route.id);
                ps.setInt(2, vehicle.id);
                ps.setInt(3, driver.id);
                ps.setDate(4, tripDate);
                ps.setString(5, status);
                ps.setInt(6, editId);

                ps.executeUpdate();
                saved = true;

                JOptionPane.showMessageDialog(this, "Trip updated successfully.");
                dispose();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showError("Could not update trip:\n" + ex.getMessage());
            }
            return;
        }

        // INSERT NEW TRIP
        String sql = "INSERT INTO TRIP (trip_id, route_id, vehicle_id, driver_id, trip_date, status) " +
                "SELECT NVL(MAX(trip_id), 0) + 1, ?, ?, ?, ?, ? FROM TRIP";

        try (
                Connection con = OracleConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, route.id);
            ps.setInt(2, vehicle.id);
            ps.setInt(3, driver.id);
            ps.setDate(4, tripDate);
            ps.setString(5, status);

            ps.executeUpdate();
            saved = true;

            JOptionPane.showMessageDialog(this, "Trip added successfully.");
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Could not add trip:\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}