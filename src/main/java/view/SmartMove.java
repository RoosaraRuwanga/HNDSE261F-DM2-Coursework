package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class SmartMove {
    private JPanel panel1;
    private JPanel navigationPanel;
    private JPanel contentPanel;
    private JButton Trips;
    private JButton Vehicles;
    private JButton Drivers;
    private JButton Maintenance;
    private JButton Routes;
    private JButton Feedbacks;
    private JButton Passengers;
    private JButton Tickets;
    private JButton Payments;
    private JButton Reports;
    private JLabel SmartMove;

    private final CardLayout cards = new CardLayout();

    public SmartMove() {
        contentPanel.removeAll();
        contentPanel.setLayout(cards);

        // Tickets
        TicketView ticketView = new TicketView();
        register(Tickets, "Tickets",
                ticketView.getRootPanel(), ticketView::refresh);

        // Maintenance
        MaintenanceView maintenanceView = new MaintenanceView();
        register(Maintenance, "Maintenance",
                maintenanceView.getRootPanel(), maintenanceView::refresh);

        // Payments
        PaymentView paymentView = new PaymentView();
        register(Payments, "Payments",
                paymentView.getRootPanel(), paymentView::refresh);

        // Drivers
        DriverView driverView = new DriverView();
        register(Drivers, "Drivers",
                driverView.getRootPanel(), driverView::refresh);

        // Passengers
        PassengerView passengerView = new PassengerView();
        register(Passengers, "Passengers",
                passengerView, passengerView::refresh);

        // Routes
        RouteView routeView = new RouteView();
        register(Routes, "Routes",
                routeView, routeView::refresh);

        // Trips
        TripView tripView = new TripView();
        register(Trips, "Trips",
                tripView, tripView::refresh);

        // Vehicles
        VehicleView vehicleView = new VehicleView();
        register(Vehicles, "Vehicles",
                vehicleView.getRootPanel(), vehicleView::refresh);

        // Reports
        ReportView reportView = new ReportView();
        register(Reports, "Reports",
                reportView.getRootPanel(), reportView::refresh);

        // Feedback
        FeedbackView feedbackView = new FeedbackView();
        JComponent feedbackPanel =
                (JComponent) feedbackView.getContentPane();
        feedbackView.dispose();

        register(Feedbacks, "Feedback", feedbackPanel);

        // Show the Vehicles screen at startup
        vehicleView.refresh();
        cards.show(contentPanel, "Vehicles");

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public JPanel getRootPanel() {
        return panel1;
    }

    private void register(
            JButton button,
            String name,
            JComponent view) {

        register(button, name, view, null);
    }

    private void register(
            JButton button,
            String name,
            JComponent view,
            Runnable onShow) {

        contentPanel.add(view, name);

        button.addActionListener(e -> {
            if (onShow != null) {
                onShow.run();
            }

            cards.show(contentPanel, name);
            contentPanel.revalidate();
            contentPanel.repaint();
        });
    }

    private JPanel placeholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.decode("#FFE0DB"));
        p.add(new JLabel(title + " (not built yet)"));
        return p;
    }

    private void createUIComponents() {
        // Custom component creation, if required
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame =
                    new JFrame("SmartMove Transport Solutions");

            frame.setContentPane(new SmartMove().getRootPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}