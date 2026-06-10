package eventos.ui.gui;

import javax.swing.*;
import java.awt.*;

public class EventFormDialog extends JDialog {

    private final JTextField nameField;
    private final JSpinner   capacitySpinner;
    private boolean          confirmed = false;

    public EventFormDialog(Window owner) {
        super(owner, "New Event", ModalityType.APPLICATION_MODAL);
        nameField       = new JTextField(24);
        capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 10000, 1));
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        setContentPane(root);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 4, 4, 4);
        gc.anchor  = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Event Name:"), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        form.add(nameField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(new JLabel("Max Capacity:"), gc);
        gc.gridx = 1;
        form.add(capacitySpinner, gc);

        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnOk     = new JButton("Create");
        JButton btnCancel = new JButton("Cancel");
        buttons.add(btnOk);
        buttons.add(btnCancel);
        root.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
        btnOk.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Event name cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        btnCancel.addActionListener(e -> dispose());
    }

    public boolean isConfirmed()  { return confirmed; }
    public String  getEventName() { return nameField.getText().trim(); }
    public int     getCapacity()  { return (int) capacitySpinner.getValue(); }
}
