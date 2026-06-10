package eventos.ui.gui;

import eventos.service.EventManager;
import eventos.service.EventService;

import javax.swing.table.AbstractTableModel;

public class EventTableModel extends AbstractTableModel {

    private static final String[] COLUMNS =
        {"#", "Event Name", "Capacity", "Enrolled", "Occupancy", "Queue", "Available"};

    private final EventManager manager;
    private EventService[]     events;

    public EventTableModel(EventManager manager) {
        this.manager = manager;
        refresh();
    }

    public void refresh() {
        events = manager.getEvents();
        fireTableDataChanged();
    }

    @Override public int    getRowCount()          { return events.length; }
    @Override public int    getColumnCount()       { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        EventService ev = events[row];
        return switch (col) {
            case 0 -> row + 1;
            case 1 -> ev.getName();
            case 2 -> ev.getCapacity();
            case 3 -> ev.getStudentCount();
            case 4 -> String.format("%.0f%%", ev.getOccupancyRate());
            case 5 -> ev.getQueueSize();
            case 6 -> ev.getCapacity() - ev.getStudentCount();
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 0, 2, 3, 5, 6 -> Integer.class;
            default             -> String.class;
        };
    }

    public EventService getEventAt(int row) {
        return (row >= 0 && row < events.length) ? events[row] : null;
    }
}
