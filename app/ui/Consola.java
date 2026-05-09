package ui;

import model.Estudiante;

/**
 * Consola.java
 * Utilidades visuales para la terminal: barras de progreso, tablas con bordes.
 * Solo usa ANSI – sin librerías externas ni GUI.
 */
public class Consola {

    // =========================================================
    // BARRA DE PROGRESO – retorna String coloreado
    // Color dinámico: verde < 70 %, amarillo < 90 %, rojo >= 90 %
    // =========================================================
    public static String barraProgreso(int actual, int total, int ancho) {
        if (total <= 0)
            return Colores.CYAN + "[" + rep("░", ancho) + "]  0.0%" + Colores.RESET;

        double pct    = Math.min((double) actual / total, 1.0);
        int    llenas = (int) (pct * ancho);
        int    vacias = ancho - llenas;

        String colorBarra;
        if      (pct >= 0.90) colorBarra = Colores.ROJO_B;
        else if (pct >= 0.70) colorBarra = Colores.AMARILLO_B;
        else                  colorBarra = Colores.VERDE_B;

        return Colores.CYAN + "[" + Colores.RESET
                + colorBarra + rep("█", llenas) + Colores.RESET
                + Colores.CYAN + rep("░", vacias) + Colores.RESET
                + Colores.CYAN + "]" + Colores.RESET
                + " " + colorBarra + String.format("%5.1f%%", pct * 100) + Colores.RESET;
    }

    // =========================================================
    // LINEA DE OCUPACION – etiqueta + barra + fraccion
    // =========================================================
    public static void imprimirBarraOcupacion(String etiqueta, int actual, int total, int ancho) {
        System.out.printf("  %-17s %s  (%d/%d)%n",
                etiqueta + ":", barraProgreso(actual, total, ancho), actual, total);
    }

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
