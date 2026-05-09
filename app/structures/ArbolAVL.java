package structures;

import model.Estudiante;
import ui.Colores;

/**
 * ArbolAVL.java
 * Arbol Binario de Busqueda Auto-Balanceado (AVL).
 * Clave de ordenamiento: ID del estudiante.
 *
 * ESTRUCTURA DE DATOS: Arbol AVL
 * Complejidad de todas las operaciones: O(log n)
 * Garantia de altura: h <= 1.44 * log2(n) para cualquier orden de entrada.
 */
public class ArbolAVL {

    private NodoAVL raiz;
    private int     cantidadNodos;

    public ArbolAVL() {
        this.raiz          = null;
        this.cantidadNodos = 0;
    }

    // =========================================================
    // ALTURA DE UN NODO (null = 0)
    // =========================================================
    private int altura(NodoAVL n) {
        return (n == null) ? 0 : n.getAltura();
    }

    // =========================================================
    // RECALCULAR ALTURA DE UN NODO
    // =========================================================
    private void ajustarAltura(NodoAVL n) {
        if (n != null)
            n.setAltura(1 + Math.max(altura(n.getIzquierda()), altura(n.getDerecha())));
    }

    // =========================================================
    // ROTACION SIMPLE DERECHA
    // Caso: subarbol izquierdo demasiado alto y su hijo izq. es mas alto
    // =========================================================
    private NodoAVL rotarDerecha(NodoAVL n) {
        NodoAVL m = n.getIzquierda();
        if (m.getDerecha() != null) m.getDerecha().setPadre(n);
        m.setPadre(n.getPadre());
        if      (n.getPadre() == null)                          raiz = m;
        else if (n == n.getPadre().getIzquierda()) n.getPadre().setIzquierda(m);
        else                                        n.getPadre().setDerecha(m);
        n.setIzquierda(m.getDerecha());
        m.setDerecha(n);
        n.setPadre(m);
        ajustarAltura(n);
        ajustarAltura(m);
        return m;
    }

    // =========================================================
    // ROTACION SIMPLE IZQUIERDA
    // Caso: subarbol derecho demasiado alto y su hijo der. es mas alto
    // =========================================================
    private NodoAVL rotarIzquierda(NodoAVL n) {
        NodoAVL m = n.getDerecha();
        if (m.getIzquierda() != null) m.getIzquierda().setPadre(n);
        m.setPadre(n.getPadre());
        if      (n.getPadre() == null)                          raiz = m;
        else if (n == n.getPadre().getIzquierda()) n.getPadre().setIzquierda(m);
        else                                        n.getPadre().setDerecha(m);
        n.setDerecha(m.getIzquierda());
        m.setIzquierda(n);
        n.setPadre(m);
        ajustarAltura(n);
        ajustarAltura(m);
        return m;
    }

    // =========================================================
    // REBALANCEAR HACIA LA DERECHA (subarbol izquierdo muy alto)
    // =========================================================
    private void rebalancearDerecha(NodoAVL n) {
        NodoAVL m = n.getIzquierda();
        if (altura(m.getDerecha()) > altura(m.getIzquierda()))
            rotarIzquierda(m);     // rotacion doble: primero izq. sobre m
        rotarDerecha(n);           // luego derecha sobre n
    }

    // =========================================================
    // REBALANCEAR HACIA LA IZQUIERDA (subarbol derecho muy alto)
    // =========================================================
    private void rebalancearIzquierda(NodoAVL n) {
        NodoAVL m = n.getDerecha();
        if (altura(m.getIzquierda()) > altura(m.getDerecha()))
            rotarDerecha(m);       // rotacion doble: primero der. sobre m
        rotarIzquierda(n);         // luego izquierda sobre n
    }

    // =========================================================
    // REBALANCEAR NODO Y PROPAGAR HACIA LA RAIZ
    // =========================================================
    private void rebalancear(NodoAVL n) {
        if (n == null) return;
        NodoAVL padre = n.getPadre();
        if      (altura(n.getIzquierda()) > altura(n.getDerecha()) + 1)
            rebalancearDerecha(n);          // izquierda muy alta
        else if (altura(n.getDerecha()) > altura(n.getIzquierda()) + 1)
            rebalancearIzquierda(n);        // derecha muy alta
        ajustarAltura(n);
        rebalancear(padre);                 // subir al padre
    }

