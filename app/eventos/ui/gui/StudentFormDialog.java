package eventos.ui.gui;

import javax.swing.*;
import java.awt.*;

public class StudentFormDialog extends JDialog {

    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField programField;
    private boolean          confirmed = false;

    public StudentFormDialog(Window owner) {
        super(owner, "Register Student", ModalityType.APPLICATION_MODAL);
        idField      = new JTextField(22);
        nameField    = new JTextField(22);
        emailField   = new JTextField(22);
        programField = new JTextField(22);
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
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        String[]     labels = {"Student ID:", "Full Name:", "Email:", "Program:"};
        JTextField[] fields = {idField, nameField, emailField, programField};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            form.add(new JLabel(labels[i]), gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add(fields[i], gc);
        }
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnOk     = new JButton("Register");
        JButton btnCancel = new JButton("Cancel");
        buttons.add(btnOk);
        buttons.add(btnCancel);
        root.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
        btnOk.addActionListener(e -> {
            try { Integer.parseInt(idField.getText().trim()); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Student ID must be a positive integer.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!emailField.getText().trim().contains("@")) {
                JOptionPane.showMessageDialog(this, "Email must contain '@'.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        btnCancel.addActionListener(e -> dispose());
    }

    public boolean isConfirmed()  { return confirmed; }
    public int     getStudentId() { return Integer.parseInt(idField.getText().trim()); }
    public String  getName()      { return nameField.getText().trim(); }
    public String  getEmail()     { return emailField.getText().trim(); }
    public String  getProgram()   { return programField.getText().trim().isEmpty()
                                        ? "N/A" : programField.getText().trim(); }
}
