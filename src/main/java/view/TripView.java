
package view;

import controller.TripController;
import controller.TripController.Trip;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;

public class TripView extends JPanel {

    private JPanel RootPlan;
    private JButton addTripButton;
    private JTextField txttrip;
    private JTable table1;

    private final TripController tripController = new TripController();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{
                    "ID",
                    "Route",
                    "Vehicle",
                    "Driver",
                    "Date",
                    "Status"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public TripView() {
        setLayout(new BorderLayout());
        add(RootPlan, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(36);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        // Open the Add Trip dialog
        addTripButton.addActionListener(e -> openDialog());

        // Search whenever the search text changes
        txttrip.getDocument().addDocumentListener(new DocumentListener() {
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

    // Reload trips through TripController
    public void refresh() {
        try {
            List<Trip> trips = tripController.getTrips(txttrip.getText());

            model.setRowCount(0);

            for (Trip trip : trips) {
                model.addRow(new Object[]{
                        trip.getId(),
                        trip.getRoute(),
                        trip.getVehicle(),
                        trip.getDriver(),
                        trip.getDate(),
                        trip.getStatus()
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load trips:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Open the existing AddTripDialog
    private void openDialog() {
        try {
            Window owner = SwingUtilities.getWindowAncestor(this);

            AddTripDialog dialog = new AddTripDialog(owner, null);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refresh();
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not open the trip form:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    // added so SmartMove can place this screen in its content panel
    public JPanel getRootPanel() {
        return RootPlan;
    }

}
