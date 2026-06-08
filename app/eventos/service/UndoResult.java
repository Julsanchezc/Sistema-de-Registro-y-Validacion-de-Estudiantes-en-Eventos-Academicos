package eventos.service;

public enum UndoResult {
    UNDONE,
    UNDONE_TO_QUEUE,
    NO_OPERATIONS,
    NOT_A_REMOVAL,
    ERROR
}