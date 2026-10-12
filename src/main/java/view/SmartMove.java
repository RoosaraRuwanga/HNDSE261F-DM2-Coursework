package view;

import javax.swing.*;
import java.awt.*;

public class SmartMove {
    private JPanel panel1;
    private JPanel navigationPanel;
    private JPanel contentPanel;
    private JButton Trips;
    private JButton Vehicles;
    private JButton Drivers;
    private JButton Maintance;
    private JButton Routes;
    private JButton Feedbacks;
    private JButton Passengers;
    private JButton Tickets;
    private JButton Payments;
    private JLabel SmartMove;

    private final CardLayout cards = new CardLayout();

    public SmartMove() {
        contentPanel.removeAll();
        contentPanel.setLayout(cards);

        // Real views (each reloads its data when its button is clicked)
        // Tickets view
        TicketView ticketView = new TicketView();
        register(Tickets, "Tickets", ticketView, ticketView::refresh);

        PassengerView passengerView = new PassengerView();
        register(Passengers, "Passengers", passengerView, passengerView::refresh);

        RouteView routeView = new RouteView();
        register(Routes, "Routes", routeView, routeView::refresh);

        TripView tripView = new TripView();
        register(Trips, "Trips", tripView, tripView::refresh);

        // Feedback: borrow the panel from the teammate's JFrame
        FeedbackView feedbackView = new FeedbackView();
        feedbackView.dispose();
        register(Feedbacks, "Feedback", (JComponent) feedbackView.getContentPane());

        // Placeholders until these views are built
        register(Vehicles,  "Vehicles",    placeholder("Vehicle Management"));
        register(Drivers,   "Drivers",     placeholder("Driver Management"));
        register(Maintance, "Maintenance", placeholder("Maintenance"));
        register(Payments,  "Payments",    placeholder("Payments"));

        cards.show(contentPanel, "Routes");      // screen shown at startup
    }

    public JPanel getRootPanel() {
        return panel1;
    }

    private void register(JButton button, String name, JComponent view) {
        register(button, name, view, null);
    }

    // onShow runs every time the button is clicked (use it to reload data)
    private void register(JButton button, String name, JComponent view, Runnable onShow) {
        contentPanel.add(view, name);
        button.addActionListener(e -> {
            if (onShow != null) onShow.run();
            cards.show(contentPanel, name);
        });
    }

    private JPanel placeholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.decode("#FFE0DB"));
        p.add(new JLabel(title + " (not built yet)"));
        return p;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SmartMove Transport Solutions");
            frame.setContentPane(new SmartMove().getRootPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}