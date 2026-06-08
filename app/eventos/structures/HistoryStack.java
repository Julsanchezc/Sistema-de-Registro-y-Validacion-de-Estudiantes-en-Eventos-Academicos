package eventos.structures;

import eventos.model.Student;

public class HistoryStack {

    public static class Entry {
        public final String  type;
        public final String  description;
        public final Student undoData;  // non-null only for REMOVAL entries

        public Entry(String type, String description, Student undoData) {
            this.type        = type;
            this.description = description;
            this.undoData    = undoData;
        }
    }

    private class Node {
        Entry dato;
        Node  next;
        Node(Entry e) { dato = e; }
    }

    private Node top;
    private int  size;
    private static final int MAX = 50;

    public void push(String type, String description) {
        push(type, description, null);
    }

    public void push(String type, String description, Student student) {
        if (size >= MAX) {
            if (top == null) return;
            if (top.next == null) { top = null; size--; }
            else {
                Node cur = top;
                while (cur.next.next != null) cur = cur.next;
                cur.next = null;
                size--;
            }
        }
        Node node = new Node(new Entry(type, description, student));
        node.next = top;
        top  = node;
        size++;
    }

    public Entry pop() {
        if (top == null) return null;
        Entry e = top.dato;
        top = top.next;
        size--;
        return e;
    }

    public Entry   peek()    { return top != null ? top.dato : null; }
    public boolean isEmpty() { return top == null; }
    public int     size()    { return size; }

    public Entry[] entries() {
        Entry[] arr = new Entry[size];
        Node    cur = top;
        for (int i = 0; i < size; i++) { arr[i] = cur.dato; cur = cur.next; }
        return arr;
    }
}