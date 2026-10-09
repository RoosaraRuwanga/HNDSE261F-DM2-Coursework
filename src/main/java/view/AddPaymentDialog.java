package view;

import javax.swing.*;

public class AddPaymentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblHeading;
    private JLabel lblPAssenger;
    private JTextField txtPassenger;
    private JLabel lblAMount;
    private JTextField txtAmount;
    private JLabel lblMethod;
    private JComboBox selectMethod;
    private JLabel lblTrip;
    private JComboBox selectTrip;
    private JLabel lblStatus;
    private JComboBox selectStatus;

    public AddPaymentDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
    }
}
