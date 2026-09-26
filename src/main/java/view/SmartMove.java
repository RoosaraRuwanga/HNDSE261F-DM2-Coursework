package view;

import controller.PassengerController;
import controller.RouteController;
import controller.TripController;

import javax.swing.*;
import java.awt.*;

public class SmartMove extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JButton button1;

    public SmartMove() {
        setTitle("SmartMove Transport Solutions");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(placeholder("Vehicle Management"), "Vehicles");
        contentPanel.add(placeholder("Driver Management"), "Drivers");
        contentPanel.add(placeholder("Maintenance Management"), "Maintenance");

        RouteView routeView = new RouteView();
        new RouteController(routeView);
        contentPanel.add(routeView, "Routes");

        TripView tripView = new TripView();
        new TripController(tripView);
        contentPanel.add(tripView, "Trips");

        PassengerView passengerView = new PassengerView();
        new PassengerController(passengerView);
        contentPanel.add(passengerView, "Passengers");

        contentPanel.add(placeholder("Ticket Booking"), "Tickets");
        contentPanel.add(placeholder("Payment Processing"), "Payments");
        contentPanel.add(placeholder("Feedback & Review"), "Feedback");

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "Vehicles"); // whatever screen should open first

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(160, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 10, 10, 10));

        JLabel brand = new JLabel("SmartMove");
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(14));

        sidebar.add(navButton("Vehicles"));
        sidebar.add(navButton("Drivers"));
        sidebar.add(navButton("Maintenance"));
        sidebar.add(navButton("Routes"));
        sidebar.add(navButton("Trips"));
        sidebar.add(navButton("Passengers"));
        sidebar.add(navButton("Tickets"));
        sidebar.add(navButton("Payments"));
        sidebar.add(navButton("Feedback"));

        return sidebar;
    }

    private JButton navButton(String cardName) {
        JButton button = new JButton(cardName);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        button.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        return button;
    }

    private JPanel placeholder(String moduleName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(moduleName + " — not wired up yet", SwingConstants.CENTER), BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartMove::new);
    }
}