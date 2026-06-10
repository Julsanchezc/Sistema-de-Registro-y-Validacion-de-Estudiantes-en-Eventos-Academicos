package eventos.service;

import core.AppLogger;
import eventos.model.Student;
import eventos.structures.HashTable;
import eventos.structures.MinHeap;
import org.slf4j.Logger;

import java.util.Comparator;

/*
 * Administra la colección de todos los eventos del sistema.
 *
 * Estructuras de datos utilizadas:
 *
 *   Lista enlazada simple (EventNode)
 *     Almacena los eventos en orden de creación. Se usa implementación propia
 *     en lugar de ArrayList para mantener consistencia con el resto del proyecto.
 *     Acceso por índice es O(n).
 *
 *   MinHeap (local en getEventsByAvailability)
 *     Ordena los eventos por cupos disponibles en O(n log n) sin modificar la lista original.
 *
 *   HashTable (local en getTopAttendeesGlobal)
 *     Agrega asistencias de todos los eventos en O(n) antes de construir el MinHeap de top-K.
 */
public class EventManager {

    private static final Logger log = AppLogger.get(EventManager.class);

    // Resumen de asistencia de un estudiante agregada entre todos los eventos.
    public static class AttendanceSummary {
        public final int    id;
        public final String name;
        public final int    totalAttendances;
        AttendanceSummary(int id, String name, int total) {
            this.id = id; this.name = name; this.totalAttendances = total;
        }
    }

    // Nodo de la lista enlazada que contiene un evento.
    private static class EventNode {
        EventService event;
        EventNode    next;
        EventNode(EventService e) { this.event = e; this.next = null; }
    }

    private EventNode head;       // cabeza de la lista enlazada de eventos
    private int       eventCount;

    public EventManager() { this.head = null; this.eventCount = 0; }

    // Crea un nuevo evento y lo agrega al final de la lista enlazada.
    public EventService createEvent(String name, int capacity) {
        EventService event = new EventService(name, capacity);
        addEvent(event);
        log.info("Event created: '{}' capacity={}", name, capacity);
        return event;
    }

    // Inserta un evento al final de la lista enlazada; O(n) para encontrar el último nodo.
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

    // Retorna el evento en la posición index (base 1); recorre la lista O(n).
    public EventService getEvent(int index) {
        if (index < 1 || index > eventCount) return null;
        EventNode cur = head;
        for (int i = 1; i < index; i++) cur = cur.next;
        return cur.event;
    }

    // Elimina el evento en la posición index desconectando su nodo de la lista enlazada.
    public boolean removeEvent(int index) {
        if (index < 1 || index > eventCount) return false;
        String name = getEvent(index).getName();
        if (index == 1) { head = head.next; }
        else {
            // Llegar al nodo anterior al que se elimina y saltárselo
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

    // Copia los eventos de la lista enlazada a un arreglo para facilitar iteración.
    public EventService[] getEvents() {
        EventService[] arr = new EventService[eventCount];
        EventNode      cur = head;
        for (int i = 0; i < eventCount; i++) { arr[i] = cur.event; cur = cur.next; }
        return arr;
    }

    /*
     * Retorna los eventos ordenados por cupos disponibles de menor a mayor (más lleno primero).
     *
     * Algoritmo: inserta todos los eventos en un MinHeap con comparador
     * (capacidad - ocupados) y los extrae en orden ascendente. O(n log n).
     * No modifica la lista enlazada original.
     */
    public EventService[] getEventsByAvailability() {
        EventService[] all = getEvents();
        if (all.length == 0) return all;
        // MinHeap con el evento de menos cupos libres en la raíz
        MinHeap<EventService> heap = new MinHeap<>(
            Comparator.comparingInt(ev -> ev.getCapacity() - ev.getStudentCount())
        );
        for (EventService ev : all) heap.insert(ev);
        EventService[] sorted = new EventService[all.length];
        for (int i = 0; i < sorted.length; i++) sorted[i] = heap.poll(); // extrae en orden ascendente
        return sorted;
    }

    /*
     * Retorna los K estudiantes con mayor asistencia sumada entre todos los eventos.
     *
     * Algoritmo en dos fases:
     *
     *   Fase 1 - Agregación con HashTable:
     *     Recorre todos los eventos y acumula el conteo de asistencias por ID en sumMap.
     *     nameMap guarda el nombre asociado a cada ID para no buscarlo después.
     *     O(n) en total de estudiantes.
     *
     *   Fase 2 - Top-K con MinHeap de tamaño k:
     *     Para cada entrada de sumMap inserta en el heap; si supera k elementos, expulsa el
     *     mínimo. Al terminar, el heap contiene los K estudiantes con mayor asistencia total.
     *     O(n log k).
     */
    public AttendanceSummary[] getTopAttendeesGlobal(int k) {
        if (k <= 0 || !hasEvents()) return new AttendanceSummary[0];

        // Fase 1: agregar asistencias por ID usando HashTable
        HashTable<Integer, int[]>  sumMap  = new HashTable<>(); // id -> [total asistencias]
        HashTable<Integer, String> nameMap = new HashTable<>(); // id -> nombre

        for (EventService ev : getEvents()) {
            for (Student s : ev.getStudentsSorted()) {
                int cnt = s.getAttendanceCount();
                if (cnt <= 0) continue;
                int[] existing = sumMap.get(s.getId());
                if (existing == null) {
                    sumMap.put(s.getId(), new int[]{ cnt });
                    nameMap.put(s.getId(), s.getName());
                } else {
                    existing[0] += cnt; // acumular en el arreglo de un elemento (paso por referencia)
                }
            }
        }

        // Fase 2: seleccionar top-K con MinHeap
        MinHeap<AttendanceSummary> heap = new MinHeap<>(
            Comparator.comparingInt(a -> a.totalAttendances)
        );
        sumMap.forEach((id, cnt) -> {
            heap.insert(new AttendanceSummary(id, nameMap.get(id), cnt[0]));
            if (heap.size() > k) heap.poll(); // expulsar el de menor asistencia para mantener tamaño k
        });

        AttendanceSummary[] result = new AttendanceSummary[heap.size()];
        for (int i = 0; i < result.length; i++) result[i] = heap.poll();
        // El heap drena en orden ascendente; invertir para obtener el mejor primero
        for (int i = 0, j = result.length - 1; i < j; i++, j--) {
            AttendanceSummary tmp = result[i]; result[i] = result[j]; result[j] = tmp;
        }
        return result;
    }

    public int     getEventCount() { return eventCount; }
    public boolean hasEvents()     { return head != null; }
}
