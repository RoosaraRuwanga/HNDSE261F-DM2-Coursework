package view;

import controller.DriverController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
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

    private final DriverController driverController =
            new DriverController();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Licence", "Contact", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public DriverView() {
        setLayout(new BorderLayout());
        add(RootPanelD, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        btnDriverAdd.addActionListener(e -> openDialog());

        SearchBoxD.getDocument().addDocumentListener(
                new DocumentListener() {
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
                }
        );

        refresh();
    }

    public void refresh() {
        try {
            List<DriverController.Driver> drivers =
                    driverController.getDrivers(SearchBoxD.getText());

            model.setRowCount(0);

            for (DriverController.Driver driver : drivers) {
                model.addRow(new Object[]{
                        driver.getId(),
                        driver.getName(),
                        driver.getLicense(),
                        driver.getContact(),
                        driver.getStatus()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load drivers:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openDialog() {
        AddDriverDialog dialog = new AddDriverDialog(
                SwingUtilities.getWindowAncestor(this)
        );

        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refresh();
        }
    }

    // Allows SmartMove to access the panel.
    public JPanel getRootPanel() {
        return RootPanelD;
    }
}