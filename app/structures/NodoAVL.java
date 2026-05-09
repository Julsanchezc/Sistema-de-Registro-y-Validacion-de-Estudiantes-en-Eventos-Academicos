package structures;

import model.Estudiante;

/**
 * NodoAVL.java
 * Nodo del Arbol AVL. Almacena un Estudiante, punteros a hijos, padre y altura local.
 */
public class NodoAVL {

    private Estudiante estudiante;
    private NodoAVL    izquierda;
    private NodoAVL    derecha;
    private NodoAVL    padre;
    private int        altura;

    public NodoAVL(Estudiante estudiante) {
        this.estudiante = estudiante;
        this.altura     = 1;
        izquierda = derecha = padre = null;
    }
   public Estudiante getEstudiante()        { return estudiante; }
    public NodoAVL    getIzquierda()         { return izquierda; }
    public NodoAVL    getDerecha()           { return derecha; }
    public NodoAVL    getPadre()             { return padre; }
    public int        getAltura()            { return altura; }
    public int        getIdEstudiante()      { return estudiante.getId(); }

    public void setEstudiante(Estudiante e)  { this.estudiante = e; }
    public void setIzquierda(NodoAVL n)      { this.izquierda  = n; }
    public void setDerecha(NodoAVL n)        { this.derecha    = n; }
    public void setPadre(NodoAVL n)          { this.padre      = n; }
    public void setAltura(int a)             { this.altura     = a; }

    public int getFactorBalance() {
        int altIzq = (izquierda != null) ? izquierda.getAltura() : 0;
        int altDer = (derecha   != null) ? derecha.getAltura()   : 0;
        return altDer - altIzq;
    }

    public void recalcularAltura() {
        int altIzq = (izquierda != null) ? izquierda.getAltura() : 0;
        int altDer = (derecha   != null) ? derecha.getAltura()   : 0;
        this.altura = 1 + Math.max(altIzq, altDer);
    }
}

