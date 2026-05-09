package performance;

import structures.ArbolBST;
import model.Estudiante;
import system.ValidadorEventos;
import ui.Colores;
import ui.Consola;

import java.io.*;
import java.util.*;

/**
 * MedidorRendimiento.java
 * Pruebas de rendimiento comparativo: Arbol AVL vs Arbol BST.
 * Tamanios: n = 10^4, 10^5, 10^6 elementos.
 *
 * Metodologia:
 *   - Datos aleatorios (Fisher-Yates, semilla fija 42) para garantizar
 *     reproducibilidad y evitar el caso degenerado del BST.
 *   - Cronometro de alta precision (System.nanoTime).
 *   - Se miden insercion, busqueda y eliminacion por separado.
 *   - Altura real del arbol comparada con log2(n) teorico.
 *
 * Exporta resultados/results.csv e invoca automaticamente el script
 * Python para generar graficas comparativas.
 */
public class MedidorRendimiento {

    // Subcarpeta relativa donde se guardan results.csv y las graficas PNG.
    private static final String CARPETA = "results";

    // Punto de entrada. Ejecuta las pruebas con los tres tamanios canonicos
    // (10 000, 100 000 y 1 000 000) elegidos para ilustrar la escala logaritmica.
    public static void main(String[] args) {
        int[] tamanios = { 10_000, 100_000, 1_000_000 };
        ejecutarPruebas(tamanios);
    }

    // Orquesta el flujo completo: imprime la cabecera, mide cada n, muestra la
    // altura real vs. log2(n), ejecuta el caso degenerado, exporta el CSV
    // e invoca el script Python para generar las graficas comparativas.
    public static void ejecutarPruebas(int[] tamanios) {
        System.out.println(Colores.titulo(
                "\n╔═══════════════════════════════════════════════════════════════════════════════╗"));
        System.out.println(Colores.titulo(
                "║          ANALISIS DE RENDIMIENTO COMPARATIVO – AVL vs BST                   ║"));
        System.out.println(Colores.titulo(
                "╠════════════╦═══════════════════════════╦═══════════════════════════╦═══════════╣"));
        System.out.println(Colores.titulo(
                "║          n ║   AVL (ins / bus / elm)   ║   BST (ins / bus / elm)  ║ Alt AVL/BST║"));
        System.out.println(Colores.titulo(
                "╠════════════╬═══════════════════════════╬═══════════════════════════╬═══════════╣"));

        long[][] tabla = new long[tamanios.length][8];
        for (int i = 0; i < tamanios.length; i++) {
            System.out.printf("  Midiendo n = %,d ...%n", tamanios[i]);
            tabla[i] = medir(tamanios[i]);
            System.out.printf(
                    Colores.CYAN
                    + "║ %,9d ║ "  + Colores.VERDE_B  + "%5d / %4d / %4d ms"
                    + Colores.CYAN + " ║ " + Colores.AMARILLO_B + "%5d / %4d / %4d ms"
                    + Colores.CYAN + " ║ " + Colores.CYAN_B + "%5d / %-4d"
                    + Colores.CYAN + " ║" + Colores.RESET + "%n",
                    tamanios[i],
                    tabla[i][0], tabla[i][1], tabla[i][2],
                    tabla[i][4], tabla[i][5], tabla[i][6],
                    tabla[i][3], tabla[i][7]);
        }

        System.out.println(Colores.titulo(
                "╚═══════════╩═══════════════════════════╩═══════════════════════════╩═══════════╝"));
        System.out.println();
        System.out.println(Colores.bold("  Altura teorica O(log2 n):"));
        for (int n : tamanios)
            System.out.printf("    n = %,10d  ->  log2(n) = %.2f%n",
                    n, Math.log(n) / Math.log(2));
        System.out.println();
        System.out.println(Colores.info(
                "  Datos: orden aleatorio (Fisher-Yates, semilla 42)."));
        System.out.println(Colores.info(
                "  AVL garantiza h <= 1.44*log2(n) para CUALQUIER orden de entrada."));
        System.out.println(Colores.info(
                "  BST garantiza O(log n) esperado solo con datos aleatorios."));

        demostracionCasoDegenerado();

        File   carpeta = crearCarpeta();
        String csvPath = exportarCSV(tamanios, tabla, carpeta);
        if (csvPath != null) llamarPython(csvPath, carpeta);
    }

