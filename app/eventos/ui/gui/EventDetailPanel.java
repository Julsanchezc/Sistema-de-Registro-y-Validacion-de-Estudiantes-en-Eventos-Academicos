package eventos.ui.gui;

import core.AppPaths;
import eventos.model.Student;
import eventos.service.EventService;
import eventos.service.RegisterResult;
import eventos.service.UndoResult;

import javax.swing.table.JTableHeader;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

/**
 * EventDetailPanel.java — Modo oscuro carbon-slate.
 * Búsqueda integrada en la barra de herramientas (por ID, nombre o email).
 * Búsqueda por nombre/email hace scan lineal sobre los estudiantes del AVL.
 */
public class EventDetailPanel extends JPanel {

    private final MainWindow      mainWindow;
    private EventService          event;

    private final JLabel          titleLabel;
    private final JLabel          statsLabel;
    private final JProgressBar    progressBar;

    private final StudentTableModel        studentModel;
    private final JTable                   studentTable;
    private final DefaultListModel<String> queueModel;
    private final DefaultListModel<String> historyModel;

    // Panel de búsqueda integrado
    private final JTextField      searchField;
    private final JComboBox<String> searchType;
    private final JLabel          searchResult;

    private static final String[] NAMES = {
        "Ana Garcia","Carlos Lopez","Diana Perez","Eduardo Gomez","Francisca Ruiz",
        "Gabriel Torres","Helena Morales","Ivan Castro","Julia Vargas","Kevin Reyes",
        "Laura Mendoza","Miguel Herrera","Natalia Flores","Oscar Romero","Paula Diaz",
        "Rodrigo Ortiz","Sofia Molina","Tomas Guerrero","Valentina Cruz","Wilmer Rios"
    };
    private static final String[] PROGRAMS = {
        "Ing. Sistemas","Ing. Industrial","Economia","Medicina","Ing. Civil",
        "Matematicas","Fisica","Quimica","Biologia","Derecho"
    };

    public EventDetailPanel(MainWindow mainWindow) {
        this.mainWindow   = mainWindow;
        this.titleLabel   = new JLabel();
        this.statsLabel   = new JLabel();
        this.progressBar  = Theme.progressBar(1);
        this.studentModel = new StudentTableModel();
        this.studentTable = buildStudentTable();
        this.queueModel   = new DefaultListModel<>();
        this.historyModel = new DefaultListModel<>();
        this.searchField  = styledSearchField();
        this.searchType   = buildSearchType();
        this.searchResult = Theme.colorLabel("", Theme.TEXT_DIM, Theme.FONT_SMALL);
        buildUI();
    }

    public void setEvent(EventService ev) {
        this.event = ev;
        refresh();
    }

