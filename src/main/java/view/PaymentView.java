package view;

import data.OracleConnector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PaymentView {
    private JLabel lblHeading;
    private JButton btnAddPayment;
    private JTextField txtSearchPayments;
    private JTable tblPayments;
    private JPanel Panel;
    private JLabel lblSearchPayments;

    public PaymentView() {
        String[] columns = {"Payment ID", "Ticket ID", "Amount", "Payment Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        tblPayments.setModel(model);
        loadData();

        btnAddPayment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddPaymentDialog dialog = new AddPaymentDialog();
                dialog.pack();
                dialog.setVisible(true);
                loadData();
            }
        });

        txtSearchPayments.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadData();
            }
        });
    }

    public void loadData() {
        DefaultTableModel model = (DefaultTableModel) tblPayments.getModel();
        model.setRowCount(0);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();
            String searchText = txtSearchPayments.getText();

            String sql = "SELECT payment_id, ticket_id, amount, payment_date FROM PAYMENT";

            if (!searchText.equals("")) {
                sql = sql + " WHERE payment_id LIKE '%" + searchText + "%' " +
                        " OR ticket_id LIKE '%" + searchText + "%' " +
                        " OR amount LIKE '%" + searchText + "%'";
            }
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int paymentId = rs.getInt("payment_id");
                int ticketId = rs.getInt("ticket_id");
                double amount = rs.getDouble("amount");
                String payDate = rs.getString("payment_date");

                Object[] row = {paymentId, ticketId, amount, payDate};
                model.addRow(row);
            }

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
        finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            catch (Exception ex) {
            }
        }
    }
    //temporary main method for testing
    //public static void main(String[] args) {
       // JFrame frame = new JFrame("PaymentView");
       // PaymentView viewInstance = new PaymentView();
       // frame.setContentPane(viewInstance.Panel);
       // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      //  frame.pack();
      //  frame.setVisible(true);
   // }
}