    // =========================================================
    // MEDICION AVL + BST  (mismos datos aleatorios para ambos)
    // Retorna: [avlIns, avlBus, avlElim, avlAltura,
    //           bstIns, bstBus, bstElim, bstAltura]
    // =========================================================

    // Mide insercion, busqueda y eliminacion para AVL y BST con los mismos datos.
    // Se crean tres arboles separados por operacion (avl1/bst1=insercion,
    // avl2/bst2=busqueda, avl3/bst3=eliminacion) para que el estado residual
    // de una operacion no afecte la siguiente.
    // Retorna un arreglo de 8 posiciones:
    //   [0]=avlIns_ms  [1]=avlBus_ms  [2]=avlElim_ms  [3]=altAVL
    //   [4]=bstIns_ms  [5]=bstBus_ms  [6]=bstElim_ms  [7]=altBST
    private static long[] medir(int n) {
        Estudiante[] datos = generarDatosAleatorios(n);

        // Tres instancias AVL independientes para que cada operacion
        // se mida sobre un arbol limpio sin interferencia de estado previo.
        ValidadorEventos avl1 = new ValidadorEventos("avl_ins",  n + 1);
        ValidadorEventos avl2 = new ValidadorEventos("avl_bus",  n + 1);
        ValidadorEventos avl3 = new ValidadorEventos("avl_elim", n + 1);

        // -- AVL: insercion --------------------------------------------------
        long t0 = System.nanoTime();
        for (Estudiante e : datos) avl1.registrarSilencioso(e);
        long avlIns = (System.nanoTime() - t0) / 1_000_000; // nanoTime -> ms

        // Prellenar avl2 y avl3 antes de medir busqueda y eliminacion
        for (Estudiante e : datos) { avl2.registrarSilencioso(e); avl3.registrarSilencioso(e); }

        // -- AVL: busqueda --------------------------------------------------
        long t1 = System.nanoTime();
        for (Estudiante e : datos) avl2.verificarEstudiante(e.getId());
        long avlBus = (System.nanoTime() - t1) / 1_000_000;

        // -- AVL: eliminacion -----------------------------------------------
        long t2 = System.nanoTime();
        for (Estudiante e : datos) avl3.eliminarSilencioso(e.getId());
        long avlElim = (System.nanoTime() - t2) / 1_000_000;

        // Altura real del AVL tras insertar n elementos (avl2 no ha sido modificado desde la carga)
        int altAVL = avl2.obtenerAltura();

        // Tres instancias BST independientes, por la misma razon que los AVL.
        ArbolBST bst1 = new ArbolBST();
        ArbolBST bst2 = new ArbolBST();
        ArbolBST bst3 = new ArbolBST();

        // -- BST: insercion --------------------------------------------------
        long t3 = System.nanoTime();
        for (Estudiante e : datos) bst1.insertarBST(e);
        long bstIns = (System.nanoTime() - t3) / 1_000_000;

        // Prellenar bst2 y bst3 con los mismos datos para busqueda y eliminacion
        for (Estudiante e : datos) { bst2.insertarBST(e); bst3.insertarBST(e); }

        // -- BST: busqueda --------------------------------------------------
        long t4 = System.nanoTime();
        for (Estudiante e : datos) bst2.existe(e.getId());
        long bstBus = (System.nanoTime() - t4) / 1_000_000;

        // -- BST: eliminacion -----------------------------------------------
        long t5 = System.nanoTime();
        for (Estudiante e : datos) bst3.eliminarBST(e.getId());
        long bstElim = (System.nanoTime() - t5) / 1_000_000;

        int altBST = bst2.getAltura();

        return new long[]{ avlIns, avlBus, avlElim, altAVL, bstIns, bstBus, bstElim, altBST };
    }

    // =========================================================
    // DEMOSTRACION CASO DEGENERADO (n=2000, insercion secuencial)
    // =========================================================

