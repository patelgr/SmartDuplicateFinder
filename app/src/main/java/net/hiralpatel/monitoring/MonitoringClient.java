package net.hiralpatel.monitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonitoringClient {

    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        runClient();
    }

    public static void runClient() {
        String host = "localhost";
        int port = 5000;

        // Start the message logging task
        startLoggingTask();

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String eventMessage;
            while ((eventMessage = in.readLine()) != null) {
                // Update the queue to hold only the most recent message
                messageQueue.clear(); // Clear the queue to discard all but the most recent message
                messageQueue.offer(eventMessage); // Offer the latest incoming message to the queue
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Shutdown the scheduler gracefully
            scheduler.shutdown();
        }
    }

    private static void startLoggingTask() {
        // Schedule a task to log the most recent message from the queue at fixed intervals
        scheduler.scheduleAtFixedRate(() -> {
            String lastMessage = null;
            // Keep polling the queue for the most recent message
            while (!messageQueue.isEmpty()) {
                lastMessage = messageQueue.poll();
            }
            if (lastMessage != null) {
                System.out.println(LocalDateTime.now() + " - " + lastMessage);
            }
        }, 0, 3, TimeUnit.SECONDS); // Adjust the third parameter for the desired throttle period (e.g., 3 seconds)
    }
}
