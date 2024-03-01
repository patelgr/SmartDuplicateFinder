package net.hiralpatel.monitoring;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketEventPublisher implements Subscriber, Closeable {
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final PrintWriter out;
    private final ExecutorService executor; // Executor for asynchronous task execution

    public SocketEventPublisher(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Listening for monitoring client on port " + port);
        clientSocket = serverSocket.accept(); // Waits until a client connects
        System.out.println("Monitoring client connected");
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        executor = Executors.newCachedThreadPool(); // Initialize the executor
    }

    @Override
    public void handleEvent(String event) {
        executor.submit(() -> {
            out.println(event); // Send the event to the monitoring client asynchronously
        });
    }

    @Override
    public void close() throws IOException {
        try {
            executor.shutdown(); // Shutdown the executor
            // Ensure all pending tasks are completed before closing resources
            while (!executor.isTerminated()) {
                // Wait for all tasks to finish
            }
        } finally {
            // Close resources in reverse order of their creation
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