    // Ilustra el peor caso del BST: insertar IDs en orden secuencial 1,2,...,n
    // hace que cada nodo quede como hijo derecho del anterior, convirtiendo el
    // arbol en una lista enlazada de altura n con O(n) por operacion.
    // El AVL evita esto con rotaciones automaticas: su altura nunca supera
    // 1.44 * log2(n) sin importar el orden de insercion (teorema AVL, 1962).
    private static void demostracionCasoDegenerado() {
        int n = 2_000;
        System.out.println(Colores.warn(
                "\n─── CASO DEGENERADO: insercion secuencial 1,2,...," + n + " ───────────"));

        ValidadorEventos avlSeq = new ValidadorEventos("avl_seq", n + 1);
        ArbolBST         bstSeq = new ArbolBST();

        for (int i = 1; i <= n; i++) {
            Estudiante e = new Estudiante(i, "Est" + i, "e" + i + "@test.co", "Prog");
            avlSeq.registrarSilencioso(e);
            bstSeq.insertarBST(e);
        }

        double log2n = Math.log(n) / Math.log(2);
        System.out.printf(Colores.VERDE_B
                + "  AVL altura : %-5d  (log2(%d)=%.1f  limite 1.44*log2=%.1f)%n" + Colores.RESET,
                avlSeq.obtenerAltura(), n, log2n, 1.44 * log2n);
        System.out.printf(Colores.ROJO_B
                + "  BST altura : %-5d  (degenerado – equivale a lista enlazada)%n" + Colores.RESET,
                bstSeq.getAltura());
        System.out.println(Colores.info(
                "  Con insercion secuencial el BST degenera a O(n)."));
        System.out.println(Colores.info(
                "  El AVL mantiene O(log n) garantizado sin importar el orden."));
        System.out.println(Colores.warn(
                "────────────────────────────────────────────────────────────"));

        System.out.println();
        Consola.imprimirBarraOcupacion("AVL h=" + avlSeq.obtenerAltura(),
                avlSeq.obtenerAltura(), n, 25);
        Consola.imprimirBarraOcupacion("BST h=" + bstSeq.getAltura(),
                bstSeq.getAltura(), n, 25);
        System.out.println();
    }

    // =========================================================
    // CARPETA Y CSV
    // =========================================================

