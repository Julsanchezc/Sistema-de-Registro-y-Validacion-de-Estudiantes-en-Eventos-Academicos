
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



}