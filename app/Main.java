


public class Main {

    private static GestorEventos gestor = new GestorEventos();
    private static final Scanner sc     = new Scanner(System.in);

    private static final String ARCHIVO_DATOS = "datos_eventos.txt";

    private static final String[] NOMBRES = {
            "Ana Garcia",      "Carlos Lopez",    "Diana Perez",     "Eduardo Gomez",   "Francisca Ruiz",
            "Gabriel Torres",  "Helena Morales",  "Ivan Castro",     "Julia Vargas",    "Kevin Reyes",
            "Laura Mendoza",   "Miguel Herrera",  "Natalia Flores",  "Oscar Romero",    "Paula Diaz",
            "Rodrigo Ortiz",   "Sofia Molina",    "Tomas Guerrero",  "Valentina Cruz",  "Wilmer Rios",
            "Alejandra Nunez", "Bryan Salazar",   "Camila Jimenez",  "Daniel Rojas",    "Estefania Vega",
            "Felipe Ramos",    "Gloria Medina",   "Hugo Sandoval",   "Isabella Pena",   "Jorge Aguilar"
    };

    private static final String[] PROGRAMAS = {
            "Ing. Sistemas",  "Ing. Industrial", "Economia",        "Medicina",        "Ing. Civil",
            "Matematicas",    "Fisica",          "Quimica",         "Biologia",        "Derecho",
            "Administracion", "Arquitectura",    "Psicologia",      "Sociologia",      "Estadistica"
    };

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String[] args) {
        cabecera();
        menuGestor();
        System.out.println("\n" + Colores.info("Programa finalizado."));
        sc.close();
    }

    // =========================================================
    // CABECERA
    // =========================================================
    static void cabecera() {
        System.out.println();
        System.out.println(Colores.titulo("╔══════════════════════════════════════════════════════╗"));
        System.out.println(Colores.titulo("║  SISTEMA DE REGISTRO Y VALIDACION DE ESTUDIANTES     ║"));
        System.out.println(Colores.titulo("║       EN EVENTOS ACADEMICOS — ARBOL AVL              ║"));
        System.out.println(Colores.titulo("║       Estructuras de Datos 2016699 · UNAL 2026       ║"));
        System.out.println(Colores.titulo("╚══════════════════════════════════════════════════════╝"));
        System.out.println();
        System.out.println("  Estructuras activas: "
                + Colores.bold("AVL") + "  "
                + Colores.bold("Lista enlazada") + "  "
                + Colores.bold("Cola") + "  "
                + Colores.bold("Pila") + "  "
                + Colores.bold("BST"));
        System.out.println();
    }

    // =========================================================
    // MENU NIVEL 1: GESTOR DE EVENTOS
    // =========================================================
    static void menuGestor() {
        boolean salir = false;
        while (!salir) {
            int total = gestor.getCantidadEventos();
            System.out.println();
            System.out.println(Colores.titulo("╔══════════════════ GESTOR DE EVENTOS ══════════════════╗"));
            System.out.printf (Colores.CYAN + "║  Eventos activos: " + Colores.CYAN_B + "%-3d" + Colores.RESET + "%n", total);
            System.out.println(Colores.titulo("╠═══════════════════════════════════════════════════════╣"));
            System.out.println("║  [1] Crear nuevo evento                               ║");
            System.out.println("║  [2] Acceder a un evento                              ║");
            System.out.println("║  [3] Listar todos los eventos                         ║");
            System.out.println("║  [4] Eliminar un evento                               ║");
            System.out.println("║  [5] Guardar datos en archivo                         ║");
            System.out.println("║  [6] Cargar datos desde archivo                       ║");
            System.out.println("║  [7] Analisis de rendimiento (AVL vs BST)             ║");
            System.out.println("║  [0] Salir                                            ║");
            System.out.println(Colores.titulo("╚═══════════════════════════════════════════════════════╝"));
            System.out.print("Opcion: ");

            switch (leerInt()) {
                case 1: crearEvento();     break;
                case 2: entrarEvento();    break;
                case 3: verEventos();      break;
                case 4: eliminarEvento();  break;
                case 5: guardarDatos();    break;
                case 6: cargarDatos();     break;
                case 7: menuRendimiento(); break;
                case 0: salir = true;      break;
                default: System.out.println(Colores.error("❌ Opcion invalida"));
            }
        }
    }

    static void crearEvento() {
        System.out.print("Nombre del evento : ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println(Colores.error("❌ Nombre vacio")); return; }
        System.out.print("Capacidad maxima  : ");
        int cap = leerInt();
        if (cap <= 0) { System.out.println(Colores.error("❌ Capacidad invalida")); return; }
        gestor.crearEvento(nombre, cap);
    }

    static void verEventos() {
        gestor.listarEventos();
    }

    static void entrarEvento() {
        if (!gestor.hayEventos()) {
            System.out.println(Colores.error("❌ No hay eventos. Crea uno primero (opcion 1)."));
            return;
        }
        gestor.listarEventos();
        System.out.print("Numero de evento: ");
        int idx = leerInt();
        ValidadorEventos evento = gestor.obtenerEvento(idx);
        if (evento == null) { System.out.println(Colores.error("❌ Numero invalido")); return; }
        menuEvento(evento);
    }

    static void eliminarEvento() {
        if (!gestor.hayEventos()) { System.out.println(Colores.error("❌ No hay eventos")); return; }
        gestor.listarEventos();
        System.out.print("Numero de evento a eliminar: ");
        int idx = leerInt();
        ValidadorEventos ev = gestor.obtenerEvento(idx);
        if (ev == null) { System.out.println(Colores.error("❌ Numero invalido")); return; }
        System.out.print("¿Confirmar eliminar \"" + ev.getNombreEvento() + "\"? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            gestor.eliminarEvento(idx);
            System.out.println(Colores.ok("✓ Evento eliminado"));
        } else {
            System.out.println("Cancelado.");
        }
    }

    static void guardarDatos() {
        System.out.print("Ruta del archivo [ENTER = " + ARCHIVO_DATOS + "]: ");
        String ruta = sc.nextLine().trim();
        if (ruta.isEmpty()) ruta = ARCHIVO_DATOS;
        gestor.guardarDatos(ruta);
    }

    static void cargarDatos() {
        System.out.print("Ruta del archivo [ENTER = " + ARCHIVO_DATOS + "]: ");
        String ruta = sc.nextLine().trim();
        if (ruta.isEmpty()) ruta = ARCHIVO_DATOS;
        System.out.print(Colores.warn("⚠ Cargar reemplazara los datos actuales. Continuar? (s/n): "));
        if (!sc.nextLine().trim().equalsIgnoreCase("s")) { System.out.println("Cancelado."); return; }
        gestor.cargarDatos(ruta);
    }

    // =========================================================
    // MENU NIVEL 2: DENTRO DE UN EVENTO
    // =========================================================
    static void menuEvento(ValidadorEventos evento) {
        boolean salir = false;
        while (!salir) {
            int ins  = evento.getCantidadEstudiantes();
            int cap  = evento.getCapacidad();
            int cola = evento.getTamanoColaEspera();
            int hist = evento.getTamanoHistorial();
            int asst = evento.getCantidadAsistencias();

            System.out.println();
            System.out.println(Colores.titulo("╔══════════════════════════════════════════════════════════╗"));
            System.out.println(Colores.CYAN + Colores.NEGRITA
                    + "║  EVENTO: " + evento.getNombreEvento() + Colores.RESET);

            // Barra de ocupacion visual
            System.out.print("║  ");
            System.out.println(Consola.barraProgreso(ins, cap, 32)
                    + "  " + Colores.CYAN_B + ins + "/" + cap + " inscritos" + Colores.RESET);

            System.out.printf(Colores.CYAN
                            + "║  Asistencia: " + Colores.AMARILLO_B + "%d"
                            + Colores.CYAN + "  │  Cola: " + Colores.MAGENTA + "%d"
                            + Colores.CYAN + "  │  Historial: %d ops" + Colores.RESET + "%n",
                    asst, cola, hist);
            System.out.println(Colores.titulo("╠══════════════════════════════════════════════════════════╣"));
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
            System.out.println(Colores.titulo("╚══════════════════════════════════════════════════════════╝"));
            System.out.print("Opcion: ");

            switch (leerInt()) {
                case 1:  registrar(evento);                        break;
                case 2:  verificar(evento);                        break;
                case 3:  asistencia(evento);                       break;
                case 4:  eliminarEstudiante(evento);               break;
                case 5:  evento.deshacerUltimaEliminacion();       break;
                case 6:  evento.listarEstudiantes();               break;
                case 7:  evento.mostrarColaEspera();               break;
                case 8:  evento.mostrarHistorial();                break;
                case 9:  evento.imprimirEstado();                  break;
                case 10: evento.visualizarArbol();                 break;
                case 11: agregarAleatorios(evento);                break;
                case 12: exportarCSV(evento);                      break;
                case 13: borrarLista(evento);                      break;
                case 0:  salir = true;                             break;
                default: System.out.println(Colores.error("❌ Opcion invalida"));
            }
        }
    }


    // =========================================================
    // ACCIONES DEL EVENTO
    // =========================================================
    static void registrar(ValidadorEventos e) {
        System.out.print("ID institucional : "); int    id   = leerInt();
        System.out.print("Nombre completo  : "); String nom  = sc.nextLine().trim();
        System.out.print("Correo           : "); String cor  = sc.nextLine().trim();
        System.out.print("Programa         : "); String prog = sc.nextLine().trim();
        e.registrarEstudiante(id, nom, cor, prog);
    }

    static void verificar(ValidadorEventos e) {
        System.out.print("ID a buscar: ");
        int id = leerInt();
        if (e.verificarEstudiante(id))
            System.out.println(Colores.ok("✓ Encontrado:") + "\n" + e.obtenerEstudiante(id));
        else
            System.out.println(Colores.error("❌ No existe un estudiante con ID " + id));
    }

    static void asistencia(ValidadorEventos e) {
        System.out.print("ID del estudiante: ");
        e.marcarAsistencia(leerInt());
    }

    static void eliminarEstudiante(ValidadorEventos e) {
        System.out.print("ID a eliminar: ");
        int id = leerInt();
        if (!e.verificarEstudiante(id)) {
            System.out.println(Colores.error("❌ No encontrado"));
            return;
        }
        System.out.print(Colores.warn("¿Confirmar eliminar ID " + id + "? (s/n): "));
        if (sc.nextLine().trim().equalsIgnoreCase("s")) e.eliminarEstudiante(id);
        else System.out.println("Cancelado.");
    }

    // =========================================================
    // EXPORTAR CSV
    // =========================================================
    static void exportarCSV(ValidadorEventos e) {
        String nombre = e.getNombreEvento().replaceAll("[^a-zA-Z0-9_-]", "_");
        String path   = "results/events/" + nombre + ".csv";
        System.out.print("Ruta de exportacion [ENTER = " + path + "]: ");
        String entrada = sc.nextLine().trim();
        if (!entrada.isEmpty()) path = entrada;
        e.exportarCSV(path);
    }

    // =========================================================
    // BORRAR LISTA COMPLETA
    // =========================================================
    static void borrarLista(ValidadorEventos e) {
        if (e.estaVacio()) { System.out.println("La lista ya esta vacia."); return; }
        System.out.println(Colores.warn(
                "\n⚠  ATENCION: Esta accion eliminara " + e.getCantidadEstudiantes()
                        + " estudiantes de \"" + e.getNombreEvento() + "\"."));
        System.out.println(Colores.warn("   La cola de espera tambien se limpiara."));
        System.out.print("Escribe " + Colores.bold("CONFIRMAR") + " para continuar: ");
        if (sc.nextLine().trim().equals("CONFIRMAR")) {
            e.borrarLista();
        } else {
            System.out.println("Cancelado. La lista no fue modificada.");
        }
    }

    // =========================================================
    // AGREGAR ESTUDIANTES ALEATORIOS — con barra de progreso
    // =========================================================
    static void agregarAleatorios(ValidadorEventos e) {
        int disponibles = e.getCapacidad() - e.getCantidadEstudiantes();

        System.out.println();
        if (disponibles > 0) {
            System.out.println("  Capacidad disponible : " + Colores.bold(String.valueOf(disponibles)));
        } else {
            System.out.println(Colores.warn("  Evento lleno. Los que no quepan iran a la cola."));
        }
        System.out.print("¿Cuantos estudiantes aleatorios deseas agregar?: ");

        int cantidad = leerInt();
        if (cantidad <= 0) { System.out.println(Colores.error("❌ Cantidad invalida")); return; }

        int idInicio = e.getCantidadEstudiantes() + e.getTamanoColaEspera() + 1;
        System.out.println();

        int enEvento = 0;
        int enCola   = 0;
        int idActual = idInicio;

        for (int i = 0; i < cantidad; i++) {
            String nombre   = NOMBRES  [idActual % NOMBRES.length];
            String programa = PROGRAMAS[idActual % PROGRAMAS.length];
            String correo   = "est" + String.format("%04d", idActual) + "@unal.edu.co";

            int resultado = e.registrarBulk(idActual, nombre, correo, programa);
            if      (resultado == 1) enEvento++;
            else if (resultado == 0) enCola++;
            idActual++;

            Consola.progresoBulkAdd(i + 1, cantidad, enEvento, enCola);
        }

        System.out.println(Colores.ok(
                "\n✅ Listo: " + enEvento + " registrado(s) en el evento, "
                        + enCola + " en cola de espera."));
    }

    // =========================================================
    // MENU RENDIMIENTO (AVL vs BST)
    // =========================================================
    static void menuRendimiento() {
        System.out.println();
        System.out.println(Colores.titulo("╔═══════════════ PRUEBAS DE RENDIMIENTO ═══════════════╗"));
        System.out.println("║  [1]  n = 10,000      (10^4)                          ║");
        System.out.println("║  [2]  n = 100,000     (10^5)                          ║");
        System.out.println("║  [3]  n = 1,000,000   (10^6)                          ║");
        System.out.println("║  [4]  TODAS + graficas Python automaticas              ║");
        System.out.println("║  [0]  Volver                                           ║");
        System.out.println(Colores.titulo("╚═══════════════════════════════════════════════════════╝"));
        System.out.print("Opcion: ");

        int[] sizes = { 10_000, 100_000, 1_000_000 };
        int op = leerInt();

        if      (op >= 1 && op <= 3) MedidorRendimiento.ejecutarPruebas(new int[]{ sizes[op - 1] });
        else if (op == 4)            MedidorRendimiento.ejecutarPruebas(sizes);
    }


}