package eventos.ui.gui;

import eventos.model.Student;

import javax.swing.table.AbstractTableModel;

public class StudentTableModel extends AbstractTableModel {

    private static final String[] COLUMNS =
        {"ID", "Name", "Email", "Program", "Attended", "Att. Count"};

    private Student[] students = new Student[0];

    public void setStudents(Student[] s) {
        students = (s != null) ? s : new Student[0];
        fireTableDataChanged();
    }

    @Override public int    getRowCount()          { return students.length; }
    @Override public int    getColumnCount()       { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        Student s = students[row];
        return switch (col) {
            case 0 -> s.getId();
            case 1 -> s.getName();
            case 2 -> s.getEmail();
            case 3 -> s.getProgram();
            case 4 -> s.isAttended() ? "Yes" : "No";
            case 5 -> s.getAttendanceCount();
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return (col == 0 || col == 5) ? Integer.class : String.class;
    }

    public Student getStudentAt(int row) {
        return (row >= 0 && row < students.length) ? students[row] : null;
    }
}
