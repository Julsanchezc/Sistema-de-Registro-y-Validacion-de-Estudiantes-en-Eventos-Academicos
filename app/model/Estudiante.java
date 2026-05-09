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
    public int     getId()                    { return id; }
    public String  getNombre()                { return nombre; }
    public String  getCorreo()                { return correo; }
    public String  getPrograma()              { return programa; }
    public boolean isAsistencia()             { return asistencia; }
    public void    setAsistencia(boolean a)   { this.asistencia = a; }

    @Override
    public String toString() {
        return String.format("  ID: %-8d | %-25s | %-30s | %-20s | Asistencia: %s",
                id, nombre, correo, programa, asistencia ? "Si" : "No");
    }
}

