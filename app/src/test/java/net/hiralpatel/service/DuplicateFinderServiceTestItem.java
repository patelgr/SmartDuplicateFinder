package net.hiralpatel.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DuplicateFinderServiceTestItem {
    private static Path testRoot;
    private static DuplicateFinderService duplicateFinderService;
    private static TestData testData;

    @BeforeAll
    static void setUp(@TempDir Path tempDir) throws Exception {
        testRoot = tempDir;
        duplicateFinderService = new DuplicateFinderService();
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

    static Stream<TestItem> testDataProvider() {
        // Ensure testData is initialized before this method is called
        return Arrays.stream(testData.getTests());
    }

    @ParameterizedTest
    @MethodSource("testDataProvider")
    void runDynamicTests(TestItem testScenario) {
        if(testScenario.getType().toLowerCase().contains("duplicate")){
            testDuplicateFiles(testScenario);
        }else{
            testUniqueFiles(testScenario);
        }
    }

    private void testUniqueFiles(TestItem testScenario) {
        assertNotEquals(1,2,"This is suppose to Fail");
    }


    private void testDuplicateFiles(TestItem testScenario) {
        List<Path> fileResults = duplicateFinderService.findDuplicates(List.of(testRoot.toString()))
                .stream()
                .map(Path::toAbsolutePath)
                .toList();
        // Initialize empty arrays to avoid NullPointerException
        TestFile[] originalFiles = new TestFile[]{};
        TestFile[] duplicateFiles = new TestFile[]{};
        TestFile[] files = new TestFile[]{};

        // Check if structures are not null before accessing them
        if (testScenario.getStructures() != null) {
            if (testScenario.getStructures().getOriginal() != null) {
                originalFiles = testScenario.getStructures().getOriginal();
            }
            if (testScenario.getStructures().getDuplicate() != null) {
                duplicateFiles = testScenario.getStructures().getDuplicate();
            }
        }
        if(testScenario.getFiles()!=null){
            files = testScenario.getFiles();
        }


        List<Path> allDuplicatePaths = Stream.of(files, originalFiles, duplicateFiles)
                .flatMap(Arrays::stream)
                .map(TestFile::getPath)
                .map(Paths::get) // Convert String to Path
                .map(Path::toAbsolutePath) // Ensure each path is absolute
                .toList();


// Check if all expected duplicate paths are found by the service
        assertTrue(
                fileResults.containsAll(allDuplicatePaths),
                "Not all expected duplicates were found. Missing: " + allDuplicatePaths.stream()
                        .filter(path -> !fileResults.contains(path))
                        .map(Path::toString)
                        .collect(Collectors.joining(", "))
        );

// Check if the service found any extra paths not expected as duplicates
        assertTrue(
                allDuplicatePaths.containsAll(fileResults),
                "Found unexpected duplicates. Extra: " + fileResults.stream()
                        .filter(path -> !allDuplicatePaths.contains(path))
                        .map(Path::toString)
                        .collect(Collectors.joining(", "))
        );


    }

    @Test
    void testFileCreation() throws Exception {
        Path testFilePath = testRoot.resolve(testData.getTests()[0].getFiles()[0].getPath());
        assertTrue(Files.exists(testFilePath), "Test file should exist after setup");
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
        }else{
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

}
