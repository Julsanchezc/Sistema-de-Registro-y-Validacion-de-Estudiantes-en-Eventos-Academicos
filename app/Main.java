


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


}