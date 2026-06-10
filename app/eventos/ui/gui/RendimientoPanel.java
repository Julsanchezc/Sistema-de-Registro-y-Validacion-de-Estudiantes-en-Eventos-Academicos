package eventos.ui.gui;

import eventos.service.BenchmarkRunner;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;

/**
 * RendimientoPanel.java — Modo oscuro carbon-slate.
 * Resultados mostrados en UI visual con tabla + log coloreado,
 * sin necesidad de revisar la consola.
 */
public class RendimientoPanel extends JPanel {

    private final JTextArea    logArea;
    private final JButton      btnEjecutar;
    private final JLabel       lblEstado;
    private final JProgressBar progressBar;
    private boolean            corriendo = false;

    // Tabla de resultados
    private final javax.swing.table.DefaultTableModel resultModel;
    private final JTable resultTable;

    public RendimientoPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);

        logArea = Theme.logArea();

        this.resultModel = new javax.swing.table.DefaultTableModel(
                new String[]{"Estructura", "n", "Inserción (ms)", "Búsqueda (ms)", "Eliminación (ms)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        this.resultTable = buildResultTable();

        btnEjecutar = Theme.primaryButton("Ejecutar Prueba de Rendimiento");
        lblEstado   = Theme.colorLabel("  Listo. Presiona el botón para iniciar.",
                Theme.TEXT_SUB, Theme.FONT_UI);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setBackground(new Color(0x1A1F30));
        progressBar.setForeground(Theme.ACCENT);
        progressBar.setBorder(null);
        progressBar.setPreferredSize(new Dimension(0, 4));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    private JTable buildResultTable() {
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

        javax.swing.table.JTableHeader h = t.getTableHeader();
        h.setBackground(Theme.SURFACE_HIGH);
        h.setForeground(Theme.TEXT_SUB);
        h.setFont(Theme.FONT_UI_B);
        h.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Renderer para colorear según estructura
        javax.swing.table.DefaultTableCellRenderer cr = new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setHorizontalAlignment(col == 0 || col == 1 ? SwingConstants.LEFT : SwingConstants.RIGHT);
                if (!sel) {
                    String struct = tbl.getValueAt(row, 0) != null ? tbl.getValueAt(row, 0).toString() : "";
                    if (struct.contains("AVL"))         l.setForeground(Theme.GREEN);
                    else if (struct.contains("Degen"))  l.setForeground(Theme.RED);
                    else                                l.setForeground(Theme.YELLOW);
                    l.setBackground(Theme.SURFACE);
                } else {
                    l.setBackground(Theme.SEL_BG);
                    l.setForeground(Theme.SEL_FG);
                }
                l.setOpaque(true);
                return l;
            }
        };
        for (int i = 0; i < 5; i++) t.getColumnModel().getColumn(i).setCellRenderer(cr);
        int[] w = {120, 80, 120, 120, 120};
        for (int i = 0; i < 5; i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        return t;
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(Theme.SURFACE_HIGH);
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        JLabel title = Theme.sectionLabel("Análisis de Rendimiento — AVL vs BST");
        JLabel sub = Theme.colorLabel(
                "Mide tiempos de inserción, búsqueda y eliminación a n = 10⁴, 10⁵, 10⁶",
                Theme.TEXT_SUB, Theme.FONT_SMALL);
        left.add(title);
        left.add(sub);
        p.add(left, BorderLayout.CENTER);

        btnEjecutar.setPreferredSize(new Dimension(300, 38));
        btnEjecutar.addActionListener(e -> ejecutar());
        p.add(btnEjecutar, BorderLayout.EAST);
        return p;
    }

    private JSplitPane buildContent() {
        JPanel infoPanel = buildInfoCards();

        // Panel derecho: tabla de resultados encima + log abajo
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Theme.BG);
        rightPanel.setBorder(new MatteBorder(0, 1, 0, 0, Theme.BORDER));

        // Tabla de resultados
        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setBackground(Theme.SURFACE_HIGH);
        JLabel tableTitle = Theme.colorLabel("  Resultados de la prueba",
                Theme.TEXT_DIM, Theme.FONT_SMALL);
        tableTitle.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(6, 6, 6, 6)));
        tableTitle.setBackground(Theme.SURFACE_HIGH);
        tableTitle.setOpaque(true);
        tableSection.add(tableTitle, BorderLayout.NORTH);
        JScrollPane tableSp = new JScrollPane(resultTable);
        tableSp.setBorder(null);
        tableSp.setBackground(Theme.SURFACE);
        tableSp.getViewport().setBackground(Theme.SURFACE);
        tableSection.add(tableSp, BorderLayout.CENTER);
        tableSection.setPreferredSize(new Dimension(0, 180));

        // Log completo (salida del benchmark)
        JPanel logSection = new JPanel(new BorderLayout());
        logSection.setBackground(new Color(0x0D1018));
        JLabel logTitle = Theme.colorLabel("  Salida completa del benchmark",
                Theme.TEXT_DIM, Theme.FONT_SMALL);
        logTitle.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(6, 6, 6, 6)));
        logTitle.setBackground(new Color(0x141824));
        logTitle.setOpaque(true);

        logArea.setText("  Presiona 'Ejecutar Prueba de Rendimiento' para iniciar.\n\n"
                + "  Tamaños de prueba : n = 10.000 · 100.000 · 1.000.000\n"
                + "  Operaciones       : inserción · búsqueda · eliminación\n"
                + "  Caso degenerado   : inserción secuencial 1..2000\n\n"
                + "  Los resultados también se exportan a results/results.csv\n"
                + "  Los resultados se mostrarán AQUÍ (no en consola).");
        logSection.add(logTitle, BorderLayout.NORTH);
        logSection.add(Theme.scrollPane(logArea), BorderLayout.CENTER);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableSection, logSection);
        rightSplit.setResizeWeight(0.35);
        rightSplit.setDividerSize(4);
        rightSplit.setBorder(null);
        rightSplit.setBackground(Theme.BG);
        rightPanel.add(rightSplit, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, rightPanel);
        split.setResizeWeight(0.28);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(Theme.BG);
        return split;
    }

    private JPanel buildInfoCards() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG);
        p.setBorder(new EmptyBorder(14, 14, 14, 10));

        p.add(infoCard("AVL Tree",
                "Auto-balanceado. Garantiza h ≤ 1.44·log₂(n) en cualquier caso.",
                "O(log n)", Theme.GREEN, Theme.GREEN_LIGHT));
        p.add(Box.createVerticalStrut(10));
        p.add(infoCard("BST Estándar",
                "Sin balanceo. Degrada a O(n) con inserción secuencial.",
                "O(log n)*", Theme.YELLOW, Theme.YELLOW_LIGHT));
        p.add(Box.createVerticalStrut(10));
        p.add(infoCard("BST Degenerado",
                "Inserción 1, 2, 3... produce una lista enlazada.",
                "O(n) BST", Theme.RED, Theme.RED_LIGHT));
        p.add(Box.createVerticalStrut(14));

        JPanel gauges = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        gauges.setBackground(Theme.SURFACE_HIGH);
        gauges.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        gauges.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        gauges.setAlignmentX(Component.LEFT_ALIGNMENT);

        gauges.add(gaugeCard("AVL h=10",    Theme.circularIndicator(10, 14, Theme.GREEN)));
        gauges.add(gaugeCard("BST h=14",    Theme.circularIndicator(14, 14, Theme.YELLOW)));
        gauges.add(gaugeCard("BST-Degen h=n", Theme.circularIndicator(14, 14, Theme.RED)));

        JLabel gTitle = Theme.colorLabel("Altura relativa (n=1024)", Theme.TEXT_DIM, Theme.FONT_SMALL);
        gTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(gTitle);
        p.add(Box.createVerticalStrut(6));
        p.add(gauges);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel infoCard(String title, String desc, String badge, Color badgeColor, Color badgeBg) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setBackground(Theme.SURFACE_HIGH);
        p.setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(Theme.FONT_UI_B);
        lTitle.setForeground(Theme.TEXT);

        JLabel lBadge = new JLabel(badge);
        lBadge.setFont(new Font(Theme.FONT_SMALL.getName(), Font.BOLD, 11));
        lBadge.setForeground(badgeColor);
        lBadge.setBackground(badgeBg);
        lBadge.setOpaque(true);
        lBadge.setBorder(new CompoundBorder(
                new LineBorder(badgeColor.darker(), 1, true),
                new EmptyBorder(2, 8, 2, 8)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lTitle, BorderLayout.WEST);
        top.add(lBadge, BorderLayout.EAST);

        JLabel lDesc = new JLabel("<html><body style='color:#94A3B8;font-size:11px'>"
                + desc.replace("\n", "<br>") + "</body></html>");
        lDesc.setFont(Theme.FONT_SMALL);

        p.add(top,   BorderLayout.NORTH);
        p.add(lDesc, BorderLayout.CENTER);
        return p;
    }

    private JPanel gaugeCard(String label, JPanel gauge) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = Theme.colorLabel(label, Theme.TEXT_DIM, Theme.FONT_SMALL);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(gauge, BorderLayout.CENTER);
        p.add(l, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x0C0F18));
        p.setBorder(new MatteBorder(1, 0, 0, 0, Theme.BORDER));

        progressBar.setPreferredSize(new Dimension(0, 4));
        p.add(progressBar, BorderLayout.NORTH);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 14, 5, 14));
        row.add(lblEstado, BorderLayout.WEST);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private void ejecutar() {
        if (corriendo) return;
        corriendo = true;
        btnEjecutar.setEnabled(false);
        btnEjecutar.setText("  Ejecutando...");
        progressBar.setIndeterminate(true);
        lblEstado.setText("  Ejecutando prueba de rendimiento...");
        logArea.setText("");
        resultModel.setRowCount(0);

        new Thread(() -> {
            PrintStream oldOut = System.out;
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(buf) {
                @Override public void println(String x) {
                    super.println(x);
                    updateLog(buf);
                }
                @Override public void print(String x) {
                    super.print(x);
                    updateLog(buf);
                }
            };
            System.setOut(ps);
            try {
                BenchmarkRunner.runBenchmarks(new int[]{10_000, 100_000, 1_000_000});
            } catch (Exception ex) {
                System.out.println("\nError: " + ex.getMessage());
            } finally {
                System.setOut(oldOut);
                String finalTxt = buf.toString().replaceAll("\u001B\\[[0-9;]*[mA-Za-z]", "");
                // Parsear todas las líneas del output completo para llenar la tabla
                for (String ln : finalTxt.split("\\r?\\n")) {
                    parseResultLine(ln);
                }
                SwingUtilities.invokeLater(() -> {
                    logArea.setText(finalTxt);
                    logArea.setCaretPosition(0);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setForeground(Theme.GREEN);
                    lblEstado.setText("  Prueba completada. Resultados en results/");
                    btnEjecutar.setEnabled(true);
                    btnEjecutar.setText("Ejecutar de nuevo");
                    corriendo = false;
                });
            }
        }, "benchmark-thread").start();
    }

    private void updateLog(ByteArrayOutputStream buf) {
        String txt = buf.toString().replaceAll("\u001B\\[[0-9;]*[mA-Za-z]", "");
        SwingUtilities.invokeLater(() -> {
            logArea.setText(txt);
            logArea.setCaretPosition(Math.max(0, logArea.getText().length() - 1));
        });
    }

    /**
     * Parsea líneas del benchmark de BenchmarkRunner.
     * Formato real de línea de datos:
     *   10,000        11 /      1 /      5 ms         5 /      2 /      5 ms     16 / 30
     * Se crean dos filas: una para AVL y otra para BST.
     * La sección degenerada genera una fila extra.
     */
    private void parseResultLine(String line) {
        if (line == null) return;
        // Quitar códigos ANSI
        String clean = line.replaceAll("\u001B\\[[0-9;]*[mA-Za-z]", "").trim();

        // Línea de datos: empieza con el número n seguido de los tiempos AVL y BST
        // Ejemplo: "10,000        11 /      1 /      5 ms         5 /      2 /      5 ms     16 / 30"
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "^([\\d,\\.]+)\\s+(\\d+)\\s*/\\s*(\\d+)\\s*/\\s*(\\d+)\\s*ms\\s+(\\d+)\\s*/\\s*(\\d+)\\s*/\\s*(\\d+)\\s*ms"
        ).matcher(clean);

        if (m.find()) {
            String nVal   = m.group(1).replace(",", "").replace(".", "");
            String avlIns = m.group(2), avlBus = m.group(3), avlElim = m.group(4);
            String bstIns = m.group(5), bstBus = m.group(6), bstElim = m.group(7);
            SwingUtilities.invokeLater(() -> {
                resultModel.addRow(new Object[]{"AVL",          nVal, avlIns, avlBus, avlElim});
                resultModel.addRow(new Object[]{"BST Estándar", nVal, bstIns, bstBus, bstElim});
            });
            return;
        }

        // Caso degenerado — formato: "AVL altura: 11     (log2...)"
        java.util.regex.Matcher avlH = java.util.regex.Pattern
            .compile("AVL altura:\\s*(\\d+)").matcher(clean);
        if (avlH.find()) {
            final String h = avlH.group(1);
            SwingUtilities.invokeLater(() ->
                resultModel.addRow(new Object[]{"AVL Degen", "2000", h, "-", "-"}));
        }
        java.util.regex.Matcher bstH = java.util.regex.Pattern
            .compile("BST altura:\\s*(\\d+)").matcher(clean);
        if (bstH.find()) {
            final String h = bstH.group(1);
            SwingUtilities.invokeLater(() ->
                resultModel.addRow(new Object[]{"BST Degenerado", "2000", h, "-", "-"}));
        }
    }
}
