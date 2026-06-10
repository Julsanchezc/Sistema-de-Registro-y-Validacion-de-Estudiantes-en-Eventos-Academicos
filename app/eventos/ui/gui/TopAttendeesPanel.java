package eventos.ui.gui;

import eventos.model.Student;
import eventos.service.EventManager;
import eventos.service.EventManager.AttendanceSummary;
import eventos.service.EventService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class TopAttendeesPanel extends JPanel {

    private final EventManager manager;
    private final JSpinner     kSpinner;
    private final JComboBox<String> scopeBox;
    private final ResultModel  resultModel;
    private final JTable       resultTable;
    private final JLabel       statusLabel;

    private EventService currentEvent = null;

    public TopAttendeesPanel(EventManager manager) {
        this.manager     = manager;
        this.kSpinner    = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        this.scopeBox    = new JComboBox<>(new String[]{"Global (all events)", "Selected event"});
        this.resultModel = new ResultModel();
        this.resultTable = buildTable();
        this.statusLabel = new JLabel("  Ready.");
        buildUI();
    }

    public void setCurrentEvent(EventService ev) { this.currentEvent = ev; }

    private JTable buildTable() {
        JTable t = new JTable(resultModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(24);
        t.setFillsViewportHeight(true);
        return t;
    }

    private void buildUI() {
        setLayout(new BorderLayout(6, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel title = new JLabel("Top Attendees");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        controls.add(title);
        controls.add(Box.createHorizontalStrut(16));
        controls.add(new JLabel("Scope:"));
        controls.add(scopeBox);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(new JLabel("Top K:"));
        controls.add(kSpinner);
        JButton btnCalc = new JButton("Calculate");
        btnCalc.addActionListener(e -> calculate());
        controls.add(btnCalc);

        // Info label
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel info = new JLabel(
            "<html><i>Uses HashTable for aggregation + MinHeap (O(n log k)) for top-K selection.</i></html>");
        info.setForeground(Color.GRAY);
        info.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 0));
        infoPanel.add(controls, BorderLayout.NORTH);
        infoPanel.add(info, BorderLayout.SOUTH);

        add(infoPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void calculate() {
        int    k     = (int) kSpinner.getValue();
        String scope = (String) scopeBox.getSelectedItem();

        if ("Selected event".equals(scope)) {
            if (currentEvent == null) {
                JOptionPane.showMessageDialog(this,
                    "Open an event first and click 'Top K' from the event detail view,\n"
                    + "or switch scope to 'Global'.",
                    "No event selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Student[] top = currentEvent.getTopAttendees(k);
            resultModel.setFromStudents(top, currentEvent.getName());
            statusLabel.setText("  Top " + top.length + " attendees in event: " + currentEvent.getName());
        } else {
            AttendanceSummary[] top = manager.getTopAttendeesGlobal(k);
            resultModel.setFromSummaries(top);
            statusLabel.setText("  Top " + top.length + " attendees across all events.");
        }
    }

    // --- Inner table model ---

    private static class ResultModel extends AbstractTableModel {

        private static final String[] COLS_GLOBAL  = {"Rank", "Student ID", "Name", "Total Attendance"};
        private static final String[] COLS_LOCAL   = {"Rank", "Student ID", "Name", "Program", "Att. Count"};

        private Object[][] rows = new Object[0][];
        private String[]   cols = COLS_GLOBAL;

        void setFromSummaries(AttendanceSummary[] data) {
            cols = COLS_GLOBAL;
            rows = new Object[data.length][];
            for (int i = 0; i < data.length; i++)
                rows[i] = new Object[]{i + 1, data[i].id, data[i].name, data[i].totalAttendances};
            fireTableStructureChanged();
        }

        void setFromStudents(Student[] data, String eventName) {
            cols = COLS_LOCAL;
            rows = new Object[data.length][];
            for (int i = 0; i < data.length; i++)
                rows[i] = new Object[]{i + 1, data[i].getId(), data[i].getName(),
                                        data[i].getProgram(), data[i].getAttendanceCount()};
            fireTableStructureChanged();
        }

        @Override public int    getRowCount()          { return rows.length; }
        @Override public int    getColumnCount()       { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }
        @Override public Object getValueAt(int r, int c) { return rows[r][c]; }
        @Override public Class<?> getColumnClass(int col) {
            return (col == 0 || col == 1 || col == cols.length - 1) ? Integer.class : String.class;
        }
    }
}
