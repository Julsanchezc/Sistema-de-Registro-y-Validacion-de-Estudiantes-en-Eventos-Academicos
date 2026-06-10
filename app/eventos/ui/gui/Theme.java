package eventos.ui.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Theme.java — Modo oscuro estilo "carbon slate".
 *
 * Paleta oscura original, inspirada en herramientas de desarrollador modernas:
 *   Fondo base         : #0F1117  (casi negro, tono ligeramente azulado)
 *   Superficie panel   : #181C27  (azul-gris oscuro)
 *   Superficie elevada : #1E2336  (tarjetas, sidebars)
 *   Sidebar            : #161A28
 *   Borde sutil        : #2A2F45
 *   Acento primario    : #6C8EFF  (azul-violeta suave, diferente al azul Word)
 *   Acento hover       : #8BA4FF
 *   Acento presionado  : #5070E8
 *   Acento suave (bg)  : #232A45  (para activos/hover en sidebar)
 *   Texto principal    : #E2E8F0  (blanco suave)
 *   Texto secundario   : #94A3B8
 *   Texto tenue        : #475569
 *   Verde              : #34D399  (verde esmeralda)
 *   Verde suave        : #1A3A32
 *   Rojo               : #F87171  (rojo coral)
 *   Rojo suave         : #3A1A1A
 *   Ámbar              : #FBBF24
 *   Ámbar suave        : #3A2E10
 */
public final class Theme {

    // ── Colores base ─────────────────────────────────────────────────────
    public static final Color BG            = new Color(0x0F1117);
    public static final Color SURFACE       = new Color(0x181C27);
    public static final Color SURFACE_HIGH  = new Color(0x1E2336);
    public static final Color SIDEBAR       = new Color(0x161A28);
    public static final Color BORDER        = new Color(0x2A2F45);
    public static final Color BORDER_FOCUS  = new Color(0x6C8EFF);

    // ── Acento ───────────────────────────────────────────────────────────
    public static final Color ACCENT        = new Color(0x6C8EFF);
    public static final Color ACCENT_HOVER  = new Color(0x8BA4FF);
    public static final Color ACCENT_PRESS  = new Color(0x5070E8);
    public static final Color ACCENT_LIGHT  = new Color(0x232A45);  // bg suave activo
    public static final Color ACCENT_TEXT   = new Color(0x6C8EFF);  // texto acento

    // ── Texto ─────────────────────────────────────────────────────────────
    public static final Color TEXT          = new Color(0xE2E8F0);
    public static final Color TEXT_SUB      = new Color(0x94A3B8);
    public static final Color TEXT_DIM      = new Color(0x475569);
    public static final Color TEXT_ON_ACCENT= new Color(0xFFFFFF);

    // ── Semánticos ────────────────────────────────────────────────────────
    public static final Color GREEN         = new Color(0x34D399);
    public static final Color GREEN_LIGHT   = new Color(0x1A3A32);
    public static final Color RED           = new Color(0xF87171);
    public static final Color RED_LIGHT     = new Color(0x3A1A1A);
    public static final Color YELLOW        = new Color(0xFBBF24);
    public static final Color YELLOW_LIGHT  = new Color(0x3A2E10);

    public static final Color SEL_BG        = new Color(0x2A3460);
    public static final Color SEL_FG        = new Color(0xE2E8F0);

