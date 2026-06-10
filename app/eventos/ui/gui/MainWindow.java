package eventos.ui.gui;

import eventos.repository.EventRepository;
import eventos.service.EventManager;
import eventos.service.EventService;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final EventManager    manager;
    private final EventRepository repository;

    private final EventListPanel    eventListPanel;
    private final EventDetailPanel  eventDetailPanel;
    private final TopAttendeesPanel topAttendeesPanel;

    private final JPanel      eventsCard;
    private final CardLayout  eventsCardLayout;
    private final JTabbedPane tabbedPane;

    public MainWindow() {
        manager           = new EventManager();
        repository        = new EventRepository();
        eventListPanel    = new EventListPanel(manager, repository, this);
        eventDetailPanel  = new EventDetailPanel(this);
        topAttendeesPanel = new TopAttendeesPanel(manager);

        eventsCardLayout = new CardLayout();
        eventsCard       = new JPanel(eventsCardLayout);
        tabbedPane       = new JTabbedPane();

        buildUI();
    }

    private void buildUI() {
        setTitle("Student Event Registration System — UNAL 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        eventsCard.add(eventListPanel,   "list");
        eventsCard.add(eventDetailPanel, "detail");
        eventsCardLayout.show(eventsCard, "list");

        tabbedPane.addTab("Events",        eventsCard);
        tabbedPane.addTab("Top Attendees", topAttendeesPanel);

        add(tabbedPane, BorderLayout.CENTER);

        JLabel status = new JLabel(
            "  Structures: AVL · BST · Queue · HistoryStack · LinkedList · HashTable · MinHeap");
        status.setFont(status.getFont().deriveFont(Font.PLAIN, 11f));
        status.setForeground(Color.GRAY);
        status.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        add(status, BorderLayout.SOUTH);
    }

    public void openEvent(EventService event) {
        eventDetailPanel.setEvent(event);
        topAttendeesPanel.setCurrentEvent(event);
        eventsCardLayout.show(eventsCard, "detail");
        tabbedPane.setSelectedIndex(0);
    }

    public void backToList() {
        eventListPanel.refresh();
        eventsCardLayout.show(eventsCard, "list");
    }

    public EventManager      getManager()           { return manager; }
    public EventRepository   getRepository()        { return repository; }
    public TopAttendeesPanel getTopAttendeesPanel() { return topAttendeesPanel; }
}
