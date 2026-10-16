package view;

import javax.swing.*;
import data.OracleConnector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditPassengerDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblName;
    private JLabel lblContact;
    private JLabel lblID;
    private JComboBox selectID;
    private JTextField txtName;
    private JTextField txtContact;
    private boolean confirmed = false;

    public EditPassengerDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSave);
        selectID.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer selectedId = (Integer) selectID.getSelectedItem();
                if (selectedId != null) {
                    loadPassengerDetails(selectedId);
                }
            }
        });

        loadPassengerIds();

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void loadPassengerIds() {
        selectID.removeAllItems();
        String sql = "SELECT passenger_id FROM PASSENGER ORDER BY passenger_id";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                selectID.addItem(rs.getInt("passenger_id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading IDs: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPassengerDetails(int passengerId) {
        String sql = "SELECT passenger_name, contact FROM PASSENGER WHERE passenger_id = ?";

        try (Connection con = OracleConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtName.setText(rs.getString("passenger_name"));
                    txtContact.setText(rs.getString("contact"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading details: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        Integer selectedId = (Integer) selectID.getSelectedItem();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Please select an ID.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();

        if (name.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = OracleConnector.getConnection()) {
            con.setAutoCommit(false); // Enable transactional integrity handling

            String updatePassengerSql = "UPDATE PASSENGER SET passenger_name = ?, contact = ? WHERE passenger_id = ?";
            try (PreparedStatement psPassenger = con.prepareStatement(updatePassengerSql)) {
                psPassenger.setString(1, name);
                psPassenger.setString(2, contact);
                psPassenger.setInt(3, selectedId);
                psPassenger.executeUpdate();
            }

            con.commit();
            JOptionPane.showMessageDialog(this, "Changes saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
            confirmed = true;
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database update operation rejected:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
