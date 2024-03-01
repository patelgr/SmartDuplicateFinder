package net.hiralpatel.monitoring;

public interface Subscriber {
    void handleEvent(String event);
}
