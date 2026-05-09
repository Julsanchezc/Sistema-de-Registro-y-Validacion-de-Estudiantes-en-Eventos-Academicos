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
    // REGISTRO CON MENSAJES (uso interactivo)
    // =========================================================
    public boolean registrarEstudiante(int id, String nombre, String correo, String programa) {
        if (!validarDatos(id, nombre, correo)) return false;

        if (arbolEstudiantes.existe(id)) {
            System.out.println(Colores.error("✘ Ya existe un estudiante con ID " + id));
            return false;
        }

        if (arbolEstudiantes.getCantidadEstudiantes() >= capacidadMaxima) {
            if (existeEnCola(id)) {
                System.out.println(Colores.error("✘ Ya esta en la cola de espera (ID " + id + ")"));
                return false;
            }
            Estudiante enCola = new Estudiante(id, nombre, correo, programa);
            colaEspera.encolar(enCola);
            historial.registrar("COLA",
                    "ID:" + id + " - " + nombre + " (pos." + colaEspera.getTamanio() + ")");
            System.out.println(Colores.warn("⌛ Evento lleno. " + nombre
                    + " agregado a la cola (posicion " + colaEspera.getTamanio() + ")."));
            return false;
        }

        Estudiante nuevo    = new Estudiante(id, nombre, correo, programa);
        boolean   insertado = arbolEstudiantes.insertar(nuevo);
        if (insertado) {
            historial.registrar("REGISTRO", "ID:" + id + " - " + nombre);
            System.out.println(Colores.ok("✔ Registrado: " + nombre + " (ID: " + id + ")"));
        }
        return insertado;
    }

    // =========================================================
    // CONSULTAS
    // =========================================================
    public boolean    verificarEstudiante(int id) { return arbolEstudiantes.existe(id); }
    public Estudiante obtenerEstudiante(int id)   { return arbolEstudiantes.buscar(id); }

    public boolean marcarAsistencia(int id) {
        Estudiante est = arbolEstudiantes.buscar(id);
        if (est != null) {
            est.setAsistencia(true);
            historial.registrar("ASISTENCIA", "ID:" + id + " - " + est.getNombre());
            System.out.println(Colores.ok("✔ Asistencia marcada: " + est.getNombre()));
            return true;
        }
        System.out.println(Colores.error("✘ ID " + id + " no encontrado"));
        return false;
    }

    // =========================================================
    // COLA DE ESPERA
    // =========================================================
    public void mostrarColaEspera() {
        if (colaEspera.estaVacia()) {
            System.out.println(Colores.info("  Cola de espera vacia"));
            return;
        }
        System.out.println(Colores.titulo(
                "\n╔══════ COLA DE ESPERA ══════════════════════════════╗"));
        System.out.printf(Colores.CYAN + "║  %d estudiante(s) en espera" + Colores.RESET + "%n",
                colaEspera.getTamanio());
        System.out.println(Colores.titulo(
                "╠════════════════════════════════════════════════════╣"));
        int pos = 1;
        for (Object obj : colaEspera.contenido()) {
            Estudiante e = (Estudiante) obj;
            System.out.printf("  %2d. ID:%-8d | %-25s | %s%n",
                    pos++, e.getId(), e.getNombre(), e.getPrograma());
        }
        System.out.println(Colores.titulo(
                "╚════════════════════════════════════════════════════╝\n"));
    }

    // =========================================================
    // HISTORIAL
    // =========================================================
    public void mostrarHistorial() {
        System.out.println(Colores.titulo(
                "\n╔══════ HISTORIAL DE OPERACIONES ════════════════════╗"));
        System.out.printf(Colores.CYAN + "║  %d operacion(es) registradas (LIFO)"
                + Colores.RESET + "%n", historial.getTamanio());
        System.out.println(Colores.titulo(
                "╠════════════════════════════════════════════════════╣"));
        historial.mostrar();
        System.out.println(Colores.titulo(
                "╚════════════════════════════════════════════════════╝\n"));
    }

    // =========================================================
    // GETTERS
    // =========================================================
    public String  getNombreEvento()        { return nombreEvento; }
    public int     getCantidadEstudiantes() { return arbolEstudiantes.getCantidadEstudiantes(); }
    public int     getTamanoColaEspera()    { return colaEspera.getTamanio(); }
    public int     getTamanoHistorial()     { return historial.getTamanio(); }
    public int     getCapacidad()           { return capacidadMaxima; }
    public boolean estaVacio()              { return arbolEstudiantes.estaVacio(); }

    // =========================================================
    // VALIDACION INTERNA
    // =========================================================
    private boolean validarDatos(int id, String nombre, String correo) {
        if (id <= 0)
            { System.out.println(Colores.error("✘ ID invalido"));     return false; }
        if (nombre == null || nombre.isBlank())
            { System.out.println(Colores.error("✘ Nombre vacio"));    return false; }
        if (correo == null || !correo.contains("@"))
            { System.out.println(Colores.error("✘ Correo invalido")); return false; }
        return true;
    }

    private boolean existeEnCola(int id) {
        for (Object obj : colaEspera.contenido()) {
            if (((Estudiante) obj).getId() == id) return true;
        }
        return false;
    }
}
