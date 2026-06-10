# Estructuras de Datos — Sistema de Registro de Eventos Académicos

## Resumen general

El sistema gestiona el registro de estudiantes en eventos académicos con cupo limitado.
Para ello usa **cinco estructuras de datos propias** (sin usar colecciones de la JDK),
cada una elegida según la operación que debe ser eficiente.

---

## 1. Árbol AVL (`AvlTree`)

### ¿Qué es?

Un **Árbol Binario de Búsqueda auto-balanceado**. Los nodos se insertan ordenados por el ID
del estudiante. Después de cada inserción o eliminación, el árbol verifica que la diferencia
de alturas entre el subárbol izquierdo y el derecho de cada nodo no supere 1. Si la supera,
aplica **rotaciones** para corregirlo.

### ¿Por qué no un BST simple?

Con un BST sin balanceo, si los estudiantes se insertan en orden (ID 1, 2, 3, 4...),
el árbol degenera en una lista y las búsquedas se vuelven O(n).
El AVL garantiza O(log n) siempre.

```
BST con entrada ordenada        AVL con la misma entrada
(degenera en lista)             (se balancea automáticamente)

1                                      3
 \                                   /   \
  2                                 2     4
   \                               /       \
    3                             1         5
     \
      4
       \
        5
```

### Rotaciones

Cuando el árbol se desequilibra, aplica una de cuatro rotaciones:

| Caso | Situación | Solución |
|------|-----------|----------|
| Left-Left | Inserción en subárbol izquierdo del hijo izquierdo | Rotación simple derecha |
| Right-Right | Inserción en subárbol derecho del hijo derecho | Rotación simple izquierda |
| Left-Right | Inserción en subárbol derecho del hijo izquierdo | Rotación doble (izq + der) |
| Right-Left | Inserción en subárbol izquierdo del hijo derecho | Rotación doble (der + izq) |

### Uso en el sistema

- **Almacena los estudiantes registrados** en cada evento, ordenados por ID.
- El **recorrido in-order** (izquierdo → raíz → derecho) produce la lista ordenada
  automáticamente, sin necesidad de ordenar después. Se usa para exportar el CSV
  y para alimentar el algoritmo de top-K.

### Complejidades

| Operación | Complejidad |
|-----------|-------------|
| Insertar | O(log n) |
| Buscar | O(log n) |
| Eliminar | O(log n) |
| Recorrer todos | O(n) |

---

## 2. Tabla de Hash (`HashTable`)

### ¿Qué es?

Una estructura que asocia una **clave con un valor** y permite encontrar ese valor en
tiempo constante O(1). Internamente usa un arreglo de cubos; la posición del cubo se
calcula con una función hash. Cuando dos claves caen en el mismo cubo (**colisión**),
se encadenan en una lista enlazada dentro de ese cubo.

```
put(1001, Estudiante_A)
put(2003, Estudiante_B)
put(1045, Estudiante_C)

  Cubo 0: -> null
  Cubo 1: -> [ID:2003, B] -> null
  Cubo 2: -> [ID:1001, A] -> [ID:1045, C] -> null   <- colisión resuelta por encadenamiento
  Cubo 3: -> null
  ...
```

Cuando la tabla se llena al 75% de su capacidad, se duplica y todos los elementos
se redistribuyen (**rehash**) para mantener las cadenas cortas.

### Uso en el sistema

- **Índice de búsqueda rápida por ID** en `EventService`: mantiene un `HashTable<Integer, Student>`
  paralelo al árbol AVL. Buscar un estudiante por ID usa la tabla (O(1)) en vez de recorrer
  el árbol (O(log n)).
- **Agregación de asistencias globales** en `EventManager.getTopAttendeesGlobal`:
  se usan dos tablas temporales para sumar el conteo de asistencias de todos los eventos
  antes de construir el top-K.

### Complejidades

| Operación | Complejidad promedio |
|-----------|----------------------|
| Insertar / actualizar | O(1) |
| Buscar | O(1) |
| Eliminar | O(1) |

