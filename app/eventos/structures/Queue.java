package eventos.structures;

// Returns Object[] from contents() to avoid ClassCastException with type erasure.
public class Queue<T> {

    private class Node {
        T    data;
        Node next;
        Node(T d) { data = d; }
    }

    private Node front;
    private Node back;
    private int  size;

    public Queue() { front = null; back = null; size = 0; }

    public void enqueue(T data) {
        Node node = new Node(data);
        if (back == null) { front = back = node; }
        else              { back.next = node; back = node; }
        size++;
    }

    public T dequeue() {
        if (front == null) return null;
        T data = front.data;
        front  = front.next;
        if (front == null) back = null;
        size--;
        return data;
    }

    public T       peek()    { return front != null ? front.data : null; }
    public boolean isEmpty() { return front == null; }
    public int     size()    { return size; }

    public Object[] contents() {
        Object[] arr = new Object[size];
        Node     cur = front;
        for (int i = 0; i < size; i++) { arr[i] = cur.data; cur = cur.next; }
        return arr;
    }
}