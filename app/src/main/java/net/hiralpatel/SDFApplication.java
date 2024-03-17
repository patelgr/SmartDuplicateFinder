package net.hiralpatel;

import net.hiralpatel.duplication.DuplicateFinder;
import net.hiralpatel.monitoring.EventPublisher;
import net.hiralpatel.monitoring.Events;
import net.hiralpatel.monitoring.MonitoringClient;
import net.hiralpatel.monitoring.SocketEventPublisher;
import net.hiralpatel.ui.ReportGenerator;
import net.hiralpatel.ui.ReportGeneratorBySize;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class SDFApplication {
    public static void main(String[] args) {
        LocalDateTime started = LocalDateTime.now();
        System.out.println("Started: " + started);
        String mode = System.getProperty("mode"); // Check JVM property
        System.out.println("Mode:" + mode);

        if ("client".equals(mode)) {
            MonitoringClient.runClient();
        } else {
            System.out.println("Mode:server");
            EventPublisher publisher = EventPublisher.INSTANCE;
//            List<Path> directoryPaths = List.of(Paths.get("/Volumes/Seagate5tb/Documents"),Paths.get("/Users/gaurav/Documents/Home"));
//            List<Path> directoryPaths = List.of(Paths.get("/Volumes/Seagate5tb/Documents"));
            List<Path> directoryPaths = List.of(Paths.get("/Volumes/Seagate5tb/Documents/Pictures/Old"));
            DuplicateFinder finder = new DuplicateFinder();
            try (SocketEventPublisher socketEventPublisher = new SocketEventPublisher(5000)) {
                publisher.addSubscriber(socketEventPublisher); // Correctly adds as a Subscriber

                // Use Events utility class for creating Event objects
                publisher.publishEvent(Events.InfoEvent("Application started"));
                Map<String, List<Path>> duplicates = finder.findDuplicates(directoryPaths);

                // Generate and output report
                ReportGeneratorBySize.generateReport(duplicates);
                ReportGenerator.generateReport(duplicates);
                publisher.publishEvent(Events.InfoEvent("Processing completed"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            LocalDateTime ended = LocalDateTime.now();
            System.out.println("Ended: " + ended + ",TimeTaken" + Duration.between(started, ended));
        }
    }



    private static List<Path> parseArguments(String[] args) {
        String mode = null;
        List<Path> directoryPaths = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-mode":
                    if (i + 1 < args.length) {
                        mode = args[++i];
                    } else {
                        throw new IllegalArgumentException("Mode not specified.");
                    }
                    break;
                case "-paths":
                    if (i + 1 < args.length) {
                        String[] paths = args[++i].split(",");
                        for (String path : paths) {
                            directoryPaths.add(Paths.get(path.trim()));
                        }
                    } else {
                        throw new IllegalArgumentException("Paths not specified.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        if (mode == null || mode.isEmpty()) {
            throw new IllegalArgumentException("Mode is required.");
        }

        if (directoryPaths.isEmpty()) {
            throw new IllegalArgumentException("At least one path is required.");
        }

        return directoryPaths;
    }
}
