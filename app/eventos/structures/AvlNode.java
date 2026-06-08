package eventos.structures;

import eventos.model.Student;

public class AvlNode {

    private Student student;
    private AvlNode left;
    private AvlNode right;
    private AvlNode parent;
    private int     height;

    public AvlNode(Student student) {
        this.student = student;
        this.height  = 1;
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

    public int getBalanceFactor() {
        int leftH  = (left  != null) ? left.height  : 0;
        int rightH = (right != null) ? right.height : 0;
        return rightH - leftH;
    }
}