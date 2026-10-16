package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Calendar;

public class EditTripDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblRoute;
    private JComboBox selectRoute;
    private JLabel lblvehicle;
    private JComboBox selectVehicle;
    private JLabel lblDriver;
    private JComboBox selectDriver;
    private JLabel lblDate;
    private JSpinner selectDate;
    private JLabel lblStatus;
    private JComboBox selectStatus;
    private JLabel lblID;
    private JComboBox selectID;
    private int targetTripId = -1;

    public EditTripDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSave);
        setupDatePickerOnly();
        populateDropdownOptions();
        loadTripIds();

        selectID.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer selectedTripId = (Integer) selectID.getSelectedItem();
                if (selectedTripId != null) {
                    loadExistingTripData(selectedTripId);
                }
            }
        });

        if (selectID.getItemCount() > 0) {
            selectID.setSelectedIndex(0);
        }

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTripChanges();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void setupDatePickerOnly() {
        SpinnerDateModel dateModel = new SpinnerDateModel(
                new java.util.Date(),
                null,
                null,
                Calendar.DATE
        );
        selectDate.setModel(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(selectDate, "yyyy-MM-dd");
        selectDate.setEditor(dateEditor);
    }

    private void loadTripIds() {
        selectID.removeAllItems();
        String sql = "SELECT trip_id FROM TRIP ORDER BY trip_id";
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                selectID.addItem(rs.getInt("trip_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading Trip IDs:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateDropdownOptions() {
        try (Connection con = OracleConnector.getConnection()) {
            String routeSql = "SELECT route_id, start_location, destination FROM ROUTE ORDER BY route_id";
            try (PreparedStatement ps = con.prepareStatement(routeSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    selectRoute.addItem(rs.getInt("route_id") + " - " + rs.getString("start_location") + " to " + rs.getString("destination"));
                }
            }

            String vehicleSql = "SELECT vehicle_id, license_number FROM VEHICLE ORDER BY vehicle_id";
            try (PreparedStatement ps = con.prepareStatement(vehicleSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    selectVehicle.addItem(rs.getInt("vehicle_id") + " - " + rs.getString("license_number"));
                }
            }
            String driverSql = "SELECT driver_id, driver_name FROM DRIVER ORDER BY driver_id";
            try (PreparedStatement ps = con.prepareStatement(driverSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    selectDriver.addItem(rs.getInt("driver_id") + " - " + rs.getString("driver_name"));
                }
            }
            selectStatus.removeAllItems();
            selectStatus.addItem("Scheduled");
            selectStatus.addItem("Completed");
            selectStatus.addItem("Cancelled");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error initializing dropdown parameters:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadExistingTripData(int tripId) {
        String sql = "SELECT route_id, vehicle_id, driver_id, trip_date, status FROM TRIP WHERE trip_id = ?";
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    setSelectedComboValue(selectRoute, rs.getInt("route_id"));
                    setSelectedComboValue(selectVehicle, rs.getInt("vehicle_id"));
                    setSelectedComboValue(selectDriver, rs.getInt("driver_id"));
                    selectStatus.setSelectedItem(rs.getString("status"));
                    Date tripDate = rs.getDate("trip_date");
                    if (tripDate != null) {
                        selectDate.setValue(new java.util.Date(tripDate.getTime()));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading existing trip profiles:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setSelectedComboValue(JComboBox<String> comboBox, int targetId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String item = comboBox.getItemAt(i);
            if (item.startsWith(targetId + " ")) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private int getIdFromSelection(JComboBox<String> comboBox) {
        String selectedItem = (String) comboBox.getSelectedItem();
        if (selectedItem != null) {
            return Integer.parseInt(selectedItem.split(" - ")[0]);
        }
        return -1;
    }

    private void saveTripChanges() {
        Integer selectedTripId = (Integer) selectID.getSelectedItem();
        if (selectedTripId == null) {
            JOptionPane.showMessageDialog(this, "No active Trip ID selected.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int routeId = getIdFromSelection(selectRoute);
        int vehicleId = getIdFromSelection(selectVehicle);
        int driverId = getIdFromSelection(selectDriver);
        String status = (String) selectStatus.getSelectedItem();
        java.util.Date spinnerDate = (java.util.Date) selectDate.getValue();
        java.sql.Date sqlDate = new java.sql.Date(spinnerDate.getTime());

        String sql = "UPDATE TRIP SET route_id = ?, vehicle_id = ?, driver_id = ?, trip_date = ?, status = ? WHERE trip_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, vehicleId);
            ps.setInt(3, driverId);
            ps.setDate(4, sqlDate);
            ps.setString(5, status);
            ps.setInt(6, selectedTripId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Trip metrics updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database update operation rejected:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
