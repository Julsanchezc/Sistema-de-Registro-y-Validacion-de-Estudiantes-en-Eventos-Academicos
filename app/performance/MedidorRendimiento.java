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
 */
public class MedidorRendimiento {

    private static final String CARPETA = "results";

    public static void main(String[] args) {
        int[] tamanios = { 10_000, 100_000, 1_000_000 };
        ejecutarPruebas(tamanios);
    }

    public static void ejecutarPruebas(int[] tamanios) {
        System.out.println("Iniciando analisis de rendimiento AVL vs BST...");
        for (int n : tamanios) {
            System.out.printf("  Midiendo n = %,d ...%n", n);
            // TODO: llamar a medir(n) e imprimir resultados
        }
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
