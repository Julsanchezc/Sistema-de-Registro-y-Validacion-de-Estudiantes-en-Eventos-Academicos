package eventos.service;

import core.AppLogger;
import eventos.model.Student;
import eventos.structures.AvlTree;
import eventos.structures.HashTable;
import eventos.structures.HistoryStack;
import eventos.structures.MinHeap;
import eventos.structures.Queue;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;

/*
 * Gestiona todos los datos de un único evento académico coordinando cinco estructuras:
 *
 *   AvlTree studentTree
 *     Almacena los estudiantes registrados ordenados por ID.
 *     Garantiza O(log n) en inserción/búsqueda/eliminación.
 *     El recorrido in-order produce la lista ordenada para CSV y top-K.
 *
 *   HashTable<Integer, Student> index
 *     Índice paralelo al árbol para buscar por ID en O(1) promedio.
 *     Se mantiene sincronizado con el árbol: toda inserción/eliminación actualiza ambas.
 *
 *   Queue<Student> waitingQueue
 *     Lista de espera FIFO. Cuando el evento está lleno, los nuevos estudiantes se encolan
 *     y son promovidos al árbol en orden de llegada cuando se libera un cupo.
 *
 *   HistoryStack history
 *     Pila LIFO de operaciones. Registra cada acción y guarda los datos necesarios
 *     para deshacer eliminaciones (undo).
 *
 *   MinHeap (local en getTopAttendees)
 *     No se mantiene como campo permanente; se construye al vuelo para calcular
 *     el top-K en O(n log k).
 */
public class EventService {

    private static final Logger log = AppLogger.get(EventService.class);

    private AvlTree                     studentTree;  // fuente de verdad, ordenada por ID
    private HashTable<Integer, Student> index;        // acceso O(1) por ID, sincronizado con el árbol
    private String                      eventName;
    private int                         maxCapacity;
    private Queue<Student>              waitingQueue; // lista de espera FIFO
    private HistoryStack                history;      // historial de operaciones y soporte para undo
    private Student                     lastPromoted; // último estudiante promovido desde la cola

    public EventService(String eventName, int maxCapacity) {
        this.studentTree  = new AvlTree();
        this.index        = new HashTable<>();
        this.eventName    = eventName;
        this.maxCapacity  = maxCapacity;
        this.waitingQueue = new Queue<>();
        this.history      = new HistoryStack();
        this.lastPromoted = null;
        log.debug("EventService created: '{}' capacity={}", eventName, maxCapacity);
    }

    /*
     * Registra un estudiante en el evento:
     *   1. Valida los datos de entrada.
     *   2. Verifica duplicados en el índice (HashTable) en O(1).
     *   3. Si hay cupo: inserta en el árbol AVL y en el índice.
     *   4. Si no hay cupo: encola en la cola de espera (Queue).
     * Toda operación exitosa queda registrada en la pila de historial.
     */
    public RegisterResult registerStudent(int id, String name, String email, String program) {
        if (!isValidInput(id, name, email)) return RegisterResult.INVALID_DATA;
        if (index.containsKey(id))          return RegisterResult.DUPLICATE_ID; // O(1) en HashTable

        if (studentTree.getStudentCount() >= maxCapacity) {
            if (isInQueue(id)) return RegisterResult.DUPLICATE_IN_QUEUE;
            waitingQueue.enqueue(new Student(id, name, email, program));
            history.push("QUEUED", "ID:" + id + " - " + name + " (pos." + waitingQueue.size() + ")");
            log.info("Student queued: id={} name='{}' pos={}", id, name, waitingQueue.size());
            return RegisterResult.QUEUED;
        }

        Student student = new Student(id, name, email, program);
        if (studentTree.insert(student)) {     // O(log n) en AvlTree
            index.put(id, student);            // O(1) en HashTable
            history.push("REGISTERED", "ID:" + id + " - " + name);
            log.info("Student registered: id={} name='{}'", id, name);
            return RegisterResult.REGISTERED;
        }
        return RegisterResult.INVALID_DATA;
    }