    // =========================================================
    // INSERCION  O(log n)
    // Iterativa: recorre hasta la posicion correcta, luego rebalancea
    // =========================================================
    public boolean insertar(Estudiante est) {
        if (existe(est.getId())) return false;
        NodoAVL nuevo = new NodoAVL(est);
        if (raiz == null) { raiz = nuevo; cantidadNodos++; return true; }
        NodoAVL cur = raiz, padre = null;
        while (cur != null) {
            padre = cur;
            cur = (est.getId() < cur.getIdEstudiante())
                    ? cur.getIzquierda() : cur.getDerecha();
        }
        nuevo.setPadre(padre);
        if (est.getId() < padre.getIdEstudiante()) padre.setIzquierda(nuevo);
        else                                        padre.setDerecha(nuevo);
        rebalancear(padre);    // restaurar propiedad AVL
        cantidadNodos++;
        return true;
    }

    // =========================================================
    // BUSQUEDA  O(log n)
    // =========================================================
    public Estudiante buscar(int id) {
        NodoAVL n = buscarRec(raiz, id);
        return (n != null) ? n.getEstudiante() : null;
    }

    private NodoAVL buscarRec(NodoAVL nodo, int id) {
        if (nodo == null)            return null;
        int cur = nodo.getIdEstudiante();
        if      (id < cur) return buscarRec(nodo.getIzquierda(), id);
        else if (id > cur) return buscarRec(nodo.getDerecha(),   id);
        else               return nodo;
    }

    public boolean existe(int id) { return buscar(id) != null; }

    // =========================================================
    // ELIMINACION  O(log n)
    // =========================================================
    public boolean eliminar(int id) {
        NodoAVL n = buscarRec(raiz, id);
        if (n == null) return false;
        eliminarNodo(n);
        cantidadNodos--;
        return true;
    }

    private void eliminarNodo(NodoAVL n) {
        if (n.getIzquierda() != null && n.getDerecha() != null) {
            // Dos hijos: copiar sucesor inorden y eliminar sucesor
            NodoAVL sucesor = minimoNodo(n.getDerecha());
            n.setEstudiante(sucesor.getEstudiante());
            eliminarNodo(sucesor);
            return;
        }
        // Cero o un hijo
        NodoAVL hijo  = (n.getIzquierda() != null) ? n.getIzquierda() : n.getDerecha();
        NodoAVL padre = n.getPadre();
        if (hijo  != null) hijo.setPadre(padre);
        if (padre == null) {
            raiz = hijo;
        } else if (n == padre.getIzquierda()) {
            padre.setIzquierda(hijo);
        } else {
            padre.setDerecha(hijo);
        }
        rebalancear(padre);
    }

    private NodoAVL minimoNodo(NodoAVL nodo) {
        while (nodo.getIzquierda() != null) nodo = nodo.getIzquierda();
        return nodo;
    }

    // =========================================================
    // RECORRIDO INORDEN — imprime en orden ascendente por ID
    // =========================================================
    public void recorridoInorden() {
        inordenRec(raiz);
    }

    private void inordenRec(NodoAVL nodo) {
        if (nodo == null) return;
        inordenRec(nodo.getIzquierda());
        System.out.println(nodo.getEstudiante());
        inordenRec(nodo.getDerecha());
    }

    // =========================================================
    // COLECTAR INORDEN — retorna array de estudiantes ordenados
    // =========================================================
    public Estudiante[] coleccionarInorden() {
        Estudiante[] arr = new Estudiante[cantidadNodos];
        int[]        idx = { 0 };
        coleccionarRec(raiz, arr, idx);
        return arr;
    }

    private void coleccionarRec(NodoAVL nodo, Estudiante[] arr, int[] idx) {
        if (nodo == null) return;
        coleccionarRec(nodo.getIzquierda(), arr, idx);
        arr[idx[0]++] = nodo.getEstudiante();
        coleccionarRec(nodo.getDerecha(),   arr, idx);
    }

