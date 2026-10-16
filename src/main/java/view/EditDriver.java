package view;

import controller.DriverController;
import controller.DriverController.Driver;

import javax.swing.*;
import java.awt.Window;
import java.sql.SQLException;

public class EditDriver extends JDialog {
    private JPanel RootPanel;
    private JLabel lblDHead;
    private JLabel lblDName;
    private JPanel MainPanel;
    private JTextField txtDName;
    private JTextField txtDLicense;     // see the designer note below
    private JLabel lblDLicense;
    private JLabel lblDContact;
    private JTextField txtDContact;
    private JLabel lblDStatus;
    private JComboBox<String> cmbAddD;
    private JPanel btnPanel;
    private JButton btnAddD;
    private JButton BtnCancelD;

    private final DriverController driverController = new DriverController();
    private final int driverId;
    private boolean saved = false;

    public EditDriver(Window owner, Driver driver) {
        super(owner, "Edit Driver", ModalityType.APPLICATION_MODAL);
        this.driverId = driver.getId();

        setContentPane(RootPanel);

        cmbAddD.setModel(new DefaultComboBoxModel<>(
                new String[]{"Active", "Inactive", "On Leave"}
        ));

        // Pre-fill the form with the selected driver's current values
        txtDName.setText(driver.getName());
        txtDLicense.setText(driver.getLicense());
        txtDContact.setText(driver.getContact());
        cmbAddD.setSelectedItem(driver.getStatus());

        btnAddD.addActionListener(e -> save());
        BtnCancelD.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnAddD);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void save() {
        try {
            driverController.updateDriver(
                    driverId,
                    txtDName.getText(),
                    txtDLicense.getText(),
                    txtDContact.getText(),
                    (String) cmbAddD.getSelectedItem()
            );

            saved = true;
            dispose();

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error("Could not update driver: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}