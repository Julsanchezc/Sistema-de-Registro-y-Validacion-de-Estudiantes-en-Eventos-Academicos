# Sistema de Registro y Validación de Estudiantes en Eventos Académicos

Sistema de gestión de asistencia a eventos académicos implementado en Java, usando un **Árbol AVL** como estructura principal de almacenamiento y múltiples estructuras adicionales. Desarrollado para la asignatura **Estructuras de Datos (2016699) — UNAL 2026**.

---

## Integrantes

| Nombre completo | Correo institucional |
|---|---|
| Edison Stiven Quintero Motta | edquinterom@unal.edu.co |
| Juan Diego Sánchez Peña | Juasanchezpe@unal.edu.co |
| Juan Pablo Gómez Cristancho | jugomezcr@unal.edu.co |
| Julian Santiago Sanchez Castro *(Líder)* | julsanchezc@unal.edu.co |
| Rafael Ramírez León | rramirezl@unal.edu.co |

---

## Descripción del proyecto

El sistema permite gestionar el registro y la asistencia de estudiantes en múltiples eventos académicos. Cada evento tiene su propio árbol AVL que almacena y organiza a los estudiantes por ID institucional, garantizando búsquedas, inserciones y eliminaciones en tiempo **O(log n)**.

Todas las estructuras de datos se implementan manualmente desde cero, sin librerías externas de colecciones:

| Estructura | Clase | Uso |
|---|---|---|
| Árbol AVL | `ArbolAVL` + `NodoAVL` | Almacenamiento principal de estudiantes por evento — O(log n) |
| Árbol BST | `ArbolBST` | Comparativa de rendimiento frente al AVL |
| Cola FIFO | `Cola<T>` | Lista de espera automática cuando el aforo está completo |
| Pila LIFO | `PilaHistorial` | Historial de operaciones + deshacer última eliminación |
| Lista enlazada | `GestorEventos` | Gestión de múltiples eventos + persistencia en archivo |

---

## Estructura del proyecto

```
app/
├── Main.java                        # Punto de entrada — menús interactivos
├── model/
│   └── Estudiante.java              # Modelo de datos: id, nombre, correo, programa, asistencia
├── ui/
│   ├── Colores.java                 # Constantes ANSI y helpers de color
│   └── Consola.java                 # Barras de progreso, tablas con bordes Unicode
├── structures/
│   ├── NodoAVL.java                 # Nodo AVL: estudiante + altura + punteros (incl. padre)
│   ├── ArbolAVL.java                # Árbol AVL auto-balanceado — O(log n)
│   ├── ArbolBST.java                # BST sin balanceo — comparativa de rendimiento
│   ├── Cola.java                    # Cola genérica FIFO — lista de espera
│   └── PilaHistorial.java           # Pila LIFO — historial de ops + undo
├── system/
│   ├── ValidadorEventos.java        # Lógica de un evento (AVL + Cola + Pila)
│   └── GestorEventos.java           # Lista enlazada de eventos + persistencia
└── performance/
    └── MedidorRendimiento.java      # Pruebas AVL vs BST — 10⁴, 10⁵, 10⁶

scripts/
└── graficar_rendimiento.py          # Genera 4 gráficas PNG comparativas desde el CSV

results/
├── results.csv                      # Resultados de rendimiento (generado automáticamente)
├── grafica_tiempos.png              # Tiempos AVL por operación
├── grafica_comparativa.png         # Comparativa AVL vs BST
├── grafica_altura.png               # Altura real vs log₂(n)
└── grafica_tabla.png                # Resumen completo con tabla

data/
└── datos_eventos.txt                # Persistencia del estado (generado al guardar)
```

---

## Requisitos

| Herramienta | Versión mínima |
|---|---|
| Java JDK | 11 |
| Python | 3.8 (opcional, para gráficas) |
| matplotlib | cualquiera (se instala automáticamente si falta) |

---

## Compilar y ejecutar

### PowerShell (Windows)

```powershell
# Compilar
javac -d app\out -sourcepath app (Get-ChildItem app -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName)

# Ejecutar
java -cp app\out Main
```

### Linux / macOS / Git Bash

```bash
# Compilar
javac -d app/out -sourcepath app $(find app -name "*.java")

# Ejecutar
java -cp app/out Main
```

