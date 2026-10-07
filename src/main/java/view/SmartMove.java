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
    private JLabel SamartMove;

    private final CardLayout cards = new CardLayout();

    public SmartMove() {
        contentPanel.removeAll();            // clears the "not wired up yet" label
        contentPanel.setLayout(cards);

        // Swap each placeholder for the real view as you build it
        register(Vehicles,    "Vehicles",    placeholder("Vehicle Management"));
        register(Drivers,     "Drivers",     placeholder("Driver Management"));
        register(Maintance,   "Maintenance", placeholder("Maintenance"));
        register(Routes,      "Routes",      placeholder("Routes"));
        register(Trips,       "Trips",       placeholder("Trips"));
        register(Passengers,  "Passengers",  placeholder("Passengers"));
        register(Tickets,     "Tickets",     placeholder("Tickets"));
        register(Payments,    "Payments",    placeholder("Payments"));
        register(Feedbacks,   "Feedback",    placeholder("Feedback"));

        cards.show(contentPanel, "Vehicles");   // default screen on startup
    }

    public JPanel getRootPanel() {
        return panel1;
    }

    private void register(JButton button, String name, JComponent view) {
        contentPanel.add(view, name);
        button.addActionListener(e -> cards.show(contentPanel, name));
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