    // Versión para carga masiva/generación aleatoria. Retorna 1=registrado, 0=encolado, -1=ignorado.
    public int registerBulk(int id, String name, String email, String program) {
        if (!isValidInput(id, name, email)) return -1;
        if (index.containsKey(id))          return -1;

        if (studentTree.getStudentCount() >= maxCapacity) {
            if (isInQueue(id)) return -1;
            waitingQueue.enqueue(new Student(id, name, email, program));
            history.push("QUEUED", "ID:" + id + " - " + name + " (bulk pos." + waitingQueue.size() + ")");
            return 0;
        }

        Student student = new Student(id, name, email, program);
        if (studentTree.insert(student)) {
            index.put(id, student);
            history.push("REGISTERED", "ID:" + id + " - " + name + " (bulk)");
            return 1;
        }
        return -1;
    }

    // Solo para benchmarks: omite historial, cola e índice para medir el árbol AVL puro.
    public boolean registerQuiet(Student s) {
        if (studentTree.getStudentCount() >= maxCapacity) return false;
        return studentTree.insert(s);
    }

    // Carga un estudiante desde persistencia (JSON); restaura estado completo sin pasar por la lógica de negocio.
    public void loadStudent(int id, String name, String email, String program,
                            boolean attended, int attendanceCount) {
        Student s = new Student(id, name, email, program);
        s.setAttended(attended);
        s.setAttendanceCount(attendanceCount);
        studentTree.insert(s);
        index.put(id, s);
    }

    // Carga un estudiante en la cola de espera desde persistencia.
    public void loadToQueue(int id, String name, String email, String program) {
        waitingQueue.enqueue(new Student(id, name, email, program));
    }

    public boolean hasStudent(int id)  { return index.containsKey(id); }
    public Student findStudent(int id) { return index.get(id); } // O(1) via HashTable

    // Marca asistencia; actualiza el contador y registra la acción en el historial.
    public boolean markAttendance(int id) {
        Student s = index.get(id); // O(1) via HashTable
        if (s == null) return false;
        s.setAttended(true);
        s.setAttendanceCount(s.getAttendanceCount() + 1);
        history.push("ATTENDANCE", "ID:" + id + " - " + s.getName()
                + " (count:" + s.getAttendanceCount() + ")");
        log.info("Attendance marked: id={} count={}", id, s.getAttendanceCount());
        return true;
    }

    // Elimina un estudiante: borra del árbol y del índice, guarda los datos en el historial
    // para undo, y promueve al primero de la cola de espera para ocupar el cupo liberado.
    public boolean removeStudent(int id) {
        Student s  = index.get(id);
        boolean ok = studentTree.remove(id); // O(log n) en AvlTree
        if (ok) {
            index.remove(id);                // O(1) en HashTable
            // undoData guarda el estudiante para que HistoryStack permita restaurarlo
            history.push("REMOVAL", "ID:" + id + " - " + s.getName(), s);
            log.info("Student removed: id={}", id);
            lastPromoted = promoteFromQueue();
        }
        return ok;
    }

    // Versión silenciosa de eliminación para uso interno (no actualiza historial).
    public boolean removeQuiet(int id) {
        boolean ok = studentTree.remove(id);
        if (ok) index.remove(id);
        return ok;
    }

    // Extrae el primero de la cola FIFO y lo registra en el árbol e índice.
    // Se llama automáticamente después de cada eliminación para ocupar el cupo liberado.
    private Student promoteFromQueue() {
        if (waitingQueue.isEmpty()) return null;
        Student promoted = waitingQueue.dequeue(); // extrae el frente de la Queue (FIFO)
        studentTree.insert(promoted);
        index.put(promoted.getId(), promoted);
        history.push("PROMOTED", "ID:" + promoted.getId() + " - " + promoted.getName());
        log.info("Student promoted from queue: id={}", promoted.getId());
        return promoted;
    }

    public Student getLastPromoted() { return lastPromoted; }

    // Deshace la última eliminación usando la pila de historial.
    // Inspecciona el tope con peek: si el tipo es "REMOVAL" y tiene undoData,
    // restaura al estudiante. Si el evento está lleno, lo envía a la cola de espera.
    public UndoResult undoLastRemoval() {
        HistoryStack.Entry e = history.peek();
        if (e == null)                                        return UndoResult.NO_OPERATIONS;
        if (!e.type.equals("REMOVAL") || e.undoData == null) return UndoResult.NOT_A_REMOVAL;
        history.pop();
        Student s = e.undoData;

        if (studentTree.getStudentCount() >= maxCapacity) {
            // Evento lleno: el estudiante restaurado va a la cola de espera
            waitingQueue.enqueue(s);
            history.push("QUEUED", "ID:" + s.getId() + " - " + s.getName() + " (undo->queue)");
            log.info("Undo: id={} moved to queue (event full)", s.getId());
            return UndoResult.UNDONE_TO_QUEUE;
        }

        if (studentTree.insert(s)) {
            index.put(s.getId(), s);
            history.push("UNDO", "Restored ID:" + s.getId() + " - " + s.getName());
            log.info("Undo successful: id={}", s.getId());
            return UndoResult.UNDONE;
        }
        return UndoResult.ERROR;
    }

