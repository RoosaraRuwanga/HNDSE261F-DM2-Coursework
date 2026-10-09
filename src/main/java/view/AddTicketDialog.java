package view;

import javax.swing.*;

public class AddTicketDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblPassenger;
    private JTextField txtPassenger;
    private JLabel lblTrip;
    private JTextField textField1;
    private JLabel lblSeat;
    private JComboBox SelectSeat;
    private JLabel lblStatus;
    private JComboBox SelectStatus;

    public AddTicketDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }
}
