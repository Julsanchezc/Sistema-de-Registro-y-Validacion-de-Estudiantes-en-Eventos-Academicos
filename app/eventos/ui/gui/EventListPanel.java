package eventos.ui.gui;

import core.AppPaths;
import core.exceptions.PersistenceException;
import eventos.repository.EventRepository;
import eventos.service.EventManager;
import eventos.service.EventService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EventListPanel extends JPanel {

    private final EventManager    manager;
    private final EventRepository repository;
    private final MainWindow      mainWindow;
    private final EventTableModel tableModel;
    private final JTable          table;
    private final JTextArea       agendaArea;

    public EventListPanel(EventManager manager, EventRepository repo, MainWindow win) {
        this.manager    = manager;
        this.repository = repo;
        this.mainWindow = win;
        this.tableModel = new EventTableModel(manager);
        this.table      = buildTable();
        this.agendaArea = new JTextArea();
        buildUI();
    }

    private JTable buildTable() {
        JTable t = new JTable(tableModel);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(24);
        t.setFillsViewportHeight(true);
        int[] widths = {36, 260, 72, 72, 72, 56, 76};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelected();
            }
        });
        return t;
    }

    private void buildUI() {
        setLayout(new BorderLayout(4, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildToolbar(),       BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildAgendaPanel(),   BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JLabel title = new JLabel("Event Manager");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        p.add(title);
        p.add(new JSeparator(SwingConstants.VERTICAL) {{ setPreferredSize(new Dimension(1, 20)); }});

        p.add(btn("New",     this::createEvent));
        p.add(btn("Open",    this::openSelected));
        p.add(btn("Delete",  this::deleteSelected));
        p.add(new JSeparator(SwingConstants.VERTICAL) {{ setPreferredSize(new Dimension(1, 20)); }});
        p.add(btn("Save",    this::saveData));
        p.add(btn("Load",    this::loadData));
        p.add(Box.createHorizontalStrut(8));
        p.add(btn("Refresh Agenda", this::refreshAgenda));
        return p;
    }

    private JPanel buildAgendaPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Availability Agenda  (MinHeap — least available capacity first)",
            TitledBorder.LEFT, TitledBorder.TOP));
        agendaArea.setEditable(false);
        agendaArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        agendaArea.setText("  Press 'Refresh Agenda' to compute the heap-ordered event list.");
        JScrollPane sp = new JScrollPane(agendaArea);
        sp.setPreferredSize(new Dimension(0, 110));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JButton btn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        return b;
    }

    // --- actions ---

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
        if (row < 0) { info("Select an event first."); return; }
        EventService ev = tableModel.getEventAt(row);
        if (ev != null) mainWindow.openEvent(ev);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { info("Select an event first."); return; }
        EventService ev = tableModel.getEventAt(row);
        int ok = JOptionPane.showConfirmDialog(this,
            "Delete event \"" + ev.getName() + "\"?  This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            manager.removeEvent(row + 1);
            refresh();
        }
    }

    private void saveData() {
        try {
            repository.save(manager, AppPaths.Files.EVENTS_JSON);
            JOptionPane.showMessageDialog(this,
                "Saved " + manager.getEventCount() + " event(s) to\n"
                    + AppPaths.Files.EVENTS_JSON.toAbsolutePath(),
                "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (PersistenceException ex) {
            JOptionPane.showMessageDialog(this, "Error saving:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        int ok = JOptionPane.showConfirmDialog(this,
            "Loading will replace all current data. Continue?",
            "Confirm Load", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            repository.load(manager, AppPaths.Files.EVENTS_JSON);
            refresh();
            JOptionPane.showMessageDialog(this,
                "Loaded " + manager.getEventCount() + " event(s).",
                "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (PersistenceException ex) {
            JOptionPane.showMessageDialog(this, "Error loading:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAgenda() {
        EventService[] sorted = manager.getEventsByAvailability();
        if (sorted.length == 0) { agendaArea.setText("  No events."); return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.length; i++) {
            EventService ev  = sorted[i];
            int          avl = ev.getCapacity() - ev.getStudentCount();
            sb.append(String.format("  %2d.  %-38s  %4d / %4d enrolled   %4d spots free%n",
                i + 1, ev.getName(), ev.getStudentCount(), ev.getCapacity(), avl));
        }
        agendaArea.setText(sb.toString());
        agendaArea.setCaretPosition(0);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refresh() { tableModel.refresh(); }
}
