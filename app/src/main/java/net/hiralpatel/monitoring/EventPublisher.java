package net.hiralpatel.monitoring;

import java.util.ArrayList;
import java.util.List;

public enum EventPublisher {
    INSTANCE;

    private final List<Subscriber> subscribers = new ArrayList<>();

    public void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void publishEvent(String event) {
        for (Subscriber subscriber : subscribers) {
            subscriber.handleEvent(event);
        }
    }
}
