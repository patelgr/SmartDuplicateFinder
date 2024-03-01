package net.hiralpatel.analytics;

import java.util.Map;

public class StepStats {
    private final long timeTakenMillis;
    private final long itemCount;

    // Constructor
    public StepStats(long timeTakenMillis, long itemCount) {
        this.timeTakenMillis = timeTakenMillis;
        this.itemCount = itemCount;
    }

    // Updated displayStatsReport method
    public static void displayStatsReport(Map<String, StepStats> stats) {
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
        stats.forEach((step, stat) -> System.out.printf("| %-" + (stepWidth - 1) + "s | %" + timeWidth + "d | %" + countWidth + "d |\n",
                step, stat.getTimeTakenMillis(), stat.getItemCount()));
        System.out.println(separatorRow);
    }

    public long getTimeTakenMillis() {
        return timeTakenMillis;
    }

    public long getItemCount() {
        return itemCount;
    }
}
