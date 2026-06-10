package eventos.ui.gui;

import core.AppPaths;
import eventos.model.Student;
import eventos.service.EventService;
import eventos.service.RegisterResult;
import eventos.service.UndoResult;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EventDetailPanel extends JPanel {

    private final MainWindow      mainWindow;
    private EventService          event;

    // Header widgets
    private final JLabel          titleLabel;
    private final JLabel          statsLabel;
    private final JProgressBar    progressBar;

    // Center panels
    private final StudentTableModel studentModel;
    private final JTable            studentTable;
    private final DefaultListModel<String> queueModel;
    private final DefaultListModel<String> historyModel;

    // Random-add helper data
    private static final String[] NAMES = {
        "Ana Garcia", "Carlos Lopez", "Diana Perez", "Eduardo Gomez", "Francisca Ruiz",
        "Gabriel Torres", "Helena Morales", "Ivan Castro", "Julia Vargas", "Kevin Reyes",
        "Laura Mendoza", "Miguel Herrera", "Natalia Flores", "Oscar Romero", "Paula Diaz",
        "Rodrigo Ortiz", "Sofia Molina", "Tomas Guerrero", "Valentina Cruz", "Wilmer Rios"
    };
    private static final String[] PROGRAMS = {
        "Ing. Sistemas", "Ing. Industrial", "Economia", "Medicina", "Ing. Civil",
        "Matematicas", "Fisica", "Quimica", "Biologia", "Derecho"
    };

    public EventDetailPanel(MainWindow mainWindow) {
        this.mainWindow   = mainWindow;
        this.titleLabel   = new JLabel();
        this.statsLabel   = new JLabel();
        this.progressBar  = new JProgressBar(0, 1);
        this.studentModel = new StudentTableModel();
        this.studentTable = buildStudentTable();
        this.queueModel   = new DefaultListModel<>();
        this.historyModel = new DefaultListModel<>();
        buildUI();
    }

    public void setEvent(EventService ev) {
        this.event = ev;
        refresh();
    }

    // ---------- UI construction ----------

    private JTable buildStudentTable() {
        JTable t = new JTable(studentModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(24);
        t.setFillsViewportHeight(true);
        int[] widths = {56, 180, 180, 130, 60, 70};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return t;
    }

    private void buildUI() {
        setLayout(new BorderLayout(4, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 4));
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JButton btnBack = new JButton("← Back");
        btnBack.addActionListener(e -> mainWindow.backToList());
        p.add(btnBack, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        progressBar.setStringPainted(true);
        info.add(titleLabel);
        info.add(progressBar);
        p.add(info, BorderLayout.CENTER);

        statsLabel.setFont(statsLabel.getFont().deriveFont(Font.PLAIN, 12f));
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(statsLabel, BorderLayout.EAST);
        return p;
    }

    private JSplitPane buildCenter() {
        // Right panel: queue + history stacked
        JList<String> queueList   = new JList<>(queueModel);
        JList<String> historyList = new JList<>(historyModel);

        JPanel queuePanel = titled("Queue", new JScrollPane(queueList));
        JPanel histPanel  = titled("History", new JScrollPane(historyList));

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queuePanel, histPanel);
        right.setResizeWeight(0.35);
        right.setDividerSize(5);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            titled("Students (sorted by ID — AVL in-order)", new JScrollPane(studentTable)),
            right);
        split.setResizeWeight(0.65);
        split.setDividerSize(5);
        return split;
    }

    private JPanel titled(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildButtons() {
        JPanel outer = new JPanel(new GridLayout(2, 1, 2, 2));
        outer.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel row1 = row(
            btn("Register",        this::registerStudent),
            btn("Find by ID",      this::findStudent),
            btn("Mark Attendance", this::markAttendance),
            btn("Remove",          this::removeStudent),
            btn("Undo Removal",    this::undoRemoval)
        );
        JPanel row2 = row(
            btn("View AVL",        this::viewAvlTree),
            btn("Export CSV",      this::exportCsv),
            btn("Add Random",      this::addRandom),
            btn("Top K",           this::showTopK),
            btn("Clear All",       this::clearAll)
        );
        outer.add(row1);
        outer.add(row2);
        return outer;
    }

    private JPanel row(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        for (JButton b : buttons) p.add(b);
        return p;
    }

    private JButton btn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    // ---------- Refresh ----------

    public void refresh() {
        if (event == null) return;
        int students = event.getStudentCount();
        int cap      = event.getCapacity();

        titleLabel.setText("Event: " + event.getName());
        progressBar.setMaximum(cap);
        progressBar.setValue(students);
        progressBar.setString(students + " / " + cap
                + String.format("  (%.0f%%)", event.getOccupancyRate()));
        statsLabel.setText("Queue: " + event.getQueueSize()
                + "    Attendance: " + event.getAttendanceCount()
                + "    Hist: " + event.getHistorySize());

        studentModel.setStudents(event.getStudentsSorted());

        queueModel.clear();
        Object[] q = event.getQueueContents();
        for (int i = 0; i < q.length; i++) {
            Student s = (Student) q[i];
            queueModel.addElement((i + 1) + ".  ID:" + s.getId() + "  " + s.getName());
        }

        historyModel.clear();
        for (var entry : event.getHistoryEntries())
            historyModel.addElement("[" + entry.type + "]  " + entry.description);
    }

    // ---------- Operations ----------

    private void registerStudent() {
        StudentFormDialog dlg = new StudentFormDialog(mainWindow);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        RegisterResult r = event.registerStudent(
            dlg.getStudentId(), dlg.getName(), dlg.getEmail(), dlg.getProgram());
        switch (r) {
            case REGISTERED        -> info("Registered: " + dlg.getName() + " (ID: " + dlg.getStudentId() + ")");
            case QUEUED            -> info("Event full. Added to queue (position " + event.getQueueSize() + ").");
            case DUPLICATE_ID      -> warn("ID " + dlg.getStudentId() + " is already registered.");
            case DUPLICATE_IN_QUEUE-> warn("ID " + dlg.getStudentId() + " is already in the queue.");
            case INVALID_DATA      -> error("Invalid data: ID must be > 0, name required, email must contain @.");
        }
        refresh();
    }

    private void findStudent() {
        Integer id = askId("Find by Student ID:");
        if (id == null) return;
        Student s = event.findStudent(id);  // O(1) via HashTable
        if (s != null)
            JOptionPane.showMessageDialog(this,
                "<html><b>" + s.getName() + "</b><br>"
                + "ID: " + s.getId() + "<br>"
                + "Email: " + s.getEmail() + "<br>"
                + "Program: " + s.getProgram() + "<br>"
                + "Attended: " + (s.isAttended() ? "Yes" : "No") + "<br>"
                + "Attendance count: " + s.getAttendanceCount() + "</html>",
                "Student found", JOptionPane.INFORMATION_MESSAGE);
        else
            warn("No student with ID " + id + ".");
    }

    private void markAttendance() {
        Integer id = askId("Mark attendance for Student ID:");
        if (id == null) return;
        if (event.markAttendance(id))
            info("Attendance marked for ID " + id + ".");
        else
            warn("No student with ID " + id + ".");
        refresh();
    }

    private void removeStudent() {
        Integer id = askId("Remove Student ID:");
        if (id == null) return;
        if (!event.hasStudent(id)) { warn("No student with ID " + id + "."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "Remove student ID " + id + "? A queued student will be promoted if available.",
            "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        event.removeStudent(id);
        Student p = event.getLastPromoted();
        if (p != null) info("Removed. Promoted from queue: " + p.getName() + " (ID: " + p.getId() + ")");
        refresh();
    }

    private void undoRemoval() {
        UndoResult r = event.undoLastRemoval();
        switch (r) {
            case UNDONE          -> info("Last removal undone. Student restored.");
            case UNDONE_TO_QUEUE -> info("Restored but moved to queue (event full).");
            case NO_OPERATIONS   -> warn("No operations to undo.");
            case NOT_A_REMOVAL   -> warn("Last operation was not a removal.");
            case ERROR           -> error("Could not restore — ID already exists.");
        }
        refresh();
    }

    private void viewAvlTree() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf), old = System.out;
        System.setOut(ps);
        event.visualizeTree();
        System.setOut(old);
        String clean = buf.toString().replaceAll("\\x1B\\[[0-9;]*[mA-Za-z]", "");

        JTextArea area = new JTextArea(clean);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(620, 420));
        JOptionPane.showMessageDialog(this, sp,
            "AVL Tree — " + event.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void exportCsv() {
        String safe = event.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
        String path = AppPaths.Dirs.RESULTS.resolve("events").resolve(safe + ".csv").toString();
        if (event.exportCSV(path))
            info("Exported " + event.getStudentCount() + " students to:\n" + path);
        else
            warn("No students to export.");
        refresh();
    }

    private void addRandom() {
        String s = JOptionPane.showInputDialog(this, "How many random students?", "10");
        if (s == null) return;
        int count;
        try { count = Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { error("Invalid number."); return; }
        if (count <= 0) { error("Count must be > 0."); return; }

        int startId = event.getStudentCount() + event.getQueueSize() + 1;
        int reg = 0, queued = 0;
        for (int i = 0; i < count; i++) {
            int    cid     = startId + i;
            String name    = NAMES   [cid % NAMES.length];
            String program = PROGRAMS[cid % PROGRAMS.length];
            String email   = "est" + String.format("%04d", cid) + "@unal.edu.co";
            int    r       = event.registerBulk(cid, name, email, program);
            if      (r == 1) reg++;
            else if (r == 0) queued++;
        }
        info("Added: " + reg + " registered, " + queued + " queued, "
            + (count - reg - queued) + " skipped (duplicates).");
        refresh();
    }

    private void showTopK() {
        String s = JOptionPane.showInputDialog(this, "Show top K students by attendance count:", "5");
        if (s == null) return;
        int k;
        try { k = Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { error("Invalid number."); return; }
        if (k <= 0) { error("K must be > 0."); return; }

        Student[] top = event.getTopAttendees(k);
        // Also update the TopAttendeesPanel
        mainWindow.getTopAttendeesPanel().setCurrentEvent(event);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-8s %-25s %-12s%n", "Rank", "ID", "Name", "Att. Count"));
        sb.append("-".repeat(54)).append("\n");
        for (int i = 0; i < top.length; i++)
            sb.append(String.format("%-4d %-8d %-25s %-12d%n",
                i + 1, top[i].getId(), top[i].getName(), top[i].getAttendanceCount()));
        if (top.length == 0) sb.append("  (No students with attendance > 0)");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(460, 260));
        JOptionPane.showMessageDialog(this, sp,
            "Top " + k + " Attendees — " + event.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void clearAll() {
        if (event.isEmpty()) { warn("No students to clear."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "This will delete ALL " + event.getStudentCount()
                + " students and clear the queue. This cannot be undone.",
            "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        int n = event.clearAll();
        info("Cleared " + n + " students.");
        refresh();
    }

    // ---------- Helpers ----------

    private Integer askId(String prompt) {
        String s = JOptionPane.showInputDialog(this, prompt);
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { error("Invalid ID."); return null; }
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",    JOptionPane.INFORMATION_MESSAGE);
    }
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE);
    }
}
