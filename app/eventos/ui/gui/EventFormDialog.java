package eventos.ui.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * EventFormDialog.java — Modo oscuro carbon-slate.
 */
public class EventFormDialog extends JDialog {

    private final JTextField nameField;
    private final JSpinner   capacitySpinner;
    private boolean          confirmed = false;

    public EventFormDialog(Window owner) {
        super(owner, "Nuevo Evento", ModalityType.APPLICATION_MODAL);
        nameField       = styledField(24);
        capacitySpinner = buildSpinner();
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

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(Theme.FONT_TITLE);
        dot.setForeground(Theme.ACCENT);
        JLabel title = Theme.sectionLabel("Crear Nuevo Evento");
        titleRow.add(dot);
        titleRow.add(title);
        titleRow.setBorder(new EmptyBorder(0, 0, 8, 0));
        root.add(titleRow, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE_HIGH);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 4, 6, 10);
        gc.anchor  = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(Theme.colorLabel("Nombre del Evento:", Theme.TEXT_SUB, Theme.FONT_UI), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        form.add(nameField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(Theme.colorLabel("Capacidad Máxima:", Theme.TEXT_SUB, Theme.FONT_UI), gc);
        gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        form.add(capacitySpinner, gc);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.SURFACE_HIGH);
        buttons.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnOk     = Theme.primaryButton("Crear Evento");
        JButton btnCancel = Theme.secondaryButton("Cancelar");
        buttons.add(btnCancel);
        buttons.add(btnOk);
        root.add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
        btnOk.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del evento no puede estar vacío.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
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

    private JSpinner buildSpinner() {
        JSpinner s = new JSpinner(new SpinnerNumberModel(30, 1, 10000, 1));
        s.setBackground(Theme.SURFACE_HIGH);
        s.setForeground(Theme.TEXT);
        s.setFont(Theme.FONT_UI);
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(new Color(0x0D1018));
            de.getTextField().setForeground(Theme.TEXT);
            de.getTextField().setCaretColor(Theme.ACCENT);
        }
        return s;
    }

    public boolean isConfirmed()  { return confirmed; }
    public String  getEventName() { return nameField.getText().trim(); }
    public int     getCapacity()  { return (int) capacitySpinner.getValue(); }
}