> Ejecutar siempre desde la raíz del proyecto para que las rutas `data/` y `results/` funcionen correctamente.

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

**Funcionalidades destacadas:**

- **Cola de espera automática** — cuando el evento alcanza su aforo, los nuevos estudiantes se encolan automáticamente. Al eliminar un inscrito, el primero de la cola es promovido.
- **Deshacer eliminación** — la opción `[5]` restaura el último estudiante eliminado gracias a la pila de historial.
- **Barra de progreso animada** — la opción `[11]` popula el evento con una barra animada en tiempo real.
- **Tabla de estudiantes** — la opción `[6]` muestra una tabla con bordes Unicode y celdas coloreadas (verde ✓ / rojo ✗).
- **Visualización del árbol** — la opción `[10]` imprime el árbol AVL con altura y factor de balance coloreado (verde=0, amarillo=±1).
- **Persistencia** — guardar/cargar el estado completo de todos los eventos en `data/datos_eventos.txt`.
- **Estado con dos porcentajes** — la opción `[9]` muestra % de inscritos sobre capacidad y % de asistencia sobre inscritos.

---

## Análisis de rendimiento (opción 7)

Ejecuta inserción, búsqueda y eliminación comparando **AVL vs BST** con datos aleatorios (Fisher-Yates, semilla 42) para n = 10.000, 100.000 y 1.000.000. Los resultados se exportan a `results/results.csv` y Python genera las gráficas automáticamente.

### Resultados obtenidos

| n | AVL Ins (ms) | AVL Bus (ms) | AVL Elm (ms) | Alt AVL | BST Ins (ms) | BST Bus (ms) | BST Elm (ms) | Alt BST | log₂(n) |
|---|---|---|---|---|---|---|---|---|---|
| 10.000 | 7 | 2 | 2 | 16 | 6 | 2 | 3 | 30 | 13.29 |
| 100.000 | 54 | 34 | 35 | 20 | 38 | 31 | 46 | 44 | 16.61 |
| 1.000.000 | 1.251 | 809 | 988 | 24 | 1.149 | 843 | 968 | 47 | 19.93 |

**Caso degenerado** con n=2.000 inserción secuencial:
- AVL altura: **11** (log₂(2000)=10.97 — límite 1.44×log₂=15.8)
- BST altura: **2.000** (degenerado — equivale a lista enlazada)

### Gráficas generadas

| Gráfica | Contenido |
|---|---|
| `grafica_tiempos.png` | Tiempos AVL por operación para cada n |
| `grafica_comparativa.png` | Comparativa AVL vs BST en barras |
| `grafica_altura.png` | Altura real AVL y BST vs log₂(n) teórico |
| `grafica_tabla.png` | Resumen comparativo completo con tabla |

> Si Python no corre automáticamente:
> ```bash
> python scripts/graficar_rendimiento.py results/results.csv results/
> ```

---

## Complejidad de las operaciones

| Operación | AVL | BST (aleatorio) | BST (secuencial) |
|---|---|---|---|
| Inserción | O(log n) | O(log n) esperado | O(n) |
| Búsqueda | O(log n) | O(log n) esperado | O(n) |
| Eliminación | O(log n) | O(log n) esperado | O(n) |
| Altura garantizada | h ≤ 1.44·log₂(n) | ~2.5·log₂(n) promedio | h = n |
| Cola: encolar/desencolar | O(1) | — | — |
| Pila: push/pop | O(1) | — | — |

---

## Lenguajes y herramientas

- **Java** — lógica principal, estructuras de datos, interfaz CLI con colores ANSI
- **Python / matplotlib** — generación de gráficas comparativas de rendimiento

---

## Referencias

1. Cormen, T. H. et al. (2009). *Introduction to Algorithms* (3rd ed.). MIT Press.
2. Adelson-Velsky, G., & Landis, E. (1962). An algorithm for the organization of information. *Soviet Mathematics Doklady*.
3. Weiss, M. A. (2012). *Data Structures and Algorithm Analysis in Java* (3rd ed.). Pearson.
4. Sedgewick, R., & Wayne, K. (2011). *Algorithms* (4th ed.). Addison-Wesley.
5. Streib, J. T., & Soma, T. (2017). *Guide to Data Structures: A Concise Introduction Using Java*. Springer.
