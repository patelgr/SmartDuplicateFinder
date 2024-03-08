package net.hiralpatel.monitoring;

public interface Subscriber {
    void handleEvent(Event event);
    boolean isInterestedIn(LogLevel logLevel);
}

