# Sistema de Registro y Validación de Estudiantes en Eventos Académicos

Sistema de gestión de asistencia a eventos académicos implementado en Java, usando un **Árbol AVL** como estructura principal de almacenamiento. Desarrollado para la asignatura **Estructuras de Datos (2016699) — UNAL 2026**.

---

## Integrantes

| Nombre completo | Correo institucional |
|---|---|
| Edison Stiven Quintero Motta | edquinterom@unal.edu.co |
| Juan Diego Sánchez Peña | juasanchezpe@unal.edu.co |
| Juan Pablo Gómez Cristancho | jugomezcr@unal.edu.co |
| Julian Santiago Sanchez Castro *(Líder)* | julsanchezc@unal.edu.co |
| Rafael Ramírez León | rramirezl@unal.edu.co |

---

## Descripción

El sistema permite crear y gestionar múltiples eventos académicos. Cada evento mantiene su propio árbol AVL con los estudiantes inscritos, una cola de espera FIFO para cuando el aforo se llena, y una pila LIFO para deshacer la última eliminación.

Todas las estructuras de datos están implementadas manualmente desde cero, sin librerías de colecciones.

---

## Arquitectura

El proyecto sigue una arquitectura en capas con dos raíces de código fuente:

```
src/core/           → infraestructura transversal (rutas, logs, config, excepciones)
app/eventos/
  model/            → Student — modelo de dominio
  structures/       → AvlTree, AvlNode, BstTree, Queue, HistoryStack
  service/          → EventService, EventManager, PerformanceService, enums
  repository/       → EventRepository — única clase con acceso al archivo JSON
  ui/console/       → ConsoleApp, Colors, Terminal — toda la UI por consola
  ui/gui/           → MainWindow (stub listo para Swing)
```

**Flujo de bootstrap** (orden de inicialización en `Main.java`):

```
AppPaths.ensureDirs() → AppSettings.isLoaded() → new ConsoleApp().run()
```

---

## Estructura de archivos

```
├── src/
│   └── core/
│       ├── AppPaths.java           # Rutas estáticas: data/, logs/, results/
│       ├── AppLogger.java          # Wrapper de SLF4J
│       ├── AppSettings.java        # Carga config.properties del classpath
│       └── exceptions/
│           ├── AppException.java
│           └── PersistenceException.java
│
├── app/
│   └── eventos/
│       ├── Main.java
│       ├── model/
│       │   └── Student.java        # id, name, email, program, attended
│       ├── structures/
│       │   ├── AvlNode.java        # Nodo AVL con puntero padre
│       │   ├── AvlTree.java        # AVL auto-balanceado — O(log n)
│       │   ├── BstTree.java        # BST sin balanceo — solo para benchmarks
│       │   ├── Queue.java          # Cola genérica FIFO
│       │   └── HistoryStack.java   # Pila LIFO con soporte undo
│       ├── service/
│       │   ├── EventService.java   # Lógica de un evento (AVL + Cola + Pila)
│       │   ├── EventManager.java   # Lista enlazada de eventos
│       │   ├── PerformanceService.java  # Benchmarks AVL vs BST
│       │   ├── RegisterResult.java # Enum resultado de registro
│       │   └── UndoResult.java     # Enum resultado de deshacer
│       ├── repository/
│       │   └── EventRepository.java  # Guardar/cargar JSON con Jackson
│       └── ui/
│           ├── console/
│           │   ├── ConsoleApp.java # Toda la interacción por consola
│           │   ├── Colors.java     # Constantes y helpers ANSI
│           │   └── Terminal.java   # Tablas, barras de progreso
│           └── gui/
│               └── MainWindow.java # Stub para futura GUI Swing
│
├── resources/
│   ├── config.properties           # Configuración de la aplicación
│   └── logback.xml                 # Configuración de logs (JSON a logs/app.log)
│
├── data/                           # Generado en ejecución (.gitignored)
│   └── events.json                 # Estado persistido de todos los eventos
│
├── logs/                           # Generado en ejecución (.gitignored)
│   └── app.log                     # Logs estructurados en formato NDJSON
│
├── results/                        # Generado al correr benchmarks
│   └── results.csv
│
├── build.gradle
├── settings.gradle
├── gradle.properties               # Apunta al JDK 21 (requerido)
└── gradlew.bat
```