    // ── tabla de estudiantes ──────────────────────────────────────────────
    private JTable buildStudentTable() {
        JTable t = new JTable(studentModel);
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

        // columna Attended
        t.getColumnModel().getColumn(4).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(
                            tbl, val, sel, foc, row, col);
                    boolean yes = "Yes".equals(val);
                    l.setForeground(sel ? Theme.TEXT : (yes ? Theme.GREEN : Theme.TEXT_DIM));
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setBackground(sel ? Theme.SEL_BG : Theme.SURFACE);
                    l.setOpaque(true);
                    return l;
                }
            });

        int[] widths = {60, 180, 185, 130, 64, 74};
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return t;
    }

    private JTextField styledSearchField() {
        JTextField f = new JTextField(20);
        f.setBackground(new Color(0x0D1018));
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.ACCENT);
        f.setFont(Theme.FONT_UI);
        f.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(4, 10, 4, 10)));
        f.putClientProperty("placeholderText", "Buscar...");
        return f;
    }

    private JComboBox<String> buildSearchType() {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Por ID", "Por Nombre", "Por Email"});
        cb.setBackground(Theme.SURFACE_HIGH);
        cb.setForeground(Theme.TEXT);
        cb.setFont(Theme.FONT_UI);
        cb.setPreferredSize(new Dimension(120, 30));
        return cb;
    }

    // ── construcción de la UI ─────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 4));
        p.setBackground(Theme.SURFACE_HIGH);
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(10, 14, 10, 14)));

        JButton btnBack = Theme.secondaryButton("Volver");
        btnBack.addActionListener(e -> mainWindow.backToList());
        p.add(btnBack, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setOpaque(false);
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT);
        progressBar.setPreferredSize(new Dimension(0, 14));
        info.add(titleLabel);
        info.add(progressBar);
        p.add(info, BorderLayout.CENTER);

        statsLabel.setFont(Theme.FONT_SMALL);
        statsLabel.setForeground(Theme.TEXT_SUB);
        statsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(statsLabel, BorderLayout.EAST);

        return p;
    }

    /** Panel de búsqueda con selector de tipo + campo + resultado inline. */
    private JPanel buildSearchPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(Theme.SURFACE_HIGH);
        p.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        JLabel lbl = Theme.colorLabel("Buscar:", Theme.TEXT_SUB, Theme.FONT_UI);
        p.add(lbl);
        p.add(searchType);
        p.add(searchField);

        JButton btnSearch = Theme.primaryButton("Buscar");
        btnSearch.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        btnSearch.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnSearch.addActionListener(e -> performSearch());
        p.add(btnSearch);

        JButton btnClear = Theme.secondaryButton("Limpiar");
        btnClear.setFont(Theme.FONT_SMALL);
        btnClear.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        btnClear.addActionListener(e -> {
            searchField.setText("");
            searchResult.setText("");
            studentTable.clearSelection();
        });
        p.add(btnClear);
        p.add(searchResult);

        // Enter en el campo dispara la búsqueda
        searchField.addActionListener(e -> performSearch());

        return p;
    }

    private void performSearch() {
        if (event == null) return;
        String query = searchField.getText().trim();
        if (query.isEmpty()) { searchResult.setText(""); return; }

        String type = (String) searchType.getSelectedItem();
        Student found = null;

        if ("Por ID".equals(type)) {
            try {
                int id = Integer.parseInt(query);
                found = event.findStudent(id);   // O(1) via HashTable
            } catch (NumberFormatException ex) {
                searchResult.setForeground(Theme.RED);
                searchResult.setText("  ID debe ser numérico");
                return;
            }
        } else {
            // Búsqueda lineal por nombre o email sobre el arreglo del AVL
            Student[] all = event.getStudentsSorted();
            for (Student s : all) {
                if ("Por Nombre".equals(type)) {
                    if (s.getName().toLowerCase().contains(query.toLowerCase())) { found = s; break; }
                } else {  // Por Email
                    if (s.getEmail().toLowerCase().contains(query.toLowerCase())) { found = s; break; }
                }
            }
        }

        if (found != null) {
            final Student s = found;
            searchResult.setForeground(Theme.GREEN);
            searchResult.setText("  OK: " + s.getName() + "  (ID: " + s.getId() + ")");

            // Resaltar fila en la tabla
            Student[] all = event.getStudentsSorted();
            for (int i = 0; i < all.length; i++) {
                if (all[i].getId() == s.getId()) {
                    studentTable.setRowSelectionInterval(i, i);
                    studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                    break;
                }
            }
        } else {
            searchResult.setForeground(Theme.RED);
            searchResult.setText("  No encontrado");
            studentTable.clearSelection();
        }
    }

    private JSplitPane buildCenter() {
        // Panel izquierdo: búsqueda + tabla estudiantes
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Theme.SURFACE);
        leftPanel.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(0, 0, 0, 0)));

        JLabel studTitle = Theme.colorLabel("  Estudiantes — AVL ordenado por ID",
                Theme.TEXT_DIM, Theme.FONT_SMALL);
        studTitle.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(5, 4, 5, 4)));
        studTitle.setBackground(Theme.SURFACE_HIGH);
        studTitle.setOpaque(true);

        leftPanel.add(studTitle, BorderLayout.NORTH);
        leftPanel.add(buildSearchPanel(), BorderLayout.NORTH);  // will override — use wrapper
        // Fix: use a vertical wrapper
        JPanel leftTop = new JPanel(new BorderLayout());
        leftTop.setOpaque(false);
        leftTop.add(studTitle, BorderLayout.NORTH);
        leftTop.add(buildSearchPanel(), BorderLayout.SOUTH);
        leftPanel.add(leftTop, BorderLayout.NORTH);
        leftPanel.add(Theme.scrollPane(studentTable), BorderLayout.CENTER);

        // Panel derecho: cola + historial
        JList<String> queueList   = styledList(queueModel);
        JList<String> historyList = styledList(historyModel);

        JPanel queuePanel = titledPanel("Cola de Espera", Theme.scrollPane(queueList));
        JPanel histPanel  = titledPanel("Historial de Operaciones", Theme.scrollPane(historyList));

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, queuePanel, histPanel);
        right.setResizeWeight(0.35);
        right.setDividerSize(4);
        right.setBackground(Theme.BG);
        right.setBorder(null);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, right);
        split.setResizeWeight(0.65);
        split.setDividerSize(4);
        split.setBackground(Theme.BG);
        split.setBorder(null);
        return split;
    }

    private JList<String> styledList(DefaultListModel<String> model) {
        JList<String> l = new JList<>(model);
        l.setBackground(Theme.SURFACE);
        l.setForeground(Theme.TEXT);
        l.setFont(Theme.FONT_UI);
        l.setSelectionBackground(Theme.SEL_BG);
        l.setSelectionForeground(Theme.SEL_FG);
        return l;
    }

    private JPanel titledPanel(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.SURFACE);
        p.setBorder(new LineBorder(Theme.BORDER, 1));

        JLabel lbl = Theme.colorLabel("  " + title, Theme.TEXT_DIM, Theme.FONT_SMALL);
        lbl.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(5, 4, 5, 4)));
        lbl.setBackground(Theme.SURFACE_HIGH);
        lbl.setOpaque(true);

        p.add(lbl, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildButtons() {
        JPanel outer = new JPanel(new GridLayout(2, 1, 0, 2));
        outer.setBackground(Theme.SURFACE_HIGH);
        outer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(8, 10, 8, 10)));

        JButton btnRegister = Theme.primaryButton("+ Registrar");
        JButton btnAttend   = Theme.secondaryButton("Asistencia");
        JButton btnRemove   = Theme.dangerButton("Eliminar");
        JButton btnUndo     = Theme.secondaryButton("Deshacer");

        JButton btnAvl      = Theme.secondaryButton("Ver AVL");
        JButton btnExport   = Theme.secondaryButton("Exportar CSV");
        JButton btnRandom   = Theme.secondaryButton("Aleatorio");
        JButton btnTopK     = Theme.secondaryButton("Top K");
        JButton btnClear    = Theme.dangerButton("Limpiar Todo");

        btnRegister.addActionListener(e -> registerStudent());
        btnAttend  .addActionListener(e -> markAttendance());
        btnRemove  .addActionListener(e -> removeStudent());
        btnUndo    .addActionListener(e -> undoRemoval());
        btnAvl     .addActionListener(e -> viewAvlTree());
        btnExport  .addActionListener(e -> exportCsv());
        btnRandom  .addActionListener(e -> addRandom());
        btnTopK    .addActionListener(e -> showTopK());
        btnClear   .addActionListener(e -> clearAll());

        outer.add(row(btnRegister, btnAttend, btnRemove, btnUndo));
        outer.add(row(btnAvl, btnExport, btnRandom, btnTopK, btnClear));
        return outer;
    }

    private JPanel row(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        p.setOpaque(false);
        for (JButton b : buttons) p.add(b);
        return p;
    }

    // ── refresco ──────────────────────────────────────────────────────────
    public void refresh() {
        if (event == null) return;
        int students = event.getStudentCount();
        int cap      = event.getCapacity();

        titleLabel.setText("Evento:  " + event.getName());
        progressBar.setMaximum(cap);
        progressBar.setValue(students);
        progressBar.setString(students + " / " + cap
                + String.format("  (%.0f%%)", event.getOccupancyRate()));

        double pct = cap > 0 ? (double) students / cap : 0;
        progressBar.setForeground(pct >= 0.95 ? Theme.RED
                                : pct >= 0.70 ? Theme.YELLOW
                                             : Theme.ACCENT);

        statsLabel.setText("Cola: " + event.getQueueSize()
                + "     Asistencia: " + event.getAttendanceCount()
                + "     Historial: " + event.getHistorySize() + " ops"
                + "     AVL h=" + event.getTreeHeight());

        studentModel.setStudents(event.getStudentsSorted());

        queueModel.clear();
        Object[] q = event.getQueueContents();
        for (int i = 0; i < q.length; i++) {
            Student s = (Student) q[i];
            queueModel.addElement(String.format("%2d.  ID:%-6d %s", i + 1, s.getId(), s.getName()));
        }

        historyModel.clear();
        for (var entry : event.getHistoryEntries())
            historyModel.addElement("[" + entry.type + "]  " + entry.description);
    }

    // ── operaciones ───────────────────────────────────────────────────────
    private void registerStudent() {
        StudentFormDialog dlg = new StudentFormDialog(mainWindow);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;
        RegisterResult r = event.registerStudent(
            dlg.getStudentId(), dlg.getName(), dlg.getEmail(), dlg.getProgram());
        switch (r) {
            case REGISTERED         -> info("Registrado: " + dlg.getName() + " (ID: " + dlg.getStudentId() + ")");
            case QUEUED             -> info("Evento lleno — agregado a la cola (posición " + event.getQueueSize() + ").");
            case DUPLICATE_ID       -> warn("ID ya está registrado.");
            case DUPLICATE_IN_QUEUE -> warn("ID " + dlg.getStudentId() + " ya está en la cola.");
            case INVALID_DATA       -> error("Datos inválidos: ID > 0, nombre requerido, email debe contener @.");
        }
        refresh();
    }

    private void markAttendance() {
        Integer id = askId("Marcar asistencia para ID:");
        if (id == null) return;
        if (event.markAttendance(id)) info("Asistencia marcada para ID " + id + ".");
        else warn("No hay estudiante con ID " + id + ".");
        refresh();
    }

    private void removeStudent() {
        Integer id = askId("Eliminar estudiante con ID:");
        if (id == null) return;
        if (!event.hasStudent(id)) { warn("No hay estudiante con ID " + id + "."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "Eliminar estudiante ID " + id + "? Un estudiante en cola será promovido si hay disponibilidad.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        event.removeStudent(id);
        Student p = event.getLastPromoted();
        if (p != null) info("Eliminado. Promovido de la cola: " + p.getName() + " (ID: " + p.getId() + ")");
        refresh();
    }

    private void undoRemoval() {
        UndoResult r = event.undoLastRemoval();
        switch (r) {
            case UNDONE          -> info("Última eliminación deshecha. Estudiante restaurado.");
            case UNDONE_TO_QUEUE -> info("Restaurado pero movido a la cola (evento lleno).");
            case NO_OPERATIONS   -> warn("Sin operaciones para deshacer.");
            case NOT_A_REMOVAL   -> warn("La última operación no fue una eliminación.");
            case ERROR           -> error("No se pudo restaurar — ID ya existe.");
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

        JTextArea area = Theme.logArea();
        area.setText(clean.isEmpty() ? "(El árbol está vacío)" : clean);
        JScrollPane sp = Theme.scrollPane(area);
        sp.setPreferredSize(new Dimension(680, 460));
        JOptionPane.showMessageDialog(this, sp,
            "Árbol AVL — " + event.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar estudiantes a CSV");
        String safeName = event.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
        fc.setSelectedFile(new File(
                AppPaths.Dirs.RESULTS.resolve("events").resolve(safeName + ".csv").toString()));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) path += ".csv";
        if (event.exportCSV(path))
            info("Exportado: " + event.getStudentCount() + " estudiantes a:\n" + path);
        else
            warn("Sin estudiantes para exportar.");
        refresh();
    }

    private void addRandom() {
        String s = JOptionPane.showInputDialog(this, "¿Cuántos estudiantes aleatorios?", "10");
        if (s == null) return;
        int count;
        try { count = Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { error("Número inválido."); return; }
        if (count <= 0) { error("El número debe ser > 0."); return; }

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
        info("Agregados: " + reg + " registrados, " + queued + " en cola, "
            + (count - reg - queued) + " duplicados omitidos.");
        refresh();
    }

    private void showTopK() {
        String s = JOptionPane.showInputDialog(this,
                "Mostrar top K estudiantes por asistencia:", "5");
        if (s == null) return;
        int k;
        try { k = Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { error("Número inválido."); return; }
        if (k <= 0) { error("K debe ser > 0."); return; }

        Student[] top = event.getTopAttendees(k);
        mainWindow.getTopAttendeesPanel().setCurrentEvent(event);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-8s %-25s %-12s%n", "Rank", "ID", "Nombre", "Asistencias"));
        sb.append("─".repeat(52)).append("\n");
        for (int i = 0; i < top.length; i++)
            sb.append(String.format("%-4d %-8d %-25s %-12d%n",
                i + 1, top[i].getId(), top[i].getName(), top[i].getAttendanceCount()));
        if (top.length == 0) sb.append("  (No hay estudiantes con asistencias)");

        JTextArea area = Theme.logArea();
        area.setText(sb.toString());
        JScrollPane sp = Theme.scrollPane(area);
        sp.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(this, sp,
            "Top " + k + " Asistentes — " + event.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void clearAll() {
        if (event.isEmpty()) { warn("Sin estudiantes para limpiar."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "Esto eliminará TODOS los " + event.getStudentCount()
                + " estudiantes y vaciará la cola. No se puede deshacer.",
            "Confirmar limpieza", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        int n = event.clearAll();
        info("Limpiados " + n + " estudiantes.");
        refresh();
    }

    // ── helpers ───────────────────────────────────────────────────────────
    private Integer askId(String prompt) {
        String s = JOptionPane.showInputDialog(this, prompt);
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { error("ID inválido — debe ser un entero."); return null; }
    }

    private void info (String msg) { JOptionPane.showMessageDialog(this, msg, "Info",    JOptionPane.INFORMATION_MESSAGE); }
    private void warn (String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
}
