package net.hiralpatel.monitoring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum EventPublisher {
    INSTANCE;

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<Subscriber, Integer> subscriberErrorCounts = new ConcurrentHashMap<>();

    public void addSubscriber(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void removeSubscriber(Subscriber subscriber) {
        boolean removed = subscribers.remove(subscriber);
        if (removed) {
            // Optionally, also clear the error count for the removed subscriber
            subscriberErrorCounts.remove(subscriber);
            System.out.println("Subscriber removed successfully.");
        } else {
            System.out.println("Subscriber not found.");
        }
    }

    public void publishEvent(Object event) {
        subscribers.forEach(subscriber -> submitEvent(event, subscriber));
    }

    private void submitEvent(Object event, Subscriber subscriber) {
        executorService.submit(() -> {
            try {
                subscriber.handleEvent(event);
            } catch (Exception e) {
                handleSubscriberError(subscriber, e);
            }
        });
    }

    private void handleSubscriberError(Subscriber subscriber, Exception e) {
        int errorCount = subscriberErrorCounts.compute(subscriber, (sub, count) -> (count == null) ? 1 : count + 1);

        if (errorCount == 1 || errorCount % 10 == 0) {
            System.err.println("Error handling event in subscriber. Occurrence #" + errorCount + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
