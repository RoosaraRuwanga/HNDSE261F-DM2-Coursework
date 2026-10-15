
package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;

public class AddPassengerDialog extends JDialog {

    private JPanel rootPanel;
    private JTextField txtName;
    private JTextField txtContact;
    private JLabel lblTickets;
    private JTextField txtTickets;
    private JButton cancelButton;
    private JButton addButton;

    private boolean confirmed = false;

    // Add mode
    public AddPassengerDialog(Window owner) {
        super(owner, "Add passenger", ModalityType.APPLICATION_MODAL);

        setContentPane(rootPanel);

        txtTickets.setText("0");
        txtTickets.setEditable(true);

        addButton.addActionListener(e -> onAdd());
        cancelButton.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(addButton);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // Edit mode
    public AddPassengerDialog(
            Window owner,
            String name,
            String contact,
            int tickets
    ) {
        this(owner);

        setTitle("Edit passenger");
        addButton.setText("Save changes");

        txtName.setText(name);
        txtContact.setText(contact);
        txtTickets.setText(String.valueOf(tickets));

        lblTickets.setVisible(true);
        txtTickets.setVisible(true);

        pack();
        setLocationRelativeTo(owner);
    }

    private void onAdd() {
        if (txtName.getText().isBlank()
                || txtContact.getText().isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Name and contact are required."
            );
            return;
        }

        try {
            int tickets = Integer.parseInt(txtTickets.getText().trim());

            if (tickets < 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tickets cannot be negative."
                );
                return;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid ticket count."
            );
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getPassengerName() {
        return txtName.getText().trim();
    }

    public String getContact() {
        return txtContact.getText().trim();
    }

    public int getTickets() {
        return Integer.parseInt(txtTickets.getText().trim());
    }

}
