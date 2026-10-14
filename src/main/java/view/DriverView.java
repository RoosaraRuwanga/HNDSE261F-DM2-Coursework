package view;

import com.mongodb.client.model.Window;
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

<<<<<<< HEAD
    private final DriverController driverController =
            new DriverController();
=======
    //This will be used to form the connection with the Controller Class
    private final DriverController driverController = new DriverController();
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71

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

<<<<<<< HEAD
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
=======
        //This is the All Important Search Function and document listener provides that
        SearchBoxD.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refresh(); }
            public void removeUpdate(DocumentEvent e)  { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71

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
<<<<<<< HEAD
        AddDriverDialog dialog = new AddDriverDialog(
                SwingUtilities.getWindowAncestor(this)
        );

=======
        AddDriverDialog dialog = new AddDriverDialog ((SwingUtilities.getWindowAncestor(this));
>>>>>>> 2f0e2ba1517bdd044b8a9c1823d7aec32f939d71
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