---

## 3. Cola FIFO (`Queue`)

### ¿Qué es?

Una estructura **First In, First Out**: el primero en entrar es el primero en salir.
Implementada con lista enlazada simple; mantiene un puntero al frente (`front`)
y al fondo (`back`) para que insertar y extraer sean ambos O(1).

```
Enqueue(A) -> [A]
Enqueue(B) -> [A, B]
Enqueue(C) -> [A, B, C]
Dequeue()  -> retorna A  ->  [B, C]
Dequeue()  -> retorna B  ->  [C]
```

### Uso en el sistema

**Lista de espera** cuando un evento llega a su capacidad máxima:

1. El evento tiene cupo para 3 estudiantes. Llegan 5.
2. Los primeros 3 se insertan en el árbol AVL.
3. Los estudiantes 4 y 5 se encolan en la `Queue`.
4. Cuando se elimina un estudiante del evento, se llama a `promoteFromQueue()`:
   extrae el primero de la cola (el que llevaba más tiempo esperando) y lo registra
   automáticamente en el árbol.

Esto garantiza que el acceso al evento sea justo: **orden de llegada**.

### Complejidades

| Operación | Complejidad |
|-----------|-------------|
| Enqueue (agregar al fondo) | O(1) |
| Dequeue (extraer del frente) | O(1) |
| Peek (consultar frente) | O(1) |

---

## 4. Pila de Historial (`HistoryStack`)

### ¿Qué es?

Una estructura **Last In, First Out (LIFO)**: el último en entrar es el primero en salir.
Implementada con lista enlazada; el tope de la pila (`top`) apunta siempre a la operación
más reciente.

```
push("REGISTERED", "Juan")
push("ATTENDANCE", "María")
push("REMOVAL",    "Pedro")

Estado de la pila (tope arriba):
  [REMOVAL - Pedro]   <- tope (pop() sacaría este primero)
  [ATTENDANCE - María]
  [REGISTERED - Juan]
```

Tiene un límite de **50 entradas**; cuando se supera, la entrada más antigua (el fondo)
se descarta para no crecer infinitamente.

### Uso en el sistema

- **Historial de operaciones**: cada acción en `EventService` queda registrada
  (registro, asistencia, eliminación, exportación, etc.).
- **Operación de deshacer (undo)**: las entradas de tipo `REMOVAL` guardan
  el objeto `Student` completo. Si se llama a `undoLastRemoval()`:
  1. Se inspecciona el tope con `peek()`.
  2. Si es una eliminación, se extrae con `pop()` y el estudiante se restaura
     en el árbol AVL y el índice.
  3. Si el evento estaba lleno, el estudiante restaurado va a la cola de espera.

### Complejidades

| Operación | Complejidad |
|-----------|-------------|
| Push (apilar) | O(1) |
| Pop (desapilar) | O(1) |
| Peek (consultar tope) | O(1) |

---

## 5. Min-Heap (`MinHeap`)

### ¿Qué es?

Un árbol binario completo donde cada nodo es **menor o igual** que sus hijos,
por lo que el mínimo siempre está en la raíz. Se representa como arreglo: el elemento
en posición `i` tiene sus hijos en `2i+1` y `2i+2`, y su padre en `(i-1)/2`.

```
Arreglo: [10, 20, 15, 30, 25]

Vista como árbol:
           10         <- mínimo siempre en la raíz
          /  \
        20    15
       /  \
      30   25
```

### Algoritmo top-K con Min-Heap de tamaño k

Este es el uso más importante del heap en el sistema.
Encontrar los K estudiantes con más asistencias de forma eficiente:

1. Se mantiene un heap de tamaño máximo k.
2. Por cada estudiante nuevo:
   - Se inserta en el heap.
   - Si el heap tiene más de k elementos, se extrae el mínimo (el de menor asistencia).
3. Al terminar, el heap contiene exactamente los K con mayor asistencia.

