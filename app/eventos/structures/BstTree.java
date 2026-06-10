package eventos.structures;

import eventos.model.Student;

/*
 * Árbol Binario de Búsqueda (BST) sin balanceo automático.
 *
 * Uso en el sistema: exclusivamente en benchmarks (BenchmarkRunner).
 * Se contrasta con el AvlTree para demostrar que el AVL mantiene O(log n)
 * en el peor caso, mientras el BST puede degradarse a O(n) cuando las
 * inserciones llegan en orden (el árbol degenera en lista enlazada).
 *
 * Complejidades (caso promedio / peor caso con entrada ordenada):
 *   insert / find / remove  -> O(log n) / O(n)
 *   getHeight (iterativo)   -> O(n)
 */
public class BstTree {

    // Nodo interno: almacena un estudiante y apunta a sus dos subárboles.
    private class Node {
        Node    left;
        Student student;
        Node    right;
        Node(Student s) { left = null; student = s; right = null; }
    }

    private Node root;
    private int  count;

    // Inserta ordenando por ID; descarta duplicados silenciosamente.
    public boolean insert(Student s) {
        if (contains(s.getId())) return false;
        root = insertRec(s, root);
        count++;
        return true;
    }

    private Node insertRec(Student s, Node p) {
        if (p == null)                          return new Node(s);
        if (s.getId() < p.student.getId())      p.left  = insertRec(s, p.left);
        else if (s.getId() > p.student.getId()) p.right = insertRec(s, p.right);
        return p;
    }

    public Student find(int id) {
        Node n = findRec(root, id);
        return (n != null) ? n.student : null;
    }

    private Node findRec(Node p, int id) {
        if (p == null)               return null;
        if (id < p.student.getId())  return findRec(p.left,  id);
        if (id > p.student.getId())  return findRec(p.right, id);
        return p;
    }

    public boolean contains(int id) { return find(id) != null; }

    public boolean remove(int id) {
        if (!contains(id)) return false;
        root = removeRec(id, root);
        count--;
        return true;
    }

    /*
     * Eliminación recursiva con tres casos:
     *   hoja            -> retorna null (el padre queda sin ese hijo)
     *   un hijo         -> retorna ese hijo (lo sube al lugar del nodo eliminado)
     *   dos hijos       -> copia datos del sucesor in-order (mínimo del subárbol derecho)
     *                      y luego elimina recursivamente ese sucesor
     */
    private Node removeRec(int id, Node p) {
        if (p == null) return null;
        if      (id < p.student.getId()) p.left  = removeRec(id, p.left);
        else if (id > p.student.getId()) p.right = removeRec(id, p.right);
        else {
            if      (p.left == null && p.right == null) return null;    // hoja
            else if (p.left == null)                    return p.right; // solo hijo derecho
            else if (p.right == null)                   return p.left;  // solo hijo izquierdo
            else {
                // Dos hijos: promover el sucesor in-order
                Node t   = minNode(p.right);
                p.student = t.student;
                p.right   = removeRec(t.student.getId(), p.right);
            }
        }
        return p;
    }

    private Node minNode(Node p) {
        while (p.left != null) p = p.left;
        return p;
    }

    /*
     * Calcula la altura de forma iterativa con una pila explícita (arreglos paralelos).
     * Se evita la recursión porque con entrada ordenada el árbol degenera en lista lineal
     * (n niveles) y una implementación recursiva lanzaría StackOverflowError.
     * La pila manual guarda pares (nodo, profundidad) y lleva el máximo encontrado.
     */
    public int getHeight() {
        if (root == null) return 0;
        int    maxH   = 0;
        Node[] stack  = new Node[Math.max(count * 2, 16)];
        int[]  depths = new int [Math.max(count * 2, 16)];
        int    top    = 0;
        stack[top] = root; depths[top] = 1; top++;
        while (top > 0) {
            top--;
            Node n = stack[top]; int d = depths[top];
            if (d > maxH) maxH = d;
            if (n.left  != null) { stack[top] = n.left;  depths[top] = d + 1; top++; }
            if (n.right != null) { stack[top] = n.right; depths[top] = d + 1; top++; }
        }
        return maxH;
    }

    public int     size()    { return count; }
    public boolean isEmpty() { return root == null; }
}
