package eventos.service;

import core.AppLogger;
import core.AppPaths;
import eventos.model.Student;
import eventos.structures.BstTree;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Runtime-callable benchmark: AVL vs BST for insert / find / delete.
 * No I/O inside timing blocks; destructive ops use independent pre-populated structures.
 */
public class BenchmarkRunner {

    private static final Logger log = AppLogger.get(BenchmarkRunner.class);

    private static final String SEP    = "-".repeat(66);
    private static final String DOUBLE = "=".repeat(66);

    public static void runBenchmarks(int[] sizes) {
        log.info("Starting benchmarks: {} sizes", sizes.length);

        System.out.println("\n" + DOUBLE);
        System.out.println("  ANALISIS COMPARATIVO DE RENDIMIENTO — AVL vs BST");
        System.out.printf ("  Datos    : orden aleatorio (Fisher-Yates, semilla 42)%n");
        System.out.printf ("  Tamaños  : %,d  /  %,d  /  %,d%n", sizes[0], sizes[1], sizes[2]);
        System.out.println("  Eliminar : árbol FRESCO independiente por cada n.");
        System.out.println(DOUBLE);

        System.out.println();
        System.out.printf("  %-12s  %28s  %28s  %9s%n",
                "n", "AVL (ins / búsq / elim ms)", "BST (ins / búsq / elim ms)", "H AVL/BST");
        System.out.println("  " + SEP);

        long[][] table = new long[sizes.length][8];
        for (int i = 0; i < sizes.length; i++) {
            table[i] = measure(sizes[i]);
            System.out.printf("  %,12d  %8d / %6d / %6d ms  %8d / %6d / %6d ms  %5d / %-4d%n",
                    sizes[i],
                    table[i][0], table[i][1], table[i][2],
                    table[i][4], table[i][5], table[i][6],
                    table[i][3], table[i][7]);
        }

        System.out.println("  " + SEP);
        System.out.println();
        System.out.println("  Altura teórica O(log2 n):");
        for (int n : sizes)
            System.out.printf("    n = %,10d  ->  log2(n) = %.2f%n", n, Math.log(n) / Math.log(2));

        System.out.println();
        System.out.println("  Datos en orden aleatorio (Fisher-Yates, semilla 42).");
        System.out.println("  AVL garantiza h <= 1.44*log2(n) en CUALQUIER orden de inserción.");
        System.out.println("  BST garantiza O(log n) esperado solo con datos aleatorios.");

        showDegenerateCase();

        File dir = AppPaths.Dirs.RESULTS.toFile();
        dir.mkdirs();
        String csvPath = exportCSV(sizes, table, dir);
        if (csvPath != null) callPython(csvPath, dir);
    }

    /** Each operation is timed on its own FRESH structure — no cascading state. */
    private static long[] measure(int n) {
        Student[] data = generateRandomData(n);

        // AVL: three independent services (ins, find, del)
        EventService avl1 = new EventService("avl_ins",  n + 1);
        EventService avl2 = new EventService("avl_find", n + 1);
        EventService avl3 = new EventService("avl_del",  n + 1);

        long t0 = System.nanoTime();
        for (Student s : data) avl1.registerQuiet(s);
        long avlIns = ms(t0);

        for (Student s : data) { avl2.registerQuiet(s); avl3.registerQuiet(s); }

        long t1 = System.nanoTime();
        for (Student s : data) avl2.hasStudent(s.getId());
        long avlFind = ms(t1);

        // del: tree pre-populated OUTSIDE timed block → always starts full
        long t2 = System.nanoTime();
        for (Student s : data) avl3.removeQuiet(s.getId());
        long avlDel = ms(t2);

        int avlHeight = avl2.getTreeHeight();

        // BST: same pattern
        BstTree bst1 = new BstTree(); BstTree bst2 = new BstTree(); BstTree bst3 = new BstTree();

        long t3 = System.nanoTime();
        for (Student s : data) bst1.insert(s);
        long bstIns = ms(t3);

        for (Student s : data) { bst2.insert(s); bst3.insert(s); }

        long t4 = System.nanoTime();
        for (Student s : data) bst2.contains(s.getId());
        long bstFind = ms(t4);

        long t5 = System.nanoTime();
        for (Student s : data) bst3.remove(s.getId());
        long bstDel = ms(t5);

        int bstHeight = bst2.getHeight();

        return new long[]{ avlIns, avlFind, avlDel, avlHeight, bstIns, bstFind, bstDel, bstHeight };
    }

    private static void showDegenerateCase() {
        int n = 2_000;
        System.out.println("\n--- CASO DEGENERADO: inserción secuencial 1, 2, ..., " + n + " ---");

        EventService avlSeq = new EventService("avl_seq", n + 1);
        BstTree      bstSeq = new BstTree();

        for (int i = 1; i <= n; i++) {
            Student s = new Student(i, "Stu" + i, "s" + i + "@test.co", "Prog");
            avlSeq.registerQuiet(s);
            bstSeq.insert(s);
        }

        double log2n = Math.log(n) / Math.log(2);
        System.out.printf("  AVL altura: %-5d  (log2(%d)=%.1f  límite 1.44*log2=%.1f)%n",
                avlSeq.getTreeHeight(), n, log2n, 1.44 * log2n);
        System.out.printf("  BST altura: %-5d  (degenerado — equivalente a lista enlazada)%n",
                bstSeq.getHeight());
        System.out.println("  Inserción secuencial en BST degenera a O(n).");
        System.out.println("  AVL mantiene O(log n) garantizado sin importar el orden.");
        System.out.println("-------------------------------------------------------------------");
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
            log.info("CSV exportado: {}", csv.getAbsolutePath());
            System.out.println("  CSV guardado: " + csv.getAbsolutePath());
            return csv.getAbsolutePath();
        } catch (IOException e) {
            log.error("Error guardando CSV: {}", e.getMessage());
            System.out.println("  Error guardando CSV: " + e.getMessage());
            return null;
        }
    }

    private static void callPython(String csvPath, File resultsDir) {
        System.out.println("\n  Generando gráficas con Python...");
        File script = findScript(resultsDir);
        if (script == null) {
            System.out.println("  AVISO: graficar_rendimiento.py no encontrado");
            return;
        }
        String python = findPython();
        if (python == null) {
            System.out.println("  AVISO: Python no encontrado.");
            System.out.println("  Ejecuta manualmente: python \"" + script.getAbsolutePath()
                    + "\" \"" + csvPath + "\" \"" + resultsDir.getAbsolutePath() + "\"");
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
            if (exit == 0) System.out.println("  Gráficas guardadas en: " + resultsDir.getAbsolutePath());
            else           System.out.println("  Python terminó con error (código " + exit + ").");
        } catch (IOException | InterruptedException e) {
            System.out.println("  Error ejecutando Python: " + e.getMessage());
        }
    }

    /** Fisher-Yates shuffle with fixed seed=42. IDs are 1..n. */
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

    private static long ms(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
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
}
