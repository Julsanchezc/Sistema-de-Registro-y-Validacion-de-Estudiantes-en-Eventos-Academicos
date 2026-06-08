package eventos.ui.console;

import core.AppPaths;
import core.exceptions.PersistenceException;
import eventos.model.Student;
import eventos.repository.EventRepository;
import eventos.service.EventManager;
import eventos.service.EventService;
import eventos.service.PerformanceService;
import eventos.service.RegisterResult;
import eventos.service.UndoResult;

import java.util.Scanner;

public class ConsoleApp {

    private final EventManager    manager    = new EventManager();
    private final EventRepository repository = new EventRepository();
    private final Scanner         sc         = new Scanner(System.in);

    private static final String[] NAMES = {
        "Ana Garcia",      "Carlos Lopez",    "Diana Perez",     "Eduardo Gomez",   "Francisca Ruiz",
        "Gabriel Torres",  "Helena Morales",  "Ivan Castro",     "Julia Vargas",    "Kevin Reyes",
        "Laura Mendoza",   "Miguel Herrera",  "Natalia Flores",  "Oscar Romero",    "Paula Diaz",
        "Rodrigo Ortiz",   "Sofia Molina",    "Tomas Guerrero",  "Valentina Cruz",  "Wilmer Rios",
        "Alejandra Nunez", "Bryan Salazar",   "Camila Jimenez",  "Daniel Rojas",    "Estefania Vega",
        "Felipe Ramos",    "Gloria Medina",   "Hugo Sandoval",   "Isabella Pena",   "Jorge Aguilar"
    };

    private static final String[] PROGRAMS = {
        "Ing. Sistemas",  "Ing. Industrial", "Economia",        "Medicina",        "Ing. Civil",
        "Matematicas",    "Fisica",          "Quimica",         "Biologia",        "Derecho",
        "Administracion", "Arquitectura",    "Psicologia",      "Sociologia",      "Estadistica"
    };

    public void run() {
        printHeader();
        mainMenu();
        System.out.println("\n" + Colors.info("Programa finalizado."));
        sc.close();
    }

    private void printHeader() {
        System.out.println();
        System.out.println(Colors.title("╔══════════════════════════════════════════════════════╗"));
        System.out.println(Colors.title("║  SISTEMA DE REGISTRO Y VALIDACION DE ESTUDIANTES     ║"));
        System.out.println(Colors.title("║       EN EVENTOS ACADEMICOS — ARBOL AVL              ║"));
        System.out.println(Colors.title("║       Estructuras de Datos 2016699 · UNAL 2026       ║"));
        System.out.println(Colors.title("╚══════════════════════════════════════════════════════╝"));
        System.out.println();
        System.out.println("  Estructuras activas: "
                + Colors.bold("AVL") + "  " + Colors.bold("Lista enlazada") + "  "
                + Colors.bold("Cola") + "  " + Colors.bold("Pila") + "  " + Colors.bold("BST"));
        System.out.println();
    }

    private void mainMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println();
            System.out.println(Colors.title("╔══════════════════ GESTOR DE EVENTOS ══════════════════╗"));
            System.out.printf(Colors.CYAN + "║  Eventos activos: " + Colors.CYAN_B + "%-3d" + Colors.RESET + "%n",
                    manager.getEventCount());
            System.out.println(Colors.title("╠═══════════════════════════════════════════════════════╣"));
            System.out.println("║  [1] Crear nuevo evento                               ║");
            System.out.println("║  [2] Acceder a un evento                              ║");
            System.out.println("║  [3] Listar todos los eventos                         ║");
            System.out.println("║  [4] Eliminar un evento                               ║");
            System.out.println("║  [5] Guardar datos en archivo                         ║");
            System.out.println("║  [6] Cargar datos desde archivo                       ║");
            System.out.println("║  [7] Analisis de rendimiento (AVL vs BST)             ║");
            System.out.println("║  [0] Salir                                            ║");
            System.out.println(Colors.title("╚═══════════════════════════════════════════════════════╝"));
            System.out.print("Opcion: ");

