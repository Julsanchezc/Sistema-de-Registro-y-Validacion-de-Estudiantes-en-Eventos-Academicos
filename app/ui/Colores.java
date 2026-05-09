package ui;

/**
 * Colores.java
 * Constantes ANSI y metodos de ayuda para dar color a la salida de consola.
 */
public class Colores {

    public static final String RESET      = "\033[0m";
    public static final String NEGRITA    = "\033[1m";
    public static final String ROJO       = "\033[31m";
    public static final String VERDE      = "\033[32m";
    public static final String AMARILLO   = "\033[33m";
    public static final String AZUL       = "\033[34m";
    public static final String MAGENTA    = "\033[35m";
    public static final String CYAN       = "\033[36m";
    public static final String ROJO_B     = "\033[91m";
    public static final String VERDE_B    = "\033[92m";
    public static final String AMARILLO_B = "\033[93m";
    public static final String CYAN_B     = "\033[96m";

    public static String ok(String s)     { return VERDE_B            + s + RESET; }
    public static String error(String s)  { return ROJO_B             + s + RESET; }
    public static String warn(String s)   { return AMARILLO           + s + RESET; }
    public static String info(String s)   { return CYAN_B             + s + RESET; }
    public static String titulo(String s) { return CYAN + NEGRITA     + s + RESET; }
    public static String bold(String s)   { return NEGRITA            + s + RESET; }
}
