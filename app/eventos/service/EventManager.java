package eventos.service;

import core.AppLogger;
import org.slf4j.Logger;

public class EventManager {

    private static final Logger log = AppLogger.get(EventManager.class);

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

    public int     getEventCount() { return eventCount; }
    public boolean hasEvents()     { return head != null; }
}