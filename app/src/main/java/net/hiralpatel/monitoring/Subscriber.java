package net.hiralpatel.monitoring;

public interface Subscriber {
    void handleEvent(Object event);
}
