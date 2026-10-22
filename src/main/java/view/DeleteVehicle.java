package view;

import controller.VehicleController;
import controller.VehicleController.Vehicle;

import javax.swing.*;
import java.awt.Window;
import java.sql.SQLException;

public class DeleteVehicle extends JDialog {
    private JPanel RootPanel;
    private JLabel lblVHead;
    private JLabel lblVName;
    private JTextField txtVName;
    private JPanel MainPanel;
    private JLabel lblVLicense;
    private JTextField txtVLicense;
    private JLabel lblVSeats;
    private JTextField txtVSeats;
    private JLabel lblVStatus;
    private JComboBox cmbDeleteV;
    private JButton btnDeleteV;
    private JButton BtnCancelV;

    private final VehicleController vehicleController = new VehicleController();
    private final int vehicleId;
    private boolean deleted = false;

    public DeleteVehicle(Window owner, Vehicle vehicle) {
        super(owner, "Delete Vehicle", ModalityType.APPLICATION_MODAL);
        this.vehicleId = vehicle.getId();
        setContentPane(RootPanel);

        // Show the record for confirmation only, so every field is read-only
        cmbDeleteV.setModel(new DefaultComboBoxModel<>(new String[]{vehicle.getStatus()}));
        txtVName.setText(vehicle.getType());
        txtVLicense.setText(vehicle.getLicense());
        txtVSeats.setText(String.valueOf(vehicle.getSeats()));

        txtVName.setEditable(false);
        txtVLicense.setEditable(false);
        txtVSeats.setEditable(false);
        cmbDeleteV.setEnabled(false);

        btnDeleteV.setText("Delete Vehicle");
        btnDeleteV.addActionListener(e -> delete());
        BtnCancelV.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isDeleted() {
        return deleted;
    }

    private void delete() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete this vehicle?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            vehicleController.deleteVehicle(vehicleId);
            deleted = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error("Could not delete vehicle: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}