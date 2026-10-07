package view;

import controller.FeedbackController;
import javax.swing.*;
import java.sql.SQLException;

public class FeedbackView extends JFrame{
    private JPanel MainPanel;
    private JLabel LabelTitle;
    private JSpinner spnPID;
    private JSpinner spnRID;
    private JSpinner spnDID;
    private JSpinner spnVID;
    private JSpinner spnRating;
    private JTextField txtComment;
    private JButton btnSubmit;
    private JTextField txtSearch;
    private JSpinner spnRouteSearch;
    private JButton btnSearch;
    private JButton btnSortRoute;
    private JButton btnSortHighest;
    private JScrollPane scroll;
    private JTable tblFeedback;
    private FeedbackController feedbackService = new FeedbackController();

    public FeedbackView() {
        setTitle("SmartMove Transport Solutions");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 720);
        setContentPane(MainPanel);
        setLocationRelativeTo(null);
        setVisible(true);


        // force spinner to be 1 - 5 stars
        spnRating.setValue(1);
        spnRating.addChangeListener(e -> {
            SpinnerNumberModel model = (SpinnerNumberModel) spnRating.getModel();
            int rating = model.getNumber().intValue();

            if (rating < 1) {
                rating = 1;
            }
            else if (rating > 5) {
                rating = 5;
            }
            spnRating.setValue(rating);
        });

        btnSubmit.addActionListener(e -> {
            try {
                int passengerId = (int) spnPID.getValue();
                int routeId = (int) spnRID.getValue();
                int vehicleId = (int) spnVID.getValue();
                int driverId = (int) spnDID.getValue();
                int rating = (int) spnRating.getValue();
                String comment = txtComment.getText().trim();

                feedbackService.submitFeedback(passengerId, routeId, vehicleId, driverId, rating, comment);
                JOptionPane.showMessageDialog(this, "Feedback submitted successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for ID/rating fields.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error checking database: " + ex.getMessage());
            }
        });
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            tblFeedback.setModel(feedbackService.searchFeedbackByKeyword(keyword));
        });

        btnSortRoute.addActionListener(e -> {
            try {
                int routeId = (int) spnRouteSearch.getValue();
                tblFeedback.setModel(feedbackService.getFeedbackByRoute(routeId));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric Route ID.");
            }
        });

        btnSortHighest.addActionListener(e -> {
            tblFeedback.setModel(feedbackService.getHighestRated());
        });

    }
    public static void main(String[] args){
        new FeedbackView();
    }
}
