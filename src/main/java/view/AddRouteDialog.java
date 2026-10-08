package view;

import data.OracleConnector;

import javax.swing.*;
import java.awt.Window;
import java.sql.*;

public class AddRouteDialog extends JDialog {
    private JPanel RootPanel;
    private JTextField txtStart;
    private JTextField txtDestination;
    private JButton addButton;
    private JButton cancelButton;

    private final Integer editId;   // null = adding a new route
    private boolean saved = false;

    // Add mode
    public AddRouteDialog(Window owner) {
        this(owner, null, "", "");
    }

    // Edit mode: pre-fills the fields with the selected route
    public AddRouteDialog(Window owner, Integer routeId, String start, String destination) {
        super(owner, routeId == null ? "Add Route" : "Edit Route", ModalityType.APPLICATION_MODAL);
        this.editId = routeId;
        setContentPane(RootPanel);

        txtStart.setText(start);
        txtDestination.setText(destination);
        if (editId != null) addButton.setText("Save");

        addButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(addButton);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void save() {
        String start = txtStart.getText().trim();
        String destination = txtDestination.getText().trim();

        if (start.isEmpty() || destination.isEmpty()) {
            error("Start and destination are required.");
            return;
        }

        String sql;
        if (editId == null) {
            // next ID = highest existing ID + 1 (no sequence needed)
            sql = "INSERT INTO ROUTE (route_id, start_location, destination) " +
                    "SELECT NVL(MAX(route_id), 0) + 1, ?, ? FROM ROUTE";
        } else {
            sql = "UPDATE ROUTE SET start_location = ?, destination = ? WHERE route_id = ?";
        }

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, destination);
            if (editId != null) ps.setInt(3, editId);
            ps.executeUpdate();

            saved = true;
            dispose();
        } catch (SQLException ex) {
            error("Could not save route: " + ex.getMessage());
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}