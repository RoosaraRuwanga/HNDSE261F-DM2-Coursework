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

public class ChangeStatusDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblTicket;
    private JLabel lblStatus;
    private JComboBox selectTicket;
    private JComboBox selectStatus;

    public ChangeStatusDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        selectStatus.addItem("Booked");
        selectStatus.addItem("Pending");
        selectStatus.addItem("Cancelled");
        loadTicketIds();

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTicketStatus();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadTicketIds() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = OracleConnector.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT ticket_id FROM TICKET ORDER BY ticket_id ASC");

            selectTicket.removeAllItems();
            while (rs.next()) {
                selectTicket.addItem(rs.getString("ticket_id"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading Ticket IDs: " + e.getMessage());
        } finally {
            closeConnections(conn, stmt, rs);
        }
    }

    private void updateTicketStatus() {
        if (selectTicket.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a Ticket ID first.");
            return;
        }

        String selectedTicketId = selectTicket.getSelectedItem().toString();
        String chosenStatus = selectStatus.getSelectedItem().toString();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = OracleConnector.getConnection();
            String sql = "UPDATE TICKET SET status = ? WHERE ticket_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, chosenStatus);
            pstmt.setInt(2, Integer.parseInt(selectedTicketId));
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Ticket Status Updated Successfully!");
            }
            dispose();
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

}
