package eventos.structures;

import eventos.model.Student;

/*
 * Árbol Binario de Búsqueda Auto-Balanceado (AVL).
 *
 * Uso en el sistema: almacena los estudiantes registrados en un evento, ordenados
 * por ID de menor a mayor. Garantiza O(log n) en inserción, búsqueda y eliminación
 * incluso en el peor caso, a diferencia del BST simple que se degrada a O(n)
 * con inserciones ordenadas.
 *
 * El balance se mantiene con rotaciones después de cada operación: si la diferencia
 * de alturas entre subárbol izquierdo y derecho supera 1 en algún nodo, se aplica
 * la rotación simple o doble correspondiente y se recalcula la altura.
 *
 * El recorrido in-order (izquierdo -> raíz -> derecho) produce los estudiantes
 * en orden ascendente por ID, lo que se aprovecha en collectInOrder() para
 * exportar CSV y alimentar el MinHeap de top-K.
 *
 * Complejidades:
 *   insert / find / remove  -> O(log n)
 *   collectInOrder          -> O(n)
 */
public class AvlTree {

    private AvlNode root;
    private int     nodeCount;

    public AvlTree() { this.root = null; this.nodeCount = 0; }

    // -----------------------------------------------------------------------
    // Utilidades de altura
    // -----------------------------------------------------------------------

    private int height(AvlNode n) { return (n == null) ? 0 : n.getHeight(); }

    // Recalcula la altura de n a partir de sus hijos; llamar DESPUÉS de rotaciones.
    private void updateHeight(AvlNode n) {
        if (n != null) n.setHeight(1 + Math.max(height(n.getLeft()), height(n.getRight())));
    }

    // -----------------------------------------------------------------------
    // Rotaciones
    // -----------------------------------------------------------------------

    /*
     * Rotación simple hacia la derecha (caso Left-Left).
     * Se aplica cuando el subárbol izquierdo de n es demasiado alto y su hijo
     * izquierdo m no tiene el subárbol derecho más alto que el izquierdo.
     *
     *      n                  m
     *     / \                / \
     *    m   C    ->        A   n
     *   / \                    / \
     *  A   B                  B   C
     *
     * m sube al lugar de n; n baja como hijo derecho de m.
     */
    private AvlNode rotateRight(AvlNode n) {
        AvlNode m = n.getLeft();
        if (m.getRight() != null) m.getRight().setParent(n); // el hijo derecho de m pasa a n
        m.setParent(n.getParent());
        if      (n.getParent() == null)                              root = m;
        else if (n == n.getParent().getLeft()) n.getParent().setLeft(m);
        else                                   n.getParent().setRight(m);
        n.setLeft(m.getRight());
        m.setRight(n);
        n.setParent(m);
        updateHeight(n); updateHeight(m); // actualizar primero n (está más abajo), luego m
        return m;
    }

    /*
     * Rotación simple hacia la izquierda (caso Right-Right).
     * Se aplica cuando el subárbol derecho de n es demasiado alto y su hijo
     * derecho m no tiene el subárbol izquierdo más alto que el derecho.
     *
     *   n                    m
     *  / \                  / \
     * A   m      ->        n   C
     *    / \              / \
     *   B   C            A   B
     *
     * m sube; n baja como hijo izquierdo de m.
     */
    private AvlNode rotateLeft(AvlNode n) {
        AvlNode m = n.getRight();
        if (m.getLeft() != null) m.getLeft().setParent(n); // el hijo izquierdo de m pasa a n
        m.setParent(n.getParent());
        if      (n.getParent() == null)                              root = m;
        else if (n == n.getParent().getLeft()) n.getParent().setLeft(m);
        else                                   n.getParent().setRight(m);
        n.setRight(m.getLeft());
        m.setLeft(n);
        n.setParent(m);
        updateHeight(n); updateHeight(m);
        return m;
    }

    // Caso Left-Right: el hijo izquierdo m está cargado a la derecha.
    // Primero se rota m a la izquierda para convertirlo en caso Left-Left,
    // luego se aplica la rotación derecha normal sobre n.
    private void rebalanceRight(AvlNode n) {
        AvlNode m = n.getLeft();
        if (height(m.getRight()) > height(m.getLeft())) rotateLeft(m);
        rotateRight(n);
    }

    // Caso Right-Left: el hijo derecho m está cargado a la izquierda.
    // Primero se rota m a la derecha para convertirlo en caso Right-Right,
    // luego se aplica la rotación izquierda normal sobre n.
    private void rebalanceLeft(AvlNode n) {
        AvlNode m = n.getRight();
        if (height(m.getLeft()) > height(m.getRight())) rotateRight(m);
        rotateLeft(n);
    }

    // Sube desde n hasta la raíz verificando y corrigiendo el balance en cada nodo.
    // Se llama después de toda inserción o eliminación.
    private void rebalance(AvlNode n) {
        if (n == null) return;
        AvlNode parent = n.getParent();
        if      (height(n.getLeft())  > height(n.getRight()) + 1) rebalanceRight(n);
        else if (height(n.getRight()) > height(n.getLeft())  + 1) rebalanceLeft(n);
        updateHeight(n);
        rebalance(parent); // propagar hacia la raíz
    }

    // -----------------------------------------------------------------------
    // Operaciones principales
    // -----------------------------------------------------------------------