```
Ejemplo: top-2 de [A=5, B=1, C=8, D=3, E=6]

  Insert A(5) -> heap: [5]
  Insert B(1) -> heap: [1, 5]        size=2, no expulsar
  Insert C(8) -> heap: [1, 5, 8]     size=3 > 2, expulsar min(1=B)
  heap: [5, 8]
  Insert D(3) -> heap: [3, 5, 8]     size=3 > 2, expulsar min(3=D)
  heap: [5, 8]
  Insert E(6) -> heap: [5, 6, 8]     size=3 > 2, expulsar min(5=A)
  heap: [6, 8]

  Resultado top-2: C(8), E(6)  ✓
```

### Uso en el sistema

| Método | Uso del heap |
|--------|--------------|
| `EventService.getTopAttendees(k)` | Top-K estudiantes por asistencia en un evento |
| `EventManager.getTopAttendeesGlobal(k)` | Top-K estudiantes por asistencia entre todos los eventos |
| `EventManager.getEventsByAvailability()` | Ordenar eventos por cupos disponibles (menor a mayor) |

### Complejidades

| Operación | Complejidad |
|-----------|-------------|
| Insertar | O(log n) |
| Extraer mínimo (poll) | O(log n) |
| Consultar mínimo (peek) | O(1) |
| Top-K sobre n elementos | O(n log k) |

---

## Cómo trabajan juntas en `EventService`

```
                        ┌──────────────────────────────────────────┐
                        │             EventService                 │
                        │                                          │
  registerStudent() ──► │  HashTable ──► ¿ya existe el ID? (O(1)) │
                        │                                          │
     ¿hay cupo? ──No──► │  Queue.enqueue()  (lista de espera)      │
          │             │                                          │
         Sí             │  AvlTree.insert() (registro, O(log n))   │
          │             │  HashTable.put()  (índice, O(1))         │
          └─────────────│  HistoryStack.push("REGISTERED")         │
                        │                                          │
  removeStudent() ─────►│  AvlTree.remove() (O(log n))             │
                        │  HashTable.remove() (O(1))               │
                        │  HistoryStack.push("REMOVAL", undoData)  │
                        │  Queue.dequeue() ──► promover al árbol   │
                        │                                          │
  undoLastRemoval() ───►│  HistoryStack.peek() ──► ¿es REMOVAL?    │
                        │  HistoryStack.pop() ──► restaurar        │
                        │  AvlTree.insert() + HashTable.put()      │
                        │                                          │
  getTopAttendees(k) ──►│  AvlTree.collectInOrder() (O(n))         │
                        │  MinHeap top-K (O(n log k))              │
                        └──────────────────────────────────────────┘
```

---

## Comparativa de complejidades

| Operación | Sin estructuras adecuadas | Con las estructuras del sistema |
|-----------|--------------------------|----------------------------------|
| Buscar estudiante por ID | O(n) lista lineal | O(1) HashTable |
| Registrar estudiante ordenado | O(n) inserción en arreglo | O(log n) AVL |
| Siguiente en lista de espera | O(n) búsqueda del más antiguo | O(1) Queue |
| Top-K por asistencia | O(n log n) ordenar todo | O(n log k) MinHeap |
| Deshacer última eliminación | O(n) buscar la última op. | O(1) HistoryStack |

---

## BST vs AVL (demostración en benchmarks)

El sistema incluye un `BstTree` sin balanceo **exclusivamente para benchmarks**.
Su propósito es demostrar empíricamente la diferencia de rendimiento:

| Escenario | BST altura | AVL altura |
|-----------|-----------|-----------|
| 1000 estudiantes en orden aleatorio | ~20 niveles | ~10 niveles |
| 1000 estudiantes insertados por ID creciente | ~1000 niveles | ~10 niveles |

Con IDs crecientes el BST degenera en lista y cada búsqueda recorre hasta 1000 nodos.
El AVL se rebalancea en cada inserción y nunca supera los ~10 niveles para 1000 nodos.
