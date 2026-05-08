
public class ArbolAVL {

    private NodoAVL raiz;
    private int     cantidadNodos;

    public ArbolAVL() {
        this.raiz          = null;
        this.cantidadNodos = 0;
    }

    // =========================================================
    // ALTURA DE UN NODO (null = 0)
    // =========================================================
    private int altura(NodoAVL n) {
        return (n == null) ? 0 : n.getAltura();
    }

    // =========================================================
    // RECALCULAR ALTURA DE UN NODO
    // =========================================================
    private void ajustarAltura(NodoAVL n) {
        if (n != null)
            n.setAltura(1 + Math.max(altura(n.getIzquierda()), altura(n.getDerecha())));
    }

    // =========================================================
    // ROTACION SIMPLE DERECHA
    // Caso: subarbol izquierdo demasiado alto y su hijo izq. es mas alto
    // =========================================================
    private NodoAVL rotarDerecha(NodoAVL n) {
        NodoAVL m = n.getIzquierda();
        if (m.getDerecha() != null) m.getDerecha().setPadre(n);
        m.setPadre(n.getPadre());
        if      (n.getPadre() == null)                          raiz = m;
        else if (n == n.getPadre().getIzquierda()) n.getPadre().setIzquierda(m);
        else                                        n.getPadre().setDerecha(m);
        n.setIzquierda(m.getDerecha());
        m.setDerecha(n);
        n.setPadre(m);
        ajustarAltura(n);
        ajustarAltura(m);
        return m;
    }

    // =========================================================
    // ROTACION SIMPLE IZQUIERDA
    // Caso: subarbol derecho demasiado alto y su hijo der. es mas alto
    // =========================================================
    private NodoAVL rotarIzquierda(NodoAVL n) {
        NodoAVL m = n.getDerecha();
        if (m.getIzquierda() != null) m.getIzquierda().setPadre(n);
        m.setPadre(n.getPadre());
        if      (n.getPadre() == null)                          raiz = m;
        else if (n == n.getPadre().getIzquierda()) n.getPadre().setIzquierda(m);
        else                                        n.getPadre().setDerecha(m);
        n.setDerecha(m.getIzquierda());
        m.setIzquierda(n);
        n.setPadre(m);
        ajustarAltura(n);
        ajustarAltura(m);
        return m;
    }

    // =========================================================
    // REBALANCEAR HACIA LA DERECHA (subarbol izquierdo muy alto)
    // =========================================================
    private void rebalancearDerecha(NodoAVL n) {
        NodoAVL m = n.getIzquierda();
        if (altura(m.getDerecha()) > altura(m.getIzquierda()))
            rotarIzquierda(m);     // rotacion doble: primero izq. sobre m
        rotarDerecha(n);           // luego derecha sobre n
    }

    // =========================================================
    // REBALANCEAR HACIA LA IZQUIERDA (subarbol derecho muy alto)
    // =========================================================
    private void rebalancearIzquierda(NodoAVL n) {
        NodoAVL m = n.getDerecha();
        if (altura(m.getIzquierda()) > altura(m.getDerecha()))
            rotarDerecha(m);       // rotacion doble: primero der. sobre m
        rotarIzquierda(n);         // luego izquierda sobre n
    }

    // =========================================================
    // REBALANCEAR NODO Y PROPAGAR HACIA LA RAIZ
    // =========================================================
    private void rebalancear(NodoAVL n) {
        if (n == null) return;
        NodoAVL padre = n.getPadre();
        if      (altura(n.getIzquierda()) > altura(n.getDerecha()) + 1)
            rebalancearDerecha(n);          // izquierda muy alta
        else if (altura(n.getDerecha()) > altura(n.getIzquierda()) + 1)
            rebalancearIzquierda(n);        // derecha muy alta
        ajustarAltura(n);
        rebalancear(padre);                 // subir al padre
    }

    // =========================================================
    // INSERCION  O(log n)
    // Iterativa: recorre hasta la posicion correcta, luego rebalancea
    // =========================================================
    public boolean insertar(Estudiante est) {
        if (existe(est.getId())) return false;
        NodoAVL nuevo = new NodoAVL(est);
        if (raiz == null) { raiz = nuevo; cantidadNodos++; return true; }
        NodoAVL cur = raiz, padre = null;
        while (cur != null) {
            padre = cur;
            cur = (est.getId() < cur.getIdEstudiante())
                    ? cur.getIzquierda() : cur.getDerecha();
        }
        nuevo.setPadre(padre);
        if (est.getId() < padre.getIdEstudiante()) padre.setIzquierda(nuevo);
        else                                        padre.setDerecha(nuevo);
        rebalancear(padre);    // restaurar propiedad AVL
        cantidadNodos++;
        return true;
    }

}