package view;

import javax.swing.*;
import java.sql.Date;
import java.sql.SQLException;
import controller.ReportService;

public class ReportView extends JFrame {
    private JPanel MainPanel;
    private JLabel LabelTitle;
    private JButton btnReport_PopRoutes;
    private JButton btnReport_Revenue;
    private JButton btnReport_PassengerHistory;
    private JButton btnReport_Maintenance;
    private JTable tblReport;
    private JScrollPane scroll;
    private JTextField txtStartDate;
    private JTextField txtPassengerID;
    private JTextField txtEndDate;
    private ReportService reportService = new ReportService();

    public ReportView() {
        setTitle("SmartMove Transport Solutions");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setContentPane(MainPanel);
        setLocationRelativeTo(null);
        setVisible(true);

        btnReport_PopRoutes.addActionListener(e -> {
            try {
                tblReport.setModel(reportService.getPopularRoutes());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage());
            }
        });

        btnReport_Maintenance.addActionListener(e -> {
            try {
                tblReport.setModel(reportService.getMaintenanceDue());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage());
            }
        });

        btnReport_PassengerHistory.addActionListener(e -> {
            try {
                // Using trim here to cut off blank spaces, note to other members to PLEASE use this
                String input = txtPassengerID.getText().trim();
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a Passenger ID.");
                    return;
                }

                int passengerId = Integer.parseInt(input);
                tblReport.setModel(reportService.getPassengerHistory(passengerId));

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage());
            }
        });

        btnReport_Revenue.addActionListener(e -> {
            try {
                String startInput = txtStartDate.getText().trim();
                String endInput = txtEndDate.getText().trim();

                if (startInput.isEmpty() || endInput.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter both start and end dates.");
                    return;
                }

                Date startDate = Date.valueOf(startInput);
                Date endDate = Date.valueOf(endInput);
                tblReport.setModel(reportService.getRevenue(startDate, endDate));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage());
            }
        });

    }

    public static void main(String[] args){
        new ReportView();
    }

}
