package view;

import controller.VehicleController;
import controller.VehicleController.Vehicle;    // This lets you write just "Vehicle" which I spent 30mins on for this bugfix

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;

public class VehicleView extends JPanel {
    private JPanel RootPanelV;
    private JPanel MainPanelV;
    private JPanel headerPanel;
    private JButton btnVehicleAdd;
    private JTextField SearchBoxV;
    private JTable table1;
    private JLabel lblVManage;
    private JButton btnVehicleEdit;
    private JButton btnVehicleDel;
    private JPanel searchPanel;
    private JLabel lblSearch;


    private final VehicleController vehicleController = new VehicleController();

    //This is the tablw rhat displays all the values with each respective column
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Type", "License", "Seats", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public VehicleView() {
        setLayout(new BorderLayout());
        add(RootPanelV, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        btnVehicleAdd.addActionListener(e -> openDialog());
        btnVehicleEdit.addActionListener(e -> openEditDialog());
        btnVehicleDel.addActionListener(e -> openDeleteDialog());

        //All string is stored in a document and this form fetches allat info and documentlistener catches any changes
        SearchBoxV.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        refresh();
    }

    // Reload vehicles through VehicleController
    public void refresh() {
        try {
            List<Vehicle> vehicles = vehicleController.getVehicles(SearchBoxV.getText());

            model.setRowCount(0);

            for (Vehicle vehicle : vehicles) {
                model.addRow(new Object[]{
                        vehicle.getId(),
                        vehicle.getType(),
                        vehicle.getLicense(),
                        vehicle.getSeats(),
                        vehicle.getStatus()
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load vehicles:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Open the AddVehicleDialog
    private void openDialog() {
        try {
            Window owner = SwingUtilities.getWindowAncestor(this);

            AddVehicleDialog dialog = new AddVehicleDialog(owner);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refresh();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Could not open the vehicle form:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public JPanel getRootPanel() {
        return RootPanelV;
    }

    // Builds a Vehicle from the highlighted table row (null if nothing is selected)
    private Vehicle getSelectedVehicle() {
        int row = table1.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle first.");
            return null;
        }
        return new Vehicle(
                (int) model.getValueAt(row, 0),
                (String) model.getValueAt(row, 1),
                (String) model.getValueAt(row, 2),
                (int) model.getValueAt(row, 3),
                (String) model.getValueAt(row, 4)
        );
    }

    private void openEditDialog() {
        Vehicle vehicle = getSelectedVehicle();
        if (vehicle == null) return;

        EditVehicle dialog = new EditVehicle(SwingUtilities.getWindowAncestor(this), vehicle);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refresh();
        }
    }

    private void openDeleteDialog() {
        Vehicle vehicle = getSelectedVehicle();
        if (vehicle == null) return;

        DeleteVehicle dialog = new DeleteVehicle(SwingUtilities.getWindowAncestor(this), vehicle);
        dialog.setVisible(true);

        if (dialog.isDeleted()) {
            refresh();
        }
    }
}