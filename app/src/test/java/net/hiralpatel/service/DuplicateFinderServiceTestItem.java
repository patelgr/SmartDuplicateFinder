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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DuplicateFinderServiceTestItem {
    private static Path testRoot;
    private static DuplicateFinderService duplicateFinderService;
    private static Data testData;

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
            TagInspector tagInspector = tag -> tag.getClassName().equals(Data.class.getName());
            loaderOptions.setTagInspector(tagInspector);
            Yaml yaml = new Yaml(new Constructor(Data.class, loaderOptions));

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
                Structures structure = scenario.getStructures();
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
        switch (testScenario.getType()) {
            case "unique":
                testUniqueFiles(testScenario);
                break;
            case "file-level-duplicate":
                testFileLevelDuplicates(testScenario);
                break;
            case "directory-level-duplicate":
                testDirectoryLevelDuplicates(testScenario);
                break;
            case "multi-level-structure-duplicate":
                testMultiLevelDirectoryDuplicates(testScenario);
                break;
            default:
                throw new IllegalArgumentException("Unknown test type: " + testScenario.getType());
        }
    }

    private void testMultiLevelDirectoryDuplicates(TestItem testScenario) {
    }

    private void testDirectoryLevelDuplicates(TestItem testScenario) {
    }

    private void testFileLevelDuplicates(TestItem testScenario) {
    }

    private void testUniqueFiles(TestItem testScenario) {
        List<FileResult> fileResults = duplicateFinderService.findDuplicates(List.of(testRoot.toString()));

        List<Map<Long, List<Path>>> uniqueFilesList = fileResults.stream().filter(fileResult -> fileResult instanceof UniqueFileGroup).map(FileResult::getFiles).toList();
        assertEquals(1,uniqueFilesList.size());
        Map<Long, List<Path>> uniqueFilesMap = uniqueFilesList.get(0);
        // Assert that the files expected to be unique are correctly identified
        // Assuming each 'File' object in the scenario has a 'shouldBeUnique' boolean or similar
        for (TestFile file : testScenario.getFiles()) {
            String path = file.getPath();
            Path filePath = testRoot.resolve(path);
            List<Path> list = uniqueFilesMap.values().stream()
                    .flatMap(List::stream).toList();
            boolean mapContainsFile = list.stream()
                    .anyMatch(getPathPredicate(filePath));
            assertTrue(mapContainsFile
                    ,"File expected to be unique was not identified as such: " + path);
        }
    }

    private static Predicate<Path> getPathPredicate(Path filePath) {
        return currentFile -> {
            try {
                return Files.isSameFile(currentFile, filePath);
            } catch (IOException e) {
                return false;
            }
        };
    }

    @Test
    void testFileCreation() throws Exception {
//         Assuming your YAML configuration specifies a file at this path
        Path testFilePath = testRoot.resolve(testData.getTests()[0].getFiles()[0].getPath());

        // Check if the file exists
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
