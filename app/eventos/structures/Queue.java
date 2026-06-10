package eventos.structures;

/*
 * Cola genérica FIFO implementada con lista enlazada simple.
 *
 * Uso en el sistema: lista de espera de un evento. Cuando el evento alcanza
 * su capacidad máxima, los nuevos estudiantes se encolan aquí y son promovidos
 * al árbol AVL en orden de llegada cuando se libera un cupo.
 *
 * Complejidades:
 *   enqueue / dequeue / peek  -> O(1)
 *   contents                  -> O(n)
 */
// Retorna Object[] en contents() para evitar ClassCastException por borrado de tipos genéricos.
public class Queue<T> {

    // Nodo interno: almacena el dato y apunta al siguiente elemento de la cola.
    private class Node {
        T    data;
        Node next;
        Node(T d) { data = d; }
    }

    private Node front; // apunta al primer elemento (el próximo en salir)
    private Node back;  // apunta al último elemento (donde se insertan los nuevos)
    private int  size;

    public Queue() { front = null; back = null; size = 0; }

    // Agrega un elemento al fondo de la cola.
    public void enqueue(T data) {
        Node node = new Node(data);
        if (back == null) { front = back = node; } // primera inserción: frente y fondo son el mismo
        else              { back.next = node; back = node; }
        size++;
    }

    // Extrae y retorna el elemento del frente; retorna null si la cola está vacía.
    // Si la cola queda vacía, back también se pone a null para no dejar referencias huérfanas.
    public T dequeue() {
        if (front == null) return null;
        T data = front.data;
        front  = front.next;
        if (front == null) back = null;
        size--;
        return data;
    }

    // Consulta el frente sin extraerlo.
    public T       peek()    { return front != null ? front.data : null; }
    public boolean isEmpty() { return front == null; }
    public int     size()    { return size; }

    // Devuelve todos los elementos en orden de llegada como arreglo de Object.
    // Se usa Object[] en lugar de T[] porque crear arreglos de tipo genérico lanza ClassCastException.
    public Object[] contents() {
        Object[] arr = new Object[size];
        Node     cur = front;
        for (int i = 0; i < size; i++) { arr[i] = cur.data; cur = cur.next; }
        return arr;
    }
}
