package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.MaintenanceController;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Locale;

public class AddMaintenanceDialog extends JDialog {
    private JPanel RootPanelMD;
    private JPanel MainPanelMD;
    private JLabel lblMHead;
    private JLabel lblMVId;
    private JTextField txtMVId;
    private JLabel lblMType;
    private JTextField txtMType;
    private JLabel lblMDate;
    private JTextField txtMDate;
    private JLabel lblMCost;
    private JTextField txtMCost;
    private JLabel lblMVStatus;
    private JComboBox cmbMVStatus;
    private JButton btnAddM;
    private JButton BtnCancelM;
    private JLabel lblMDueD;
    private JTextField txtMDueD;

    private final MaintenanceController maintenanceController = new MaintenanceController();
    private boolean saved = false;

    public AddMaintenanceDialog(Window owner) {
        super(owner, "Add Maintenance Record", ModalityType.APPLICATION_MODAL);
        setContentPane(RootPanelMD);

        cmbMVStatus.setModel(new DefaultComboBoxModel<>(
                new String[]{"Planned", "In Progress", "Completed"})
        );

        btnAddM.addActionListener(e -> save());
        BtnCancelM.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnAddM);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void save() {
        int vehicleId;
        double cost;
        Date date;
        Date nextDue = null;

        try {
            vehicleId = Integer.parseInt(txtMVId.getText().trim());
        } catch (NumberFormatException ex) {
            error("Vehicle ID must be a whole number.");
            return;
        }

        try {
            cost = Double.parseDouble(txtMCost.getText().trim());
        } catch (NumberFormatException ex) {
            error("Cost must be a number, e.g. 1500.00");
            return;
        }

        try {
            date = Date.valueOf(txtMDate.getText().trim());   // expects yyyy-MM-dd
        } catch (IllegalArgumentException ex) {
            error("Maintenance date must be in the format yyyy-MM-dd, e.g. 2026-10-14");
            return;
        }

        // Next due date is optional, so only parse it if something was typed
        String dueText = txtMDueD.getText().trim();
        if (!dueText.isEmpty()) {
            try {
                nextDue = Date.valueOf(dueText);
            } catch (IllegalArgumentException ex) {
                error("Next due date must be in the format yyyy-MM-dd, or left blank.");
                return;
            }
        }

        try {
            maintenanceController.addMaintenance(
                    vehicleId,
                    date,
                    txtMType.getText(),
                    cost,
                    nextDue,
                    (String) cmbMVStatus.getSelectedItem());

            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        } catch (SQLException ex) {
            error("Could not save maintenance record: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

}