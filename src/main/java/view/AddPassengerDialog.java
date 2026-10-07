package view;

import javax.swing.*;
import java.awt.*;

public class AddPassengerDialog extends JDialog {
    private JPanel rootPanel;
    private JTextField txtName;
    private JTextField txtContact;
    private JComboBox cmbStatus;
    private JButton cancelButton;
    private JButton addButton;

    private boolean confirmed = false;

    // add mode: empty form
    public AddPassengerDialog(Window owner) {
        super(owner, "Add passenger", Dialog.ModalityType.APPLICATION_MODAL);
        setContentPane(rootPanel);

        cmbStatus.setModel(new DefaultComboBoxModel<String>(new String[]{"Active", "Inactive"}));

        addButton.addActionListener(e -> onAdd());
        cancelButton.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(addButton);               // Enter = Add
        getRootPane().registerKeyboardAction(e -> dispose(),     // Esc = Cancel
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // edit mode: same form, pre-filled with the passenger's current values
    public AddPassengerDialog(Window owner, String name, String contact) {
        this(owner);
        setTitle("Edit passenger");
        addButton.setText("Save changes");
        txtName.setText(name);
        txtContact.setText(contact);
        pack();                          // re-fit in case the button text is wider
        setLocationRelativeTo(owner);
    }

    private void onAdd() {
        if (txtName.getText().isBlank() || txtContact.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Name and contact are required.");
            return;
        }
        confirmed = true;
        dispose();
    }

    // getters the view uses to read what was typed
    public boolean isConfirmed()      { return confirmed; }
    public String getPassengerName()  { return txtName.getText().trim(); }  // not getName(): JDialog already has one
    public String getContact()        { return txtContact.getText().trim(); }
    public String getStatus()         { return (String) cmbStatus.getSelectedItem(); }
}