---

## Requisitos

| Herramienta | Versión |
|---|---|
| Java JDK | **21** (ver nota abajo) |
| Gradle | incluido via `gradlew.bat` — no requiere instalación |
| Python + matplotlib | opcional, para generar gráficas del benchmark |

> **Nota JDK:** El proyecto usa Gradle 8.14, que soporta hasta Java 24. Si tienes Java 26 instalado, el archivo `gradle.properties` ya apunta al JDK 21 ubicado en `C:\Program Files\Java\jdk-21`. Si tu JDK 21 está en otra ruta, edita esa línea:
> ```properties
> org.gradle.java.home=C:\\Program Files\\Java\\jdk-21
> ```

---

## Compilar y ejecutar

Todos los comandos se corren desde la raíz del proyecto en PowerShell:

```powershell
# Compilar
.\gradlew.bat build

# Ejecutar
.\gradlew.bat run

# Compilar y ejecutar en un paso
.\gradlew.bat run --console=plain
```

> `--console=plain` evita que Gradle sobreescriba las líneas de la interfaz con su barra de progreso.

---

## Uso del sistema

### Nivel 1 — Gestor de eventos

```
╔══════════════════ GESTOR DE EVENTOS ══════════════════╗
║  [1] Crear nuevo evento                               ║
║  [2] Acceder a un evento                              ║
║  [3] Listar todos los eventos                         ║
║  [4] Eliminar un evento                               ║
║  [5] Guardar datos en archivo                         ║
║  [6] Cargar datos desde archivo                       ║
║  [7] Análisis de rendimiento (AVL vs BST)             ║
║  [0] Salir                                            ║
╚═══════════════════════════════════════════════════════╝
```

### Nivel 2 — Dentro de un evento

```
╔══════════════════════════════════════════════════════════╗
║  EVENTO: Conferencia UNAL
║  [████████████████░░░░░░░░░░░░░░░░]  50.0%  15/30 inscritos
║  Asistencia: 3  │  Cola: 2  │  Historial: 18 ops
╠══════════════════════════════════════════════════════════╣
║  [1]  Registrar estudiante                               ║
║  [2]  Consultar por ID                                   ║
║  [3]  Marcar asistencia                                  ║
║  [4]  Eliminar estudiante                                ║
║  [5]  Deshacer última eliminación                        ║
║  [6]  Listar estudiantes (tabla)                         ║
║  [7]  Ver cola de espera                                 ║
║  [8]  Historial de operaciones                           ║
║  [9]  Estado del evento                                  ║
║  [10] Visualizar árbol AVL                               ║
║  [11] Agregar estudiantes aleatorios                     ║
║  [12] Exportar lista a CSV                               ║
║  [13] BORRAR LISTA COMPLETA                              ║
╚══════════════════════════════════════════════════════════╝
```

**Comportamientos importantes:**

- **Cola de espera automática** — cuando el evento alcanza su aforo, nuevos registros van a la cola. Al eliminar un inscrito, el primero de la cola es promovido automáticamente.
- **Deshacer eliminación** — `[5]` restaura el último estudiante eliminado. Si el evento está lleno, lo mueve a la cola en vez de reinsertarlo.
- **Historial** — `[8]` muestra las últimas 50 operaciones (LIFO). Las entradas con `[*]` son eliminaciones que pueden deshacerse.
- **Barra animada** — `[11]` agrega N estudiantes aleatorios mostrando progreso en tiempo real.

---

## Estructuras de datos

| Estructura | Clase | Uso en el sistema |
|---|---|---|
| Árbol AVL | `AvlTree` + `AvlNode` | Almacenamiento principal por evento — O(log n) |
| Cola FIFO | `Queue<T>` | Lista de espera cuando el aforo está completo |
| Pila LIFO | `HistoryStack` | Historial de operaciones + deshacer última eliminación |
| Lista enlazada | `EventManager` (nodos internos) | Gestión de múltiples eventos |
| Árbol BST | `BstTree` | Solo para comparativa de rendimiento |

