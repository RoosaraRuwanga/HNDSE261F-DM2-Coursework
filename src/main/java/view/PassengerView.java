package view;

import data.OracleConnector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.function.IntConsumer;

public class PassengerView extends JPanel {
    private JPanel headerPanel;
    private JButton addPassengerButton;
    private JTextField searchBoxTextField;
    private JPanel rootPanel;
    private JTable table1;
    private JLabel lblSearch;
    private JButton editPassengerButton;
    private JButton removePassengerButton;

    // columns 4 and 5 hold the Edit and Delete buttons
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact", "Tickets"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
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

        searchBoxTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        addPassengerButton.addActionListener(e -> showAddDialog());
        editPassengerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditPassengerDialog dialog = new EditPassengerDialog();
                dialog.pack();
                dialog.setVisible(true);
                refresh();
            }
        });

        removePassengerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemovePassengerDialog dialog = new RemovePassengerDialog();
                dialog.pack();
                dialog.setVisible(true);
                refresh();
            }
        });
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
                if (edit != null) {
                    name = edit[0];
                    contact = edit[1];
                }   // edited in this session
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
        String sql = "INSERT INTO PASSENGER (passenger_id, passenger_name, contact) " +
                "VALUES (passenger_seq.NEXTVAL, ?, ?)";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dialog.getPassengerName());
            ps.setString(2, dialog.getContact());

            int inserted = ps.executeUpdate();
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this, "New passenger added to database successfully!");
                refresh();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save passenger to database:\n" + ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void editRow(int row) {
        int id = (Integer) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        String contact = (String) model.getValueAt(row, 2);
        int tickets = ((Number) model.getValueAt(row, 3)).intValue();

        AddPassengerDialog dialog = new AddPassengerDialog(
                SwingUtilities.getWindowAncestor(this),
                name,
                contact,
                tickets
        );

        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        String newName = dialog.getPassengerName();
        String newContact = dialog.getContact();
        int newTickets = dialog.getTickets();

        // Update a passenger added locally
        Object[] local = findLocal(id);

        if (local != null) {
            local[1] = newName;
            local[2] = newContact;
            local[3] = newTickets;

            refresh();
            return;
        }

        // Update passenger details in Oracle
        String sql = "UPDATE PASSENGER " +
                "SET passenger_name = ?, contact = ? " +
                "WHERE passenger_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setString(2, newContact);
            ps.setInt(3, id);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Passenger updated successfully.\n" +
                                "Ticket count entered: " + newTickets
                );

                refresh();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Passenger not found."
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not update passenger:\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

    /**
     * Puts a clickable button in every row of one table column.
     */
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

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}