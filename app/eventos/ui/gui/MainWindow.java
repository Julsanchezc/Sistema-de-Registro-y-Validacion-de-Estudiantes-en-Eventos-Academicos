package eventos.ui.gui;

import eventos.repository.EventRepository;
import eventos.service.EventManager;
import eventos.service.EventService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * MainWindow.java — Modo oscuro "carbon slate".
 * Sidebar con íconos + nombre de sistema limpio sin metadatos del curso.
 */
public class MainWindow extends JFrame {

    private final EventManager    manager;
    private final EventRepository repository;

    private final EventListPanel    eventListPanel;
    private final EventDetailPanel  eventDetailPanel;
    private final TopAttendeesPanel topAttendeesPanel;
    private final RendimientoPanel  rendimientoPanel;

    private final JPanel     contentCard;
    private final CardLayout contentLayout;

    private JButton btnEventos;
    private JButton btnAsistentes;
    private JButton btnRendimiento;

    public MainWindow() {
        manager           = new EventManager();
        repository        = new EventRepository();
        eventListPanel    = new EventListPanel(manager, repository, this);
        eventDetailPanel  = new EventDetailPanel(this);
        topAttendeesPanel = new TopAttendeesPanel(manager);
        rendimientoPanel  = new RendimientoPanel();

        contentLayout = new CardLayout();
        contentCard   = new JPanel(contentLayout);
        buildUI();
    }

    private void buildUI() {
        setTitle("Sistema de Registro y Validación de Estudiantes en Eventos Académicos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        getContentPane().setLayout(new BorderLayout());

        // ── header superior ─────────────────────────────────────────────
        getContentPane().add(buildHeader(), BorderLayout.NORTH);

        // ── layout central: sidebar + contenido ─────────────────────────
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG);
        center.add(buildSidebar(), BorderLayout.WEST);

        contentCard.setBackground(Theme.BG);
        contentCard.add(eventListPanel,    "eventos");
        contentCard.add(eventDetailPanel,  "detalle");
        contentCard.add(topAttendeesPanel, "asistentes");
        contentCard.add(rendimientoPanel,  "rendimiento");
        contentLayout.show(contentCard, "eventos");
        center.add(contentCard, BorderLayout.CENTER);

        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.SURFACE_HIGH);
        p.setPreferredSize(new Dimension(0, 52));
        p.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Logo + nombre del sistema
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        // Punto de acento como logo
        JLabel dot = new JLabel("\u25CF");
        dot.setFont(new Font(Theme.FONT_H1.getName(), Font.BOLD, 18));
        dot.setForeground(Theme.ACCENT);

        JLabel name = new JLabel("Sistema de Registro de Estudiantes en Eventos");
        name.setFont(Theme.FONT_UI_B);
        name.setForeground(Theme.TEXT);
        left.add(dot);
        left.add(name);
        p.add(left, BorderLayout.WEST);

