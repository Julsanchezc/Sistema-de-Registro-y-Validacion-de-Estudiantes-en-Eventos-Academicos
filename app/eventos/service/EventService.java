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

public class EventService {

    private static final Logger log = AppLogger.get(EventService.class);

    private AvlTree                  studentTree;
    private HashTable<Integer, Student> index;   // O(1) lookup by student ID
    private String                   eventName;
    private int                      maxCapacity;
    private Queue<Student>           waitingQueue;
    private HistoryStack             history;
    private Student                  lastPromoted;

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

    public RegisterResult registerStudent(int id, String name, String email, String program) {
        if (!isValidInput(id, name, email)) return RegisterResult.INVALID_DATA;
        if (index.containsKey(id))          return RegisterResult.DUPLICATE_ID;

        if (studentTree.getStudentCount() >= maxCapacity) {
            if (isInQueue(id)) return RegisterResult.DUPLICATE_IN_QUEUE;
            waitingQueue.enqueue(new Student(id, name, email, program));
            history.push("QUEUED", "ID:" + id + " - " + name + " (pos." + waitingQueue.size() + ")");
            log.info("Student queued: id={} name='{}' pos={}", id, name, waitingQueue.size());
            return RegisterResult.QUEUED;
        }

        Student student = new Student(id, name, email, program);
        if (studentTree.insert(student)) {
            index.put(id, student);
            history.push("REGISTERED", "ID:" + id + " - " + name);
            log.info("Student registered: id={} name='{}'", id, name);
            return RegisterResult.REGISTERED;
        }
        return RegisterResult.INVALID_DATA;
    }

    // Used for bulk/random generation. Returns 1=registered, 0=queued, -1=skipped.
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

    // Used by benchmarks only — skips history, queue, and index.
    public boolean registerQuiet(Student s) {
        if (studentTree.getStudentCount() >= maxCapacity) return false;
        return studentTree.insert(s);
    }

    public void loadStudent(int id, String name, String email, String program,
                            boolean attended, int attendanceCount) {
        Student s = new Student(id, name, email, program);
        s.setAttended(attended);
        s.setAttendanceCount(attendanceCount);
        studentTree.insert(s);
        index.put(id, s);
    }

    public void loadToQueue(int id, String name, String email, String program) {
        waitingQueue.enqueue(new Student(id, name, email, program));
    }

    public boolean hasStudent(int id)  { return index.containsKey(id); }
    public Student findStudent(int id) { return index.get(id); }  // O(1)

    public boolean markAttendance(int id) {
        Student s = index.get(id);
        if (s == null) return false;
        s.setAttended(true);
        s.setAttendanceCount(s.getAttendanceCount() + 1);
        history.push("ATTENDANCE", "ID:" + id + " - " + s.getName()
                + " (count:" + s.getAttendanceCount() + ")");
        log.info("Attendance marked: id={} count={}", id, s.getAttendanceCount());
        return true;
    }

    public boolean removeStudent(int id) {
        Student s  = index.get(id);
        boolean ok = studentTree.remove(id);
        if (ok) {
            index.remove(id);
            history.push("REMOVAL", "ID:" + id + " - " + s.getName(), s);
            log.info("Student removed: id={}", id);
            lastPromoted = promoteFromQueue();
        }
        return ok;
    }

    public boolean removeQuiet(int id) {
        boolean ok = studentTree.remove(id);
        if (ok) index.remove(id);
        return ok;
    }

    private Student promoteFromQueue() {
        if (waitingQueue.isEmpty()) return null;
        Student promoted = waitingQueue.dequeue();
        studentTree.insert(promoted);
        index.put(promoted.getId(), promoted);
        history.push("PROMOTED", "ID:" + promoted.getId() + " - " + promoted.getName());
        log.info("Student promoted from queue: id={}", promoted.getId());
        return promoted;
    }

    public Student getLastPromoted() { return lastPromoted; }

    public UndoResult undoLastRemoval() {
        HistoryStack.Entry e = history.peek();
        if (e == null)                                        return UndoResult.NO_OPERATIONS;
        if (!e.type.equals("REMOVAL") || e.undoData == null) return UndoResult.NOT_A_REMOVAL;
        history.pop();
        Student s = e.undoData;

        if (studentTree.getStudentCount() >= maxCapacity) {
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

    // Returns the top K students by attendance count using a MinHeap (O(n log k)).
    public Student[] getTopAttendees(int k) {
        if (k <= 0) return new Student[0];
        Student[] all  = studentTree.collectInOrder();
        MinHeap<Student> heap = new MinHeap<>(Comparator.comparingInt(Student::getAttendanceCount));
        for (Student s : all) {
            heap.insert(s);
            if (heap.size() > k) heap.poll(); // evict the lowest
        }
        Student[] result = new Student[heap.size()];
        for (int i = 0; i < result.length; i++) result[i] = heap.poll();
        // Reverse: heap drains ascending (min first), we want descending (best first)
        for (int i = 0, j = result.length - 1; i < j; i++, j--) {
            Student tmp = result[i]; result[i] = result[j]; result[j] = tmp;
        }
        return result;
    }

    public HistoryStack.Entry getLastHistoryEntry() { return history.peek(); }

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
            studentTree.writeInOrderCSV(pw);
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
