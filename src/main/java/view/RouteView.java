package view;

import data.OracleConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.IntConsumer;

public class RouteView extends JPanel {

    private JPanel RootPanel;
    private JButton button1;       // Add Route
    private JTextField txtroute;   // unused now, kept so the form still binds
    private JTable table1;

    // columns 3 and 4 hold the Edit and Delete buttons
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Start", "Destination", "", ""}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return col >= 3; }
    };

    public RouteView() {
        setLayout(new BorderLayout());
        add(RootPanel, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        new ButtonColumn(table1, 3, "Edit",   Color.decode("#40434A"), this::editRow);
        new ButtonColumn(table1, 4, "Delete", Color.decode("#AD353F"), this::deleteRow);
        table1.getColumnModel().getColumn(3).setMaxWidth(90);
        table1.getColumnModel().getColumn(4).setMaxWidth(90);

        button1.addActionListener(e -> {
            AddRouteDialog dialog = new AddRouteDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isSaved()) refresh();
        });

        refresh();
    }

    public void refresh() {
        String sql = "SELECT route_id, start_location, destination FROM ROUTE ORDER BY route_id";

        try (Connection c = OracleConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("route_id"),
                        rs.getString("start_location"),
                        rs.getString("destination")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load routes:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editRow(int row) {
        int id = (Integer) model.getValueAt(row, 0);
        String start = (String) model.getValueAt(row, 1);
        String destination = (String) model.getValueAt(row, 2);

        AddRouteDialog dialog = new AddRouteDialog(
                SwingUtilities.getWindowAncestor(this), id, start, destination);
        dialog.setVisible(true);
        if (dialog.isSaved()) refresh();
    }

    private void deleteRow(int row) {
        int id = (Integer) model.getValueAt(row, 0);
        String name = model.getValueAt(row, 1) + " - " + model.getValueAt(row, 2);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete route \"" + name + "\"?",
                "Delete route", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        try (Connection c = OracleConnector.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM ROUTE WHERE route_id = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
            refresh();
        } catch (SQLException ex) {
            // ORA-02292: child record found (a TRIP uses this route)
            String msg = ex.getErrorCode() == 2292
                    ? "This route can't be deleted because trips are using it."
                    : "Could not delete route:\n" + ex.getMessage();
            JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Puts a clickable button in every row of one table column. */
    private static class ButtonColumn extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor {

        private final JButton renderButton = new JButton();
        private final JButton editorButton = new JButton();
        private int editingRow;

        ButtonColumn(JTable table, int column, String text, Color background, IntConsumer onClick) {
            for (JButton b : new JButton[]{renderButton, editorButton}) {
                b.setText(text);
                b.setBackground(background);
                b.setForeground(Color.decode("#FFE0DB"));
                b.setOpaque(true);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
            }
            editorButton.addActionListener(e -> {
                int modelRow = table.convertRowIndexToModel(editingRow);
                fireEditingStopped();
                onClick.accept(modelRow);
            });
            table.getColumnModel().getColumn(column).setCellRenderer(this);
            table.getColumnModel().getColumn(column).setCellEditor(this);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                       boolean focus, int row, int col) {
            return renderButton;
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel,
                                                     int row, int col) {
            editingRow = row;
            return editorButton;
        }

        @Override public Object getCellEditorValue() { return ""; }
    }
}