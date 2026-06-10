package eventos.structures;

import java.util.function.BiConsumer;

/*
 * Tabla de hash genérica con resolución de colisiones por encadenamiento separado.
 *
 * Uso en el sistema:
 *   - EventService: índice HashTable<Integer, Student> que permite buscar un estudiante
 *     por ID en O(1) sin recorrer el árbol AVL (que sería O(log n)).
 *   - EventManager.getTopAttendeesGlobal: dos tablas temporales (sumMap, nameMap) para
 *     agregar asistencias de todos los eventos antes de construir el MinHeap de top-K.
 *
 * Estructura interna: arreglo de cubos (table); cada cubo es el encabezado de una lista
 * enlazada de Entry. Cuando dos claves producen el mismo índice (colisión), sus entradas
 * se encadenan en esa lista.
 *
 * Cuando size/capacity supera el LOAD_FACTOR (0.75), la tabla se duplica y todas las
 * entradas se rehashean para mantener las cadenas cortas y la complejidad O(1) promedio.
 *
 * Complejidades (caso promedio):
 *   put / get / remove  -> O(1)
 *   resize (rehash)     -> O(n) amortizado
 *   forEach             -> O(n)
 */
public class HashTable<K, V> {

    private static final int   INITIAL_CAPACITY = 16;
    // Si size/capacity supera este umbral, la tabla se duplica para evitar cadenas largas.
    private static final float LOAD_FACTOR      = 0.75f;

    // Par clave-valor con puntero al siguiente en la cadena del mismo cubo.
    // Varios Entry conviven en el mismo cubo cuando hay colisiones.
    private static class Entry<K, V> {
        K key; V value; Entry<K, V> next;
        Entry(K k, V v) { key = k; value = v; }
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] table = new Entry[INITIAL_CAPACITY];
    private int capacity = INITIAL_CAPACITY;
    private int size     = 0;

    // Inserta o actualiza el valor de key.
    // Si la clave ya existe en la cadena del cubo, sobreescribe el valor sin duplicarla.
    // Si es nueva, se inserta al frente del cubo en O(1).
    public void put(K key, V value) {
        if ((float) size / capacity >= LOAD_FACTOR) resize();
        int idx = index(key);
        for (Entry<K, V> e = table[idx]; e != null; e = e.next)
            if (e.key.equals(key)) { e.value = value; return; } // clave existente: actualizar
        // Clave nueva: insertar al frente del cubo
        Entry<K, V> e = new Entry<>(key, value);
        e.next     = table[idx];
        table[idx] = e;
        size++;
    }

    // Retorna el valor de key, o null si no existe.
    public V get(K key) {
        for (Entry<K, V> e = table[index(key)]; e != null; e = e.next)
            if (e.key.equals(key)) return e.value;
        return null;
    }

    // Elimina la entrada con key y retorna su valor; retorna null si no existía.
    public V remove(K key) {
        int idx = index(key);
        Entry<K, V> cur = table[idx], prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                // Desconectar el nodo de la cadena enlazada
                if (prev == null) table[idx] = cur.next;
                else              prev.next  = cur.next;
                size--;
                return cur.value;
            }
            prev = cur; cur = cur.next;
        }
        return null;
    }

    public boolean containsKey(K key) { return get(key) != null; }
    public int     size()              { return size; }
    public boolean isEmpty()           { return size == 0; }

    public void clear() { java.util.Arrays.fill(table, null); size = 0; }

    // Itera sobre todos los pares en orden no determinista (depende del hash).
    public void forEach(BiConsumer<K, V> action) {
        for (Entry<K, V> head : table)
            for (Entry<K, V> e = head; e != null; e = e.next)
                action.accept(e.key, e.value);
    }

    // Calcula el índice del cubo para key.
    // Se mezclan bits altos y bajos con XOR para reducir colisiones cuando los hashCodes
    // solo difieren en bits de orden alto, lo que ocurre con frecuencia en IDs pequeños.
    private int index(K key) {
        int h = key.hashCode();
        return Math.abs(h ^ (h >>> 16)) % capacity;
    }

    // Duplica la capacidad y rehashea todos los elementos al nuevo arreglo.
    // Reutiliza los nodos Entry existentes (sin crear objetos nuevos),
    // simplemente reenlazando sus punteros next a los nuevos cubos.
    @SuppressWarnings("unchecked")
    private void resize() {
        capacity *= 2;
        Entry<K, V>[] n = new Entry[capacity];
        for (Entry<K, V> head : table)
            for (Entry<K, V> e = head; e != null; ) {
                Entry<K, V> nx = e.next;
                int h   = e.key.hashCode();
                int idx = Math.abs(h ^ (h >>> 16)) % capacity;
                e.next = n[idx]; n[idx] = e; e = nx; // insertar al frente del nuevo cubo
            }
        table = n;
    }
}
