package eventos.structures;

import eventos.model.Student;

public class AvlTree {

    private AvlNode root;
    private int     nodeCount;

    public AvlTree() { this.root = null; this.nodeCount = 0; }

    private int height(AvlNode n) { return (n == null) ? 0 : n.getHeight(); }

    private void updateHeight(AvlNode n) {
        if (n != null) n.setHeight(1 + Math.max(height(n.getLeft()), height(n.getRight())));
    }

    private AvlNode rotateRight(AvlNode n) {
        AvlNode m = n.getLeft();
        if (m.getRight() != null) m.getRight().setParent(n);
        m.setParent(n.getParent());
        if      (n.getParent() == null)                              root = m;
        else if (n == n.getParent().getLeft()) n.getParent().setLeft(m);
        else                                   n.getParent().setRight(m);
        n.setLeft(m.getRight());
        m.setRight(n);
        n.setParent(m);
        updateHeight(n); updateHeight(m);
        return m;
    }

    private AvlNode rotateLeft(AvlNode n) {
        AvlNode m = n.getRight();
        if (m.getLeft() != null) m.getLeft().setParent(n);
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

    private void rebalanceRight(AvlNode n) {
        AvlNode m = n.getLeft();
        if (height(m.getRight()) > height(m.getLeft())) rotateLeft(m);
        rotateRight(n);
    }

    private void rebalanceLeft(AvlNode n) {
        AvlNode m = n.getRight();
        if (height(m.getLeft()) > height(m.getRight())) rotateRight(m);
        rotateLeft(n);
    }

    private void rebalance(AvlNode n) {
        if (n == null) return;
        AvlNode parent = n.getParent();
        if      (height(n.getLeft())  > height(n.getRight()) + 1) rebalanceRight(n);
        else if (height(n.getRight()) > height(n.getLeft())  + 1) rebalanceLeft(n);
        updateHeight(n);
        rebalance(parent);
    }

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

    private void removeNode(AvlNode n) {
        if (n.getLeft() != null && n.getRight() != null) {
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

    private AvlNode minNode(AvlNode node) {
        while (node.getLeft() != null) node = node.getLeft();
        return node;
    }

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