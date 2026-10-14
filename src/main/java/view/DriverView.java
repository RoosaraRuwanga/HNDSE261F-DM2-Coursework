package view;

import com.mongodb.client.model.Window;
import controller.DriverController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Driver;
import java.sql.SQLException;

public class DriverView extends JPanel {
    private JPanel RootPanelD;
    private JPanel MainPanelD;
    private JLabel lblDManage;
    private JButton btnDriverAdd;
    private JTextField SearchBoxD;
    private JTable table1;
    private JScrollPane TbleD;

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

        //This is the All Important Search Function and document listener provides that
        SearchBoxD.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refresh(); }
            public void removeUpdate(DocumentEvent e)  { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
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
        AddDriverDialog dialog = new AddDriverDialog ((SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refresh();
        }
    }
}

