package eventos.ui.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * StudentFormDialog.java — Modo oscuro carbon-slate.
 */
public class StudentFormDialog extends JDialog {

    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField programField;
    private boolean          confirmed = false;

    public StudentFormDialog(Window owner) {
        super(owner, "Registrar Estudiante", ModalityType.APPLICATION_MODAL);
        idField      = styledField(22);
        nameField    = styledField(22);
        emailField   = styledField(22);
        programField = styledField(22);
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 14));
        root.setBackground(Theme.SURFACE_HIGH);
        root.setBorder(new EmptyBorder(24, 28, 20, 28));
        setContentPane(root);

        // Título con punto de acento
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(Theme.FONT_TITLE);
        dot.setForeground(Theme.ACCENT);
        JLabel title = Theme.sectionLabel("Registrar Estudiante");
        titleRow.add(dot);
        titleRow.add(title);
        titleRow.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(titleRow, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE_HIGH);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 10);
        gc.anchor = GridBagConstraints.WEST;

        String[]     labels = {"ID Estudiante:", "Nombre Completo:", "Correo Electrónico:", "Programa:"};
        JTextField[] fields = {idField, nameField, emailField, programField};
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i;
            gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            form.add(Theme.colorLabel(labels[i], Theme.TEXT_SUB, Theme.FONT_UI), gc);
            gc.gridx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add(fields[i], gc);
        }
        root.add(form, BorderLayout.CENTER);

        // Botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.SURFACE_HIGH);
        buttons.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnOk     = Theme.primaryButton("Registrar");
        JButton btnCancel = Theme.secondaryButton("Cancelar");
        buttons.add(btnCancel);
        buttons.add(btnOk);
        root.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
        btnOk.addActionListener(e -> {
            try { Integer.parseInt(idField.getText().trim()); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "El ID debe ser un entero positivo.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!emailField.getText().trim().contains("@")) {
                JOptionPane.showMessageDialog(this, "El correo debe contener @.", "Validación",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        btnCancel.addActionListener(e -> dispose());
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(0x0D1018));
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.ACCENT);
        f.setFont(Theme.FONT_UI);
        f.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(5, 10, 5, 10)));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(Theme.ACCENT, 1),
                        new EmptyBorder(5, 10, 5, 10)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new LineBorder(Theme.BORDER, 1),
                        new EmptyBorder(5, 10, 5, 10)));
            }
        });
        return f;
    }

    public boolean isConfirmed()  { return confirmed; }
    public int     getStudentId() { return Integer.parseInt(idField.getText().trim()); }
    public String  getName()      { return nameField.getText().trim(); }
    public String  getEmail()     { return emailField.getText().trim(); }
    public String  getProgram()   {
        String p = programField.getText().trim();
        return p.isEmpty() ? "N/A" : p;
    }
}
