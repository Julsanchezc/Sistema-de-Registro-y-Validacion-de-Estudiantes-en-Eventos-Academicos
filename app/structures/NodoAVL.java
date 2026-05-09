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
