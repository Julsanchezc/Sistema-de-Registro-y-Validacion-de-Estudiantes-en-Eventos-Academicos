package eventos.structures;

import eventos.model.Student;

/*
 * Nodo del árbol AVL.
 *
 * Almacena un Student y mantiene punteros a padre, hijo izquierdo e hijo derecho.
 * El puntero a padre permite hacer rotaciones sin pila auxiliar: al rotar basta
 * ajustar los punteros del padre directamente.
 *
 * El campo height se actualiza en cada rotación para que el árbol calcule el
 * factor de equilibrio en O(1) sin tener que recorrer subárboles.
 */
public class AvlNode {

    private Student student;
    private AvlNode left;
    private AvlNode right;
    private AvlNode parent; // referencia al padre: permite rotaciones sin pila auxiliar
    private int     height; // altura del subárbol con raíz en este nodo; una hoja tiene altura 1

    public AvlNode(Student student) {
        this.student = student;
        this.height  = 1;           // nodo recién creado es hoja, altura 1
        left = right = parent = null;
    }

    public Student getStudent()       { return student; }
    public AvlNode getLeft()          { return left; }
    public AvlNode getRight()         { return right; }
    public AvlNode getParent()        { return parent; }
    public int     getHeight()        { return height; }
    public int     getStudentId()     { return student.getId(); }

    public void setStudent(Student s) { this.student = s; }
    public void setLeft(AvlNode n)    { this.left    = n; }
    public void setRight(AvlNode n)   { this.right   = n; }
    public void setParent(AvlNode n)  { this.parent  = n; }
    public void setHeight(int h)      { this.height  = h; }

    /*
     * Factor de equilibrio = altura(derecho) - altura(izquierdo).
     *   0   -> perfectamente balanceado
     *  +1   -> ligeramente cargado a la derecha (válido en AVL)
     *  -1   -> ligeramente cargado a la izquierda (válido en AVL)
     *  >=+2 -> subárbol derecho demasiado alto: AvlTree aplica rotación izquierda
     *  <=-2 -> subárbol izquierdo demasiado alto: AvlTree aplica rotación derecha
     */
    public int getBalanceFactor() {
        int leftH  = (left  != null) ? left.height  : 0;
        int rightH = (right != null) ? right.height : 0;
        return rightH - leftH;
    }
}
