package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Calendar;

public class EditPaymentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblID;
    private JLabel lblTicketID;
    private JLabel lblAmount;
    private JLabel lblDate;
    private JComboBox selectPayment;
    private JComboBox selectTicket;
    private JTextField txtAmount;
    private JSpinner selectDate;

    public EditPaymentDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setupDatePickerOnly();
        populateTicketIds();
        loadPaymentIds();
        selectPayment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer selectedPaymentId = (Integer) selectPayment.getSelectedItem();
                if (selectedPaymentId != null) {
                    loadExistingPaymentData(selectedPaymentId);
                }
            }
        });
        if (selectPayment.getItemCount() > 0) {
            selectPayment.setSelectedIndex(0);
        }

        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePaymentChanges();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void setupDatePickerOnly() {
        SpinnerDateModel dateModel = new SpinnerDateModel(
                new java.util.Date(),
                null,
                null,
                Calendar.DATE
        );
        selectDate.setModel(dateModel);

        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(selectDate, "yyyy-MM-dd");
        selectDate.setEditor(dateEditor);
    }

    private void populateTicketIds() {
        selectTicket.removeAllItems();
        String sql = "SELECT ticket_id FROM TICKET ORDER BY ticket_id";
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                selectTicket.addItem(rs.getInt("ticket_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error initializing Ticket tracking items:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentIds() {
        selectPayment.removeAllItems();
        String sql = "SELECT payment_id FROM PAYMENT ORDER BY payment_id";
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                selectPayment.addItem(rs.getInt("payment_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error initializing Payment index items:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadExistingPaymentData(int paymentId) {
        String sql = "SELECT ticket_id, amount, payment_date FROM PAYMENT WHERE payment_id = ?";
        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    selectTicket.setSelectedItem(rs.getInt("ticket_id"));
                    txtAmount.setText(String.format("%.2f", rs.getDouble("amount")));

                    Date pDate = rs.getDate("payment_date");
                    if (pDate != null) {
                        selectDate.setValue(new java.util.Date(pDate.getTime()));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading target item data logs:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePaymentChanges() {
        Integer selectedPaymentId = (Integer) selectPayment.getSelectedItem();
        Integer selectedTicketId = (Integer) selectTicket.getSelectedItem();

        if (selectedPaymentId == null || selectedTicketId == null) {
            JOptionPane.showMessageDialog(this, "Key index fields must contain selections.",
                    "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String amountText = txtAmount.getText().trim();
        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount parameter space cannot remain blank.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive decimal currency amount.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        java.util.Date spinnerDate = (java.util.Date) selectDate.getValue();
        java.sql.Date sqlDate = new java.sql.Date(spinnerDate.getTime());

        String sql = "UPDATE PAYMENT SET ticket_id = ?, amount = ?, payment_date = ? WHERE payment_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selectedTicketId);
            ps.setDouble(2, amount);
            ps.setDate(3, sqlDate);
            ps.setInt(4, selectedPaymentId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Payment parameters updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database transaction update rejected:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
