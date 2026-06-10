package eventos.structures;

import java.util.Comparator;

/*
 * Min-Heap genérico implementado sobre un arreglo dinámico.
 *
 * Un Min-Heap es un árbol binario completo representado en arreglo donde el elemento
 * en posición i tiene sus hijos en 2i+1 (izquierdo) y 2i+2 (derecho), y su padre en
 * (i-1)/2. La propiedad de heap garantiza que cada nodo es menor o igual que sus hijos,
 * por lo que el mínimo siempre está en el índice 0.
 *
 * El comparador inyectado en el constructor determina qué elemento es el "mínimo".
 *
 * Uso en el sistema (algoritmo top-K con heap de tamaño k):
 *   - EventService.getTopAttendees: top-K estudiantes por asistencia en un evento.
 *     Se mantiene un heap de tamaño k; si el nuevo elemento supera al mínimo actual
 *     (tope del heap), el mínimo es expulsado con poll y entra el nuevo.
 *     Al terminar el recorrido, el heap contiene exactamente los K mayores.
 *   - EventManager.getTopAttendeesGlobal: mismo algoritmo pero agregando asistencias
 *     de todos los eventos.
 *   - EventManager.getEventsByAvailability: ordena eventos por cupos disponibles
 *     extrayendo del heap en orden ascendente.
 *
 * Complejidades:
 *   insert / poll  -> O(log n)
 *   peek           -> O(1)
 */
public class MinHeap<T> {

    @SuppressWarnings("unchecked")
    private T[]                 data = (T[]) new Object[16]; // arreglo backing del heap
    private int                 size = 0;
    private final Comparator<T> cmp; // define el orden; el "mínimo" según este comparador va a la raíz

    public MinHeap(Comparator<T> comparator) { this.cmp = comparator; }

    // Inserta un elemento al final del arreglo y lo sube (siftUp) hasta restaurar la propiedad de heap.
    public void insert(T item) {
        if (size == data.length) grow();
        data[size] = item;
        siftUp(size++);
    }

    // Retorna el mínimo (raíz, índice 0) sin extraerlo.
    public T       peek()    { return size == 0 ? null : data[0]; }
    public int     size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    // Extrae y retorna el mínimo.
    // El último elemento se mueve a la raíz para mantener el árbol completo,
    // y luego se hunde (siftDown) hasta restaurar la propiedad de heap.
    public T poll() {
        if (size == 0) return null;
        T min = data[0];
        data[0]    = data[--size];
        data[size] = null;          // liberar referencia para el recolector de basura
        if (size > 0) siftDown(0);
        return min;
    }

    public void clear() { java.util.Arrays.fill(data, 0, size, null); size = 0; }

    // Sube el elemento en la posición i intercambiando con su padre
    // mientras sea menor que él. Asegura que el mínimo llegue a la raíz.
    private void siftUp(int i) {
        while (i > 0) {
            int p = (i - 1) >>> 1; // índice del padre: (i-1)/2 con desplazamiento de bits
            if (cmp.compare(data[i], data[p]) >= 0) break; // ya está en posición correcta
            swap(i, p); i = p;
        }
    }

    // Hunde el elemento en la posición i intercambiando con el menor de sus hijos
    // mientras viole la propiedad de heap.
    // s (smallest) rastrea cuál de los tres candidatos es el menor para decidir si seguir bajando.
    private void siftDown(int i) {
        for (;;) {
            int l = (i << 1) + 1, r = l + 1, s = i; // l = hijo izq, r = hijo der, s = índice del menor
            if (l < size && cmp.compare(data[l], data[s]) < 0) s = l;
            if (r < size && cmp.compare(data[r], data[s]) < 0) s = r;
            if (s == i) break; // el nodo ya es menor o igual que ambos hijos: heap válido
            swap(i, s); i = s;
        }
    }

    private void swap(int a, int b) { T t = data[a]; data[a] = data[b]; data[b] = t; }

    // Duplica la capacidad del arreglo backing (length << 1 = length * 2).
    @SuppressWarnings("unchecked")
    private void grow() {
        T[] n = (T[]) new Object[data.length << 1];
        System.arraycopy(data, 0, n, 0, size);
        data = n;
    }
}