    // ── Fuentes ───────────────────────────────────────────────────────────
    public static final Font FONT_UI    = resolveFont("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_UI_B  = resolveFont("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_TITLE = resolveFont("Segoe UI", Font.BOLD,   15);
    public static final Font FONT_SMALL = resolveFont("Segoe UI", Font.PLAIN,  11);
    public static final Font FONT_MONO  = resolveFont("Consolas", Font.PLAIN,  12);
    public static final Font FONT_H1    = resolveFont("Segoe UI", Font.BOLD,   20);

    private static Font resolveFont(String name, int style, int size) {
        Font f = new Font(name, style, size);
        if (!f.getFamily().equalsIgnoreCase(name))
            f = new Font(Font.SANS_SERIF, style, size);
        return f;
    }

    // ── Aplicar tema globalmente ──────────────────────────────────────────
    public static void apply() {
        UIManager.put("Panel.background",             SURFACE);
        UIManager.put("Frame.background",             BG);
        UIManager.put("OptionPane.background",        SURFACE_HIGH);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground",             TEXT);
        UIManager.put("Label.font",                   FONT_UI);
        UIManager.put("Button.background",            SURFACE_HIGH);
        UIManager.put("Button.foreground",            TEXT);
        UIManager.put("Button.font",                  FONT_UI);
        UIManager.put("Button.border",
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(4, 14, 4, 14)));
        UIManager.put("Button.focus",                 new Color(0, 0, 0, 0));
        UIManager.put("TextField.background",         SURFACE_HIGH);
        UIManager.put("TextField.foreground",         TEXT);
        UIManager.put("TextField.caretForeground",    ACCENT);
        UIManager.put("TextField.font",               FONT_UI);
        UIManager.put("TextField.border",
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1),
                        new EmptyBorder(3, 8, 3, 8)));
        UIManager.put("Spinner.background",           SURFACE_HIGH);
        UIManager.put("Spinner.foreground",           TEXT);
        UIManager.put("Spinner.font",                 FONT_UI);
        UIManager.put("ComboBox.background",          SURFACE_HIGH);
        UIManager.put("ComboBox.foreground",          TEXT);
        UIManager.put("ComboBox.font",                FONT_UI);
        UIManager.put("ComboBox.selectionBackground", ACCENT);
        UIManager.put("ComboBox.selectionForeground", TEXT_ON_ACCENT);
        UIManager.put("Table.background",             SURFACE);
        UIManager.put("Table.foreground",             TEXT);
        UIManager.put("Table.font",                   FONT_UI);
        UIManager.put("Table.gridColor",              BORDER);
        UIManager.put("Table.selectionBackground",    SEL_BG);
        UIManager.put("Table.selectionForeground",    SEL_FG);
        UIManager.put("TableHeader.background",       SURFACE_HIGH);
        UIManager.put("TableHeader.foreground",       TEXT_SUB);
        UIManager.put("TableHeader.font",             FONT_UI_B);
        UIManager.put("ScrollPane.background",        SURFACE);
        UIManager.put("ScrollBar.background",         SURFACE);
        UIManager.put("ScrollBar.thumb",              new Color(0x2A2F45));
        UIManager.put("ScrollBar.track",              SURFACE);
        UIManager.put("TabbedPane.background",        BG);
        UIManager.put("TabbedPane.foreground",        TEXT_SUB);
        UIManager.put("TabbedPane.selected",          SURFACE);
        UIManager.put("TabbedPane.contentAreaColor",  SURFACE);
        UIManager.put("TabbedPane.font",              FONT_UI_B);
        UIManager.put("SplitPane.background",         BG);
        UIManager.put("SplitPane.dividerSize",        4);
        UIManager.put("List.background",              SURFACE);
        UIManager.put("List.foreground",              TEXT);
        UIManager.put("List.font",                    FONT_UI);
        UIManager.put("List.selectionBackground",     SEL_BG);
        UIManager.put("List.selectionForeground",     SEL_FG);
        UIManager.put("TextArea.background",          new Color(0x12151F));
        UIManager.put("TextArea.foreground",          TEXT);
        UIManager.put("TextArea.font",                FONT_MONO);
        UIManager.put("TextArea.caretForeground",     ACCENT);
        UIManager.put("ToolTip.background",           SURFACE_HIGH);
        UIManager.put("ToolTip.foreground",           TEXT);
        UIManager.put("ToolTip.font",                 FONT_SMALL);
        UIManager.put("OptionPane.buttonFont",        FONT_UI);
        UIManager.put("PopupMenu.background",         SURFACE_HIGH);
        UIManager.put("PopupMenu.foreground",         TEXT);
        UIManager.put("MenuItem.background",          SURFACE_HIGH);
        UIManager.put("MenuItem.foreground",          TEXT);
        UIManager.put("MenuItem.selectionBackground", SEL_BG);
        UIManager.put("MenuItem.selectionForeground", TEXT);
        UIManager.put("CheckBox.background",          SURFACE);
        UIManager.put("CheckBox.foreground",          TEXT);
        UIManager.put("RadioButton.background",       SURFACE);
        UIManager.put("RadioButton.foreground",       TEXT);
    }

    // ── Fábrica de componentes ─────────────────────────────────────────────

    /** Botón primario — acento violeta-azul. */
    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_UI_B);
        b.setForeground(TEXT_ON_ACCENT);
        b.setBackground(ACCENT);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 18, 6, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e)  { b.setBackground(ACCENT_HOVER);  }
            @Override public void mouseExited (MouseEvent e)  { b.setBackground(ACCENT);         }
            @Override public void mousePressed(MouseEvent e)  { b.setBackground(ACCENT_PRESS);  }
            @Override public void mouseReleased(MouseEvent e) { b.setBackground(ACCENT_HOVER);  }
        });
        return b;
    }

    /** Botón secundario — borde sutil modo oscuro. */
    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_UI);
        b.setForeground(TEXT);
        b.setBackground(SURFACE_HIGH);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(5, 14, 5, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0x252C42));
                b.setBorder(new CompoundBorder(
                        new LineBorder(ACCENT, 1, true),
                        new EmptyBorder(5, 14, 5, 14)));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(SURFACE_HIGH);
                b.setBorder(new CompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(5, 14, 5, 14)));
            }
        });
        return b;
    }

    /** Botón peligro — rojo coral. */
    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_UI);
        b.setForeground(RED);
        b.setBackground(SURFACE_HIGH);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x4A2020), 1, true),
                new EmptyBorder(5, 14, 5, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(RED_LIGHT);
                b.setBorder(new CompoundBorder(
                        new LineBorder(RED, 1, true),
                        new EmptyBorder(5, 14, 5, 14)));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(SURFACE_HIGH);
                b.setBorder(new CompoundBorder(
                        new LineBorder(new Color(0x4A2020), 1, true),
                        new EmptyBorder(5, 14, 5, 14)));
            }
        });
        return b;
    }

    /** Etiqueta de título de sección. */
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT);
        return l;
    }

    /** Etiqueta secundaria. */
    public static JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_SUB);
        return l;
    }

    /** JLabel con color específico. */
    public static JLabel colorLabel(String text, Color color, Font font) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(font);
        return l;
    }

    /** Panel toolbar modo oscuro. */
    public static JPanel toolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        p.setBackground(SURFACE_HIGH);
        p.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        return p;
    }

    /** Separador vertical. */
    public static JSeparator vSep() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setPreferredSize(new Dimension(1, 20));
        s.setForeground(BORDER);
        return s;
    }

    /** Scrollpane oscuro. */
    public static JScrollPane scrollPane(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.setBackground(SURFACE);
        sp.getViewport().setBackground(SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        // Scrollbar oscura
        sp.getVerticalScrollBar().setBackground(SURFACE);
        sp.getHorizontalScrollBar().setBackground(SURFACE);
        return sp;
    }

    /** Área de texto tipo log/terminal oscura. */
    public static JTextArea logArea() {
        JTextArea ta = new JTextArea();
        ta.setBackground(new Color(0x0D1018));
        ta.setForeground(new Color(0xA8B4CC));
        ta.setFont(FONT_MONO);
        ta.setEditable(false);
        ta.setBorder(new EmptyBorder(10, 14, 10, 14));
        ta.setCaretColor(ACCENT);
        return ta;
    }

    /** Barra de progreso modo oscuro. */
    public static JProgressBar progressBar(int max) {
        JProgressBar pb = new JProgressBar(0, max);
        pb.setStringPainted(true);
        pb.setBackground(new Color(0x1A1F30));
        pb.setForeground(ACCENT);
        pb.setFont(FONT_SMALL);
        pb.setBorder(null);
        pb.setPreferredSize(new Dimension(0, 18));
        return pb;
    }

    /**
     * Indicador circular (donut) modo oscuro.
     */
    public static JPanel circularIndicator(int value, int max, Color arcColor) {
        return new JPanel() {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(64, 64));
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int pad = 4, stroke = 7;
                int x = pad, y = pad, diam = Math.min(w, h) - 2 * pad;

                g2.setColor(new Color(0x2A2F45));
                g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(x, y, diam, diam, 90, -360);

                double pct = max > 0 ? (double) value / max : 0;
                int angle = (int) Math.round(pct * 360);
                g2.setColor(arcColor);
                g2.drawArc(x, y, diam, diam, 90, -angle);

                String pctStr = Math.round(pct * 100) + "%";
                g2.setFont(FONT_SMALL.deriveFont(Font.BOLD, 10f));
                g2.setColor(TEXT);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(pctStr)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(pctStr, tx, ty);
                g2.dispose();
            }
        };
    }
}
