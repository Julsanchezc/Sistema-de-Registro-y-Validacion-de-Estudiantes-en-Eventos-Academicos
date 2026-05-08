


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


}