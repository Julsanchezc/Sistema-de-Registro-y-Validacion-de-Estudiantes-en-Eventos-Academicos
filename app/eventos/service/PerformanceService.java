package eventos.service;

import core.AppLogger;
import core.AppPaths;
import eventos.model.Student;
import eventos.structures.BstTree;
import eventos.ui.console.Colors;
import eventos.ui.console.Terminal;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class PerformanceService {

    private static final Logger log = AppLogger.get(PerformanceService.class);

    public static void runBenchmarks(int[] sizes) {
        log.info("Starting performance benchmarks: {} sizes", sizes.length);

        System.out.println(Colors.title(
                "\n╔═══════════════════════════════════════════════════════════════════════════════╗"));
        System.out.println(Colors.title(
                "║          COMPARATIVE PERFORMANCE ANALYSIS – AVL vs BST                      ║"));
        System.out.println(Colors.title(
                "╠════════════╦═══════════════════════════╦═══════════════════════════╦═══════════╣"));
        System.out.println(Colors.title(
                "║          n ║   AVL (ins / find / del)  ║   BST (ins / find / del) ║ H AVL/BST ║"));
        System.out.println(Colors.title(
                "╠════════════╬═══════════════════════════╬═══════════════════════════╬═══════════╣"));

        long[][] table = new long[sizes.length][8];
        for (int i = 0; i < sizes.length; i++) {
            System.out.printf("  Measuring n = %,d ...%n", sizes[i]);
            table[i] = measure(sizes[i]);
            System.out.printf(
                    Colors.CYAN
                    + "║ %,9d ║ "  + Colors.GREEN_B  + "%5d / %4d / %4d ms"
                    + Colors.CYAN + " ║ " + Colors.YELLOW_B + "%5d / %4d / %4d ms"
                    + Colors.CYAN + " ║ " + Colors.CYAN_B + "%5d / %-4d"
                    + Colors.CYAN + " ║" + Colors.RESET + "%n",
                    sizes[i],
                    table[i][0], table[i][1], table[i][2],
                    table[i][4], table[i][5], table[i][6],
                    table[i][3], table[i][7]);
        }

        System.out.println(Colors.title(
                "╚═══════════╩═══════════════════════════╩═══════════════════════════╩═══════════╝"));
        System.out.println();
        System.out.println(Colors.bold("  Theoretical height O(log2 n):"));
        for (int n : sizes)
            System.out.printf("    n = %,10d  ->  log2(n) = %.2f%n", n, Math.log(n) / Math.log(2));
        System.out.println();
        System.out.println(Colors.info("  Data: random order (Fisher-Yates, seed 42)."));
        System.out.println(Colors.info("  AVL guarantees h <= 1.44*log2(n) for ANY insertion order."));
        System.out.println(Colors.info("  BST guarantees O(log n) expected only with random data."));

        showDegenerateCase();

        File dir = AppPaths.Dirs.RESULTS.toFile();
        dir.mkdirs();
        String csvPath = exportCSV(sizes, table, dir);
        if (csvPath != null) callPython(csvPath, dir);
    }

    private static long[] measure(int n) {
        Student[] data = generateRandomData(n);

        EventService avl1 = new EventService("avl_ins",  n + 1);
        EventService avl2 = new EventService("avl_find", n + 1);
        EventService avl3 = new EventService("avl_del",  n + 1);

        long t0 = System.nanoTime();
        for (Student s : data) avl1.registerQuiet(s);
        long avlIns = (System.nanoTime() - t0) / 1_000_000;

        for (Student s : data) { avl2.registerQuiet(s); avl3.registerQuiet(s); }

        long t1 = System.nanoTime();
        for (Student s : data) avl2.hasStudent(s.getId());
        long avlFind = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        for (Student s : data) avl3.removeQuiet(s.getId());
        long avlDel = (System.nanoTime() - t2) / 1_000_000;

        int avlHeight = avl2.getTreeHeight();

        BstTree bst1 = new BstTree(); BstTree bst2 = new BstTree(); BstTree bst3 = new BstTree();

        long t3 = System.nanoTime();
        for (Student s : data) bst1.insert(s);
        long bstIns = (System.nanoTime() - t3) / 1_000_000;

        for (Student s : data) { bst2.insert(s); bst3.insert(s); }

        long t4 = System.nanoTime();
        for (Student s : data) bst2.contains(s.getId());
        long bstFind = (System.nanoTime() - t4) / 1_000_000;

        long t5 = System.nanoTime();
        for (Student s : data) bst3.remove(s.getId());
        long bstDel = (System.nanoTime() - t5) / 1_000_000;

        int bstHeight = bst2.getHeight();

        return new long[]{ avlIns, avlFind, avlDel, avlHeight, bstIns, bstFind, bstDel, bstHeight };
    }

    private static void showDegenerateCase() {
        int n = 2_000;
        System.out.println(Colors.warn("\n─── DEGENERATE CASE: sequential insert 1,2,...," + n + " ───────────"));

        EventService avlSeq = new EventService("avl_seq", n + 1);
        BstTree      bstSeq = new BstTree();

        for (int i = 1; i <= n; i++) {
            Student s = new Student(i, "Stu" + i, "s" + i + "@test.co", "Prog");
            avlSeq.registerQuiet(s);
            bstSeq.insert(s);
        }

        double log2n = Math.log(n) / Math.log(2);
        System.out.printf(Colors.GREEN_B
                + "  AVL height: %-5d  (log2(%d)=%.1f  limit 1.44*log2=%.1f)%n" + Colors.RESET,
                avlSeq.getTreeHeight(), n, log2n, 1.44 * log2n);
        System.out.printf(Colors.RED_B
                + "  BST height: %-5d  (degenerate – equivalent to linked list)%n" + Colors.RESET,
                bstSeq.getHeight());
        System.out.println(Colors.info("  Sequential BST degenerates to O(n)."));
        System.out.println(Colors.info("  AVL maintains O(log n) guaranteed regardless of insertion order."));
        System.out.println(Colors.warn("────────────────────────────────────────────────────────────"));
        System.out.println();
        Terminal.printOccupancyBar("AVL h=" + avlSeq.getTreeHeight(), avlSeq.getTreeHeight(), n, 25);
        Terminal.printOccupancyBar("BST h=" + bstSeq.getHeight(),     bstSeq.getHeight(),     n, 25);
        System.out.println();
    }

    private static String exportCSV(int[] sizes, long[][] table, File dir) {
        File csv = new File(dir, "results.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(csv))) {
            pw.println("n,avl_ins_ms,avl_find_ms,avl_del_ms,avl_height,"
                     + "bst_ins_ms,bst_find_ms,bst_del_ms,bst_height,log2n");
            for (int i = 0; i < sizes.length; i++) {
                double log2n = Math.log(sizes[i]) / Math.log(2);
                pw.printf("%d,%d,%d,%d,%d,%d,%d,%d,%d,%.4f%n",
                        sizes[i],
                        table[i][0], table[i][1], table[i][2], table[i][3],
                        table[i][4], table[i][5], table[i][6], table[i][7],
                        log2n);
            }
            log.info("Results CSV exported: {}", csv.getAbsolutePath());
            System.out.println(Colors.ok("  CSV saved: " + csv.getAbsolutePath()));
            return csv.getAbsolutePath();
        } catch (IOException e) {
            log.error("Error saving CSV: {}", e.getMessage());
            System.out.println(Colors.error("  Error saving CSV: " + e.getMessage()));
            return null;
        }
    }

    private static void callPython(String csvPath, File resultsDir) {
        System.out.println("\n  Generating charts with Python...");
        File script = findScript(resultsDir);
        if (script == null) {
            System.out.println(Colors.warn("  WARNING: graficar_rendimiento.py not found"));
            return;
        }
        String python = findPython();
        if (python == null) {
            System.out.println(Colors.warn("  WARNING: Python not found."));
            System.out.println("  Run manually: python \"" + script.getAbsolutePath() + "\""
                    + " \"" + csvPath + "\" \"" + resultsDir.getAbsolutePath() + "\"");
            return;
        }
        try {
            Process check = new ProcessBuilder(python, "-c", "import matplotlib")
                    .redirectErrorStream(true).start();
            check.waitFor();
            if (check.exitValue() != 0)
                new ProcessBuilder(python, "-m", "pip", "install", "matplotlib", "--quiet")
                        .inheritIO().start().waitFor();
            int exit = new ProcessBuilder(python, script.getAbsolutePath(),
                    csvPath, resultsDir.getAbsolutePath())
                    .inheritIO().start().waitFor();
            if (exit == 0) System.out.println(Colors.ok("\n  Charts saved to: " + resultsDir.getAbsolutePath()));
            else           System.out.println(Colors.error("  Python exited with error (code " + exit + ")."));
        } catch (IOException | InterruptedException e) {
            System.out.println(Colors.error("  Error running Python: " + e.getMessage()));
        }
    }

    private static String findPython() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            String home = System.getProperty("user.home");
            String[] paths = {
                home + "\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
                home + "\\AppData\\Local\\Programs\\Python\\Python312\\python.exe",
                home + "\\AppData\\Local\\Programs\\Python\\Python311\\python.exe",
                home + "\\AppData\\Local\\Programs\\Python\\Python310\\python.exe",
                home + "\\AppData\\Local\\Programs\\Python\\Python39\\python.exe",
                "C:\\Python313\\python.exe", "C:\\Python312\\python.exe",
                home + "\\miniconda3\\python.exe", home + "\\anaconda3\\python.exe",
            };
            for (String p : paths) {
                File f = new File(p);
                if (f.exists() && f.canExecute()) return f.getAbsolutePath();
            }
            File pythonDir = new File(home + "\\AppData\\Local\\Programs\\Python");
            if (pythonDir.isDirectory()) {
                File[] versions = pythonDir.listFiles(File::isDirectory);
                if (versions != null)
                    for (File v : versions) {
                        File exe = new File(v, "python.exe");
                        if (exe.exists() && exe.canExecute()) return exe.getAbsolutePath();
                    }
            }
            if (new File("C:\\Windows\\py.exe").exists()) return "C:\\Windows\\py.exe";
            return null;
        } else {
            for (String cmd : new String[]{ "python3", "python" }) {
                try {
                    Process p = new ProcessBuilder(cmd, "--version").redirectErrorStream(true).start();
                    p.waitFor();
                    if (p.exitValue() == 0) return cmd;
                } catch (IOException | InterruptedException ignored) {}
            }
            return null;
        }
    }

    private static File findScript(File resultsDir) {
        String name = "graficar_rendimiento.py";
        File[] roots = {
            resultsDir.getAbsoluteFile().getParentFile(),
            new File(System.getProperty("user.dir")).getAbsoluteFile()
        };
        for (File root : roots) {
            File dir = root;
            for (int level = 0; level < 6; level++) {
                if (dir == null) break;
                if (new File(dir, name).exists()) return new File(dir, name);
                File[] subs = dir.listFiles(File::isDirectory);
                if (subs != null)
                    for (File sub : subs)
                        if (new File(sub, name).exists()) return new File(sub, name);
                dir = dir.getParentFile();
            }
        }
        return null;
    }

    private static Student[] generateRandomData(int n) {
        Student[] data = new Student[n];
        for (int i = 0; i < n; i++)
            data[i] = new Student(i + 1, "Stu" + i, "s" + i + "@test.co", "Prog");
        Random rnd = new Random(42);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Student tmp = data[i]; data[i] = data[j]; data[j] = tmp;
        }
        return data;
    }
}