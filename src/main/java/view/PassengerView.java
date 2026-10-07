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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;

public class PassengerView extends JPanel {
    private JPanel headerPanel;
    private JButton addPassengerButton;
    private JTextField searchBoxTextField;
    private JPanel rootPanel;
    private JTable table1;

    // columns 4 and 5 hold the Edit and Delete buttons
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact", "Tickets", "", ""}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return col >= 4; }
    };

    // in-memory changes layered on top of Oracle (lost when the app closes)
    private final List<Object[]> localRows = new ArrayList<>();       // passengers added here
    private final Map<Integer, String[]> edits = new HashMap<>();     // Oracle id -> {name, contact}
    private final Set<Integer> deletedIds = new HashSet<>();          // Oracle ids hidden

    public PassengerView() {
        setLayout(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        new ButtonColumn(table1, 4, "Edit",   Color.decode("#40434A"), this::editRow);
        new ButtonColumn(table1, 5, "Delete", Color.decode("#AD353F"), this::deleteRow);
        table1.getColumnModel().getColumn(4).setMaxWidth(90);
        table1.getColumnModel().getColumn(5).setMaxWidth(90);

        searchBoxTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refresh(); }
            public void removeUpdate(DocumentEvent e)  { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        addPassengerButton.addActionListener(e -> showAddDialog());
        refresh();
    }

    public void refresh() {
        String sql = "SELECT p.passenger_id, p.passenger_name, p.contact, COUNT(tk.ticket_id) "
                + "FROM PASSENGER p LEFT JOIN TICKET tk ON tk.passenger_id = p.passenger_id "
                + "GROUP BY p.passenger_id, p.passenger_name, p.contact "
                + "ORDER BY p.passenger_id";

        String term = searchBoxTextField.getText().trim().toLowerCase();

        try (Connection c = OracleConnector.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            model.setRowCount(0);
            while (rs.next()) {
                int id = rs.getInt(1);
                if (deletedIds.contains(id)) continue;           // deleted in this session

                String name = rs.getString(2);
                String contact = rs.getString(3);
                String[] edit = edits.get(id);
                if (edit != null) { name = edit[0]; contact = edit[1]; }   // edited in this session
                if (name == null) name = "";

                if (name.toLowerCase().contains(term)) {
                    model.addRow(new Object[]{id, name, contact, rs.getInt(4)});
                }
            }

            for (Object[] r : localRows) {                       // added in this session
                if (((String) r[1]).toLowerCase().contains(term)) {
                    model.addRow(r);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load passengers:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        AddPassengerDialog dialog = new AddPassengerDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return;

        int nextId = 1;
        for (int i = 0; i < model.getRowCount(); i++) {
            nextId = Math.max(nextId, (Integer) model.getValueAt(i, 0) + 1);
        }
        for (Object[] r : localRows) {
            nextId = Math.max(nextId, (Integer) r[0] + 1);
        }

        localRows.add(new Object[]{nextId, dialog.getPassengerName(), dialog.getContact(), 0});
        refresh();
    }

    private void editRow(int row) {
        int id = (Integer) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        String contact = (String) model.getValueAt(row, 2);

        AddPassengerDialog dialog = new AddPassengerDialog(
                SwingUtilities.getWindowAncestor(this), name, contact);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) return;

        Object[] local = findLocal(id);
        if (local != null) {                                     // a passenger added in this session
            local[1] = dialog.getPassengerName();
            local[2] = dialog.getContact();
        } else {                                                 // a passenger from Oracle
            edits.put(id, new String[]{dialog.getPassengerName(), dialog.getContact()});
        }
        refresh();
    }

    private void deleteRow(int row) {
        int id = (Integer) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete passenger \"" + name + "\"?",
                "Delete passenger", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        Object[] local = findLocal(id);
        if (local != null) {
            localRows.remove(local);
        } else {
            deletedIds.add(id);
        }
        refresh();
    }

    private Object[] findLocal(int id) {
        for (Object[] r : localRows) {
            if ((Integer) r[0] == id) return r;
        }
        return null;
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