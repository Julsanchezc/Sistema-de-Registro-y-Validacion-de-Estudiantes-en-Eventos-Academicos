package ui;

import model.Estudiante;

/**
 * Consola.java
 * Utilidades visuales para la terminal: barras de progreso, tablas con bordes.
 * Solo usa ANSI – sin librerias externas ni GUI.
 */
public class Consola {

    // =========================================================
    // BARRA DE PROGRESO – retorna String coloreado
    // Color dinamico: verde < 70 %, amarillo < 90 %, rojo >= 90 %
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

    // =========================================================
    // PROGRESO EN LINEA – sobreescribe la misma linea con \r
    // Llama con actual == total para emitir el salto de linea final
    // =========================================================
    public static void progresoBulkAdd(int actual, int total, int enEvento, int enCola) {
        int    ancho = 28;
        double pct   = total > 0 ? Math.min((double) actual / total, 1.0) : 0;
        int    ll    = (int) (pct * ancho);
        int    va    = ancho - ll;

        String barra = Colores.VERDE_B + rep("█", ll)
                     + Colores.CYAN   + rep("░", va) + Colores.RESET;

        System.out.printf("\r  Procesando [%s] %d/%d  "
                + Colores.VERDE_B  + "evento:%-4d" + Colores.RESET
                + "  " + Colores.MAGENTA + "cola:%-4d"  + Colores.RESET,
                barra, actual, total, enEvento, enCola);
        System.out.flush();
        if (actual >= total) System.out.println();
    }

    // =========================================================
    // TABLA DE ESTUDIANTES CON BORDES
    // Columnas: ID | Nombre | Correo | Programa | Asistencia
    // =========================================================
    private static final int W_ID   = 8;
    private static final int W_NOM  = 26;
    private static final int W_COR  = 30;
    private static final int W_PROG = 21;
    private static final int W_AST  = 9;

    public static void imprimirTablaEstudiantes(Estudiante[] lista) {
        if (lista == null || lista.length == 0) {
            System.out.println(Colores.warn("  (Sin estudiantes registrados)"));
            return;
        }

        String top = "╔" + rep("═", W_ID+2) + "╦" + rep("═", W_NOM+2)
                   + "╦" + rep("═", W_COR+2) + "╦" + rep("═", W_PROG+2)
                   + "╦" + rep("═", W_AST+2) + "╗";
        String sep = "╠ " + rep("═", W_ID+2) + "╬" + rep("═", W_NOM+2)
                   + "╬" + rep("═", W_COR+2) + "╬" + rep("═", W_PROG+2)
                   + "╬" + rep("═", W_AST+2) + "╣";
        String bot = "╚" + rep("═", W_ID+2) + "╩" + rep("═", W_NOM+2)
                   + "╩" + rep("═", W_COR+2) + "╩" + rep("═", W_PROG+2)
                   + "╩" + rep("═", W_AST+2) + "╝";

        System.out.println(Colores.titulo(top));
        System.out.printf(Colores.CYAN + Colores.NEGRITA
                + "║ %-" + W_ID   + "s ║ %-" + W_NOM  + "s ║ %-" + W_COR
                + "s ║ %-" + W_PROG + "s ║ %-" + W_AST  + "s ║" + Colores.RESET + "%n",
                "ID", "Nombre", "Correo", "Programa", "Asistencia");
        System.out.println(Colores.titulo(sep));

        for (Estudiante e : lista) {
            String nom  = truncar(e.getNombre(),   W_NOM);
            String cor  = truncar(e.getCorreo(),   W_COR);
            String prog = truncar(e.getPrograma(), W_PROG);

            System.out.printf("║ %-" + W_ID + "d ║ %-" + W_NOM + "s ║ %-" + W_COR
                            + "s ║ %-" + W_PROG + "s ║ ",
                    e.getId(), nom, cor, prog);

            if (e.isAsistencia()) System.out.print(Colores.VERDE_B + "  ✔  Si  " + Colores.RESET);
            else                  System.out.print(Colores.ROJO_B  + "  ✘  No  " + Colores.RESET);
            System.out.println(" ║");
        }

        System.out.println(Colores.titulo(bot));
        System.out.printf("  " + Colores.CYAN_B + "%d estudiante(s) listados"
                + Colores.RESET + "%n%n", lista.length);
    }

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
