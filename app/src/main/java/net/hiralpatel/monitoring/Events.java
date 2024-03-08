package net.hiralpatel.monitoring;

public class Events {

    public static Event InfoEvent(String message) {
        return new Event(LogLevel.INFO, message);
    }

    // You can add more factory methods for different log levels or specific types of events
    public static Event DebugEvent(String message) {
        return new Event(LogLevel.DEBUG, message);
    }

    public static Event ErrorEvent(String message) {
        return new Event(LogLevel.ERROR, message);
    }

}
