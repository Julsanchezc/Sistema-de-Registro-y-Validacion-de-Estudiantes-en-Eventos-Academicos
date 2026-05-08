package structures;

import model.Estudiante;

/**
 * PilaHistorial.java
 * Pila (LIFO) para registrar el historial de operaciones sobre un evento.
 * ESTRUCTURA DE DATOS: Pila (Stack)
 * Uso en el sistema: auditoria de operaciones y deshacer la ultima eliminacion.
 *
 * Complejidad: push O(1), pop O(1), mostrar O(n)
 * Capacidad maxima: MAX=50 entradas (ventana deslizante: descarta la mas antigua).
 */
public class PilaHistorial {

    // =========================================================
    // REGISTRO PUBLICO — cada entrada del historial
    // =========================================================
    public static class Registro {
        public final String     tipo;
        public final String     descripcion;
        public final Estudiante datosUndo;  // != null solo en ELIMINACION

        public Registro(String tipo, String descripcion, Estudiante datosUndo) {
            this.tipo        = tipo;
            this.descripcion = descripcion;
            this.datosUndo   = datosUndo;
        }
    }
  
    // =========================================================
    // NODO INTERNO
    // =========================================================
    private class Nodo {
        Registro dato;
        Nodo     siguiente;
        Nodo(Registro d) { dato = d; }
    }

    // =========================================================
    // ATRIBUTOS
    // =========================================================
    private Nodo tope;
    private int  tamanio;
    private static final int MAX = 50;

    // =========================================================
    // PUSH (apilar)  O(1) amortizado
    // Cuando llega al maximo descarta la entrada mas antigua (fondo).
    // =========================================================
    public void registrar(String tipo, String descripcion) {
        registrar(tipo, descripcion, null);
    }

    public void registrar(String tipo, String descripcion, Estudiante est) {
        if (tamanio >= MAX) {
            if (tope == null) return;
            if (tope.siguiente == null) {
                tope = null;
                tamanio--;
            } else {
                Nodo actual = tope;
                while (actual.siguiente.siguiente != null) actual = actual.siguiente;
                actual.siguiente = null;
                tamanio--;
            }
        }
        Nodo nuevo = new Nodo(new Registro(tipo, descripcion, est));
        nuevo.siguiente = tope;
        tope    = nuevo;
        tamanio++;
    }

    // =========================================================
    // POP (desapilar)  O(1)
    // =========================================================
    public Registro desapilar() {
        if (tope == null) return null;
        Registro r = tope.dato;
        tope = tope.siguiente;
        tamanio--;
        return r;
    }
