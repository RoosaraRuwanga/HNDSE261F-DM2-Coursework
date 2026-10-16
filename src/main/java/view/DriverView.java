package view;

//import com.intellij.uiDesigner.core.GridConstraints;
//import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.DriverController;
import controller.DriverController.Driver;     // YOUR Driver class

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DriverView extends JPanel {
    private JPanel RootPanelD;
    private JPanel MainPanelD;
    private JLabel lblDManage;
    private JButton btnDriverAdd;
    private JTextField SearchBoxD;
    private JTable table1;
    private JScrollPane TbleD;
    private JButton btnDriverEdit;
    private JButton btnDriverDel;
    private JPanel searchPanel;
    private JLabel lblSearch;

    //This will be used to form the connection with the Controller Class
    private final DriverController driverController = new DriverController();

    //This is the Table holding the values of the Schema or DB
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Licence", "Contact", "Status"}, 0
    ) {
        //Overriding "isCelleditable = false" will stop users from typing into the cells
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DriverView() {
        setLayout(new BorderLayout());
        add(RootPanelD, BorderLayout.CENTER);

        //Dis da table
        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        btnDriverAdd.addActionListener(e -> openDialog());
        //More Newly Added
        btnDriverEdit.addActionListener(e -> openEditDialog());
        btnDriverDel.addActionListener(e -> openDeleteDialog());



        //This is the All Important Search Function and document listener provides that
        SearchBoxD.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        refresh();
    }

    public void refresh() {
        try {
            List<Driver> drivers = driverController.getDrivers(SearchBoxD.getText());

            model.setRowCount(0);
            for (Driver d : drivers) {
                model.addRow(new Object[]{
                        d.getId(), d.getName(), d.getLicense(), d.getContact(), d.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load drivers:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDialog() {
        AddDriverDialog dialog = new AddDriverDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refresh();
        }
    }

    // Builds a Driver from the highlighted table row (null if nothing is selected)
    private Driver getSelectedDriver() {
        int row = table1.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver first.");
            return null;
        }
        return new Driver(
                (int) model.getValueAt(row, 0),
                (String) model.getValueAt(row, 1),
                (String) model.getValueAt(row, 2),
                (String) model.getValueAt(row, 3),
                (String) model.getValueAt(row, 4)
        );
    }

    private void openEditDialog() {
        Driver driver = getSelectedDriver();
        if (driver == null) return;

        EditDriver dialog = new EditDriver(SwingUtilities.getWindowAncestor(this), driver);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refresh();
        }
    }

    private void openDeleteDialog() {
        Driver driver = getSelectedDriver();
        if (driver == null) return;

        DeleteDriver dialog = new DeleteDriver(SwingUtilities.getWindowAncestor(this), driver);
        dialog.setVisible(true);

        if (dialog.isDeleted()) {
            refresh();
        }
    }

    public JPanel getRootPanel() {
        return RootPanelD;
    }

}

