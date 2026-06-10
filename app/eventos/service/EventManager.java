package eventos.service;

import core.AppLogger;
import eventos.model.Student;
import eventos.structures.HashTable;
import eventos.structures.MinHeap;
import org.slf4j.Logger;

import java.util.Comparator;

public class EventManager {

    private static final Logger log = AppLogger.get(EventManager.class);

    // Summary of a student's attendance aggregated across all events
    public static class AttendanceSummary {
        public final int    id;
        public final String name;
        public final int    totalAttendances;
        AttendanceSummary(int id, String name, int total) {
            this.id = id; this.name = name; this.totalAttendances = total;
        }
    }

    private static class EventNode {
        EventService event;
        EventNode    next;
        EventNode(EventService e) { this.event = e; this.next = null; }
    }

    private EventNode head;
    private int       eventCount;

    public EventManager() { this.head = null; this.eventCount = 0; }

    public EventService createEvent(String name, int capacity) {
        EventService event = new EventService(name, capacity);
        addEvent(event);
        log.info("Event created: '{}' capacity={}", name, capacity);
        return event;
    }

    private void addEvent(EventService e) {
        EventNode node = new EventNode(e);
        if (head == null) { head = node; }
        else {
            EventNode cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = node;
        }
        eventCount++;
    }

    public EventService getEvent(int index) {
        if (index < 1 || index > eventCount) return null;
        EventNode cur = head;
        for (int i = 1; i < index; i++) cur = cur.next;
        return cur.event;
    }

    public boolean removeEvent(int index) {
        if (index < 1 || index > eventCount) return false;
        String name = getEvent(index).getName();
        if (index == 1) { head = head.next; }
        else {
            EventNode cur = head;
            for (int i = 1; i < index - 1; i++) cur = cur.next;
            cur.next = cur.next.next;
        }
        eventCount--;
        log.info("Event removed: '{}'", name);
        return true;
    }

    public void clear() {
        head       = null;
        eventCount = 0;
    }

    public EventService[] getEvents() {
        EventService[] arr = new EventService[eventCount];
        EventNode      cur = head;
        for (int i = 0; i < eventCount; i++) { arr[i] = cur.event; cur = cur.next; }
        return arr;
    }

    // Returns events ordered by available capacity ascending (least available = most urgent first).
    // Uses MinHeap: O(n log n).
    public EventService[] getEventsByAvailability() {
        EventService[] all = getEvents();
        if (all.length == 0) return all;
        MinHeap<EventService> heap = new MinHeap<>(
            Comparator.comparingInt(ev -> ev.getCapacity() - ev.getStudentCount())
        );
        for (EventService ev : all) heap.insert(ev);
        EventService[] sorted = new EventService[all.length];
        for (int i = 0; i < sorted.length; i++) sorted[i] = heap.poll();
        return sorted;
    }

    // Returns the top K students by total attendance across all events.
    // Uses HashTable for aggregation + MinHeap for top-K selection: O(n log k).
    public AttendanceSummary[] getTopAttendeesGlobal(int k) {
        if (k <= 0 || !hasEvents()) return new AttendanceSummary[0];

        HashTable<Integer, int[]>  sumMap  = new HashTable<>();
        HashTable<Integer, String> nameMap = new HashTable<>();

        for (EventService ev : getEvents()) {
            for (Student s : ev.getStudentsSorted()) {
                int cnt = s.getAttendanceCount();
                if (cnt <= 0) continue;
                int[] existing = sumMap.get(s.getId());
                if (existing == null) {
                    sumMap.put(s.getId(), new int[]{ cnt });
                    nameMap.put(s.getId(), s.getName());
                } else {
                    existing[0] += cnt;
                }
            }
        }

        MinHeap<AttendanceSummary> heap = new MinHeap<>(
            Comparator.comparingInt(a -> a.totalAttendances)
        );
        sumMap.forEach((id, cnt) -> {
            heap.insert(new AttendanceSummary(id, nameMap.get(id), cnt[0]));
            if (heap.size() > k) heap.poll(); // evict the lowest
        });

        AttendanceSummary[] result = new AttendanceSummary[heap.size()];
        for (int i = 0; i < result.length; i++) result[i] = heap.poll();
        // Reverse: heap drains ascending, we want descending (best first)
        for (int i = 0, j = result.length - 1; i < j; i++, j--) {
            AttendanceSummary tmp = result[i]; result[i] = result[j]; result[j] = tmp;
        }
        return result;
    }

    public int     getEventCount() { return eventCount; }
    public boolean hasEvents()     { return head != null; }
}
