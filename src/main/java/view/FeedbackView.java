package view;

import controller.FeedbackController;
import controller.FeedbackController.RouteOption;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.sql.SQLException;

public class FeedbackView extends JPanel {

    private JPanel RootPanel;
    private JPanel headerPanel;
    private JTextField txtSearch;
    private JComboBox<RouteOption> cmbRoute;
    private JComboBox<String> cmbRating;
    private JButton btnSearch;
    private JButton btnReset;
    private JTable tbleFeedBack;
    private JLabel Search;
    private JLabel Route;
    private JLabel Rating;

    private final FeedbackController controller = new FeedbackController();

    public FeedbackView() {
        // TEMPORARY: lists every field the form did not bind. Delete once nothing prints.
        for (var f : FeedbackView.class.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                if (f.get(this) == null) {
                    System.out.println("UNBOUND: " + f.getName());
                }
            } catch (IllegalAccessException ignored) { }
        }

        setLayout(new BorderLayout());
        add(RootPanel, BorderLayout.CENTER);

        // table setup
        tbleFeedBack.setRowHeight(32);
        tbleFeedBack.setFillsViewportHeight(true);
        tbleFeedBack.setAutoCreateRowSorter(true); // click a header to sort

        // show the rating column as stars, same as the passenger form
        tbleFeedBack.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                int r = value instanceof Number ? ((Number) value).intValue() : 0;
                setText("\u2605".repeat(r) + "\u2606".repeat(Math.max(0, 5 - r)));
            }
        });

        cmbRating.setModel(new DefaultComboBoxModel<>(
                new String[]{"All ratings", "5", "4", "3", "2", "1"}));
        loadRoutes();

        // events
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbRoute.setSelectedIndex(0);
            cmbRating.setSelectedIndex(0);
            refresh();
        });

        refresh();
    }

    // Reload the table from MongoDB, applying the filters
    public void refresh() {
        RouteOption route = (RouteOption) cmbRoute.getSelectedItem();
        int routeId = route == null ? 0 : route.id;
        int rating = cmbRating.getSelectedIndex() <= 0
                ? 0
                : Integer.parseInt((String) cmbRating.getSelectedItem());

        try {
            DefaultTableModel model =
                    controller.getFeedback(txtSearch.getText(), routeId, rating);
            tbleFeedBack.setModel(model);
            setColumnWidths();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load feedback:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadRoutes() {
        try {
            DefaultComboBoxModel<RouteOption> model = new DefaultComboBoxModel<>();
            model.addElement(RouteOption.ALL);
            for (RouteOption r : controller.getRouteOptions()) {
                model.addElement(r);
            }
            cmbRoute.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load routes:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setColumnWidths() {
        int[] widths = {130, 120, 170, 150, 120, 90, 400};
        for (int i = 0; i < widths.length && i < tbleFeedBack.getColumnCount(); i++) {
            tbleFeedBack.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public JPanel getRootPanel() {
        return RootPanel;
    }
}