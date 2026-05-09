package model;

/**
 * Estudiante.java
 * Modelo de datos para un estudiante del sistema.
 */
public class Estudiante {

    private int     id;
    private String  nombre;
    private String  correo;
    private String  programa;
    private boolean asistencia;

    public Estudiante(int id, String nombre, String correo, String programa) {
        this.id         = id;
        this.nombre     = nombre;
        this.correo     = correo;
        this.programa   = programa;
        this.asistencia = false;
    }
