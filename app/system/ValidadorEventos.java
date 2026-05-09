package system;

import structures.ArbolAVL;
import structures.Cola;
import structures.PilaHistorial;
import model.Estudiante;
import ui.Colores;
import ui.Consola;

/**
 * ValidadorEventos.java
 * Modulo de alto nivel que gestiona el registro y validacion
 * de estudiantes en un evento academico.
 *
 * Estructuras de datos utilizadas:
 *   - ArbolAVL          : almacenamiento principal de estudiantes  O(log n)
 *   - Cola<Estudiante>  : lista de espera cuando el aforo esta completo (FIFO)
 *   - PilaHistorial     : registro de operaciones con soporte de deshacer (LIFO)
 */
public class ValidadorEventos {

    private ArbolAVL              arbolEstudiantes;
    private String                nombreEvento;
    private int                   capacidadMaxima;
    private Cola<Estudiante>      colaEspera;
    private PilaHistorial         historial;

    public ValidadorEventos(String nombreEvento, int capacidadMaxima) {
        this.arbolEstudiantes = new ArbolAVL();
        this.nombreEvento     = nombreEvento;
        this.capacidadMaxima  = capacidadMaxima;
        this.colaEspera       = new Cola<>();
        this.historial        = new PilaHistorial();
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public String  getNombreEvento()        { return nombreEvento; }
    public int     getCantidadEstudiantes() { return arbolEstudiantes.getCantidadEstudiantes(); }
    public int     getCapacidad()           { return capacidadMaxima; }
    public boolean estaVacio()              { return arbolEstudiantes.estaVacio(); }

    // =========================================================
    // VALIDACION INTERNA
    // =========================================================
    private boolean validarDatos(int id, String nombre, String correo) {
        if (id <= 0)
            { System.out.println(Colores.error("ID invalido"));     return false; }
        if (nombre == null || nombre.isBlank())
            { System.out.println(Colores.error("Nombre vacio"));    return false; }
        if (correo == null || !correo.contains("@"))
            { System.out.println(Colores.error("Correo invalido")); return false; }
        return true;
    }

    private boolean existeEnCola(int id) {
        for (Object obj : colaEspera.contenido()) {
            if (((Estudiante) obj).getId() == id) return true;
        }
        return false;
    }
}