    /*
     * Retorna los K estudiantes con mayor asistencia usando un MinHeap de tamaño k (O(n log k)).
     *
     * Algoritmo:
     *   1. Obtiene todos los estudiantes del árbol en orden ascendente por ID (O(n)).
     *   2. Mantiene un MinHeap de tamaño k: si el nuevo estudiante supera al mínimo actual,
     *      el mínimo es expulsado con poll y entra el nuevo.
     *      Al terminar, el heap contiene exactamente los K con mayor asistencia.
     *   3. Extrae el heap y revierte el arreglo para obtener orden descendente (mejor primero).
     */
    public Student[] getTopAttendees(int k) {
        if (k <= 0) return new Student[0];
        Student[] all  = studentTree.collectInOrder();
        // MinHeap con comparador por asistencia: el de menor asistencia queda en la raíz (tope)
        MinHeap<Student> heap = new MinHeap<>(Comparator.comparingInt(Student::getAttendanceCount));
        for (Student s : all) {
            heap.insert(s);
            if (heap.size() > k) heap.poll(); // expulsar el de menor asistencia para mantener tamaño k
        }
        Student[] result = new Student[heap.size()];
        for (int i = 0; i < result.length; i++) result[i] = heap.poll();
        // El heap drena en orden ascendente; invertir para obtener el mejor primero
        for (int i = 0, j = result.length - 1; i < j; i++, j--) {
            Student tmp = result[i]; result[i] = result[j]; result[j] = tmp;
        }
        return result;
    }

    public HistoryStack.Entry getLastHistoryEntry() { return history.peek(); }

    // Reinicia el evento: recrea las tres estructuras principales y lo registra en el historial.
    public int clearAll() {
        int n = studentTree.getStudentCount();
        studentTree  = new AvlTree();
        index        = new HashTable<>();
        waitingQueue = new Queue<>();
        history.push("CLEARED", "Removed " + n + " students and cleared queue");
        log.warn("List cleared: {} students removed from event '{}'", n, eventName);
        return n;
    }

    public boolean exportCSV(String path) {
        if (studentTree.isEmpty()) return false;
        java.io.File file = new java.io.File(path);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("id,name,email,program,attended,attendanceCount");
            studentTree.writeInOrderCSV(pw); // in-order del árbol AVL = CSV ordenado por ID
            history.push("EXPORT", "CSV -> " + file.getName());
            log.info("CSV exported: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Error exporting CSV: {}", e.getMessage());
            return false;
        }
    }

    public Student[]            getStudentsSorted()  { return studentTree.collectInOrder(); }
    public Object[]             getQueueContents()   { return waitingQueue.contents(); }
    public HistoryStack.Entry[] getHistoryEntries()  { return history.entries(); }
    public void                 visualizeTree()      { studentTree.visualize(); }

    public String  getName()            { return eventName; }
    public int     getStudentCount()    { return studentTree.getStudentCount(); }
    public int     getAttendanceCount() { return studentTree.countAttendances(); }
    public int     getQueueSize()       { return waitingQueue.size(); }
    public int     getHistorySize()     { return history.size(); }
    public int     getTreeHeight()      { return studentTree.getHeight(); }
    public int     getCapacity()        { return maxCapacity; }
    public boolean isEmpty()            { return studentTree.isEmpty(); }

    public double getOccupancyRate() {
        return (double) getStudentCount() / maxCapacity * 100;
    }

    private boolean isValidInput(int id, String name, String email) {
        return id > 0
            && name  != null && !name.isBlank()
            && email != null && email.contains("@");
    }

    // Recorre el arreglo de la cola para verificar si un ID ya está esperando. O(n) en la cola.
    private boolean isInQueue(int id) {
        for (Object obj : waitingQueue.contents())
            if (((Student) obj).getId() == id) return true;
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s  [%d/%d  %.0f%%  queue:%d]",
                eventName, getStudentCount(), maxCapacity,
                getOccupancyRate(), getQueueSize());
    }
}