            switch (readInt()) {
                case 1: createEvent();      break;
                case 2: enterEvent();       break;
                case 3: listEvents();       break;
                case 4: deleteEvent();      break;
                case 5: saveData();         break;
                case 6: loadData();         break;
                case 7: performanceMenu();  break;
                case 0: exit = true;        break;
                default: System.out.println(Colors.error("❌ Opcion invalida"));
            }
        }
    }

    private void createEvent() {
        System.out.print("Nombre del evento : ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println(Colors.error("❌ Nombre vacio")); return; }
        System.out.print("Capacidad maxima  : ");
        int cap = readInt();
        if (cap <= 0) { System.out.println(Colors.error("❌ Capacidad invalida")); return; }
        manager.createEvent(name, cap);
        System.out.println(Colors.ok("✓ Evento creado: \"" + name + "\"  (capacidad: " + cap + ")"));
    }

    private void listEvents() {
        EventService[] events = manager.getEvents();
        if (events.length == 0) {
            System.out.println(Colors.warn("  (No hay eventos creados)"));
            return;
        }
        System.out.println(Colors.title(
                "\n╔══════ EVENTOS REGISTRADOS ═══════════════════════════════════════╗"));
        for (int i = 0; i < events.length; i++) {
            EventService ev   = events[i];
            int    count  = ev.getStudentCount();
            int    cap    = ev.getCapacity();
            double pct    = ev.getOccupancyRate();
            int    queue  = ev.getQueueSize();
            String queueStr = queue > 0 ? "  " + Colors.MAGENTA + "cola:" + queue + Colors.RESET : "";
            System.out.printf(Colors.CYAN + "║  [" + Colors.CYAN_B + "%d" + Colors.CYAN + "] "
                    + Colors.CYAN_B + "%-35s" + Colors.RESET
                    + " " + Colors.GREEN_B + "%3d" + Colors.RESET + "/%d  (%.0f%%)%s%n",
                    i + 1, ev.getName(), count, cap, pct, queueStr);
        }
        System.out.println(Colors.title("╚══════════════════════════════════════════════════════════════╝\n"));
    }

    private void enterEvent() {
        if (!manager.hasEvents()) {
            System.out.println(Colors.error("❌ No hay eventos. Crea uno primero (opcion 1)."));
            return;
        }
        listEvents();
        System.out.print("Numero de evento: ");
        int idx = readInt();
        EventService event = manager.getEvent(idx);
        if (event == null) { System.out.println(Colors.error("❌ Numero invalido")); return; }
        eventMenu(event);
    }

    private void deleteEvent() {
        if (!manager.hasEvents()) { System.out.println(Colors.error("❌ No hay eventos")); return; }
        listEvents();
        System.out.print("Numero de evento a eliminar: ");
        int idx = readInt();
        EventService ev = manager.getEvent(idx);
        if (ev == null) { System.out.println(Colors.error("❌ Numero invalido")); return; }
        System.out.print("¿Confirmar eliminar \"" + ev.getName() + "\"? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            manager.removeEvent(idx);
            System.out.println(Colors.ok("✓ Evento eliminado"));
        } else { System.out.println("Cancelado."); }
    }

    private void saveData() {
        System.out.print("Ruta del archivo [ENTER = " + AppPaths.Files.EVENTS_JSON + "]: ");
        String input = sc.nextLine().trim();
        java.nio.file.Path path = input.isEmpty()
                ? AppPaths.Files.EVENTS_JSON
                : java.nio.file.Paths.get(input);
        try {
            repository.save(manager, path);
            System.out.println(Colors.ok("✓ Datos guardados en: " + path.toAbsolutePath()));
        } catch (PersistenceException e) {
            System.out.println(Colors.error("❌ Error al guardar: " + e.getMessage()));
        }
    }

    private void loadData() {
        System.out.print("Ruta del archivo [ENTER = " + AppPaths.Files.EVENTS_JSON + "]: ");
        String input = sc.nextLine().trim();
        java.nio.file.Path path = input.isEmpty()
                ? AppPaths.Files.EVENTS_JSON
                : java.nio.file.Paths.get(input);
        System.out.print(Colors.warn("⚠ Cargar reemplazara los datos actuales. Continuar? (s/n): "));
        if (!sc.nextLine().trim().equalsIgnoreCase("s")) { System.out.println("Cancelado."); return; }
        try {
            repository.load(manager, path);
            System.out.println(Colors.ok("✓ Datos cargados desde: " + path.toAbsolutePath()));
        } catch (PersistenceException e) {
            System.out.println(Colors.error("❌ Error al cargar: " + e.getMessage()));
        }
    }

    private void eventMenu(EventService event) {
        boolean exit = false;
        while (!exit) {
            int count = event.getStudentCount();
            int cap   = event.getCapacity();
            int queue = event.getQueueSize();
            int hist  = event.getHistorySize();
            int att   = event.getAttendanceCount();

            System.out.println();
            System.out.println(Colors.title("╔══════════════════════════════════════════════════════════╗"));
            System.out.println(Colors.CYAN + Colors.BOLD + "║  EVENTO: " + event.getName() + Colors.RESET);
            System.out.print("║  ");
            System.out.println(Terminal.progressBar(count, cap, 32)
                    + "  " + Colors.CYAN_B + count + "/" + cap + " inscritos" + Colors.RESET);
            System.out.printf(Colors.CYAN
                            + "║  Asistencia: " + Colors.YELLOW_B + "%d"
                            + Colors.CYAN + "  │  Cola: " + Colors.MAGENTA + "%d"
                            + Colors.CYAN + "  │  Historial: %d ops" + Colors.RESET + "%n",
                    att, queue, hist);
            System.out.println(Colors.title("╠══════════════════════════════════════════════════════════╣"));
            System.out.println("║  [1]  Registrar estudiante                               ║");
            System.out.println("║  [2]  Consultar por ID                                   ║");
            System.out.println("║  [3]  Marcar asistencia                                  ║");
            System.out.println("║  [4]  Eliminar estudiante                                ║");
            System.out.println("║  [5]  Deshacer ultima eliminacion                        ║");
            System.out.println("║  [6]  Listar estudiantes (tabla)                         ║");
            System.out.println("║  [7]  Ver cola de espera                                 ║");
            System.out.println("║  [8]  Historial de operaciones                           ║");
            System.out.println("║  [9]  Estado del evento                                  ║");
            System.out.println("║  [10] Visualizar arbol AVL                               ║");
            System.out.println("║  [11] Agregar estudiantes aleatorios                     ║");
            System.out.println("║  [12] Exportar lista a CSV                               ║");
            System.out.println("║  [13] BORRAR LISTA COMPLETA                              ║");
            System.out.println("║  [0]  Volver al gestor                                   ║");
            System.out.println(Colors.title("╚══════════════════════════════════════════════════════════╝"));
            System.out.print("Opcion: ");

            switch (readInt()) {
                case 1:  register(event);            break;
                case 2:  lookup(event);              break;
                case 3:  markAttendance(event);      break;
                case 4:  removeStudent(event);       break;
                case 5:  undoRemoval(event);         break;
                case 6:  listStudents(event);        break;
                case 7:  showQueue(event);           break;
                case 8:  showHistory(event);         break;
                case 9:  showStatus(event);          break;
                case 10: event.visualizeTree();      break;
                case 11: addRandom(event);           break;
                case 12: exportCSV(event);           break;
                case 13: clearList(event);           break;
                case 0:  exit = true;                break;
                default: System.out.println(Colors.error("❌ Opcion invalida"));
            }
        }
    }

    private void register(EventService e) {
        System.out.print("ID institucional : "); int    id      = readInt();
        System.out.print("Nombre completo  : "); String name    = sc.nextLine().trim();
        System.out.print("Correo           : "); String email   = sc.nextLine().trim();
        System.out.print("Programa         : "); String program = sc.nextLine().trim();

        RegisterResult r = e.registerStudent(id, name, email, program);
        switch (r) {
            case REGISTERED        -> System.out.println(Colors.ok("✔ Registrado: " + name + " (ID: " + id + ")"));
            case QUEUED            -> System.out.println(Colors.warn("⌛ Evento lleno. " + name
                                        + " en cola (posicion " + e.getQueueSize() + ")."));
            case DUPLICATE_ID      -> System.out.println(Colors.error("✘ Ya existe un estudiante con ID " + id));
            case DUPLICATE_IN_QUEUE-> System.out.println(Colors.error("✘ Ya esta en la cola de espera (ID " + id + ")"));
            case INVALID_DATA      -> System.out.println(Colors.error("✘ Datos invalidos (ID>0, nombre y correo con @)"));
        }
    }

    private void lookup(EventService e) {
        System.out.print("ID a buscar: ");
        int id = readInt();
        Student s = e.findStudent(id);
        if (s != null) System.out.println(Colors.ok("✓ Encontrado:") + "\n" + s);
        else           System.out.println(Colors.error("❌ No existe un estudiante con ID " + id));
    }

    private void markAttendance(EventService e) {
        System.out.print("ID del estudiante: ");
        int id = readInt();
        if (e.markAttendance(id)) System.out.println(Colors.ok("✔ Asistencia marcada para ID " + id));
        else                      System.out.println(Colors.error("✘ ID " + id + " no encontrado"));
    }

    private void removeStudent(EventService e) {
        System.out.print("ID a eliminar: ");
        int id = readInt();
        if (!e.hasStudent(id)) { System.out.println(Colors.error("❌ No encontrado")); return; }
        System.out.print(Colors.warn("¿Confirmar eliminar ID " + id + "? (s/n): "));
        if (!sc.nextLine().trim().equalsIgnoreCase("s")) { System.out.println("Cancelado."); return; }
        if (e.removeStudent(id)) {
            System.out.println(Colors.ok("✔ Eliminado (ID: " + id + ")"));
            Student promoted = e.getLastPromoted();
            if (promoted != null)
                System.out.println(Colors.info("  ► Promovido desde cola: "
                        + promoted.getName() + " (ID: " + promoted.getId() + ")"));
        }
    }

    private void undoRemoval(EventService e) {
        String last = e.getLastHistoryEntry() != null
                ? " [ultimo: " + e.getLastHistoryEntry().type + "]" : "";
        UndoResult r = e.undoLastRemoval();
        switch (r) {
            case UNDONE          -> System.out.println(Colors.ok("✔ Eliminacion deshecha correctamente."));
            case UNDONE_TO_QUEUE -> System.out.println(Colors.warn("⚠  Restaurado pero movido a cola (evento lleno)."));
            case NO_OPERATIONS   -> System.out.println(Colors.error("✘ No hay operaciones que deshacer."));
            case NOT_A_REMOVAL   -> System.out.println(Colors.warn("⚠  La ultima operacion" + last + " no es una eliminacion."));
            case ERROR           -> System.out.println(Colors.error("✘ No se pudo restaurar (ID ya existe)."));
        }
    }

    private void listStudents(EventService e) {
        if (e.isEmpty()) { System.out.println(Colors.warn("  Sin estudiantes registrados")); return; }
        System.out.println(Colors.title("\n  LISTA DE ESTUDIANTES – orden ascendente por ID"));
        Terminal.printStudentTable(e.getStudentsSorted());
    }

    private void showQueue(EventService e) {
        Object[] contents = e.getQueueContents();
        if (contents.length == 0) { System.out.println(Colors.info("  Cola de espera vacia")); return; }
        System.out.println(Colors.title("\n╔══════ COLA DE ESPERA ══════════════════════════════╗"));
        System.out.printf(Colors.CYAN + "║  %d estudiante(s) en espera" + Colors.RESET + "%n", contents.length);
        System.out.println(Colors.title("╠════════════════════════════════════════════════════╣"));
        for (int i = 0; i < contents.length; i++) {
            Student s = (Student) contents[i];
            System.out.printf("  %2d. ID:%-8d | %-25s | %s%n", i + 1, s.getId(), s.getName(), s.getProgram());
        }
        System.out.println(Colors.title("╚════════════════════════════════════════════════════╝\n"));
    }

    private void showHistory(EventService e) {
        System.out.println(Colors.title("\n╔══════ HISTORIAL DE OPERACIONES ════════════════════╗"));
        System.out.printf(Colors.CYAN + "║  %d operacion(es) registradas (LIFO)" + Colors.RESET + "%n",
                e.getHistorySize());
        System.out.println(Colors.title("╠════════════════════════════════════════════════════╣"));
        Terminal.printHistory(e.getHistoryEntries());
        System.out.println(Colors.title("╚════════════════════════════════════════════════════╝\n"));
    }

    private void showStatus(EventService e) {
        int count = e.getStudentCount();
        int att   = e.getAttendanceCount();
        System.out.println(Colors.title("\n╔══════ ESTADO DEL EVENTO ═══════════════════════════╗"));
        System.out.printf(Colors.CYAN + "║  Evento    : " + Colors.CYAN_B + "%-37s" + Colors.RESET + "%n", e.getName());
        System.out.printf(Colors.CYAN + "║  Capacidad : " + Colors.RESET + "%-37d%n", e.getCapacity());
        System.out.printf(Colors.CYAN + "║  Altura AVL: " + Colors.RESET + "%-37d%n", e.getTreeHeight());
        System.out.printf(Colors.CYAN + "║  Cola esp. : " + Colors.RESET + "%-37d%n", e.getQueueSize());
        System.out.printf(Colors.CYAN + "║  Historial : " + Colors.RESET + "%d ops%n", e.getHistorySize());
        System.out.println(Colors.title("╠════════════════════════════════════════════════════╣"));
        Terminal.printOccupancyBar("Inscritos",      count, e.getCapacity(), 22);
        Terminal.printOccupancyBar("Con asistencia", att,   count,           22);
        System.out.println(Colors.title("╚════════════════════════════════════════════════════╝\n"));
    }

    private void exportCSV(EventService e) {
        String safeName = e.getName().replaceAll("[^a-zA-Z0-9_-]", "_");
        String path     = AppPaths.Dirs.RESULTS.resolve("events").resolve(safeName + ".csv").toString();
        System.out.print("Ruta de exportacion [ENTER = " + path + "]: ");
        String input = sc.nextLine().trim();
        if (!input.isEmpty()) path = input;
        if (e.exportCSV(path))
            System.out.println(Colors.ok("✔ Exportado (" + e.getStudentCount() + " estudiantes): " + path));
        else
            System.out.println(Colors.warn("⚠  No hay estudiantes para exportar."));
    }

    private void clearList(EventService e) {
        if (e.isEmpty()) { System.out.println("La lista ya esta vacia."); return; }
        System.out.println(Colors.warn("\n⚠  ATENCION: Esta accion eliminara "
                + e.getStudentCount() + " estudiantes de \"" + e.getName() + "\"."));
        System.out.println(Colors.warn("   La cola de espera tambien se limpiara."));
        System.out.print("Escribe " + Colors.bold("CONFIRMAR") + " para continuar: ");
        if (sc.nextLine().trim().equals("CONFIRMAR")) {
            int n = e.clearAll();
            System.out.println(Colors.ok("✔ Lista borrada. Se eliminaron " + n + " estudiantes."));
        } else { System.out.println("Cancelado. La lista no fue modificada."); }
    }

    private void addRandom(EventService e) {
        int available = e.getCapacity() - e.getStudentCount();
        System.out.println();
        if (available > 0) System.out.println("  Capacidad disponible : " + Colors.bold(String.valueOf(available)));
        else               System.out.println(Colors.warn("  Evento lleno. Los que no quepan iran a la cola."));
        System.out.print("¿Cuantos estudiantes aleatorios deseas agregar?: ");
        int count = readInt();
        if (count <= 0) { System.out.println(Colors.error("❌ Cantidad invalida")); return; }

        int startId = e.getStudentCount() + e.getQueueSize() + 1;
        System.out.println();
        int inEvent = 0, inQueue = 0, curId = startId;

        for (int i = 0; i < count; i++) {
            String name    = NAMES   [curId % NAMES.length];
            String program = PROGRAMS[curId % PROGRAMS.length];
            String email   = "est" + String.format("%04d", curId) + "@unal.edu.co";
            int result = e.registerBulk(curId, name, email, program);
            if      (result == 1) inEvent++;
            else if (result == 0) inQueue++;
            curId++;
            Terminal.bulkAddProgress(i + 1, count, inEvent, inQueue);
        }
        System.out.println(Colors.ok("\n✅ Listo: " + inEvent + " registrado(s) en el evento, "
                + inQueue + " en cola de espera."));
    }

    private void performanceMenu() {
        System.out.println();
        System.out.println(Colors.title("╔═══════════════ PRUEBAS DE RENDIMIENTO ═══════════════╗"));
        System.out.println("║  [1]  n = 10,000      (10^4)                          ║");
        System.out.println("║  [2]  n = 100,000     (10^5)                          ║");
        System.out.println("║  [3]  n = 1,000,000   (10^6)                          ║");
        System.out.println("║  [4]  TODAS + graficas Python automaticas              ║");
        System.out.println("║  [0]  Volver                                           ║");
        System.out.println(Colors.title("╚═══════════════════════════════════════════════════════╝"));
        System.out.print("Opcion: ");

        int[] sizes = { 10_000, 100_000, 1_000_000 };
        int op = readInt();
        if      (op >= 1 && op <= 3) PerformanceService.runBenchmarks(new int[]{ sizes[op - 1] });
        else if (op == 4)            PerformanceService.runBenchmarks(sizes);
    }

    private int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException ex) { return -1; }
    }
}