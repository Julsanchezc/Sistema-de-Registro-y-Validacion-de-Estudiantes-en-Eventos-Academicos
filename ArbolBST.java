package structures;

import model.Estudiante;

/**
 * ArbolBST.java
 * Arbol Binario de Busqueda (BST) SIN auto-balanceo.
 * ESTRUCTURA DE DATOS: Arbol BST
 * Uso en el sistema: comparativa de rendimiento frente al Arbol AVL.
 *
 * A diferencia del AVL:
 *   - No realiza rotaciones, por lo que es mas rapido en inserciones aisladas.
 *   - Con datos en orden secuencial degenera a una lista enlazada (altura = n).
 *   - Con datos aleatorios la altura esperada es ~2.5 * log2(n).
 *   - El AVL garantiza h <= 1.44 * log2(n) para CUALQUIER orden de entrada.
 */
public class ArbolBST {

    // =========================================================
    // CLASE INTERNA: NODO
    // =========================================================
    private class Nodo {
        Nodo       izq;
        Estudiante est;
        Nodo       der;

        Nodo()             { this(null); }
        Nodo(Estudiante e) { izq = null; est = e; der = null; }
    }

    private Nodo raiz;
    private int  cantidad;


    // =========================================================
    // INSERCION (driver publico)
    // =========================================================
    public boolean insertarBST(Estudiante e) {
        if (existe(e.getId())) return false;
        raiz = insertar(e, raiz);
        cantidad++;
        return true;
    }

    // =========================================================
    // INSERCION (recursivo privado)
    // =========================================================
    private Nodo insertar(Estudiante e, Nodo p) {
        if (p == null)
            p = new Nodo(e);
        else if (e.getId() < p.est.getId())
            p.izq = insertar(e, p.izq);       // ir a la izquierda
        else if (e.getId() > p.est.getId())
            p.der = insertar(e, p.der);       // ir a la derecha
        else
            System.out.println("Elemento ya existe, no insertado.");
        return p;
    }

    // =========================================================
    // BUSQUEDA  O(h)
    // =========================================================
    public Estudiante buscar(int id) {
        Nodo n = buscarRec(raiz, id);
        return (n != null) ? n.est : null;
    }

    private Nodo buscarRec(Nodo p, int id) {
        if (p == null)          return null;
        if (id < p.est.getId()) return buscarRec(p.izq, id);
        if (id > p.est.getId()) return buscarRec(p.der, id);
        return p;
    }

    public boolean existe(int id) { return buscar(id) != null; }

    // =========================================================
    // ELIMINACION (driver publico)
    // =========================================================
    public boolean eliminarBST(int id) {
        if (!existe(id)) return false;
        raiz = eliminar(id, raiz);
        cantidad--;
        return true;
    }

    // =========================================================
    // ELIMINACION (recursivo privado)
    // Casos: hoja, 1 hijo, 2 hijos (reemplaza con minimo del subarbol derecho)
    // =========================================================
    private Nodo eliminar(int id, Nodo p) {
        if (p != null) {
            if      (id < p.est.getId())
                p.izq = eliminar(id, p.izq);
            else if (id > p.est.getId())
                p.der = eliminar(id, p.der);
            else {
                if      (p.izq == null && p.der == null)
                    p = null;                                    // 1) hoja
                else if (p.izq == null)
                    p = p.der;                                   // 2) solo hijo derecho
                else if (p.der == null)
                    p = p.izq;                                   // 2) solo hijo izquierdo
                else {
                    Nodo t = minimoNodo(p.der);                  // 3) dos hijos
                    p.est  = t.est;                              //    copiar minimo derecho
                    p.der  = eliminar(t.est.getId(), p.der);     //    eliminar duplicado
                }
            }
        } else
            System.out.println("Elemento no encontrado.");
        return p;
    }

    // =========================================================
    // ENCONTRAR MINIMO (iterativo)  — rama mas izquierda = minimo
    // =========================================================
    private Nodo minimoNodo(Nodo p) {
        if (p != null)
            while (p.izq != null)
                p = p.izq;
        return p;
    }

    // =========================================================
    // RECORRIDO INORDEN (driver publico)
    // =========================================================
    public void recorrerBST() {
        System.out.print("El arbol es:");
        if (raiz != null)
            recorrer(raiz);
        else
            System.out.print(" Vacio");
        System.out.println();
    }

    // =========================================================
    // RECORRIDO INORDEN (recursivo privado)
    // Izquierda -> Raiz -> Derecha  =>  produce salida ordenada
    // =========================================================
    private void recorrer(Nodo ptr) {
        if (ptr.izq != null) recorrer(ptr.izq);
        System.out.print(" " + ptr.est.getId());
        if (ptr.der != null) recorrer(ptr.der);
    }

    // =========================================================
    // ALTURA — iterativa con pila manual para evitar StackOverflow
    // en casos degenerados (insercion secuencial produce altura = n).
    // =========================================================
    public int getAltura() {
        if (raiz == null) return 0;
        int    maxH      = 0;
        Nodo[] pilaNodes = new Nodo[Math.max(cantidad * 2, 16)];
        int[]  pilaProf  = new int [Math.max(cantidad * 2, 16)];
        int    top       = 0;
        pilaNodes[top] = raiz;
        pilaProf [top] = 1;
        top++;
        while (top > 0) {
            top--;
            Nodo n = pilaNodes[top];
            int  d = pilaProf [top];
            if (d > maxH) maxH = d;
            if (n.izq != null) { pilaNodes[top] = n.izq; pilaProf[top] = d + 1; top++; }
            if (n.der != null) { pilaNodes[top] = n.der; pilaProf[top] = d + 1; top++; }
        }
        return maxH;
    }

    public int     getCantidad() { return cantidad; }
    public boolean estaVacio()   { return raiz == null; }
}