    // Crea la carpeta "results" en el directorio de trabajo si aun no existe.
    private static File crearCarpeta() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) carpeta.mkdirs();
        System.out.println("  Carpeta resultados: " + carpeta.getAbsolutePath());
        return carpeta;
    }

    // Escribe los resultados en results/results.csv.
    // Incluye una columna log2n pre-calculada para que el script Python
    // pueda graficar directamente sin calculos adicionales.
    // Retorna la ruta absoluta del CSV, o null si hubo un error de escritura.
    private static String exportarCSV(int[] tamanios, long[][] tabla, File carpeta) {
        File csv = new File(carpeta, "results.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(csv))) {
            pw.println("n,avl_ins_ms,avl_bus_ms,avl_elim_ms,avl_altura,"
                     + "bst_ins_ms,bst_bus_ms,bst_elim_ms,bst_altura,log2n");
            for (int i = 0; i < tamanios.length; i++) {
                double log2n = Math.log(tamanios[i]) / Math.log(2);
                pw.printf("%d,%d,%d,%d,%d,%d,%d,%d,%d,%.4f%n",
                        tamanios[i],
                        tabla[i][0], tabla[i][1], tabla[i][2], tabla[i][3],
                        tabla[i][4], tabla[i][5], tabla[i][6], tabla[i][7],
                        log2n);
            }
            System.out.println(Colores.ok("  CSV guardado: " + csv.getAbsolutePath()));
            return csv.getAbsolutePath();
        } catch (IOException e) {
            System.out.println(Colores.error("  Error al guardar CSV: " + e.getMessage()));
            return null;
        }
    }

    // =========================================================
    // LLAMAR PYTHON
    // =========================================================

    // Invoca graficar_rendimiento.py como subproceso para generar los PNG.
    // Flujo: buscar el script -> localizar Python -> verificar matplotlib
    //        -> ejecutar con el CSV como argumento.
    // Si algun paso falla, imprime el comando manual en lugar de lanzar
    // una excepcion para no cortar la ejecucion del benchmark.
    private static void llamarPython(String csvAbsPath, File carpetaResultados) {
        System.out.println("\n  Generando graficas con Python...");
        File script = buscarScript(carpetaResultados);
        if (script == null) {
            System.out.println(Colores.warn(
                    "  AVISO: No se encontro graficar_rendimiento.py"));
            System.out.println("  Coloca el script en la raiz del proyecto o en scripts/");
            return;
        }
        System.out.println("  Script: " + script.getAbsolutePath());

        String python = buscarEjecutablePython();
        if (python == null) {
            System.out.println(Colores.warn("  AVISO: No se encontro Python instalado."));
            imprimirComandoManual(script, csvAbsPath, carpetaResultados);
            return;
        }
        System.out.println("  Python: " + python);

        try {
            Process check = new ProcessBuilder(python, "-c", "import matplotlib")
                    .redirectErrorStream(true).start();
            check.waitFor();
            if (check.exitValue() != 0) {
                System.out.println("  Instalando matplotlib...");
                new ProcessBuilder(python, "-m", "pip", "install", "matplotlib", "--quiet")
                        .inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("  Advertencia matplotlib: " + e.getMessage());
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    python, script.getAbsolutePath(),
                    csvAbsPath, carpetaResultados.getAbsolutePath());
            pb.inheritIO();
            int exit = pb.start().waitFor();
            if (exit == 0) {
                System.out.println(Colores.ok(
                        "\n  Graficas generadas en: " + carpetaResultados.getAbsolutePath()));
                System.out.println("  grafica_tiempos.png  |  grafica_comparativa.png"
                                 + "  |  grafica_altura.png  |  grafica_tabla.png");
            } else {
                System.out.println(Colores.error(
                        "  Python termino con error (codigo " + exit + ")."));
                imprimirComandoManual(script, csvAbsPath, carpetaResultados);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(Colores.error("  Error al ejecutar Python: " + e.getMessage()));
            imprimirComandoManual(script, csvAbsPath, carpetaResultados);
        }
    }

    // =========================================================
    // BUSCAR EJECUTABLE PYTHON (Windows + Unix)
    // =========================================================

    // Localiza el ejecutable de Python segun el sistema operativo.
    // En Windows prueba rutas estaticas conocidas (python.org + Conda),
    // luego escanea dinaminamente %LOCALAPPDATA%\Programs\Python para
    // detectar versiones recien instaladas, y como ultimo recurso usa
    // el lanzador py.exe del instalador oficial.
    // En Unix/macOS prueba python3 y python en el PATH del proceso.
    // Retorna la ruta del ejecutable encontrado, o null si no hay Python.
    private static String buscarEjecutablePython() {
        boolean esWindows = System.getProperty("os.name").toLowerCase().contains("win");

        if (esWindows) {
            String userHome = System.getProperty("user.home");

            // Rutas estaticas: versiones estandar de python.org y entornos Conda.
            // Ordenadas de mayor a menor version para preferir la mas reciente.
            String[] rutasWin = {
                userHome + "\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
                userHome + "\\AppData\\Local\\Programs\\Python\\Python312\\python.exe",
                userHome + "\\AppData\\Local\\Programs\\Python\\Python311\\python.exe",
                userHome + "\\AppData\\Local\\Programs\\Python\\Python310\\python.exe",
                userHome + "\\AppData\\Local\\Programs\\Python\\Python39\\python.exe",
                "C:\\Python313\\python.exe", "C:\\Python312\\python.exe",
                "C:\\Python311\\python.exe", "C:\\Python310\\python.exe",
                userHome + "\\miniconda3\\python.exe",
                userHome + "\\anaconda3\\python.exe",
                userHome + "\\AppData\\Local\\miniconda3\\python.exe",
                userHome + "\\AppData\\Local\\anaconda3\\python.exe",
                "C:\\ProgramData\\miniconda3\\python.exe",
                "C:\\ProgramData\\anaconda3\\python.exe",
            };
            for (String ruta : rutasWin) {
                File f = new File(ruta);
                if (f.exists() && f.canExecute()) return f.getAbsolutePath();
            }
            // Escaneo dinamico: cubre versiones no listadas arriba (ej. Python 3.14+).
            // Lista los subdirectorios de la carpeta de instalaciones y prueba cada uno.
            File pythonDir = new File(
                    userHome + "\\AppData\\Local\\Programs\\Python");
            if (pythonDir.isDirectory()) {
                File[] versiones = pythonDir.listFiles(File::isDirectory);
                if (versiones != null) {
                    for (File v : versiones) {
                        File exe = new File(v, "python.exe");
                        if (exe.exists() && exe.canExecute()) return exe.getAbsolutePath();
                    }
                }
            }
            // Ultimo recurso: lanzador py.exe del instalador oficial de Windows.
            // Funciona incluso si python.exe no esta en el PATH del usuario.
            if (new File("C:\\Windows\\py.exe").exists())
                return "C:\\Windows\\py.exe";
            if (new File("C:\\Windows\\System32\\py.exe").exists())
                return "C:\\Windows\\System32\\py.exe";
            return null;
        } else {
            // Se prefiere python3 sobre python para evitar invocar Python 2
            // en sistemas que aun lo tengan instalado.
            for (String cmd : new String[]{ "python3", "python" }) {
                try {
                    Process p = new ProcessBuilder(cmd, "--version")
                            .redirectErrorStream(true).start();
                    p.waitFor();
                    if (p.exitValue() == 0) return cmd;
                } catch (IOException | InterruptedException ignored) {}
            }
            return null;
        }
    }

    // =========================================================
    // BUSCAR SCRIPT PYTHON
    // =========================================================

    // Busca graficar_rendimiento.py subiendo hasta 6 niveles desde la carpeta
    // de resultados y desde el directorio de trabajo. En cada nivel revisa
    // tambien los subdirectorios directos (scripts/, tools/, etc.).
    // Retorna el File del script encontrado, o null si no existe.
    private static File buscarScript(File carpetaResultados) {
        String nombre = "graficar_rendimiento.py";
        File[] inicios = {
            carpetaResultados.getAbsoluteFile().getParentFile(),
            new File(System.getProperty("user.dir")).getAbsoluteFile()
        };
        for (File inicio : inicios) {
            File dir = inicio;
            for (int nivel = 0; nivel < 6; nivel++) {
                if (dir == null) break;
                if (new File(dir, nombre).exists()) return new File(dir, nombre);
                File[] subs = dir.listFiles(File::isDirectory);
                if (subs != null) {
                    for (File sub : subs)
                        if (new File(sub, nombre).exists()) return new File(sub, nombre);
                }
                dir = dir.getParentFile();
            }
        }
        return null;
    }

    // Muestra en consola el comando equivalente para que el usuario pueda
    // ejecutar el script Python manualmente si la invocacion automatica fallo.
    private static void imprimirComandoManual(File script, String csvPath, File carpeta) {
        System.out.println("  Ejecuta manualmente:");
        System.out.println("  python \"" + script.getAbsolutePath() + "\""
                + " \"" + csvPath + "\" \"" + carpeta.getAbsolutePath() + "\"");
    }

    // =========================================================
    // GENERACION DE DATOS ALEATORIOS (Fisher-Yates, semilla 42)
    // =========================================================

    // Genera n estudiantes con IDs 1..n y los mezcla con el algoritmo Fisher-Yates.
    // La semilla fija (42) garantiza reproducibilidad entre ejecuciones: siempre
    // el mismo orden aleatorio, lo que hace los resultados comparables en el tiempo.
    // Se usa la misma secuencia para AVL y BST, asegurando una medicion justa.
    private static Estudiante[] generarDatosAleatorios(int n) {
        Estudiante[] datos = new Estudiante[n];
        for (int i = 0; i < n; i++)
            datos[i] = new Estudiante(i + 1, "Est" + i, "e" + i + "@test.co", "Prog");
        Random rnd = new Random(42);
        // Recorre el arreglo de atras hacia adelante intercambiando cada elemento
        // con uno elegido al azar entre los que quedan sin procesar.
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Estudiante tmp = datos[i]; datos[i] = datos[j]; datos[j] = tmp;
        }
        return datos;
    }
}
