package view;

import data.OracleConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TicketView {
    private JTextField txtSearchTicket;
    private JTable tblTickets;
    private JPanel RootPanel;
    private JButton addTicketsButton;
    private JButton btnChangeStatus;

    public TicketView(){
        String[] columns = {"ID", "Passenger ID", "Trip ID", "Seat", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        tblTickets.setModel(model);
        loadData();

        addTicketsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddTicketDialog dialog = new AddTicketDialog();
                dialog.pack();
                dialog.setVisible(true);
                loadData();
            }
        });

        txtSearchTicket.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadData();
            }
        });

        btnChangeStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeStatusDialog dialog = new ChangeStatusDialog();
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                loadData();
            }
        });
    }

    public void loadData(){
        DefaultTableModel model = (DefaultTableModel) tblTickets.getModel();
        model.setRowCount(0);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();
            String searchText = txtSearchTicket.getText();
            String sql = "SELECT ticket_id, passenger_id, trip_id, seat_no, status FROM TICKET";

            if (!searchText.equals("")) {
                sql = sql + " WHERE ticket_id LIKE '%" + searchText + "%' " +
                        " OR seat_no LIKE '%" + searchText + "%' " +
                        " OR status LIKE '%" + searchText + "%'";
            }
            rs = stmt.executeQuery(sql);

            while(rs.next()){
                int id = rs.getInt("ticket_id");
                int passenger = rs.getInt("passenger_id");
                int trip = rs.getInt("trip_id");
                String seat = rs.getString("seat_no");
                String status = rs.getString("status");
                Object[] row = {id, passenger, trip, seat, status};
                model.addRow(row);
            }

        }
        catch (Exception e){
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
        finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
            }
        }
    }
    //Main method for testing purposes
    //public static void main(String[] args) {
    //JFrame frame = new JFrame("TicketView");
    // TicketView viewInstance = new TicketView();
    // frame.setContentPane(viewInstance.RootPanel);
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.pack();
    // frame.setVisible(true);
    //}

    // added so SmartMove can place this screen in its content panel
    public JPanel getRootPanel() {
        return RootPanel;
    }

    // added so SmartMove can reload the table when the Tickets button is clicked
    public void refresh() {
        loadData();
    }
}