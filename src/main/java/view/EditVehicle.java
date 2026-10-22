package view;

import controller.VehicleController;
import controller.VehicleController.Vehicle;

import javax.swing.*;
import java.awt.Window;
import java.sql.SQLException;

public class EditVehicle extends JDialog {
    private JPanel RootPanel;
    private JPanel MainPanel;
    private JLabel lblVHead;
    private JLabel lblVName;
    private JTextField txtVName;
    private JLabel lblVLicense;
    private JTextField txtVLicense;
    private JLabel lblVSeats;
    private JTextField txtVSeats;
    private JLabel lblVStatus;
    private JComboBox cmbDeleteV;
    private JButton btnEditV;
    private JButton BtnCancelV;

    private final VehicleController vehicleController = new VehicleController();
    private final int vehicleId;
    private boolean saved = false;

    public EditVehicle(Window owner, Vehicle vehicle) {
        super(owner, "Edit Vehicle", ModalityType.APPLICATION_MODAL);
        this.vehicleId = vehicle.getId();
        setContentPane(RootPanel);

        cmbDeleteV.setModel(new DefaultComboBoxModel<>(
                new String[]{"Active", "Under Maintenance", "Inactive"}));

        // Pre-fill the form (txtVName holds the vehicle type)
        txtVName.setText(vehicle.getType());
        txtVLicense.setText(vehicle.getLicense());
        txtVSeats.setText(String.valueOf(vehicle.getSeats()));
        cmbDeleteV.setSelectedItem(vehicle.getStatus());

        btnEditV.setText("Save Changes");
        btnEditV.addActionListener(e -> save());
        BtnCancelV.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnEditV);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void save() {
        int seats;
        try {
            seats = Integer.parseInt(txtVSeats.getText().trim());
        } catch (NumberFormatException ex) {
            error("Available seats must be a whole number.");
            return;
        }

        try {
            vehicleController.updateVehicle(
                    vehicleId,
                    txtVName.getText(),
                    txtVLicense.getText(),
                    seats,
                    (String) cmbDeleteV.getSelectedItem());

            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error("Could not update vehicle: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}