    // Inserta un estudiante ordenado por ID.
    // Desciende como en un BST normal, inserta la hoja y rebalancea hacia arriba.
    // Retorna false si ya existe un estudiante con ese ID.
    public boolean insert(Student s) {
        if (contains(s.getId())) return false;
        AvlNode node = new AvlNode(s);
        if (root == null) { root = node; nodeCount++; return true; }
        AvlNode cur = root, parent = null;
        while (cur != null) {
            parent = cur;
            cur = (s.getId() < cur.getStudentId()) ? cur.getLeft() : cur.getRight();
        }
        node.setParent(parent);
        if (s.getId() < parent.getStudentId()) parent.setLeft(node);
        else                                   parent.setRight(node);
        rebalance(parent);
        nodeCount++;
        return true;
    }

    public Student find(int id) {
        AvlNode n = findNode(root, id);
        return (n != null) ? n.getStudent() : null;
    }

    // Búsqueda binaria recursiva; O(log n) garantizado por el balance del AVL.
    private AvlNode findNode(AvlNode node, int id) {
        if (node == null)           return null;
        int cur = node.getStudentId();
        if      (id < cur) return findNode(node.getLeft(),  id);
        else if (id > cur) return findNode(node.getRight(), id);
        else               return node;
    }

    public boolean contains(int id) { return find(id) != null; }

    public boolean remove(int id) {
        AvlNode n = findNode(root, id);
        if (n == null) return false;
        removeNode(n);
        nodeCount--;
        return true;
    }

    /*
     * Elimina un nodo con tres casos estándar del BST:
     *   1. Dos hijos: copia datos del sucesor in-order (mínimo del subárbol derecho)
     *      y elimina recursivamente ese sucesor, que tiene a lo sumo un hijo.
     *   2. Un hijo: el padre apunta directamente al único hijo.
     *   3. Hoja: se desconecta del padre.
     * En todos los casos se rebalancea desde el padre del nodo eliminado hacia la raíz.
     */
    private void removeNode(AvlNode n) {
        if (n.getLeft() != null && n.getRight() != null) {
            // Caso dos hijos: copiar datos del sucesor y eliminarlo (tiene <= 1 hijo)
            AvlNode successor = minNode(n.getRight());
            n.setStudent(successor.getStudent());
            removeNode(successor);
            return;
        }
        AvlNode child  = (n.getLeft() != null) ? n.getLeft() : n.getRight();
        AvlNode parent = n.getParent();
        if (child  != null) child.setParent(parent);
        if (parent == null) { root = child; }
        else if (n == parent.getLeft()) { parent.setLeft(child); }
        else                            { parent.setRight(child); }
        rebalance(parent);
    }

    // Nodo más a la izquierda del subárbol: es el mínimo (sucesor in-order).
    private AvlNode minNode(AvlNode node) {
        while (node.getLeft() != null) node = node.getLeft();
        return node;
    }

    // -----------------------------------------------------------------------
    // Recorridos y exportación
    // -----------------------------------------------------------------------

    // Recorre en in-order y devuelve los estudiantes ordenados ascendentemente por ID.
    // idx se pasa como arreglo de un elemento para poder modificarlo dentro de la recursión.
    public Student[] collectInOrder() {
        Student[] arr = new Student[nodeCount];
        int[]     idx = { 0 };
        collectRec(root, arr, idx);
        return arr;
    }

    private void collectRec(AvlNode node, Student[] arr, int[] idx) {
        if (node == null) return;
        collectRec(node.getLeft(),  arr, idx);
        arr[idx[0]++] = node.getStudent();
        collectRec(node.getRight(), arr, idx);
    }

    public void visualize() {
        System.out.println("\n╔══════ AVL TREE STRUCTURE ══════════════════════════════╗");
        System.out.printf("║  Nodes: %d  │  Height: %d  │  bf  0:balanced  ±1:OK%n",
                nodeCount, getHeight());
        System.out.println("╚════════════════════════════════════════════════════════╝");
        if (isEmpty()) System.out.println("  (Empty tree)");
        else           visualizeRec(root, "", true);
        System.out.println();
    }

    private void visualizeRec(AvlNode node, String prefix, boolean isLast) {
        if (node == null) return;
        int bf = node.getBalanceFactor();
        System.out.print(prefix);
        System.out.print(isLast ? "└── " : "├── ");
        System.out.println("ID:" + node.getStudentId()
                + "  h=" + node.getHeight()
                + "  bf=" + String.format("%+d", bf)
                + "  " + node.getStudent().getName());
        String newPrefix = prefix + (isLast ? "    " : "│   ");
        if (node.getLeft() != null || node.getRight() != null) {
            visualizeRec(node.getLeft(),  newPrefix, node.getRight() == null);
            visualizeRec(node.getRight(), newPrefix, true);
        }
    }

    // Escribe todos los estudiantes en CSV usando in-order (quedan ordenados por ID).
    public void writeInOrderCSV(java.io.PrintWriter pw) {
        writeInOrderRec(root, pw);
    }

    private void writeInOrderRec(AvlNode n, java.io.PrintWriter pw) {
        if (n == null) return;
        writeInOrderRec(n.getLeft(), pw);
        Student s = n.getStudent();
        pw.printf("%d,%s,%s,%s,%s,%d%n", s.getId(), s.getName(), s.getEmail(),
                s.getProgram(), s.isAttended(), s.getAttendanceCount());
        writeInOrderRec(n.getRight(), pw);
    }

    // Cuenta cuántos estudiantes tienen asistencia marcada; recorre todo el árbol O(n).
    public int countAttendances() { return countAttendancesRec(root); }

    private int countAttendancesRec(AvlNode node) {
        if (node == null) return 0;
        int count = node.getStudent().isAttended() ? 1 : 0;
        return count + countAttendancesRec(node.getLeft()) + countAttendancesRec(node.getRight());
    }

    public int     getStudentCount() { return nodeCount; }
    public int     getHeight()       { return (root != null) ? root.getHeight() : 0; }
    public boolean isEmpty()         { return root == null; }
}
