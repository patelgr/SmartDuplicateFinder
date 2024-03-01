package net.hiralpatel.service;

import net.hiralpatel.duplication.DuplicateFinder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DuplicateFinderServiceTestItem {
    private static Path testRoot;
    private static DuplicateFinder duplicateFinderService;
    private static TestData testData;

    //    @BeforeAll
//    static void setUp(@TempDir Path tempDir) throws Exception {
//        testRoot = tempDir;
//        duplicateFinderService = new DuplicateFinder();
//        createTestEnvironment();
//    }
    @BeforeAll
    static void setUp() throws Exception {
        // Define the date-time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        // Format the current date and time according to the specified format
        String formattedDateTime = now.format(formatter);
        // Set the testRoot with the formatted date and time
        testRoot = Path.of("test/" + formattedDateTime + "/").toAbsolutePath();
        duplicateFinderService = new DuplicateFinder();
        createTestEnvironment();
    }

    private static void createTestEnvironment() throws IOException {
        try (InputStream inputStream = DuplicateFinderServiceTestItem.class.getClassLoader().getResourceAsStream("test-data.yml")) {
            if (inputStream == null) {
                throw new IOException("Resource file not found: test-data.yml");
            }

            // Read the content of the stream into a String
            String yamlContent = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Initialize YAML parser with LoaderOptions
            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setAllowDuplicateKeys(false);
            TagInspector tagInspector = tag -> tag.getClassName().equals(TestData.class.getName());
            loaderOptions.setTagInspector(tagInspector);
            Yaml yaml = new Yaml(new Constructor(TestData.class, loaderOptions));

            // Parse the YAML content
            testData = yaml.load(yamlContent);

            // Proceed with processing testData...
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
        }
        if (testData == null) {
            throw new IllegalStateException("Failed to load test data from YAML file. The file may be empty or improperly formatted.");
        }
        for (TestItem scenario : testData.getTests()) {
            if (scenario.getFiles() != null) {
                for (TestFile file : scenario.getFiles()) {
                    createFileWithContent(testRoot, file.getPath(), file.getContent());
                }
            }

            if (scenario.getStructures() != null) {
                TestStructures structure = scenario.getStructures();
                for (TestFile file : structure.getOriginal()) {
                    createFileWithContent(testRoot, file.getPath(), file.getContent());
                }
                for (TestFile file : structure.getDuplicate()) {
                    createFileWithContent(testRoot, file.getPath(), file.getContent());
                }
            }
        }
    }

    private static void createFileWithContent(Path root, String relativePath, String content) throws IOException {
        Path filePath = root.resolve(relativePath);
        System.out.println("Creating file at: " + filePath.toAbsolutePath());
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, content);
    }

    @AfterAll
    static void tearDown() throws IOException {
        deleteDirectoryRecursively(testRoot);
        if (Files.exists(testRoot)) {
            System.out.println("Directory Not Deleted");
            throw new IOException("Failed to delete the test directory during cleanup.");
        } else {
            System.out.println("Deleted All Files");
        }
    }

    private static void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }
    }

    @Test
    public void testUniqueFiles() {
        Arrays.stream(testData.getTests()).filter(a -> a.getType().toLowerCase().contains("duplicate")).toList();
        assertNotEquals(1, 2, "This is suppose to Fail");
    }

    @Test
    public void testExtraDuplicateFiles() {
        List<TestItem> testItems = Arrays.stream(testData.getTests()).filter(a -> a.getType().toLowerCase().contains("duplicate")).toList();
        for (TestItem testScenario : testItems) {
            Map<Long, List<Path>> duplicates = duplicateFinderService.findDuplicates(List.of(testRoot));
            List<Path> fileResults = duplicates.values().stream()
                    .flatMap(Collection::stream)
                    .map(Path::toAbsolutePath)
                    .toList();

            // Convert expectedResult String[] to List<Path>
            List<Path> expectedPaths = Arrays.stream(testScenario.getExpectedResult())
                    .map(Paths::get)
                    .map(testRoot::resolve) // Resolve against testRoot to get absolute paths
                    .map(Path::toAbsolutePath)
                    .toList();


            // Optionally, assert that the service did not find any extra paths not expected as duplicates
            assertTrue(expectedPaths.containsAll(fileResults),
                    "Found unexpected duplicates. Extra: " + fileResults.stream()
                            .filter(path -> !expectedPaths.contains(path))
                            .map(Path::toString)
                            .collect(Collectors.joining(", ")));
        }
    }

    @Test
    public void testMissingDuplicateFiles() {
        List<TestItem> testItems = Arrays.stream(testData.getTests()).filter(a -> a.getType().toLowerCase().contains("duplicate")).toList();
        for (TestItem testScenario : testItems) {
            Map<Long, List<Path>> duplicates = duplicateFinderService.findDuplicates(List.of(testRoot));
            List<Path> fileResults = duplicates.values().stream()
                    .flatMap(Collection::stream)
                    .map(Path::toAbsolutePath)
                    .toList();

            // Convert expectedResult String[] to List<Path>
            List<Path> expectedPaths = Arrays.stream(testScenario.getExpectedResult())
                    .map(Paths::get)
                    .map(testRoot::resolve) // Resolve against testRoot to get absolute paths
                    .map(Path::toAbsolutePath)
                    .toList();

            // Assert that all expected duplicate paths are found by the service
            assertTrue(fileResults.containsAll(expectedPaths),
                    "Not all expected duplicates were found. Missing: " + expectedPaths.stream()
                            .filter(path -> !fileResults.contains(path))
                            .map(Path::toString)
                            .collect(Collectors.joining(", ")));
        }
    }

    @Test
    void testFileCreation() throws Exception {
        Path testFilePath = testRoot.resolve(testData.getTests()[0].getFiles()[0].getPath());
        assertTrue(Files.exists(testFilePath), "Test file should exist after setup");
    }

}
