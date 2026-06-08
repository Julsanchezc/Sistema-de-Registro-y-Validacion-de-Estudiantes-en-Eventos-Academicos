package eventos.ui.console;

public class Colors {

    public static final String RESET    = "\033[0m";
    public static final String BOLD     = "\033[1m";
    public static final String RED      = "\033[31m";
    public static final String GREEN    = "\033[32m";
    public static final String YELLOW   = "\033[33m";
    public static final String BLUE     = "\033[34m";
    public static final String MAGENTA  = "\033[35m";
    public static final String CYAN     = "\033[36m";
    public static final String RED_B    = "\033[91m";
    public static final String GREEN_B  = "\033[92m";
    public static final String YELLOW_B = "\033[93m";
    public static final String CYAN_B   = "\033[96m";

    public static String ok(String s)    { return GREEN_B     + s + RESET; }
    public static String error(String s) { return RED_B       + s + RESET; }
    public static String warn(String s)  { return YELLOW      + s + RESET; }
    public static String info(String s)  { return CYAN_B      + s + RESET; }
    public static String title(String s) { return CYAN + BOLD + s + RESET; }
    public static String bold(String s)  { return BOLD        + s + RESET; }
}