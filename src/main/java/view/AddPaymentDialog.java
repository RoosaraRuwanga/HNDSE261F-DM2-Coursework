package view;

import javax.swing.*;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import data.OracleConnector;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AddPaymentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblPAssenger;
    private JLabel lblAMount;
    private JTextField txtAmount;
    private JComboBox selectTicket;

    public AddPaymentDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        loadUnpaidTickets();

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePayment();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadUnpaidTickets() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT ticket_id FROM TICKET WHERE ticket_id NOT IN (SELECT ticket_id FROM PAYMENT WHERE ticket_id IS NOT NULL) ORDER BY ticket_id";
            rs = stmt.executeQuery(sql);
            selectTicket.removeAllItems();
            while (rs.next()) {
                selectTicket.addItem(rs.getString("ticket_id"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading unpaid tickets: " + e.getMessage());
        } finally {
            closeConnections(conn, stmt, rs);
        }
    }

    private void savePayment() {
        if (selectTicket.getSelectedItem() == null || txtAmount.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Please select a Ticket ID and enter the Amount.");
            return;
        }

        String selectedTicketId = selectTicket.getSelectedItem().toString();
        String amountText = txtAmount.getText();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = OracleConnector.getConnection();
            String sql = "INSERT INTO PAYMENT (payment_id, ticket_id, amount, payment_date) " +
                    "VALUES ((SELECT NVL(MAX(payment_id), 0) + 1 FROM PAYMENT), ?, ?, SYSDATE)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(selectedTicketId));
            pstmt.setDouble(2, Double.parseDouble(amountText));
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Payment Recorded Successfully!");
            dispose();

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric value for the Amount.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            closeConnections(conn, pstmt, null);
        }
    }


    private void closeConnections(Connection c, Statement s, ResultSet r) {
        try {
            if (r != null) r.close();
            if (s != null) s.close();
            if (c != null) c.close();
        } catch (Exception e) {
        }
    }
    public static boolean hasUnpaidTickets() {
        String sql = "SELECT COUNT(*) FROM TICKET WHERE ticket_id NOT IN " +
                "(SELECT ticket_id FROM PAYMENT WHERE ticket_id IS NOT NULL)";
        try (Connection conn = OracleConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
