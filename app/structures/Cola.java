package structures;

/**
 * Cola.java
 * Cola generica (FIFO) implementada con lista enlazada simple.
 * ESTRUCTURA DE DATOS: Cola (Queue)
 * Uso en el sistema: lista de espera cuando un evento llega a su aforo maximo.
 *
 * Complejidad: encolar O(1), desencolar O(1), contenido O(n)
 */
public class Cola<T> {

    private class Nodo {
        T    dato;
        Nodo siguiente;
        Nodo(T d) { dato = d; }
    }

    private Nodo frente;
    private Nodo fin;
    private int  tamanio;

    public Cola() {
        frente  = null;
        fin     = null;
        tamanio = 0;
    }

    // =========================================================
    // ENCOLAR — inserta al final  O(1)
    // =========================================================
    public void encolar(T dato) {
        Nodo nuevo = new Nodo(dato);
        if (fin == null) {
            frente = fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamanio++;
    }

    // =========================================================
    // DESENCOLAR — extrae del frente  O(1)
    // =========================================================
    public T desencolar() {
        if (frente == null) return null;
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamanio--;
        return dato;
    }

    // =========================================================
    // CONSULTAS
    // =========================================================
    public T       verFrente()  { return frente != null ? frente.dato : null; }
    public boolean estaVacia()  { return frente == null; }
    public int     getTamanio() { return tamanio; }

    // =========================================================
    // CONTENIDO — devuelve copia como Object[]  O(n)
    // Retorna Object[] para evitar ClassCastException con genericos en Java
    // (type erasure: (T[]) new Object[n] sigue siendo Object[] en runtime).
    // El caller hace el cast individual de cada elemento.
    // =========================================================
    public Object[] contenido() {
        Object[] arr    = new Object[tamanio];
        Nodo     actual = frente;
        for (int i = 0; i < tamanio; i++) {
            arr[i] = actual.dato;
            actual = actual.siguiente;
        }
        return arr;
    }
}
