/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package net.hiralpatel.cli;

import net.hiralpatel.model.Directory;
import net.hiralpatel.service.DuplicateFinderService;

import java.time.LocalDateTime;

public class AppCli {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: AppCli <directory_path>");
            System.exit(1);
        }

        DuplicateFinderService service = new DuplicateFinderService();
        System.out.println(LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        service.findAndDisplayDuplicates(args);
        long endTime = System.currentTimeMillis();
        System.out.println("timetaken:" + (endTime - startTime)/1000);
        System.out.println(LocalDateTime.now());
    }

}