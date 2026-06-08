package eventos.ui.console;

import eventos.model.Student;
import eventos.structures.HistoryStack;

public class Terminal {

    private static final int W_ID    = 8;
    private static final int W_NAME  = 26;
    private static final int W_EMAIL = 30;
    private static final int W_PROG  = 21;
    private static final int W_ATT   = 9;

    public static String progressBar(int current, int total, int width) {
        if (total <= 0)
            return Colors.CYAN + "[" + rep("░", width) + "]  0.0%" + Colors.RESET;

        double pct    = Math.min((double) current / total, 1.0);
        int    filled = (int) (pct * width);
        int    empty  = width - filled;

        String barColor;
        if      (pct >= 0.90) barColor = Colors.RED_B;
        else if (pct >= 0.70) barColor = Colors.YELLOW_B;
        else                  barColor = Colors.GREEN_B;

        return Colors.CYAN + "[" + Colors.RESET
                + barColor + rep("█", filled) + Colors.RESET
                + Colors.CYAN + rep("░", empty) + Colors.RESET
                + Colors.CYAN + "]" + Colors.RESET
                + " " + barColor + String.format("%5.1f%%", pct * 100) + Colors.RESET;
    }

    public static void printOccupancyBar(String label, int current, int total, int width) {
        System.out.printf("  %-17s %s  (%d/%d)%n",
                label + ":", progressBar(current, total, width), current, total);
    }

    public static void bulkAddProgress(int current, int total, int inEvent, int inQueue) {
        int    width  = 28;
        double pct    = total > 0 ? Math.min((double) current / total, 1.0) : 0;
        int    filled = (int) (pct * width);
        int    empty  = width - filled;
        String bar    = Colors.GREEN_B + rep("█", filled) + Colors.CYAN + rep("░", empty) + Colors.RESET;
        System.out.printf("\r  Processing [%s] %d/%d  "
                + Colors.GREEN_B + "event:%-4d" + Colors.RESET
                + "  " + Colors.MAGENTA + "queue:%-4d" + Colors.RESET,
                bar, current, total, inEvent, inQueue);
        System.out.flush();
        if (current >= total) System.out.println();
    }

    public static void printStudentTable(Student[] list) {
        if (list == null || list.length == 0) {
            System.out.println(Colors.warn("  (No students registered)"));
            return;
        }
        String top = "╔" + rep("═", W_ID+2) + "╦" + rep("═", W_NAME+2)
                   + "╦" + rep("═", W_EMAIL+2) + "╦" + rep("═", W_PROG+2)
                   + "╦" + rep("═", W_ATT+2) + "╗";
        String sep = "╠ " + rep("═", W_ID+2) + "╬" + rep("═", W_NAME+2)
                   + "╬" + rep("═", W_EMAIL+2) + "╬" + rep("═", W_PROG+2)
                   + "╬" + rep("═", W_ATT+2) + "╣";
        String bot = "╚" + rep("═", W_ID+2) + "╩" + rep("═", W_NAME+2)
                   + "╩" + rep("═", W_EMAIL+2) + "╩" + rep("═", W_PROG+2)
                   + "╩" + rep("═", W_ATT+2) + "╝";

        System.out.println(Colors.title(top));
        System.out.printf(Colors.CYAN + Colors.BOLD
                + "║ %-" + W_ID + "s ║ %-" + W_NAME + "s ║ %-" + W_EMAIL
                + "s ║ %-" + W_PROG + "s ║ %-" + W_ATT + "s ║" + Colors.RESET + "%n",
                "ID", "Name", "Email", "Program", "Attended");
        System.out.println(Colors.title(sep));

        for (Student s : list) {
            String name  = truncate(s.getName(),    W_NAME);
            String email = truncate(s.getEmail(),   W_EMAIL);
            String prog  = truncate(s.getProgram(), W_PROG);
            System.out.printf("║ %-" + W_ID + "d ║ %-" + W_NAME + "s ║ %-" + W_EMAIL
                            + "s ║ %-" + W_PROG + "s ║ ",
                    s.getId(), name, email, prog);
            if (s.isAttended()) System.out.print(Colors.GREEN_B + "  ✔  Yes " + Colors.RESET);
            else                System.out.print(Colors.RED_B   + "  ✘  No  " + Colors.RESET);
            System.out.println(" ║");
        }
        System.out.println(Colors.title(bot));
        System.out.printf("  " + Colors.CYAN_B + "%d student(s) listed" + Colors.RESET + "%n%n", list.length);
    }

    public static void printHistory(HistoryStack.Entry[] entries) {
        if (entries == null || entries.length == 0) {
            System.out.println("  (History empty)");
            return;
        }
        for (int i = 0; i < entries.length; i++) {
            String type     = String.format("%-12s", entries[i].type);
            String undoMark = (entries[i].undoData != null) ? " [*]" : "";
            System.out.printf("  %2d. [%s] %s%s%n", i + 1, type, entries[i].description, undoMark);
        }
        System.out.println("  [*] = removal that can be undone");
    }

    private static String rep(String s, int n) {
        StringBuilder sb = new StringBuilder(n * s.length());
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}