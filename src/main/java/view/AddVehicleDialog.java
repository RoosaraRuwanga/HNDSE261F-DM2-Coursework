package view;

import controller.VehicleController;
import javax.swing.*;
import java.awt.Window;
import java.sql.SQLException;

public class AddVehicleDialog extends JDialog {
    private JPanel RootPanelVD;
    private JPanel MainPanelVD;
    private JTextField txtVType;
    private JLabel lblVLicence;
    private JTextField txtVLicense;
    private JButton BtnCancelV;
    private JButton btnAddV;
    private JLabel lblVtype;
    private JLabel lblVSeats;
    private JTextField txtVSeats;
    private JLabel lblVStatus;
    private JLabel lblVHead;
    private JPanel BtnPanelV;
    private JComboBox cmbAddV;

    //This is used to call the Controller and make a connection
    private final VehicleController vehicleController = new VehicleController();
    private boolean saved = false;

    //Modal is used so that user may not continue to open multiple tabs while still in Dialog
    public AddVehicleDialog(Window owner) {
        super(owner, "Add Vehicle", ModalityType.APPLICATION_MODAL); //I SPENT AN HOUR ON BUGS!>!>!?!?!
        setContentPane(RootPanelVD);
        cmbAddV.setModel(new DefaultComboBoxModel<>(
                new String[]{"Active", "Under Maintenance", "Inactive"})
        );

        btnAddV.addActionListener(e -> save());
        BtnCancelV.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnAddV);

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
            vehicleController.addVehicle(
                    txtVType.getText(),
                    txtVLicense.getText(),
                    seats,
                    (String) cmbAddV.getSelectedItem());

            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error("Could not save vehicle: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}