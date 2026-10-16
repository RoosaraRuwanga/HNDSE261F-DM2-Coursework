package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class RemovePaymentDialog extends JDialog {
    private JPanel contentPane;
    private JButton btnRemovePayment;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblPaymentID;
    private JComboBox selectPayment;

    public RemovePaymentDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnRemovePayment);
        loadPaymentIds();
        btnRemovePayment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removePaymentRecord();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    private void loadPaymentIds() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT payment_id FROM PAYMENT ORDER BY payment_id ASC";
            rs = stmt.executeQuery(sql);
            selectPayment.removeAllItems();
            while (rs.next()) {
                selectPayment.addItem(rs.getString("payment_id"));
            }

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading Payment IDs: " + e.getMessage());
        }
        finally {
            closeConnections(conn, stmt, rs);
        }
    }


    private void removePaymentRecord() {
        if (selectPayment.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "There are no payments available to remove.");
            return;
        }

        String selectedPaymentId = selectPayment.getSelectedItem().toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete Payment ID: " + selectedPaymentId + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = OracleConnector.getConnection();
            String sql = "DELETE FROM PAYMENT WHERE payment_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(selectedPaymentId));
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Payment Removed Successfully!");
            dispose();

        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
        finally {
            closeConnections(conn, pstmt, null);
        }
    }

    private void closeConnections(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            if (c != null) c.close();
        }
        catch (Exception e) {
        }
    }
}
