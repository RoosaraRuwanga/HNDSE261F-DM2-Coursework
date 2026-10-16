package view;

import controller.DriverController;
import controller.DriverController.Driver;

import javax.swing.*;
import java.awt.Window;
import java.sql.SQLException;

public class DeleteDriver extends JDialog {
    private JPanel RootPanel;
    private JPanel MainPanel;
    private JLabel lblDHead;
    private JLabel lblDName;
    private JTextField txtDName;
    private JTextField txtDLicense;     // see the designer note below
    private JLabel lblDLicense;
    private JLabel lblDContact;
    private JTextField txtDContact;
    private JLabel lblDStatus;
    private JComboBox<String> cmbAddD;
    private JButton btnAddD;
    private JButton BtnCancelD;

    private final DriverController driverController = new DriverController();
    private final int driverId;
    private boolean deleted = false;

    public DeleteDriver(Window owner, Driver driver) {
        super(owner, "Delete Driver", ModalityType.APPLICATION_MODAL);
        this.driverId = driver.getId();

        setContentPane(RootPanel);

        // Show the record for confirmation only, so every field is read-only
        cmbAddD.setModel(new DefaultComboBoxModel<>(new String[]{driver.getStatus()}));
        txtDName.setText(driver.getName());
        txtDLicense.setText(driver.getLicense());
        txtDContact.setText(driver.getContact());

        txtDName.setEditable(false);
        txtDLicense.setEditable(false);
        txtDContact.setEditable(false);
        cmbAddD.setEnabled(false);

        btnAddD.addActionListener(e -> delete());
        BtnCancelD.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isDeleted() {
        return deleted;
    }

    private void delete() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete this driver?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            driverController.deleteDriver(driverId);
            deleted = true;
            dispose();

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error("Could not delete driver: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}