package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import controller.MaintenanceController;
import controller.MaintenanceController.Maintenance;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class MaintenanceView extends JPanel {
    private JPanel RootPanelM;
    private JPanel MainPanelM;
    private JLabel lblMRecords;
    private JButton btnMRecAdd;
    private JTextField SearchBoxM;
    private JTable table1;
    private JScrollPane TblM;
    private JButton btnMRecEdit;
    private JButton btnMRecDel;
    private JPanel searchPanel;
    private JLabel lblSearch;

    private final MaintenanceController maintenanceController = new MaintenanceController();

    // Table columns that match the Maintenance table just like previously(If I'm correct...)
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Vehicle ID", "Date", "Type", "Cost", "Next Due", "Status"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public MaintenanceView() {
        setLayout(new BorderLayout());
        add(RootPanelM, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        btnMRecAdd.addActionListener(e -> openDialog());

        // Live search: reloads the table every time the text changes
        SearchBoxM.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        refresh();
    }

    // Reload maintenance records through MaintenanceController
    public void refresh() {
        try {
            List<Maintenance> records = maintenanceController.getRecords(SearchBoxM.getText());

            model.setRowCount(0);
            for (Maintenance m : records) {
                model.addRow(new Object[]{
                        m.getId(),
                        m.getVehicleId(),
                        m.getDate(),
                        m.getType(),
                        m.getCost(),
                        m.getNextDue(),
                        m.getStatus()
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not load maintenance records:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Open the `actual AddMaintenanceDialog Form
    private void openDialog() {
        try {
            Window owner = SwingUtilities.getWindowAncestor(this);

            AddMaintenanceDialog dialog = new AddMaintenanceDialog(owner);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refresh();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not open the maintenance form:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getRootPanel() {
        return RootPanelM;
    }

}