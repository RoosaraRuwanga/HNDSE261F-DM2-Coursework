package view;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.factories.CC;
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
    private JButton btnReport_Driver;
    private JTable tblReport;
    private JScrollPane scroll;
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

        btnReport_Driver.addActionListener(e -> {
            try {
                tblReport.setModel(reportService.getDriverActivity());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading report: " + ex.getMessage());
            }
        });
    }

    public static void main(String[] args){
        new ReportView();
    }

}
