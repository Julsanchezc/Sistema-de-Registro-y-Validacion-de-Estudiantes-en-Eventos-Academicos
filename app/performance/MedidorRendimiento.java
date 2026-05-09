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
 */
public class MedidorRendimiento {

    private static final String CARPETA = "results";

    public static void main(String[] args) {
        int[] tamanios = { 10_000, 100_000, 1_000_000 };
        ejecutarPruebas(tamanios);
    }

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
    }

    // =========================================================
    // MEDICION AVL + BST  (mismos datos aleatorios para ambos)
    // Retorna: [avlIns, avlBus, avlElim, avlAltura,
    //           bstIns, bstBus, bstElim, bstAltura]
    // =========================================================
    private static long[] medir(int n) {
        Estudiante[] datos = generarDatosAleatorios(n);

        // — AVL ————————————————————————————————————————————
        ValidadorEventos avl1 = new ValidadorEventos("avl_ins",  n + 1);
        ValidadorEventos avl2 = new ValidadorEventos("avl_bus",  n + 1);
        ValidadorEventos avl3 = new ValidadorEventos("avl_elim", n + 1);

        long t0 = System.nanoTime();
        for (Estudiante e : datos) avl1.registrarSilencioso(e);
        long avlIns = (System.nanoTime() - t0) / 1_000_000;

        for (Estudiante e : datos) { avl2.registrarSilencioso(e); avl3.registrarSilencioso(e); }

        long t1 = System.nanoTime();
        for (Estudiante e : datos) avl2.verificarEstudiante(e.getId());
        long avlBus = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        for (Estudiante e : datos) avl3.eliminarSilencioso(e.getId());
        long avlElim = (System.nanoTime() - t2) / 1_000_000;

        int altAVL = avl2.obtenerAltura();

        // — BST ————————————————————————————————————————————
        ArbolBST bst1 = new ArbolBST();
        ArbolBST bst2 = new ArbolBST();
        ArbolBST bst3 = new ArbolBST();

        long t3 = System.nanoTime();
        for (Estudiante e : datos) bst1.insertarBST(e);
        long bstIns = (System.nanoTime() - t3) / 1_000_000;

        for (Estudiante e : datos) { bst2.insertarBST(e); bst3.insertarBST(e); }

        long t4 = System.nanoTime();
        for (Estudiante e : datos) bst2.existe(e.getId());
        long bstBus = (System.nanoTime() - t4) / 1_000_000;

        long t5 = System.nanoTime();
        for (Estudiante e : datos) bst3.eliminarBST(e.getId());
        long bstElim = (System.nanoTime() - t5) / 1_000_000;

        int altBST = bst2.getAltura();

        return new long[]{ avlIns, avlBus, avlElim, altAVL, bstIns, bstBus, bstElim, altBST };
    }

    // =========================================================
    // GENERACION DE DATOS ALEATORIOS (Fisher-Yates, semilla 42)
    // =========================================================
    private static Estudiante[] generarDatosAleatorios(int n) {
        Estudiante[] datos = new Estudiante[n];
        for (int i = 0; i < n; i++)
            datos[i] = new Estudiante(i + 1, "Est" + i, "e" + i + "@test.co", "Prog");
        Random rnd = new Random(42);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Estudiante tmp = datos[i]; datos[i] = datos[j]; datos[j] = tmp;
        }
        return datos;
    }
}
