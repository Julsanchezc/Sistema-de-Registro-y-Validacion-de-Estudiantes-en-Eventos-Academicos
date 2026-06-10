package eventos.ui.gui;

import core.AppPaths;
import core.exceptions.PersistenceException;
import eventos.repository.EventRepository;
import eventos.service.EventManager;
import eventos.service.EventService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;

/**
 * EventListPanel.java — Modo oscuro carbon-slate.
 * Agenda (MinHeap) con tabla visual en lugar de área de texto.
 */
public class EventListPanel extends JPanel {

    private final EventManager    manager;
    private final EventRepository repository;
    private final MainWindow      mainWindow;
    private final EventTableModel tableModel;
    private final JTable          table;

    // Agenda MinHeap como tabla visual
    private final DefaultTableModel agendaModel;
    private final JTable            agendaTable;

    public EventListPanel(EventManager manager, EventRepository repo, MainWindow win) {
        this.manager    = manager;
        this.repository = repo;
        this.mainWindow = win;
        this.tableModel = new EventTableModel(manager);
        this.table      = buildTable();

        this.agendaModel = new DefaultTableModel(
                new String[]{"#", "Evento", "Inscritos", "Capacidad", "Disponibles", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.agendaTable = buildAgendaTable();
        buildUI();
    }

    // ── tabla principal ───────────────────────────────────────────────────
    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(30);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.setBackground(Theme.SURFACE);
        t.setForeground(Theme.TEXT);
        t.setFont(Theme.FONT_UI);
        t.setGridColor(Theme.BORDER);
        t.setSelectionBackground(Theme.SEL_BG);
        t.setSelectionForeground(Theme.SEL_FG);

        JTableHeader h = t.getTableHeader();
        h.setBackground(Theme.SURFACE_HIGH);
        h.setForeground(Theme.TEXT_SUB);
        h.setFont(Theme.FONT_UI_B);
        h.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Renderer columna Ocupación
        t.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String s = val != null ? val.toString().replace("%", "") : "0";
                    try {
                        double pct = Double.parseDouble(s);
                        l.setForeground(pct >= 95 ? Theme.RED
                                      : pct >= 70 ? Theme.YELLOW
                                                  : Theme.GREEN);
                    } catch (NumberFormatException ignored) { l.setForeground(Theme.TEXT); }
                }
                l.setBackground(sel ? Theme.SEL_BG : Theme.SURFACE);
                l.setOpaque(true);
                return l;
            }
        });

        int[] widths = {36, 260, 80, 80, 80, 64, 80};
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });
        return t;
    }

    // ── tabla de agenda (MinHeap) ─────────────────────────────────────────
    private JTable buildAgendaTable() {
        JTable t = new JTable(agendaModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(26);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.setBackground(new Color(0x0D1018));
        t.setForeground(Theme.TEXT);
        t.setFont(Theme.FONT_UI);
        t.setGridColor(new Color(0x1E2336));
        t.setSelectionBackground(Theme.SEL_BG);
        t.setSelectionForeground(Theme.SEL_FG);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(0x141824));
        h.setForeground(Theme.TEXT_DIM);
        h.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        h.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Columna Estado con color
        t.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String s = val != null ? val.toString() : "";
                if (!sel) {
                    if (s.contains("Lleno"))       l.setForeground(Theme.RED);
                    else if (s.contains("Casi"))   l.setForeground(Theme.YELLOW);
                    else                           l.setForeground(Theme.GREEN);
                }
                l.setBackground(sel ? Theme.SEL_BG : new Color(0x0D1018));
                l.setOpaque(true);
                return l;
            }
        });

        // Columna Disponibles con color
        t.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    try {
                        int v = Integer.parseInt(val.toString());
                        l.setForeground(v == 0 ? Theme.RED : v < 10 ? Theme.YELLOW : Theme.GREEN);
                    } catch (Exception e) { l.setForeground(Theme.TEXT); }
                }
                l.setBackground(sel ? Theme.SEL_BG : new Color(0x0D1018));
                l.setOpaque(true);
                return l;
            }
        });

        int[] widths2 = {30, 220, 80, 80, 90, 80};
        for (int i = 0; i < widths2.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths2[i]);

        return t;
    }

    // ── UI ────────────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        add(buildToolbar(),       BorderLayout.NORTH);
        add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(buildAgendaPanel(),   BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel p = Theme.toolbar();

        p.add(Theme.sectionLabel("Gestión de Eventos"));
        p.add(Theme.vSep());

        JButton btnNew  = Theme.primaryButton("+ Nuevo");
        JButton btnOpen = Theme.secondaryButton("Abrir");
        JButton btnDel  = Theme.dangerButton("Eliminar");
        p.add(btnNew);
        p.add(btnOpen);
        p.add(btnDel);
        p.add(Theme.vSep());

        JButton btnSave   = Theme.secondaryButton("Guardar");
        JButton btnLoad   = Theme.secondaryButton("Cargar");
        p.add(btnSave);
        p.add(btnLoad);
        p.add(Theme.vSep());

        JButton btnAgenda = Theme.secondaryButton("Actualizar Prioridades");
        p.add(btnAgenda);

        btnNew   .addActionListener(e -> createEvent());
        btnOpen  .addActionListener(e -> openSelected());
        btnDel   .addActionListener(e -> deleteSelected());
        btnSave  .addActionListener(e -> saveData());
        btnLoad  .addActionListener(e -> loadData());
        btnAgenda.addActionListener(e -> refreshAgenda());

        return p;
    }

    private JPanel buildAgendaPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x0D1018));
        p.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));
        p.setPreferredSize(new Dimension(0, 130));

        // Header de la agenda
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        header.setBackground(new Color(0x141824));
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        JLabel icon = new JLabel("*");
        icon.setFont(Theme.FONT_SMALL);
        icon.setForeground(Theme.ACCENT);

        JLabel lbl = Theme.colorLabel("Prioridad de Disponibilidad — MinHeap (menor disponibilidad primero)",
                Theme.TEXT_SUB, Theme.FONT_SMALL);
        JLabel hint = Theme.colorLabel("· Presiona 'Actualizar Prioridades' para recalcular",
                Theme.TEXT_DIM, Theme.FONT_SMALL);
        header.add(icon);
        header.add(lbl);
        header.add(hint);
        p.add(header, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(agendaTable);
        sp.setBorder(null);
        sp.setBackground(new Color(0x0D1018));
        sp.getViewport().setBackground(new Color(0x0D1018));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ── acciones ──────────────────────────────────────────────────────────
    private void createEvent() {
        EventFormDialog dlg = new EventFormDialog(mainWindow);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            manager.createEvent(dlg.getEventName(), dlg.getCapacity());
            refresh();
        }
    }

    private void openSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { info("Selecciona un evento primero (o haz doble clic)."); return; }
        EventService ev = tableModel.getEventAt(row);
        if (ev != null) mainWindow.openEvent(ev);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { info("Selecciona un evento primero."); return; }
        EventService ev = tableModel.getEventAt(row);
        int ok = JOptionPane.showConfirmDialog(this,
            "Eliminar evento \"" + ev.getName() + "\"?  Esta acción no se puede deshacer.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            manager.removeEvent(row + 1);
            refresh();
        }
    }

    private void saveData() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar datos de eventos");
        fc.setSelectedFile(new File(AppPaths.Files.EVENTS_JSON.toAbsolutePath().toString()));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = fc.getSelectedFile().toPath();
        if (!path.toString().toLowerCase().endsWith(".json"))
            path = path.resolveSibling(path.getFileName() + ".json");
        try {
            repository.save(manager, path);
            JOptionPane.showMessageDialog(this,
                "Guardado: " + manager.getEventCount() + " evento(s)\n" + path,
                "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (PersistenceException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        int ok = JOptionPane.showConfirmDialog(this,
            "Cargar reemplazará todos los datos actuales. ¿Continuar?",
            "Confirmar carga", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cargar datos de eventos");
        fc.setCurrentDirectory(AppPaths.Files.EVENTS_JSON.getParent().toFile());
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivos JSON (*.json)", "json"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = fc.getSelectedFile().toPath();
        try {
            repository.load(manager, path);
            refresh();
            JOptionPane.showMessageDialog(this,
                "Cargado: " + manager.getEventCount() + " evento(s).",
                "Cargado", JOptionPane.INFORMATION_MESSAGE);
        } catch (PersistenceException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAgenda() {
        EventService[] sorted = manager.getEventsByAvailability();
        agendaModel.setRowCount(0);
        if (sorted.length == 0) return;
        for (int i = 0; i < sorted.length; i++) {
            EventService ev  = sorted[i];
            int cap  = ev.getCapacity();
            int reg  = ev.getStudentCount();
            int avl  = cap - reg;
            double pct = cap > 0 ? (double) reg / cap * 100 : 0;
            String estado = pct >= 100 ? "Lleno"
                          : pct >= 80  ? "Casi lleno"
                                       : "Disponible";
            agendaModel.addRow(new Object[]{i + 1, ev.getName(), reg, cap, avl, estado});
        }
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refresh() { tableModel.refresh(); }
}
