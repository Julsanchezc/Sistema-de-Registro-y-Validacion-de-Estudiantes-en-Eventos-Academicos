package eventos.structures;

import java.util.Comparator;

public class MinHeap<T> {

    @SuppressWarnings("unchecked")
    private T[]                 data = (T[]) new Object[16];
    private int                 size = 0;
    private final Comparator<T> cmp;

    public MinHeap(Comparator<T> comparator) { this.cmp = comparator; }

    public void insert(T item) {
        if (size == data.length) grow();
        data[size] = item;
        siftUp(size++);
    }

    public T       peek()    { return size == 0 ? null : data[0]; }
    public int     size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    public T poll() {
        if (size == 0) return null;
        T min = data[0];
        data[0]    = data[--size];
        data[size] = null;
        if (size > 0) siftDown(0);
        return min;
    }

    public void clear() { java.util.Arrays.fill(data, 0, size, null); size = 0; }

    private void siftUp(int i) {
        while (i > 0) {
            int p = (i - 1) >>> 1;
            if (cmp.compare(data[i], data[p]) >= 0) break;
            swap(i, p); i = p;
        }
    }

    private void siftDown(int i) {
        for (;;) {
            int l = (i << 1) + 1, r = l + 1, s = i;
            if (l < size && cmp.compare(data[l], data[s]) < 0) s = l;
            if (r < size && cmp.compare(data[r], data[s]) < 0) s = r;
            if (s == i) break;
            swap(i, s); i = s;
        }
    }

    private void swap(int a, int b) { T t = data[a]; data[a] = data[b]; data[b] = t; }

    @SuppressWarnings("unchecked")
    private void grow() {
        T[] n = (T[]) new Object[data.length << 1];
        System.arraycopy(data, 0, n, 0, size);
        data = n;
    }
}
