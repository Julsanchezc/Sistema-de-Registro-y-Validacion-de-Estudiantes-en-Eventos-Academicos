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

    private ArbolAVL              arbolEstudiantes; // almacenamiento principal O(log n)
    private String                nombreEvento;
    private int                   capacidadMaxima;
    private Cola<Estudiante>      colaEspera;       // lista de espera FIFO cuando el aforo esta lleno
    private PilaHistorial         historial;        // registro de operaciones LIFO (soporta deshacer)

    // Inicializa el evento con su nombre y capacidad maxima de inscritos.
    // Crea las tres estructuras internas: AVL para inscritos, Cola de espera
    // e historial de operaciones.
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

    // Registra un estudiante con validacion completa y mensajes en consola.
    // Si el aforo esta lleno lo encola automaticamente en lugar de rechazarlo.
    // Retorna true si fue insertado en el evento, false en cualquier otro caso.
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
    // REGISTRO BULK – silencioso, con soporte de cola
    // Retorna: 1=registrado en evento, 0=en cola de espera, -1=error/duplicado
    // =========================================================

    // Version silenciosa de registrarEstudiante para carga masiva desde archivos.
    // No imprime mensajes en consola para no saturar la salida durante un bulk-add.
    // Los codigos de retorno permiten al llamador contabilizar cuantos fueron al
    // evento, cuantos a la cola y cuantos fallaron, sin parsear mensajes de texto.
    public int registrarBulk(int id, String nombre, String correo, String programa) {
        if (!validarDatos(id, nombre, correo)) return -1;
        if (arbolEstudiantes.existe(id))       return -1;

        if (arbolEstudiantes.getCantidadEstudiantes() >= capacidadMaxima) {
            if (existeEnCola(id)) return -1;
            colaEspera.encolar(new Estudiante(id, nombre, correo, programa));
            historial.registrar("COLA",
                    "ID:" + id + " - " + nombre + " (bulk pos." + colaEspera.getTamanio() + ")");
            return 0;
        }

        Estudiante nuevo = new Estudiante(id, nombre, correo, programa);
        if (arbolEstudiantes.insertar(nuevo)) {
            historial.registrar("REGISTRO", "ID:" + id + " - " + nombre + " (bulk)");
            return 1;
        }
        return -1;
    }

    // =========================================================
    // REGISTRO SILENCIOSO – solo para pruebas de rendimiento
    // Sin prints, sin historial, sin cola
    // =========================================================

    // Inserta directamente en el AVL sin tocar el historial ni la cola.
    // Diseñado para MedidorRendimiento: eliminar el overhead de prints y
    // estructuras auxiliares garantiza que solo se mide el AVL puro.
    public boolean registrarSilencioso(Estudiante est) {
        if (arbolEstudiantes.getCantidadEstudiantes() >= capacidadMaxima) return false;
        return arbolEstudiantes.insertar(est);
    }

    // =========================================================
    // CARGA DIRECTA (restauracion desde archivo)
    // =========================================================

    // Restaura un estudiante desde el archivo de persistencia, conservando
    // su estado de asistencia original. No valida duplicados ni verifica
    // capacidad porque GestorEventos garantiza que los datos del archivo son
    // consistentes antes de llamar este metodo.
    public void cargarEstudiante(int id, String nombre, String correo,
                                 String programa, boolean asistencia) {
        Estudiante e = new Estudiante(id, nombre, correo, programa);
        e.setAsistencia(asistencia);
        arbolEstudiantes.insertar(e);
    }

    // Restaura un estudiante en la cola de espera al cargar el archivo guardado.
    public void cargarEnCola(int id, String nombre, String correo, String programa) {
        colaEspera.encolar(new Estudiante(id, nombre, correo, programa));
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    // Verifica si un ID esta inscrito en el evento (O(log n) sobre el AVL).
    public boolean    verificarEstudiante(int id) { return arbolEstudiantes.existe(id); }
    // Retorna el objeto Estudiante completo, o null si el ID no existe.
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
    // ELIMINACION
    // =========================================================

    // Elimina el estudiante del AVL, guarda su objeto en el historial para
    // permitir deshacer, y dispara la promocion automatica desde la cola.
    public boolean eliminarEstudiante(int id) {
        Estudiante est = arbolEstudiantes.buscar(id);
        boolean    ok  = arbolEstudiantes.eliminar(id);
        if (ok) {
            historial.registrar("ELIMINACION", "ID:" + id + " - " + est.getNombre(), est);
            System.out.println(Colores.ok("✔ Eliminado (ID: " + id + ")"));
            promoverDeCola();
        } else {
            System.out.println(Colores.error("✘ No encontrado"));
        }
        return ok;
    }

    // Version sin efectos secundarios para pruebas de rendimiento.
    // No actualiza historial ni cola; solo mide la velocidad del AVL.
    public boolean eliminarSilencioso(int id) {
        return arbolEstudiantes.eliminar(id);
    }

    // =========================================================
    // PROMOCION AUTOMATICA DESDE COLA
    // =========================================================

    // Saca al primero en la cola FIFO y lo inscribe en el evento cada vez
    // que se libera un cupo (tras eliminarEstudiante o deshacerUltimaEliminacion).
    // Garantiza que nadie en espera se salte injustamente el orden de llegada.
    private void promoverDeCola() {
        if (colaEspera.estaVacia()) return;
        Estudiante promovido = colaEspera.desencolar();
        arbolEstudiantes.insertar(promovido);
        historial.registrar("PROMOCION",
                "ID:" + promovido.getId() + " - " + promovido.getNombre() + " (promovido)");
        System.out.println(Colores.info(
                "  ► Promovido desde cola: " + promovido.getNombre()
                + " (ID: " + promovido.getId() + ")"));
    }

    // =========================================================
    // DESHACER ULTIMA ELIMINACION
    // =========================================================

    // Restaura el ultimo estudiante eliminado usando el objeto guardado en el historial.
    // Casos especiales:
    //   - Si el tope del historial no es una ELIMINACION, avisa y no hace nada.
    //   - Si el evento esta lleno al restaurar, el estudiante va a la cola en lugar
    //     de ser rechazado, para no perder el dato del deshacer.
    public void deshacerUltimaEliminacion() {
        PilaHistorial.Registro r = historial.verTope();
        if (r == null) {
            System.out.println(Colores.error("✘ No hay operaciones que deshacer"));
            return;
        }
        if (!r.tipo.equals("ELIMINACION") || r.datosUndo == null) {
            System.out.println(Colores.warn(
                    "⚠  La ultima operacion [" + r.tipo + "] no es una eliminacion."));
            return;
        }
        historial.desapilar();
        Estudiante est = r.datosUndo;
        if (arbolEstudiantes.getCantidadEstudiantes() >= capacidadMaxima) {
            System.out.println(Colores.warn(
                    "⚠  No hay espacio para restaurar. " + est.getNombre() + " va a la cola."));
            colaEspera.encolar(est);
            historial.registrar("COLA",
                    "ID:" + est.getId() + " - " + est.getNombre() + " (undo->cola)");
            return;
        }
        if (arbolEstudiantes.insertar(est)) {
            historial.registrar("DESHACER",
                    "Restaurado ID:" + est.getId() + " - " + est.getNombre());
            System.out.println(Colores.ok(
                    "✔ Deshecho: " + est.getNombre() + " (ID: " + est.getId() + ") restaurado."));
        } else {
            System.out.println(Colores.error(
                    "✘ No se pudo restaurar (ID " + est.getId() + " ya existe)."));
        }
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
    // BORRAR LISTA COMPLETA
    // =========================================================

    // Descarta el AVL y la cola de espera creando nuevas instancias vacias.
    // El historial se mantiene para que quede registro del borrado masivo.
    public void borrarLista() {
        int n = arbolEstudiantes.getCantidadEstudiantes();
        arbolEstudiantes = new ArbolAVL();
        colaEspera       = new Cola<>();
        historial.registrar("BORRADO", "Se eliminaron " + n + " estudiantes y se limpio la cola");
        System.out.println(Colores.ok("✔ Lista borrada. Se eliminaron " + n + " estudiantes."));
    }

    // =========================================================
    // EXPORTAR LISTA A CSV
    // =========================================================
    public void exportarCSV(String path) {
        if (arbolEstudiantes.estaVacio()) {
            System.out.println(Colores.warn("⚠  No hay estudiantes para exportar."));
            return;
        }
        java.io.File archivo = new java.io.File(path);
        if (archivo.getParentFile() != null) archivo.getParentFile().mkdirs();
        try (java.io.PrintWriter pw =
                     new java.io.PrintWriter(new java.io.FileWriter(archivo))) {
            pw.println("id,nombre,correo,programa,asistencia");
            arbolEstudiantes.escribirInorden(pw, "CSV");
            historial.registrar("EXPORTACION", "CSV -> " + archivo.getName());
            System.out.println(Colores.ok("✔ Exportado (" + getCantidadEstudiantes()
                    + " estudiantes): " + archivo.getAbsolutePath()));
        } catch (java.io.IOException e) {
            System.out.println(Colores.error("✘ Error al exportar: " + e.getMessage()));
        }
    }

    // =========================================================
    // ESCRIBIR PARA PERSISTENCIA (llamado por GestorEventos)
    // =========================================================

    // Serializa el evento al formato de texto propio del sistema:
    //   EVENTO|nombre|capacidad
    //   ESTUDIANTE|id|nombre|correo|programa|asistencia   (inorden del AVL)
    //   COLA|id|nombre|correo|programa                    (en orden FIFO)
    // GestorEventos llama este metodo para cada evento al guardar el archivo.
    public void escribirEnArchivo(java.io.PrintWriter pw) {
        pw.println("EVENTO|" + nombreEvento + "|" + capacidadMaxima);
        arbolEstudiantes.escribirInorden(pw, "PERSIST");
        for (Object obj : colaEspera.contenido()) {
            Estudiante e = (Estudiante) obj;
            pw.println("COLA|" + e.getId() + "|" + e.getNombre()
                    + "|" + e.getCorreo() + "|" + e.getPrograma());
        }
    }

    // =========================================================
    // REPORTES
    // =========================================================

    // Muestra el resumen del evento con barras de ocupacion para inscritos
    // y asistencia. Util como vista rapida antes de iniciar la sesion.
    public void imprimirEstado() {
        int inscritos  = getCantidadEstudiantes();
        int asistieron = arbolEstudiantes.contarAsistencias();

        System.out.println(Colores.titulo(
                "\n╔══════ ESTADO DEL EVENTO ═══════════════════════════╗"));
        System.out.printf(Colores.CYAN + "║  Evento    : " + Colores.CYAN_B + "%-37s"
                + Colores.RESET + "%n", nombreEvento);
        System.out.printf(Colores.CYAN + "║  Capacidad : " + Colores.RESET + "%-37d%n",
                capacidadMaxima);
        System.out.printf(Colores.CYAN + "║  Altura AVL: " + Colores.RESET + "%-37d%n",
                obtenerAltura());
        System.out.printf(Colores.CYAN + "║  Cola esp. : " + Colores.RESET + "%-37d%n",
                colaEspera.getTamanio());
        System.out.printf(Colores.CYAN + "║  Historial : " + Colores.RESET + "%d ops%n",
                historial.getTamanio());
        System.out.println(Colores.titulo(
                "╠════════════════════════════════════════════════════╣"));
        Consola.imprimirBarraOcupacion("Inscritos", inscritos, capacidadMaxima, 22);
        Consola.imprimirBarraOcupacion("Con asistencia", asistieron, inscritos, 22);
        System.out.println(Colores.titulo(
                "╚════════════════════════════════════════════════════╝\n"));
    }

    // Lista todos los inscritos en orden ascendente por ID (recorrido inorden del AVL).
    public void listarEstudiantes() {
        if (arbolEstudiantes.estaVacio()) {
            System.out.println(Colores.warn("  Sin estudiantes registrados"));
            return;
        }
        System.out.println(Colores.titulo(
                "\n  LISTA DE ESTUDIANTES – orden ascendente por ID"));
        Consola.imprimirTablaEstudiantes(arbolEstudiantes.coleccionarInorden());
    }

    public void visualizarArbol() { arbolEstudiantes.visualizar(); }

    // =========================================================
    // GETTERS
    // =========================================================
    public String  getNombreEvento()        { return nombreEvento; }
    public int     getCantidadEstudiantes() { return arbolEstudiantes.getCantidadEstudiantes(); }
    public int     getCantidadAsistencias() { return arbolEstudiantes.contarAsistencias(); }
    public int     getTamanoColaEspera()    { return colaEspera.getTamanio(); }
    public int     getTamanoHistorial()     { return historial.getTamanio(); }
    public int     obtenerAltura()          { return arbolEstudiantes.getAltura(); }
    public int     getCapacidad()           { return capacidadMaxima; }
    public boolean estaVacio()              { return arbolEstudiantes.estaVacio(); }

    public double getPorcentajeOcupacion() {
        return (double) getCantidadEstudiantes() / capacidadMaxima * 100;
    }

    // =========================================================
    // VALIDACION INTERNA
    // =========================================================

    // Valida los campos minimos antes de cualquier insercion:
    //   id > 0, nombre no vacio, correo contiene '@'.
    // Centralizar aqui evita duplicar las mismas reglas en registrarEstudiante
    // y registrarBulk.
    private boolean validarDatos(int id, String nombre, String correo) {
        if (id <= 0)
            { System.out.println(Colores.error("✘ ID invalido"));     return false; }
        if (nombre == null || nombre.isBlank())
            { System.out.println(Colores.error("✘ Nombre vacio"));    return false; }
        if (correo == null || !correo.contains("@"))
            { System.out.println(Colores.error("✘ Correo invalido")); return false; }
        return true;
    }

    // Busqueda lineal O(n) sobre la cola para evitar duplicados en espera.
    // La cola raramente supera unos pocos cientos de elementos, por lo que
    // el costo es despreciable comparado con la insercion en el AVL.
    private boolean existeEnCola(int id) {
        for (Object obj : colaEspera.contenido()) {
            if (((Estudiante) obj).getId() == id) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        int    ins  = getCantidadEstudiantes();
        int    cola = colaEspera.getTamanio();
        double pct  = getPorcentajeOcupacion();
        String colaStr = cola > 0
                ? "  " + Colores.MAGENTA + "cola:" + cola + Colores.RESET : "";
        return String.format(
                Colores.CYAN_B + "%-35s" + Colores.RESET
                + " " + Colores.VERDE_B + "%3d" + Colores.RESET
                + "/%d  (%.0f%%)" + "%s",
                nombreEvento, ins, capacidadMaxima, pct, colaStr);
    }
}
