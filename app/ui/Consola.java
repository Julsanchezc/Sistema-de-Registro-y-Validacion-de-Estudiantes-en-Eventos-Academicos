package ui;

import model.Estudiante;

/**
 * Consola.java
 * Utilidades visuales para la terminal: barras de progreso, tablas con bordes.
 * Solo usa ANSI – sin librerías externas ni GUI.
 */
public class Consola {

    // Anchos fijos de columna para la tabla de estudiantes
    private static final int W_ID   = 8;
    private static final int W_NOM  = 26;
    private static final int W_COR  = 30;
    private static final int W_PROG = 21;
    private static final int W_AST  = 9;

    // =========================================================
    // UTILIDADES INTERNAS
    // =========================================================
    private static String rep(String s, int n) {
        StringBuilder sb = new StringBuilder(n * s.length());
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    private static String truncar(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