        // Badge de versión a la derecha
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JLabel badge = new JLabel("v2.0");
        badge.setFont(Theme.FONT_SMALL);
        badge.setForeground(Theme.ACCENT);
        badge.setBackground(Theme.ACCENT_LIGHT);
        badge.setOpaque(true);
        badge.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x3A4570), 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        right.add(badge);
        p.add(right, BorderLayout.EAST);

        // Centrar verticalmente
        p.add(Box.createVerticalStrut(0), BorderLayout.CENTER);
        // Ajustar padding vertical
        left.setBorder(new EmptyBorder(14, 0, 0, 0));
        right.setBorder(new EmptyBorder(14, 0, 0, 0));

        return p;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.SIDEBAR);
        side.setBorder(new MatteBorder(0, 0, 0, 1, Theme.BORDER));
        side.setPreferredSize(new Dimension(200, 0));

        side.add(Box.createVerticalStrut(20));

        side.add(sidebarSection("NAVEGACIÓN"));
        btnEventos    = sidebarButton("Eventos",     "eventos");
        btnAsistentes = sidebarButton("Asistentes",  "asistentes");
        side.add(btnEventos);
        side.add(btnAsistentes);

        side.add(Box.createVerticalStrut(8));
        side.add(sidebarDivider());
        side.add(Box.createVerticalStrut(8));
        side.add(sidebarSection("ANÁLISIS"));
        btnRendimiento = sidebarButton("Rendimiento", "rendimiento");
        side.add(btnRendimiento);

        side.add(Box.createVerticalGlue());

        // chips de estructuras al fondo de la sidebar
        side.add(sidebarStructureInfo());

        markActive(btnEventos);
        return side;
    }

    private JPanel sidebarStructureInfo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(10, 12, 16, 12)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = Theme.colorLabel("Estructuras activas", Theme.TEXT_DIM, Theme.FONT_SMALL);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(8));

        String[] structures = {"AVL", "BST", "Cola", "HashTable", "MinHeap", "Pila"};
        JPanel row1 = chipRow(structures, 0, 3);
        JPanel row2 = chipRow(structures, 3, 6);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(row1);
        p.add(Box.createVerticalStrut(4));
        p.add(row2);
        return p;
    }

    private JPanel chipRow(String[] items, int from, int to) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        for (int i = from; i < to && i < items.length; i++) {
            JLabel chip = new JLabel(items[i]);
            chip.setFont(new Font(Theme.FONT_SMALL.getName(), Font.BOLD, 9));
            chip.setForeground(Theme.ACCENT);
            chip.setBackground(Theme.ACCENT_LIGHT);
            chip.setOpaque(true);
            chip.setBorder(new CompoundBorder(
                    new LineBorder(new Color(0x3A4570), 1, true),
                    new EmptyBorder(1, 5, 1, 5)));
            p.add(chip);
        }
        return p;
    }

    private JLabel sidebarSection(String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font(Theme.FONT_SMALL.getName(), Font.BOLD, 10));
        l.setForeground(Theme.TEXT_DIM);
        l.setBorder(new EmptyBorder(4, 0, 6, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return l;
    }

    private JSeparator sidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JButton sidebarButton(String text, String card) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT_UI);
        b.setForeground(Theme.TEXT_SUB);
        b.setBackground(Theme.SIDEBAR);
        b.setOpaque(true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(9, 14, 9, 14));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!b.getBackground().equals(Theme.ACCENT_LIGHT))
                    b.setBackground(new Color(0x1C2235));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!b.getBackground().equals(Theme.ACCENT_LIGHT))
                    b.setBackground(Theme.SIDEBAR);
            }
        });

        b.addActionListener(e -> {
            markActive(b);
            contentLayout.show(contentCard, card);
        });
        return b;
    }

    private void markActive(JButton active) {
        for (JButton btn : new JButton[]{btnEventos, btnAsistentes, btnRendimiento}) {
            if (btn == null) continue;
            btn.setBackground(Theme.SIDEBAR);
            btn.setForeground(Theme.TEXT_SUB);
            btn.setFont(Theme.FONT_UI);
            btn.setBorder(new EmptyBorder(9, 14, 9, 14));
        }
        // Barra lateral izquierda de color como indicador activo
        active.setBackground(Theme.ACCENT_LIGHT);
        active.setForeground(Theme.TEXT);
        active.setFont(Theme.FONT_UI_B);
        active.setBorder(new CompoundBorder(
                new MatteBorder(0, 3, 0, 0, Theme.ACCENT),
                new EmptyBorder(9, 11, 9, 14)));
    }

    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        p.setBackground(new Color(0x0C0F18));
        p.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        JLabel dot = new JLabel("\u25CF");
        dot.setFont(Theme.FONT_SMALL);
        dot.setForeground(Theme.GREEN);
        p.add(dot);

        JLabel lbl = Theme.colorLabel("Listo  ·  AVL + BST + Cola + HashTable + MinHeap + Pila de Historial",
                Theme.TEXT_DIM, Theme.FONT_SMALL);
        p.add(lbl);
        return p;
    }

    public void openEvent(EventService event) {
        eventDetailPanel.setEvent(event);
        topAttendeesPanel.setCurrentEvent(event);
        contentLayout.show(contentCard, "detalle");
        markActive(btnEventos);
    }

    public void backToList() {
        eventListPanel.refresh();
        contentLayout.show(contentCard, "eventos");
        markActive(btnEventos);
    }

    public EventManager      getManager()           { return manager; }
    public EventRepository   getRepository()        { return repository; }
    public TopAttendeesPanel getTopAttendeesPanel() { return topAttendeesPanel; }
}
