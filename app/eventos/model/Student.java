package eventos.model;

public class Student {

    private int     id;
    private String  name;
    private String  email;
    private String  program;
    private boolean attended;

    public Student(int id, String name, String email, String program) {
        this.id       = id;
        this.name     = name;
        this.email    = email;
        this.program  = program;
        this.attended = false;
    }

    public int     getId()                 { return id; }
    public String  getName()               { return name; }
    public String  getEmail()              { return email; }
    public String  getProgram()            { return program; }
    public boolean isAttended()            { return attended; }
    public void    setAttended(boolean a)  { this.attended = a; }

    @Override
    public String toString() {
        return String.format("  ID: %-8d | %-25s | %-30s | %-20s | Attended: %s",
                id, name, email, program, attended ? "Yes" : "No");
    }
}