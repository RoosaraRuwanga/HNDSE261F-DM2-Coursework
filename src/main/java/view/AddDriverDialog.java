package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.DriverController;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.sql.SQLException;
import java.util.Locale;

public class AddDriverDialog extends JDialog {

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
                new String[]{"Active", "Inactive", "On Leave"}
        ));

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