    // =========================================================
    // VISUALIZACION — imprime el arbol con colores ANSI
    // =========================================================
    public void visualizar() {
        System.out.println(Colores.titulo(
                "\n╔══════ ESTRUCTURA ARBOL AVL ════════════════════════════╗"));
        System.out.printf(
                Colores.CYAN    + "║  Nodos: "  + Colores.CYAN_B    + "%d"
                        + Colores.CYAN  + "  │  Altura: " + Colores.CYAN_B  + "%d"
                        + Colores.CYAN  + "  │  fb "
                        + Colores.VERDE_B    + " 0"  + Colores.CYAN + ":balance  "
                        + Colores.AMARILLO_B + "±1" + Colores.CYAN + ":OK"
                        + Colores.RESET + "%n",
                cantidadNodos, getAltura());
        System.out.println(Colores.titulo(
                "╚════════════════════════════════════════════════════════╝"));
        if (estaVacio()) System.out.println(Colores.warn("  (Arbol vacio)"));
        else             visualizarRec(raiz, "", true);
        System.out.println();
    }

    private void visualizarRec(NodoAVL nodo, String pref, boolean esUltimo) {
        if (nodo == null) return;
        int    fb      = nodo.getFactorBalance();
        String colorFb = (fb == 0)           ? Colores.VERDE_B
                : (Math.abs(fb) == 1) ? Colores.AMARILLO_B
                  :                       Colores.ROJO_B;
        System.out.print(pref);
        System.out.print(Colores.CYAN + (esUltimo ? "└── " : "├── ") + Colores.RESET);
        System.out.println(
                Colores.CYAN  + "ID:" + Colores.CYAN_B + nodo.getIdEstudiante() + Colores.RESET
                        + "  " + Colores.CYAN + "h=" + Colores.RESET + nodo.getAltura()
                        + "  fb=" + colorFb + String.format("%+d", fb) + Colores.RESET
                        + "  " + Colores.AMARILLO + nodo.getEstudiante().getNombre() + Colores.RESET);
        String nuevoPref = pref + (esUltimo ? "    " : "│   ");
        if (nodo.getIzquierda() != null || nodo.getDerecha() != null) {
            visualizarRec(nodo.getIzquierda(), nuevoPref, nodo.getDerecha() == null);
            visualizarRec(nodo.getDerecha(),   nuevoPref, true);
        }
    }

    // =========================================================
    // CONTAR ASISTENCIAS
    // =========================================================
    public int contarAsistencias() {
        return contarAsistenciasRec(raiz);
    }

    private int contarAsistenciasRec(NodoAVL nodo) {
        if (nodo == null) return 0;
        int cuenta = nodo.getEstudiante().isAsistencia() ? 1 : 0;
        return cuenta
                + contarAsistenciasRec(nodo.getIzquierda())
                + contarAsistenciasRec(nodo.getDerecha());
    }

    // =========================================================
    // ESCRIBIR INORDEN A ARCHIVO — exportacion CSV y persistencia
    // =========================================================
    public void escribirInorden(java.io.PrintWriter pw, String prefijo) {
        escribirInordenRec(raiz, pw, prefijo);
    }

    private void escribirInordenRec(NodoAVL n, java.io.PrintWriter pw, String prefijo) {
        if (n == null) return;
        escribirInordenRec(n.getIzquierda(), pw, prefijo);
        Estudiante e = n.getEstudiante();
        if (prefijo.equals("CSV")) {
            pw.printf("%d,%s,%s,%s,%s%n",
                    e.getId(), e.getNombre(), e.getCorreo(), e.getPrograma(), e.isAsistencia());
        } else {
            pw.println("ESTUDIANTE|" + e.getId() + "|" + e.getNombre()
                    + "|" + e.getCorreo() + "|" + e.getPrograma() + "|" + e.isAsistencia());
        }
        escribirInordenRec(n.getDerecha(), pw, prefijo);
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public int     getCantidadEstudiantes() { return cantidadNodos; }
    public int     getAltura()              { return (raiz != null) ? raiz.getAltura() : 0; }
    public boolean estaVacio()              { return raiz == null; }
}