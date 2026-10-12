
package view;

import data.OracleConnector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TripView extends JPanel {

    private JPanel RootPlan;
    private JButton addTripButton;
    private JTextField txttrip;
    private JTable table1;

    // Table columns without Edit and Delete buttons
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

        // =========================
        // TABLE SETTINGS
        // =========================

        table1.setModel(model);
        table1.setRowHeight(36);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        // =========================
        // ADD TRIP BUTTON
        // =========================

        addTripButton.addActionListener(e -> openDialog(null));

        // =========================
        // SEARCH
        // =========================

        txttrip.getDocument().addDocumentListener(
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

        // =========================
        // LOAD DATA
        // =========================

        refresh();
    }

    // =========================================================
    // REFRESH TABLE
    // =========================================================

    public void refresh() {

        String term = "%" + txttrip.getText().trim().toLowerCase() + "%";

        String sql =
                "SELECT t.trip_id, " +
                        "r.start_location || ' - ' || r.destination AS route_name, " +
                        "v.license_number, " +
                        "d.driver_name, " +
                        "t.trip_date, " +
                        "t.status " +
                        "FROM TRIP t " +
                        "JOIN ROUTE r ON t.route_id = r.route_id " +
                        "JOIN VEHICLE v ON t.vehicle_id = v.vehicle_id " +
                        "JOIN DRIVER d ON t.driver_id = d.driver_id " +
                        "WHERE LOWER(r.start_location || ' - ' || r.destination) LIKE ? " +
                        "OR LOWER(v.license_number) LIKE ? " +
                        "OR LOWER(d.driver_name) LIKE ? " +
                        "OR LOWER(t.status) LIKE ? " +
                        "ORDER BY t.trip_date, t.trip_id";

        try (
                Connection con = OracleConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, term);
            }

            try (ResultSet rs = ps.executeQuery()) {

                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(
                            new Object[]{
                                    rs.getInt("trip_id"),
                                    rs.getString("route_name"),
                                    rs.getString("license_number"),
                                    rs.getString("driver_name"),
                                    rs.getDate("trip_date"),
                                    rs.getString("status")
                            }
                    );
                }
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

    // =========================================================
    // OPEN ADD TRIP DIALOG
    // =========================================================

    private void openDialog(Integer tripId) {

        try {
            Window owner = SwingUtilities.getWindowAncestor(this);

            AddTripDialog dialog = new AddTripDialog(owner, tripId);
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
}
