
package view;

import controller.RouteController;
import controller.RouteController.Route;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.SQLException;
import java.util.List;

public class RouteView extends JPanel {

    private JPanel RootPanel;
    private JButton button1;
    private JTextField txtroute;
    private JTable table1;

    private final RouteController routeController = new RouteController();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Start", "Destination"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public RouteView() {
        setLayout(new BorderLayout());
        add(RootPanel, BorderLayout.CENTER);

        table1.setModel(model);
        table1.setRowHeight(32);
        table1.setFillsViewportHeight(true);
        table1.setShowVerticalLines(false);
        table1.setGridColor(Color.decode("#828C9B"));
        table1.setForeground(Color.decode("#191A1C"));

        button1.addActionListener(e -> {
            AddRouteDialog dialog =
                    new AddRouteDialog(SwingUtilities.getWindowAncestor(this));

            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refresh();
            }
        });

        refresh();
    }

    // Load routes through RouteController
    public void refresh() {
        try {
            List<Route> routes = routeController.getAllRoutes();

            model.setRowCount(0);

            for (Route route : routes) {
                model.addRow(new Object[]{
                        route.getId(),
                        route.getStart(),
                        route.getDestination()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load routes:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
