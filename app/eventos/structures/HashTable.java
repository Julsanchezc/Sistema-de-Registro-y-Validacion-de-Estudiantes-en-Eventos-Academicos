package eventos.structures;

import java.util.function.BiConsumer;

public class HashTable<K, V> {

    private static final int   INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR      = 0.75f;

    private static class Entry<K, V> {
        K key; V value; Entry<K, V> next;
        Entry(K k, V v) { key = k; value = v; }
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] table = new Entry[INITIAL_CAPACITY];
    private int capacity = INITIAL_CAPACITY;
    private int size     = 0;

    public void put(K key, V value) {
        if ((float) size / capacity >= LOAD_FACTOR) resize();
        int idx = index(key);
        for (Entry<K, V> e = table[idx]; e != null; e = e.next)
            if (e.key.equals(key)) { e.value = value; return; }
        Entry<K, V> e = new Entry<>(key, value);
        e.next     = table[idx];
        table[idx] = e;
        size++;
    }

    public V get(K key) {
        for (Entry<K, V> e = table[index(key)]; e != null; e = e.next)
            if (e.key.equals(key)) return e.value;
        return null;
    }

    public V remove(K key) {
        int idx = index(key);
        Entry<K, V> cur = table[idx], prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
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

    public void forEach(BiConsumer<K, V> action) {
        for (Entry<K, V> head : table)
            for (Entry<K, V> e = head; e != null; e = e.next)
                action.accept(e.key, e.value);
    }

    private int index(K key) {
        int h = key.hashCode();
        return Math.abs(h ^ (h >>> 16)) % capacity;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        capacity *= 2;
        Entry<K, V>[] n = new Entry[capacity];
        for (Entry<K, V> head : table)
            for (Entry<K, V> e = head; e != null; ) {
                Entry<K, V> nx = e.next;
                int h   = e.key.hashCode();
                int idx = Math.abs(h ^ (h >>> 16)) % capacity;
                e.next = n[idx]; n[idx] = e; e = nx;
            }
        table = n;
    }
}
