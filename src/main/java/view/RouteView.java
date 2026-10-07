package view;

import data.OracleConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.*;

public class RouteView extends JPanel {

    private JPanel RootPanel;
    private JButton button1;
    private JTextField txtroute;
    private JTable table1;

    private final DefaultTableModel model;

    public RouteView() {
        setLayout(new BorderLayout());
        add(RootPanel, BorderLayout.CENTER);

        model = new DefaultTableModel(new String[]{"ID", "Start", "Destination"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table1.setModel(model);

        button1.addActionListener(e -> addRoute());
        refresh();
    }

    // loads the ROUTE table into the grid (called on startup, on button click, and after adding)
    public void refresh() {
        model.setRowCount(0);
        String sql = "SELECT route_id, start_location, destination FROM ROUTE ORDER BY route_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("route_id"),
                        rs.getString("start_location"),
                        rs.getString("destination")
                });
            }
        } catch (SQLException ex) {
            showError("Could not load routes: " + ex.getMessage());
        }
    }

    // expects text like "Colombo - Kandy"
    private void addRoute() {
        String[] parts = txtroute.getText().trim().split("\\s*-\\s*");

        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            showError("Enter the route as: Start - Destination (e.g. Colombo - Kandy)");
            return;
        }

        String sql = "INSERT INTO ROUTE (route_id, start_location, destination) " +
                "VALUES (route_seq.NEXTVAL, ?, ?)";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, parts[0]);
            ps.setString(2, parts[1]);
            ps.executeUpdate();

            txtroute.setText("");
            refresh();
        } catch (SQLException ex) {
            showError("Could not add route: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}