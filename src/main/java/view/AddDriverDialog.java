package view;

<<<<<<< HEAD
=======
import java.awt.Window;
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71
import controller.DriverController;

import javax.swing.*;
import java.awt.Window;
import java.sql.SQLException;

public class AddDriverDialog extends JDialog {
<<<<<<< HEAD

=======
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71
    private JPanel RootPanelDD;
    private JPanel MainPanelDD;
    private JLabel lblDHead;
    private JLabel lblDName;
    private JTextField txtDName;
    private JLabel lblDContact;
    private JTextField txtDContact;
    private JLabel lblDStatus;
    private JComboBox<String> cmbAddD;
    private JPanel BtnPanelD;
    private JButton btnAddD;
    private JButton BtnCancelD;
    private JLabel lblDLicense;
    private JTextField txtDLicense;

    private final DriverController driverController =
            new DriverController();

    private boolean saved = false;

    public AddDriverDialog(Window owner) {
        super(owner, "Add Driver", ModalityType.APPLICATION_MODAL);

        setContentPane(RootPanelDD);

        cmbAddD.setModel(new DefaultComboBoxModel<>(
<<<<<<< HEAD
                new String[]{"Active", "Inactive", "On Leave"}
        ));

        btnAddD.addActionListener(e -> save());
        BtnCancelD.addActionListener(e -> dispose());

=======
                new String[]{"Active", "Inactive", "On Leave"}));

        btnAddD.addActionListener(e -> save());
        BtnCancelD.addActionListener(e -> dispose());
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71
        getRootPane().setDefaultButton(btnAddD);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void save() {
        try {
            driverController.addDriver(
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
            error("Could not save driver: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}