package net.hiralpatel.monitoring;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketEventPublisher implements Subscriber, Closeable {
    private final ServerSocket serverSocket;
    private final ExecutorService clientHandlerExecutor = Executors.newCachedThreadPool();
    private final Map<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    // Variables to store the last two messages
    private String lastMessage = null;
    private String secondLastMessage = null;
    private final Object messageLock = new Object(); // Lock for synchronizing access to the last two messages

    public SocketEventPublisher(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Listening for monitoring client on port " + port);

        new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    System.out.println("Monitoring client connected: " + clientSocket.getRemoteSocketAddress());

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientWriters.put(clientSocket, writer);

                    // Send the last two messages to the new client
                    synchronized (messageLock) {
                        if (secondLastMessage != null) {
                            writer.println(secondLastMessage);
                            writer.flush();
                        }
                        if (lastMessage != null) {
                            writer.println(lastMessage);
                            writer.flush();
                        }
                    }

                    clientHandlerExecutor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.out.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try {
            clientSocket.setSoTimeout(10000); // Optional: set a timeout for client read operations
            while (!clientSocket.isClosed() && clientSocket.isConnected()) {
                // Keep the connection alive; handle incoming messages if needed
            }
        } catch (IOException e) {
            System.err.println("Error with client connection: " + e.getMessage());
        } finally {
            clientWriters.remove(clientSocket);
            System.out.println("Monitoring client disconnected: " + clientSocket.getRemoteSocketAddress());
        }
    }

    @Override
    public void handleEvent(Event event) {
        // Convert the event to a string representation, for example:
        String eventString = "Level: " + event.logLevel() + " - Data: " + event.eventData();

        synchronized (messageLock) {
            secondLastMessage = lastMessage; // Shift the last message to second last
            lastMessage = eventString; // Update the last message with the new event
        }

        clientWriters.forEach((socket, writer) -> {
            writer.println(eventString); // Send the event to all connected clients
            writer.flush();
        });
    }

    @Override
    public boolean isInterestedIn(LogLevel logLevel) {
        // For simplicity, this subscriber is interested in all log levels
        // You can add more complex logic here if needed
        return true;
    }

    @Override
    public void close() throws IOException {
        try {
            clientHandlerExecutor.shutdownNow();
            serverSocket.close();
            clientWriters.forEach((socket, writer) -> {
                writer.close();
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            });
        } finally {
            // Ensure server socket is closed
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
}
