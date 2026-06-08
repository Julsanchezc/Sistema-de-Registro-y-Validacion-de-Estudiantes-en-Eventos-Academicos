package eventos.service;

public enum RegisterResult {
    REGISTERED,
    QUEUED,
    DUPLICATE_ID,
    DUPLICATE_IN_QUEUE,
    INVALID_DATA
}