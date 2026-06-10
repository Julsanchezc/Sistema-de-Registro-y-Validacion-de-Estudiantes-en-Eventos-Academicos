package eventos.ui.gui;

import eventos.model.Student;
import eventos.service.EventManager;
import eventos.service.EventManager.AttendanceSummary;
import eventos.service.EventService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * TopAttendeesPanel.java — Modo oscuro carbon-slate.
 */
public class TopAttendeesPanel extends JPanel {

    private final EventManager      manager;
    private final JSpinner          kSpinner;
    private final JComboBox<String> scopeBox;
    private final ResultModel       resultModel;
    private final JTable            resultTable;
    private final JLabel            statusLabel;

    private EventService currentEvent = null;

    public TopAttendeesPanel(EventManager manager) {
        this.manager     = manager;
        this.kSpinner    = buildSpinner();
        this.scopeBox    = buildScopeBox();
        this.resultModel = new ResultModel();
        this.resultTable = buildTable();
        this.statusLabel = Theme.colorLabel("  Listo.", Theme.TEXT_DIM, Theme.FONT_SMALL);
        buildUI();
    }

    public void setCurrentEvent(EventService ev) { this.currentEvent = ev; }

    private JSpinner buildSpinner() {
        JSpinner s = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        s.setBackground(Theme.SURFACE_HIGH);
        s.setForeground(Theme.TEXT);
        s.setFont(Theme.FONT_UI);
        s.setPreferredSize(new Dimension(64, 30));
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(Theme.SURFACE_HIGH);
            de.getTextField().setForeground(Theme.TEXT);
            de.getTextField().setCaretColor(Theme.ACCENT);
        }
        return s;
    }

    private JComboBox<String> buildScopeBox() {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Global (todos los eventos)", "Evento seleccionado"});
        cb.setBackground(Theme.SURFACE_HIGH);
        cb.setForeground(Theme.TEXT);
        cb.setFont(Theme.FONT_UI);
        return cb;
    }

    private JTable buildTable() {
        JTable t = new JTable(resultModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(28);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.setBackground(Theme.SURFACE);
        t.setForeground(Theme.TEXT);
        t.setFont(Theme.FONT_UI);
        t.setGridColor(Theme.BORDER);
        t.setSelectionBackground(Theme.SEL_BG);
        t.setSelectionForeground(Theme.SEL_FG);

        JTableHeader header = t.getTableHeader();
        header.setBackground(Theme.SURFACE_HIGH);
        header.setForeground(Theme.TEXT_SUB);
        header.setFont(Theme.FONT_UI_B);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Columna Rank con color y centrada
        t.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    int rank = row + 1;
                    l.setForeground(rank == 1 ? Theme.YELLOW
                                  : rank == 2 ? Theme.TEXT_SUB
                                  : rank == 3 ? new Color(0xCD7F32)
                                              : Theme.TEXT_DIM);
                    l.setBackground(Theme.SURFACE);
                }
                l.setOpaque(true);
                return l;
            }
        });
        return t;
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        add(buildControls(), BorderLayout.NORTH);
        add(Theme.scrollPane(resultTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(new Color(0x0C0F18));
        south.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        statusLabel.setBorder(new EmptyBorder(5, 14, 5, 14));
        south.add(statusLabel, BorderLayout.WEST);

        JLabel info = Theme.colorLabel(
            "  HashTable para agregación  ·  MinHeap O(n log k) para selección top-K",
            Theme.TEXT_DIM, Theme.FONT_SMALL);
        info.setBorder(new EmptyBorder(5, 8, 5, 14));
        south.add(info, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildControls() {
        JPanel p = Theme.toolbar();

        p.add(Theme.sectionLabel("Mejores Asistentes"));
        p.add(Box.createHorizontalStrut(16));
        p.add(Theme.vSep());

        p.add(Theme.colorLabel("Alcance:", Theme.TEXT_SUB, Theme.FONT_UI));
        p.add(scopeBox);

        p.add(Box.createHorizontalStrut(10));
        p.add(Theme.colorLabel("Top K:", Theme.TEXT_SUB, Theme.FONT_UI));
        p.add(kSpinner);

        JButton btnCalc = Theme.primaryButton("Calcular");
        btnCalc.addActionListener(e -> calculate());
        p.add(Box.createHorizontalStrut(8));
        p.add(btnCalc);

        return p;
    }

    private void calculate() {
        int    k     = (int) kSpinner.getValue();
        String scope = (String) scopeBox.getSelectedItem();

        if ("Evento seleccionado".equals(scope)) {
            if (currentEvent == null) {
                JOptionPane.showMessageDialog(this,
                    "Abre un evento primero y haz clic en Top K desde el detalle,\n"
                    + "o cambia el alcance a Global.",
                    "Sin evento seleccionado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Student[] top = currentEvent.getTopAttendees(k);
            resultModel.setFromStudents(top, currentEvent.getName());
            statusLabel.setText("  Top " + top.length + " asistentes en: " + currentEvent.getName());
        } else {
            AttendanceSummary[] top = manager.getTopAttendeesGlobal(k);
            resultModel.setFromSummaries(top);
            statusLabel.setText("  Top " + top.length + " asistentes en todos los eventos.");
        }
    }

    // ── Inner table model ─────────────────────────────────────────────────
    private static class ResultModel extends AbstractTableModel {
        private static final String[] COLS_GLOBAL = {"Rank","Student ID","Nombre","Total Asistencias"};
        private static final String[] COLS_LOCAL  = {"Rank","Student ID","Nombre","Programa","Asistencias"};
        private Object[][] rows = new Object[0][];
        private String[]   cols = COLS_GLOBAL;

        void setFromSummaries(AttendanceSummary[] data) {
            cols = COLS_GLOBAL;
            rows = new Object[data.length][];
            for (int i = 0; i < data.length; i++)
                rows[i] = new Object[]{i+1, data[i].id, data[i].name, data[i].totalAttendances};
            fireTableStructureChanged();
        }

        void setFromStudents(Student[] data, String eventName) {
            cols = COLS_LOCAL;
            rows = new Object[data.length][];
            for (int i = 0; i < data.length; i++)
                rows[i] = new Object[]{i+1, data[i].getId(), data[i].getName(),
                                       data[i].getProgram(), data[i].getAttendanceCount()};
            fireTableStructureChanged();
        }

        @Override public int    getRowCount()            { return rows.length; }
        @Override public int    getColumnCount()         { return cols.length; }
        @Override public String getColumnName(int col)   { return cols[col]; }
        @Override public Object getValueAt(int r, int c) { return rows[r][c]; }
        @Override public Class<?> getColumnClass(int col) {
            return (col == 0 || col == 1 || col == cols.length-1) ? Integer.class : String.class;
        }
    }
}
