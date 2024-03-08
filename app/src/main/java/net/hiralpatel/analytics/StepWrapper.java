package net.hiralpatel.analytics;

import net.hiralpatel.monitoring.EventPublisher;
import net.hiralpatel.monitoring.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StepWrapper {

    private final EventPublisher publisher;
    private static final List<StepStats> stepStatsList = new ArrayList<>();

    public StepWrapper() {
        this.publisher = EventPublisher.INSTANCE; // Assuming EventPublisher is a singleton
    }

    public <T> T execute(String stepName, Supplier<T> stepLogic) {
        publisher.publishEvent(Events.InfoEvent("Starting " + stepName));

        long startTime = System.currentTimeMillis();
        T result = stepLogic.get(); // Execute the step logic
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        long totalSize = 0;
        if (result instanceof Map) {
            for (Object value : ((Map) result).values()) {
                if (value instanceof List) {
                    totalSize += ((List<?>) value).size();
                }
            }
        } else if (result instanceof List) {
            totalSize = ((List<?>) result).size();
        }


        StepStats stats = new StepStats(duration, totalSize, stepName);
        stepStatsList.add(stats);
        publisher.publishEvent(Events.InfoEvent(String.format("Completed %s in %d ms. Total items: %d", stepName, duration, totalSize)));

        return result;
    }


    // Updated displayStatsReport method
    public static void displayStatsReport() {
        int stepWidth = 35;
        int timeWidth = 12;
        int countWidth = 10; // For file count

        // Header
        String separatorRow = String.format("%1$" + (stepWidth + timeWidth + countWidth + 8) + "s", "|").replace(' ', '-');
        System.out.println(separatorRow);
        System.out.printf("| %" + (stepWidth - 1) + "s | %" + timeWidth + "s | %" + countWidth + "s |\n",
                "Process Step", "Time (ms)", "Count");
        System.out.println(separatorRow);

        // Rows
        stepStatsList.forEach((stat) -> System.out.printf("| %-" + (stepWidth - 1) + "s | %" + timeWidth + "d | %" + countWidth + "d |\n",
                stat.step(), stat.timeTakenMillis(), stat.itemCount()));
        System.out.println(separatorRow);
    }

}
