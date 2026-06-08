package eventos.service;

import core.AppLogger;
import eventos.model.Student;
import eventos.structures.AvlTree;
import eventos.structures.HistoryStack;
import eventos.structures.Queue;
import org.slf4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class EventService {

    private static final Logger log = AppLogger.get(EventService.class);

    private AvlTree        studentTree;
    private String         eventName;
    private int            maxCapacity;
    private Queue<Student> waitingQueue;
    private HistoryStack   history;
    private Student        lastPromoted;

    public EventService(String eventName, int maxCapacity) {
        this.studentTree  = new AvlTree();
        this.eventName    = eventName;
        this.maxCapacity  = maxCapacity;
        this.waitingQueue = new Queue<>();
        this.history      = new HistoryStack();
        this.lastPromoted = null;
        log.debug("EventService created: '{}' capacity={}", eventName, maxCapacity);
    }

    public RegisterResult registerStudent(int id, String name, String email, String program) {
        if (!isValidInput(id, name, email)) return RegisterResult.INVALID_DATA;
        if (studentTree.contains(id))       return RegisterResult.DUPLICATE_ID;

        if (studentTree.getStudentCount() >= maxCapacity) {
            if (isInQueue(id)) return RegisterResult.DUPLICATE_IN_QUEUE;
            waitingQueue.enqueue(new Student(id, name, email, program));
            history.push("QUEUED", "ID:" + id + " - " + name + " (pos." + waitingQueue.size() + ")");
            log.info("Student queued: id={} name='{}' pos={}", id, name, waitingQueue.size());
            return RegisterResult.QUEUED;
        }

        Student student = new Student(id, name, email, program);
        if (studentTree.insert(student)) {
            history.push("REGISTERED", "ID:" + id + " - " + name);
            log.info("Student registered: id={} name='{}'", id, name);
            return RegisterResult.REGISTERED;
        }
        return RegisterResult.INVALID_DATA;
    }

    // Used for bulk/random generation. Returns 1=registered, 0=queued, -1=skipped.
    public int registerBulk(int id, String name, String email, String program) {
        if (!isValidInput(id, name, email)) return -1;
        if (studentTree.contains(id))       return -1;

        if (studentTree.getStudentCount() >= maxCapacity) {
            if (isInQueue(id)) return -1;
            waitingQueue.enqueue(new Student(id, name, email, program));
            history.push("QUEUED", "ID:" + id + " - " + name + " (bulk pos." + waitingQueue.size() + ")");
            return 0;
        }

        Student student = new Student(id, name, email, program);
        if (studentTree.insert(student)) {
            history.push("REGISTERED", "ID:" + id + " - " + name + " (bulk)");
            return 1;
        }
        return -1;
    }

    // Used by benchmarks only — skips history and queue.
    public boolean registerQuiet(Student s) {
        if (studentTree.getStudentCount() >= maxCapacity) return false;
        return studentTree.insert(s);
    }

    public void loadStudent(int id, String name, String email, String program, boolean attended) {
        Student s = new Student(id, name, email, program);
        s.setAttended(attended);
        studentTree.insert(s);
    }

    public void loadToQueue(int id, String name, String email, String program) {
        waitingQueue.enqueue(new Student(id, name, email, program));
    }

    public boolean hasStudent(int id)  { return studentTree.contains(id); }
    public Student findStudent(int id) { return studentTree.find(id); }

    public boolean markAttendance(int id) {
        Student s = studentTree.find(id);
        if (s == null) return false;
        s.setAttended(true);
        history.push("ATTENDANCE", "ID:" + id + " - " + s.getName());
        log.info("Attendance marked: id={}", id);
        return true;
    }

    public boolean removeStudent(int id) {
        Student s  = studentTree.find(id);
        boolean ok = studentTree.remove(id);
        if (ok) {
            history.push("REMOVAL", "ID:" + id + " - " + s.getName(), s);
            log.info("Student removed: id={}", id);
            lastPromoted = promoteFromQueue();
        }
        return ok;
    }

    public boolean removeQuiet(int id) { return studentTree.remove(id); }

    private Student promoteFromQueue() {
        if (waitingQueue.isEmpty()) return null;
        Student promoted = waitingQueue.dequeue();
        studentTree.insert(promoted);
        history.push("PROMOTED", "ID:" + promoted.getId() + " - " + promoted.getName());
        log.info("Student promoted from queue: id={}", promoted.getId());
        return promoted;
    }

    public Student getLastPromoted() { return lastPromoted; }

    public UndoResult undoLastRemoval() {
        HistoryStack.Entry e = history.peek();
        if (e == null)                                       return UndoResult.NO_OPERATIONS;
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
            history.push("UNDO", "Restored ID:" + s.getId() + " - " + s.getName());
            log.info("Undo successful: id={}", s.getId());
            return UndoResult.UNDONE;
        }
        return UndoResult.ERROR;
    }

    public HistoryStack.Entry getLastHistoryEntry() { return history.peek(); }

    public int clearAll() {
        int n = studentTree.getStudentCount();
        studentTree  = new AvlTree();
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
            pw.println("id,name,email,program,attended");
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