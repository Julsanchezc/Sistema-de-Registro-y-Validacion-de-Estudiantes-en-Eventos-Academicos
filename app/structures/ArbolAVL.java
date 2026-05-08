
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




}