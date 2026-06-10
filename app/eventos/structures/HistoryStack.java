package eventos.structures;

import eventos.model.Student;

/*
 * Pila LIFO para registrar el historial de operaciones de un evento y soportar undo.
 *
 * Uso en el sistema: cada operación relevante (registro, asistencia, eliminación, etc.)
 * se apila como un Entry en EventService. La operación undoLastRemoval inspecciona el
 * tope con peek: si la última operación fue una eliminación, el estudiante original está
 * guardado en Entry.undoData y puede restaurarse.
 *
 * Tiene un límite de MAX (50) entradas; al superarlo se descarta la entrada más antigua
 * (la del fondo) para no crecer indefinidamente en memoria.
 *
 * Complejidades:
 *   push / pop / peek  -> O(1)  (O(MAX) constante al descartar el fondo)
 *   entries            -> O(n)
 */
public class HistoryStack {

    // Una entrada del historial: tipo de operación, descripción legible y datos para undo.
    // undoData solo se rellena en entradas de tipo "REMOVAL" para permitir restaurar al estudiante.
    public static class Entry {
        public final String  type;
        public final String  description;
        public final Student undoData;

        public Entry(String type, String description, Student undoData) {
            this.type        = type;
            this.description = description;
            this.undoData    = undoData;
        }
    }

    // Nodo de la lista enlazada que implementa la pila.
    private class Node {
        Entry dato;
        Node  next;
        Node(Entry e) { dato = e; }
    }

    private Node top;  // tope de la pila (operación más reciente)
    private int  size;
    // Al llegar a MAX entradas se descarta la del fondo para no crecer sin límite.
    private static final int MAX = 50;

    // Apila una operación sin datos de undo (registro, asistencia, exportación, etc.).
    public void push(String type, String description) {
        push(type, description, null);
    }

    // Apila una operación con datos opcionales para undo.
    // Si se alcanzó el límite MAX, elimina primero el nodo del fondo (el más antiguo).
    public void push(String type, String description, Student student) {
        if (size >= MAX) {
            if (top == null) return;
            if (top.next == null) { top = null; size--; }
            else {
                // Recorrer hasta el penúltimo nodo y cortar el último (fondo de la pila)
                Node cur = top;
                while (cur.next.next != null) cur = cur.next;
                cur.next = null;
                size--;
            }
        }
        Node node = new Node(new Entry(type, description, student));
        node.next = top; // el nuevo nodo apunta al antiguo tope
        top  = node;
        size++;
    }

    // Extrae y retorna la entrada del tope; retorna null si la pila está vacía.
    public Entry pop() {
        if (top == null) return null;
        Entry e = top.dato;
        top = top.next;
        size--;
        return e;
    }

    // Consulta el tope sin extraerlo; usado por undoLastRemoval para verificar antes de actuar.
    public Entry   peek()    { return top != null ? top.dato : null; }
    public boolean isEmpty() { return top == null; }
    public int     size()    { return size; }

    // Devuelve todas las entradas de más reciente a más antigua (para mostrar en la GUI).
    public Entry[] entries() {
        Entry[] arr = new Entry[size];
        Node    cur = top;
        for (int i = 0; i < size; i++) { arr[i] = cur.dato; cur = cur.next; }
        return arr;
    }
}
