package view;

import data.OracleConnector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TripView extends JPanel {

    private JPanel RootPlan;
    private JButton addTripButton;
    private JTextField txttrip;
    private JTable table1;

    // Custom Button Styling Colors (matching UI image)
    private static final Color EDIT_COLOR = Color.decode("#3B4351");   // Dark Gray/Navy
    private static final Color DELETE_COLOR = Color.decode("#A53344"); // Red / Maroon
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{
                    "ID",
                    "Route",
                    "Vehicle",
                    "Driver",
                    "Date",
                    "Status",
                    "Edit",
                    "Delete"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Allow column 6 (Edit) and 7 (Delete) to be editable so buttons can respond to clicks
            return column == 6 || column == 7;
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

        table1.getColumnModel().getColumn(6).setMaxWidth(90);
        table1.getColumnModel().getColumn(7).setMaxWidth(90);

        // Apply Custom Button Renderers & Editors to Columns 6 and 7
        table1.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer(EDIT_COLOR));
        table1.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), EDIT_COLOR, row -> editTrip(row)));

        table1.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer(DELETE_COLOR));
        table1.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), DELETE_COLOR, row -> deleteTrip(row)));


        // =========================
        // ADD TRIP BUTTON
        // =========================

        addTripButton.addActionListener(e -> {
            System.out.println("ADD TRIP BUTTON CLICKED");
            openDialog(null);
        });


        // =========================
        // SEARCH
        // =========================

        txttrip.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) { refresh(); }

                    @Override
                    public void removeUpdate(DocumentEvent e) { refresh(); }

                    @Override
                    public void changedUpdate(DocumentEvent e) { refresh(); }
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
                                    rs.getString("status"),
                                    "Edit",
                                    "Delete"
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
    // OPEN ADD / EDIT DIALOG
    // =========================================================

    private void openDialog(Integer tripId) {
        try {
            System.out.println(tripId == null ? "Opening Add Trip..." : "Opening Edit Trip...");
            Window owner = SwingUtilities.getWindowAncestor(this);
            AddTripDialog dialog = new AddTripDialog(owner, tripId);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                System.out.println("Trip saved.");
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


    // =========================================================
    // EDIT TRIP
    // =========================================================

    private void editTrip(int row) {
        int tripId = (Integer) model.getValueAt(row, 0);
        openDialog(tripId);
    }


    // =========================================================
    // DELETE TRIP
    // =========================================================

    private void deleteTrip(int row) {
        int tripId = (Integer) model.getValueAt(row, 0);

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Trip ID " + tripId + "?",
                "Delete Trip",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try (
                Connection con = OracleConnector.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM TRIP WHERE trip_id = ?")
        ) {
            ps.setInt(1, tripId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Trip deleted successfully.");
            refresh();

        } catch (SQLException ex) {
            ex.printStackTrace();

            if (ex.getErrorCode() == 2292) {
                JOptionPane.showMessageDialog(
                        this,
                        "This trip cannot be deleted because tickets are already booked for it.",
                        "Cannot Delete",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not delete trip:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // =========================================================
    // INNER CLASSES: BUTTON RENDERER & EDITOR
    // =========================================================

    @FunctionalInterface
    private interface TableButtonCallback {
        void onClick(int row);
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer(Color bg) {
            setOpaque(true);
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(bg);
            setForeground(BUTTON_TEXT_COLOR);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {

        private final JButton button;
        private String label;
        private boolean isPushed;
        private final TableButtonCallback callback;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, Color bg, TableButtonCallback callback) {
            super(checkBox);
            this.callback = callback;

            button = new JButton();
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBackground(bg);
            button.setForeground(BUTTON_TEXT_COLOR);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && table != null) {
                int modelRow = table.convertRowIndexToModel(table.getEditingRow());
                SwingUtilities.invokeLater(() -> callback.onClick(modelRow));
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}