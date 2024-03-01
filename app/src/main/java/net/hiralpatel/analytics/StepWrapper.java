package net.hiralpatel.analytics;

import net.hiralpatel.monitoring.EventPublisher;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StepWrapper {

    private final EventPublisher publisher;

    public StepWrapper() {
        this.publisher = EventPublisher.INSTANCE; // Assuming EventPublisher is a singleton
    }

    public <T> T execute(String stepName, Supplier<T> stepLogic) {
        publisher.publishEvent("Starting " + stepName);

        long startTime = System.currentTimeMillis();
        T result = stepLogic.get(); // Execute the step logic
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        long totalSize = 0;
        if (result instanceof Map) {
            for (Object value : ((Map) result).values()) {
                if (value instanceof List) {
                    totalSize += ((List) value).size();
                }
            }
        } else if (result instanceof List) {
            totalSize = ((List) result).size();
        }


        StepStats stats = new StepStats(duration, totalSize);
        publisher.publishEvent(String.format("Completed %s in %d ms. Total items: %d", stepName, duration, totalSize));

        return result;
    }
}
