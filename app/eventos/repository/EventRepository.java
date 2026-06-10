package eventos.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import core.AppLogger;
import core.exceptions.PersistenceException;
import eventos.model.Student;
import eventos.service.EventManager;
import eventos.service.EventService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private static final Logger       log    = AppLogger.get(EventRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static class SystemDTO {
        public List<EventDTO> events = new ArrayList<>();
    }

    public static class EventDTO {
        public String           name;
        public int              maxCapacity;
        public List<StudentDTO> students = new ArrayList<>();
        public List<StudentDTO> queue    = new ArrayList<>();
    }

    public static class StudentDTO {
        public int     id;
        public String  name;
        public String  email;
        public String  program;
        public boolean attended;
        public int     attendanceCount;
    }

    public void save(EventManager manager, Path path) {
        SystemDTO dto = new SystemDTO();

        for (EventService ev : manager.getEvents()) {
            EventDTO edto = new EventDTO();
            edto.name        = ev.getName();
            edto.maxCapacity = ev.getCapacity();

            for (Object obj : ev.getStudentsSorted()) {
                Student s     = (Student) obj;
                StudentDTO sd = new StudentDTO();
                sd.id              = s.getId();
                sd.name            = s.getName();
                sd.email           = s.getEmail();
                sd.program         = s.getProgram();
                sd.attended        = s.isAttended();
                sd.attendanceCount = s.getAttendanceCount();
                edto.students.add(sd);
            }

            for (Object obj : ev.getQueueContents()) {
                Student s     = (Student) obj;
                StudentDTO sd = new StudentDTO();
                sd.id              = s.getId();
                sd.name            = s.getName();
                sd.email           = s.getEmail();
                sd.program         = s.getProgram();
                sd.attended        = false;
                sd.attendanceCount = 0;
                edto.queue.add(sd);
            }

            dto.events.add(edto);
        }

        try {
            path.getParent().toFile().mkdirs();
            MAPPER.writeValue(path.toFile(), dto);
            log.info("Data saved to: {}", path.toAbsolutePath());
        } catch (IOException e) {
            log.error("Error saving: {}", e.getMessage());
            throw new PersistenceException("Failed to save to " + path, e);
        }
    }

    public void load(EventManager manager, Path path) {
        if (!path.toFile().exists())
            throw new PersistenceException("File not found: " + path);

        try {
            SystemDTO dto = MAPPER.readValue(path.toFile(), SystemDTO.class);
            manager.clear();

            for (EventDTO edto : dto.events) {
                EventService ev = manager.createEvent(edto.name, edto.maxCapacity);
                for (StudentDTO sd : edto.students)
                    ev.loadStudent(sd.id, sd.name, sd.email, sd.program,
                                   sd.attended, sd.attendanceCount);
                for (StudentDTO sd : edto.queue)
                    ev.loadToQueue(sd.id, sd.name, sd.email, sd.program);
            }

            log.info("Data loaded from: {} ({} event(s))", path.toAbsolutePath(), dto.events.size());
        } catch (IOException e) {
            log.error("Error loading: {}", e.getMessage());
            throw new PersistenceException("Failed to load from " + path, e);
        }
    }
}
