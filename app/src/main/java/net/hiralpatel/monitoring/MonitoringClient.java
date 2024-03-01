package net.hiralpatel.monitoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MonitoringClient {
    public static void main(String[] args) {
        runClient();
    }

    public static void runClient() {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String eventMessage;
            while ((eventMessage = in.readLine()) != null) {
                System.out.println("Event: " + eventMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