---

## Persistencia

El estado completo se guarda en `data/events.json` usando **Jackson Databind**. El archivo incluye todos los eventos con sus estudiantes inscritos y cola de espera.

```json
{
  "events": [
    {
      "name": "Conferencia UNAL",
      "maxCapacity": 30,
      "students": [
        { "id": 1, "name": "Ana Garcia", "email": "...", "program": "...", "attended": false }
      ],
      "queue": []
    }
  ]
}
```

Las opciones `[5] Guardar` y `[6] Cargar` del gestor permiten especificar una ruta personalizada. Si se presiona Enter sin escribir nada, se usa `data/events.json` por defecto.

---

## Logs

La aplicación escribe logs estructurados en `logs/app.log` en formato **NDJSON** (una línea JSON por entrada). La consola solo muestra mensajes de nivel `WARN` o superior.

```json
{"@timestamp":"2026-06-08T...","level":"INFO","message":"Student registered: id=1 name='Ana Garcia'"}
```

---

## Análisis de rendimiento (opción 7)

Compara **AVL vs BST** con datos aleatorios (Fisher-Yates, semilla 42) para n = 10.000, 100.000 y 1.000.000. Los resultados se exportan a `results/results.csv` y Python genera las gráficas automáticamente si está instalado.

### Resultados obtenidos

| n | AVL ins | AVL find | AVL del | Alt AVL | BST ins | BST find | BST del | Alt BST | log₂(n) |
|---|---|---|---|---|---|---|---|---|---|
| 10.000 | 7 ms | 2 ms | 2 ms | 16 | 6 ms | 2 ms | 3 ms | 30 | 13.29 |
| 100.000 | 54 ms | 34 ms | 35 ms | 20 | 38 ms | 31 ms | 46 ms | 44 | 16.61 |
| 1.000.000 | 1.251 ms | 809 ms | 988 ms | 24 | 1.149 ms | 843 ms | 968 ms | 47 | 19.93 |

**Caso degenerado** — inserción secuencial 1, 2, …, 2000:
- AVL altura: **11** (log₂(2000) = 10.97, límite 1.44× = 15.8)
- BST altura: **2000** (degenerado — equivale a lista enlazada)

> Si Python no corre automáticamente:
> ```bash
> python results/graficar_rendimiento.py results/results.csv results/
> ```

---

## Complejidad

| Operación | AVL | BST (aleatorio) | BST (secuencial) |
|---|---|---|---|
| Inserción | O(log n) | O(log n) esperado | O(n) |
| Búsqueda | O(log n) | O(log n) esperado | O(n) |
| Eliminación | O(log n) | O(log n) esperado | O(n) |
| Altura garantizada | h ≤ 1.44·log₂(n) | ~2.5·log₂(n) promedio | h = n |
| Cola: enqueue/dequeue | O(1) | — | — |
| Pila: push/pop | O(1) | — | — |

---

## Dependencias (Gradle)

| Librería | Uso |
|---|---|
| `slf4j-api` + `logback-classic` | Logging |
| `logstash-logback-encoder` | Formato JSON en los logs |
| `jackson-databind` | Serialización JSON para persistencia |
| `junit-jupiter` | Tests unitarios |

---

## Referencias

1. Cormen, T. H. et al. (2009). *Introduction to Algorithms* (3rd ed.). MIT Press.
2. Adelson-Velsky, G., & Landis, E. (1962). An algorithm for the organization of information. *Soviet Mathematics Doklady*.
3. Weiss, M. A. (2012). *Data Structures and Algorithm Analysis in Java* (3rd ed.). Pearson.
4. Sedgewick, R., & Wayne, K. (2011). *Algorithms* (4th ed.). Addison-Wesley.
5. Streib, J. T., & Soma, T. (2017). *Guide to Data Structures: A Concise Introduction Using Java*. Springer.