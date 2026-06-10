package eventos.model;

public class Student {

    private int     id;
    private String  name;
    private String  email;
    private String  program;
    private boolean attended;
    private int     attendanceCount;

    public Student(int id, String name, String email, String program) {
        this.id              = id;
        this.name            = name;
        this.email           = email;
        this.program         = program;
        this.attended        = false;
        this.attendanceCount = 0;
    }

    public int     getId()                   { return id; }
    public String  getName()                 { return name; }
    public String  getEmail()                { return email; }
    public String  getProgram()              { return program; }
    public boolean isAttended()              { return attended; }
    public int     getAttendanceCount()      { return attendanceCount; }
    public void    setAttended(boolean a)    { this.attended = a; }
    public void    setAttendanceCount(int c) { this.attendanceCount = c; }

    @Override
    public String toString() {
        return String.format("  ID: %-8d | %-25s | %-30s | %-20s | Attended: %s | Cnt: %d",
                id, name, email, program, attended ? "Yes" : "No", attendanceCount);
    }
}
